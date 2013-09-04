package net.es.oscars.nsibridge.task;


import net.es.oscars.nsibridge.beans.db.ConnectionRecord;
import net.es.oscars.nsibridge.beans.db.ResvRecord;
import net.es.oscars.nsibridge.common.PersistenceHolder;
import net.es.oscars.nsibridge.oscars.OscarsOps;
import net.es.oscars.nsibridge.oscars.OscarsProvQueue;
import net.es.oscars.nsibridge.prov.DB_Util;
import net.es.oscars.nsibridge.prov.NSI_Util;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.ifce.ServiceException;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.LifecycleStateEnumType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.ProvisionStateEnumType;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.persistence.EntityManager;
import java.util.Date;
import java.util.List;

public class ProvMonitor implements Job {

    private static final Logger log = Logger.getLogger(ProvMonitor.class);


    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        Date now = new Date();
        List<ConnectionRecord> recordList;
        try {
             recordList = DB_Util.getConnectionRecords();

        } catch (ServiceException ex) {
            log.error(ex);
            return;
        }

        for (ConnectionRecord cr: recordList) {
            if (cr.getLifecycleState() == null) {
                continue;
            }

            // do not set up oscars if we are TERMINATING / TERMINATED / FAILED
            if (cr.getLifecycleState().equals(LifecycleStateEnumType.CREATED)) {
                if (cr.getProvisionState() == null) {
                    continue;
                }

                if (cr.getProvisionState().equals(ProvisionStateEnumType.PROVISIONED)) {
                    String connId = cr.getConnectionId();

                    ResvRecord rr = ConnectionRecord.getCommittedResvRecord(cr);
                    if (rr == null) {
                        // log.debug("no committed reserve record for: "+connId);
                        continue;
                    } else {
                        // log.debug("found committed reserve record for: "+connId);
                    }

                    if (rr.getStartTime().before(now) && rr.getEndTime().after(now)) {
                        OscarsProvQueue.getInstance().scheduleOp(connId, OscarsOps.SETUP);
                        // log.info("should start SETUP: "+connId);
                    } else {
                        // log.debug("now: "+now+" start: "+rr.getStartTime()+" end: "+rr.getEndTime());
                    }
                }

                if (cr.getProvisionState().equals(ProvisionStateEnumType.RELEASED)) {
                    String connId = cr.getConnectionId();
                    OscarsProvQueue.getInstance().scheduleOp(connId, OscarsOps.TEARDOWN);
                    // log.info("should start TEARDOWN: "+connId);

                    ResvRecord rr = ConnectionRecord.getCommittedResvRecord(cr);
                    if (rr == null) {
                        continue;
                    }


                    if (rr.getEndTime().before(now)) {
                        // log.info("should start teardown "+connId);
                    }
                }
            }


        }


    }



}
