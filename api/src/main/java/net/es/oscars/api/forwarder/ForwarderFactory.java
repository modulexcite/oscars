package net.es.oscars.api.forwarder;

import java.net.URL;
import java.util.List;

import org.apache.log4j.Logger;


import net.es.oscars.api.common.OSCARSIDC;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.lookup.soap.gen.Protocol;

public class ForwarderFactory {
    private static final long      serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(ForwarderFactory.class.getName());
    
    public static Forwarder getForwarder (String domainId) throws OSCARSServiceException {

        List<Protocol> protos = OSCARSIDC.getInstance().getLookupClient().lookup("urn:ogf:network:domain=" + domainId);
        if ( (protos == null) || (protos.size() == 0)) {
            throw new OSCARSServiceException ("register request failed","system");
        }

        URL url = null;

        for (Protocol p : protos) {
            if ("http://oscars.es.net/OSCARS/06".equals(p.getType())) {
                // OSCARS 0.6 is the preferred protocol
                try {
                    url =  new URL (p.getLocation());
                    Forwarder forwarder = new Forwarder06 (domainId, url);
                    return forwarder;
                } catch (Exception e) {
                    // Invalid URL. Ignore protocol
                    continue;
                }
            } else if ("http://oscars.es.net/OSCARS".equals(p.getType())) {
                // OSCARS 0.5 is acceptable
                try {
                    url =  new URL (p.getLocation());
                    Forwarder forwarder = new Forwarder05 (domainId, url);
                    return forwarder;
                } catch (Exception e) {
                    // Invalid URL. Ignore protocol
                    continue;
                }
            }
        }

        LOG.error("no location for IDC controlling domain " + domainId);
        throw new OSCARSServiceException ("no location for IDC controlling domain " + domainId);
    }
    
}
