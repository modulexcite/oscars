package net.es.oscars.api.compat;

import java.lang.RuntimeException;
import java.lang.reflect.Member;
import java.net.NetPermission;
import java.util.UUID;

import javax.xml.ws.WebServiceContext;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import net.es.oscars.api.soap.gen.v05.ResDetails;
import net.es.oscars.api.soap.gen.v06.*;
import net.es.oscars.authN.beans.Attribute;
import net.es.oscars.common.soap.gen.MessagePropertiesType;
import net.es.oscars.common.soap.gen.SubjectAttributes;
import net.es.oscars.resourceManager.beans.Reservation;
import net.es.oscars.utils.sharedConstants.AuthZConstants;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.topology.PathTools;

import oasis.names.tc.saml._2_0.assertion.AttributeType;
import org.oasis_open.docs.wsn.b_2.MessageType;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;

public class DataTranslator05 {

    public static net.es.oscars.api.soap.gen.v06.CreateReply translate(net.es.oscars.api.soap.gen.v05.CreateReply createReply05)
            throws OSCARSServiceException {

        net.es.oscars.api.soap.gen.v06.CreateReply createReply06 = new net.es.oscars.api.soap.gen.v06.CreateReply();
        net.es.oscars.api.soap.gen.v06.ReservedConstraintType reservedConstraint06 = new net.es.oscars.api.soap.gen.v06.ReservedConstraintType();
        net.es.oscars.api.soap.gen.v06.UserRequestConstraintType userRequestConstraint06 = new net.es.oscars.api.soap.gen.v06.UserRequestConstraintType();
        MessagePropertiesType msgProps = new MessagePropertiesType();

        try {
            createReply06.setGlobalReservationId(createReply05.getGlobalReservationId());
            createReply06.setStatus(createReply05.getStatus());
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate v05.CreateReply");
        }
        try {
            createReply06.setToken(createReply05.getToken());
        } catch (Exception e) {
            // ignore
        }

        try {
            reservedConstraint06.setStartTime(Long.parseLong(createReply05.getPathInfo().getPath().getLifetime().getStart().toString()));
            reservedConstraint06.setEndTime(Long.parseLong(createReply05.getPathInfo().getPath().getLifetime().getEnd().toString()));
            reservedConstraint06.setPathInfo(translate(createReply05.getPathInfo()));
            createReply06.setReservedConstraint(reservedConstraint06);
        } catch (NullPointerException e) {
            // ignore
        }

        try {
            userRequestConstraint06.setStartTime(Long.parseLong(createReply05.getPathInfo().getPath().getLifetime().getStart().toString()));
            userRequestConstraint06.setEndTime(Long.parseLong(createReply05.getPathInfo().getPath().getLifetime().getEnd().toString()));
            userRequestConstraint06.setPathInfo(translate(createReply05.getPathInfo()));
            createReply06.setUserRequestConstraint(userRequestConstraint06);
        } catch (NullPointerException e) {
            throw new OSCARSServiceException("Unable to translate v05.CreateReply");
        }

        try {
            String transId = PathTools.getLocalDomainId() + "-V05-" + UUID.randomUUID().toString();
            msgProps.setGlobalTransactionId(transId);
            createReply06.setMessageProperties(msgProps);
        } catch (Exception e) {
            // ignore
        }
        return createReply06;
    }

    public static net.es.oscars.api.soap.gen.v05.CreateReply translate(net.es.oscars.api.soap.gen.v06.CreateReply createReply06)
            throws OSCARSServiceException {

        net.es.oscars.api.soap.gen.v05.CreateReply createReply05 = new net.es.oscars.api.soap.gen.v05.CreateReply();

        try {
            createReply05.setToken(createReply06.getToken());
        } catch (Exception e) {
            // ignore
        }

        try {
            createReply05.setGlobalReservationId(createReply06.getGlobalReservationId());
            createReply05.setStatus(createReply06.getStatus());
            createReply05.setPathInfo(translate(createReply06.getReservedConstraint().getPathInfo()));
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate 0.6 CreateReply");
        }
        return createReply05;
    }

