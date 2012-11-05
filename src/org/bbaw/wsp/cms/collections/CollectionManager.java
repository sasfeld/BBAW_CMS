package org.bbaw.wsp.cms.collections;

import java.io.File;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.bbaw.wsp.cms.dochandler.DocumentHandler;
import org.bbaw.wsp.cms.dochandler.parser.text.parser.EdocIndexMetadataFetcherTool;
import org.bbaw.wsp.cms.document.MetadataRecord;
import org.bbaw.wsp.cms.document.XQuery;
import org.bbaw.wsp.cms.scheduler.CmsDocOperation;

import de.mpg.mpiwg.berlin.mpdl.exception.ApplicationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

public class CollectionManager {
  private static Logger LOGGER = Logger.getLogger(CollectionManager.class.getName());
  private CollectionReader collectionReader;  // has the collection infos of the configuration files
  private int counter = 0;
  private static CollectionManager confManager;

  public static CollectionManager getInstance() throws ApplicationException {
    if(confManager == null) {
      confManager = new CollectionManager();
      confManager.init();
    }
    return confManager;
  }
  
  private void init() throws ApplicationException {
    collectionReader = CollectionReader.getInstance();
  }
  
  /**
   * Update of all collections (if update parameter in configuration is true)
   */
  public void updateCollections() throws ApplicationException {
    updateCollections(false);
  }
  
  /**
   * Update of all collections
   * @param forceUpdate if true then update is always done (also if update parameter in configuration is false)
   * @throws ApplicationException
   */
  public void updateCollections(boolean forceUpdate) throws ApplicationException {
    ArrayList<Collection> collections = collectionReader.getCollections();
    for (Collection collection : collections) {
      updateCollection(collection, forceUpdate);
    }
  }

  /**
   * Update of the collection with that collectionId (if update parameter in configuration is true)
   * @param collectionId
   * @throws ApplicationException
   */
  public void updateCollection(String collectionId) throws ApplicationException {
    updateCollection(collectionId, false);
  }
  
  /**
   * Update of the collection with that collectionId
   * @param collectionId
   * @param forceUpdate if true then update is always done (also if update parameter in configuration is false)
   * @throws ApplicationException
   */
  public void updateCollection(String collectionId, boolean forceUpdate) throws ApplicationException {
    Collection collection = collectionReader.getCollection(collectionId);
    updateCollection(collection, forceUpdate);
  }

