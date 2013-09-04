package net.es.oscars.nsibridge.state.resv;


import net.es.oscars.nsibridge.beans.db.ConnectionRecord;
import net.es.oscars.nsibridge.config.SpringContext;
import net.es.oscars.nsibridge.config.TimingConfig;
import net.es.oscars.nsibridge.ifces.CallbackMessages;
import net.es.oscars.nsibridge.ifces.NsiResvMdl;
import net.es.oscars.nsibridge.ifces.StateException;
import net.es.oscars.nsibridge.ifces.StateMachineType;
import net.es.oscars.nsibridge.oscars.*;
import net.es.oscars.nsibridge.prov.DB_Util;
import net.es.oscars.nsibridge.prov.NSI_SM_Holder;
import net.es.oscars.nsibridge.prov.NSI_Util;
import net.es.oscars.nsibridge.prov.RequestHolder;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.ifce.ServiceException;
import net.es.oscars.nsibridge.task.*;
import net.es.oscars.utils.task.TaskException;
import net.es.oscars.utils.task.sched.Workflow;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.Set;
import java.util.UUID;


public class NSI_UP_Resv_Impl implements NsiResvMdl {
    protected String connectionId = "";
    protected TimingConfig tc;
    private static final Logger log = Logger.getLogger(NSI_UP_Resv_Impl.class);


    public NSI_UP_Resv_Impl(String connId) {
        tc = SpringContext.getInstance().getContext().getBean("timingConfig", TimingConfig.class);
        connectionId = connId;
    }

    @Override
    public UUID localRollback(String correlationId) {
        // TODO
        return null;
    }


    @Override
    public UUID localCheck(String correlationId) {
        long now = new Date().getTime();
        Workflow wf = Workflow.getInstance();
        UUID taskId = null;


        ConnectionRecord cr = null;
        try {
            NSI_Util.isConnectionOK(connectionId);
            cr = DB_Util.getConnectionRecord(connectionId);
        } catch (ServiceException e) {
            try {
                NSI_Resv_SM.handleEvent(connectionId, correlationId, NSI_Resv_Event.LOCAL_RESV_CHECK_FL);
            } catch (StateException e1) {
                e1.printStackTrace();
            }
        }

        NSI_SM_Holder smh = NSI_SM_Holder.getInstance();
        NSI_Resv_SM rsm = smh.getResvStateMachines().get(connectionId);

        RequestHolder rh = RequestHolder.getInstance();


        OscarsResvOrModifyTask ost = new OscarsResvOrModifyTask();
        ost.setCorrelationId(correlationId);
        ost.setSuccessEvent(NSI_Resv_Event.LOCAL_RESV_CHECK_CF);
        ost.setFailEvent(NSI_Resv_Event.LOCAL_RESV_CHECK_FL);
        ost.setSmt(StateMachineType.RSM);
        ost.setOscarsOp(OscarsOps.RESERVE);


        Double d = (tc.getTaskInterval() * 1000);
        Long when = now + d.longValue();
        log.info("scheduling Modify / Resv for connId:" + connectionId + ", will run in " + d + "ms");
        try {
            taskId = wf.schedule(ost, when);
            log.info("task id: "+taskId);

        } catch (TaskException e) {
            log.error(e);
            try {
                DB_Util.saveException(connectionId, correlationId, e.toString());
                NSI_Resv_SM.handleEvent(connectionId, correlationId, NSI_Resv_Event.LOCAL_RESV_CHECK_FL);
                rh.removeResvRequest(connectionId);
            } catch (ServiceException ex) {
                log.error(ex);
            } catch (StateException ex) {
                log.error(ex);
            }
        }

        return taskId;
    }

    @Override
    public UUID localHold(String correlationId) {
        UUID taskId = null;
        log.debug("localHold: " + connectionId);
        // nothing to do - everything is done in the check phase
        return taskId;
    }

