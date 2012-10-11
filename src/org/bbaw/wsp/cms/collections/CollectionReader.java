package org.bbaw.wsp.cms.collections;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bbaw.wsp.cms.general.Constants;

import de.mpg.mpiwg.berlin.mpdl.exception.ApplicationException;
import de.mpg.mpiwg.berlin.mpdl.xml.xquery.XQueryEvaluator;

public class CollectionReader {
  private HashMap<String, Collection> collectionContainer;
  private static CollectionReader collectionReader;

  private CollectionReader() throws ApplicationException {
    collectionContainer = new HashMap<String, Collection>();
    readConfFiles();
  }

  public static CollectionReader getInstance() throws ApplicationException {
    if (collectionReader == null)
      collectionReader = new CollectionReader();
    return collectionReader;
  }

  public ArrayList<Collection> getCollections() {
    ArrayList<Collection> collections = null;
    if (collectionContainer != null) {
      collections = new ArrayList<Collection>();
      java.util.Collection<Collection> values = collectionContainer.values();
      for (Collection collection : values) {
        collections.add(collection);
      }
    }
    return collections;
  }

  public Collection getCollection(String collectionId) {
    Collection collection = collectionContainer.get(collectionId);
    return collection;
  }

  private void readConfFiles() throws ApplicationException {
    try {
      // holt alle Konfigurationsdateien aus dem Konfigurationsordner
      PathExtractor pathExtractor = new PathExtractor();
      String confDir = Constants.getInstance().getConfDir();
      List<String> configsFileList = pathExtractor.extractPathLocally(confDir);
      for (String configFileName : configsFileList) {
        File configFile = new File(configFileName);
        XQueryEvaluator xQueryEvaluator = new XQueryEvaluator();
        URL configFileUrl = configFile.toURI().toURL();
        Collection collection = new Collection();
        collection.setConfigFileName(configFileName);
        String update = xQueryEvaluator.evaluateAsString(configFileUrl, "/wsp/collection/update/text()");
        if (update != null) {
          if (update.equals("true"))
            collection.setUpdateNecessary(true);
        }
        String collectionId = xQueryEvaluator.evaluateAsString(configFileUrl, "/wsp/collection/id/text()");
        if(collectionId != null) {
          collection.setId(collectionId);
        }
        String mainLanguage = xQueryEvaluator.evaluateAsString(configFileUrl, "/wsp/collection/mainLanguage/text()");
        if(mainLanguage != null) {
          collection.setMainLanguage(mainLanguage);
        }
        String collectionName = xQueryEvaluator.evaluateAsString(configFileUrl, "/wsp/collection/name/text()");
        if(collectionName != null) {
          collection.setName(collectionName);
        }
        String metadataUrlStr = xQueryEvaluator.evaluateAsStringValueJoined(configFileUrl, "/wsp/collection/metadata/url");
        if(metadataUrlStr != null) {
          String[] metadataUrls = metadataUrlStr.split(" ");
          collection.setMetadataUrls(metadataUrls);
        }
        String metadataUrlPrefix = xQueryEvaluator.evaluateAsString(configFileUrl, "/wsp/collection/metadata/urlPrefix/text()");
        if(metadataUrlPrefix != null) {
          collection.setMetadataUrlPrefix(metadataUrlPrefix);
        }
        String metadataUrlType = xQueryEvaluator.evaluateAsString(configFileUrl, "/wsp/collection/metadata/urlType/text()");
        if(metadataUrlType != null) {
          collection.setMetadataUrlType(metadataUrlType);
        }
        String collectionDataUrlStr = xQueryEvaluator.evaluateAsStringValueJoined(configFileUrl, "/wsp/collection/url/dataUrl", " ");
        if(collectionDataUrlStr != null) {
          String[] collectionDataUrl = collectionDataUrlStr.split(" ");
          collection.setDataUrls(collectionDataUrl);
        }
        String collectionDataUrlPrefix = xQueryEvaluator.evaluateAsString(configFileUrl, "/wsp/collection/url/dataUrlPrefix/text()");
        if(collectionDataUrlPrefix != null) {
          collection.setDataUrlPrefix(collectionDataUrlPrefix);
        }
        String fieldsStr = xQueryEvaluator.evaluateAsStringValueJoined(configFileUrl, "/wsp/collection/fields/field", "###");
        ArrayList<String> fieldsArrayList = new ArrayList<String>();
        if(fieldsStr != null) {
          String[] fields = fieldsStr.split("###");
          for (int i=0; i<fields.length; i++) {
            String field = fields[i].trim();
            if (! field.isEmpty())
              fieldsArrayList.add(field);
          }
        }
        collection.setFields(fieldsArrayList);
        String excludesStr = xQueryEvaluator.evaluateAsStringValueJoined(configFileUrl, "/wsp/collection/url/exclude");
        if (excludesStr != null) {
          collection.setExcludesStr(excludesStr);
        }
        collectionContainer.put(collection.getId(), collection);
      }
    } catch (Exception e) {
      throw new ApplicationException(e);
    }
  }

}
