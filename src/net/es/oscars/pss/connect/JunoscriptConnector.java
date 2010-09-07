package net.es.oscars.pss.connect;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.common.PSSConnectorConfigBean;

/**
 * a connector for Juniper routers using JunoScript
 * 
 * Make sure to create a new instance of this class for each 
 * router.
 * 
 * @author haniotak, dwrobertson
 *
 */
public class JunoscriptConnector {
    private Logger log = Logger.getLogger(JunoscriptConnector.class);
    
    private InputStream in   = null;
    private OutputStream out = null;
    private Session session = null;
    private Channel channel = null;
    private String router = null;
    
    private PSSConnectorConfigBean config = null;
    
    public JunoscriptConnector(PSSConnectorConfigBean config, String router) {
        this.config = config;
        this.router = router;
    }

    /**
     * Shut down gracefully.
     */
    private void shutdown() throws IOException {
        if (this.channel != null) {
            this.channel.disconnect();
        }
        if (this.session != null) {
            this.session.disconnect();
        }
        if (this.in != null) {
            this.in.close();
        } 
        if (this.out != null) {
            this.out.close();
        }
    }

    /**
     *  @throws IOException
     *  @throws PSSException
     */
    private void connect() throws IOException, PSSException {

        this.log.info("connect.start");
        if (config == null) {
            throw new PSSException("no config set for ssh to "+router);
        } else if (config.getLogin() == null) {
            throw new PSSException("login null for ssh to "+router);
        } else if (config.getKeystore() == null) {
            throw new PSSException("keystore null for ssh to "+router);
        } else if (config.getPassphrase() == null) {
            throw new PSSException("passphrase null for ssh to "+router);
        } else if (router == null) {
            throw new PSSException("null router");
        }
        
        
        JSch jsch = new JSch();
        try {
            jsch.addIdentity(config.getKeystore(), config.getPassphrase());
            this.session = jsch.getSession(config.getLogin(), router, 22);
            
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            this.session.setConfig(config);
            this.session.connect();

            this.channel = this.session.openChannel("exec");
            this.in = this.channel.getInputStream();
            this.out = this.channel.getOutputStream();
            ((ChannelExec) this.channel).setCommand("junoscript");
            this.channel.connect();
            this.log.info("connect.finish");
        } catch (JSchException ex) {
            throw new PSSException(ex.getMessage());
        }
    }



    /**
     * Sends the XML command to the server.
     * @param doc XML document with Junoscript commands
     * @throws IOException
     * @throws JDOMException
     * @throws PSSException
     */
    public Document sendCommand(Document doc)
            throws IOException, JDOMException, PSSException {
        log.info("sendCommand.start for "+router);
        this.connect();

        XMLOutputter outputter = new XMLOutputter();
        Format format = outputter.getFormat();
        format.setLineSeparator("\n");
        format.setEncoding("US-ASCII");
        outputter.setFormat(format);
        // log, and then send to router
        if (config.isLogRequest()) {
            String logOutput = outputter.outputString(doc);
            this.log.info("\nCOMMAND\n\n" + logOutput);
        }
        
        this.log.debug("sending command...");
        // send command
        outputter.output(doc, this.out);
        
        this.log.info("waiting for response...");
        
        ByteArrayOutputStream buff  = new ByteArrayOutputStream();
        Document response = null;
        SAXBuilder b = new SAXBuilder();
        doc = b.build(this.in);
        if (doc == null) {
            throw new PSSException("Router "+router+" did not return a response");
        }
        outputter.output(response, buff);
        
        // for logging purposes only
        if (config.isLogResponse()) {
            this.log.info("\nRESPONSE:\n\n"+buff.toString());
        }
        this.log.info("response received");
        this.log.info("sendCommand.end for "+router);
        this.shutdown();
        return response;
    }


}