  private void updateCollection(Collection collection, boolean forceUpdate) throws ApplicationException {
    boolean isUpdateNecessary = collection.isUpdateNecessary();
    if (isUpdateNecessary || forceUpdate) {
      String[] collectionDataUrls = collection.getDataUrls();
      String excludesStr = collection.getExcludesStr();
      if (collectionDataUrls != null) {
        List<String> collectionDocumentUrls = new ArrayList<String>();
        for (int i=0; i<collectionDataUrls.length; i++) {
          String url = collectionDataUrls[i];
          if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
            List<String> collectionDocumentUrlsTemp = extractDocumentUrls(url, excludesStr);
            collectionDocumentUrls.addAll(collectionDocumentUrlsTemp);
          } else {
            collectionDocumentUrls.add(url);
          }
        }
        collection.setDocumentUrls(collectionDocumentUrls);
      }
      addDocuments(collection); 
      String configFileName = collection.getConfigFileName();
      File configFile = new File(configFileName);
      setUpdate(configFile, false);
    }
  }
  
  private void addDocuments(Collection collection) throws ApplicationException {
    DocumentHandler docHandler = new DocumentHandler();
    ArrayList<MetadataRecord> mdRecords = getMetadataRecords(collection);
    for (int i=0; i<mdRecords.size(); i++) {
      MetadataRecord mdRecord = mdRecords.get(i);
      String docUrl = mdRecord.getUri();
      String docId = mdRecord.getDocId();
      String collectionId = mdRecord.getCollectionNames();
      counter++;
      Date now = new Date();
      LOGGER.info(counter + ". " + now.toString() + " Collection: " + collectionId + ": Create: " + docId);
      CmsDocOperation docOp = new CmsDocOperation("create", docUrl, null, docId);
      docOp.setMdRecord(mdRecord);
      ArrayList<String> fields = collection.getFields();
      if (fields != null) {
        String[] fieldsArray = new String[fields.size()];
        for (int j=0;j<fields.size();j++) {
          String f = fields.get(j);
          fieldsArray[j] = f;
        }
        docOp.setElementNames(fieldsArray);
      }
      String mainLanguage = collection.getMainLanguage();
      docOp.setMainLanguage(mainLanguage);
      try {
        docHandler.doOperation(docOp);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private ArrayList<MetadataRecord> getMetadataRecords(Collection collection) throws ApplicationException {
    ArrayList<MetadataRecord> mdRecords = null;
    try {
      String collectionId = collection.getId();
      String dataUrlPrefix = collection.getDataUrlPrefix();
      String metadataUrlPrefix = collection.getMetadataUrlPrefix();
      List<String> documentUrls = collection.getDocumentUrls();
      String[] metadataUrls = collection.getMetadataUrls();
      String metadataUrlType = collection.getMetadataUrlType();  // single or many
      Hashtable<String, XQuery> xQueries = collection.getxQueries();
      if (metadataUrls != null) {
        if (metadataUrlType != null && metadataUrlType.equals("single")) {
          mdRecords = new ArrayList<MetadataRecord>();
          for (int i=0; i<metadataUrls.length; i++) {
            String metadataUrl = metadataUrls[i];
            MetadataRecord mdRecord = new MetadataRecord();
            String uri = null;
            String docId = null;
            if (collectionId.equals("edoc")) {
              EdocIndexMetadataFetcherTool.fetchHtmlDirectly(metadataUrl, mdRecord);
              String httpEdocUrl = mdRecord.getRealDocUrl();
              if (httpEdocUrl != null) {             
                String docIdTmp = httpEdocUrl.replaceAll(metadataUrlPrefix, "");
                docId = "/" + collectionId + docIdTmp;
                String fileEdocUrl = "file:" + dataUrlPrefix + docIdTmp;
                uri = fileEdocUrl;
              } else {
                LOGGER.severe("Fetching metadata failed for: " + metadataUrl + " (no url in index.html found)");
              }
            } else {
              // TODO
            }
            if (docId != null && uri != null) {
              mdRecord.setDocId(docId);
              mdRecord.setUri(uri);
              mdRecord.setCollectionNames(collectionId);
              String mainLanguage = collection.getMainLanguage();
              mdRecord.setLanguage(mainLanguage);
              mdRecord.setSchemaName(null);
              mdRecord.setxQueries(xQueries);
              mdRecords.add(mdRecord);
            }
          }
        } else {
          // TODO
        }
      }
      if (metadataUrls == null && documentUrls != null) {
        mdRecords = new ArrayList<MetadataRecord>();
        for (int i=0; i<documentUrls.size(); i++) {
          MetadataRecord mdRecord = new MetadataRecord();
          String docUrl = documentUrls.get(i);
          URL uri = new URL(docUrl);
          String uriPath = uri.getPath();
          String prefix = collection.getDataUrlPrefix();
          if (prefix == null)
            prefix = "/exist/rest/db";
          if(uriPath.startsWith(prefix)){
            uriPath = uriPath.substring(prefix.length());
          }
          String docId = "/" + collectionId + uriPath;
          mdRecord.setDocId(docId);
          mdRecord.setUri(docUrl);
          mdRecord.setCollectionNames(collectionId);
          mdRecord.setxQueries(xQueries);
          mdRecords.add(mdRecord);
        }
      }
    } catch (MalformedURLException e) {
      throw new ApplicationException(e);
    }
    return mdRecords;
  }
  
  private void setUpdate(File configFile, boolean update) throws ApplicationException {
    try {
      // flag im Konfigurations-File auf false setzen durch Serialisierung in das File
      DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = docFactory.newDocumentBuilder();
      Document configFileDocument = builder.parse(configFile);
      NodeList updateNodeList = configFileDocument.getElementsByTagName("update");
      Node n = updateNodeList.item(0);
      if (update)
        n.setTextContent("true");
      else 
        n.setTextContent("false");
      FileOutputStream os = new FileOutputStream(configFile);
      XMLSerializer ser = new XMLSerializer(os, null);
      ser.serialize(configFileDocument);  //  Vorsicht: wenn es auf true ist: es wird alles neu indexiert
    } catch (Exception e) {
      throw new ApplicationException(e);
    }
  }
  
  private List<String> extractDocumentUrls(String collectionDataUrl, String excludesStr) {
    List<String> documentUrls = null;
    if (! collectionDataUrl.equals("")){
      PathExtractor extractor = new PathExtractor();
      documentUrls = extractor.initExtractor(collectionDataUrl, excludesStr);
    }
    return documentUrls;
  }

}
