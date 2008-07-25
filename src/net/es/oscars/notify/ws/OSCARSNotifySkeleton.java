package net.es.oscars.notify.ws;

import java.util.List;
import java.util.ArrayList;
import java.util.Vector;
import java.security.Principal;
import java.security.cert.X509Certificate;

import org.apache.axis2.context.*;
import org.apache.ws.security.handler.*;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSConstants;
import org.apache.log4j.*;
import org.hibernate.*;

import org.oasis_open.docs.wsn.b_2.*;
import org.oasis_open.docs.wsn.br_2.*;
import net.es.oscars.aaa.AAAException;
import net.es.oscars.aaa.UserManager;
import net.es.oscars.aaa.UserManager.AuthValue;
import net.es.oscars.database.Initializer;
import net.es.oscars.database.HibernateUtil;

/**
 *  OSCARSNotifySkeleton java skeleton for the axisService
 */
public class OSCARSNotifySkeleton implements OSCARSNotifySkeletonInterface{
    private Logger log;
    private UserManager userMgr;
    private Principal certIssuer;
    private Principal certSubject;
    
    /**
     * Called from the Axis2 framework during initialization of the service.
     *
     * If the service has application scope, this method is called when the
     * system starts up. Otherwise it is called when the first request comes.
     *
     * @param sc
     */
    public void init(ServiceContext sc) {
        this.log = Logger.getLogger(this.getClass());
        this.log.info("OSCARSNotify init.start");
        Initializer initializer = new Initializer();
        List<String> dbnames = new ArrayList<String>();
        dbnames.add("aaa");
        dbnames.add("notify");
        initializer.initDatabase(dbnames);
        this.userMgr = new UserManager("aaa");
        this.log.info("OSCARSNotify init.end");
    }
    
	public void Notify(Notify request){
	    this.log.debug("Received a notification message from publisher");
	    SubscriptionAdapter sa = new SubscriptionAdapter();
	    sa.notify(request);
	    return;
	}

	public SubscribeResponse Subscribe(Subscribe request)
           throws AAAFaultMessage, TopicNotSupportedFault,
                  InvalidTopicExpressionFault, UnsupportedPolicyRequestFault,
                  UnacceptableInitialTerminationTimeFault,
                  InvalidMessageContentExpressionFault,
                  InvalidProducerPropertiesExpressionFault,
                  SubscribeCreationFailedFault,
                  TopicExpressionDialectUnknownFault,
                  InvalidFilterFault,NotifyMessageNotSupportedFault,
                  UnrecognizedPolicyRequestFault{
                  
        String login = this.checkUser();          
        SubscriptionAdapter sa = new SubscriptionAdapter();
		SubscribeResponse response = sa.subscribe(request, login);
		
		return response;
	}
    
	public RenewResponse Renew(Renew request)
	    throws UnacceptableTerminationTimeFault{
		//TODO : fill this with the necessary business logic
		throw new  java.lang.UnsupportedOperationException("Please implement " + this.getClass().getName() + "#Renew");
	}

	public UnsubscribeResponse Unsubscribe(Unsubscribe request)
	    throws UnableToDestroySubscriptionFault{
		//TODO : fill this with the necessary business logic
		throw new  java.lang.UnsupportedOperationException("Please implement " + this.getClass().getName() + "#Unsubscribe");
	}

	public PauseSubscriptionResponse PauseSubscription(
	       PauseSubscription request) throws PauseFailedFault{
		//TODO : fill this with the necessary business logic
		throw new  java.lang.UnsupportedOperationException("Please implement " + this.getClass().getName() + "#PauseSubscription");
	}

	public ResumeSubscriptionResponse ResumeSubscription(
	       ResumeSubscription request)
	       throws ResumeFailedFault{
		//TODO : fill this with the necessary business logic
		throw new  java.lang.UnsupportedOperationException("Please implement " + this.getClass().getName() + "#ResumeSubscription");
	}
	
	public RegisterPublisherResponse RegisterPublisher(RegisterPublisher request)
            throws TopicNotSupportedFault,InvalidTopicExpressionFault,
                PublisherRegistrationFailedFault,
                UnacceptableInitialTerminationTimeFault,
                PublisherRegistrationRejectedFault{
        //TODO : fill this with the necessary business logic
        throw new  java.lang.UnsupportedOperationException("Please implement " + this.getClass().getName() + "#RegisterPublisher");
    }

    public DestroyRegistrationResponse DestroyRegistration(DestroyRegistration request)
            throws ResourceNotDestroyedFault{
        //TODO : fill this with the necessary business logic
        throw new  java.lang.UnsupportedOperationException("Please implement " + this.getClass().getName() + "#DestroyRegistration");
    }
    