    public static net.es.oscars.api.soap.gen.v06.ResCreateContent translate(net.es.oscars.api.soap.gen.v05.ResCreateContent createReservation05,
             String src) throws OSCARSServiceException {

        net.es.oscars.api.soap.gen.v06.ResCreateContent createReservation06 = new net.es.oscars.api.soap.gen.v06.ResCreateContent();
        MessagePropertiesType msgProps = new MessagePropertiesType();
        net.es.oscars.api.soap.gen.v06.UserRequestConstraintType userConstraints06 = new net.es.oscars.api.soap.gen.v06.UserRequestConstraintType();
        net.es.oscars.api.soap.gen.v06.PathInfo pathInfo06 = new net.es.oscars.api.soap.gen.v06.PathInfo();

        try {
            createReservation06.setDescription(createReservation05.getDescription());
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate 0.5 ResCreateContent");
        }

        try {
            userConstraints06.setBandwidth(createReservation05.getBandwidth());
            userConstraints06.setStartTime(createReservation05.getStartTime());
            userConstraints06.setEndTime(createReservation05.getEndTime());
        } catch (NumberFormatException e) {
            throw new OSCARSServiceException("Unable to translate 0.5 ResCreateContent");
        }

        try {
            createReservation06.setGlobalReservationId(createReservation05.getGlobalReservationId());
        } catch (Exception e) {
            // ignore
        }

        try {
            pathInfo06 = translate(createReservation05.getPathInfo());
            userConstraints06.setPathInfo(pathInfo06);
            createReservation06.setUserRequestConstraint(userConstraints06);
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate 0.5 ResCreateContent");
        }

        if (src != null) {
            // This is a resCreateContent created by a 0.5 IDC (Forward message). Needs to fill up reservedConstraints
            net.es.oscars.api.soap.gen.v06.ReservedConstraintType reservedConstraints06 =
                    new net.es.oscars.api.soap.gen.v06.ReservedConstraintType();

            reservedConstraints06.setBandwidth(createReservation05.getBandwidth());
            reservedConstraints06.setStartTime(createReservation05.getStartTime());
            reservedConstraints06.setEndTime(createReservation05.getEndTime());
            reservedConstraints06.setPathInfo(pathInfo06);
            createReservation06.setReservedConstraint(reservedConstraints06);
        }

        String transId = PathTools.getLocalDomainId() + "-V05-" + UUID.randomUUID().toString();
        msgProps.setGlobalTransactionId(transId);
        createReservation06.setMessageProperties(msgProps);
        return createReservation06;
    }

    public static net.es.oscars.api.soap.gen.v05.ResCreateContent translate(net.es.oscars.api.soap.gen.v06.ResCreateContent resCreateContent06,
            String src) throws OSCARSServiceException {

        net.es.oscars.api.soap.gen.v05.ResCreateContent resCreateContent05 = new net.es.oscars.api.soap.gen.v05.ResCreateContent();

        try {
            resCreateContent05.setGlobalReservationId(resCreateContent06.getGlobalReservationId());
        } catch (Exception e) {
            // ignore
        }

        try {
            resCreateContent05.setDescription(resCreateContent06.getDescription());
            resCreateContent05.setBandwidth(resCreateContent06.getReservedConstraint().getBandwidth());
            resCreateContent05.setStartTime(resCreateContent06.getReservedConstraint().getStartTime());
            resCreateContent05.setEndTime(resCreateContent06.getReservedConstraint().getEndTime());
            resCreateContent05.setPathInfo(translate(resCreateContent06.getReservedConstraint().getPathInfo()));
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate 0.6 ResCreateContent");
        }
        return resCreateContent05;
    }

    public static net.es.oscars.api.soap.gen.v06.CreatePathContent translate(net.es.oscars.api.soap.gen.v05.CreatePathContent createPath05)
            throws OSCARSServiceException {
        net.es.oscars.api.soap.gen.v06.CreatePathContent createPathContent06 = new net.es.oscars.api.soap.gen.v06.CreatePathContent();
        MessagePropertiesType msgProps = new MessagePropertiesType();

        try {
            createPathContent06.setToken(createPath05.getToken());
        } catch (Exception e) {
            // ignore
        }

        try {
            createPathContent06.setGlobalReservationId(createPath05.getGlobalReservationId());
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate v05 CreatePathContent");
        }

        String transId = PathTools.getLocalDomainId() + "-V05-" + UUID.randomUUID().toString();
        msgProps.setGlobalTransactionId(transId);
        createPathContent06.setMessageProperties(msgProps);
        return createPathContent06;
    }

    public static net.es.oscars.api.soap.gen.v05.CreatePathContent translate(net.es.oscars.api.soap.gen.v06.CreatePathContent createPath06)
            throws OSCARSServiceException {
        net.es.oscars.api.soap.gen.v05.CreatePathContent createPathContent05 = new net.es.oscars.api.soap.gen.v05.CreatePathContent();
        try {
            createPathContent05.setGlobalReservationId(createPath06.getGlobalReservationId());
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate v06 CreatePathContent");
        }
        try {
            createPathContent05.setToken(createPath06.getToken());
        } catch (Exception e) {
            // ignore
        }
        return createPathContent05;
    }

    public static net.es.oscars.api.soap.gen.v06.CreatePathResponseContent translate(net.es.oscars.api.soap.gen.v05.CreatePathResponseContent createPathReply05)
            throws OSCARSServiceException {
        net.es.oscars.api.soap.gen.v06.CreatePathResponseContent createPathResponseClient06 = new net.es.oscars.api.soap.gen.v06.CreatePathResponseContent();
        MessagePropertiesType msgProps = new MessagePropertiesType();

        try {
            createPathResponseClient06.setGlobalReservationId(createPathReply05.getGlobalReservationId());
            createPathResponseClient06.setStatus(createPathReply05.getStatus());
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate v05 CreatePathReply");
        }

        String transId = PathTools.getLocalDomainId() + "-V05-" + UUID.randomUUID().toString();
        msgProps.setGlobalTransactionId(transId);
        createPathResponseClient06.setMessageProperties(msgProps);
        return createPathResponseClient06;
    }

