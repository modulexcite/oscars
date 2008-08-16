package net.es.oscars.rmi;

import java.io.*;
import java.util.*;
import org.apache.log4j.*;
import org.hibernate.*;
import net.es.oscars.aaa.*;
import net.es.oscars.aaa.UserManager.*;
import net.es.oscars.bss.*;
import net.es.oscars.notify.*;
import net.es.oscars.oscars.*;
import net.es.oscars.pss.PSSException;

public class UnsafeTeardownPathRmiHandler {
    private OSCARSCore core;
    private Logger log;


    public UnsafeTeardownPathRmiHandler() {
        this.log = Logger.getLogger(this.getClass());
        this.core = OSCARSCore.getInstance();
    }

    public
        HashMap<String, Object> teardownPath(HashMap<String, String[]> inputMap,
                                             String userName)
            throws IOException {

        this.log.debug("teardownPath.start");
        HashMap<String, Object> result = new HashMap<String, Object>();
        String methodName = "TeardownPath";

        UserManager userMgr =  new UserManager("aaa");
        EventProducer eventProducer = new EventProducer();
        Reservation reservation = null;
        result.put("method", methodName);

        Session aaa = core.getAaaSession();
        aaa.beginTransaction();
        AuthValue authVal = userMgr.checkAccess(userName, "Reservations", "modify");
        if (authVal == AuthValue.DENIED) {
            result.put("error", "no permission to force teardown of path");
            aaa.getTransaction().rollback();
            this.log.debug("teardownPath failed: permission denied");
            return result;
        }
        aaa.getTransaction().commit();

        String[] paramValues = inputMap.get("gri");
        String gri = paramValues[0];

        Session bss = core.getBssSession();
        bss.beginTransaction();
        String errMessage = null;
        /* UNCOMMENT THIS BLOCK FOR TESTING
        try {
            ReservationDAO resvDAO = new ReservationDAO(core.getBssDbName());
            Reservation resv = resvDAO.query(gri);
            this.core.getPathSetupManager().teardown(resv, resv.getStatus(), false);
            eventProducer.addEvent(OSCARSEvent.PATH_TEARDOWN_COMPLETED, userName, "WBUI", reservation);
        } catch (PSSException e) {
            errMessage = e.getMessage();
        } catch (BSSException e) {
            errMessage = e.getMessage();
        } finally {
            if (errMessage != null) {
                result.put("error", errMessage);
                bss.getTransaction().rollback();
                if (reservation != null){
                    eventProducer.addEvent(OSCARSEvent.PATH_TEARDOWN_FAILED, userName, "WBUI", reservation, "", errMessage);
                }
                this.log.debug("teardownPath failed: " + errMessage);
                return result;
            }
        }
        result.put("gri", reservation.getGlobalReservationId());
        result.put("status", "Manually tore down reservation with GRI " + reservation.getGlobalReservationId());
        */
        /* REMOVE THIS LINE FOR TESTING */
        result.put("status", "Not implemented yet");
        result.put("method", methodName);
        result.put("success", Boolean.TRUE);

        bss.getTransaction().commit();
        this.log.debug("teardownPath.end");
        return result;
    }
}