package net.es.oscars.pss.sw.junos;

import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.topology.Path;
import net.es.oscars.pss.common.PSSHandler;
import net.es.oscars.pss.common.PSSDirection;
import net.es.oscars.pss.common.PSSHandlerConfigBean;

public class SW_Junos implements PSSHandler {
    private PSSHandlerConfigBean config;

    public void setup(Reservation resv, Path localPath,
            PSSDirection direction) {
        // TODO Auto-generated method stub
    }

    public void status(Reservation resv, Path localPath,
            PSSDirection direction) {
        // TODO Auto-generated method stub
    }

    public void teardown(Reservation resv, Path localPath,
            PSSDirection direction) {
        // TODO Auto-generated method stub
    }


    public void setConfig(PSSHandlerConfigBean config) {
        this.config = config;
    }

    public PSSHandlerConfigBean getConfig() {
        return config;
    }



}