    public static net.es.oscars.api.soap.gen.v05.CreatePathResponseContent translate(net.es.oscars.api.soap.gen.v06.CreatePathResponseContent createPathReply06)
            throws OSCARSServiceException {
        net.es.oscars.api.soap.gen.v05.CreatePathResponseContent createPathResponseContent05 = new net.es.oscars.api.soap.gen.v05.CreatePathResponseContent();

        try {
            createPathResponseContent05.setGlobalReservationId(createPathReply06.getGlobalReservationId());
            createPathResponseContent05.setStatus(createPathReply06.getStatus());
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate v06 CreatePathResponseContent");
        }

        return createPathResponseContent05;
    }

    public static net.es.oscars.api.soap.gen.v06.TeardownPathContent translate(net.es.oscars.api.soap.gen.v05.TeardownPathContent teardownPath05)
            throws OSCARSServiceException {
        net.es.oscars.api.soap.gen.v06.TeardownPathContent teardownPathContent06 = new net.es.oscars.api.soap.gen.v06.TeardownPathContent();
        MessagePropertiesType msgProps = new MessagePropertiesType();

        try {
            teardownPathContent06.setGlobalReservationId(teardownPath05.getGlobalReservationId());
            teardownPathContent06.setToken(teardownPath05.getToken());
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate v05 TeardownPathContent");
        }

        String transId = PathTools.getLocalDomainId() + "-V05-" + UUID.randomUUID().toString();
        msgProps.setGlobalTransactionId(transId);
        teardownPathContent06.setMessageProperties(msgProps);
        return teardownPathContent06;
    }

    public static net.es.oscars.api.soap.gen.v05.TeardownPathContent translate(net.es.oscars.api.soap.gen.v06.TeardownPathContent teardownPath06)
            throws OSCARSServiceException {
        net.es.oscars.api.soap.gen.v05.TeardownPathContent teardownPathContent05 = new net.es.oscars.api.soap.gen.v05.TeardownPathContent();

        try {
            teardownPathContent05.setGlobalReservationId(teardownPath06.getGlobalReservationId());
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate v06 TeardownPathContent");
        }

        try {
            teardownPathContent05.setToken(teardownPath06.getToken());
        } catch (Exception e) {
            // ignore
        }

        return teardownPathContent05;
    }

    public static net.es.oscars.api.soap.gen.v06.TeardownPathResponseContent translate(net.es.oscars.api.soap.gen.v05.TeardownPathResponseContent teardownPathReply05)
            throws OSCARSServiceException {
        net.es.oscars.api.soap.gen.v06.TeardownPathResponseContent teardownPathResponseContent06 = new net.es.oscars.api.soap.gen.v06.TeardownPathResponseContent();
        MessagePropertiesType msgProps = new MessagePropertiesType();

        try {
            teardownPathResponseContent06.setGlobalReservationId(teardownPathReply05.getGlobalReservationId());
            teardownPathResponseContent06.setStatus(teardownPathReply05.getStatus());
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate v05 TeardownPathResponseContent");
        }

        String transId = PathTools.getLocalDomainId() + "-V05-" + UUID.randomUUID().toString();
        msgProps.setGlobalTransactionId(transId);
        teardownPathResponseContent06.setMessageProperties(msgProps);
        return teardownPathResponseContent06;
    }

    public static net.es.oscars.api.soap.gen.v05.TeardownPathResponseContent translate(net.es.oscars.api.soap.gen.v06.TeardownPathResponseContent teardownPathReply06)
            throws OSCARSServiceException {
        net.es.oscars.api.soap.gen.v05.TeardownPathResponseContent teardownPathResponseContent05 = new net.es.oscars.api.soap.gen.v05.TeardownPathResponseContent();

        try {
            teardownPathResponseContent05.setGlobalReservationId(teardownPathReply06.getGlobalReservationId());
            teardownPathResponseContent05.setStatus(teardownPathReply06.getStatus());
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate v06 TeardownPathResponseContent");
        }
        return teardownPathResponseContent05;
    }

    public static net.es.oscars.api.soap.gen.v06.CancelResContent translate(net.es.oscars.api.soap.gen.v05.GlobalReservationId cancelReservation05)
            throws OSCARSServiceException {
        net.es.oscars.api.soap.gen.v06.CancelResContent cancelResContent06 = new net.es.oscars.api.soap.gen.v06.CancelResContent();
        MessagePropertiesType msgProps = new MessagePropertiesType();

        try {
            cancelResContent06.setGlobalReservationId(cancelReservation05.getGri());
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate v05 GlobalReservationId");
        }

        String transId = PathTools.getLocalDomainId() + "-V05-" + UUID.randomUUID().toString();
        msgProps.setGlobalTransactionId(transId);
        cancelResContent06.setMessageProperties(msgProps);
        return cancelResContent06;
    }

