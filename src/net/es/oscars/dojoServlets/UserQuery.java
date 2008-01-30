package net.es.oscars.dojoServlets;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;
import org.hibernate.*;
import net.sf.json.*;

import net.es.oscars.database.HibernateUtil;
import net.es.oscars.aaa.User;
import net.es.oscars.aaa.UserManager;
import net.es.oscars.aaa.UserManager.AuthValue;
import net.es.oscars.aaa.Institution;
import net.es.oscars.aaa.AAAException;


public class UserQuery extends HttpServlet {
    private Logger log;
    private String dbname;
    
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws IOException, ServletException {

        this.log = Logger.getLogger(this.getClass());
        this.dbname = "aaa";
        this.log.debug("userQuery:start");

        User targetUser = null;
        boolean self =  false; // is query about the current user
        boolean modifyAllowed = false;

        UserSession userSession = new UserSession();
        UserManager mgr = new UserManager("aaa");
        List<Institution> institutions = null;
        List<String> attrNames = new ArrayList<String>();
        Utils utils = new Utils();

        PrintWriter out = response.getWriter();
        response.setContentType("text/json-comment-filtered");
        String userName = userSession.checkSession(out, request);
        if (userName == null) { return; }

        String profileName = request.getParameter("login");
        Session aaa = 
            HibernateUtil.getSessionFactory("aaa").getCurrentSession();
        aaa.beginTransaction();

        if (profileName != null) { // get here by clicking on a name in the users list
            if (profileName.equals(userName)) { 
                self =true; 
            } else {
                self = false;
            }
        } else { // profileName is null - get here by clicking on tab navigation
            profileName = userName;
            self=true;
        }
        AuthValue authVal = mgr.checkAccess(userName, "Users", "query");
        
        if ((authVal == AuthValue.ALLUSERS)  ||  ( self && (authVal == AuthValue.SELFONLY))) {
              targetUser= mgr.query(profileName);
         } else {
            utils.handleFailure(out,"no permission to query users", aaa,null);
            return;
        }
        /* check to see if user has modify permission for this user
         *     used by conentSection to set the action on submit
         */
       authVal = mgr.checkAccess(userName, "Users", "modify");
       if (self) {attrNames = mgr.getAttrNames();}
       else {attrNames = mgr.getAttrNames(profileName);}
        
        if ((authVal == AuthValue.ALLUSERS)  ||  ( self && (authVal == AuthValue.SELFONLY))) {
              modifyAllowed = true;
         } else {
            modifyAllowed = false;;
        }
        institutions = mgr.getInstitutions();
        Map outputMap = new HashMap();
        outputMap.put("status", "Profile for user " + profileName);
        this.contentSection(
                outputMap, targetUser, modifyAllowed,
                (authVal == AuthValue.ALLUSERS),
                institutions, attrNames);
        outputMap.put("method", "UserQuery");
        outputMap.put("success", Boolean.TRUE);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("/* " + jsonObject + " */");
        aaa.getTransaction().commit();
        this.log.debug("userQuery:finish");
    }

