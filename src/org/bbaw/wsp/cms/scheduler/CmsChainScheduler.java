package org.bbaw.wsp.cms.scheduler;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import java.util.logging.Logger;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobListener;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import de.mpg.mpiwg.berlin.mpdl.exception.ApplicationException;


public class CmsChainScheduler {
  private static CmsChainScheduler instance;
  private static String CRUD_JOB = "MPDL_CRUD_JOB";
  private static String CRUD_TRIGGER = "MPDL_CRUD_TRIGGER";
  private static String CRUD_GROUP = "MPDL_CRUD_GROUP";
  private static Logger LOGGER = Logger.getLogger(CmsDocJob.class.getName());
  private org.quartz.Scheduler scheduler;
  private JobListener jobListener;
  private Queue<CmsDocOperation> docOperationQueue = new PriorityQueue<CmsDocOperation>();
  private HashMap<Integer, CmsDocOperation> finishedDocOperations = new HashMap<Integer, CmsDocOperation>();
  private boolean operationInProgress = false;
  private int jobOrderId = 0;
  
  public static CmsChainScheduler getInstance() throws ApplicationException {
    if (instance == null) {
      instance = new CmsChainScheduler();
      instance.init();
    }
    return instance;
  }

  public CmsDocOperation doOperation(CmsDocOperation docOperation) throws ApplicationException {
    jobOrderId++;
    docOperation.setOrderId(jobOrderId);
    queueOperation(docOperation);
    scheduleNextOperation();
    return docOperation;
  }
  
  public void finishOperation(CmsDocOperation docOperation) throws ApplicationException {
    operationInProgress = false;
    Date now = new Date();
    docOperation.setEnd(now);
    docOperation.setStatus("finished");
    int jobId = new Integer(docOperation.getOrderId());
    finishedDocOperations.put(jobId, docOperation);
    log(docOperation);
    // schedule next job if there is one
    scheduleNextOperation();
  }
  
  private void log(CmsDocOperation docOperation) {
    Date startTime = docOperation.getStart();
    Date endTime = docOperation.getEnd();
    long executionTime = -1;
    if (startTime != null && endTime != null)
      executionTime = (endTime.getTime() - startTime.getTime());
    String jobInfo = "Document operation " + docOperation.toString() + ": started at: " + startTime + 
      " and ended at: " + endTime + " (needed time: " + executionTime + " ms)";
    LOGGER.info(jobInfo);
  }
  
  public synchronized void scheduleNextOperation() throws ApplicationException {
    if (isOperationInProgress()) {
      // nothing, operation has to wait
    } else {
      CmsDocOperation docOperation = docOperationQueue.poll();
      if (docOperation == null) {
        // if queue is empty then do nothing (there are no more operations to execute)
      } else {
        Date now = new Date();
        operationInProgress = true;
        docOperation.setStart(now);
        scheduleJob(docOperation, now);
      }
    }
  }
  
  public ArrayList<CmsDocOperation> getDocOperations() throws ApplicationException {
    ArrayList<CmsDocOperation> docOperations = new ArrayList<CmsDocOperation>();
    try {
      // first: all finished jobs
      Collection<CmsDocOperation> finiDocOperations = finishedDocOperations.values();
      docOperations.addAll(finiDocOperations);
      // second: all currently executed jobs
      if (operationInProgress) {
        List<JobExecutionContext> currentJobs = (List<JobExecutionContext>) scheduler.getCurrentlyExecutingJobs();
        Iterator<JobExecutionContext> iter = currentJobs.iterator();
        while (iter.hasNext()) {
          JobExecutionContext jobExecutionContext = iter.next();
          CmsDocOperation docOperation = getDocOperation(jobExecutionContext);
          if (docOperation != null) {
            docOperations.add(docOperation);
          }
        }
      }
      // third: all queued jobs
      Iterator<CmsDocOperation> iter = docOperationQueue.iterator();
      while (iter.hasNext()) {
        CmsDocOperation docOperation = iter.next();
        docOperations.add(docOperation);
      }
    } catch (SchedulerException e) {
      LOGGER.severe(e.getMessage());
      throw new ApplicationException(e);
    }
    return docOperations;
  }
    