    public static net.es.oscars.api.soap.gen.v05.GlobalReservationId translate(net.es.oscars.api.soap.gen.v06.CancelResContent cancelResContent06)
            throws OSCARSServiceException {
        net.es.oscars.api.soap.gen.v05.GlobalReservationId globalReservationId05 = new net.es.oscars.api.soap.gen.v05.GlobalReservationId();

        try {
            globalReservationId05.setGri(cancelResContent06.getGlobalReservationId());
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate v06 CancelResContent");
        }

        return globalReservationId05;
    }

    public static net.es.oscars.api.soap.gen.v06.CancelResReply translate(String cancelReservationReply05) {
        net.es.oscars.api.soap.gen.v06.CancelResReply cancelResReply06 = new net.es.oscars.api.soap.gen.v06.CancelResReply();
        MessagePropertiesType msgProps = new MessagePropertiesType();

        cancelResReply06.setStatus(cancelReservationReply05);

        String transId = PathTools.getLocalDomainId() + "-V05-" + UUID.randomUUID().toString();
        msgProps.setGlobalTransactionId(transId);
        cancelResReply06.setMessageProperties(msgProps);
        return cancelResReply06;
    }

    public static String translate(net.es.oscars.api.soap.gen.v06.CancelResReply cancelReservationReply06) {
        return cancelReservationReply06.getMessageProperties().getGlobalTransactionId();
    }

    public static net.es.oscars.api.soap.gen.v06.ResCreateContent translate(net.es.oscars.api.soap.gen.v05.ResCreateContent createReservation05)
            throws OSCARSServiceException {

        net.es.oscars.api.soap.gen.v06.ResCreateContent resCreateContent06 = new net.es.oscars.api.soap.gen.v06.ResCreateContent();
        net.es.oscars.api.soap.gen.v06.ReservedConstraintType reservedConstraint06 = new net.es.oscars.api.soap.gen.v06.ReservedConstraintType();
        net.es.oscars.api.soap.gen.v06.UserRequestConstraintType userRequestConstraint06 = new net.es.oscars.api.soap.gen.v06.UserRequestConstraintType();
        MessagePropertiesType msgProps = new MessagePropertiesType();

        try {
            resCreateContent06.setGlobalReservationId(createReservation05.getGlobalReservationId());
        } catch (Exception e)                                                                              {
            // ignore
        }

        try {
            resCreateContent06.setDescription(createReservation05.getDescription());
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate v05 ResCreateContent");
        }

        try {
            reservedConstraint06.setPathInfo(translate(createReservation05.getPathInfo()));
            reservedConstraint06.setStartTime(createReservation05.getStartTime());
            reservedConstraint06.setEndTime(createReservation05.getEndTime());
            reservedConstraint06.setBandwidth(createReservation05.getBandwidth());
            resCreateContent06.setReservedConstraint(reservedConstraint06);
        } catch (Exception e) {
            //ignore
        }

        try {
            userRequestConstraint06.setPathInfo(translate(createReservation05.getPathInfo()));
            userRequestConstraint06.setStartTime(createReservation05.getStartTime());
            userRequestConstraint06.setEndTime(createReservation05.getEndTime());
            userRequestConstraint06.setBandwidth(createReservation05.getBandwidth());
            resCreateContent06.setUserRequestConstraint(userRequestConstraint06);
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate v05 ResCreateContent");
        }

        String transId = PathTools.getLocalDomainId() + "-V05-" + UUID.randomUUID().toString();
        msgProps.setGlobalTransactionId(transId);
        resCreateContent06.setMessageProperties(msgProps);
        return resCreateContent06;
    }

    public static net.es.oscars.api.soap.gen.v05.ResCreateContent translate(ResCreateContent modifyReservation06)
            throws OSCARSServiceException {
        net.es.oscars.api.soap.gen.v05.ResCreateContent resCreateContent05 = new net.es.oscars.api.soap.gen.v05.ResCreateContent();

        try {
            resCreateContent05.setGlobalReservationId(modifyReservation06.getGlobalReservationId());
            resCreateContent05.setDescription(modifyReservation06.getDescription());
            resCreateContent05.setStartTime(modifyReservation06.getReservedConstraint().getStartTime());
            resCreateContent05.setEndTime(modifyReservation06.getReservedConstraint().getEndTime());
            resCreateContent05.setBandwidth(modifyReservation06.getReservedConstraint().getBandwidth());
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate v06 ResCreateContent");
        }

        try {
            resCreateContent05.setPathInfo(translate(modifyReservation06.getReservedConstraint().getPathInfo()));
        } catch (Exception e) {
            // ignore
        }
        return resCreateContent05;
    }

