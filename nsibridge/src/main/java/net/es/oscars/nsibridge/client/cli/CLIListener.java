package net.es.oscars.nsibridge.client.cli;

import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.requester.ConnectionRequesterPort;

import org.apache.cxf.bus.spring.SpringBusFactory;

import org.apache.cxf.jaxws.JaxWsServerFactoryBean;

import java.util.HashMap;
import java.util.Map;

/**
 * A basic server that listens for asynchronous responses.
 */
public class CLIListener {
    private org.apache.cxf.endpoint.Server server;
    private boolean running = false;
    
    /**
     * Creates a listener with the given configuration
     * @param url the URL where the listener should run
     * @param busConfigFile the bus configuration file for the listener
     * @param cr the handler for requests received by this listener
     * @throws Exception
     */
    public CLIListener(String url, String busConfigFile, ConnectionRequesterPort cr) throws Exception {
        JaxWsServerFactoryBean sf = new JaxWsServerFactoryBean();
        Map props = sf.getProperties();
        if (props == null) {
            props = new HashMap<String, Object>();
        }
        props.put("jaxb.additionalContextClasses",
                        new Class[] {
                            net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.services.point2point.ObjectFactory.class
                        });
        sf.setProperties(props);

        SpringBusFactory factory = new SpringBusFactory();
        factory.createBus(busConfigFile);
        sf.setServiceClass(ConnectionRequesterPort.class);
        sf.setAddress(url);
        sf.setServiceBean(cr);

        server = sf.create();
    }
    
    public void start(){
        server.start();
        this.setRunning(true);
    }
    
    public void stop() {
        server.stop();
        server.destroy();
        this.setRunning(false);
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}