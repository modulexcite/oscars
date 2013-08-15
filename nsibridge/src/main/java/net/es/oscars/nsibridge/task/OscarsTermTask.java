package net.es.oscars.nsibridge.task;


import net.es.oscars.api.soap.gen.v06.CancelResContent;
import net.es.oscars.api.soap.gen.v06.CancelResReply;
import net.es.oscars.nsibridge.beans.SimpleRequest;
import net.es.oscars.nsibridge.beans.db.ConnectionRecord;
import net.es.oscars.nsibridge.ifces.StateException;
import net.es.oscars.nsibridge.prov.*;
import net.es.oscars.nsibridge.state.life.NSI_Life_Event;
import net.es.oscars.nsibridge.state.life.NSI_Life_SM;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.task.Task;
import net.es.oscars.utils.task.TaskException;
import org.apache.log4j.Logger;

public class OscarsTermTask extends Task  {

    private String connId = "";
    private static final Logger log = Logger.getLogger(OscarsTermTask.class);
    public OscarsTermTask(String connId) {
        this.connId = connId;

        this.scope = "oscars";
    }
    public void onRun() throws TaskException {
        log.debug(this.id + " starting");
        try {
            super.onRun();
            ConnectionRecord cr = NSI_Util.getConnectionRecord(connId);
            if (cr!= null) {
                log.debug("found connection entry for connId: "+connId);
            } else {
                throw new TaskException("could not find connection entry for connId: "+connId);
            }


            RequestHolder rh = RequestHolder.getInstance();
            NSI_SM_Holder smh = NSI_SM_Holder.getInstance();


            SimpleRequest req = rh.findSimpleRequest(connId);
            NSI_Life_SM tsm = smh.getLifeStateMachines().get(connId);
            String oscarsGri = cr.getOscarsGri();


            if (req != null) {
                log.debug("found request for connId: "+connId);
            }

            if (tsm != null) {
                log.debug("found state machine for connId: "+connId);
            }


            CancelResContent rc = NSI_OSCARS_Translation.makeOscarsCancel(oscarsGri);

            try {
                CancelResReply reply = OscarsProxy.getInstance().sendCancel(rc);
                if (reply.getStatus().equals("FAILED")) {
                    tsm.process(NSI_Life_Event.LOCAL_TERM_FAILED);
                } else {
                    tsm.process(NSI_Life_Event.LOCAL_TERM_CONFIRMED);
                }
            } catch (OSCARSServiceException e) {
                try {
                    tsm.process(NSI_Life_Event.LOCAL_TERM_FAILED);
                } catch (StateException e1) {
                    e.printStackTrace();
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            this.onFail();
        }

        log.debug(this.id + " finishing");

        this.onSuccess();
    }

}