    public static net.es.oscars.api.soap.gen.v06.ModifyResContent translate(net.es.oscars.api.soap.gen.v05.ModifyResContent modifyReservation05, String src)
            throws OSCARSServiceException {
        net.es.oscars.api.soap.gen.v06.ModifyResContent modifyReservation06 = new net.es.oscars.api.soap.gen.v06.ModifyResContent();
        net.es.oscars.api.soap.gen.v06.UserRequestConstraintType userConstraints06 = new net.es.oscars.api.soap.gen.v06.UserRequestConstraintType();
       net.es.oscars.api.soap.gen.v06.PathInfo pathInfo06 = new net.es.oscars.api.soap.gen.v06.PathInfo();

        try {
            modifyReservation06.setGlobalReservationId(modifyReservation05.getGlobalReservationId());
            modifyReservation06.setDescription(modifyReservation05.getDescription());

            userConstraints06.setBandwidth(modifyReservation05.getBandwidth());
            userConstraints06.setStartTime(modifyReservation05.getStartTime());
            userConstraints06.setEndTime(modifyReservation05.getEndTime());
            pathInfo06 = translate(modifyReservation05.getPathInfo());
            userConstraints06.setPathInfo(pathInfo06);
            modifyReservation06.setUserRequestConstraint(userConstraints06);
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate v05 ModifyResConstraint");
        }

        if (src != null) {
            // This is a ModifyResContent created by a 0.5 IDC (Forward message). Needs to fill up reservedConstraints
            net.es.oscars.api.soap.gen.v06.ReservedConstraintType reservedConstraints06 =
                    new net.es.oscars.api.soap.gen.v06.ReservedConstraintType();

            reservedConstraints06.setBandwidth(modifyReservation05.getBandwidth());
            reservedConstraints06.setStartTime(modifyReservation05.getStartTime());
            reservedConstraints06.setEndTime(modifyReservation05.getEndTime());
            reservedConstraints06.setPathInfo(pathInfo06);
            modifyReservation06.setReservedConstraint(reservedConstraints06);
        }

        MessagePropertiesType msgProps = new MessagePropertiesType();
        String transId = PathTools.getLocalDomainId() + "-V05-" + UUID.randomUUID().toString();
        msgProps.setGlobalTransactionId(transId);
        modifyReservation06.setMessageProperties(msgProps);
        return modifyReservation06;
    }

    public static net.es.oscars.api.soap.gen.v05.ModifyResContent translate(net.es.oscars.api.soap.gen.v06.ModifyResContent modifyResContent06)
            throws OSCARSServiceException {
        net.es.oscars.api.soap.gen.v05.ModifyResContent modifyResContent05 = new net.es.oscars.api.soap.gen.v05.ModifyResContent();

        try {
            modifyResContent05.setGlobalReservationId(modifyResContent06.getGlobalReservationId());
            modifyResContent05.setDescription(modifyResContent06.getDescription());
            modifyResContent05.setStartTime(modifyResContent06.getReservedConstraint().getStartTime());
            modifyResContent05.setEndTime(modifyResContent06.getReservedConstraint().getEndTime());
            modifyResContent05.setBandwidth(modifyResContent06.getReservedConstraint().getBandwidth());
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate v06 ModifyResContent");
        }

        try {
            modifyResContent05.setPathInfo(translate(modifyResContent06.getReservedConstraint().getPathInfo()));
        } catch (Exception e) {
            // ignore
        }
        return modifyResContent05;
    }

    public static net.es.oscars.api.soap.gen.v05.ModifyResReply translate(net.es.oscars.api.soap.gen.v06.ModifyResReply modifyResReply06)
            throws OSCARSServiceException {
        net.es.oscars.api.soap.gen.v05.ModifyResReply modifyResReply05 = new net.es.oscars.api.soap.gen.v05.ModifyResReply();
        ResDetails resDetails = new ResDetails();

        try {
            resDetails.setBandwidth(modifyResReply06.getReservation().getReservedConstraint().getBandwidth());
            resDetails.setCreateTime(modifyResReply06.getReservation().getCreateTime());
            resDetails.setDescription(modifyResReply06.getReservation().getDescription());
            resDetails.setLogin(modifyResReply06.getReservation().getLogin());
            resDetails.setStartTime(modifyResReply06.getReservation().getReservedConstraint().getStartTime());
            resDetails.setEndTime(modifyResReply06.getReservation().getReservedConstraint().getEndTime());
            resDetails.setStatus(modifyResReply06.getReservation().getStatus());
            resDetails.setPathInfo(translate(modifyResReply06.getReservation().getReservedConstraint().getPathInfo()));

            modifyResReply05.setReservation(resDetails);
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate v06 ModifyResReply");
        }
        return modifyResReply05;
    }