  public CmsDocOperation getDocOperation(int jobId) throws ApplicationException {
    CmsDocOperation docOperation = null;
    try {
      // first try: looks into currently executing jobs
      if (operationInProgress) {
        List<JobExecutionContext> currentJobs = (List<JobExecutionContext>) scheduler.getCurrentlyExecutingJobs();
        Iterator<JobExecutionContext> iter = currentJobs.iterator();
        while (iter.hasNext()) {
          JobExecutionContext jobExecutionContext = iter.next();
          docOperation = getDocOperation(jobExecutionContext);
          if (docOperation != null) {
            int dopOpJobId = docOperation.getOrderId();
            if (jobId == dopOpJobId)
              return docOperation;
          }
        }
      }
      // second try: look into finished jobs
      docOperation = finishedDocOperations.get(new Integer(jobId));
      if (docOperation != null) {
        return docOperation;
      }
      // third try: look into queued jobs
      Iterator<CmsDocOperation> iter = docOperationQueue.iterator();
      while (iter.hasNext()) {
        docOperation = iter.next();
        if (docOperation.getOrderId() == jobId)
          return docOperation;
      }
    } catch (SchedulerException e) {
      LOGGER.severe(e.getMessage());
      throw new ApplicationException(e);
    }
    // if not found return null
    return null;
  }
  
  public CmsDocOperation getDocOperation(JobExecutionContext jobExecutionContext) {
    CmsDocOperation docOperation = null;
    if (jobExecutionContext != null) {
      JobDetail job = jobExecutionContext.getJobDetail();
      JobDataMap parameters = job.getJobDataMap();
      docOperation = (CmsDocOperation) parameters.get("operation");
    }
    return docOperation;
  }
  
  private void queueOperation(CmsDocOperation docOperation) {
    int operationsBefore = docOperationQueue.size();
    if (operationsBefore == 0)
     docOperation.setStatus("waiting in operation queue");
    else 
      docOperation.setStatus("waiting in operation queue: " + operationsBefore + " operations heve to be executed before this operation");
    docOperationQueue.offer(docOperation);
  }
  
  private synchronized boolean isOperationInProgress() {
    return operationInProgress;  
  }
  
  private void scheduleJob(CmsDocOperation docOperation, Date fireTime) throws ApplicationException {
    try {
      int jobId = docOperation.getOrderId();
      String jobName = CRUD_JOB + "-id-" + jobId + "-timeId-" + fireTime;
      JobDetail job = new JobDetail(jobName, CRUD_GROUP, CmsDocJob.class);
      JobDataMap parameters = new JobDataMap();
      parameters.put("operation", docOperation);
      job.setJobDataMap(parameters);
      job.addJobListener(jobListener.getName());        
      String triggerName = CRUD_TRIGGER + "-id-" + jobId + "-timeId-" + fireTime;
      Trigger trigger = new SimpleTrigger(triggerName, CRUD_GROUP, fireTime);
      scheduler.scheduleJob(job, trigger);
      String jobInfo = "Schedule document operation: " + docOperation.toString() + ": done at: " + fireTime.toString();
      LOGGER.info(jobInfo);
    } catch (SchedulerException e) {
      LOGGER.severe(e.getMessage());
      throw new ApplicationException(e);
    }
  }
  
  private void init() throws ApplicationException {
    try {
      if (scheduler == null) {
        String quartzPath = getQuartzPath();
        StdSchedulerFactory schedulerFactory = new StdSchedulerFactory(quartzPath);
        scheduler = schedulerFactory.getScheduler();
        jobListener = new CmsChainSchedulerListener();
        scheduler.addJobListener(jobListener);
        scheduler.start();
        LOGGER.info("Started Quartz scheduler factory: " + quartzPath);
      } 
    } catch (SchedulerException e) {
      LOGGER.severe(e.getMessage());
      throw new ApplicationException(e);
    }
  }
  
  public void end() throws ApplicationException {
    try {
      if (scheduler != null) {
        scheduler.shutdown();
      }
      String quartzPath = getQuartzPath();
      LOGGER.info("Ended Quartz scheduler factory: " + quartzPath);
    } catch (SchedulerException e) {
      LOGGER.severe(e.getMessage());
      throw new ApplicationException(e);
    }
  }

  private String getQuartzPath() {
    URL quartzUrl = CmsChainScheduler.class.getResource("quartz.properties");
    String quartzPath = quartzUrl.getPath();
    if (quartzPath.indexOf(".jar!") != -1) {
      int beginIndex = quartzPath.indexOf(".jar!") + 6;
      quartzPath = quartzPath.substring(beginIndex);
    }
    return quartzPath;    
  }
}