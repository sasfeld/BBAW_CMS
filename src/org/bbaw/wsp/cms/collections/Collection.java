package org.bbaw.wsp.cms.collections;

import java.util.ArrayList;
import java.util.List;

public class Collection {
  private String id;
  private String name;
  private String[] dataUrls;  // urls of data (could also be starting url)
  private String dataUrlPrefix;  // prefix of url which is not relevant as id 
  private String[] metadataUrls;  // metadata urls for fetching records
  private String metadataUrlPrefix;  // prefix of metadataUrl which is not relevant as id
  private String metadataUrlType;  // metadata url type e.g. "single" (url delivers single metadata record) or "many" (url delivers many metadata records)
  private String excludesStr; // excludes below dataUrl separated by a blank
  private String mainLanguage;
  private ArrayList<String> fields;
  private List<String> excludeField;
  private List<String> formats;
  private boolean updateNecessary;
  private List<String> documentUrls; // list of document urls (fetched e.g. by an harvester) 
  private String configFileName;  // configuration file name
  
  public Collection() {
    id = "";
    name = "";
    dataUrls = null;
    excludesStr = "";
    mainLanguage = "";
    fields = new ArrayList<String>();
    excludeField = new ArrayList<String>();
    formats = new ArrayList<String>();
    updateNecessary = false;
    documentUrls = new ArrayList<String>();
    configFileName = "";
  }
  
  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setId(String id) {
    this.id = id;
  }
  
  public List<String> getDocumentUrls() {
    return documentUrls;
  }

  public void setDocumentUrls(List<String> documentUrls){
    this.documentUrls = documentUrls;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String[] getDataUrls() {
    return dataUrls;
  }

  public void setDataUrls(String[] dataUrls) {
    this.dataUrls = dataUrls;
  }

  public String getDataUrlPrefix() {
    return dataUrlPrefix;
  }

  public void setDataUrlPrefix(String dataUrlPrefix) {
    this.dataUrlPrefix = dataUrlPrefix;
  }

  public String[] getMetadataUrls() {
    return metadataUrls;
  }

  public void setMetadataUrls(String[] metadataUrls) {
    this.metadataUrls = metadataUrls;
  }

  public String getMetadataUrlPrefix() {
    return metadataUrlPrefix;
  }

  public void setMetadataUrlPrefix(String metadataUrlPrefix) {
    this.metadataUrlPrefix = metadataUrlPrefix;
  }

  public String getMetadataUrlType() {
    return metadataUrlType;
  }

  public void setMetadataUrlType(String metadataUrlType) {
    this.metadataUrlType = metadataUrlType;
  }

  public String getExcludesStr() {
    return excludesStr;
  }

  public void setExcludesStr(String excludesStr) {
    this.excludesStr = excludesStr;
  }

  public List<String> getFormats() {
    return formats;
  }

  public void setFormats(List<String> formats) {
    this.formats = formats;
  }

  public List<String> getExcludeField() {
    return excludeField;
  }

  public void setExcludeField(List<String> excludeField) {
    this.excludeField = excludeField;
  }
  
  public String getMainLanguage() {
    return mainLanguage;
  }

  public void setMainLanguage(String mainLanguage) {
    this.mainLanguage = mainLanguage;
  }

  public ArrayList<String> getFields() {
    return fields;
  }

  public void setFields(ArrayList<String> fields) {
    this.fields = fields;
  }

  public boolean isUpdateNecessary() {
    return updateNecessary;
  }

  public void setUpdateNecessary(boolean updateNecessary) {
    this.updateNecessary = updateNecessary;
  }

  public String getConfigFileName() {
    return configFileName;
  }

  public void setConfigFileName(String configFileName) {
    this.configFileName = configFileName;
  }
}