    public static net.es.oscars.api.soap.gen.v06.ModifyResReply translate(net.es.oscars.api.soap.gen.v05.ModifyResReply modifyResReply05)
            throws OSCARSServiceException {
        net.es.oscars.api.soap.gen.v06.ModifyResReply modifyResReply06 = new net.es.oscars.api.soap.gen.v06.ModifyResReply();
        net.es.oscars.api.soap.gen.v06.ResDetails resDetails06 = new net.es.oscars.api.soap.gen.v06.ResDetails();
        net.es.oscars.api.soap.gen.v06.ReservedConstraintType reservedConstraint06 = new net.es.oscars.api.soap.gen.v06.ReservedConstraintType();
        net.es.oscars.api.soap.gen.v06.UserRequestConstraintType userRequestConstraint06 = new net.es.oscars.api.soap.gen.v06.UserRequestConstraintType();
        MessagePropertiesType msgProps = new MessagePropertiesType();
        SubjectAttributes originator = new SubjectAttributes();
        AttributeType attr = new AttributeType();

        try {
            resDetails06.setGlobalReservationId(modifyResReply05.getReservation().getGlobalReservationId());
            resDetails06.setDescription(modifyResReply05.getReservation().getDescription());
            resDetails06.setStatus(modifyResReply05.getReservation().getStatus());
            resDetails06.setCreateTime(modifyResReply05.getReservation().getCreateTime());
            resDetails06.setLogin(modifyResReply05.getReservation().getLogin());

            reservedConstraint06.setBandwidth(modifyResReply05.getReservation().getBandwidth());
            reservedConstraint06.setStartTime(modifyResReply05.getReservation().getStartTime());
            reservedConstraint06.setEndTime(modifyResReply05.getReservation().getEndTime());
            reservedConstraint06.setPathInfo(translate(modifyResReply05.getReservation().getPathInfo()));
            resDetails06.setReservedConstraint(reservedConstraint06);
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate v05 ModifyResReply");
        }
        try {
            userRequestConstraint06.setBandwidth(modifyResReply05.getReservation().getBandwidth());
            userRequestConstraint06.setStartTime(modifyResReply05.getReservation().getStartTime());
            userRequestConstraint06.setEndTime(modifyResReply05.getReservation().getEndTime());
            userRequestConstraint06.setPathInfo(translate(modifyResReply05.getReservation().getPathInfo()));
            resDetails06.setUserRequestConstraint(userRequestConstraint06);
        } catch (Exception e) {
            //ignore
        }

        modifyResReply06.setReservation(resDetails06);

        String transId = PathTools.getLocalDomainId() + "-V05-" + UUID.randomUUID().toString();
        msgProps.setGlobalTransactionId(transId);

        String loginName = modifyResReply05.getReservation().getLogin();
        attr.setName(AuthZConstants.LOGIN_ID);
        attr.getAttributeValue().add(loginName);
        originator.getSubjectAttribute().add(attr);
        msgProps.setOriginator(originator);
        modifyResReply06.setMessageProperties(msgProps);
        return modifyResReply06;
    }

    public static net.es.oscars.api.soap.gen.v05.PathInfo translate(net.es.oscars.api.soap.gen.v06.PathInfo pathInfo06)
            throws OSCARSServiceException {
        net.es.oscars.api.soap.gen.v05.PathInfo pathInfo05 = new net.es.oscars.api.soap.gen.v05.PathInfo();
        CtrlPlanePathContent ctrlPlanePathContent = new CtrlPlanePathContent();

        try {
            pathInfo05.setPathType(pathInfo06.getPathType());
            pathInfo05.setLayer2Info(translate(pathInfo06.getLayer2Info()));
            pathInfo05.setLayer3Info(translate(pathInfo06.getLayer3Info()));
            pathInfo05.setMplsInfo(translate(pathInfo06.getMplsInfo()));
        } catch (Exception e) {
            // ignore
        }

        try {
            pathInfo05.setPathSetupMode(pathInfo06.getPathSetupMode());
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate v06 PathInfo");
        }

        try {
            ctrlPlanePathContent.setDirection(pathInfo06.getPath().getDirection());
            ctrlPlanePathContent.setId(pathInfo06.getPath().getId());
            ctrlPlanePathContent.setLifetime(pathInfo06.getPath().getLifetime());
        } catch (Exception e) {
            // ignore
        }

        pathInfo05.setPath(ctrlPlanePathContent);
        return pathInfo05;
    }

    public static net.es.oscars.api.soap.gen.v06.PathInfo translate(net.es.oscars.api.soap.gen.v05.PathInfo pathInfo05)
            throws OSCARSServiceException {
        net.es.oscars.api.soap.gen.v06.PathInfo pathInfo06 = new net.es.oscars.api.soap.gen.v06.PathInfo();

        try {
            pathInfo06.setPathSetupMode(pathInfo05.getPathSetupMode());
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate v05 PathInfo");
        }

        try {
            pathInfo06.setPath(pathInfo05.getPath());
            pathInfo06.setPathType(pathInfo05.getPathType());
            pathInfo06.setLayer2Info(translate(pathInfo05.getLayer2Info()));
            pathInfo06.setLayer3Info(translate(pathInfo05.getLayer3Info()));
            pathInfo06.setMplsInfo(translate(pathInfo05.getMplsInfo()));
        } catch (Exception e) {
            //ignore
        }
        return pathInfo06;
    }

