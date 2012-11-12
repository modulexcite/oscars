
/**
 * Please modify this class to meet your needs
 * This class is not complete
 */

package net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.provider;

import java.util.logging.Logger;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

/**
 * This class was generated by Apache CXF 2.2.7
 * Wed Nov 07 14:42:36 PST 2012
 * Generated source version: 2.2.7
 * 
 */

@javax.jws.WebService(
                      serviceName = "ConnectionServiceProvider",
                      portName = "ConnectionServiceProviderPort",
                      targetNamespace = "http://schemas.ogf.org/nsi/2012/03/connection/provider",
                      wsdlLocation = "file:/Users/haniotak/ij/0_6_trunk/nsibridge/schema/nsi-2_0/ogf_nsi_connection_provider_v2_0.wsdl",
                      endpointInterface = "net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.provider.ConnectionProviderPort")
                      
public class ConnectionProviderPortImpl implements ConnectionProviderPort {

    private static final Logger LOG = Logger.getLogger(ConnectionProviderPortImpl.class.getName());

    /* (non-Javadoc)
     * @see net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.provider.ConnectionProviderPort#terminate(java.lang.String  connectionId ,)net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers.CommonHeaderType  header ,)net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers.CommonHeaderType  header1 )*
     */
    public void terminate(java.lang.String connectionId,net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers.CommonHeaderType header,javax.xml.ws.Holder<net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers.CommonHeaderType> header1) throws net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.ifce.ServiceException    { 
        LOG.info("Executing operation terminate");
        System.out.println(connectionId);
        System.out.println(header);
        try {
            net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers.CommonHeaderType header1Value = null;
            header1.value = header1Value;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        //throw new net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.ifce.ServiceException("serviceException...");
    }

    /* (non-Javadoc)
     * @see net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.provider.ConnectionProviderPort#release(java.lang.String  connectionId ,)net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers.CommonHeaderType  header ,)net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers.CommonHeaderType  header1 )*
     */
    public void release(java.lang.String connectionId,net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers.CommonHeaderType header,javax.xml.ws.Holder<net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers.CommonHeaderType> header1) throws net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.ifce.ServiceException    { 
        LOG.info("Executing operation release");
        System.out.println(connectionId);
        System.out.println(header);
        try {
            net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers.CommonHeaderType header1Value = null;
            header1.value = header1Value;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        //throw new net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.ifce.ServiceException("serviceException...");
    }

    /* (non-Javadoc)
     * @see net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.provider.ConnectionProviderPort#provision(java.lang.String  connectionId ,)net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers.CommonHeaderType  header ,)net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers.CommonHeaderType  header1 )*
     */
    public void provision(java.lang.String connectionId,net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers.CommonHeaderType header,javax.xml.ws.Holder<net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers.CommonHeaderType> header1) throws net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.ifce.ServiceException    { 
        LOG.info("Executing operation provision");
        System.out.println(connectionId);
        System.out.println(header);
        try {
            net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers.CommonHeaderType header1Value = null;
            header1.value = header1Value;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        //throw new net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.ifce.ServiceException("serviceException...");
    }

    /* (non-Javadoc)
     * @see net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.provider.ConnectionProviderPort#reserve(java.lang.String  globalReservationId ,)java.lang.String  description ,)java.lang.String  connectionId ,)net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types.ReservationRequestCriteriaType  criteria ,)net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers.CommonHeaderType  header ,)net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers.CommonHeaderType  header1 )*
     */
    public void reserve(java.lang.String globalReservationId,java.lang.String description,java.lang.String connectionId,net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types.ReservationRequestCriteriaType criteria,net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers.CommonHeaderType header,javax.xml.ws.Holder<net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers.CommonHeaderType> header1) throws net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.ifce.ServiceException    { 
        LOG.info("Executing operation reserve");
        System.out.println(globalReservationId);
        System.out.println(description);
        System.out.println(connectionId);
        System.out.println(criteria);
        System.out.println(header);
        try {
            net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers.CommonHeaderType header1Value = null;
            header1.value = header1Value;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        //throw new net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.ifce.ServiceException("serviceException...");
    }

    /* (non-Javadoc)
     * @see net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.provider.ConnectionProviderPort#modifyCancel(java.lang.String  connectionId ,)net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers.CommonHeaderType  header ,)net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers.CommonHeaderType  header1 )*
     */
    public void modifyCancel(java.lang.String connectionId,net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers.CommonHeaderType header,javax.xml.ws.Holder<net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers.CommonHeaderType> header1) throws net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.ifce.ServiceException    { 
        LOG.info("Executing operation modifyCancel");
        System.out.println(connectionId);
        System.out.println(header);
        try {
            net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers.CommonHeaderType header1Value = null;
            header1.value = header1Value;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        //throw new net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.ifce.ServiceException("serviceException...");
    }

    /* (non-Javadoc)
     * @see net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.provider.ConnectionProviderPort#queryConfirmed(net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types.QueryConfirmedType  queryConfirmed ,)net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers.CommonHeaderType  header )*
     */
    public net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.ifce.GenericAcknowledgmentType queryConfirmed(net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types.QueryConfirmedType queryConfirmed,javax.xml.ws.Holder<net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers.CommonHeaderType> header) throws net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.ifce.ServiceException    { 
        LOG.info("Executing operation queryConfirmed");
        System.out.println(queryConfirmed);
        System.out.println(header.value);
        try {
            net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.ifce.GenericAcknowledgmentType _return = null;
            return _return;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        //throw new net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.ifce.ServiceException("serviceException...");
    }

    /* (non-Javadoc)
     * @see net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.provider.ConnectionProviderPort#modify(java.lang.String  connectionId ,)net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers.CommonHeaderType  header ,)net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers.CommonHeaderType  header1 )*
     */
    public void modify(java.lang.String connectionId,net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers.CommonHeaderType header,javax.xml.ws.Holder<net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers.CommonHeaderType> header1) throws net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.ifce.ServiceException    { 
        LOG.info("Executing operation modify");
        System.out.println(connectionId);
        System.out.println(header);
        try {
            net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers.CommonHeaderType header1Value = null;
            header1.value = header1Value;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        //throw new net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.ifce.ServiceException("serviceException...");
    }

    /* (non-Javadoc)
     * @see net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.provider.ConnectionProviderPort#queryFailed(net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.types.ServiceExceptionType  serviceException ,)net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers.CommonHeaderType  header ,)net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers.CommonHeaderType  header1 )*
     */
    public void queryFailed(net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.types.ServiceExceptionType serviceException,net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers.CommonHeaderType header,javax.xml.ws.Holder<net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers.CommonHeaderType> header1) throws net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.ifce.ServiceException    { 
        LOG.info("Executing operation queryFailed");
        System.out.println(serviceException);
        System.out.println(header);
        try {
            net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers.CommonHeaderType header1Value = null;
            header1.value = header1Value;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        //throw new net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.ifce.ServiceException("serviceException...");
    }

    /* (non-Javadoc)
     * @see net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.provider.ConnectionProviderPort#query(net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types.QueryOperationType  operation ,)net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types.QueryFilterType  queryFilter ,)net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers.CommonHeaderType  header ,)net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers.CommonHeaderType  header1 )*
     */
    public void query(net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types.QueryOperationType operation,net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types.QueryFilterType queryFilter,net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers.CommonHeaderType header,javax.xml.ws.Holder<net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers.CommonHeaderType> header1) throws net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.ifce.ServiceException    { 
        LOG.info("Executing operation query");
        System.out.println(operation);
        System.out.println(queryFilter);
        System.out.println(header);
        try {
            net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers.CommonHeaderType header1Value = null;
            header1.value = header1Value;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        //throw new net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.ifce.ServiceException("serviceException...");
    }

    /* (non-Javadoc)
     * @see net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.provider.ConnectionProviderPort#modifyCheck(java.lang.String  connectionId ,)net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types.ModifyRequestCriteriaType  criteria ,)net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers.CommonHeaderType  header ,)net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers.CommonHeaderType  header1 )*
     */
    public void modifyCheck(java.lang.String connectionId,net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types.ModifyRequestCriteriaType criteria,net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers.CommonHeaderType header,javax.xml.ws.Holder<net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers.CommonHeaderType> header1) throws net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.ifce.ServiceException    { 
        LOG.info("Executing operation modifyCheck");
        System.out.println(connectionId);
        System.out.println(criteria);
        System.out.println(header);
        try {
            net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers.CommonHeaderType header1Value = null;
            header1.value = header1Value;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        //throw new net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.ifce.ServiceException("serviceException...");
    }

}