	/**
	 * COPIED FROM net.es.oscars.oscars.OSCARSSkeleton
     * Called from checkUser to get the DN out of the message context.
     *
     * @param opContext includes the MessageContext containing the message
     *                  signer
     */
    private void setOperationContext() {

        this.log.debug("setOperationContext.start");
        this.certSubject = null;
        this.certIssuer = null;
        try {
            MessageContext inContext =
                    MessageContext.getCurrentMessageContext();
            // opContext.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
            if (inContext == null) {
                this.log.debug("setOperationContext.start: context is NULL");
                return;
            }
            Vector results = (Vector)
                        inContext.getProperty(WSHandlerConstants.RECV_RESULTS);
            for (int i = 0; results != null && i < results.size(); i++) {
                WSHandlerResult hResult = (WSHandlerResult) results.get(i);
                Vector hResults = hResult.getResults();
                for (int j = 0; j < hResults.size(); j++) {
                    WSSecurityEngineResult eResult =
                            (WSSecurityEngineResult) hResults.get(j);
                    // An encryption or timestamp action does not have an
                    // associated principal. Only Signature and UsernameToken
                    // actions return a principal.
                    if ((((java.lang.Integer) eResult.get(
                            WSSecurityEngineResult.TAG_ACTION)).intValue() == WSConstants.SIGN) ||
                        (((java.lang.Integer) eResult.get(
                            WSSecurityEngineResult.TAG_ACTION)).intValue() == WSConstants.UT)) {
                    this.log.debug("setOperationContext.getSecurityInfo, " +
                        "Principal's name: " +
                        ((Principal) eResult.get(
                            WSSecurityEngineResult.TAG_PRINCIPAL)).getName());
                    this.setCertSubject(((X509Certificate) eResult.get(
                            WSSecurityEngineResult.TAG_X509_CERTIFICATE)).getSubjectDN());
                    this.setCertIssuer(((X509Certificate) eResult.get(
                            WSSecurityEngineResult.TAG_X509_CERTIFICATE)).getIssuerDN());
                } else if (((java.lang.Integer) eResult.get(
                            WSSecurityEngineResult.TAG_ACTION)).intValue() == WSConstants.ENCR) {
                    // Encryption action returns what ?
                } else if (((java.lang.Integer) eResult.get(
                            WSSecurityEngineResult.TAG_ACTION)).intValue() == WSConstants.TS) {
                    // Timestamp action returns a Timestamp
                    //System.out.println("Timestamp created: " +
                    //eResult.getTimestamp().getCreated());
                    //System.out.println("Timestamp expires: " +
                    //eResult.getTimestamp().getExpires());
                }
            }

            }
        } catch (Exception e) {
            this.log.error("setOperationContext.exception: " + e.getMessage());
        }
        this.log.debug("setOperationContext.finish");
    }

    /**
     *  COPIED FROM net.es.oscars.oscars.OSCARSSkeleton
     *  Called from each of the messages to check that the user who signed the
     *  message is entered in the user table.
     *  Also checks to see if there was a certificate in the message, which
     *  should never happen unless the axis2/rampart configuration is incorrect.
     *
     * @return login A string with the login associated with the certSubject
     * @throws AAAFaultMessage
     */
    public String checkUser() throws AAAFaultMessage {

        String login = null;
        String[] dnElems = null;
        setOperationContext();

        if (this.certSubject == null){
            this.log.error("checkUser: no certSubject found in message");
            AAAFaultMessage AAAErrorEx = new AAAFaultMessage(
                                 "checkUser: no certSubject found in message");
            throw AAAErrorEx;
        }

        Session aaa =
            HibernateUtil.getSessionFactory("aaa").getCurrentSession();
        aaa.beginTransaction();

        // lookup up using input DN first
        String origDN = this.certSubject.getName();
        this.log.debug("checkUser: " + origDN);
        try {
            login = this.userMgr.loginFromDN(origDN);
            if (login == null) {
                // if that fails try the reverse of the elements in the DN
                dnElems = origDN.split(",");
                String dn = " " + dnElems[0];
                for (int i = 1; i < dnElems.length; i++) {
                    dn = dnElems[i] + "," + dn;
                }
                dn = dn.substring(1);
                this.log.debug("checkUser: " + dn);

                login = this.userMgr.loginFromDN(dn);
                if (login == null) {
                    this.log.error("checkUser invalid user: " + origDN);
                    AAAFaultMessage AAAErrorEx =
                        new AAAFaultMessage("checkUser: invalid user" + origDN);
                    aaa.getTransaction().rollback();
                    throw AAAErrorEx;
                }
            }
        } catch (AAAException ex) {
            this.log.error("checkUser: no attributes for user: " + origDN);
            AAAFaultMessage AAAErrorEx =
                new AAAFaultMessage("checkUser: no attributes for user " + origDN + " :  " + ex.getMessage());
            aaa.getTransaction().rollback();
            throw AAAErrorEx;
        }
        this.log.info("checkUser authenticated user: " + login);
        aaa.getTransaction().commit();
        return login;
    }
    
    public UserManager getUserManager() { return this.userMgr; }

    public void setCertSubject(Principal DN) { this.certSubject = DN; }

    public void setCertIssuer(Principal DN) { this.certIssuer = DN; }

}
