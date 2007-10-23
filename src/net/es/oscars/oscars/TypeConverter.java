/**
 * Type converter between Axis2 classes and Hibernate beans.
 *
 * @author David Robertson, Mary Thompson, Jason Lee
 */
package net.es.oscars.oscars;

import java.util.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.*;

// code generated by Martin Swany's schemas
import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlanePathContent;
import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlaneHopContent;

import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.topology.*;
import net.es.oscars.wsdlTypes.*;


/**
 * Has methods to convert between Axis2 WSDL type classes and Hibernate beans.
 * Used by both the API and the WBUI.
 */
public class TypeConverter {

    private Logger log;

    public TypeConverter() {
        this.log = Logger.getLogger(this.getClass());
    }

    /**
     * Builds Hibernate Reservation bean, given Axis2 ResCreateContent class.
     *
     * @param params ResCreateContent instance
     * @return A Hibernate Reservation instance
     * @throws BSSException
     */
    public Reservation contentToReservation(ResCreateContent params) 
            throws BSSException {

        Reservation resv = new Reservation();
        PathInfo pathInfo = params.getPathInfo();
        Layer2Info layer2Info = pathInfo.getLayer2Info(); 
        Layer3Info layer3Info = pathInfo.getLayer3Info(); 
        // have to do error checking here because tooling doesn't handle
        // WSDL choice elements yet
        if ((layer2Info == null) && (layer3Info == null)) {
            throw new BSSException("No path information provided");
        } else if ((layer2Info != null) && (layer3Info != null)) {
            throw new BSSException(
                    "Cannot provide both layer 2 and layer 3 information");
        }
        // Hibernate will pick up error if any properties are null that
        // the database schema says cannot be null
        resv.setStartTime(params.getStartTime());
        resv.setEndTime(params.getEndTime());
        Long bandwidth = new Long(
                Long.valueOf((long)params.getBandwidth() * 1000000L));
        resv.setBandwidth(bandwidth);
        resv.setDescription(params.getDescription());
        resv.setGlobalReservationId(params.getGlobalReservationId());
        
        return resv;
    }

    /**
     * Builds Axis2 CreateReply class, given Hibernate Reservation bean.
     *
     * @param resv A Hibernate Reservation instance
     * @return CreateReply instance
     */
    public CreateReply reservationToReply(Reservation resv) {
        CreateReply reply = new CreateReply();
        reply.setGlobalReservationId(resv.getGlobalReservationId());
        reply.setToken("unimplemented yet");
        reply.setStatus(resv.getStatus());
        return reply;
    }

    /**
     * Builds Axis2 ResDetails class, given Hibernate bean.
     * Note that this is used by only by query, and is using
     * information from a stored reservation.
     * 
     * @param resv A Hibernate reservation instance
     * @return ResDetails instance
     */
    public ResDetails reservationToDetails(Reservation resv) {

        ResDetails reply = new ResDetails();
        reply.setGlobalReservationId(resv.getGlobalReservationId());
        reply.setLogin(resv.getLogin());
        reply.setStatus(resv.getStatus());
        reply.setStartTime(resv.getStartTime());
        reply.setEndTime(resv.getEndTime());
        reply.setCreateTime(resv.getCreatedTime());
        int bandwidth = resv.getBandwidth().intValue();
        reply.setBandwidth(bandwidth);
        reply.setDescription(resv.getDescription());
        reply.setPathInfo(this.getPathInfo(resv));
        return reply;
    }

    /**
     * Builds list of Axis2 ListReply instances, given list of Hibernate
     * Reservation beans and a list of PathInfo structures.
     *
     * @param reservations A list of Hibernate Reservation beans
     * @return ListReply A list of Axis2 ListReply instances
     */
    public ListReply reservationToListReply(List<Reservation> reservations) {
        ListReply reply = new ListReply();
        int ctr = 0;

        if (reservations == null) { 
            this.log.info("toListReply, reservations is null");
            return reply;
        }
        int listLength = reservations.size();
        ResDetails[] resList = new ResDetails[listLength];
        for (Reservation resv: reservations) {
            ResDetails details = this.reservationToDetails(resv);
            resList[ctr] = details;
            ctr++;
        }
        reply.setResDetails(resList);
        return reply;
    }
    
    /**
     * Builds all components of Axis2 PathInfo structure, given a
     * Hibernate Reservation bean.
     *
     * @param resv a Reservation instance
     * @return pathInfo a filled in PathInfo Axis2 type
     */
    public PathInfo getPathInfo(Reservation resv) {
        PathInfo pathInfo = new PathInfo();
        pathInfo.setPathSetupMode(resv.getPath().getPathSetupMode());
        pathInfo.setPath(this.pathToCtrlPlane(resv.getPath()));
        // one of these is allowed to be null
        Layer2Info layer2Info = this.pathToLayer2Info(resv.getPath());
        pathInfo.setLayer2Info(layer2Info);
        Layer3Info layer3Info = this.pathToLayer3Info(resv.getPath());
        pathInfo.setLayer3Info(layer3Info);
        // allowed to be null
        MplsInfo mplsInfo = this.pathToMplsInfo(resv.getPath());
        pathInfo.setMplsInfo(mplsInfo);
        return pathInfo;
    }

