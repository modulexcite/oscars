package net.es.oscars.nsibridge.state.resv;


import net.es.oscars.nsibridge.ifces.NsiResvMdl;


public class NSI_UP_Resv_Impl implements NsiResvMdl {
    String connectionId = "";
    public NSI_UP_Resv_Impl(String connId) {
        connectionId = connId;
    }


    /*
    @Override
    public void doLocalResv() {
        long now = new Date().getTime();

        Workflow wf = Workflow.getInstance();
        Task oscarsResv = new OscarsResvTask(connectionId);

        try {
            wf.schedule(oscarsResv, now + 1000);
        } catch (TaskException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendNsiResvCF() {
        long now = new Date().getTime();

        Workflow wf = Workflow.getInstance();
        Task sendNsiMsg = new SendNSIMessageTask(connectionId, CallbackMessages.RESV_CF);

        try {
            wf.schedule(sendNsiMsg, now + 1000);
        } catch (TaskException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendNSIResvFL() {
        long now = new Date().getTime();

        Workflow wf = Workflow.getInstance();
        Task sendNsiMsg = new SendNSIMessageTask(connectionId, CallbackMessages.RESV_FL);

        try {
            wf.schedule(sendNsiMsg, now + 1000);
        } catch (TaskException e) {
            e.printStackTrace();
        }
    }
    */

    @Override
    public void localCheck() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void localHold() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void localCommit() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void localAbort() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void sendRsvCF() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void sendRsvFL() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void sendRsvCmtCF() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void sendRsvCmtFL() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void sendRsvAbtCF() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void sendRsvTimeout() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

}