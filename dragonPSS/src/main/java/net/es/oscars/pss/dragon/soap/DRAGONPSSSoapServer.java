package net.es.oscars.pss.dragon.soap;

import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.soap.OSCARSService;
import net.es.oscars.utils.soap.OSCARSSoapService;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;

import net.es.oscars.pss.soap.gen.PSSService;
import net.es.oscars.pss.soap.gen.PSSPortType;
import net.es.oscars.pss.workflow.WorkflowInspectorJob;

@OSCARSService (
        implementor = "net.es.oscars.pss.soap.PSSSoapHandler",
        serviceName = ServiceNames.SVC_PSS,
        config = ConfigDefaults.CONFIG
)

public class DRAGONPSSSoapServer extends OSCARSSoapService<PSSService, PSSPortType> {
    private static DRAGONPSSSoapServer instance;

    public static DRAGONPSSSoapServer getInstance() throws OSCARSServiceException {
        if (instance == null) {
            instance = new DRAGONPSSSoapServer();
        }
        return instance;
    }

    private DRAGONPSSSoapServer() throws OSCARSServiceException {
        super(ServiceNames.SVC_PSS);
        WorkflowInspectorJob wfJob = new WorkflowInspectorJob();
        wfJob.start();

    }
}
