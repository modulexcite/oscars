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


public class Attributes extends HttpServlet {
    private Logger log;
    
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws IOException, ServletException {


        this.log = Logger.getLogger(this.getClass());
        String methodName = "Attributes";
        this.log.debug("servlet.start");
        UserSession userSession = new UserSession();
        PrintWriter out = response.getWriter();
        String[] ops = request.getQueryString().split("=");
        if (ops.length != 2) {
            this.log.error("Incorrect input from Attributes page");
            Utils.handleFailure(out, "incorrect input from Attributes page",
                                methodName, null);
            return;
        }
        String opName = ops[1];

        response.setContentType("text/json-comment-filtered");
        String userName = userSession.checkSession(out, request, methodName);
        if (userName == null) {
            this.log.error("No user session: cookies invalid");
            return;
        }
        Session aaa = 
            HibernateUtil.getSessionFactory(Utils.getDbName()).getCurrentSession();
        aaa.beginTransaction();     
        UserManager mgr = new UserManager(Utils.getDbName());
        
        AuthValue authVal = mgr.checkAccess(userName, "AAA", "modify");
        if (authVal == AuthValue.DENIED) {
            this.log.error("No permission to modify Attributes table.");
            Utils.handleFailure(out, "no permission to modify Attributes table",
                                methodName, aaa);
            return;
        }
        Map outputMap = new HashMap();
        String saveAttrName = request.getParameter("saveAttrName");
        if (saveAttrName != null) {
            saveAttrName = saveAttrName.trim();
        }
        String attributeEditName = request.getParameter("attributeEditName").trim();
        String attributeEditDescr =
            request.getParameter("attributeEditDescription").trim();
        String attributeEditType =
            request.getParameter("attributeTypes").trim();
        try {
            if (opName.equals("add")) {
                methodName = "AttributeAdd";
                this.addAttribute(attributeEditName, attributeEditDescr,
                                  attributeEditType);
                outputMap.put("status", "Added attribute: " +
                                         attributeEditName);
            } else if (opName.equals("modify")) {
                methodName = "AttributeModify";
                this.modifyAttribute(saveAttrName, attributeEditName,
                                     attributeEditDescr, attributeEditType);
                if (!saveAttrName.equals(attributeEditName)) {
                    outputMap.put("status", "Changed attribute name from " +
                                       saveAttrName + " to " + attributeEditName);
                } else {
                    outputMap.put("status", "Modified attribute " +
                                            saveAttrName);
                }
            } else if (opName.equals("delete")) {
                methodName = "AttributeDelete";
                this.deleteAttribute(attributeEditName);
                outputMap.put("status", "Deleted attribute: " +
                                         attributeEditName);
            } else {
                methodName = "AttributeList";
                outputMap.put("status", "Attributes management");
            }
        } catch (AAAException e) {
            this.log.error(e.getMessage());
            Utils.handleFailure(out, e.getMessage(), methodName, aaa);
            return;
        }
        // always output latest list
        this.outputAttributes(outputMap);
        outputMap.put("method", methodName);
        outputMap.put("success", Boolean.TRUE);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("/* " + jsonObject + " */");
        aaa.getTransaction().commit();
        this.log.debug("servlet.end");
    }

    public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }

    /**
     * outputAttributes - gets the initial list of attributes.
     *  
     * @param outputMap Map containing JSON data
     */
    public void outputAttributes(Map outputMap) {

        AttributeDAO attributeDAO = new AttributeDAO(Utils.getDbName());
        List<Attribute> attributes = attributeDAO.list();
        ArrayList attributeList = new ArrayList();
        for (Attribute attribute: attributes) {
            ArrayList attributeEntry = new ArrayList();
            attributeEntry.add(attribute.getName());
            attributeEntry.add(attribute.getDescription());
            attributeEntry.add(attribute.getAttrType());
            attributeList.add(attributeEntry);
        }
        outputMap.put("attributeData", attributeList);
    }

    /**
     * addAttribute - add an attribute if it doesn't already exist.
     *  
     * @param String newName name of new attribute
     * @param String newDescription description of new attribute
     * @param String newType type of new attribute
     * @throws AAAException
     */
    public void addAttribute(String newName, String newDescription,
                             String newType)
            throws AAAException {

        AttributeDAO dao = new AttributeDAO(Utils.getDbName());
        Attribute oldAttribute = dao.queryByParam("name", newName);
        if (oldAttribute != null) {
            throw new AAAException("Attribute " + newName +
                                   " already exists");
        }
        Attribute attribute = new Attribute();
        attribute.setName(newName);
        attribute.setDescription(newDescription);
        attribute.setAttrType(newType);
        dao.create(attribute);
    }

    /**
     * modifyAttribute - change an attribute's name, description, and/or type.
     *  
     * @param String oldName old name of attribute
     * @param String newName new name of attribute
     * @param String descr attribute description
     * @param String attrType type of attribute
     * @throws AAAException
     */
    public void modifyAttribute(String oldName, String newName, String descr,
                                String attrType)
           throws AAAException {

        AttributeDAO dao = new AttributeDAO(Utils.getDbName());
        Attribute attribute = dao.queryByParam("name", oldName);
        if (attribute == null) {
            throw new AAAException("Attribute " + oldName +
                                   " does not exist to be modified");
        }
        attribute.setName(newName);
        attribute.setDescription(descr);
        attribute.setAttrType(attrType);
        dao.update(attribute);
    }

    /**
     * deleteAttribute - delete an attribute, but only if no users
     *     currently belong to it
     *  
     * @param String attributeName name of attribute to delete
     * @throws AAAException
     */
    public void deleteAttribute(String attributeName)
           throws AAAException {

        boolean existingUsers = false;
        boolean existingAuthorizations = false;
        AttributeDAO dao = new AttributeDAO(Utils.getDbName());
        UserAttributeDAO userAttributeDAO =
            new UserAttributeDAO(Utils.getDbName());
        AuthorizationDAO authDAO = new AuthorizationDAO(Utils.getDbName());
        Attribute attribute = dao.queryByParam("name", attributeName);
        if (attribute == null) {
            throw new AAAException("Attribute " + attributeName +
                                   " does not exist to be deleted");
        }
        List<User> users = userAttributeDAO.getUsersByAttribute(attributeName);
        StringBuilder sb = new StringBuilder();
        if (users.size() != 0) {
            sb.append(attributeName + " has existing users: ");
            for (User user: users) {
                sb.append(user.getLogin() + " ");
            }
            existingUsers = true;
        }
        List<Authorization> auths = authDAO.listAuthByAttr(attributeName);
        if (auths.size() != 0) {
            if (existingUsers) {
                sb.append(".  There are " + auths.size() + " existing authorizations " +
                    "with this attribute.");
            } else {
                sb.append(attributeName + " has " + auths.size() +
                          " associated authorizations.  Cannot delete.");
            }
            existingAuthorizations = true;
        }
        if (existingUsers || existingAuthorizations) {
            throw new AAAException(sb.toString());
        }
        dao.remove(attribute);
    }
}
