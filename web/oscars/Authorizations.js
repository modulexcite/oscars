/*
Authorizations.js:  Handles authorizations list functionality.
                    Note that it uses a grid.
David Robertson (dwrobertson@lbl.gov)
*/

/* Functions:
goToAdd()
handleReply(responseObject, ioArgs)
tabSelected(contentPane, oscarsStatus)
refreshAuthGrid()
onAuthRowSelect(evt)
*/

dojo.provide("oscars.Authorizations");

// Goes to the add version of the authorization details page.
// This is a client side only function.
oscars.Authorizations.goToAdd = function () {
    oscars.AuthorizationDetails.resetFields(false);
    var modifyAuthorizationNode = dojo.byId("modifyAuthorizationDisplay");
    modifyAuthorizationNode.style.display = "none";
    var addAuthorizationNode = dojo.byId("addAuthorizationDisplay");
    addAuthorizationNode.style.display = "";
    // set to authorization details tab
    var mainTabContainer = dijit.byId("mainTabContainer");
    var authDetailsPane = dijit.byId("authDetailsPane");
    mainTabContainer.selectChild(authDetailsPane);
    var oscarsStatus = dojo.byId("oscarsStatus");
    oscarsStatus.className = "success";
    oscarsStatus.innerHTML = "Adding authorization";
    oscarsState.authorizationState.clearAuthState();
};

// handles all servlet replies
oscars.Authorizations.handleReply = function (responseObject, ioArgs) {
    if (responseObject.method == "AuthorizationList") {
        if (!oscars.Form.resetStatus(responseObject)) {
            return;
        }
        // set parameter values in form from responseObject
        oscars.Form.applyParams(responseObject);
        var mainTabContainer = dijit.byId("mainTabContainer");
        var authGrid = dijit.byId("authGrid");
        var model = authGrid.model;
        model.setData(responseObject.authData);
        authGrid.setSortIndex(0, true);
        authGrid.sort();
        authGrid.update();
        authGrid.resize();
        authGrid.resize();
        oscarsState.authGridInitialized = true;
    }
};

// takes action based on this tab being clicked on
oscars.Authorizations.tabSelected = function (
        /* ContentPane widget */ contentPane,
        /* domNode */ oscarsStatus) {
    oscarsStatus.innerHTML = "Authorizations list";
    oscarsStatus.className = "success";
    var authGrid = dijit.byId("authGrid");
    // Creation apparently needs to be programmatic, after the ContentPane
    // has been selected and its style no longer display:none
    if (authGrid && (!oscarsState.authGridInitialized)) {
        dojo.connect(authGrid, "onRowClick", oscars.Authorizations.onAuthRowSelect);
        oscars.Authorizations.refreshAuthGrid();
    } else {
        var authListFormNode = dijit.byId("authListForm").domNode;
        // if authorizations have been added, or attributes have been
        // modified, list needs to be updated
        if (authListFormNode.authsAdded.value ||
            authListFormNode.authListAttrsUpdated.value) {
            oscars.Authorizations.refreshAuthGrid();
            authListFormNode.authsAdded.value = "";
            authListFormNode.authListAttrsUpdated.value = "";
        }
    }
};

// refresh authorizations list from servlet
oscars.Authorizations.refreshAuthGrid = function () {
    dojo.xhrPost({
        url: 'servlet/AuthorizationList',
        handleAs: "json-comment-filtered",
        load: oscars.Authorizations.handleReply,
        error: oscars.Form.handleError,
        form: dijit.byId("authListForm").domNode
    });
};

// select authorization details based on row select in grid
oscars.Authorizations.onAuthRowSelect = function (/*Event*/ evt) {
    var mainTabContainer = dijit.byId("mainTabContainer");
    var authDetailsPane = dijit.byId("authDetailsPane");
    var modifyAuthorizationNode = dojo.byId("modifyAuthorizationDisplay");
    modifyAuthorizationNode.style.display = "";
    var addAuthorizationNode = dojo.byId("addAuthorizationDisplay");
    addAuthorizationNode.style.display = "none";
    var authGrid = dijit.byId("authGrid");
    var formNode = dijit.byId("authDetailsForm").domNode;
    // clear constraint value if any
    formNode.reset();
    // set four parameters necessary to retrieve authorization
    // dijit.byId doesn't seem to work outside form and tab
    var attributeName = authGrid.model.getDatum(evt.rowIndex, 0);
    var resourceName = authGrid.model.getDatum(evt.rowIndex, 1);
    var permissionName = authGrid.model.getDatum(evt.rowIndex, 2);
    var constraintName = authGrid.model.getDatum(evt.rowIndex, 3);
    var constraintValue = authGrid.model.getDatum(evt.rowIndex, 4);
    oscarsState.authorizationState.saveAuthState(attributeName,
            resourceName, permissionName, constraintName, constraintValue);
    // No need to query server; grid already contains all information.
    // Can't set up menus in grid; different fields may require different
    // subsets of values of other fields.
    // Set tab to authorization details, which allows authorization modification
    // and cloning.
    mainTabContainer.selectChild(authDetailsPane);
    var oscarsStatus = dojo.byId("oscarsStatus");
    oscarsStatus.className = "success";
    oscarsStatus.innerHTML = "Modifying authorization";
};
