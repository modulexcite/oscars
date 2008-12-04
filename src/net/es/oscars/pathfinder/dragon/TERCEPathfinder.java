package net.es.oscars.pathfinder.dragon;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;
import java.rmi.RemoteException;

import net.es.oscars.*;
import net.es.oscars.oscars.*;
import net.es.oscars.pathfinder.*;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.pathfinder.generic.*;
import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.topology.*;

import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.log4j.*;

import edu.internet2.hopi.dragon.terce.ws.types.rce.*;
import edu.internet2.hopi.dragon.terce.ws.service.*;

/**
 * TERCEPathfinder that uses TERCE to calculate path
 *
 * @author Andrew Lake (alake@internet2.edu), David Robertson (dwrobertson@lbl.gov)
 */
public class TERCEPathfinder extends Pathfinder implements PCE {
    private Properties props;
    private Logger log;
    
    /**
     * Constructor that initializes TERCE properties from oscars.properties file
     *
     */
    public TERCEPathfinder(String dbname) {
        super(dbname);
        this.log = Logger.getLogger(this.getClass());
        PropHandler propHandler = new PropHandler("oscars.properties");
        this.props = propHandler.getPropertyGroup("terce", true);
    }

    /**
     * Finds a path given just source and destination or by expanding
     * a path the user explicitly sets
     *
     * @param pathInfo PathInfo instance containing interdomain hops
     * @return intradomain path used for resource scheduling
     * @throws PathfinderException
     */
    public PathInfo findPath(PathInfo pathInfo, Reservation reservation) throws PathfinderException{

        InterdomainPathfinder interPathfinder = new InterdomainPathfinder(this.dbname);
        PathInfo intraPathInfo = interPathfinder.findPath(pathInfo, reservation);
        CtrlPlaneHopContent[] intraHops = intraPathInfo.getPath().getHop();
        CtrlPlanePathContent intraPath = new CtrlPlanePathContent();
        boolean firstHop = true;
        for(int i = 0; i < (intraHops.length - 1); i++){
            String src = TypeConverter.hopToURN(intraHops[i]);
            String dest = TypeConverter.hopToURN(intraHops[i+1]);
            Link srcLink = null;
            Link destLink = null;
            try{
                srcLink = TopologyUtil.getLink(src,this.dbname);
                destLink = TopologyUtil.getLink(dest,this.dbname);
            }catch(BSSException e){
                throw new PathfinderException("Error processing " +
                    "intra-domain path: " +  e.getMessage());
            }
            Link srcRemoteLink = srcLink.getRemoteLink();
            Node srcNode = srcLink.getPort().getNode();
            Node destNode = destLink.getPort().getNode();
            
            if(firstHop){
                intraPath.addHop(intraHops[i]);
            }
            if(srcNode.equals(destNode) || 
                (srcRemoteLink != null && srcRemoteLink.equals(destLink))){
                intraPath.addHop(intraHops[i+1]);
            }else{
                CtrlPlanePathContent tercePath = this.terce(src, dest);
                CtrlPlaneHopContent[] terceHops = tercePath.getHop();
                for(int j = 1; j < terceHops.length; j++){
                    //if statement maintains given objects
                    if(TypeConverter.hopToURN(terceHops[j]).equals(src)){
                         intraPath.addHop(intraHops[i]);
                    }else if(TypeConverter.hopToURN(terceHops[j]).equals(dest)){
                       intraPath.addHop(intraHops[i+1]);
                    }else{
                        intraPath.addHop(terceHops[j]);
                    }
                }
            }
            firstHop = false;
        }
        
        intraPathInfo.setPath(intraPath);

        return intraPathInfo;
    }
    
    /**
     * Extract layer2Info/srcEndpoint and path then passes it to super class
     * findIngress method.
     *
     * @param pathInfo the PathInfo element in which to find an ingress
     * @return a String containing the link-id URN of the ingress
     * @throws PathfinderException
     */
    public String findIngress(PathInfo pathInfo) throws PathfinderException{
        this.log.debug("findIngress.start");
        CtrlPlanePathContent path = pathInfo.getPath();
        Layer2Info layer2Info = pathInfo.getLayer2Info();
        if(layer2Info == null){
            throw new PathfinderException("This IDC requires layer2Info");
        }
        String src = layer2Info.getSrcEndpoint();
        
        this.log.debug("findIngress.end");
        return super.findIngress(src, path);
    }

    /**
     * Retrieves path calculation from TERCE
     *
     * @param src string with IP address of source host
     * @param dest string with IP address of destination host
     * @return responseContent list of hops in path
     * @throws PathfinderException
     */
    public CtrlPlanePathContent terce(String src, String dest)
            throws PathfinderException {

        String terceURL = this.props.getProperty("url");
        FindPath fp = new FindPath();
        FindPathContent request = new FindPathContent();
        FindPathResponse response = null;
        FindPathResponseContent responseContent= null;
        CtrlPlanePathContent path = null;
        CtrlPlaneHopContent[] hops = null;
        TERCEStub terce= null;
        ConfigurationContext configContext = null;
        String errMessage = "";
        String repo = System.getenv("CATALINA_HOME");
        String axis2Config = "";
        
        this.log.info("terce.start");
        this.log.info("src=" + src);
        this.log.info("dest=" + dest);
        repo += (repo.endsWith("/") ? "" :"/");
        repo += "shared/classes/terce.conf/repo/";
        axis2Config = repo + "axis2.xml";
        
        /* Calculate path */
        try {
            configContext = ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(repo, axis2Config);
            terce = new TERCEStub(configContext, terceURL);

            /* Format Request */
            request.setSrcEndpoint(src);
            request.setDestEndpoint(dest);
            request.setVtag("any");
            request.setPreferred(true);
            request.setStrict(true);
            request.setAllvtags(true);

            /* Send request and get response*/
            fp.setFindPath(request);
            response = terce.findPath(fp);
            responseContent = response.getFindPathResponse();
            path = responseContent.getPath();
            hops = path.getHop();

            log.info("terce.path.start");
            for(int i = 0; i < hops.length; i++){
                log.info("terce.path.hop=" + TypeConverter.hopToURN(hops[i]));
            }
            log.info("terce.path.end");
            this.log.info("terce.end");
        }catch (RemoteException e) {
            errMessage = e.getMessage();
        }catch (RCEFaultMessage e) {
            errMessage = e.getFaultMessage().getMsg();
        }catch (Exception e) {
            errMessage = e.getMessage();
        }finally{
            //must terminate configContext to prevent memory leak
            errMessage += this.cleanUp(configContext);
            
            //throw exception
            if(!errMessage.equals("")){
                throw new PathfinderException(errMessage);
            }
        }

        return responseContent.getPath();
    }
    
    /**
     * Prevents memory leak in axis2 client
     *
     * @param configContext an Axis2 ConfigurationContext to delete
     * @return an error message if one occurred. An empty string otherwise.
     */
    private String cleanUp(ConfigurationContext configContext){
        if(configContext == null){
            return "";
        }
        
        try{
            configContext.terminate();
        }catch(Exception e){
            return "Unable to disconnect from TERCE: " + e.getMessage();
        }
        
        return "";
    }
}
