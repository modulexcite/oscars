import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.*;

import net.es.oscars.oscars.AAAFaultMessage;
import net.es.oscars.oscars.BSSFaultMessage;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.client.Client;

public class ListReservationsClient extends ExampleClient {
    /**
     * @param args
     *            [0] directory name of the client repository contains
     *            rampart.mar and axis2.xml [1] the default url of the service
     *            endpoint
     */
    public static void main(String[] args) {
        try {
            ListReservationsClient cl = new ListReservationsClient();
            cl.list(args, true);
        } catch (AAAFaultMessage e1) {
            System.out
                    .println("AAAFaultMessage from listReservations");
            System.out.println(e1.getFaultMessage().getMsg());
        } catch (java.rmi.RemoteException e1) {
            System.out
                    .println("RemoteException returned from listReservations");
            System.out.println(e1.getMessage());
        } catch (Exception e1) {
            System.out
                    .println("OSCARSStub threw exception in listReservations");
            System.out.println(e1.getMessage());
            e1.printStackTrace();
        }
    }

    public ListReply list(String[] args, boolean isInteractive)
            throws AAAFaultMessage, BSSFaultMessage,
            java.rmi.RemoteException, Exception {

        super.init(args, isInteractive);

        // make the call to the server
        ListReply response = this.getClient().listReservations();
        this.outputResponse(response);
        return response;
    }

    public void outputResponse(ListReply response) {
        ResDetails[] resList;
        if ((response != null) && (resList = response.getResDetails()) != null) {
            for (int i = 0; i < resList.length; i++) {
                System.out.println("GRI: " + resList[i].getGlobalReservationId());
                System.out.println("Login: " + resList[i].getLogin());
                System.out.println("Status: "
                        + resList[i].getStatus().toString());
                PathInfo pathInfo = resList[i].getPathInfo();
                if (pathInfo == null) {
                    System.err.println("No path for this reservation. ");
                    continue;
                }
                Layer3Info layer3Info = pathInfo.getLayer3Info();
                if (layer3Info != null) {
                    System.out.println("Source host: " +
                            layer3Info.getSrcHost());
                    System.out.println("Destination host: " +
                            layer3Info.getDestHost());
                }
                System.out.println(" ");
            }
        } else {
            System.out.println("no reservations were found");
        }
    }
}
