package net.es.oscars.nsibridge.beans.db;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.LifecycleStateEnumType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.ProvisionStateEnumType;

import javax.persistence.*;
import java.lang.Long;
import java.lang.String;
import java.util.Set;

@Entity
public class ConnectionRecord {
    protected Long id;
    protected String connectionId;
    protected String oscarsGri;

    protected Set<DataplaneStatusRecord> dataplaneStatusRecords;
    protected Set<ResvRecord> resvRecords;
    protected Set<OscarsStatusRecord> oscarsStatusRecords;
    protected ProvisionStateEnumType provisionState;
    protected LifecycleStateEnumType lifecycleState;


    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable
    public Set<DataplaneStatusRecord> getDataplaneStatusRecords() {
        return dataplaneStatusRecords;
    }

    public void setDataplaneStatusRecords(Set<DataplaneStatusRecord> dataplaneStatusRecords) {
        this.dataplaneStatusRecords = dataplaneStatusRecords;
    }

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable
    public Set<ResvRecord> getResvRecords() {
        return resvRecords;
    }

    public void setResvRecords(Set<ResvRecord> resvRecords) {
        this.resvRecords = resvRecords;
    }

    public ProvisionStateEnumType getProvisionState() {
        return provisionState;
    }

    public void setProvisionState(ProvisionStateEnumType provisionState) {
        this.provisionState = provisionState;
    }

    public LifecycleStateEnumType getLifecycleState() {
        return lifecycleState;
    }

    public void setLifecycleState(LifecycleStateEnumType lifecycleState) {
        this.lifecycleState = lifecycleState;
    }

    public String getOscarsGri() {
        return oscarsGri;
    }

    public void setOscarsGri(String oscarsGri) {
        this.oscarsGri = oscarsGri;
    }

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable
    public Set<OscarsStatusRecord> getOscarsStatusRecords() {
        return oscarsStatusRecords;
    }

    public void setOscarsStatusRecords(Set<OscarsStatusRecord> oscarsStatusRecords) {
        this.oscarsStatusRecords = oscarsStatusRecords;
    }
}
