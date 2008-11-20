package net.es.oscars.interdomain;

import java.rmi.RemoteException;
import java.util.*;

import org.apache.log4j.*;
import org.apache.axis2.AxisFault;

import net.es.oscars.oscars.OSCARSCore;
import net.es.oscars.oscars.OSCARSStub;
import net.es.oscars.oscars.TypeConverter;
import net.es.oscars.oscars.AAAFaultMessage;
import net.es.oscars.oscars.BSSFaultMessage;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.topology.*;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.client.*;
import net.es.oscars.notify.EventProducer;
import net.es.oscars.notify.OSCARSEvent;

/**
 * Forwarding client.
 */
public class Forwarder extends Client {
    private Logger log;
    private TypeConverter tc;
    private OSCARSCore core;

    public Forwarder() {
        this.log = Logger.getLogger(this.getClass());
        this.core = OSCARSCore.getInstance();
        this.tc = this.core.getTypeConverter();
    }

    private void setup(Reservation resv, String url)
            throws InterdomainException {

        this.log.debug("setup.start: " + url);
        String catalinaHome = System.getProperty("catalina.home");
        // check for trailing slash
        if (!catalinaHome.endsWith("/")) {
            catalinaHome += "/";
        }
        String repo = catalinaHome + "shared/classes/repo/";
        System.setProperty("axis2.xml", repo + "axis2.xml");
        try {
            super.setUp(true, url, repo, repo + "axis2.xml");
        } catch (AxisFault af) {
            this.log.error("setup.axisFault: " + af.getMessage());
            throw new InterdomainException("failed to reach remote domain:" + url +  af.getMessage());
        }
        this.log.debug("setup.finish: " + url);
    }

    public CreateReply create(Reservation resv, PathInfo pathInfo)
            throws InterdomainException {

        CreateReply createReply = null;
        String login = resv.getLogin();
        Path path = resv.getPath("intra");
        if (path == null) {
           throw new InterdomainException(
                  "no path provided to forwarder create");
        }
        Domain nextDomain = path.getNextDomain();
        if (nextDomain == null) { return null; }
        String url = nextDomain.getUrl();
        this.log.info("create.start forward to  " + url);
        EventProducer eventProducer = new EventProducer();
        eventProducer.addEvent(OSCARSEvent.RESV_CREATE_FWD_STARTED, login, "JOB", resv, pathInfo);
        ForwardReply reply = this.forward("createReservation", resv, pathInfo, url);
        eventProducer.addEvent(OSCARSEvent.RESV_CREATE_FWD_ACCEPTED, login, "JOB", resv, pathInfo);
        createReply = reply.getCreateReservation();
        this.log.info("create.finish GRI is: " +
                      createReply.getGlobalReservationId());
        return createReply;
    }

    public ModifyResReply modify(Reservation resv, Reservation persistentResv, PathInfo pathInfo) throws InterdomainException {

        String url = null;
        String login = resv.getLogin();
         
        // currently get the next domain from the stored path
        if (persistentResv.getPath("intra") != null && persistentResv.getPath("intra").getNextDomain() != null) {
            url = persistentResv.getPath("intra").getNextDomain().getUrl();
        }

        if (url != null) {
            this.log.info("modify.start forward to  " + url);
            EventProducer eventProducer = new EventProducer();
            eventProducer.addEvent(OSCARSEvent.RESV_MODIFY_FWD_STARTED, login, "JOB", persistentResv, pathInfo);
            ForwardReply reply = this.forward("modifyReservation", resv, pathInfo, url);
            eventProducer.addEvent(OSCARSEvent.RESV_MODIFY_FWD_ACCEPTED, login, "JOB", persistentResv, pathInfo);
            ModifyResReply modifyReply = reply.getModifyReservation();
            this.log.info("modify.finish GRI is: " + modifyReply.getReservation().getGlobalReservationId());
            return modifyReply;
        } else {
            return null;
        }
    }


    public ResDetails query(Reservation resv) throws InterdomainException {

        String url = null;

        if (resv.getPath("intra") != null && resv.getPath("intra").getNextDomain() != null) {
            url = resv.getPath("intra").getNextDomain().getUrl();
        }
        if (url == null) { return null; }
        this.log.info("query forward to " + url);
        ForwardReply reply = this.forward("queryReservation", resv, null, url);
        return reply.getQueryReservation();
    }

    public String cancel(Reservation resv) throws InterdomainException {

        String url = null;
        String login = resv.getLogin();
        if (resv.getPath("intra") != null && resv.getPath("intra").getNextDomain() != null) {
            url = resv.getPath("intra").getNextDomain().getUrl();
        }
        if (url == null) { return null; }
        this.log.info("cancel start forward to: " + url);
        EventProducer eventProducer = new EventProducer();
        eventProducer.addEvent(OSCARSEvent.RESV_CANCEL_FWD_STARTED, login, "JOB", resv);
        ForwardReply reply = this.forward("cancelReservation", resv, null, url);
        eventProducer.addEvent(OSCARSEvent.RESV_CANCEL_FWD_ACCEPTED, login, "JOB", resv);
        return reply.getCancelReservation();
    }

    public CreatePathResponseContent createPath(Reservation resv)
            throws InterdomainException {

        String url = null;
        if (resv.getPath("intra") != null && resv.getPath("intra").getNextDomain() != null) {
            url = resv.getPath("intra").getNextDomain().getUrl();
        }
        if (url == null) { return null; }
        this.log.info("createPath forward to: " + url);
        ForwardReply reply = this.forward("createPath", resv, null, url);
        return reply.getCreatePath();
    }

