package net.es.oscars.servlets;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;
import org.hibernate.*;
import net.sf.json.*;

import net.es.oscars.database.HibernateUtil;
import net.es.oscars.aaa.*;
import net.es.oscars.aaa.UserManager.AuthValue;


public class AuthorizationModify extends HttpServlet {

    public void
        doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        UserSession userSession = new UserSession();
        UserManager mgr = new UserManager(Utils.getDbName());
        Logger log = Logger.getLogger(this.getClass());
        log.debug("servlet.start");

        String methodName = "AuthorizationModify";
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        String userName = userSession.checkSession(out, request, methodName);
        if (userName == null) {
            log.error("No user session: cookies invalid");
            return;
        }
        Session aaa = 
            HibernateUtil.getSessionFactory(Utils.getDbName()).getCurrentSession();
        aaa.beginTransaction();
        AuthValue authVal = mgr.checkAccess(userName, "AAA", "modify");
        if (authVal == AuthValue.DENIED)  { 
            log.error("Not allowed to modify an authorization");
            Utils.handleFailure(out, "not allowed to modify an authorization",
                                methodName, aaa);
            return;
        }
        String attribute = request.getParameter("authAttributeName");
        String permission = request.getParameter("permissionName");
        String resource = request.getParameter("resourceName");
        String constraintName = request.getParameter("constraintName");
        String constraintValue = request.getParameter("constraintValue");
        String origAttribute = request.getParameter("oldAuthAttributeName");
        String origPermission = request.getParameter("oldPermissionName");
        String origResource = request.getParameter("oldResourceName");
        String origConstraint = request.getParameter("oldConstraintName");
        
        log.debug("modifying attribute: " + origAttribute + " to "+ attribute +
                " resource: " + origResource + " to " + resource + 
                " permission: " + origPermission + " to " + permission + 
                " constraintName: " + origConstraint + " to " + constraintName );
        
        AuthorizationDAO authDAO = new AuthorizationDAO(Utils.getDbName());
        try {
            Authorization auth = authDAO.query(origAttribute,origResource,origPermission, 
                    origConstraint);
            authDAO.update(auth,attribute,resource,permission,constraintName, constraintValue);
        } catch ( AAAException e) {
            log.error(e.getMessage());
            Utils.handleFailure(out, e.getMessage(), methodName, aaa);
            return;           
        }
        Map outputMap = new HashMap();
        outputMap.put("status", "Authorization modified");
        outputMap.put("method", methodName);
        outputMap.put("success", Boolean.TRUE);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("{}&&" + jsonObject);
        aaa.getTransaction().commit();
        log.debug("servlet.end");      
    }

    public void
        doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }
    
  
}