    public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }

    /**
     * writes out the parameter values that are the result of a user query
     * 
     * @param outputMap map with parameter values for userPane
     * @param user the user whose information is being displayed 
     * @param modifyAllowed - true if the  user displaying this information has 
     *                        permission to modify it
     * @param modifyRights true if the  user displaying this information has 
     *                     permission to modify the target user's attributes
     * @param insts list of all institutions (a constant?)
     * @param attrNames all the attributes of the target user
     */
    public void
        contentSection(Map outputMap, User user, boolean modifyAllowed,
                       boolean modifyRights, List<Institution> insts,
                       List<String> attrNames) {

        this.log.debug("contentSection: start");
        if (modifyAllowed) {
            outputMap.put("allowModify", Boolean.TRUE);
            outputMap.put("userHeader",
                          "Editing profile for user: " + user.getLogin());
        } else {
            outputMap.put("allowModify", Boolean.FALSE);
            outputMap.put("userHeader", "Profile for user: " + user.getLogin());
        } 
        if (attrNames == null) {
            this.log.debug("contentSection: attrNames is null");
        }
        if (attrNames.isEmpty()) {
            this.log.debug("contentSection: attrNames is empty");
        }

        String strParam = user.getLogin();
        if (strParam == null) { strParam = ""; }
        outputMap.put("profileName", strParam);
        strParam = user.getPassword();
        if (strParam != null) {
            outputMap.put("password", "********");
        }
        if (strParam != null) {
           outputMap.put("passwordConfirmation", "********");
        }
        strParam = user.getFirstName();
        if (strParam != null) {
           outputMap.put("firstName", strParam);
        }
        strParam = user.getLastName();
        if (strParam != null) {
           outputMap.put("lastName", strParam);
        }
        strParam = user.getCertSubject();
        if (strParam != null) {
           outputMap.put("certSubject", strParam);
        }
        strParam = user.getCertIssuer();
        if (strParam != null) {
           outputMap.put("certIssuer", strParam);
        }
        this.outputInstitutionMenu(outputMap, insts, user);
        this.outputRoleMap(outputMap, attrNames, modifyRights);
        
        strParam = user.getDescription();
        if (strParam != null) {
           outputMap.put("description", strParam);
        }
        strParam = user.getEmailPrimary();
        if (strParam != null) {
           outputMap.put("emailPrimary", strParam);
        }
        strParam = user.getEmailSecondary();
        if (strParam != null) {
           outputMap.put("emailSecondary", strParam);
        }
        strParam = user.getPhonePrimary();
        if (strParam != null) {
           outputMap.put("phonePrimary", strParam);
        }
        strParam = user.getPhoneSecondary();
        if (strParam != null) {
           outputMap.put("phoneSecondary", strParam);
        }
        this.log.debug("contentSection: finish");
    }

    public void
        outputInstitutionMenu(Map outputMap, List<Institution> insts,
                              User user) {

        Institution userInstitution = null;
        String institutionName = "";
        StringBuffer sb = new StringBuffer();

        sb.append("<select class='required' name='institutionName'>");
        userInstitution = user.getInstitution();
        if (userInstitution != null) {
            institutionName = userInstitution.getName();
        } else {
            // use default
            institutionName = "Energy Sciences Network";
        }
        for (Institution i: insts) {
            sb.append("<option value='" + i.getName() + "' ");
            if (i.getName().equals(institutionName)) {
                sb.append("selected='selected'" );
            }
            sb.append(">" + i.getName() + "</option>" );
        }
        sb.append("</select>");
        outputMap.put("institutionMenuDiv", sb.toString());
    }
    
    public void outputRoleMap(Map outputMap, List<String> attrNames,
                               boolean modify) {

        Map roleMap = new HashMap();
        if (attrNames.contains("OSCARS-user")) {
            roleMap.put("oscarsUserRole", Boolean.TRUE);
        } else {
            roleMap.put("oscarsUserRole", Boolean.FALSE);
        }
        if (attrNames.contains("OSCARS-engineer")) {
            roleMap.put("engineerUserRole", Boolean.TRUE);
        } else {
            roleMap.put("engineerUserRole", Boolean.FALSE);
        }
        if (attrNames.contains("OSCARS-administrator")){
            roleMap.put("adminUserRole", Boolean.TRUE);
        } else {
            roleMap.put("adminUserRole", Boolean.FALSE);
        }
        if (attrNames.contains("OSCARS-service")) {
            roleMap.put("serviceUserRole", Boolean.TRUE);
        } else {
            roleMap.put("serviceUserRole", Boolean.FALSE);
        }
        if (modify) {
            roleMap.put("modify", Boolean.TRUE);
        } else {
            roleMap.put("modify", Boolean.FALSE);
        }
        outputMap.put("roleCheckboxes", roleMap);
    }
}
