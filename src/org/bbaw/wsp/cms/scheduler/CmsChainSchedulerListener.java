package org.bbaw.wsp.cms.scheduler;

import java.util.logging.Logger;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

import de.mpg.mpiwg.berlin.mpdl.exception.ApplicationException;


public class CmsChainSchedulerListener implements JobListener {
  private static Logger LOGGER = Logger.getLogger(CmsDocJob.class.getName());
  
  public String getName() {
    return "MpdlJobChainingListener";
  }

  public void jobToBeExecuted(JobExecutionContext inContext) {
  }

  public void jobExecutionVetoed(JobExecutionContext inContext) {
    String message = "Quartz: JobChainingListener: Job execution was vetoed.";
    LOGGER.fine(message);
  }

  public void jobWasExecuted(JobExecutionContext inContext, JobExecutionException inException) {
    // after finishing his job it tries to schedule the next operation (if there is one in the queue)
    CmsDocOperation docOperation = null;
    try {
      CmsChainScheduler mpdlChainScheduler = CmsChainScheduler.getInstance();
      docOperation = getDocOperation(inContext);
      mpdlChainScheduler.finishOperation(docOperation);
    } catch (ApplicationException e) {
      if (docOperation != null) {
        docOperation.setErrorMessage(e.getMessage());
      }
      LOGGER.severe(e.getMessage());
    }
  }

  private CmsDocOperation getDocOperation(JobExecutionContext context) {
    CmsDocOperation docOperation = null;
    if (context != null) {
      JobDetail job = context.getJobDetail();
      JobDataMap parameters = job.getJobDataMap();
      docOperation = (CmsDocOperation) parameters.get("operation");
    }
    return docOperation;
  }
  

}
