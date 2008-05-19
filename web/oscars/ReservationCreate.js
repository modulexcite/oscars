/*
ReservationCreate.js:   Handles reservation creation.
Last modified:  May 19, 2008
David Robertson (dwrobertson@lbl.gov)
*/

/* Functions:
init()
handleReply(responseObject, ioArgs)
tabSelected(contentPaneWidget, changeStatus)
layerChooser(evt);
checkDateTimes()
*/

dojo.provide("oscars.ReservationCreate");

// posts request to server to set parameters in initial create reservation form
oscars.ReservationCreate.init = function () {
    dojo.xhrPost({
        url: "servlet/CreateReservationForm",
        handleAs: "json-comment-filtered",
        load: oscars.ReservationCreate.handleReply,
        error: oscars.Form.handleError,
        form: dijit.byId("reservationCreateForm").domNode
   });
};

// posts request to server to create reservation
oscars.ReservationCreate.createReservation = function () {
    var legalDates = oscars.ReservationCreate.checkDateTimes();
    // status bar shows error message
    if (!legalDates) {
        return;
    }
    // check validity of rest of fields
    var valid = dijit.byId("reservationCreateForm").validate();
    if (!valid) {
        return;
    } 
    var oscarsStatus = dojo.byId("oscarsStatus");
    oscarsStatus.className = "inprocess";
    oscarsStatus.innerHTML = "Creating reservation...";
    dojo.xhrPost({
      url: 'servlet/CreateReservation',
      handleAs: "json-comment-filtered",
      load: oscars.ReservationCreate.handleReply,
      error: oscars.Form.handleError,
      form: dijit.byId("reservationCreateForm").domNode
    });
};

// handles replies from servlets having to do with reservation creation
oscars.ReservationCreate.handleReply = function (responseObject, ioArgs) {
    if (!oscars.Form.resetStatus(responseObject)) {
        return;
    }
    var mainTabContainer = dijit.byId("mainTabContainer");
    if (responseObject.method == "CreateReservationForm") {
        // set parameter values in form from responseObject
        oscars.Form.applyParams(responseObject);
    } else if (responseObject.method == "CreateReservation") {
        // transition to reservation details tab on successful creation
        var formParam = dijit.byId("reservationDetailsForm").domNode;
        formParam.gri.value = responseObject.gri;
        dojo.xhrPost({
            url: 'servlet/QueryReservation',
            handleAs: "json-comment-filtered",
            load: oscars.ReservationDetails.handleReply,
            error: oscars.Form.handleError,
            form: dijit.byId("reservationDetailsForm").domNode
        });
        // set tab to reservation details
        var resvDetailsPaneTab = dijit.byId("reservationDetailsPane");
        mainTabContainer.selectChild(resvDetailsPaneTab);
    }
};

// take action when this tab is clicked on
oscars.ReservationCreate.tabSelected = function (
        /* ContentPane widget */ contentPane,
        /* Boolean */ oscarsStatus,
        /* Boolean */ changeStatus) {
    if (changeStatus) {
        oscarsStatus.innerHTML = "Reservation creation form";
    }
    if (contentPane.href == "") {
        contentPane.setHref("forms/reservationCreate.html");
    }
};

// chooses layer 2 or layer 3 parameters to display in create reservation page
oscars.ReservationCreate.layerChooser = function (/*Event*/ evt) {
    var i;
    var layer2Nodes = dojo.query(".layer2");
    var layer3Nodes = dojo.query(".layer3");
    if (evt.target.id == "layer2") {
        for (i = 0; i < layer2Nodes.length; i++) {
            layer2Nodes[i].style.display = ""; 
        }
        for (i = 0; i < layer3Nodes.length; i++) {
            layer3Nodes[i].style.display = "none"; 
        }
    } else if (evt.target.id == "layer3") {
        for (i = 0; i < layer2Nodes.length; i++) {
            layer2Nodes[i].style.display = "none"; 
        }
        for (i = 0; i < layer3Nodes.length; i++) {
            layer3Nodes[i].style.display = ""; 
        }
    }
};

// check create reservation form's start and end date and time's, and
// converts hidden form fields to seconds
oscars.ReservationCreate.checkDateTimes = function () {
    var currentDate = new Date();
    var msg = null;
    var startSeconds =
        oscars.DigitalClock.convertDateTime(currentDate, "startDate",
                                            "startTime", true);
    // default is 4 minutes in the future
    var endDate = new Date(startSeconds*1000 + 60*4*1000);
    var endSeconds =
            oscars.DigitalClock.convertDateTime(endDate, "endDate", "endTime",
                                                true);
    // additional checks for legality
    // check for start time more than four minutes in the past
    if (startSeconds < (currentDate.getTime()/1000 - 240)) {
        msg = "Start time is more than four minutes in the past";
    } else if (startSeconds > endSeconds) {
        msg = "End time is before start time";
    } else if (startSeconds == endSeconds) {
        msg = "End time is the same as start time";
    }
    if (msg != null) {
        var oscarsStatus = dojo.byId("oscarsStatus");
        oscarsStatus.className = "failure";
        oscarsStatus.innerHTML = msg;
        return false;
    }
    var startSecondsN = dojo.byId("hiddenStartSeconds");
    // set hidden field value, which is what servlet uses
    startSecondsN.value = startSeconds;
    var endSecondsN = dojo.byId("hiddenEndSeconds");
    endSecondsN.value = endSeconds;
    return true;
};
