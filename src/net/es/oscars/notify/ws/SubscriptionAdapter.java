package net.es.oscars.notify.ws;

import java.util.*;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.namespace.QName;
import org.apache.log4j.*;
import org.oasis_open.docs.wsn.b_2.*;
import org.oasis_open.docs.wsn.br_2.*;
import org.w3.www._2005._08.addressing.*;
import org.apache.axis2.databinding.ADBException;
import org.apache.axis2.databinding.types.URI;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.hibernate.*;
import org.jaxen.JaxenException;
import org.jaxen.SimpleNamespaceContext;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.notify.*;
import net.es.oscars.client.Client;
import net.es.oscars.PropHandler;
import net.es.oscars.notify.ws.policy.*;

import org.apache.axis2.databinding.utils.writer.MTOMAwareXMLSerializer;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

/** 
 * SubscriptionAdapter provides a translation layer between Axis2 and Hibernate. 
 * It is intended to provide a gateway for Axis2 into more general core functionality
 * of the notification broker.
 *
 * @author Andrew Lake (alake@internet2.edu)
 */
public class SubscriptionAdapter{
    private Logger log;
    private String subscriptionManagerURL;
    private HashMap<String,String> namespaces;
    private String dbname;
    private String repo;
    
    /** Default constructor */
    public SubscriptionAdapter(String dbname){
        this.log = Logger.getLogger(this.getClass());
        this.dbname = dbname;
        String catalinaHome = System.getProperty("catalina.home");
        // check for trailing slash
        if (!catalinaHome.endsWith("/")) {
            catalinaHome += "/";
        }
        this.repo = catalinaHome + "shared/classes/repo/";
        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties props = propHandler.getPropertyGroup("notifybroker", true); 
        this.subscriptionManagerURL = props.getProperty("url");
        if(this.subscriptionManagerURL == null){
            String localhost = null;
            try{
                localhost = InetAddress.getLocalHost().getHostName();
            }catch(Exception e){
                this.log.error("Please set 'notifybroker.url' in oscars.properties!");
            }
            this.subscriptionManagerURL = "https://" + localhost + ":8443/axis2/services/OSCARSNotify";
        }
        this.log.info("OSCARSNotify.url=" + this.subscriptionManagerURL);
        
        //TODO: Loads namespace prefixes from properties file
        this.namespaces = new HashMap<String,String>();
        this.namespaces.put("idc", "http://oscars.es.net/OSCARS");
        this.namespaces.put("nmwg-ctrlp", "http://ogf.org/schema/network/topology/ctrlPlane/20070626/");
        this.namespaces.put("wsa", "http://www.w3.org/2005/08/addressing");
    }
    
