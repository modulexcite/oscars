package net.es.oscars.bss.topology;

import net.es.oscars.database.HibernateBean;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import org.hibernate.Hibernate;

import java.io.Serializable;


/**
 * Ipaddr is adapted from a Middlegen class automatically generated
 * from the schema for the bss.ipaddrs table.
 */
public class Ipaddr extends HibernateBean implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4151;

    /** persistent field */
    private boolean valid;

    /** persistent field */
    private String IP;

    /** persistent field */
    private Link link;

    /** default constructor */
    public Ipaddr() {
    }

    /**
     * @return valid a boolean indicating whether this entry is still valid
     */
    public boolean isValid() {
        return this.valid;
    }

    /**
     * @param valid a boolean indicating whether this entry is still valid
     */
    public void setValid(boolean valid) {
        this.valid = valid;
    }

    /**
     * @return IP a string with the IP address
     */
    public String getIP() {
        return this.IP;
    }

    /**
     * @param IP a string with the IP address
     */
    public void setIP(String IP) {
        this.IP = IP;
    }

    /**
     * @return link link instance (association used)
     */
    public Link getLink() {
        return this.link;
    }

    /**
     * @param link a link instance (association used)
     */
    public void setLink(Link link) {
        this.link = link;
    }

    // need to override superclass because dealing with transient
    // instances as well
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        Class thisClass = Hibernate.getClass(this);

        if ((o == null) || (thisClass != Hibernate.getClass(o))) {
            return false;
        }

        Ipaddr castOther = (Ipaddr) o;

        // if both of these have been saved to the database
        if ((this.getId() != null) && (castOther.getId() != null)) {
            return new EqualsBuilder().append(this.getId(), castOther.getId())
                                      .isEquals();
        } else {
            // used in updating the topology database; only these fields
            // are important in determining equality
            return new EqualsBuilder().append(this.getIP(), castOther.getIP())
                                      .append(this.getLink(), castOther.getLink())
                                      .isEquals();
        }
    }

    /**
     * Copyies only information useful for detaching
     * object from hibernate and passing to other processes.
     *
     * @return a copy of this ipaddr
     **/
    public Ipaddr copy(){
        Ipaddr ipaddrCopy = new Ipaddr();
        ipaddrCopy.setIP(this.IP);
        ipaddrCopy.setValid(this.valid);
        return ipaddrCopy;
    }
    
    public String toString() {
        return new ToStringBuilder(this).append("id", getId()).toString();
    }
}
