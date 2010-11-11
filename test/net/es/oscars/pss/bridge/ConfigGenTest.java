package net.es.oscars.pss.bridge;

import net.es.oscars.bss.BSSException;
import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.bridge.alcatel.sr.BridgeAlcatelSRConfigGen;
import net.es.oscars.pss.bridge.brocade.xmr.BridgeBrocadeXMRConfigGen;
import net.es.oscars.pss.bridge.cisco.nexus.BridgeCiscoNexusConfigGen;
import net.es.oscars.pss.bridge.junos.ex.BridgeJunosEXConfigGen;
import net.es.oscars.pss.bridge.junos.mx.BridgeJunosMXConfigGen;

import org.testng.annotations.Test;

@Test(groups={ "pss.bridge" })
public class ConfigGenTest {

    @Test
    public void testL2Setup() throws BSSException, PSSException {
        String out = "";
        
        BridgeJunosEXConfigGen ex = BridgeJunosEXConfigGen.getInstance();
        ex.setTemplateDir("conf/pss");
        BridgeJunosMXConfigGen mx = BridgeJunosMXConfigGen.getInstance();
        mx.setTemplateDir("conf/pss");
        BridgeCiscoNexusConfigGen nx = BridgeCiscoNexusConfigGen.getInstance();
        nx.setTemplateDir("conf/pss");
        BridgeBrocadeXMRConfigGen bx = BridgeBrocadeXMRConfigGen.getInstance();
        bx.setTemplateDir("conf/pss");
        BridgeAlcatelSRConfigGen al = BridgeAlcatelSRConfigGen.getInstance();
        al.setTemplateDir("conf/pss");

        
        out += ex.generateL2Setup("xe-0/0/0", "xe-2/0/0", 345, "IDC VLAN 345");
        out += ex.generateL2Teardown("xe-0/0/0", "xe-2/0/0", 345);
        
        out += mx.generateL2Setup("xe-0/0/0", "xe-2/0/0", 345);
        out += mx.generateL2Teardown("xe-0/0/0", "xe-2/0/0", 345);

        out += nx.generateL2Setup("xe-0/0/0", "xe-2/0/0", 345, "IDC VLAN 345");
        out += nx.generateL2Teardown("xe-0/0/0", "xe-2/0/0", 345);
        
        out += bx.generateL2Setup("xe-0/0/0", "xe-2/0/0", 345, "IDC VLAN 345");
        out += bx.generateL2Teardown("xe-0/0/0", "xe-2/0/0", 345);
        
        out += al.generateL2Setup("xe-0/0/0", "xe-2/0/0", 345);
        out += al.generateL2Teardown("xe-0/0/0", "xe-2/0/0", 345);
        System.out.println(out);
       
    }
    @Test
    public void testL2Teardown() throws BSSException, PSSException {
        //
    }


}