    @Override
    public UUID localCommit(String correlationId) {
        UUID taskId = null;

        log.debug("localCommit: " + connectionId);
        NSI_SM_Holder smh = NSI_SM_Holder.getInstance();
        NSI_Resv_SM rsm = smh.getResvStateMachines().get(connectionId);

        String exString = "";

        boolean okCommit = true;
        try {
            DB_Util.commitResvRecord(connectionId);
            Set<UUID> taskIds = rsm.process(NSI_Resv_Event.LOCAL_RESV_COMMIT_CF, correlationId);
            taskId = taskIds.iterator().next();
            DB_Util.persistStateMachines(connectionId);
        } catch (ServiceException ex) {
            log.error(ex);
            exString = ex.toString();
            okCommit = false;
        } catch (StateException ex) {
            log.error(ex);
            exString = ex.toString();
            okCommit = false;
        }

        if (!okCommit) {
            try {
                DB_Util.saveException(connectionId, correlationId, exString);
                Set<UUID> taskIds = rsm.process(NSI_Resv_Event.LOCAL_RESV_COMMIT_FL, correlationId);
                taskId = taskIds.iterator().next();
                DB_Util.persistStateMachines(connectionId);
            } catch (StateException ex) {
                log.error(ex);
            } catch (ServiceException ex) {
                log.error(ex);
            }
        }

        return taskId;
    }

    @Override
    public UUID localAbort(String correlationId) {

        UUID taskId = null;
        boolean okAbort = true;

        NSI_SM_Holder smh = NSI_SM_Holder.getInstance();
        NSI_Resv_SM rsm = smh.getResvStateMachines().get(connectionId);
        String exString = "";

        ConnectionRecord cr = null;
        try {
            NSI_Util.isConnectionOK(connectionId);
            cr = DB_Util.getConnectionRecord(connectionId);
            if (cr.getOscarsGri() == null) {
                okAbort = false;
            }
        } catch (ServiceException e) {
            log.error(e);
            okAbort = false;
        }

        try {
            DB_Util.abortResvRecord(connectionId);
            Set<UUID> taskIds = rsm.process(NSI_Resv_Event.LOCAL_RESV_ABORT_CF, correlationId);
            taskId = taskIds.iterator().next();
            DB_Util.persistStateMachines(connectionId);
        } catch (ServiceException ex) {
            exString = ex.toString();
            log.error(ex);

            okAbort = false;
        } catch (StateException ex) {
            exString = ex.toString();
            log.error(ex);
            okAbort = false;
        }


        if (!okAbort) {
            try {
                DB_Util.saveException(connectionId, correlationId, exString);
                Set<UUID> taskIds = rsm.process(NSI_Resv_Event.LOCAL_RESV_ABORT_FL, correlationId);
                taskId = taskIds.iterator().next();
                DB_Util.persistStateMachines(connectionId);
            } catch (StateException ex) {
                log.error(ex);
            } catch (ServiceException ex) {
                log.error(ex);
            }
        }
        return taskId;


        // TODO: cancel OR rollback modify

        /*
        long now = new Date().getTime();
        Workflow wf = Workflow.getInstance();

        // submit the oscars cancel()
        Double d = (tc.getTaskInterval() * 1000);
        Long when = now + d.longValue();


        OscarsCancelTask ost = new OscarsCancelTask();
        ost.setCorrelationId(correlationId);
        ost.setOscarsOp(OscarsOps.CANCEL);
        ost.setSmt(StateMachineType.RSM);
        ost.setSuccessEvent(NSI_Resv_Event.LOCAL_RESV_ABORT_CF);
        ost.setFailEvent(NSI_Resv_Event.LOCAL_RESV_ABORT_FL);

        try {
            taskId = wf.schedule(ost, when);
        } catch (TaskException e) {
            e.printStackTrace();
        }
        */

    }
    @Override
    public UUID sendRsvCF(String correlationId) {
        UUID taskId = null;

        long now = new Date().getTime();
        Workflow wf = Workflow.getInstance();

        Double d = (tc.getTaskInterval() * 1000);
        Long when = now + d.longValue();
        SendNSIMessageTask sendNsiMsg = new SendNSIMessageTask();
        sendNsiMsg.setCorrId(correlationId);
        sendNsiMsg.setConnId(connectionId);
        sendNsiMsg.setMessage(CallbackMessages.RESV_CF);



        try {
            taskId = wf.schedule(sendNsiMsg, when);
        } catch (TaskException e) {
            e.printStackTrace();
        }
        return taskId;
    }

