package org.bbaw.wsp.cms.scheduler;

import java.util.Date;

import org.bbaw.wsp.cms.document.MetadataRecord;


public class CmsDocOperation implements Comparable<CmsDocOperation> {
  private int id;
  private Date start;
  private Date end;
  private String name;
  private String status;
  private String errorMessage;
  private String uploadFileName;
  private String srcUrl;   
  private String docIdentifier;
  private String mainLanguage;
  private String[] elementNames; // element names which should be indexed (e.g. "s head")
  private MetadataRecord mdRecord;
  
  public CmsDocOperation(String name, String srcUrl, String uploadFileName, String docIdentifier) {
    this.name = name;
    this.srcUrl = srcUrl;
    this.uploadFileName = uploadFileName;
    this.docIdentifier = docIdentifier;
  }

  public int compareTo(CmsDocOperation op) {
    Integer opOrderId = new Integer(op.id);
    Integer thisOrderId = new Integer(id);
    return thisOrderId.compareTo(opOrderId);
  }
  
  public boolean isFinished() {
    if (status != null && status.equals("finished"))
      return true;
    else 
      return false;
  }
  
  public boolean isError() {
    if (errorMessage != null && errorMessage.length() > 0)
      return true;
    else 
      return false;
  }
  
  public int getOrderId() {
    return id;
  }

  public void setOrderId(int orderId) {
    this.id = orderId;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Date getStart() {
    return start;
  }

  public void setStart(Date start) {
    this.start = start;
  }

  public Date getEnd() {
    return end;
  }

  public void setEnd(Date end) {
    this.end = end;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public String getMainLanguage() {
    return mainLanguage;
  }

  public void setMainLanguage(String mainLanguage) {
    this.mainLanguage = mainLanguage;
  }

  public String getDocIdentifier() {
    return docIdentifier;
  }
  
  public void setDocIdentifier(String docIdentifier) {
    this.docIdentifier = docIdentifier;
  }
  
  public String[] getElementNames() {
    return elementNames;
  }
  
  public void setElementNames(String[] elementNames) {
    this.elementNames = elementNames;
  }
  
  public String getSrcUrl() {
    return srcUrl;
  }

  public void setSrcUrl(String srcUrl) {
    this.srcUrl = srcUrl;
  }

  public String getUploadFileName() {
    return uploadFileName;
  }

  public void setUploadFileName(String uploadFileName) {
    this.uploadFileName = uploadFileName;
  }

  public MetadataRecord getMdRecord() {
    return mdRecord;
  }

  public void setMdRecord(MetadataRecord mdRecord) {
    this.mdRecord = mdRecord;
  }

  public String toString() {
    if (name.equals("delete"))
      return name + "(" + id + ", " + docIdentifier + ")";
    else 
      return name + "(" + id + ", " + uploadFileName + ", " + docIdentifier + ")";
  }
  
}