    /**
     * Creates a new subscription based on the parameters of the request. It also adds
     * entries in its database to make sure the subscriber only gets notifications it is
     * authorized to see. 
     * 
     * @param request the Axis2 object with the Subscribe request information
     * @userLogin the login of the subscriber
     * @permissionMap A hash containing certain authorization constraints for the subscriber
     * @return an Axis2 object with the result of the subscription creation
     * @throws InvalidFilterFault
     * @throws InvalidMessageContentExpressionFault
     * @throws InvalidProducerPropertiesExpressionFault
     * @throws InvalidTopicExpressionFault
     * @throws TopicExpressionDialectUnknownFault
     * @throws TopicNotSupportedFault
     * @throws UnacceptableInitialTerminationTimeFault
     */
    public SubscribeResponse subscribe(Subscribe request, String userLogin, HashMap<String,String> permissionMap)
        throws TopicExpressionDialectUnknownFault, InvalidTopicExpressionFault,TopicNotSupportedFault,
               InvalidProducerPropertiesExpressionFault,InvalidFilterFault,InvalidMessageContentExpressionFault,
               UnacceptableInitialTerminationTimeFault{
        this.log.info("subscribe.start");
        SubscriptionManager sm = new SubscriptionManager(this.dbname);
        Subscription subscription = this.axis2Subscription(request, userLogin);
        SubscribeResponse response = null;
        ArrayList<SubscriptionFilter> filters = new ArrayList<SubscriptionFilter>();
        FilterType requestFilter = request.getFilter();
        QueryExpressionType[] producerPropsFilters = null;
        QueryExpressionType[] messageContentFilters =  null;
        TopicExpressionType[] topicFilters = null;
        if(requestFilter != null){
            producerPropsFilters = requestFilter.getProducerProperties();
            messageContentFilters = requestFilter.getMessageContent();
            topicFilters = requestFilter.getTopicExpression();
        }
        
        /* Add filters */
        if(permissionMap.containsKey("loginConstraint")){
            String constraint = permissionMap.get("loginConstraint");
            filters.add(new SubscriptionFilter("USERLOGIN", constraint));
        }else{
            filters.add(new SubscriptionFilter("USERLOGIN", "ALL"));
        }
        
        if(permissionMap.containsKey("institution")){
            String constraint = permissionMap.get("institution");
            filters.add(new SubscriptionFilter("INSTITUTION", constraint));
        }
        
        /* TODO: Add constraint on which producers can be seen. It should be 
           handled in a similar way as above where a producer or list of 
           producers is passed in a HashMap */
        producerPropsFilters = (producerPropsFilters == null) ? new QueryExpressionType[0] : producerPropsFilters;
        for(QueryExpressionType producerPropsFilter : producerPropsFilters){
            if(this.validateQueryExpression(producerPropsFilter, true)){
                String xpath = producerPropsFilter.getString();
                filters.add(new SubscriptionFilter("PRODXPATH", xpath));    
            }
        }
        
        messageContentFilters = (messageContentFilters == null) ? new QueryExpressionType[0] : messageContentFilters;
        for(QueryExpressionType messageContentFilter : messageContentFilters){
            if(this.validateQueryExpression(messageContentFilter, false)){
                String xpath = messageContentFilter.getString();
                filters.add(new SubscriptionFilter("MSGXPATH", xpath));    
            }
        }
        
        ArrayList<String> topics = this.parseTopics(topicFilters);
        boolean explicitTopics = false;
        for(String topic : topics){
             filters.add(new SubscriptionFilter("TOPIC", topic.trim()));
             explicitTopics = true;
        }
        if(!explicitTopics){
             filters.add(new SubscriptionFilter("TOPIC", "ALL"));
        }
        
        Session sess = HibernateUtil.getSessionFactory(this.dbname).getCurrentSession();
        sess.beginTransaction();
        try{
            subscription = sm.subscribe(subscription, filters);
            response = this.subscription2Axis(subscription);
        }catch(UnacceptableInitialTerminationTimeFault e){
            sess.getTransaction().rollback();
            throw e;
        }
        sess.getTransaction().commit();
        
        this.log.info("subscribe.end");
        return response;
    }
    
    /**
     * Forwards notfications to appropriate subscribers.
     *
     * @param request the notify message to forward
     */
    public void notify(NotificationMessageHolderType holder,
                       HashMap<String, ArrayList<String>> permissionMap)
                       throws ADBException,
                              InvalidTopicExpressionFault,
                              JaxenException,
                              TopicExpressionDialectUnknownFault{
        this.log.info("notify.start");
        SubscriptionManager sm = new SubscriptionManager(this.dbname);
        TopicExpressionType topicExpr = holder.getTopic();
        TopicExpressionType[] topicExprs = {topicExpr};
        ArrayList<String>topics = this.parseTopics(topicExprs);
        ArrayList<String> parentTopics = new ArrayList<String>();
        List<Subscription> authSubscriptions = null;
        EndpointReferenceType producerRef = holder.getProducerReference();
        MessageType message = holder.getMessage();
        SimpleNamespaceContext nsContext= new SimpleNamespaceContext(this.namespaces);
        OMFactory omFactory = (OMFactory) OMAbstractFactory.getOMFactory();
        OMElement omProducerRef = null;
        OMElement omMessage = message.getOMElement(NotificationMessage.MY_QNAME, omFactory);
        if(producerRef != null){
            omProducerRef = producerRef.getOMElement(NotificationMessage.MY_QNAME, omFactory);
        }
        
        //add all parent topics
        for(String topic : topics){
            String[] topicParts = topic.split("\\/");
            String topicString = "";
            for(int i = 0; i < (topicParts.length - 1); i++){
                topicString += topicParts[i];
                parentTopics.add(topicString);
                topicString += "/";
            }
        }
        topics.addAll(parentTopics);
        topics.add("ALL");
        permissionMap.put("TOPIC", topics);
        
        Session sess = HibernateUtil.getSessionFactory("notify").getCurrentSession();
        sess.beginTransaction();
        //find all subscriptions that match this topic and havethe necessaru authorizations
        authSubscriptions = sm.findSubscriptions(permissionMap);
        //apply producer and message XPATH filters
        for(Subscription authSubscription : authSubscriptions){
            this.log.debug("Applying filters for " + authSubscription.getReferenceId());
            Set filters = authSubscription.getFilters();
            Iterator i = filters.iterator();
            boolean matches = true;
            while(i.hasNext() && matches){
                SubscriptionFilter filter = (SubscriptionFilter) i.next();
                String type = filter.getType();
                try{
                    if("PRODXPATH".equals(type) && omProducerRef != null){
                        this.log.debug("Found producer filter: " + filter.getValue());
                        AXIOMXPath xpath = new AXIOMXPath(filter.getValue());
                        xpath.setNamespaceContext(nsContext);
                        matches = xpath.booleanValueOf(omProducerRef);
                        this.log.debug(matches ? "Filter matches." : "No Match");
                    }else if("MSGXPATH".equals(type)){
                        this.log.debug("Found message filter: " + filter.getValue());
                        AXIOMXPath xpath = new AXIOMXPath(filter.getValue());
                        xpath.setNamespaceContext(nsContext);
                        matches = xpath.booleanValueOf(omMessage);
                        this.log.debug(matches ? "Filter matches." : "No Match");
                    }
                }catch(JaxenException e){
                    sess.getTransaction().rollback();
                    throw e;
                }
            }
            if(matches){
                this.sendNotify(holder, authSubscription);
            } 
        }
        sess.getTransaction().commit();
        this.log.info("notify.end");
    }
    
