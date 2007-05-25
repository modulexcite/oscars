package net.es.oscars.servlets;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.hibernate.*;

import org.apache.log4j.*;

import net.es.oscars.database.Initializer;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.aaa.UserManager;
import net.es.oscars.aaa.AAAException;
import net.es.oscars.bss.ReservationManager;
import net.es.oscars.bss.Reservation;


public class AuthenticateUser extends HttpServlet {

    // This is only called once, the first time this servlet is
    // called.
    public void init() throws ServletException {
        Logger log = Logger.getLogger(this.getClass());
        log.info("init.start");
        Initializer initializer = new Initializer();
        List<String> dbnames = new ArrayList<String>();
        dbnames.add("aaa");
        dbnames.add("bss");
        initializer.initDatabase(dbnames);
        log.info("init.end");
    }

    // This is only called once, when the server is brought down.
    public void destroy() {
        Logger log = Logger.getLogger(this.getClass());
        log.info("destroy.start");
        HibernateUtil.closeSessionFactory("aaa");
        HibernateUtil.closeSessionFactory("bss");
        log.info("destroy.end");
    }

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws IOException, ServletException {

        PrintWriter out = null;
        List<Reservation> reservations = null;

        NavigationBar tabs = new NavigationBar();
        Map<String,String> tabParams = new HashMap<String,String>();
        UserSession userSession = new UserSession();
        Utils utils = new Utils();
        UserManager mgr = new UserManager("aaa");
        ListReservations lister = new ListReservations();
        ReservationManager rm = new ReservationManager("bss");

        out = response.getWriter();
        String userName = request.getParameter("userName");
        response.setContentType("text/xml");
        Session aaa = 
            HibernateUtil.getSessionFactory("aaa").getCurrentSession();
        aaa.beginTransaction();
        try {
            String unused =
                mgr.verifyLogin(userName,
                                        request.getParameter("password"));
        } catch (AAAException e) {
            utils.handleFailure(out, e.getMessage(), aaa, null);
            return;
        }
        // used to indicate which tabbed pages can be displayed (some require
        // authorization)
        tabParams.put("Info", "1");
        tabParams.put("CreateReservationForm", "1");;
        tabParams.put("ListReservations", "1");
        tabParams.put("Logout", "1");
        if (mgr.verifyAuthorized(userName, "Users", "manage")) {
            tabParams.put("UserList", "1");
            //tabParams.put("ResourceList", "1");
            //tabParams.put("AuthorizationList", "1");
        }
        else { tabParams.put("UserQuery", "1"); }
        userSession.setCookie("userName", userName, response);
        userSession.setCookie("tabName", "ListReservations", response);

        Session bss = 
            HibernateUtil.getSessionFactory("bss").getCurrentSession();
        bss.beginTransaction();
        // first page that comes up is list of reservations
        reservations = lister.getReservations(out, rm, userName);
        if (reservations == null) {
            String msg = "Error in getting reservations";
            utils.handleFailure(out, msg, aaa, bss);
            return;
        }

        out.println("<xml>");
        // output status
        out.println("<status>" + userName + " signed in.  Use tabs " +
                    "to navigate to different pages.</status>");
        // initialize navigation bar
        tabs.init(out, tabParams);
        // clear information section
        out.println("<info> </info>");
        // output initial page with list of reservations
        lister.contentSection(out, reservations, rm, userName);
        out.println("</xml>");
        aaa.getTransaction().commit();
        bss.getTransaction().commit();
    }

    public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }
}
