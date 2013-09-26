package net.es.oscars.utils.task.sched;

import net.es.oscars.utils.task.Task;
import net.es.oscars.utils.task.TaskException;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Date;

public class WFTicker implements Job {
    private Logger log = Logger.getLogger(WFTicker.class);

    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        Workflow wf = net.es.oscars.utils.task.sched.Workflow.getInstance();
        long now = new Date().getTime();
        Long sec = now / 1000;
        if (sec % 5 == 0) {
            log.debug("WFTicker alive");
        }

        try {
            Task task = wf.nextRunnable(now);
            if (task != null) {
                task.onRun();
                wf.finishRunning(task);
            }

        } catch (TaskException ex) {
            log.error(ex);
        }

    }

}