    @Override
    public UUID sendRsvFL(String correlationId) {
        long now = new Date().getTime();
        Workflow wf = Workflow.getInstance();
        UUID taskId = null;

        Double d = (tc.getTaskInterval() * 1000);
        Long when = now + d.longValue();

        SendNSIMessageTask sendNsiMsg = new SendNSIMessageTask();
        sendNsiMsg.setCorrId(correlationId);
        sendNsiMsg.setConnId(connectionId);
        sendNsiMsg.setMessage(CallbackMessages.RESV_FL);

        try {
            taskId = wf.schedule(sendNsiMsg, when);
        } catch (TaskException e) {
            e.printStackTrace();
        }
        return taskId;

    }

    @Override
    public UUID sendRsvCmtCF(String correlationId) {
        long now = new Date().getTime();
        Workflow wf = Workflow.getInstance();
        UUID taskId = null;

        Double d = (tc.getTaskInterval() * 1000);
        Long when = now + d.longValue();

        SendNSIMessageTask sendNsiMsg = new SendNSIMessageTask();
        sendNsiMsg.setCorrId(correlationId);
        sendNsiMsg.setConnId(connectionId);
        sendNsiMsg.setMessage(CallbackMessages.RESV_CM_CF);

        try {
            taskId = wf.schedule(sendNsiMsg, when);
        } catch (TaskException e) {
            e.printStackTrace();
        }
        return taskId;
    }

    @Override
    public UUID sendRsvCmtFL(String correlationId) {
        UUID taskId = null;

        long now = new Date().getTime();
        Workflow wf = Workflow.getInstance();

        Double d = (tc.getTaskInterval() * 1000);
        Long when = now + d.longValue();

        SendNSIMessageTask sendNsiMsg = new SendNSIMessageTask();
        sendNsiMsg.setCorrId(correlationId);
        sendNsiMsg.setConnId(connectionId);
        sendNsiMsg.setMessage(CallbackMessages.RESV_CM_FL);

        try {
            taskId = wf.schedule(sendNsiMsg, when);
        } catch (TaskException e) {
            e.printStackTrace();
        }
        return taskId;
    }

    @Override
    public UUID sendRsvAbtCF(String correlationId) {
        UUID taskId = null;
        long now = new Date().getTime();
        Workflow wf = Workflow.getInstance();

        Double d = (tc.getTaskInterval() * 1000);
        Long when = now + d.longValue();

        SendNSIMessageTask sendNsiMsg = new SendNSIMessageTask();
        sendNsiMsg.setCorrId(correlationId);
        sendNsiMsg.setConnId(connectionId);
        sendNsiMsg.setMessage(CallbackMessages.RESV_AB_CF);


        try {
            taskId = wf.schedule(sendNsiMsg, when);
        } catch (TaskException e) {
            e.printStackTrace();
        }
        return taskId;
    }

    @Override
    public UUID sendRsvTimeout(String correlationId) {

        UUID taskId = null;

        try {
            DB_Util.abortResvRecord(connectionId);
        } catch (ServiceException ex) {
            log.error(ex);
        }

        long now = new Date().getTime();
        Workflow wf = Workflow.getInstance();

        Double d = (tc.getTaskInterval() * 1000);
        Long when = now + d.longValue();
        try {
            Long notId = DB_Util.makeNotification(connectionId, null, CallbackMessages.RESV_TIMEOUT);

            SendNSIMessageTask sendNsiMsg = new SendNSIMessageTask();
            sendNsiMsg.setCorrId(correlationId);
            sendNsiMsg.setConnId(connectionId);
            sendNsiMsg.setMessage(CallbackMessages.RESV_TIMEOUT);
            sendNsiMsg.setNotificationId(notId);

            taskId = wf.schedule(sendNsiMsg, when);
        } catch (ServiceException e) {
            log.error(e);
        } catch (TaskException e) {
            log.error(e);
        }

        return taskId;
    }





}
