package net.es.oscars.coord.actions;

import net.es.oscars.coord.runtimepce.PCERuntimeAction;
import net.es.oscars.utils.sharedConstants.NotifyRequestTypes;
import org.apache.log4j.Logger;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;

import net.es.oscars.coord.req.CoordRequest;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.utils.sharedConstants.StateEngineValues;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.topology.PathTools;


/**
 * Process a committed event from another IDC
 * 
 * @author lomax
 *
 */
public class ModifyResvCompletedAction extends CoordAction <ResDetails,Object> {

    private static final long       serialVersionUID = 1L;
    private static Logger LOG = Logger.getLogger(CreateResvCompletedAction.class.getName());
    private OSCARSNetLogger netLogger = null;
    private static final String moduleName = ModuleName.COORD;
    
    @SuppressWarnings("unchecked")
    public ModifyResvCompletedAction (String name, CoordRequest request, ResDetails resDetails) {
        super (name, request, resDetails);
    }
    
    public void execute()  {
        String method = "execute";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        netLogger.init(ModifyResvCompletedAction.moduleName,this.getCoordRequest().getTransactionId());
        netLogger.setGRI(this.getCoordRequest().getGRI());
        LOG.debug(netLogger.start(method));

        boolean localRes = true;
        String localDomain = PathTools.getLocalDomainId();
        boolean lastDomain = true;
        ResDetails resDetails = this.getRequestData();
        CtrlPlanePathContent reservedPath = resDetails.getReservedConstraint().getPathInfo().getPath();
        try {
            if (reservedPath != null) {
                localRes = PathTools.isPathLocalOnly(reservedPath);
                String domain = PathTools.getLastDomain(reservedPath);
                lastDomain = localDomain.equals(domain);
                domain = PathTools.getFirstDomain(reservedPath);
            }
        } catch (OSCARSServiceException e) {
            LOG.error (netLogger.error(method, ErrSev.MINOR,"Cannot process PCEData " + e));
            this.fail(e);
            return;
        }
        
        // Update local status of the reservation to RESERVED
        RMUpdateStatusAction rmUpdateStatusAction = new RMUpdateStatusAction (this.getName() + "-RMUpdateStatusAction",
                                                                              this.getCoordRequest(),
                                                                              this.getCoordRequest().getGRI(),
                                                                              StateEngineValues.RESERVED);
        rmUpdateStatusAction.execute();
        
        if (rmUpdateStatusAction.getResultData() != null) {
            LOG.debug(netLogger.getMsg(method,"State was set to " + rmUpdateStatusAction.getResultData().getStatus()));
        } else {
            LOG.error(netLogger.error(method,ErrSev.MINOR, "rmUpdateStatus resultData is null."));
        }
        
        if (!localRes && !lastDomain) {
            try {
                // Send CREATE_RESV_COMPLETED event to the next IDC
                String nextDomain = PathTools.getNextDomain (resDetails.getReservedConstraint().getPathInfo().getPath(),
                                                             PathTools.getLocalDomainId());
                ReservationCompletedForwarder forwarder = new ReservationCompletedForwarder (this.getName() + "-ModifyResvCompletedForwarder",
                                                                                             this.getCoordRequest(),
                                                                                             NotifyRequestTypes.RESV_MODIFY_COMPLETED,
                                                                                             nextDomain,
                                                                                             resDetails);
                forwarder.execute();
    
                if (forwarder.getState() == CoordAction.State.FAILED) {
                    LOG.error(netLogger.error(method,ErrSev.MAJOR,
                                              "notifyRequest failed in PCERuntimeAction.setResultData with exception " +
                                              forwarder.getException().getMessage()));
                    this.fail(forwarder.getException());
                    return;
                }
            } catch (OSCARSServiceException e) {
                LOG.error (netLogger.error(method, ErrSev.CRITICAL,"Cannot forward message " + e));
                this.fail(e);
                return;                        
            }
        }
        // Release the RuntimePCE global lock
        PCERuntimeAction.releaseMutex(this.getCoordRequest().getGRI());

        this.setResultData(null);           
        this.executed();
    }  
}