    public static net.es.oscars.api.soap.gen.v06.Layer2Info translate(net.es.oscars.api.soap.gen.v05.Layer2Info layer2Info05)
            throws OSCARSServiceException {
        net.es.oscars.api.soap.gen.v06.Layer2Info layer2Info06 = new net.es.oscars.api.soap.gen.v06.Layer2Info();

        try {
            layer2Info06.setDestEndpoint(layer2Info05.getDestEndpoint());
            layer2Info06.setSrcEndpoint(layer2Info05.getSrcEndpoint());
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate v05 Layer2Info");
        }

        try {
            layer2Info06.setSrcVtag(translate(layer2Info05.getSrcVtag()));
            layer2Info06.setDestVtag(translate(layer2Info05.getDestVtag()));
        } catch (Exception e) {
            // ignore
        }
        return layer2Info06;
    }

    public static net.es.oscars.api.soap.gen.v05.Layer2Info translate(net.es.oscars.api.soap.gen.v06.Layer2Info layer2Info06)
            throws OSCARSServiceException {
        net.es.oscars.api.soap.gen.v05.Layer2Info layer2Info05 = new net.es.oscars.api.soap.gen.v05.Layer2Info();

        try {
            layer2Info05.setDestEndpoint(layer2Info06.getDestEndpoint());
            layer2Info05.setSrcEndpoint(layer2Info06.getSrcEndpoint());
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate v06 Layer2Info");
        }

        try {
            layer2Info05.setSrcVtag(translate(layer2Info06.getSrcVtag()));
            layer2Info05.setDestVtag(translate(layer2Info06.getDestVtag()));
        } catch (Exception e) {
            //ignore
        }
        return layer2Info05;
    }

    public static net.es.oscars.api.soap.gen.v05.VlanTag translate(net.es.oscars.api.soap.gen.v06.VlanTag vlanTag06)
            throws OSCARSServiceException {
        net.es.oscars.api.soap.gen.v05.VlanTag vlanTag05 = new net.es.oscars.api.soap.gen.v05.VlanTag();

        try {
            vlanTag05.setTagged(vlanTag06.isTagged());
            vlanTag05.setValue(vlanTag06.getValue());
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate v06 VlanTag");
        }
        return vlanTag05;
    }

    public static net.es.oscars.api.soap.gen.v06.VlanTag translate(net.es.oscars.api.soap.gen.v05.VlanTag vlanTag05)
            throws OSCARSServiceException {
        net.es.oscars.api.soap.gen.v06.VlanTag vlanTag06 = new net.es.oscars.api.soap.gen.v06.VlanTag();

        try {
            vlanTag06.setTagged(vlanTag05.isTagged());
            vlanTag06.setValue(vlanTag05.getValue());
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate v05 VlanTag");
        }
        return vlanTag06;
    }

    public static net.es.oscars.api.soap.gen.v06.Layer3Info translate(net.es.oscars.api.soap.gen.v05.Layer3Info layer3Info05)
            throws OSCARSServiceException {
        net.es.oscars.api.soap.gen.v06.Layer3Info layer3Info06 = new net.es.oscars.api.soap.gen.v06.Layer3Info();

        try {
            layer3Info06.setDscp(layer3Info05.getDscp());
            layer3Info06.setProtocol(layer3Info05.getProtocol());
            layer3Info06.setSrcIpPort(layer3Info05.getSrcIpPort());
            layer3Info06.setDestIpPort(layer3Info05.getDestIpPort());
        } catch (Exception e) {
            // ignore
        }

        try {
            layer3Info06.setSrcHost(layer3Info05.getSrcHost());
            layer3Info06.setDestHost(layer3Info05.getDestHost());
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate v05 Layer3Info");
        }

        return layer3Info06;
    }

    public static net.es.oscars.api.soap.gen.v05.Layer3Info translate(net.es.oscars.api.soap.gen.v06.Layer3Info layer3Info06)
            throws OSCARSServiceException {
        net.es.oscars.api.soap.gen.v05.Layer3Info layer3Info05 = new net.es.oscars.api.soap.gen.v05.Layer3Info();

        try {
            layer3Info05.setSrcHost(layer3Info06.getSrcHost());
            layer3Info05.setDestHost(layer3Info06.getDestHost());
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate v06 Layer3Info");
        }

        try {
            layer3Info05.setDestIpPort(layer3Info06.getDestIpPort());
            layer3Info05.setDscp(layer3Info06.getDscp());
            layer3Info05.setProtocol(layer3Info06.getProtocol());
            layer3Info05.setSrcIpPort(layer3Info06.getSrcIpPort());
        } catch (Exception e) {
            // ignore
        }

        return layer3Info05;
    }

