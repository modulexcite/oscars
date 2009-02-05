package net.es.oscars.rmi.bss;

import java.io.IOException;
import java.util.HashMap;
import java.rmi.Remote;
import java.rmi.RemoteException;

import net.es.oscars.bss.Reservation;
import net.es.oscars.rmi.bss.xface.*;

public interface BssRmiInterface extends Remote {

     /**
     * Default registry port
     */
    static int registryPort = 1099;
    /**
     * Default registry address
     */
    static String registryAddress = "127.0.0.1";
    /**
     * Default registry name
     */
    static String registryName = "BSSRMIServer";



    public void init() throws RemoteException;


    /**
     * Creates reservation given information from servlet.
     *
     * @param resvRequest - partially filled in reservation with requested params
     * @param userName string with authenticated login name of user
     * @return gri - new global reservation id assigned to reservation
     * @throws IOException
     * @throws RemoteException
     */
    public String
        createReservation(Reservation resvRequest, String userName)
            throws IOException, RemoteException;

    /**
     * Queries reservation given information from servlet.
     *
     * @param request RmiQueryResRequest contains input from component
     * @param userName string with authenticated login name of user
     * @return RmiQueryResReply bean containing reservation
     * @throws IOException
     * @throws RemoteException
     */
    public RmiQueryResReply
        queryReservation(RmiQueryResRequest request, String userName)
            throws IOException, RemoteException;

    /**
     * Lists reservations given criteria from other component.
     *
     * @param request - RmiListResRequest contains input from component
     *
     * @return RmiListResReply list of reservations satisfying criteria
     * @throws IOException
     * @throws RemoteException
     */

    public RmiListResReply
        listReservations(RmiListResRequest request, String userName)
            throws IOException, RemoteException;

    /**
     * Cancels reservation given information from servlet.
     * @param gri String GlobalReservationId of reservation to be canceled
     * @param userName string with authenticated login name of user
     * @throws RemoteException
     */
    public void
        cancelReservation(String gri, String userName)
            throws RemoteException;

    /**
     * Modifies reservation given information from servlet.
     *
     * @param params HashMap<String, Object> - contains input from web request
     * @param userName string with authenticated login name of user
     * @return HashMap<String, Object> - out values to pour into JSON Object.
     * @throws IOException
     * @throws RemoteException
     */
    public HashMap<String, Object>
        modifyReservation(HashMap<String, Object> params, String userName)
            throws IOException, RemoteException;

    /**
     * Sets up a path.  Forwards the request first, and sets up path if reply.
     * If there is an error during local path setup a teardownPath message
     * is issued.  Different from unsafeCreatePath, which is only for
     * local paths.
     *
     * @param request RmiPathRequest containing request parameters
     * @param userName string with authenticated login name of user
     * @return result string with status of path setup for reservation
     * @throws IOException
     * @throws RemoteException
     */
    public String
        createPath(RmiPathRequest request, String userName)
            throws IOException, RemoteException;

    /**
     * Immediately creates reservation circuit given information from servlet.
     * Only for network engineers from local domain.
     *
     * @param request RmiPathRequest containing request parameters
     * @param userName string with authenticated login name of user
     * @return result string with status of path setup for reservation
     * @throws IOException
     * @throws RemoteException
     */
    public String
        unsafeCreatePath(RmiPathRequest request, String userName)
            throws IOException, RemoteException;

    /**
     * Immediately tears down reservation circuit given info from servlet.
     * Only for network engineers from the local domain.
     *
     * @param params HashMap<String, Object> - contains input from web request
     * @param userName string with authenticated login name of user
     * @return HashMap<String, Object> - out values to pour into JSON Object.
     * @throws IOException
     * @throws RemoteException
     */
    public HashMap<String, Object>
        teardownPath(HashMap<String, Object> params, String userName)
            throws IOException, RemoteException;

    /**
     * Forces the immediate status change of a reservation.
     * Only for network engineers from the local domain.
     *
     * @param params HashMap<String, Object> - contains input from web request
     * @param userName string with authenticated login name of user
     * @return HashMap<String, Object> - out values to pour into JSON Object.
     */
    public HashMap<String, Object>
        modifyStatus(HashMap<String, Object> params, String userName)
            throws IOException, RemoteException;
}