    public void sendNotify(NotificationMessageHolderType holder, Subscription subscription){
        this.log.debug("sendNotify.start");
        Client client = new Client();
        String url = subscription.getUrl();
        
        try{
            EndpointReferenceType subRef = client.generateEndpointReference(
                   this.subscriptionManagerURL, subscription.getReferenceId());
            holder.setSubscriptionReference(subRef);
            client.setUpNotify(true, url, this.repo, this.repo + "axis2-norampart.xml");
            /* XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(System.out);
            MTOMAwareXMLSerializer mtom = new MTOMAwareXMLSerializer(writer);
            holder.serialize(Notify.MY_QNAME, OMAbstractFactory.getOMFactory(), mtom);
            mtom.flush(); */
            client.notify(holder);
        }catch(Exception e){
            this.log.info("Error sending notification: " + e);
        }
        this.log.debug("sendNotify.end");
    }
    
    /**
     * Registers a publisher in the database using the given registration parameters
     *
     * @param request the registration request
     * @param login the login of the user that send the registration
     * @return an Axis2 RegisterPublisherResponse withthe result of the registration
     * @throws UnacceptableInitialTerminationTimeFault
     * @throws PublisherRegistrationFailedFault
     */
    public RegisterPublisherResponse registerPublisher(RegisterPublisher request, String login)
                                throws UnacceptableInitialTerminationTimeFault,
                                       PublisherRegistrationFailedFault{
        this.log.info("registerPublisher.start");
        SubscriptionManager sm = new SubscriptionManager(this.dbname);
        RegisterPublisherResponse response = null;
        Publisher publisher = new Publisher();
        String publisherAddress = this.parseEPR(request.getPublisherReference());
        Calendar initTermTime = request.getInitialTerminationTime();
        boolean demand = request.getDemand();
        
        publisher.setUserLogin(login);
        publisher.setUrl(publisherAddress);
        if(initTermTime != null){
            publisher.setTerminationTime(initTermTime.getTimeInMillis()/1000L);
        }else{
            publisher.setTerminationTime(0L);
        }
        
        //TODO:Support demand based publishing
        if(demand){
            throw new PublisherRegistrationFailedFault("Demand publishing is not supported by this implementation.");
        }
        publisher.setDemand(demand);
        
        Session sess = HibernateUtil.getSessionFactory(this.dbname).getCurrentSession();
        sess.beginTransaction();
        try{
            publisher = sm.registerPublisher(publisher);
            response = this.publisher2Axis(publisher);
        }catch(UnacceptableInitialTerminationTimeFault e){
            sess.getTransaction().rollback();
            throw e;
        }
        this.log.info("registerPublisher.end");
        sess.getTransaction().commit();
        
        return response;
    }
    
