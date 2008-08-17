package net.es.oscars.tss.terce;

import org.apache.log4j.*;
import java.rmi.RemoteException;
import java.util.*;

import net.es.oscars.*;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.tss.*;

import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneTopologyContent;

import edu.internet2.hopi.dragon.terce.ws.types.tedb.*;
import edu.internet2.hopi.dragon.terce.ws.service.*;

/**
 * A TEDB that accesses a TERCE through the TERCE web service interface.
 *  
 * @author Andrew Lake (alake@internet2.edu)
 */
public class TERCEDatabase implements TEDB{

    private Properties props;
    private Logger log;
    
   /**
    * Constructor that initializes TERCE properties from oscars.properties file
    *
    */
    public TERCEDatabase(){
        this.log = Logger.getLogger(this.getClass());
        PropHandler propHandler = new PropHandler("oscars.properties");
        this.props = propHandler.getPropertyGroup("terce", true);
    }
    
    /**
     * Retrieves topology from TERCE by issuing a selectNetworkTopology 
     * web service call. A type is provided but the only currently supported
     * type is <i>all</I>. In the future other types will be supported.
     *
     * @param type the type of topology top retrieve. Currently only <i>all</i>.
     * @return the topology returned by the TERCE
     * @throws TSSException
     */
    public CtrlPlaneTopologyContent selectNetworkTopology(String type) 
            throws TSSException{
        this.log.info("selectNetworkTopology.start"); 
        TERCEStub terce = null;
        SelectNetworkTopology selectTopology = null;
        SelectNetworkTopologyContent request = null;
        SelectNetworkTopologyResponse response = null;
        SelectNetworkTopologyResponseContent responseContent = null;
        CtrlPlaneTopologyContent topology = null;
        String terceURL = this.props.getProperty("url");
        SelectTypes topoType = this.stringToSelectType(type);
        this.log.info("url=" + terceURL); 
        
        if(topoType == null){
            throw new TSSException("Invalid topology type specifed");
        }
        
        try {
            terce = new TERCEStub(terceURL);
            selectTopology = new SelectNetworkTopology();
            request = new SelectNetworkTopologyContent();
            /* Format Request */ 
            request.setFrom(topoType);
            
            /* Send request and get response*/
            selectTopology.setSelectNetworkTopology(request);
            
            response = terce.selectNetworkTopology(selectTopology);
            responseContent = response.getSelectNetworkTopologyResponse();
            topology = responseContent.getTopology();                
        } catch (RemoteException e) {
            throw new TSSException("Remote TERCE Exception: " + e.getMessage());
        } catch (TEDBFaultMessage e) {
            throw new TSSException("TERCE Exception while handling request: " + e.getMessage());
        }catch(Exception e){
            throw new TSSException("Unable to contact TERCE: " + e.getMessage());
        }
        this.log.info("selectNetworkTopology.end"); 
        return topology;
    }
    
    /**
     * Sends an insertNetworkTopology WS request to the TERCE with the
     * topology to be inserted.
     *
     * @param topology the topology to be inserted
     */
    public void insertNetworkTopology(CtrlPlaneTopologyContent topology)
            throws TSSException{
        this.log.info("insertNetworkTopology.start"); 
        TERCEStub terce = null;
        InsertNetworkTopology insertTopology = null;
        InsertNetworkTopologyContent request = null;
        InsertNetworkTopologyResponse response = null;
        InsertNetworkTopologyResponseContent responseContent = null;
        String terceURL = this.props.getProperty("url");
        this.log.info("url=" + terceURL);
        
        try {
            terce = new TERCEStub(terceURL);
            insertTopology = new InsertNetworkTopology();
            request = new InsertNetworkTopologyContent();
            
            /* Format and send Request */ 
            request.setTopology(topology);
            insertTopology.setInsertNetworkTopology(request);
            
            response = terce.insertNetworkTopology(insertTopology);
            responseContent = response.getInsertNetworkTopologyResponse();      
            this.log.info("TERCE Result " + responseContent.getResultCode() +
                ":  " + responseContent.getResultMessage());
        } catch (RemoteException e) {
            throw new TSSException("Remote TERCE Exception: " + e.getMessage());
        } catch (TEDBFaultMessage e) {
            throw new TSSException("TERCE Exception while handling request: " + e.getMessage());
        }catch(Exception e){
            throw new TSSException("Unable to contact TERCE: " + e.getMessage());
        }
        
        this.log.info("insertNetworkTopology.end"); 
        
        return;
    }
    
    /**
     * Converts string to enumerated type for axis
     *
     * @param type string to be converted
     * @return enumerated type for TERCE
     * @throws TSSException
     */
    private SelectTypes stringToSelectType(String type) throws TSSException{
        SelectTypes newType = null;
        
        if(type.toLowerCase().equals("all")){
            newType =  SelectTypes.all;
        }else if(type.toLowerCase().equals("adjacentdomains")){
            newType = SelectTypes.adjacentDomains;
        }else if(type.toLowerCase().equals("delta")){
            newType = SelectTypes.delta;
        }else if(type.toLowerCase().equals("nodes")){
            newType = SelectTypes.nodes;
        }else if(type.toLowerCase().equals("internetworklinks")){
            newType = SelectTypes.internetworkLinks;
        }
        
        return newType;
    }
}