    /**
     * Builds Axis2 CtrlPlanePathContent, given Hibernate Path bean with
     * information retrieved from database.  This is the reservation's
     * internal path returned in response to a query.
     *
     * @param path a Path instance
     * @return A CtrlPlanePathContent instance
     */
    public CtrlPlanePathContent pathToCtrlPlane(Path path) {

        String hopId = null;
        Ipaddr ipaddr = null;

        PathElem pathElem = path.getPathElem();
        CtrlPlanePathContent ctrlPlanePath = new CtrlPlanePathContent();
        while (pathElem != null) {
            CtrlPlaneHopContent hop = new CtrlPlaneHopContent();
            Link link = pathElem.getLink();
            if (path.getLayer2Data() != null) {
                Port port = link.getPort();
                Node node = port.getNode();
                Domain domain = node.getDomain();
                hopId = domain.getTopologyIdent() + ":" +
                         node.getTopologyIdent() + ":" +
                         port.getTopologyIdent() + ":" +
                         link.getTopologyIdent();
            } else {
                String nodeName = link.getPort().getNode().getTopologyIdent();
                IpaddrDAO ipaddrDAO = new IpaddrDAO("bss");
                ipaddr = ipaddrDAO.fromLink(link);
                hopId = nodeName + ": " + ipaddr.getIP();
            }
            hop.setId(hopId);
            hop.setLinkIdRef(hopId);
            ctrlPlanePath.addHop(hop);
            pathElem = pathElem.getNextElem();
        }
        ctrlPlanePath.setId("unimplemented");
        return ctrlPlanePath;
    }
    
    /**
     * Given the Hibernate bean for a path, return a filled in Axis2 instance
     * for layer 2 information.
     */
    public Layer2Info pathToLayer2Info(Path path) {
        Layer2DataDAO layer2DataDAO = new Layer2DataDAO("bss");
        // database type
        Layer2Data layer2Data = path.getLayer2Data();
        if (layer2Data == null) {
            return null;
        }
        // Axis2 type
        Layer2Info layer2Info = new Layer2Info();
        layer2Info.setSrcEndpoint(layer2Data.getSrcEndpoint());
        layer2Info.setDestEndpoint(layer2Data.getDestEndpoint());
        return layer2Info;
    }

    /**
     * Given the Hibernate bean for a path, return a filled in Axis2 instance
     * for layer 3 information.
     */
    public Layer3Info pathToLayer3Info(Path path) {

        Layer3DataDAO layer3DataDAO = new Layer3DataDAO("bss");
        // database type
        Layer3Data layer3Data = path.getLayer3Data();
        if (layer3Data == null) {
            return null;
        }
        // Axis2 type
        Layer3Info layer3Info = new Layer3Info();
        layer3Info.setSrcHost(layer3Data.getSrcHost());
        layer3Info.setDestHost(layer3Data.getDestHost());
        // makes sure that protocol is in upper case to match WSDL
        if (layer3Data.getProtocol() != null) {
            layer3Info.setProtocol(layer3Data.getProtocol().toUpperCase());
        }
        if (layer3Data.getSrcIpPort() != null) {
            layer3Info.setSrcIpPort(layer3Data.getSrcIpPort());
        }
        if (layer3Data.getDestIpPort() != null) {
            layer3Info.setDestIpPort(layer3Data.getDestIpPort());
        }
        return layer3Info;
    }

    /**
     * Given the Hibernate bean for a path, return a filled in Axis2 instance
     * for MPLS information.
     */
    public MplsInfo pathToMplsInfo(Path path) {

        MPLSDataDAO MPLSDataDAO = new MPLSDataDAO("bss");
        // database type
        MPLSData mplsData = path.getMplsData();
        if (mplsData == null) {
            return null;
        }
        // Axis2 type
        MplsInfo mplsInfo = new MplsInfo();
        int burstLimit = mplsData.getBurstLimit().intValue();
        mplsInfo.setBurstLimit(burstLimit);
        mplsInfo.setLspClass(mplsData.getLspClass());
        return mplsInfo;
    }