    /** 
     * Converts and Axis2 Subscribe object to a Subsciption Hibernate bean
     *
     * @param the Subscribe object to convert
     * @param userLogin the login of the subscriber that sent the request
     * @return the Hibernate Bean generate from the original request
     */
    private Subscription axis2Subscription(Subscribe request, String userLogin)
                                throws UnacceptableInitialTerminationTimeFault{
        Subscription subscription = new Subscription();
        String consumerAddress = this.parseEPR(request.getConsumerReference());
        long initTermTime = 
                this.parseInitTermTime(request.getInitialTerminationTime());
        
        subscription.setUrl(consumerAddress);
        subscription.setUserLogin(userLogin);
        subscription.setTerminationTime(initTermTime);
        
        return subscription;
    }
    
    /**
     * Converts a complete Subscription Hibernate bean to an Axis2 object
     *
     * @param subscription the Subscription Hibernate bean to convert
     * @return the Axis2 SubscribeResponse converted from the Subscribe object
     */
    private SubscribeResponse subscription2Axis(Subscription subscription){
        SubscribeResponse response = new SubscribeResponse();
        
        /* Set subscription reference */
		EndpointReferenceType subRef = new EndpointReferenceType();
        AttributedURIType subAttrUri = new AttributedURIType();
        try{
            URI subRefUri = new URI(this.subscriptionManagerURL);
            subAttrUri.setAnyURI(subRefUri);
        }catch(Exception e){}
        subRef.setAddress(subAttrUri);
        //set ReferenceParameters
        ReferenceParametersType subRefParams = new ReferenceParametersType();
        subRefParams.setSubscriptionId(subscription.getReferenceId());
        subRef.setReferenceParameters(subRefParams);
		
		/* Convert creation and termination time to Calendar object */
		GregorianCalendar createCal = new GregorianCalendar();
		GregorianCalendar termCal = new GregorianCalendar();
		createCal.setTimeInMillis(subscription.getCreatedTime() * 1000);
		termCal.setTimeInMillis(subscription.getTerminationTime() * 1000);
		
		response.setSubscriptionReference(subRef);
		response.setCurrentTime(createCal);
		response.setTerminationTime(termCal);
		
		return response;
    }
    
    /**
     * Converts a complete Publisher Hibernate bean to an Axis2 object
     *
     * @param publisher the Publisher Hibernate bean to convert
     * @return the Axis2 RegisterPublisherResponse converted from the Publisher object
     */
    private RegisterPublisherResponse publisher2Axis(Publisher publisher){
        RegisterPublisherResponse response = new RegisterPublisherResponse();
        
        /* Set publisher reference */
		EndpointReferenceType pubRef = new EndpointReferenceType();
        AttributedURIType pubAttrUri = new AttributedURIType();
        try{
            URI pubRefUri = new URI(this.subscriptionManagerURL);
            pubAttrUri.setAnyURI(pubRefUri);
        }catch(Exception e){}
        pubRef.setAddress(pubAttrUri);
        //set ReferenceParameters
        ReferenceParametersType pubRefParams = new ReferenceParametersType();
        pubRefParams.setPublisherRegistrationId(publisher.getReferenceId());
        pubRef.setReferenceParameters(pubRefParams);
		
		/* Set consumer reference */
		EndpointReferenceType conRef = new EndpointReferenceType();
        AttributedURIType conAttrUri = new AttributedURIType();
        try{
            URI conRefUri = new URI(this.subscriptionManagerURL);
            conAttrUri.setAnyURI(conRefUri);
        }catch(Exception e){}
        conRef.setAddress(conAttrUri);
        
		response.setPublisherRegistrationReference(pubRef);
        response.setConsumerReference(conRef);
        
		return response;
    }
    
