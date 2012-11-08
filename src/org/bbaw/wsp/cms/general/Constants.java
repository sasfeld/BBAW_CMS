package org.bbaw.wsp.cms.general;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Constants {
  private static Constants instance;
  private String applicationDirectory;
  private String configDirectory;
  private Properties properties;

  public static Constants getInstance() {
    if (instance == null) {
      instance = new Constants();
      instance.init();
    }
    return instance;
  }
  
  public static Constants getInstance(String applicationDirectory) {
    if (instance == null) {
      instance = new Constants();
      instance.applicationDirectory = applicationDirectory;
      instance.init();
    }
    return instance;
  }
  
  private void init() {
    File configDir = new File("./config");
    if (applicationDirectory != null)
      configDir = new File(applicationDirectory + "/config");
    configDirectory = configDir.getAbsolutePath();
    if (configDir.exists()) {
      File coreConstantsPropFile = new File(configDirectory + "/core/constants.properties");
      if (coreConstantsPropFile.exists()) {
        try {
          FileInputStream in = new FileInputStream(coreConstantsPropFile);
          properties = new Properties();
          properties.load(in);
          System.out.println("CMS core property file: " + coreConstantsPropFile.getAbsolutePath() + " loaded");
        } catch (IOException e) {
          e.printStackTrace();
        }
      } else {
        System.out.println("CMS core property file: " + coreConstantsPropFile.getAbsolutePath() + " not found");
      }
    } else {
      System.out.println("Application configuration directory: " + configDirectory + " not found");
    }
  }
  
  public String getDocumentsDir() {
    if (properties != null)
      return properties.getProperty("documentsDir");
    else 
      return "no properties file";
  }

  public String getLuceneDocumentsDir() {
    if (properties != null)
      return properties.getProperty("luceneDocumentsDir");
    else 
      return "no properties file";
  }

  public String getLuceneNodesDir() {
    if (properties != null)
      return properties.getProperty("luceneNodesDir");
    else 
      return "no properties file";
  }

  public String getCollectionConfDir() {
    return configDirectory + "/collections";
  }
}
