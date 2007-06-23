package net.es.oscars.servlets;

import java.io.*;
import java.util.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.servlet.*;
import javax.servlet.http.*;
import org.hibernate.*;

import net.es.oscars.oscars.TypeConverter;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.bss.ReservationManager;
import net.es.oscars.bss.Reservation;
import net.es.oscars.aaa.UserManager;
import net.es.oscars.bss.BSSException;
import net.es.oscars.aaa.AAAException;
import net.es.oscars.aaa.UserManager.AuthValue;

public class ListReservations extends HttpServlet {

    public void
        doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        List<Reservation> reservations = null;


        UserSession userSession = new UserSession();
        Utils utils = new Utils();

        PrintWriter out = response.getWriter();
        response.setContentType("text/xml");
        String userName = userSession.checkSession(out, request);
        if (userName == null) { return; }
        Session aaa = 
            HibernateUtil.getSessionFactory("aaa").getCurrentSession();
        aaa.beginTransaction();
        Session bss = 
            HibernateUtil.getSessionFactory("bss").getCurrentSession();
        bss.beginTransaction();
        reservations = this.getReservations(out, userName);
        if (reservations == null) {
            /* the status has already been reported in getReservations
            String msg = "Error in getting reservations";
            */
            aaa.getTransaction().rollback();
            bss.getTransaction().rollback();
            return;
        }
        out.println("<xml>");
        out.println("<status>Successfully retrieved reservations</status>");
        utils.tabSection(out, request, response, "ListReservations");
        this.contentSection(out, reservations, userName);
        out.println("</xml>");
        aaa.getTransaction().commit();
        bss.getTransaction().commit();
    }

    public void
        doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        this.doGet(request, response);
    }

    /**
     *  GetReservations returns either all reservations or just the reservations
     *  for this user, depending on his permissions
     *  called by this servlet and AuthenticateUser which brings up  a default
     *  page of reservations listing when a user logs in.
     *  
     * @param out PrintWriter used to report errors on the status line
     * @param rm
     * @param login
     * @return
     */
    public List<Reservation>
        getReservations(PrintWriter out, String login)  {

        ReservationManager rm = new ReservationManager("bss");
        List<Reservation> reservations = null;
        boolean allUsers = false;
        Utils utils = new Utils();

        UserManager mgr = new UserManager("aaa");
        AuthValue authVal = mgr.checkAccess(login, "Reservations", "list");
        if (authVal == AuthValue.DENIED) {
            utils.handleFailure(out, "no permission to list Reservations",  null, null);
            return null;
        }
        if (authVal == AuthValue.ALLUSERS) {allUsers=true;}
        try {
            reservations = rm.list(login, allUsers);
        } catch (BSSException e) {
            utils.handleFailure(out, e.getMessage(),  null, null);
            return null;
        }
        return reservations;
    }

    public void
        contentSection(PrintWriter out, List<Reservation> reservations,
                        String login) {

        TypeConverter tc = new TypeConverter();
        InetAddress inetAddress = null;
        String tag = "";
        String srcHost = null;
        String hostName = null;
        String destHost = null;

        out.println("<content>");
        out.println("<p>Click on a column header to sort by that column. " +
            "Times given are in the time zone of the browser.  Click on " +
            "the Reservation Tag link to view detailed information about " +
            "the reservation.</p>");
        out.println("<p><form method='post' action='' onsubmit=\"" +
            "return submitForm(this, 'ListReservations');\">");
        out.println("<input type='submit' value='Refresh'></input>");
        out.println("</form></p>");

        out.println("<table cellspacing='0' width='90%' class='sortable'>");
        out.println("<thead>");
        out.println("<tr><td>Tag</td><td>Start Time</td><td>End Time</td><td>Status</td>");
        out.println("<td>Origin</td><td>Destination</td>");
        out.println("</tr></thead> <tbody>");
        for (Reservation resv: reservations) {
            tag = tc.getReservationTag(resv);
            out.println("<tr>");
            out.println("<td>");
            out.println("<a href='#' " +
                "onclick=\"return newSection('QueryReservation', 'tag=" +
                         tag + "');\">" +
                tag + "</a>");
            out.println("</td>");
            out.println("<td name='startTime' class='dt'>" +
                         resv.getStartTime() + "</td>");
            out.println("<td name='endTime' class='dt'>" +
                         resv.getEndTime() + "</td>");
            out.println("<td>" + resv.getStatus() + "</td>");
            srcHost = resv.getSrcHost();
            try {
                inetAddress = InetAddress.getByName(srcHost);
                hostName = inetAddress.getHostName();
                out.println("<td>" + hostName + "</td>");
            } catch (UnknownHostException e) {
                out.println("<td>" + srcHost + "</td>");
            }
            destHost = resv.getDestHost();
            try {
                inetAddress = InetAddress.getByName(destHost);
                hostName = inetAddress.getHostName();
                out.println("<td>" + hostName + "</td>");
            } catch (UnknownHostException e) {
                out.println("<td>" + destHost + "</td>");
            }
            out.println("</tr>");
        }
        out.println("</tbody></table>");
        out.println("</content>");
    }
}