    /**
     * Parse a TopicExpression to make sure it is in a known Dialect.
     * It also splits topics into multiple topics. Currently this method
     * supports the SimpleTopic, ConcreteTopic, and FullTopic(partially)
     * specifications. 
     *
     * @param topicFilter the TopicExpression to parse
     * @return an array of strings containing each Topic
     * @throws InvalidTopicExpressionFault
     * @throws TopicExpressionDialectUnknownFault
     */
    private ArrayList<String> parseTopics(TopicExpressionType[] topicFilters) 
            throws TopicExpressionDialectUnknownFault,
                   InvalidTopicExpressionFault{
        if(topicFilters == null || topicFilters.length < 1){
            return new ArrayList<String>(0);
        }
        
        ArrayList<String> topics = new ArrayList<String>();
        for(TopicExpressionType topicFilter : topicFilters){
            if(topicFilter == null){
                continue;
            }
            String dialect = topicFilter.getDialect().toString();
            String topicString = topicFilter.getString();
            
            //check dialect
            if(Client.XPATH_URI.equals(dialect)){
                 throw new TopicExpressionDialectUnknownFault("The XPath Topic " +
                            "Expression dialect is not supported at this time.");
            }else if(!(Client.WS_TOPIC_SIMPLE.equals(dialect) || 
                       Client.WS_TOPIC_CONCRETE.equals(dialect) || 
                       Client.WS_TOPIC_FULL.equals(dialect))){
                throw new TopicExpressionDialectUnknownFault("Unknown Topic dialect '" + dialect + "'");
            }
            
            if(topicString == null || "".equals(topicString)){
                throw new InvalidTopicExpressionFault("Empty topic expression given.");
            }
            String[] topicTokens = topicString.split("\\|");
            for(String topicToken : topicTokens){
                topics.add(topicToken);
            }
            /* NOTE: Currently the notification broker is neutral as to the 
               type of topics is sends/receives so there is no check to see 
               if a topic is supported. This provides the greatest flexibility
               but doesn't allow the broker to return an error if it knows it
               can never send a notification for a particular topic. As we gain
               more experience we can revisit this fact */
        }
        
        return topics;
    }
    
    /**
     * Validates a QueryExpression as those used in the ProducerProperties and
     * MessageContent sections of a Subscribe Filter. Checks to see it is in the 
     * XPath dialect and that a valid XPath expression was provided.
     *
     * @param query the QueryExpression to validate
     * @param isProdProps true if this is a ProducerProperties element. Helps determine what exception to throw.
     * @return true if valid, false if no query exists. Throws an exception otherwise.
     * @throws InvalidFilterFault
     * @throws InvalidMessageContentExpressionFault
     * @throws InvalidProducerPropertiesExpressionFault
     */
    private boolean validateQueryExpression(QueryExpressionType query, boolean isProdProps)
                throws InvalidFilterFault, InvalidProducerPropertiesExpressionFault, 
                       InvalidMessageContentExpressionFault{
        if(query == null){
            return false;
        }
        
        String dialect = query.getDialect().toString();
        if(!Client.XPATH_URI.equals(dialect)){
            throw new InvalidFilterFault("Filter dialect '" + dialect +
                                         "'is not supported by this service.");
        }
        
        String xpath = query.getString();
        try{
            AXIOMXPath axiomXpath = new AXIOMXPath(xpath);
        }catch(Exception e){
            String err = "Invalid expression: " + e;
            if(isProdProps){
                throw new InvalidProducerPropertiesExpressionFault(err);
            }
            throw new InvalidMessageContentExpressionFault(err);
        }
        
        return true;
    }
    
    /**
     *  Utility function that extracts the address from an EndpointReference
     *
     * @param epr the Endpoint Reference to parse
     * @return the address of the parsed EndpointReference
     */
    private String parseEPR(EndpointReferenceType epr){
        AttributedURIType address = epr.getAddress();
        URI uri = address.getAnyURI();
        return uri.toString();
    }
    
    /**
     *  Utility function that extracts an xsd:datetime or xsd:duration from a string
     *
     * @param initTermTime the string to parse
     * @return a timestamp in seconds equivalent to the given string
     * @throws UnacceptableInitialTerminationTimeFault
     */
    private long parseInitTermTime(String initTermTime) 
                                throws UnacceptableInitialTerminationTimeFault{
        /* Parsing initial termination time since Axis2 does not like unions */
        long timestamp = 0L;
        if(initTermTime == null){
            this.log.debug("initTermTime=default");
        }else if(initTermTime.startsWith("P")){
            //duration
            this.log.debug("initTermTime=xsd:duration");
            try{
                DatatypeFactory dtFactory = DatatypeFactory.newInstance();
                Duration dur = dtFactory.newDuration(initTermTime);
                GregorianCalendar cal = new GregorianCalendar();
                dur.addTo(cal);
                timestamp = (cal.getTimeInMillis()/1000L);
            }catch(Exception e){
                throw new UnacceptableInitialTerminationTimeFault("InitialTerminationTime " +
                    "appears to be an invalid xsd:duration value.");
            }
        }else{
            //datetime or invalid
            this.log.debug("initTermTime=xsd:datetime");
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'");
            try{
                Date date = df.parse(initTermTime);
                timestamp = (date.getTime()/1000L);
            }catch(Exception e){
                throw new UnacceptableInitialTerminationTimeFault("InitialTerminationTime " +
                    "must be of type xsd:datetime or xsd:duration.");
            }
        }
        
        return timestamp;
    }
}