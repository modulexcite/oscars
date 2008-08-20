package net.es.oscars.bss.policy;

import java.util.*;

import org.apache.log4j.*;

import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;

import net.es.oscars.bss.BSSException;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.bss.topology.*;
import net.es.oscars.bss.*;
import net.es.oscars.oscars.TypeConverter;
import java.util.Properties;
import net.es.oscars.PropHandler;

/**
 * This class contains methods for handling reservation setup policy
 *
 * @author David Robertson (dwrobertson@lbl.gov), Jason Lee (jrlee@lbl.gov)
 */
public class PolicyManager {
    private Logger log;
    private String dbname;
    private String vlanFilter;
    
    public PolicyManager(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        this.dbname = dbname;
        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties props = propHandler.getPropertyGroup("policy", true);
        this.vlanFilter = props.getProperty("vlanFilter");
        if(this.vlanFilter == null){
            this.vlanFilter = "vlanMap";
        }
    }

    /**
     * Checks whether adding this reservation would cause oversubscription
     *     on a port.
     *
     * @param activeReservations existing reservations
     * @param pathInfo PathInfo instance to check for oversubscription
     * @param intraPath the intradomain path containing the links to check
     * @param newReservation new reservation instance
     * @throws BSSException
     */
    public void checkOversubscribed(
               List<Reservation> activeReservations,
               PathInfo pathInfo, CtrlPlanePathContent intraPath,
               Reservation newReservation)
            throws BSSException {
        this.log.info("checkOversubscribed.start");


        List<Link> localLinks = this.getLocalLinksFromPath(intraPath);

        BandwidthFilter bwf = new BandwidthFilter();
        bwf.applyFilter(pathInfo, intraPath.getHop(), localLinks, newReservation, activeReservations);
 
        if (pathInfo.getLayer2Info() != null) {
            PolicyFilter vlf = PolicyFilterFactory.create(this.vlanFilter);
            vlf.applyFilter(pathInfo, intraPath.getHop(), localLinks, newReservation, activeReservations);
        }

        this.log.info("checkOversubscribed.end");
    }

     /**
     * Retrieves linkIntervals given a PathInfo instance.
     * Path contains series of link id's.
     *
     * @param pathInfo PathInfo instance containing path parameters
     * @param ctrlPlanePath the intradomain path to with the linkIntervals to check
     * @param startTime start time for the new reservation
     * @param endTime end time for the new reservation
     * @param capacity capacity requested
     * @return linkIntervals map with initial Link instances as keys
     */
    private ArrayList<Link> getLocalLinksFromPath(CtrlPlanePathContent ctrlPlanePath) throws BSSException {

        this.log.info("getLinksFromPath.start");
        ArrayList<Link> links= new ArrayList<Link>();
        TypeConverter tc = new TypeConverter();
        
        if (ctrlPlanePath == null) {
            throw new BSSException("no path provided to initlinkIntervals");
        }

        CtrlPlaneHopContent[] hops = ctrlPlanePath.getHop();
        DomainDAO domainDAO = new DomainDAO(this.dbname);
        for (int i = 0; i < hops.length; i++) {
            String hopTopoId = tc.hopToURN(hops[i]);
            this.log.info(hopTopoId);
            Hashtable<String, String> parseResults = URNParser.parseTopoIdent(hopTopoId);
            String hopType = parseResults.get("type");
            String domainId = parseResults.get("domainId");

            if (hopType.equals("link")) {
                if (domainDAO.isLocal(domainId)) {
                    this.log.info("local: " + hopTopoId);
                    Link link = domainDAO.getFullyQualifiedLink(hopTopoId);
                    if (link == null) {
                        throw new BSSException("unable to find link with id " + hopTopoId);
                    }
                    links.add(link);

                } else {
                    throw new BSSException("Non-local link in intradomain path: " + hopTopoId);
                }
            } else {
                throw new BSSException("Hops in intradomain path must be " +
                                       "links but found "+hopType+" for hop: " + hopTopoId);
            }
        }

        this.log.info("getLocalLinksFromPath.end");
        return links;
    }

}
