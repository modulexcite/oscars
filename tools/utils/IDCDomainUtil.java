import java.util.*;
import java.io.*;

import net.es.oscars.bss.topology.*;
import net.es.oscars.database.*;

import org.apache.log4j.*;
import org.hibernate.*;

/**
 * IDCDomainUtil is a command-line client for adding domains to the database
 *
 * @author Andrew Lake (alake@internet2.edu)
 */
public class IDCDomainUtil extends IDCCmdUtil{
    
    public IDCDomainUtil(){
        this.log = Logger.getLogger(this.getClass());
        this.dbname = "bss";
    }
    
    /**
     * Main logic that adds organization to the database
     *
     */
    public void addDomain(){
        Scanner in = new Scanner(System.in);

        /* Init database */
        Initializer initializer = new Initializer();
        ArrayList<String> dbnames = new ArrayList<String>();
        dbnames.add(this.dbname);
        initializer.initDatabase(dbnames);
        Session bss =
            HibernateUtil.getSessionFactory(this.dbname).getCurrentSession();
        bss.beginTransaction();
        Domain domain = new Domain();
        domain.setTopologyIdent(readInput(in, "Topology Identifier (i.e. mydomain.net)", "", true));
        domain.setUrl(readInput(in, "IDC URL", "", true));
        domain.setName(readInput(in, "Descriptive Name (for display purposes)", "", true));
        domain.setAbbrev(readInput(in, "Abbreviated Name (for display purposes)", "", true));
        System.out.print("Is this your IDC's local domain? [y/n] ");
        String ans = in.next();
        if(ans.toLowerCase().startsWith("y")){
            domain.setLocal(true);
        }else{
            domain.setLocal(false);
        }
        bss.save(domain);
        bss.getTransaction().commit();
        
        System.out.println("New domain '" + domain.getTopologyIdent() + "' added.");
    }
    
    /**
     * Main logic that removes domains from the database
     *
     */
    public void removeDomain(){
        Scanner in = new Scanner(System.in);

        /* Init database */
        Initializer initializer = new Initializer();
        ArrayList<String> dbnames = new ArrayList<String>();
        dbnames.add(this.dbname);
        initializer.initDatabase(dbnames);
        Session bss =
            HibernateUtil.getSessionFactory(this.dbname).getCurrentSession();
        bss.beginTransaction();
        Domain domain = this.selectDomain(in, "domain to delete");
        System.out.print("Are you sure you want to delete '" + 
                            domain.getTopologyIdent() + "'? [y/n] ");
        String ans = in.next();
        
        if(ans.toLowerCase().startsWith("y")){
            domain.setPaths(null);
            bss.delete(domain);
            System.out.println("Domain '" + domain.getTopologyIdent() + "' deleted.");
        }else{
            System.out.println("Operation cancelled. No domain deleted.");
        }
       
        bss.getTransaction().commit();
    }
    
    /**
     * Prints the current list of domains in the database and allows the
     * user to choose one
     *
     * @param in the Scanner to use for accepting input
     * @return the selected Domain
     */
    protected Domain selectDomain(Scanner in, String label){
        DomainDAO domainDAO = new DomainDAO(this.dbname);
        List<Domain> domains = domainDAO.list();
        int i = 1;
        
        System.out.println();
        for(Domain domain : domains){
            System.out.println(i + ". " + domain.getTopologyIdent());
            i++;
        }
        
        System.out.print("Select the " + label + " (by number): ");
        int n = in.nextInt();
        in.nextLine();
        
        if(n <= 0 || n > domains.size()){
            System.err.println("Invalid domain number '" +n + "' entered");
            System.exit(0);
        }
        
        return domains.get(n-1);
    }
    
    /**
     * Main logic that adds domain service to the database
     *
     */
    public void addDomainService(){
        Scanner in = new Scanner(System.in);

        /* Init database */
        Initializer initializer = new Initializer();
        ArrayList<String> dbnames = new ArrayList<String>();
        dbnames.add(this.dbname);
        initializer.initDatabase(dbnames);
        Session bss =
            HibernateUtil.getSessionFactory(this.dbname).getCurrentSession();
        bss.beginTransaction();
        DomainService ds = new DomainService();
        ds.setDomain(this.selectDomain(in, "service's domain"));
        String type = "";
        System.out.println("Choose service type:");
        System.out.println("    1. NotificationBroker");
        System.out.println("    2. Other...");
        String choice = readInput(in, "Enter Choice", "", true);
        if("1".equals(choice)){
            type = "NB";
        }else if("2".equals(choice)){
            type = readInput(in, "Enter service type", "", true);
        }else{
            System.err.println("Invalid choice!");
            System.exit(0);
        }
        ds.setType(type);
        ds.setUrl(readInput(in, "Service URL", "", true));
        bss.save(ds);
        bss.getTransaction().commit();
        
        System.out.println("New '"+ ds.getType() + "' service at '" + 
                           ds.getUrl() + "' added.");
    }
    
    /**
     * Prints the current list of domain services in the database and allows the
     * user to choose one
     *
     * @param in the Scanner to use for accepting input
     * @return the selected DomainService
     */
    protected DomainService selectDomainService(Scanner in, String label){
        DomainServiceDAO dsDAO = new DomainServiceDAO(this.dbname);
        List<DomainService> services = dsDAO.list();
        int i = 1;
        
        System.out.println();
        for(DomainService service : services){
            System.out.println(i + ". " + service.getDomain().getTopologyIdent() + 
                               ", " + service.getType() + ", " + service.getUrl());
            i++;
        }
        
        System.out.print("Select the " + label + " (by number): ");
        int n = in.nextInt();
        in.nextLine();
        
        if(n <= 0 || n > services.size()){
            System.err.println("Invalid service number '" +n + "' entered");
            System.exit(0);
        }
        
        return services.get(n-1);
    }
    
    /**
     * Main logic that removes a service from the database
     *
     */
    public void removeDomainService(){
        Scanner in = new Scanner(System.in);

        /* Init database */
        Initializer initializer = new Initializer();
        ArrayList<String> dbnames = new ArrayList<String>();
        dbnames.add(this.dbname);
        initializer.initDatabase(dbnames);
        Session bss =
            HibernateUtil.getSessionFactory(this.dbname).getCurrentSession();
        bss.beginTransaction();
        DomainService ds = this.selectDomainService(in, "service to delete");
        System.out.print("Are you sure you want to delete '" + 
                            ds.getUrl() + "'? [y/n] ");
        String ans = in.next();
        
        if(ans.toLowerCase().startsWith("y")){
            bss.delete(ds);
            System.out.println("Service '" + ds.getUrl() + "' deleted.");
        }else{
            System.out.println("Operation cancelled. No service deleted.");
        }
       
        bss.getTransaction().commit();
    }
    
    public static void main(String[] args){
        IDCDomainUtil util = new IDCDomainUtil();
        if(args[0] == null || args[0].equals("add")){
            util.addDomain();
        }else if(args[0].equals("remove")){
            util.removeDomain();
        }else if(args[0].equals("addService")){
            util.addDomainService();
        }else if(args[0].equals("removeService")){
            util.removeDomainService();
        }else{
            System.err.println("Invalid option.");
        }
    }
}