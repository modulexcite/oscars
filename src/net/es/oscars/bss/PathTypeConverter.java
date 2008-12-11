/**
 * Performs and supports conversion operations on Hibernate Paths.
 *
 * @author Andrew Lake, David Robertson
 */
package net.es.oscars.bss;

import java.util.*;

import org.apache.log4j.*;

import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.Token;
import net.es.oscars.bss.PathManager;
import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.topology.*;
// TODO:  to remove
import net.es.oscars.oscars.TypeConverter;
import net.es.oscars.wsdlTypes.*;
// code generated by Martin Swany's schemas
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneDomainContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneLinkContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneNodeContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePortContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneSwcapContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneSwitchingCapabilitySpecificInfo;
// end to remove


/**
 * Has methods to perform and support conversion operations on Paths.
 * Any conversions involving the Axis2 PathInfo structure are performed
 * in oscars.TypeConverter.
 */
public class PathTypeConverter {

    private static Logger log = Logger.getLogger(PathTypeConverter.class);

    // do not instantiate
    private PathTypeConverter() {
    }

    /**
     * Converts Reservation Hibernate bean to a HashMap
     *
     * @param resv the Reservation to convert
     * @return the converted HashMap
     */
    public static HashMap<String, String[]>
        reservationToHashMap(Reservation resv)
    		    throws BSSException {

        HashMap<String, String[]> map = new HashMap<String, String[]>();
        if(resv == null){
            return map;
        }

        map.put("startSeconds", genHashVal(resv.getStartTime() + ""));
        map.put("endSeconds", genHashVal(resv.getEndTime() + ""));
        map.put("createSeconds", genHashVal(resv.getCreatedTime() + ""));
        map.put("bandwidth", genHashVal(resv.getBandwidth() + ""));
        map.put("status", genHashVal(resv.getStatus()));
        map.put("description", genHashVal(resv.getDescription()));
        map.put("gri", genHashVal(resv.getGlobalReservationId()));
        map.put("userLogin", genHashVal(resv.getLogin()));

        //set Token
        Token token = resv.getToken();
        if (token != null) {
            map.put("token", genHashVal(token.getValue()));
        }
        //set local path
        map.putAll(pathToHashMap(resv.getPath(PathType.LOCAL)));
        // set interdomain path
        map.putAll(pathToHashMap(resv.getPath(PathType.INTERDOMAIN)));
        return map;
    }

    /**
     * Converts HashMap to a Reservation Hibernate bean
     *
     * @param map a HashMap with parameters to initialize reservation
     * @return resv the converted Reservation
     */
    public static Reservation hashMapToReservation(HashMap<String, String[]> map){
        Reservation resv = new Reservation();
        if (map == null) {
            return resv;
        }
        resv.setStartTime(Long.parseLong(map.get("startSeconds")[0]));
        resv.setEndTime(Long.parseLong(map.get("endSeconds")[0]));
        resv.setCreatedTime(Long.parseLong(map.get("createSeconds")[0]));
        resv.setBandwidth(Long.parseLong(map.get("bandwidth")[0]));
        resv.setDescription(map.get("description")[0]);
        resv.setGlobalReservationId(map.get("gri")[0]);
        resv.setLogin(map.get("userLogin")[0]);

        //TODO: Fill-in path
        return resv;
    }

    /**
     * Converts Path Hibernate bean to a HashMap
     *
     * @param path the Path to convert
     * @return map the converted HashMap
     */
    public static HashMap<String, String[]> pathToHashMap(Path path) {

        HashMap<String, String[]> map = new HashMap<String, String[]>();
        ArrayList<String> layers = new ArrayList<String>();
        if (path == null) {
            return map;
        }
        Domain nextDomain = path.getNextDomain();
        Layer2Data layer2Data = path.getLayer2Data();
        Layer3Data layer3Data = path.getLayer3Data();
        MPLSData mplsData = path.getMplsData();
        List<PathElem> pathElems = path.getPathElems();
        ArrayList<String> pathListStr = new ArrayList<String>();
        String src = null;
        String dest = null;

        map.put("isExplicitPath", genHashVal(path.isExplicit() ? "true" : "false"));
        map.put("pathSetupMode", genHashVal(path.getPathSetupMode()));
        if(nextDomain != null){
            map.put("nextDomain", genHashVal(nextDomain.getTopologyIdent()));
        }
        if(layer3Data != null){
            src = layer3Data.getSrcHost();
            dest = layer3Data.getDestHost();
            map.put("source", genHashVal(src));
            map.put("destination", genHashVal(dest));
            //these are in the TCP/UDP headers, not IP headers, hence L4
            map.put("srcPort", genHashVal(layer3Data.getSrcIpPort() + ""));
            map.put("destPort", genHashVal(layer3Data.getDestIpPort() + ""));
            map.put("protocol", genHashVal(layer3Data.getProtocol()));
            map.put("dscp", genHashVal(layer3Data.getDscp()));
            map.put("layer", genHashVal("3"));
            layers.add("3");
        }

        if(layer2Data != null){
            src = layer2Data.getSrcEndpoint();
            dest = layer2Data.getDestEndpoint();
            map.put("source", genHashVal(src));
            map.put("destination", genHashVal(dest));
            layers.add("2");
        }
        map.put("layer", layers.toArray(new String[layers.size()]));

        if(mplsData != null){
            map.put("burstLimit", genHashVal(mplsData.getBurstLimit() + ""));
            map.put("lspClass", genHashVal(mplsData.getLspClass()));
        }

        String pathType = path.getPathHopType() == null ? "strict" : path.getPathHopType();
        map.put("pathType", genHashVal(pathType));

        ArrayList<String> pathHopInfo = new ArrayList<String>();
        for (PathElem pathElem: pathElems) {
            Link link = pathElem.getLink();
            if (link != null) {
                String linkId = link.getFQTI();
                pathListStr.add(linkId);
                pathHopInfo.add(getPathElemInfo(pathElem));
                map.putAll(vlanToHashMap(pathElem, src, dest, layer2Data));
            } else {
                log.error("Could not locate a link for pathElem, id: "+pathElem.getId());
            }
        }
        if (path.getPathType().equals(PathType.LOCAL)) {
            map.put("intradomainPath", pathListStr.toArray(new String[pathListStr.size()]));
            map.put("intradomainHopInfo", pathHopInfo.toArray(new String[pathHopInfo.size()]));
        } else {
            map.put("interdomainPath", pathListStr.toArray(new String[pathListStr.size()]));
            map.put("interdomainHopInfo", pathHopInfo.toArray(new String[pathHopInfo.size()]));
        }
        return map;
    }

