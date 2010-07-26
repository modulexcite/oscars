package net.es.oscars.pss.layer3.junos;

import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.topology.Path;
import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.common.PSSConfigProvider;
import net.es.oscars.pss.common.PSSHandler;
import net.es.oscars.pss.common.PSSDirection;
import net.es.oscars.pss.common.PSSHandlerConfigBean;
import net.es.oscars.pss.common.PathUtils;

public class Layer3_Junos implements PSSHandler {
    
    public void setup(Reservation resv, PSSDirection direction) throws PSSException {
        Path localPath = PathUtils.getLocalPath(resv);
        PSSConfigProvider pc = PSSConfigProvider.getInstance();

        
        System.out.println("starting setup for: "+resv.getGlobalReservationId());
        

    }

    public void teardown(Reservation resv, PSSDirection direction) throws PSSException {
        System.out.println("starting teardown for: "+resv.getGlobalReservationId());
    }




}
