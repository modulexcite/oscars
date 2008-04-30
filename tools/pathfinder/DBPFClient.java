import net.es.oscars.*;
import net.es.oscars.pathfinder.*;
import net.es.oscars.pathfinder.db.*;
import net.es.oscars.pathfinder.db.util.*;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.database.*;
import net.es.oscars.bss.*;
import net.es.oscars.bss.topology.*;

import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlanePathContent;

import org.hibernate.*;
import org.hibernate.cfg.*;

import org.jgrapht.alg.*;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.io.*;
import java.util.*;


public class DBPFClient {
    private static DomainDAO domDAO;

    public static void main(String[] argv) {
        List<String> dbnames = new ArrayList<String>();
        dbnames.add("bss");

        Initializer initializer = new Initializer();
        initializer.initDatabase(dbnames);

        SessionFactory sf = HibernateUtil.getSessionFactory("bss");
        sf.getCurrentSession().beginTransaction();
        Date today = new Date();
        Long now = today.getTime();
        now = now / 1000;
        Long startTime = now;
        Long endTime = now + 600;

        String start;
        String end;
        String usage = "Usage:\ndbPfClient.sh\n";

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        DBPathfinder dbpf = new DBPathfinder("bss");

        ReservationManager rm = new ReservationManager("bss");

        Reservation reservation = new Reservation();
        reservation.setBandwidth(0L);
        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);


        // set up path
        PathInfo pathInfo = new PathInfo();

        // Layer 2 stuff
        Layer2Info layer2Info = new Layer2Info();
        // layer2Info.setSrcEndpoint("urn:ogf:network:es.net:fnal-mr1:TenGigabitEthernet2/3:*");
        // layer2Info.setDestEndpoint("urn:ogf:network:es.net:chi-sl-mr1:TenGigabitEthernet1/2:*");
//        layer2Info.setSrcEndpoint("urn:ogf:network:domain=es.net:node=chi-sl-mr1:port=TenGigabitEthernet4/3:link=*");
//        layer2Info.setDestEndpoint("urn:ogf:network:domain=dcn.internet2.edu:node=chic-vlsr:port=10.100.80.185-106:link=1");

//       layer2Info.setDestEndpoint("urn:ogf:network:domain=dcn.internet2.edu:node=wash-vlsr:port=10.100.80.133-101:link=1");

        layer2Info.setSrcEndpoint("urn:ogf:network:domain=es.net:node=fnal-mr1:port=TenGigabitEthernet4/3:link=*");
        layer2Info.setDestEndpoint("urn:ogf:network:domain=es.net:node=chi-sl-mr1:port=TenGigabitEthernet4/4:link=*");
        layer2Info.setSrcEndpoint("urn:ogf:network:domain=es.net:node=anl-mr1:port=TenGigabitEthernet3/3:link=*");
        layer2Info.setDestEndpoint("urn:ogf:network:domain=es.net:node=atla-cr1:port=xe-5/1/0:link=*");

        VlanTag srcVtag = new VlanTag();
        srcVtag.setString("3140");
        srcVtag.setTagged(true);
        layer2Info.setSrcVtag(srcVtag);
        VlanTag destVtag = new VlanTag();
        destVtag.setString("3140");
        destVtag.setTagged(true);
        layer2Info.setDestVtag(destVtag);

        pathInfo.setLayer2Info(layer2Info);



        CtrlPlanePathContent path = new CtrlPlanePathContent();
        path.setId("userPath");
        ArrayList<String> hops = new ArrayList<String>();
        /*
        hops.add("urn:ogf:network:es.net:fnal-mr1:TenGigabitEthernet2/3:*");
        hops.add("urn:ogf:network:es.net:fnal-mr1:TenGigabitEthernet4/1:TenGigabitEthernet4/1.1816");

        hops.add("urn:ogf:network:es.net:chi-sl-mr1:TenGigabitEthernet7/1:TenGigabitEthernet7/1.1816");
        hops.add("urn:ogf:network:es.net:chi-sl-mr1:TenGigabitEthernet1/2:*");
        */
/*
                urn:ogf:network:domain=es.net:node=bnl-mr1:port=TenGigabitEthernet1/3:link=*
                urn:ogf:network:domain=es.net:node=bnl-mr1:port=TenGigabitEthernet1/1:link=TenGigabitEthernet1/1.101
                urn:ogf:network:domain=es.net:node=aofa-mr1:port=TenGigabitEthernet2/3:link=TenGigabitEthernet2/3.101
                urn:ogf:network:domain=es.net:node=aofa-mr1:port=TenGigabitEthernet1/3:link=*
  */

        for (String hopId : hops) {
            CtrlPlaneHopContent hop = new CtrlPlaneHopContent();
            hop.setLinkIdRef(hopId);
            path.addHop(hop);
        }
        pathInfo.setPath(path);
        pathInfo.setPathType("");


        Path result = null;
        PathInfo piResult = null;
        try {
            result = rm.getPath(reservation, pathInfo);
             //piResult = dbpf.findPath(pathInfo, reservation);
        } catch (Exception ex) {
            ex.printStackTrace(pw);
            System.out.println("Error: "+ex.getMessage());
            System.out.println(sw.toString());
        }