    public static net.es.oscars.api.soap.gen.v06.MplsInfo translate(net.es.oscars.api.soap.gen.v05.MplsInfo mplsInfo05)
            throws OSCARSServiceException {
        net.es.oscars.api.soap.gen.v06.MplsInfo mplsInfo06 = new net.es.oscars.api.soap.gen.v06.MplsInfo();

        try {
            mplsInfo06.setBurstLimit(mplsInfo05.getBurstLimit());
            mplsInfo06.setLspClass(mplsInfo05.getLspClass());
        } catch (Exception e) {
            throw new  OSCARSServiceException("Unable to translate v05 MplsInfo05");
        }

        return mplsInfo06;
    }

    public static net.es.oscars.api.soap.gen.v05.MplsInfo translate(net.es.oscars.api.soap.gen.v06.MplsInfo mplsInfo06)
            throws OSCARSServiceException {
        net.es.oscars.api.soap.gen.v05.MplsInfo mplsInfo05 = new net.es.oscars.api.soap.gen.v05.MplsInfo();

        try {
            mplsInfo05.setBurstLimit(mplsInfo06.getBurstLimit());
            mplsInfo05.setLspClass(mplsInfo06.getLspClass());
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate v06 MplsInfo06");
        }

        return mplsInfo05;
    }

    public static net.es.oscars.api.soap.gen.v06.InterDomainEventContent translate(org.oasis_open.docs.wsn.b_2.Notify notify05)
            throws OSCARSServiceException {
        net.es.oscars.api.soap.gen.v06.InterDomainEventContent interDomainEventContent = new net.es.oscars.api.soap.gen.v06.InterDomainEventContent();
        MessagePropertiesType msgProps = new MessagePropertiesType();
        net.es.oscars.api.soap.gen.v06.ResDetails resDetails = new net.es.oscars.api.soap.gen.v06.ResDetails();

        try {
            interDomainEventContent.setType(notify05.getNotificationMessage().get(0).getMessage().toString());
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate v05 Notify");
        }
/*
        try {
            interDomainEventContent.setErrorCode(null);
            interDomainEventContent.setErrorSource(null);
            interDomainEventContent.setErrorMessage(null);
            interDomainEventContent.setResDetails(null);
        } catch (Exception e) {
            //ignore
        }
*/
        String transId = PathTools.getLocalDomainId() + "-V05-" + UUID.randomUUID().toString();
        msgProps.setGlobalTransactionId(transId);
        interDomainEventContent.setMessageProperties(msgProps);

        return interDomainEventContent;
    }

    public static org.oasis_open.docs.wsn.b_2.Notify translate(net.es.oscars.api.soap.gen.v06.InterDomainEventContent eventContent06)
            throws OSCARSServiceException {
        org.oasis_open.docs.wsn.b_2.Notify notify = new org.oasis_open.docs.wsn.b_2.Notify();
        NotificationMessageHolderType notificationMessageHolder = new NotificationMessageHolderType();
        TopicExpressionType topicExpressionType = new TopicExpressionType();
        MessageType messageType = new MessageType();
        net.es.oscars.api.soap.gen.v05.ResDetails resDetails = new net.es.oscars.api.soap.gen.v05.ResDetails();
        net.es.oscars.api.soap.gen.v05.EventContent eventContent = new net.es.oscars.api.soap.gen.v05.EventContent();

        try {
            topicExpressionType.setValue("idc:IDC");
            topicExpressionType.setDialect("http://docs.oasis-open.org/wsn/t-1/TopicExpression/Full");
            notificationMessageHolder.setTopic(topicExpressionType);

            resDetails.setPathInfo(translate(eventContent06.getResDetails().getReservedConstraint().getPathInfo()));
            resDetails.setDescription(eventContent06.getResDetails().getDescription());
            resDetails.setEndTime(eventContent06.getResDetails().getReservedConstraint().getEndTime());
            resDetails.setLogin(eventContent06.getResDetails().getLogin());
            resDetails.setStatus(eventContent06.getResDetails().getStatus());
            resDetails.setBandwidth(eventContent06.getResDetails().getReservedConstraint().getBandwidth());
            resDetails.setStartTime(eventContent06.getResDetails().getReservedConstraint().getStartTime());
            resDetails.setGlobalReservationId(eventContent06.getResDetails().getGlobalReservationId());

            eventContent.setResDetails(resDetails);
            eventContent.setErrorSource(eventContent06.getErrorSource());
            eventContent.setErrorCode(eventContent06.getErrorCode());
            eventContent.setErrorMessage(eventContent06.getErrorMessage());
            eventContent.setType(eventContent06.getType());

            messageType.getAny().set(0, eventContent);
            notificationMessageHolder.setMessage(messageType);
            notify.getNotificationMessage().set(0, notificationMessageHolder);
        } catch (Exception e) {
            throw new OSCARSServiceException("Unable to translate v06 InterDomainEventContent");
        }

        return notify;
    }
}
