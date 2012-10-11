package org.bbaw.wsp.cms.scheduler;

import java.util.Date;

import java.util.logging.Logger;

import org.bbaw.wsp.cms.dochandler.DocumentHandler;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import de.mpg.mpiwg.berlin.mpdl.exception.ApplicationException;


public class CmsDocJob implements Job {
  public static String STATUS_BEGIN = "started";
  private static Logger LOGGER = Logger.getLogger(CmsDocJob.class.getName());
  private JobExecutionContext currentExecutedContext;
  
  public void execute(JobExecutionContext context) throws JobExecutionException {
    this.currentExecutedContext = context;
    CmsDocOperation docOperation = getDocOperation();
    try {
      docOperation.setStatus(STATUS_BEGIN);
      String operationName = docOperation.getName();   
      if (operationName.equals("create")) {
        DocumentHandler docHandler = new DocumentHandler();
        docHandler.doOperation(docOperation);
      } else if (operationName.equals("delete")) {
        DocumentHandler docHandler = new DocumentHandler();
        docHandler.doOperation(docOperation);
      } else if (operationName.equals("importDirectory")) {
        DocumentHandler docHandler = new DocumentHandler();
        docHandler.doOperation(docOperation);
      }
      Date startingTime = docOperation.getStart();
      String jobInfo = "Document operation " + docOperation.toString() + ": started at: " + startingTime;
      LOGGER.info(jobInfo);
      this.currentExecutedContext = null;
    } catch (Exception e) {
      try {
        // Quartz will automatically unschedule all triggers associated with this job so that it does not run again
        CmsChainScheduler mpdlChainScheduler = CmsChainScheduler.getInstance();
        mpdlChainScheduler.finishOperation(docOperation);
        String errorMessage = e.getMessage();
        if (errorMessage == null) {
          Throwable t = e.getCause();
          if (t == null) {
            errorMessage = e.toString();
          } else {
            errorMessage = t.getMessage();
          }
        }
        docOperation.setErrorMessage(errorMessage);
        LOGGER.severe(errorMessage);
        JobExecutionException jobExecutionException = new JobExecutionException(e);
        jobExecutionException.setUnscheduleAllTriggers(true);
        throw jobExecutionException;
      } catch (ApplicationException ex) {
        // nothing
      }
    }
  } 

  private CmsDocOperation getDocOperation() {
    CmsDocOperation docOperation = null;
    if (currentExecutedContext != null) {
      JobDetail job = currentExecutedContext.getJobDetail();
      JobDataMap parameters = job.getJobDataMap();
      docOperation = (CmsDocOperation) parameters.get("operation");
    }
    return docOperation;
  }

}
