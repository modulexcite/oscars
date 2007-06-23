package net.es.oscars.bss.topology;

import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;

import net.es.oscars.database.HibernateBean;

/**
 * EdgeInfo is adapted from a Middlegen class automatically generated 
 * from the schema for the bss.ipaddrs table.
 */
public class EdgeInfo extends HibernateBean implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4151;

    /** nullable persistent field */
    private Ipaddr ipaddr;

    /** persistent field */
    private String externalIP;

    /** persistent field */
    private Domain domain;

    /** nullable persistent field */
    private String localType;

    /** nullable persistent field */
    private String localValue;

    /** default constructor */
    public EdgeInfo() { }


    /**
     * @return ipaddr edge ipaddr instance 
     */ 
    public Ipaddr getIpaddr() { return this.ipaddr; }

    /**
     * @param ipaddr edge ipaddr instance
     */ 
    public void setIpaddr(Ipaddr ipaddr) { this.ipaddr = ipaddr; }



    /**
     * @return externalIP a string with the external IP address
     */ 
    public String getExternalIP() { return this.externalIP; }

    /**
     * @param externalIP a string with the external IP address
     */ 
    public void setExternalIP(String externalIP) {
        this.externalIP = externalIP;
    }


    /**
     * @return localType a string with the local id's type
     */ 
    public String getLocalType() { return this.localType; }

    /**
     * @param localType a string with the local id's type
     */ 
    public void setLocalType(String localType) {
        this.localType = localType;
    }


    /**
     * @return localValue a string with the local id's value
     */ 
    public String getLocalValue() { return this.localValue; }

    /**
     * @param localValue a string with the local id's value
     */ 
    public void setLocalValue(String localValue) {
        this.localValue = localValue;
    }


    /**
     * @return domain instance (association used)
     */ 
    public Domain getDomain() { return this.domain; }

    /**
     * @param domain a domain instance (association used)
     */ 
    public void setDomain(Domain domain) {
        this.domain = domain;
    }


    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }
}
