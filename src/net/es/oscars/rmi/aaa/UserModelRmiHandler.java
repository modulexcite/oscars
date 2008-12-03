package net.es.oscars.rmi.aaa;


import java.rmi.RemoteException;
import java.util.*;

import org.apache.log4j.*;
import org.hibernate.Session;
import org.hibernate.Hibernate;


import net.es.oscars.aaa.*;
import net.es.oscars.oscars.*;
import net.es.oscars.rmi.model.*;

public class UserModelRmiHandler extends ModelRmiHandlerImpl {
    private OSCARSCore core;
    private Logger log;


    public UserModelRmiHandler() {
        this.log = Logger.getLogger(this.getClass());
        this.core = OSCARSCore.getInstance();
    }

    public HashMap<String, Object> list(HashMap<String, Object> parameters) throws RemoteException {
        this.log.debug("listUsers.start");
        Session aaa = core.getAaaSession();

        HashMap<String, Object> result = new HashMap<String, Object>();

        String listType = (String) parameters.get("listType");
        if (listType == null) {
            listType = "plain";
        }
        List<User> users = new ArrayList<User>();

        try {
            aaa.beginTransaction();
            if (listType.equals("plain")) {
                UserDAO userDAO = new UserDAO(core.getAaaDbName());
                users = userDAO.list();

            } else if (listType.equals("byAttr")) {
                String attributeName = (String) parameters.get("attributeName");
                if (attributeName == null) {
                    aaa.getTransaction().rollback();

                    throw new RemoteException("attributeName not specified");
                }
                UserAttributeDAO dao = new UserAttributeDAO(core.getAaaDbName());
                try {
                    users = dao.getUsersByAttribute(attributeName);
                } catch (AAAException ex) {
                    aaa.getTransaction().rollback();

                    throw new RemoteException(ex.getMessage());
                }
            } else if (listType.equals("single")) {
                String userName = (String) parameters.get("username");
                if (userName == null) {
                    aaa.getTransaction().rollback();

                    throw new RemoteException("username not specified");
                }
                UserManager mgr = core.getUserManager();
                User user = mgr.query(userName);
                users = new ArrayList<User>();
                users.add(user);
            } else {
                throw new RemoteException("unknown listType");
            }
            for (User user : users) {
                Hibernate.initialize(user);
                Hibernate.initialize(user.getInstitution());
                Hibernate.initialize(user.getInstitution().getUsers());
            }
            aaa.getTransaction().commit();
        } catch (Exception ex) {
            this.log.error(ex);
            aaa.getTransaction().rollback();
            throw new RemoteException(ex.getMessage());
        } finally {

        }

        result.put("users", users);

        this.log.debug("listUsers.end");
        return result;
    }



    public HashMap<String, Object> add(HashMap<String, Object> parameters) throws RemoteException {
        this.log.debug("addUser.start");
        HashMap<String, Object> result = new HashMap<String, Object>();
        ArrayList <Integer> addRoles = (ArrayList <Integer>) parameters.get("addRoles");
        User user = (User) parameters.get("user");

        if (user == null) {
            throw new RemoteException("User not set");
        } else if (addRoles == null) {
            throw new RemoteException("Roles not set");
        }



        Session aaa = core.getAaaSession();
        aaa.beginTransaction();

        UserManager mgr = core.getUserManager();
        try {
            mgr.create(user, addRoles);
            aaa.getTransaction().commit();
        } catch (Exception ex) {
            this.log.error(ex);
            aaa.getTransaction().rollback();
            throw new RemoteException(ex.getMessage());
        } finally {

        }

        this.log.debug("addUser.end");
        return result;
    }


