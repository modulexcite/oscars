
/**
 * Please modify this class to meet your needs
 * This class is not complete
 */

package net.es.oscars.coord.trivial;
import net.es.oscars.api.soap.gen.v06.EventContent;
import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.api.soap.gen.v06.GlobalReservationId;
import net.es.oscars.api.soap.gen.v06.ListReply;
import net.es.oscars.api.soap.gen.v06.ListRequest;
import net.es.oscars.api.soap.gen.v06.CreatePathResponseContent;
import net.es.oscars.api.soap.gen.v06.CreatePathContent;
import net.es.oscars.api.soap.gen.v06.ResCreateContent;
import net.es.oscars.api.soap.gen.v06.ModifyResReply;
import net.es.oscars.api.soap.gen.v06.ModifyResContent;
import net.es.oscars.api.soap.gen.v06.TeardownPathResponseContent;
import net.es.oscars.api.soap.gen.v06.TeardownPathContent;
import net.es.oscars.api.soap.gen.v06.CreateReply;
import net.es.oscars.coord.soap.gen.UpdateStatusRespContent;
import net.es.oscars.coord.soap.gen.UpdateStatusReqContent;
import net.es.oscars.common.soap.gen.OSCARSFaultMessage;
import net.es.oscars.common.soap.gen.SubjectAttributes;
//import net.es.oscars.coord.workers.ModuleWorker;

import java.util.logging.Logger;

//import net.es.oscars.coord.req.CoordRequest;

/**
 * This class was generated by Apache CXF 2.2.1
 * Tue May 19 13:27:19 PDT 2009
 * Generated source version: 2.2.1
 *
 */

@javax.jws.WebService(
                      serviceName = "CoordService",
                      portName = "CoordPort",
                      targetNamespace = "http://oscars.es.net/OSCARS/coord",
                      endpointInterface = "net.es.oscars.coord.soap.gen.CoordPortType")
@javax.xml.ws.BindingType(value = "http://www.w3.org/2003/05/soap/bindings/HTTP/")
public class CoordImpl implements net.es.oscars.coord.soap.gen.CoordPortType {

    private static final Logger LOG = Logger.getLogger(CoordImpl.class.getName());