    /**
     * Given a PathInfo instance, converts the ERO to format for client.
     * Currently it is passed back as is for layer 2, and converted to host name
     * IP pairs for layer 3.  This is used by the create message.
     *
     * @param pathInfo a PathInfo instance
     */
    public void clientConvert(PathInfo pathInfo) {

        String hopId = null;
        String hostName = null;
        Ipaddr ipaddr = null;

        // return as is if layer 2
        if (pathInfo.getLayer2Info() != null) {
            return;
        } 
        // if layer 3, generate new path with host name/IP rather than
        // topology identifier
        CtrlPlanePathContent oldPath = pathInfo.getPath();
        CtrlPlanePathContent newPath = new CtrlPlanePathContent();
        CtrlPlaneHopContent[] oldHops = oldPath.getHop(); 
        DomainDAO domainDAO =  new DomainDAO("bss");
        for (int i=0; i < oldHops.length; i++) {
            CtrlPlaneHopContent hop = new CtrlPlaneHopContent();
            String[] componentList = oldHops[i].getLinkIdRef().split(":");
            // if local domain
            if (!componentList[3].equals("other")) {
                Link link = domainDAO.getFullyQualifiedLink(componentList);
                hostName = link.getPort().getNode().getTopologyIdent();
                IpaddrDAO ipaddrDAO = new IpaddrDAO("bss");
                ipaddr = ipaddrDAO.fromLink(link);
                hopId = hostName + ": " + ipaddr.getIP();
            } else {
                // this component is IP address in other domain
                String ip = componentList[6];
                hostName = null;
                try {
                    InetAddress inetAddress = InetAddress.getByName(ip);
                    hostName = inetAddress.getHostName();
                } catch (UnknownHostException e) {
                    ;  // non-fatal error
                }
                if ((hostName != null) && !hostName.equals(ip)) {
                    hopId = hostName + ": " + ip;
                } else {
                    hopId = ip;
                }
            }
            hop.setId(hopId);
            hop.setLinkIdRef(hopId);
            newPath.addHop(hop);
        }
        newPath.setId("unimplemented");
        pathInfo.setPath(newPath);
        return;
    }
    
    /**
     * Given a PathInfo instance, determines whether it contains information
     *     requiring special authorization to set.
     *
     * @param pathInfo a PathInfo instance
     * @return boolean indicating whether contains series of hops
     */
    public boolean checkPathAuth(PathInfo pathInfo) {

        if (pathInfo.getLayer3Info() != null) {
            CtrlPlanePathContent path = pathInfo.getPath();
            if (path != null) { return true; }
        }
        return false;
    }
    
    /**
     * Converts a string to a bit mask. The range should take the form
     * "x,y" for discontinuous ranges and "x-y" for continuous ranges. 
     * These formats can be concatenated to specify many subranges 
     * (i.e 600,3000-3001).
     * 
     * @param range the range string to be converted
     * @return a bit mask with values in given range set to 1
     * @throws BSSException
     */
    public byte[] rangeStringToMask(String range) throws BSSException{
        byte[] mask = new byte[512];
        
        range = range.replaceAll("\\s", "");
        String[] rangeList = range.split(",");
        for(int i = 0; i < rangeList.length; i++){
            String[] rangeEnds = rangeList[i].split("-");
            if(rangeEnds.length == 1){
                int tag = Integer.parseInt(rangeEnds[0]);
                if(tag < 4096){
                    mask[tag/8] = (byte)(1 << (7 - (tag % 8)));
                }
            }else if(rangeEnds.length == 2){
                int startTag = Integer.parseInt(rangeEnds[0]);
                int endTag = Integer.parseInt(rangeEnds[1]);
                if(startTag < 4096 && endTag < 4096){
                    for(int j = startTag; j <= endTag; j++){
                        mask[j/8] |= (1 << (7 - (j % 8)));
                    }
                }
            }else{
                throw new BSSException("Invalid VLAN range specified");
            }
        }
        
        /* for(int k = 0; k < mask.length; k++){
            System.out.println(k + ": " + (byte)(mask[k] & 255));
        } */
        
        return mask;
    }
    
    /**
     * Converts given mask to a range string. The range takes the form
     * "x,y" for discontinuous ranges and "x-y" for continuous ranges. 
     * These formats can be concatenated to specify many subranges 
     * (i.e 600,3000-3001).
     * 
     * @param mask the bit mask to be converted
     * @return a range string representing the given bit mask
     */
    public String maskToRangeString(byte[] mask){
        int start = 0;
        String range = new String();
        
        for(int i = 0; i < mask.length; i++){
            for(int j = 0; j < 8; j++){
                int tag = i*8 + j;
                if((mask[i] & (int)Math.pow(2, (7-j))) > 0){
                    if(start == 0){
                        start = tag;
                    }
                }else if(start != 0){
                    if(!range.equals("")){
                        range += ",";
                    }
                    range += start;
                    if(start != (tag -1)){
                        range += "-" + (tag-1);
                    }
                    start = 0;
                }
            }
        }

        return range;
    }
    
    /**
     * If given an int whose string length is less than 2, prepends a "0".
     *
     * @param dint int, for example representing month or day
     * @return fixedLength fixed length string of length 2
     */
    private String fixedLengthTime(int dint) {
        String fixedLength = null;

        if (dint < 10) { fixedLength = "0" + dint; }
        else { fixedLength = "" + dint; }
        return fixedLength;
    }
}
