package net.es.oscars.bss.topology;

import net.es.oscars.database.HibernateBean;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import org.hibernate.Hibernate;

import java.io.Serializable;

import java.util.*;


/**
 * Node is adapted from a Middlegen class automatically generated
 * from the schema for the bss.nodes table.
 */
public class Node extends HibernateBean implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4151;

    /** persistent field */
    private boolean valid;

    /** persistent field */
    private String topologyIdent;

    /** nullable persistent field */
    private Double latitude;

    /** nullable persistent field */
    private Double longitude;

    /** persistent field */
    private Domain domain;

    private Set ports = new HashSet<Port>();

    private NodeAddress nodeAddress;

    /** default constructor */
    public Node() {
    }

    /** initializing constructor */
    public Node(Domain domDB, boolean init) {
        if (!init) {
            return;
        }
        this.setValid(true);
        this.setTopologyIdent("changeme");
        this.setDomain(domDB);
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
     * @return topologyIdent a string with the name of this node
     */
    public String getTopologyIdent() {
        return this.topologyIdent;
    }

    /**
     * @param topologyIdent a string with the name of this node
     */
    public void setTopologyIdent(String topologyIdent) {
        this.topologyIdent = topologyIdent;
    }

    /**
     * @return latitude latitude in degrees where node is located
     */
    public Double getLatitude() {
        return this.latitude;
    }

    /**
     * @param latitude latitude in degrees where node is located
     */
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    /**
     * @return longitude longitude in degrees where node is located
     */
    public Double getLongitude() {
        return this.longitude;
    }

    /**
     * @param longitude longitude in degrees where node is located
     */
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    /**
     * @return domain a Domain instance (uses association)
     */
    public Domain getDomain() {
        return this.domain;
    }

    /**
     * @param domain a Domain instance (uses association)
     */
    public void setDomain(Domain domain) {
        this.domain = domain;
    }

    public void setPorts(Set ports) {
        this.ports = ports;
    }

    public Set getPorts() {
        return this.ports;
    }

    public boolean addPort(Port port) {
        boolean added = this.ports.add(port);

        if (added) {
            port.setNode(this);
        }

        return added;
    }
    public Port getPortByTopoId(String portTopologyId) {
        if (this.ports == null) {
            return null;
        }
        Iterator portIt = this.ports.iterator();
        while (portIt.hasNext()) {
            Port port = (Port) portIt.next();
            if (port.getTopologyIdent().equals(portTopologyId)) {
                return port;
            }
        }
        return null;
    }

    public void removePort(Port port) {
        this.ports.remove(port);
    }

    public void setNodeAddress(NodeAddress nodeAddress) {
        this.nodeAddress = nodeAddress;
    }

    public NodeAddress getNodeAddress() {
        return this.nodeAddress;
    }

    /**
     * Constructs the fully qualified topology identifier
     * @return the topology identifier
     */
    public String getFQTI() {
        String parentFqti = this.getDomain().getFQTI();
        String topoId = TopologyUtil.getLSTI(this.getTopologyIdent(), "Node");
        return (parentFqti + ":node=" + topoId);
    }


    public boolean equalsTopoId(Node node) {
        String thisFQTI = this.getFQTI();
        String thatFQTI = node.getFQTI();
        return thisFQTI.equals(thatFQTI);
    }
    
    /**
     * Only copies topology identifier information. Useful for detaching
     * object from hibernate and passing to other processes that only care
     * about IDs.
     *
     * @return a copy of this node
     **/
    public Node topoCopy(){
        Node nodeCopy = new Node();
        Domain domainCopy = null;
        
        nodeCopy.setTopologyIdent(this.topologyIdent);
        
        if(this.domain != null){
            domainCopy = this.domain.copy();
        }
        nodeCopy.setDomain(domainCopy);
        
        return nodeCopy;
    }

    /**
     * Checks if the given identifier refers to this element or one of its
     * children.
     * @return The element corresponding to the passed id or null if not found.
     */
    public Object lookupElement(String id) {
        if (this.getFQTI().equals(id)) {
            return this;
        }

        Hashtable<String, String> parseResults = URNParser.parseTopoIdent(id);
	String portFQID = parseResults.get("portFQID");
	if (portFQID == null) {
            return null;
	}

        Iterator portIt = this.ports.iterator();
        while (portIt.hasNext()) {
            Port p = (Port) portIt.next();
            if (portFQID.equals(p.getFQTI()))
                return p.lookupElement(id);
        }

        return null;
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

        Node castOther = (Node) o;

        // if both of these have been saved to the database
        if ((this.getId() != null) && (castOther.getId() != null)) {
            return new EqualsBuilder().append(this.getId(), castOther.getId())
                                      .isEquals();
        } else {
            // used in updating the topology database; only this field
            // are important in determining equality
            /*
            return new EqualsBuilder().append(this.getTopologyIdent(),
                castOther.getTopologyIdent()).isEquals();
                */
            return new EqualsBuilder().append(this.getTopologyIdent(),
                    castOther.getTopologyIdent())
                                          .append(this.getDomain(),
                    castOther.getDomain()).isEquals();
        }
    }

    public String toString() {
        return new ToStringBuilder(this).append("id", getId()).toString();
    }
}