        if (result == null) {
            System.out.println("No path");
        } else if (piResult != null) {
            CtrlPlanePathContent newPath = piResult.getPath();
            CtrlPlaneHopContent[] newHops = newPath.getHop();
            for (CtrlPlaneHopContent newHop : newHops) {
                System.out.println(newHop.getLinkIdRef());
            }
        } else {

            System.out.println("\nFound path:");

            PathElem elem = result.getPathElem();
            while (elem != null) {
                System.out.println(elem.getLink().getFQTI());
                elem = elem.getNextElem();
            }

            System.out.println("\nNew pathInfo:");

            CtrlPlanePathContent newPath = pathInfo.getPath();
            CtrlPlaneHopContent[] newHops = newPath.getHop();
            for (CtrlPlaneHopContent newHop : newHops) {
                System.out.println(newHop.getLinkIdRef());
            }


        }


        /*
        DBGraphAdapter dbga = new DBGraphAdapter("bss");

        List<String> dbnames = new ArrayList<String>();
        dbnames.add("bss");

        Initializer initializer = new Initializer();
        initializer.initDatabase(dbnames);

        SessionFactory sf = HibernateUtil.getSessionFactory("bss");
        sf.getCurrentSession().beginTransaction();

        domDAO = new DomainDAO("bss");

        //        Long bandwidth = 10 000 000 000L;
        // Long bandwidth = 1000000000L; // 1G
        // Long bandwidth = 2000000000L; // 1G
        // Long bandwidth = 5000000000L; // 1G
        // Long bandwidth = 10000000000L; // 10G
        Long bandwidth = 0L;

        System.out.println("Requested bandwidth is: " + bandwidth.toString());

        DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> graph = dbga.dbToGraph(bandwidth);

        start = "urn:ogf:network:es.net:albu-cr1:xe-3/0/0:*";
        end = "urn:ogf:network:es.net:atla-cr1:xe-3/0/0:*";
//        ameslab-rt1:ge-1/1/0 & probably snll-mr1:ge 4/3

        start = "urn:ogf:network:es.net:ameslab-rt1:ge-1/1/0:*";
        end = "urn:ogf:network:es.net:snll-mr1:TenGigabitEthernet4/3:*";

        doPf(start, end, graph);


        */
/*
        start = "urn:ogf:network:es.net:atla-cr1:xe-4/1/0:*";
        end = "urn:ogf:network:es.net:lbl-mr1:TenGigabitEthernet1/1:*";

        doPf(start, end, graph);

        start = "urn:ogf:network:es.net:atla-cr1:xe-4/1/0:*";
        end = "urn:ogf:network:es.net:sdsc-sdn1:TenGigabitEthernet1/1:*";

        doPf(start, end, graph);

        start = "urn:ogf:network:es.net:slac-mr1:TenGigabitEthernet2/1:*";
        end = "urn:ogf:network:es.net:sdsc-sdn1:TenGigabitEthernet1/1:*";

        doPf(start, end, graph);
        */
    }

    private static void doPf(String start, String end,
        DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> graph) {
        start = URNParser.parseTopoIdent(start).get("fqti");
        end = URNParser.parseTopoIdent(end).get("fqti");

        DijkstraShortestPath sp;
        Iterator peIt;

        sp = new DijkstraShortestPath(graph, start, end);

        if ((sp == null) || (sp.getPathEdgeList() == null)) {
            System.out.println("No path!");

            return;
        }

        peIt = sp.getPathEdgeList().iterator();

        while (peIt.hasNext()) {
            DefaultWeightedEdge edge = (DefaultWeightedEdge) peIt.next();
            printEdge(edge.toString(), graph.getEdgeWeight(edge));
        }

        System.out.println("\n\n");
    }

    private static void printEdge(String edge, double edgeWeight) {
        String[] cols = edge.toString().split("\\s\\:\\s");
        String topoId = cols[0].substring(1);
        Hashtable<String, String> parseResults = URNParser.parseTopoIdent(topoId);
        String type = parseResults.get("type");
        String bandwidth = "";
        String compact = parseResults.get("realcompact");
        Long cap;

        if (type.equals("link")) {
            Link link = domDAO.getFullyQualifiedLink(topoId);

            cap = link.getCapacity() / 1000000;

            if (cap >= 1000) {
                cap = cap / 1000;
                bandwidth = cap.toString() + "Gbps";
            } else {
                bandwidth = cap.toString() + "Mbps";
            }
        } else if (type.equals("port")) {
            Port port = domDAO.getFullyQualifiedPort(topoId);
            cap = port.getCapacity() / 1000000;

            if (cap >= 1000) {
                cap = cap / 1000;
                bandwidth = cap.toString() + "Gbps";
            } else {
                bandwidth = cap.toString() + "Mbps";
            }
        }

        if (type.equals("link") || type.equals("port")) {
            System.out.println(compact + "     " + edgeWeight + "    " +
                bandwidth);
        }
    }
}