    public RefreshPathResponseContent refreshPath(Reservation resv)
            throws InterdomainException {

        String url = null;
        if (resv.getPath("intra") != null && resv.getPath("intra").getNextDomain() != null) {
            url = resv.getPath("intra").getNextDomain().getUrl();
        }
        if (url == null) { return null; }
        this.log.info("refreshPath forward to: " + url);
        ForwardReply reply = this.forward("refreshPath", resv, null, url);
        return reply.getRefreshPath();
    }

    public TeardownPathResponseContent teardownPath(Reservation resv)
            throws InterdomainException {

        String url = null;
        if (resv.getPath("intra") != null && resv.getPath("intra").getNextDomain() != null) {
            url = resv.getPath("intra").getNextDomain().getUrl();
        }
        if (url == null) { return null; }
        this.log.info("teardownPath forward to: " + url);
        ForwardReply reply = this.forward("teardownPath", resv, null, url);
        return reply.getTeardownPath();
    }

    public ForwardReply forward(String operation, Reservation resv,
                                PathInfo pathInfo, String url)
            throws InterdomainException {

        this.log.debug("forward.start:  to " + url);
        setup(resv, url);
        String login = resv.getLogin();
        this.log.debug("forward.login: " + login);
        ForwardReply reply = null;
        Forward fwd =  new Forward();
        ForwardPayload forPayload = new ForwardPayload();
        fwd.setPayloadSender(login);
        forPayload.setContentType(operation);
        try {
            if (operation.equals("createReservation")) {
                forPayload.setCreateReservation(
                        toCreateRequest(resv, pathInfo));

            } else if (operation.equals("cancelReservation")) {
                GlobalReservationId rt = new GlobalReservationId();
                rt.setGri(resv.getGlobalReservationId());
                forPayload.setCancelReservation(rt);

            } else if (operation.equals("queryReservation")) {
                GlobalReservationId rt = new GlobalReservationId();
                rt.setGri(resv.getGlobalReservationId());
                forPayload.setQueryReservation(rt);
            } else if (operation.equals("modifyReservation")) {
                ModifyResContent modResContent = this.toModifyRequest(resv, pathInfo);
                forPayload.setModifyReservation(modResContent);

            } else if (operation.equals("createPath")) {
                CreatePathContent cp = new CreatePathContent();
                cp.setGlobalReservationId(resv.getGlobalReservationId());
                forPayload.setCreatePath(cp);
            } else if (operation.equals("refreshPath")) {
                RefreshPathContent rp = new RefreshPathContent();
                rp.setGlobalReservationId(resv.getGlobalReservationId());
                forPayload.setRefreshPath(rp);
            } else if (operation.equals("teardownPath")) {
                TeardownPathContent tp = new TeardownPathContent();
                tp.setGlobalReservationId(resv.getGlobalReservationId());
                forPayload.setTeardownPath(tp);
            }
            fwd.setPayload(forPayload);
            this.log.debug("forward.payloadSender: " + fwd.getPayloadSender());
            reply = super.forward(fwd);
            return reply;
        } catch (java.rmi.RemoteException e) {
            this.log.error("forward.failed, " + url + ": " + e.getMessage() );
            e.printStackTrace();
            throw new InterdomainException("failed to reach remote domain:" +
                                            url + e.getMessage());
        } catch (AAAFaultMessage e) {
            this.log.error("forward.AAAFaultMessage: " +
                                             e.getMessage());
            throw new InterdomainException("AAAFaultMessage from :" +
                                            url + e.getMessage());
        } catch (BSSFaultMessage e) {
            this.log.error ("forward.BSSFaultMessage: " +
                                            e.getMessage());
            throw new InterdomainException("BSSFaultMessage from :" +
                                            url +  e.getMessage());
        }
    }
    public ModifyResContent toModifyRequest(Reservation resv, PathInfo pathInfo) {
        ModifyResContent modResContent = new ModifyResContent();

        modResContent.setStartTime(resv.getStartTime());
        modResContent.setEndTime(resv.getEndTime());
        /* output bandwidth is in bytes, input is in Mbytes */
        Long bandwidth = resv.getBandwidth()/1000000;
        modResContent.setBandwidth( bandwidth.intValue());
        modResContent.setDescription(resv.getDescription());
        modResContent.setGlobalReservationId(resv.getGlobalReservationId());
        modResContent.setPathInfo(pathInfo);
        return modResContent;
    }

    public ResCreateContent toCreateRequest(Reservation resv,
                                            PathInfo pathInfo) {

        long millis = -1;
        ResCreateContent resCont = new ResCreateContent();

        /* default pathSetupMode between domains is signal-xml */
        String pathSetupMode = pathInfo.getPathSetupMode();
        if (pathSetupMode == null) {
            pathInfo.setPathSetupMode("timer-automatic");
        }

        resCont.setStartTime(resv.getStartTime());
        resCont.setEndTime(resv.getEndTime());
        /* output bandwidth is in bytes, input is in Mbytes */
        Long bandwidth = resv.getBandwidth()/1000000;
        resCont.setBandwidth( bandwidth.intValue());
        resCont.setDescription(resv.getDescription());
        resCont.setGlobalReservationId(resv.getGlobalReservationId());
        resCont.setPathInfo(pathInfo);
        return resCont;
    }
}
