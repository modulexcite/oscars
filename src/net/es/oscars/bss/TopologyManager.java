package net.es.oscars.bss;

import net.es.oscars.PropHandler;
import net.es.oscars.bss.topology.*;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.database.Initializer;
import net.es.oscars.oscars.TypeConverter;
import net.es.oscars.wsdlTypes.PathInfo;

import org.apache.log4j.*;

import org.hibernate.*;

import org.hibernate.criterion.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;


/**
 * This class contains methods to update the topology database, given
 * possibly new topology information.  To do so, it must
 *
 * 1) Compare the new information with existing topology information to
 *    see if the existing information is still valid.  Remove all invalidated
 *    node and port rows.
 *
 * 2) Save new topology information in the database if it doesn't exist in
 *    the old topology.
 *
 * 3) Recalculate the paths for all pending reservations.  If a path then
 *    violates policy by oversubscription or other means, the reservation is
 *    marked invalid, and the old path remains associated with the reservation.
 *
 * 4) Recalculate the paths for all active reservations.  If the path
 *    violates policy or is not the same after recalculation, the reservation
 *    is marked invalid, and the old path remains associated with the
 *    reservation.
 *
 * 5) Remove all invalidated ipaddrs that are not part of any reservation's
 *    path.  Remove all paths that are no longer associated with any
 *    reservation.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class TopologyManager {
    private Logger log;
    private SessionFactory sf;
    private String dbname;
    private ReservationManager rm;
    private Utils utils;
    private Properties props;
    private String localDomain;

    private HashMap<String, Domain> dbDomainMap;
    private HashMap<String, Node> dbNodeMap; 
    private HashMap<String, Port> dbPortMap; 
    private HashMap<String, Link> dbLinkMap; 
    private HashMap<Link, String> remoteLinkMap; 
    
    public TopologyManager(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        this.dbname = dbname;

        List<String> dbnames = new ArrayList<String>();
        dbnames.add(this.dbname);

        
        this.rm = new ReservationManager(this.dbname);
        this.utils = new Utils(this.dbname);

        Initializer initializer = new Initializer();
        initializer.initDatabase(dbnames);
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
        

        PropHandler propHandler = new PropHandler("oscars.properties");
        this.props = propHandler.getPropertyGroup("topo", true);
        this.setLocalDomain(this.props.getProperty("localdomain").trim());

        
        this.dbDomainMap = new HashMap<String, Domain>();
        this.dbNodeMap = new HashMap<String, Node>();
        this.dbPortMap = new HashMap<String, Port>();
        this.dbLinkMap = new HashMap<String, Link>();
        this.remoteLinkMap = new HashMap<Link, String>();
    }

    public void updateDomains(List<Domain> newDomains) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        
        this.log.info("updateDomains.start");
        this.sf.getCurrentSession().beginTransaction();
        try {
        	// step 1
	        this.log.info("merging with current topology");
	        this.mergeDomains(newDomains);
	        this.mergeRemoteLinks();
	        this.log.info("finished merging with current topology");
           // step 2
           this.log.info("recalculating pending paths");
           this.recalculatePaths("PENDING");
           this.log.info("recalculated pending paths");
           // step 3
           this.log.info("recalculating active paths");
           this.recalculatePaths("ACTIVE");
           this.log.info("recalculated active paths");
           // step 4
//           this.clean();
        } catch (BSSException e) {
           this.sf.getCurrentSession().getTransaction().rollback();

           this.log.error("updateDomains: " + e.getMessage());
           e.printStackTrace(pw);
           this.log.error(sw.toString());
           System.exit(-1);
        } catch (Exception e) {
           this.sf.getCurrentSession().getTransaction().rollback();
           this.log.error("updateDomains exception: " + e.getMessage());
           e.printStackTrace(pw);
           this.log.error(sw.toString());
           System.exit(-1);
        }
        this.sf.getCurrentSession().getTransaction().commit();
        this.log.info("updateDom.finish");
    }

    private void mergeDomains(List<Domain> newDomains) {
    	//        Domain domain = this.queryByParam("topologyIdent", topologyIdent);

        this.log.debug("mergeDomains.start");

        HashMap<String, Domain> newDomainMap = new HashMap<String, Domain>();

        DomainDAO domainDAO = new DomainDAO(this.dbname);
        List<Domain> currentDomains = domainDAO.list();

        for (Domain currentDomain : currentDomains) {
            String fqti = TopologyUtil.getFQTI(currentDomain);
            this.log.debug("  Database domain: topoIdent: [" + currentDomain.getTopologyIdent()+ "] FQTI: [" + fqti + "]");

            if (!this.dbDomainMap.containsKey(fqti)) {
            	this.dbDomainMap.put(fqti, currentDomain);
            } else {
                this.log.error("  Duplicate domain FQTIs in DB: [" + fqti + "]");
            }
        }

        // Our merging for domains means we only ADD to the domain list
        for (Domain newDomain : newDomains) {
            String newFqti = TopologyUtil.getFQTI(newDomain);

            this.log.debug("  Examiming domain, topoIdent: [" + newDomain.getTopologyIdent()+ "] FQTI: [" + newFqti + "]");

            if (!newDomainMap.containsKey(newFqti)) {
            	newDomainMap.put(newFqti, newDomain);
            } else {
                this.log.error("  Duplicate domain FQTIs in input: [" + newFqti + "], ignoring");
                continue;
            }
            
            if (newDomain.getTopologyIdent().equals(this.localDomain)) {
                newDomain.setLocal(true);
            } else {
                newDomain.setLocal(false);
            }
           
            if (!this.dbDomainMap.containsKey(newFqti)) {
                this.log.debug("  Creating domain, FQTI: [" + newFqti + "]");
                newDomain.setTopologyIdent(TopologyUtil.getLSTI(newFqti, "Domain"));
                domainDAO.create(newDomain);

            } else {
                this.log.debug("  Updating domain, FQTI: [" + newFqti + "]");
                Domain oldDomain = (Domain) dbDomainMap.get(newFqti);
                oldDomain.setTopologyIdent(TopologyUtil.getLSTI(newFqti, "Domain"));
                domainDAO.update(oldDomain); // normalize JUST IN CASE
                this.dbDomainMap.put(newFqti, oldDomain);
            }
        }

        
        // Now that everything is saved, merge domains
        for (Domain newDomain : newDomains) {
            Domain oldDomain = null;

            String newFqti = TopologyUtil.getFQTI(newDomain);

            if (this.dbDomainMap.containsKey(newFqti)) {
                oldDomain = (Domain) this.dbDomainMap.get(newFqti);
            }

            this.dbDomainMap.put(newFqti, newDomain);
        	this.mergeNodes(oldDomain, newDomain);
        }
        
        
        this.log.debug("mergeDomains.end");
    }

    private void mergeNodes(Domain oldDomain, Domain newDomain) {
        this.log.debug("mergeNodes start");
        NodeDAO nodeDAO = new NodeDAO(this.dbname);
        NodeAddressDAO nodeAddrDAO = new NodeAddressDAO(this.dbname);

        HashMap<String, Node> newNodeMap = new HashMap<String, Node>();
        HashMap<String, Node> oldNodeMap = new HashMap<String, Node>();

        if (oldDomain != null) {
            Iterator oldNodeIt = oldDomain.getNodes().iterator();
            while (oldNodeIt.hasNext()) {
                Node oldNode = (Node) oldNodeIt.next();
                String oldFqti = TopologyUtil.getFQTI(oldNode);
                this.log.debug("  Database node: topoIdent: [" + oldNode.getTopologyIdent()+ "] FQTI: [" + oldFqti + "]");

                if (!this.dbNodeMap.containsKey(oldFqti)) {
                	oldNodeMap.put(oldFqti, oldNode);
                	this.dbNodeMap.put(oldFqti, oldNode);
                } else {
                    this.log.error("  Duplicate node FQTIs in DB: [" + oldFqti + "]");
                }
            }
        }
        
        Iterator newNodeIt = newDomain.getNodes().iterator();

        // Now, create new nodes / update existing
        while (newNodeIt.hasNext()) {
            Node newNode = (Node) newNodeIt.next();
            String newFqti = TopologyUtil.getFQTI(newNode);

            this.log.debug("  Examiming node, topoIdent: [" + newNode.getTopologyIdent()+ "] FQTI: [" + newFqti + "]");

            
            if (!newNodeMap.containsKey(newFqti)) {
            	newNodeMap.put(newFqti, newNode);
            } else {
                continue;
            }

            if (!this.dbNodeMap.containsKey(newFqti)) {
                this.log.debug("  Creating node, FQTI: [" + newFqti + "]");
                if (oldDomain != null) {
                    newNode.setDomain(oldDomain);
                }
                newNode.setTopologyIdent(TopologyUtil.getLSTI(newFqti, "Node"));
                nodeDAO.create(newNode);
                
                nodeAddrDAO.create(newNode.getNodeAddress());
//                this.dbNodeMap.put(newFqti, newNode);
            } else {
                Node oldNode = this.dbNodeMap.get(newFqti);
                this.log.debug("  Updating node, FQTI: [" + newFqti + "]");

                // Topology identifier & domain id MUST the same if
                // we got this far, so let's not set them explicitly

                // Merge node addresses: set invalid if new one is null,
                // replace value if otherwise. 
                if (oldNode.getNodeAddress() == null) {
                    NodeAddress nodeAddr = TopologyUtil.initNodeAddress(oldNode);
                    oldNode.setNodeAddress(nodeAddr);
                }

                if (newNode.getNodeAddress() == null) {
                    nodeAddrDAO.remove(oldNode.getNodeAddress());
                    oldNode.setNodeAddress(null);
                } else {
                    oldNode.getNodeAddress()
                           .setAddress(newNode.getNodeAddress().getAddress());
                    nodeAddrDAO.update(oldNode.getNodeAddress());
                }

                oldNode.setTopologyIdent(TopologyUtil.getLSTI(newFqti, "Node"));
                oldNode.setValid(true);
                nodeDAO.update(oldNode);
            }
        }

    // Now that everything is saved, merge ports 
        for (String key : newNodeMap.keySet()) {
        	Node oldNode = null;
        	Node newNode = newNodeMap.get(key);

            if (this.dbNodeMap.containsKey(key)) {
            	oldNode = this.dbNodeMap.get(key);
            }
        	this.dbNodeMap.put(key, newNode);
        	this.mergePorts(oldNode, newNode);
        }

        
        // then invalidate nodes not existing any more
        for (String key : oldNodeMap.keySet()) {
            if (!newNodeMap.containsKey(key)) {
                this.log.debug("  Invalidating node [" + key + "]");
                this.invalidateNode(oldNodeMap.get(key));
            }
        }

        this.log.debug("mergeNodes end");
    }
    
    
    
    

    private void mergePorts(Node oldNode, Node newNode) {
        this.log.debug("mergePorts start");

        PortDAO portDAO = new PortDAO(this.dbname);

        HashMap<String, Port> newPortMap = new HashMap<String, Port>();
        HashMap<String, Port> oldPortMap = new HashMap<String, Port>();



        if (oldNode != null) {
            Iterator oldPortIt = oldNode.getPorts().iterator();

            while (oldPortIt.hasNext()) {
                Port oldPort = (Port) oldPortIt.next();
                String oldFqti = TopologyUtil.getFQTI(oldPort);
                
                this.log.debug("  Database port: topoIdent: [" + oldPort.getTopologyIdent()+ "] FQTI: [" + oldFqti + "]");
                
                if (!this.dbPortMap.containsKey(oldFqti)) {
                	this.dbPortMap.put(oldFqti, oldPort);
                	oldPortMap.put(oldFqti, oldPort);
                } else {
                    this.log.error("  Duplicate port FQTIs in DB: [" + oldFqti + "]");
                }
            }
        }

        Iterator newPortIt = newNode.getPorts().iterator();
        
        // Now, create new ports / update existing
        while (newPortIt.hasNext()) {
            Port newPort = (Port) newPortIt.next();

            String newFqti = TopologyUtil.getFQTI(newPort);
            
            this.log.debug("  Examiming port, topoIdent: [" + newPort.getTopologyIdent()+ "] FQTI: [" + newFqti + "]");

            if (!newPortMap.containsKey(newFqti)) {
            	newPortMap.put(newFqti, newPort);
            } else {
                continue;
            }

            if (!this.dbPortMap.containsKey(newFqti)) {
                // This port is brand new so insert it into DB
                this.log.debug("  Creating port, FQTI: [" + newFqti + "]");

                if (oldNode != null) {
                    newPort.setNode(oldNode);
                } else {
                	newPort.setNode(newNode);
                }
                newPort.setTopologyIdent(TopologyUtil.getLSTI(newFqti, "Port"));

                portDAO.create(newPort);
//                this.dbPortMap.put(newFqti, newPort);
            } else {
                this.log.debug("  Updating port, FQTI: [" + newFqti + "]");
                // This port already existed, so just copy properties
                Port oldPort = this.dbPortMap.get(newFqti);
                // copy properties
                // 
                // Note: Topology identifier & node id MUST the same if
                // we got this far, so let's not set them explicitly
                oldPort.setCapacity(newPort.getCapacity());
                oldPort.setGranularity(newPort.getGranularity());
                oldPort.setMaximumReservableCapacity(newPort.getMaximumReservableCapacity());
                oldPort.setMinimumReservableCapacity(newPort.getMinimumReservableCapacity());
                oldPort.setAlias(newPort.getAlias());
                oldPort.setTopologyIdent(TopologyUtil.getLSTI(newFqti, "Port"));

                oldPort.setValid(true);
                portDAO.update(oldPort);
            }
        }

        
        // Now that everything is saved, merge links 
        for (String key : newPortMap.keySet()) {
        	Port oldPort = null;
        	Port newPort = newPortMap.get(key);

            if (this.dbPortMap.containsKey(key)) {
            	oldPort = this.dbPortMap.get(key);
            }

            this.dbPortMap.put(key, newPort);
        	this.mergeLinks(oldPort, newPort);
        }

        // then invalidate ports not existing any more
        for (String key : oldPortMap.keySet()) {
            if (!newPortMap.containsKey(key)) {
                this.log.debug("  Invalidating port [" + key + "]");
                this.invalidatePort(oldPortMap.get(key));
            }
        }

        this.log.debug("mergePorts.end");
    }
    

    private void mergeLinks(Port oldPort, Port newPort) {
        this.log.debug("mergeLinks.start");

        LinkDAO linkDAO = new LinkDAO(this.dbname);
        
        HashMap<String, Link> newLinkMap = new HashMap<String, Link>();
        HashMap<String, Link> oldLinkMap = new HashMap<String, Link>();

        // have a look at all the links under the old port

        if (oldPort != null) {
            Iterator oldLinkIt = oldPort.getLinks().iterator();

            while (oldLinkIt.hasNext()) {
                Link oldLink = (Link) oldLinkIt.next();

                String oldFqti = TopologyUtil.getFQTI(oldLink);
                this.log.debug("  Database link: topoIdent: [" + oldLink.getTopologyIdent()+ "] FQTI: [" + oldFqti + "]");
            	
                if (!this.dbLinkMap.containsKey(oldFqti)) {
                	this.dbLinkMap.put(oldFqti, oldLink);
                } else {
                    this.log.error("Duplicate link FQTIs in DB: [" +oldFqti + "]");
                }

                if (!oldLinkMap.containsKey(oldFqti)) {
                	oldLinkMap.put(oldFqti, oldLink);
                }

            }
        }

        Iterator newLinkIt = newPort.getLinks().iterator();

        // Now, create new ports / update existing
        while (newLinkIt.hasNext()) {
            Link newLink = (Link) newLinkIt.next();
            
            String newFqti = TopologyUtil.getFQTI(newLink);

            this.log.debug("  Examiming link, topoIdent: [" + newLink.getTopologyIdent()+ "] FQTI: [" + newFqti + "]");
            
            if (!newLinkMap.containsKey(newFqti)) {
            	newLinkMap.put(newFqti, newLink);
            } else {
                continue;
            }

            if (!this.dbLinkMap.containsKey(newFqti)) {
                this.log.debug("  Creating link, FQTI: [" + newFqti + "]");
                // This link is brand new so insert it into DB
                String remFQTI = TopologyUtil.getFQTI(newLink.getRemoteLink());
                if (remFQTI != "") {
                	this.remoteLinkMap.put(newLink, remFQTI);
                }
                newLink.setRemoteLink(null);
                if (oldPort != null) {
                    newLink.setPort(oldPort);
                } else {
                    newLink.setPort(newPort);
                }
                linkDAO.create(newLink);

            } else {
                this.log.debug("  Updating link, FQTI: [" + newFqti + "]");
                // This link already existed, so just copy properties
                Link oldLink = this.dbLinkMap.get(newFqti);
                String remFQTI = TopologyUtil.getFQTI(newLink.getRemoteLink());
                if (remFQTI != "") {
                	this.remoteLinkMap.put(oldLink, remFQTI);
                }

                oldLink.setRemoteLink(null);

                // Note: Topology identifier & node id MUST the same if
                // we got this far, so let's not set them explicitly
                
                oldLink.setCapacity(newLink.getCapacity());
                oldLink.setGranularity(newLink.getGranularity());
                oldLink.setMaximumReservableCapacity(newLink.getMaximumReservableCapacity());
                oldLink.setMinimumReservableCapacity(newLink.getMinimumReservableCapacity());
                oldLink.setAlias(newLink.getAlias());

                oldLink.setValid(true);
            	
                linkDAO.update(oldLink);
            }
        }

        // Now that everything is saved, merge remote links, swcaps and ipaddrs
        for (String key : newLinkMap.keySet()) {
        	Link newLink = newLinkMap.get(key);
        	Link oldLink = null;
            if (dbLinkMap.containsKey(key)) {
            	oldLink = dbLinkMap.get(key);
            }
        	this.dbLinkMap.put(key, newLink);

            this.mergeLinkSwcaps(oldLink, newLink);
            this.mergeLinkIpaddrs(oldLink, newLink);
        }

        
        // then invalidate links not existing any more
        for (String key : oldLinkMap.keySet()) {
            if (!newLinkMap.containsKey(key)) {
                this.log.debug("Invalidating link [" + key + "]");
                this.invalidateLink(oldLinkMap.get(key));
            }
        }

        this.log.debug("mergeLinks.end");
    }

    private void mergeRemoteLinks() {
        this.log.debug("mergeRemoteLinks.start");
        
        DomainDAO domainDAO = new DomainDAO(this.dbname);
        NodeDAO nodeDAO = new NodeDAO(this.dbname);
        PortDAO portDAO = new PortDAO(this.dbname);
        LinkDAO linkDAO = new LinkDAO(this.dbname);

        // pull everything in-memory
        List<Link> dbLinks 		= linkDAO.list();

        ArrayList<String> newFQTIs =  new ArrayList<String>();
        HashMap<String, Link> dbLinkMap 	= new HashMap<String, Link>();

        for (Link dbLink : dbLinks) {
        	String fqti = TopologyUtil.getFQTI(dbLink);
        	dbLinkMap.put(fqti, dbLink);
        }

        for (Link key : this.remoteLinkMap.keySet()) {
        	String fqti = this.remoteLinkMap.get(key);
        	if (dbLinkMap.containsKey(fqti)) {
        		key.setRemoteLink(dbLinkMap.get(fqti));
        		linkDAO.update(key);
        	} else {
        		newFQTIs.add(fqti);
        	}
        }
        
    	for (String fqti : newFQTIs) {
    		this.log.debug ("  new remote link is: ["+fqti+"]");

    		String newDomLSTI = TopologyUtil.getLSTI(fqti, "Domain");
        	Domain remoteDomain = domainDAO.fromTopologyIdent(newDomLSTI);
        	if (remoteDomain == null) {
	    		this.log.debug ("  remote domain ["+newDomLSTI+"] does not exist, will create");
        		remoteDomain = TopologyUtil.initDomain();
        		if (newDomLSTI.equals(this.localDomain)) {
        			remoteDomain.setLocal(true);
                } else {
                	remoteDomain.setLocal(false);
                }
        		remoteDomain.setTopologyIdent(newDomLSTI);
        		domainDAO.create(remoteDomain);
    		}
    		

    		String newNodeLSTI = TopologyUtil.getLSTI(fqti, "Node");

    		Node remoteNode = nodeDAO.fromTopologyIdent(newNodeLSTI, remoteDomain);
    		if (remoteNode == null) {
	    		this.log.debug ("  remote Node["+newNodeLSTI+"] does not exist, will create");
    			Node newNode = TopologyUtil.initNode(remoteDomain);
        		newNode.setTopologyIdent(newNodeLSTI);
        		nodeDAO.create(newNode);
	    		remoteNode = newNode;
    		}

    		String newPortLSTI = TopologyUtil.getLSTI(fqti, "Port");
    		Port remotePort = portDAO.fromTopologyIdent(newPortLSTI, remoteNode);
    		if (remotePort == null) {
	    		this.log.debug ("  remote Port ["+newPortLSTI+"] does not exist, will create");
	    		Port newPort = TopologyUtil.initPort(remoteNode);
        		newPort.setTopologyIdent(newPortLSTI);
        		newPort.setAlias(newPortLSTI);
        		portDAO.create(newPort);
	    		remotePort = newPort;
    		}
    		
    		String newLinkLSTI = TopologyUtil.getLSTI(fqti, "Link");
    		Link remoteLink = linkDAO.fromTopologyIdent(newLinkLSTI, remotePort);
    		if (remoteLink == null) {
	    		this.log.debug ("  remote Link ["+newLinkLSTI+"] does not exist, will create");
	    		remoteLink = TopologyUtil.initLink(remotePort);
	    		remoteLink.setTopologyIdent(newLinkLSTI);
	    		remoteLink.setAlias(newLinkLSTI);
	    		linkDAO.create(remoteLink);
    		}
    		
    		
            for (Link key : this.remoteLinkMap.keySet()) {
            	String linkFqti = this.remoteLinkMap.get(key);
            	if (linkFqti.equals(fqti)) {
            		this.log.debug ("  ["+fqti+"] is set as remote link for ["+TopologyUtil.getFQTI(key)+"]");
            		key.setRemoteLink(remoteLink);
            		linkDAO.update(key);
            	}
            }
        }
    	
        this.log.debug("mergeRemoteLinks.end");
    }

    private void mergeLinkIpaddrs(Link oldLink, Link newLink) {
        this.log.debug("mergeLinkIpaddrs.start");

        IpaddrDAO ipaddrDAO = new IpaddrDAO(this.dbname);

        HashMap<String, Ipaddr> oldIpaddrMap = new HashMap<String, Ipaddr>();

        if (oldLink != null && oldLink.getIpaddrs() != null) {
        	
            Iterator oldIpaddrIt = oldLink.getIpaddrs().iterator();

            while (oldIpaddrIt.hasNext()) {
                Ipaddr oldIpaddr = (Ipaddr) oldIpaddrIt.next();
                String oldIP = oldIpaddr.getIP();
                if (oldIP == null) {
                	continue;
                }

                if (!oldIpaddrMap.containsKey(oldIP)) {
                    oldIpaddrMap.put(oldIP, oldIpaddr);
                    this.log.debug("  Existing ipaddr in DB: [" + oldIP + "]");
                } else {
                    this.log.error(
                        "  Duplicate IP addresses for same link in DB: [" + oldIP + "]");
                }
            }
        }

        HashMap<String, Ipaddr> newIpaddrMap = new HashMap<String, Ipaddr>();

        Iterator newIpaddrIt = newLink.getIpaddrs().iterator();

        // Now, create new ports / update existing
        while (newIpaddrIt.hasNext()) {
        	
            Ipaddr newIpaddr = (Ipaddr) newIpaddrIt.next();
            String newIP = newIpaddr.getIP();
            if (newIP == null) {
                this.log.error("  Null IP!");
                continue;
            }

            if (!newIpaddrMap.containsKey(newIP)) {
                newIpaddrMap.put(newIP, newIpaddr);
            } else {
                this.log.error("  Duplicate Ipaddr ids for same port in input: [" +
                    newIP + "]");
            }

            this.log.debug("  Examining ipaddr: [" + newIP + "]");

            if (!oldIpaddrMap.containsKey(newIP)) {
                this.log.debug("  Creating ipaddr (!): [" + newIP + "]");

                if (oldLink != null) {
                    newIpaddr.setLink(oldLink);
                } else {
                    newIpaddr.setLink(newLink);
                }

                newIpaddr.setIP(newIP);
                ipaddrDAO.create(newIpaddr);
            } else {
                this.log.debug("  Updating ipaddr (!): [" + newIP + "]");
                // This link already existed, so just copy properties
                Ipaddr oldIpaddr = oldIpaddrMap.get(newIP);
                oldIpaddr.setIP(newIP);
                oldIpaddr.setValid(true);

                ipaddrDAO.update(oldIpaddr);
            }
        }

        			
        // then invalidate ipaddrs not existing any more
        for (String key : oldIpaddrMap.keySet()) {
            if (!newIpaddrMap.containsKey(key)) {
                this.log.debug("  Invalidating Ipaddr [" + key + "]");

                Ipaddr oldIpaddr = oldIpaddrMap.get(key);
                oldIpaddr.setValid(false);
                ipaddrDAO.update(oldIpaddr);
            }
        }

        this.log.debug("mergeLinkIpaddrs.end");
    }

    private void mergeLinkSwcaps(Link oldLink, Link newLink) {
        L2SwitchingCapabilityDataDAO l2swcapDAO = new L2SwitchingCapabilityDataDAO(this.dbname);
        LinkDAO linkDAO = new LinkDAO(this.dbname);
        // YYYY
        this.log.debug("mergeLinkSwcap.start");

        L2SwitchingCapabilityData oldSwCap = null;
        L2SwitchingCapabilityData newSwCap = newLink.getL2SwitchingCapabilityData();

        if (oldLink != null) {
            oldSwCap = oldLink.getL2SwitchingCapabilityData();
        }

        if ((oldSwCap == null) && (newSwCap == null)) {
            // nothing to do
            this.log.debug("mergeLinkSwcap.end");
            return;
        } else if ((oldSwCap != null) && (newSwCap == null)) {
            this.log.debug("  removing l2 switching cap");
        } else if (oldSwCap == null) {
            this.log.debug("  inserting new l2 switching cap");

            // new sw cap is not null
            if (oldLink != null) {
                oldLink.setL2SwitchingCapabilityData(newSwCap);
                newSwCap.setLink(oldLink);
                l2swcapDAO.update(newSwCap);
                linkDAO.update(oldLink);
            } else {
                newLink.setL2SwitchingCapabilityData(newSwCap);
                newSwCap.setLink(newLink);
                l2swcapDAO.update(newSwCap);
                linkDAO.update(newLink);
            }
        } else {
            this.log.debug("  updating l2 switching cap");
            // neither is null, copy properties to old
            oldSwCap.setInterfaceMTU(newSwCap.getInterfaceMTU());
            oldSwCap.setVlanRangeAvailability(newSwCap.getVlanRangeAvailability());
            l2swcapDAO.update(oldSwCap);
        }

        this.log.debug("mergeLinkSwcap.end");
    }

    private void invalidateNode(Node nodeDB) {
        NodeDAO nodeDAO = new NodeDAO(this.dbname);
        nodeDB.setValid(false);
        nodeDAO.update(nodeDB);

        Iterator portIt = nodeDB.getPorts().iterator();

        while (portIt.hasNext()) {
            Port portDB = (Port) portIt.next();
            this.invalidatePort(portDB);
        }
    }

    private void invalidatePort(Port portDB) {
        PortDAO portDAO = new PortDAO(this.dbname);
        portDB.setValid(false);

        Iterator linkIt = portDB.getLinks().iterator();

        while (linkIt.hasNext()) {
            Link linkDB = (Link) linkIt.next();
            this.invalidateLink(linkDB);
        }

        portDAO.update(portDB);
    }

    private void invalidateLink(Link linkDB) {
        LinkDAO linkDAO = new LinkDAO(this.dbname);
        IpaddrDAO ipaddrDAO = new IpaddrDAO(this.dbname);
        Set<Ipaddr> currentIpaddrs = linkDB.getIpaddrs();

        for (Ipaddr currentIpaddr : currentIpaddrs) {
            currentIpaddr.setValid(false);
            ipaddrDAO.update(currentIpaddr);
        }

        linkDB.setValid(false);
        linkDAO.update(linkDB);
    }

    /**
     * Recalculates the paths for reservations with the given status.
     * If the new path violates policy by oversubscription or other means, the
     * reservation is marked invalid, and the old path remains associated with
     * the reservation.  If the reservation is active and the new path differs
     * in any way, the reservation is marked invalid.
     *
     * @param status string with status of reservations to check
     *
     * @throws BSSException
     */
    private void recalculatePaths(String status) throws BSSException {
        String ingressNodeIP = null;
        String egressNodeIP = null;
        Path path = null;
        Link link = null;
        Ipaddr ipaddr = null;

        ReservationDAO dao = new ReservationDAO(this.dbname);
        IpaddrDAO ipaddrDAO = new IpaddrDAO(this.dbname);
        List<Reservation> reservations = dao.statusReservations(status);

        for (Reservation r : reservations) {
            Path oldPath = r.getPath();

            // find old ingress and egress IP's
            // TODO:  this may no longer be necessary
            PathElem pathElem = oldPath.getPathElem();

            while (pathElem != null) {
                if (pathElem.getDescription() != null) {
                    if (pathElem.getDescription().equals("ingress")) {
                        link = pathElem.getLink();
                        ipaddr = ipaddrDAO.fromLink(link);
                        ingressNodeIP = ipaddr.getIP();
                    } else if (pathElem.getDescription().equals("egress")) {
                        link = pathElem.getLink();
                        ipaddr = ipaddrDAO.fromLink(link);
                        egressNodeIP = ipaddr.getIP();
                    }
                }

                pathElem = pathElem.getNextElem();
            }

            // This should never happen.  However, the semantics of
            // using these are different than in the original
            // reservation creation, where they can be null.
            if ((ingressNodeIP == null) || (egressNodeIP == null)) {
                r.setStatus("INVALIDATED");
                dao.update(r);

                continue;
            }

            //TODO:  build layer-specific info from old path in database
            //       assuming only the hops have the possibility of changing
            PathInfo pathInfo = new PathInfo();

            try {
                TypeConverter tc = new TypeConverter();
                // finds path and checks for oversubscription
                path = this.rm.getPath(r, pathInfo);
            } catch (BSSException e) {
                r.setStatus("INVALIDATED");
                dao.update(r);

                continue;
            }

            if (status.equals("PENDING")) {
                r.setPath(path);
                dao.update(r);
            } else if (status.equals("ACTIVE")) {
                if (!this.isDuplicate(oldPath, path)) {
                    r.setStatus("INVALIDATED");
                    dao.update(r);
                }
            }
        }
    }

    /**
     * Removes invalidated topology information, except for ipaddrs and their
     * parents associated with non-pending and non-active paths.
     * Removes paths that are no longer associated with any reservation.
     */
    private void clean() {
        this.log.info("clean.start");
        // remove any path that is not part of a reservation
        this.log.info("removing paths that are no longer in use");

        PathDAO pathDAO = new PathDAO(this.dbname);
        List<Path> paths = pathDAO.list();

        // TODO:  check to make sure this works or is needed
        for (Path path : paths) {
            Reservation resv = path.getReservation();

            if (resv == null) {
                pathDAO.remove(path);
            }
        }

        this.log.info("finished removing paths that are no longer in use");

        // remove all invalid ipaddrs that are not part of any reservation
        // (ipaddrs associated with pending and active reservations are
        // guaranteed to be valid because of path recalculation)
        this.log.info("removing invalid ipaddrs that are no longer in use");

        IpaddrDAO ipaddrDAO = new IpaddrDAO(this.dbname);
        List<Ipaddr> ipaddrs = ipaddrDAO.list();
        PathElemDAO pathElemDAO = new PathElemDAO(this.dbname);
        List<PathElem> pathElems = pathElemDAO.list();
        Set<Ipaddr> ipset = new HashSet<Ipaddr>();

        for (PathElem pathElem : pathElems) {
            Link link = pathElem.getLink();
            Ipaddr ipaddr = ipaddrDAO.fromLink(link);
            ipset.add(ipaddr);
        }

        LinkDAO linkDAO = new LinkDAO(this.dbname);

        for (Ipaddr ipaddr : ipaddrs) {
            if (!ipaddr.isValid()) {
                Link parent = ipaddr.getLink();

                // remove an address if it is not part of any paths
                // or if there is already an invalid copy of it
                if (!ipset.contains(ipaddr)) {
                    parent.removeIpaddr(ipaddr);
                    ipaddrDAO.remove(ipaddr);
                    linkDAO.update(parent);
                }
            }
        }

        this.log.info("finished removing invalid ipaddrs");

        // remove invalid links that now have no ipaddrs
        PortDAO portDAO = new PortDAO(this.dbname);
        List<Link> links = linkDAO.list();

        for (Link link : links) {
            Port parent = link.getPort();

            if (!link.isValid() && link.getIpaddrs().isEmpty()) {
                parent.removeLink(link);
                linkDAO.remove(link);
                portDAO.update(parent);
            }
        }

        this.log.info("finished removing invalid links");

        // remove invalid ports that now have no ipaddrs
        NodeDAO nodeDAO = new NodeDAO(this.dbname);
        List<Port> ports = portDAO.list();

        for (Port port : ports) {
            Node parent = port.getNode();

            if (!port.isValid() && port.getLinks().isEmpty()) {
                parent.removePort(port);
                portDAO.remove(port);
                nodeDAO.update(parent);
            }
        }

        this.log.info("finished removing invalid ports");

        // remove invalid nodes that now have no ports
        List<Node> nodes = nodeDAO.list();

        for (Node node : nodes) {
            if (!node.isValid() && node.getPorts().isEmpty()) {
                nodeDAO.remove(node);
            }
        }

        this.log.info("finished removing invalid nodes");
        this.log.info("clean.finish");
    }

    /**
     * Checks to see if two paths contain the same information.
     * If layer specific or MPLS-specific information associated with a
     * path is different, the paths are considered different.
     *
     * @param savedPath saved path information
     * @param checkPath unsaved path to check for duplicate
     * @return boolean indicating whether paths are the same
     */
    private boolean isDuplicate(Path savedPath, Path checkPath) {
        this.log.info("isDuplicate.start");

        if (!savedPath.equals(checkPath)) {
            this.log.debug("one path's fields are different");

            return false;
        }

        // first check that paths are the same length
        int firstCtr = 0;
        PathElem pathElem = savedPath.getPathElem();

        while (pathElem != null) {
            firstCtr++;
            pathElem = pathElem.getNextElem();
        }

        int secondCtr = 0;
        pathElem = checkPath.getPathElem();

        while (pathElem != null) {
            secondCtr++;
            pathElem = pathElem.getNextElem();
        }

        if (firstCtr != secondCtr) {
            this.log.debug("two paths have different lengths");

            return false;
        }

        // now that know paths are the same length,
        // check each element of the two paths for equality
        pathElem = savedPath.getPathElem();

        PathElem checkPathElem = checkPath.getPathElem();

        while (pathElem != null) {
            if (!pathElem.equals(checkPathElem)) {
                this.log.debug("two paths are different");

                return false;
            }

            pathElem = pathElem.getNextElem();
            checkPathElem = checkPathElem.getNextElem();
        }

        // check to see if the layer-specific information is the same
        Layer2DataDAO layer2DataDAO = new Layer2DataDAO(this.dbname);
        Layer3DataDAO layer3DataDAO = new Layer3DataDAO(this.dbname);
        MPLSDataDAO MPLSDataDAO = new MPLSDataDAO(this.dbname);
        Layer2Data savedLayer2Data = savedPath.getLayer2Data();
        Layer3Data savedLayer3Data = savedPath.getLayer3Data();
        MPLSData savedMPLSData = savedPath.getMplsData();
        Layer2Data checkLayer2Data = checkPath.getLayer2Data();
        Layer3Data checkLayer3Data = checkPath.getLayer3Data();
        MPLSData checkMPLSData = checkPath.getMplsData();

        if ((savedLayer2Data != null) && (checkLayer2Data != null)) {
            if (!savedLayer2Data.equals(checkLayer2Data)) {
                this.log.debug("layer 2 fields are different");

                return false;
            }
        }

        if (((savedLayer2Data == null) && (checkLayer2Data != null)) ||
                ((savedLayer2Data != null) && (checkLayer2Data == null))) {
            this.log.debug("one path is layer 2, the other is not");

            return false;
        }

        if ((savedLayer3Data != null) && (checkLayer3Data != null)) {
            if (!savedLayer3Data.equals(checkLayer3Data)) {
                this.log.debug("layer 3 fields are different");

                return false;
            }
        }

        if (((savedLayer3Data == null) && (checkLayer3Data != null)) ||
                ((savedLayer3Data != null) && (checkLayer3Data == null))) {
            this.log.debug("one path is layer 3, the other is not");

            return false;
        }

        if ((savedMPLSData != null) && (checkMPLSData != null)) {
            if (!savedMPLSData.equals(checkMPLSData)) {
                this.log.debug("MPLS-specific fields are different");

                return false;
            }
        }

        if (((savedMPLSData == null) && (checkMPLSData != null)) ||
                ((savedMPLSData != null) && (checkMPLSData == null))) {
            this.log.debug("one path is MPLS-specific, the other is not");

            return false;
        }

        this.log.info("isDuplicate.finish true");

        return true;
    }
    /**
     * localDomain getter
     * @return the value of localDomain
     */
    public String getLocalDomain() {
        return this.localDomain;
    }

    /**
     * localDomain setter
     * @param domainId The value to be set
     */
    public void setLocalDomain(String domainId) {
        this.localDomain = domainId;
    }
}