    /* (non-Javadoc)
     * @see net.es.oscars.coord.soap.gen.CoordPortType#queryReservation(net.es.oscars.coord.soap.gen.QueryResvReqContent  queryResvReq )*
     */
    public ResDetails queryReservation(SubjectAttributes subjectAttributes,GlobalReservationId queryResvReq) 
        throws OSCARSFaultMessage    { 
        LOG.info("Executing operation queryReservation");
        System.out.println(subjectAttributes);
        System.out.println(queryResvReq);
        try {
            net.es.oscars.api.soap.gen.v06.ResDetails _return = null;
            return _return;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        //throw new CoordFaultMessage("CoordFaultMessage...");
    }

    /* (non-Javadoc)
     * @see net.es.oscars.coord.soap.gen.CoordPortType#listReservation(net.es.oscars.coord.soap.gen.ListResvReqContent  listResvReq )*
     */
    public ListReply listReservations(SubjectAttributes subjectAttributes,ListRequest listResvReq) throws OSCARSFaultMessage    { 
        LOG.info("Executing operation listReservations");
        System.out.println(subjectAttributes);
        System.out.println(listResvReq);
        try {
            net.es.oscars.api.soap.gen.v06.ListReply _return = null;
            return _return;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        //throw new net.es.oscars.common.soap.gen.OSCARSFaultMessage("OSCARSFaultMessage...");
    }
    /* (non-Javadoc)
     * @see net.es.oscars.coord.soap.gen.CoordPortType#notify(org.oasis_open.docs.wsn.b_2.Notify  notify )*
     */
    public void notify(SubjectAttributes subjectAttributes,EventContent eventContent) { 
        LOG.info("Executing operation notify");
        System.out.println(eventContent);
        try {
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    /* (non-Javadoc)
     * @see net.es.oscars.coord.soap.gen.CoordPortType#updateStatus(net.es.oscars.coord.soap.gen.UpdateStatusReqContent  updateStatusReq )*
     */
    public UpdateStatusRespContent updateStatus(UpdateStatusReqContent updateStatusReq) throws OSCARSFaultMessage    {
        LOG.info("Executing operation updateStatus");
        System.out.println(updateStatusReq);
        try {
            UpdateStatusRespContent _return = null;
            return _return;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        //throw new CoordFaultMessage("CoordFaultMessage...");
    }

    /* (non-Javadoc)
     * @see net.es.oscars.coord.soap.gen.CoordPortType#createPath(net.es.oscars.coord.soap.gen.CreatePathReqContent  createPathReq )*
     */
    public CreatePathResponseContent createPath(SubjectAttributes subjectAttributes,
                CreatePathContent createPathReq, ResDetails resDetails) throws OSCARSFaultMessage    { 
        LOG.info("Executing operation createPath GRI is " + createPathReq.getGlobalReservationId());
        try {
        	// Fake response. 
            CreatePathResponseContent _return = new CreatePathResponseContent();
            _return.setGlobalReservationId(createPathReq.getGlobalReservationId());
            _return.setStatus("TESTSTATUS");
            return _return;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new OSCARSFaultMessage(ex.getMessage(), ex);
        }

    }

    /* (non-Javadoc)
     * @see net.es.oscars.coord.soap.gen.CoordPortType#createReservation(net.es.oscars.coord.soap.gen.CreateResvReqContent  createResvReq )*
     */
    public CreateReply createReservation(SubjectAttributes subjectAttributes,ResCreateContent createResvReq) 
       throws OSCARSFaultMessage    { 
        LOG.info("Executing operation createReservation");
        System.out.println(subjectAttributes);
        System.out.println(createResvReq);
        try {
            CreateReply _return = null;
            return _return;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        //throw new CoordFaultMessage("CoordFaultMessage...");
    }

    /* (non-Javadoc)
     * @see net.es.oscars.coord.soap.gen.CoordPortType#cancelReservation(net.es.oscars.coord.soap.gen.CancelResvReqContent  cancelResvReq )*
     */
    public String cancelReservation(SubjectAttributes subjectAttributes,GlobalReservationId cancelResvReq) 
        throws OSCARSFaultMessage    { 
        LOG.info("Executing operation cancelReservation");
        System.out.println(subjectAttributes);
        System.out.println(cancelResvReq);
        try {
            String _return = "";
            return _return;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        //throw new CoordFaultMessage("CoordFaultMessage...");
    }

    /* (non-Javadoc)
     * @see net.es.oscars.coord.soap.gen.CoordPortType#modifyReservation(net.es.oscars.coord.soap.gen.ModifyResvReqContent  modifyResvReq )*
     */
    public ModifyResReply modifyReservation(SubjectAttributes subjectAttributes,ModifyResContent modifyResvReq) 
        throws OSCARSFaultMessage    { 
        LOG.info("Executing operation modifyReservation");
        System.out.println(subjectAttributes);
        System.out.println(modifyResvReq);
        try {
            ModifyResReply _return = null;
            return _return;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        //throw new CoordFaultMessage("CoordFaultMessage...");
    }

    /* (non-Javadoc)
     * @see net.es.oscars.coord.soap.gen.CoordPortType#teardownPath(net.es.oscars.coord.soap.gen.TeardownPathReqContent  teardownPathReq )*
     */
    public TeardownPathResponseContent teardownPath(SubjectAttributes subjectAttributes,
            TeardownPathContent teardownPathReq, ResDetails resDetails) 
        throws OSCARSFaultMessage    { 
        LOG.info("Executing operation teardownPath");
        System.out.println(subjectAttributes);
        System.out.println(teardownPathReq);
        try {
            TeardownPathResponseContent _return = null;
            return _return;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        //throw new CoordFaultMessage("CoordFaultMessage...");
    }
}