package net.es.oscars.bss.topology;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URNParser {
      /**
     * This method parses a topology identifier and returns useful information
     * in a hashtable. The hash keys are as follows:
     * type: one of "domain", "node", "port", "link", "ipv4address", "ipv6address", "unknown"
     *
     * domainId: the domain id component (if it exists)
     * nodeId: the node id component (if it exists)
     * portId: the port id component (if it exists)
     * linkId: the link id component (if it exists)
     *
     * fqti: the fully qualified topology identifier (if applicable)
     *
     * @param topoIdent the topology identifier to parse
     * @return a Hashtable with the parse results
     */
    public static Hashtable<String, String> parseTopoIdent(String topoIdent) {


        topoIdent = topoIdent.trim();

//    	System.out.println("looking at: ["+topoIdent+"]");


        Hashtable<String, String> regexps = new Hashtable<String, String>();
        regexps.put("domainFull", "^urn:ogf:network:domain=([^:]+)$");
        regexps.put("domain", "^urn:ogf:network:([^:=]+)$");

        regexps.put("nodeFull", "^urn:ogf:network:domain=([^:]+):node=([^:]+)$");
        regexps.put("node", "^urn:ogf:network:([^:=]+):([^:=]+)$");

        regexps.put("portFull", "^urn:ogf:network:domain=([^:]+):node=([^:]+):port=([^:]+)$");
        regexps.put("port", "^urn:ogf:network:([^:=]+):([^:=]+):([^:=]+)$");

        regexps.put("linkFull", "^urn:ogf:network:domain=([^:]+):node=([^:]+):port=([^:]+):link=([^:]+)$");
        regexps.put("link", "^urn:ogf:network:([^:=]+):([^:=]+):([^:=]+):([^:=]+)$");

        String domainId = "";
        String nodeId = "";
        String portId = "";
        String linkId = "";

        String matched = "";

        Matcher matcher = null;


        for (String key: regexps.keySet()) {
            Pattern p = Pattern.compile(regexps.get(key));
            matcher = p.matcher(topoIdent);
            if (matcher.matches()) {
                if (key.equals("domain") || key.equals("domainFull")) {
                    matched = "domain";
                    domainId = matcher.group(1);
                } else if (key.equals("node") || key.equals("nodeFull") ) {
                    matched = "node";
                    domainId = matcher.group(1);
                    nodeId = matcher.group(2);
                } else if (key.equals("port") || key.equals("portFull") ) {
                    matched = "port";
                    domainId = matcher.group(1);
                    nodeId = matcher.group(2);
                    portId = matcher.group(3);
                } else if (key.equals("link") || key.equals("linkFull") ) {
                    matched = "link";
                    domainId = matcher.group(1);
                    nodeId = matcher.group(2);
                    portId = matcher.group(3);
                    linkId = matcher.group(4);
                }
            }
        }

//    	TODO: make a class for the results?
        Hashtable<String, String> result = new Hashtable<String, String>();

        if (topoIdent == null || topoIdent.equals("")) {
            result.put("type", "empty");
            return result;
        }

        String compactForm = null;
        String realCompactForm = null;
        String fqti = null;
        String addressType = "";
        
        if(matched == null){
            try {
                InetAddress[] addrs = InetAddress.getAllByName(topoIdent);
                 System.out.print("[Success]:");
                 for (int i =0; i < addrs.length;i++){
                     addressType = addrs[i].getClass().getName();
                 }
    
                 if (addressType.equals("java.net.Inet6Address")) {
                     addressType = "ipv6address";
                 } else if (addressType.equals("java.net.Inet4Address")) {
                     addressType = "ipv4address";
                 } else {
                     addressType = "unknown";
                 }
                 result.put("type", addressType);
                 matched = "address";
             } catch(UnknownHostException e){
                 if (matched == null) {
                    result.put("type", "unknown");
                    return result;
                 }
             }
        }else if (matched.equals("domain")) {
            fqti = "urn:ogf:network:domain="+domainId;
            compactForm = "urn:ogf:network:"+domainId;
            realCompactForm = domainId;
            result.put("realcompact", realCompactForm);
            result.put("compact", compactForm);
            result.put("type", "domain");
            result.put("fqti", fqti);
            result.put("domainId", domainId);
        } else if (matched.equals("node")) {
            fqti = "urn:ogf:network:domain="+domainId+":node="+nodeId;
            compactForm = "urn:ogf:network:"+domainId+":"+nodeId;
            realCompactForm = domainId+":"+nodeId;
            result.put("realcompact", realCompactForm);
            result.put("compact", compactForm);
            result.put("type", "node");
            result.put("fqti", fqti);
            result.put("domainId", domainId);
            result.put("nodeId", nodeId);
        } else if (matched.equals("port")) {
            fqti = "urn:ogf:network:domain="+domainId+":node="+nodeId+":port="+portId;
            compactForm = "urn:ogf:network:"+domainId+":"+nodeId+":"+portId;
            realCompactForm = domainId+":"+nodeId+":"+portId;
            result.put("realcompact", realCompactForm);
            result.put("compact", compactForm);
            result.put("type", "port");
            result.put("fqti", fqti);
            result.put("domainId", domainId);
            result.put("nodeId", nodeId);
            result.put("portId", portId);
        } else if (matched.equals("link")) {
            fqti = "urn:ogf:network:domain="+domainId+":node="+nodeId+":port="+portId+":link="+linkId;
            compactForm = "urn:ogf:network:"+domainId+":"+nodeId+":"+portId+":"+linkId;
            realCompactForm = domainId+":"+nodeId+":"+portId+":"+linkId;
            result.put("realcompact", realCompactForm);
            result.put("compact", compactForm);
            result.put("type", "link");
            result.put("fqti", fqti);
            result.put("domainId", domainId);
            result.put("nodeId", nodeId);
            result.put("portId", portId);
            result.put("linkId", linkId);
        }
         return result;
    }
}