    /**
     * Creates a ';' delimited String with detailed information about each hop
     * in a path.
     *
     * @param pathElem the pathElem for which to generate information
     * @return a ';' delimited String with detailed information about each hop
     */
     private static String getPathElemInfo(PathElem pathElem){
        Link link = pathElem.getLink();
        L2SwitchingCapabilityData l2scData = link.getL2SwitchingCapabilityData();
        String infoVal = link.getTrafficEngineeringMetric();
        String defaulSwcapType = PathManager.DEFAULT_SWCAP_TYPE;
        String defaulEncType = PathManager.DEFAULT_ENC_TYPE;
        if(l2scData != null){
            //TEMetric;swcap;enc;MTU;VLANRangeAvail;SuggestedVLANRange
            infoVal += ";l2sc;ethernet";
            infoVal += ";" + l2scData.getInterfaceMTU();
            infoVal += ";" + pathElem.getLinkDescr();
            infoVal += ";null";
        }else{
            //TEMetric;swcap;enc;MTU;capbility
            infoVal += ";" + defaulSwcapType + ";" + defaulEncType + ";unimplemented";
        }

        return infoVal;
     }

    /**
     * Converts PathElem Hibernate bean of a layer2 link to a HashMap
     *
     * @param elem the PathElem to convert
     * @param src the source URN of the reservation
     * @param dest the destination URN of the reservation
     * @param layer2Data the layer 2 data associated with a reservation
     * @return the converted HashMap
     */
    private static HashMap<String, String[]> vlanToHashMap(PathElem elem, String src,
                                                    String dest,
                                                    Layer2Data layer2Data){
        HashMap<String, String[]> map = new HashMap<String, String[]>();
        if(layer2Data == null){
            return map;
        }

        String linkId = elem.getLink().getFQTI();
        String descr = elem.getDescription();
        String tagField = "";
        if(linkId.equals(src)){
            tagField = "tagSrcPort";
            try{
                int vtag = Integer.parseInt(descr);
                map.put(tagField, genHashVal(vtag > 0 ? "true" : "false"));
                map.put("srcVtag", genHashVal(descr));
            }catch(Exception e){}
        }else if(linkId.equals(dest)){
            tagField = "tagDestPort";
            try{
                int vtag = Integer.parseInt(descr);
                map.put(tagField, genHashVal(vtag > 0 ? "true" : "false"));
                map.put("destVtag", genHashVal(descr));
            }catch(Exception e){}
        }

        return map;
    }

    /**
     * Generates a String array from a String
     *
     * @param value the String to convert
     * @return the converted array
     */
    private static String[] genHashVal(String value){
        if(value == null){
            return null;
        }
        String[] array = new String[1];
        array[0] = value;
        return array;
    }

     /**
      * Merge additional hops in the new path with an original path while maintaining any
      * objects in that path. Useful for pathfinders that only work on paths containing
      * IDRefs.
      *
      * @param origPathInfo the original path that may have objects to maintain
      * @param newPathInfo the new path the may only contain references
      * @param saveToNew if true writes merged path to newPathInfo, otherwise writes to origPathInfo
      */
      public static void mergePathInfo(PathInfo origPathInfo, PathInfo newPathInfo, boolean saveToNew) throws BSSException{
        CtrlPlanePathContent mergedPath = new CtrlPlanePathContent();
        int i = 0;
        int j = 0;
        CtrlPlanePathContent origPath = origPathInfo.getPath();
        CtrlPlanePathContent newPath = newPathInfo.getPath();
        HashMap<String, CtrlPlaneHopContent> hopMap = new  HashMap<String, CtrlPlaneHopContent>();
        if(newPath == null || newPath.getHop() == null ||
           newPath.getHop().length == 0){
            return;
        }else if(origPath == null || origPath.getHop() == null){
            origPath = new CtrlPlanePathContent();
            origPath.setHop(new CtrlPlaneHopContent[0]);
        }

        CtrlPlaneHopContent[] origHops = origPath.getHop();
        CtrlPlaneHopContent[] newHops = newPath.getHop();
        for(CtrlPlaneHopContent origHop : origHops){
            String urn = TypeConverter.hopToURN(origHop);
            hopMap.put(urn, origHop);
        }
        for(CtrlPlaneHopContent newHop : newHops){
            String urn = TypeConverter.hopToURN(newHop);
            if(hopMap.containsKey(urn)){
                mergedPath.addHop(hopMap.get(urn));
            }else{
                mergedPath.addHop(newHop);
            }
        }
        if(saveToNew){
            newPathInfo.setPath(mergedPath);
        }else{
            origPathInfo.setPathType(newPathInfo.getPathType());
            origPathInfo.setPath(mergedPath);
        }
      }
}