    public HashMap<String, Object> modify(HashMap<String, Object> parameters) throws RemoteException {
        this.log.debug("modifyUser.start");
        HashMap<String, Object> result = new HashMap<String, Object>();

        ArrayList <Integer> newRoles = (ArrayList <Integer>) parameters.get("newRoles");
        ArrayList <Integer> curRoles = (ArrayList <Integer>) parameters.get("curRoles");

        User user = (User) parameters.get("user");
        Integer userId = user.getId();
        Boolean setPassword = (Boolean) parameters.get("setPassword");
        if (setPassword == null) {
            setPassword = false;
        }

        if (user == null) {
            throw new RemoteException("User not set");
        } else if (newRoles == null) {
            throw new RemoteException("Roles not set");
        }

        Session aaa = core.getAaaSession();
        try {
            aaa.beginTransaction();
            UserManager mgr = core.getUserManager();
            UserAttributeDAO userAttrDAO = new UserAttributeDAO(core.getAaaDbName());
            mgr.update(user,setPassword);

            for (Integer newRoleItem : newRoles) {
                 int intNewRoleItem = newRoleItem.intValue();
                 if (!curRoles.contains(intNewRoleItem)) {
                     this.addUserAttribute(intNewRoleItem, userId);
                 }
             }
             for (Integer curRoleItem : curRoles){
                int intCurRoleItem  = curRoleItem.intValue();
                if (!newRoles.contains(intCurRoleItem)) {
                     userAttrDAO.remove(userId, intCurRoleItem);
                 }
             }
             aaa.getTransaction().commit();
        } catch (Exception ex) {
            this.log.error(ex);
            aaa.getTransaction().rollback();
            throw new RemoteException(ex.getMessage());
        } finally {

        }


        this.log.debug("modifyUser.end");
        return result;
    }


    public HashMap<String, Object> delete(HashMap<String, Object> parameters) throws RemoteException {
        this.log.debug("deleteUser.start");
        Session aaa = core.getAaaSession();
        aaa.beginTransaction();
        String username = (String) parameters.get("username");
        UserManager mgr = core.getUserManager();
        try {
            mgr.remove(username);
            aaa.getTransaction().commit();
        } catch (Exception ex) {
            this.log.error(ex);
            aaa.getTransaction().rollback();
            throw new RemoteException(ex.getMessage());
        } finally {

        }


        HashMap<String, Object> result = new HashMap<String, Object>();
        this.log.debug("deleteUser.end");
        return result;
    }



    public HashMap<String, Object> find(HashMap<String, Object> parameters) throws RemoteException {
        this.log.info("findUser.start");

        HashMap<String, Object> result = new HashMap<String, Object>();
        User user = null;
        String findBy = (String) parameters.get("findBy");

        Session aaa = core.getAaaSession();
        try {
            aaa.beginTransaction();
            if (findBy == null || findBy.equals("id")) {
                UserDAO userDAO = new UserDAO(core.getAaaDbName());
                Integer id = (Integer) parameters.get("id");
                if (id == null) {
                    aaa.getTransaction().rollback();
                    throw new RemoteException("Unknown id");
                }
                user = userDAO.findById(id, false);

            } else if (findBy.equals("username")) {
                String username = (String) parameters.get("username");
                if (username == null) {
                    aaa.getTransaction().rollback();
                    throw new RemoteException("Unknown id");
                }
                UserManager mgr = core.getUserManager();
                user = mgr.query(username);

            } else {
                aaa.getTransaction().rollback();
                throw new RemoteException("Unknown findBy");
            }
            if (user != null) {
                this.log.info("findUser.found:"+user.getLogin());
                Hibernate.initialize(user);
                Hibernate.initialize(user.getInstitution());
                Hibernate.initialize(user.getInstitution().getUsers());
                result.put("user", user);
            } else {
                throw new RemoteException("User not found");
            }
            aaa.getTransaction().commit();
        } catch (Exception ex) {
            aaa.getTransaction().rollback();
            this.log.error(ex);
            throw new RemoteException(ex.getMessage());
        }


        this.log.info("findUser.end");
        return result;
    }


    private void addUserAttribute(int attrId, int userId){

        UserAttributeDAO userAttrDAO = new UserAttributeDAO(core.getAaaDbName());
        UserAttribute userAttr = new UserAttribute();

        userAttr.setAttributeId(attrId);
        userAttr.setUserId(userId);
        userAttrDAO.create(userAttr);
    }



}