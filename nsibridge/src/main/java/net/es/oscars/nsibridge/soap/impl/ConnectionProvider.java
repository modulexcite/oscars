package net.es.oscars.nsibridge.soap.impl;

import net.es.oscars.nsibridge.beans.*;
import net.es.oscars.nsibridge.prov.RequestProcessor;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.provider.ConnectionProviderPort;

import javax.jws.WebParam;
import javax.jws.WebService;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.*;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.headers.*;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.ifce.*;
import org.apache.log4j.Logger;

import javax.xml.ws.Holder;
import java.util.UUID;

@WebService(
                      serviceName = "ConnectionServiceProvider",
                      portName = "ConnectionServiceProviderPort",
                      targetNamespace = "http://schemas.ogf.org/nsi/2013/07/connection/provider",
                      wsdlLocation = "schema/2013_07/ConnectionService/ogf_nsi_connection_provider_v2_0.wsdl",
                      endpointInterface = "net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.provider.ConnectionProviderPort")

public class ConnectionProvider implements ConnectionProviderPort {
    private static final Logger log = Logger.getLogger(ConnectionProvider.class.getName());


    @Override
    public void reserve(
            @WebParam(mode = WebParam.Mode.INOUT, name = "connectionId", targetNamespace = "") Holder<String> connectionId,
            @WebParam(name = "globalReservationId", targetNamespace = "") String globalReservationId,
            @WebParam(name = "description", targetNamespace = "") String description,
            @WebParam(name = "criteria", targetNamespace = "") ReservationRequestCriteriaType criteria,
            @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) CommonHeaderType inHeader,
            @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) Holder<CommonHeaderType> outHeader)
                throws ServiceException {
        log.info("Executing operation reserve");
        String connId;
        if (connectionId == null || connectionId.value == null || connectionId.value.equals("")) {
            log.debug("generating new connection ID (none provided)");
            connId = "urn:uuid:"+UUID.randomUUID().toString();

        } else {
            connId = connectionId.value;
        }
        log.debug("ConnectionProvider.reserve: connId: "+connId);

        ResvRequest req = new ResvRequest();
        ReserveType reserveType = new ReserveType();
        req.setReserveType(reserveType);
        req.getReserveType().setConnectionId(connId);
        req.getReserveType().setCriteria(criteria);
        req.getReserveType().setDescription(description);
        req.getReserveType().setGlobalReservationId(globalReservationId);
        req.setInHeader(inHeader);


        try {
            connectionId.value = connId;
            RequestProcessor.getInstance().startReserve(req);
            CommonHeaderType outHeaderValue = req.getOutHeader();
            outHeader.value = outHeaderValue;
        } catch (ServiceException ex) {
            log.error(ex);
            throw ex;
        } catch (Exception ex) {
            log.error(ex);
        }
    }

    @Override
    public void reserveAbort(
            @WebParam(name = "connectionId", targetNamespace = "") String connectionId,
            @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) CommonHeaderType header,
            @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) Holder<CommonHeaderType> outHeader)
                throws ServiceException {

        SimpleRequest req = new SimpleRequest();
        req.setConnectionId(connectionId);
        req.setRequestType(SimpleRequestType.RESERVE_ABORT);
        req.setInHeader(header);

        try {
            RequestProcessor.getInstance().processSimple(req);
            CommonHeaderType outHeaderValue = req.getOutHeader();
            outHeader.value = outHeaderValue;
        } catch (ServiceException ex) {
            log.error(ex);
            throw ex;
        } catch (Exception ex) {
            log.error(ex);
        }
    }


    @Override
    public void reserveCommit(
            @WebParam(name = "connectionId", targetNamespace = "") String connectionId,
            @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) CommonHeaderType header,
            @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) Holder<CommonHeaderType> outHeader)
                throws ServiceException {
        SimpleRequest req = new SimpleRequest();
        req.setConnectionId(connectionId);
        req.setRequestType(SimpleRequestType.RESERVE_COMMIT);
        req.setInHeader(header);

        try {
            RequestProcessor.getInstance().processSimple(req);
            CommonHeaderType outHeaderValue = req.getOutHeader();
            outHeader.value = outHeaderValue;
        } catch (ServiceException ex) {
            log.error(ex);
            throw ex;
        } catch (Exception ex) {
            log.error(ex);
        }
    }

    @Override
    public void provision(
            @WebParam(name = "connectionId", targetNamespace = "") String connectionId,
            @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) CommonHeaderType header,
            @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) Holder<CommonHeaderType> outHeader)
                throws ServiceException {
        SimpleRequest req = new SimpleRequest();
        req.setConnectionId(connectionId);
        req.setRequestType(SimpleRequestType.PROVISION);
        req.setInHeader(header);
        try {
            RequestProcessor.getInstance().processSimple(req);
            CommonHeaderType outHeaderValue = req.getOutHeader();
            outHeader.value = outHeaderValue;
        } catch (ServiceException ex) {
            log.error(ex);
            throw ex;
        } catch (Exception ex) {
            log.error(ex);
        }
    }

    @Override
    public void release(
            @WebParam(name = "connectionId", targetNamespace = "") String connectionId,
            @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) CommonHeaderType header,
            @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) Holder<CommonHeaderType> outHeader)
                throws ServiceException {
        SimpleRequest req = new SimpleRequest();
        req.setConnectionId(connectionId);
        req.setRequestType(SimpleRequestType.RELEASE);
        req.setInHeader(header);

        try {
            RequestProcessor.getInstance().processSimple(req);
            CommonHeaderType outHeaderValue = req.getOutHeader();
            outHeader.value = outHeaderValue;
        } catch (ServiceException ex) {
            log.error(ex);
            throw ex;
        } catch (Exception ex) {
            log.error(ex);
        }
    }

    @Override
    public void terminate(
            @WebParam(name = "connectionId", targetNamespace = "") String connectionId,
            @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) CommonHeaderType header,
            @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) Holder<CommonHeaderType> outHeader)
                throws ServiceException {

        SimpleRequest req = new SimpleRequest();
        req.setConnectionId(connectionId);
        req.setRequestType(SimpleRequestType.TERMINATE);
        req.setInHeader(header);

        try {
            RequestProcessor.getInstance().processSimple(req);
            CommonHeaderType outHeaderValue = req.getOutHeader();
            outHeader.value = outHeaderValue;
        } catch (ServiceException ex) {
            log.error(ex);
            throw ex;
        } catch (Exception ex) {
            log.error(ex);
        }
    }

    // query

    @Override
    public void queryNotification(
            @WebParam(name = "connectionId", targetNamespace = "") String connectionId,
            @WebParam(name = "startNotificationId", targetNamespace = "") Integer startNotificationId,
            @WebParam(name = "endNotificationId", targetNamespace = "") Integer endNotificationId,
            @WebParam(name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) CommonHeaderType header,
            @WebParam(mode = WebParam.Mode.OUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) Holder<CommonHeaderType> header1)
                throws ServiceException {
        //To change body of implemented methods use File | Settings | File Templates.
        // TODO
    }

    @Override
    public GenericAcknowledgmentType queryRecursive(
            @WebParam(partName = "queryRecursive", name = "queryRecursive", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/connection/types") QueryType queryRecursive,
            @WebParam(partName = "header", mode = WebParam.Mode.INOUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) Holder<CommonHeaderType> header)
                throws ServiceException {
        QueryRequest req = new QueryRequest();
        req.setQuery(queryRecursive);
        req.setInHeader(header.value);
        try {
            RequestProcessor.getInstance().recursiveQuery(req);
        } catch (Exception e) {
            throw new ServiceException(e.getMessage());
        }
        
        return new GenericAcknowledgmentType();
    }

    @Override
    public GenericAcknowledgmentType querySummary(
            @WebParam(partName = "querySummary", name = "querySummary", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/connection/types") QueryType querySummary,
            @WebParam(partName = "header", mode = WebParam.Mode.INOUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) Holder<CommonHeaderType> header)
                throws ServiceException {
        QueryRequest req = new QueryRequest();
        req.setQuery(querySummary);
        req.setInHeader(header.value);
        try {
            RequestProcessor.getInstance().asyncQuery(req);
        } catch (Exception e) {
            throw new ServiceException(e.getMessage());
        }

        return new GenericAcknowledgmentType();
    }

    // sync methods

    @Override
    public QuerySummaryConfirmedType querySummarySync(
            @WebParam(partName = "querySummarySync", name = "querySummarySync", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/connection/types") QueryType querySummarySync,
            @WebParam(partName = "header", mode = WebParam.Mode.INOUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) Holder<CommonHeaderType> header)
            throws QuerySummarySyncFailed {

        QueryRequest req = new QueryRequest();
        req.setQuery(querySummarySync);
        req.setInHeader(header.value);
        QuerySummaryConfirmedType res;
        try {
            res = RequestProcessor.getInstance().syncQuerySum(req);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        return res;

    }

    @Override
    public QueryNotificationConfirmedType queryNotificationSync(
            @WebParam(partName = "queryNotificationSync", name = "queryNotificationSync", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/connection/types") QueryNotificationType queryNotificationSync,
            @WebParam(partName = "header", mode = WebParam.Mode.INOUT, name = "nsiHeader", targetNamespace = "http://schemas.ogf.org/nsi/2013/07/framework/headers", header = true) Holder<CommonHeaderType> header)
            throws QueryNotificationSyncFailed {
        // TODO
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ConnectionProvider() {

    }

}
