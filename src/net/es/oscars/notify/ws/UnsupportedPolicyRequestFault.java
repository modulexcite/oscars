
/**
 * UnsupportedPolicyRequestFault.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.4.1  Built on : Aug 13, 2008 (05:03:35 LKT)
 */

package net.es.oscars.notify.ws;

public class UnsupportedPolicyRequestFault extends java.lang.Exception{
    
    private org.oasis_open.docs.wsn.b_2.UnsupportedPolicyRequestFault faultMessage;
    
    public UnsupportedPolicyRequestFault() {
        super("UnsupportedPolicyRequestFault");
    }
           
    public UnsupportedPolicyRequestFault(java.lang.String s) {
       super(s);
    }
    
    public UnsupportedPolicyRequestFault(java.lang.String s, java.lang.Throwable ex) {
      super(s, ex);
    }
    
    public void setFaultMessage(org.oasis_open.docs.wsn.b_2.UnsupportedPolicyRequestFault msg){
       faultMessage = msg;
    }
    
    public org.oasis_open.docs.wsn.b_2.UnsupportedPolicyRequestFault getFaultMessage(){
       return faultMessage;
    }
}
    