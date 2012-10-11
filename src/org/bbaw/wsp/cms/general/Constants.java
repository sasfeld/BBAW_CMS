package org.bbaw.wsp.cms.general;

import java.net.URL;
import java.util.Properties;

import de.mpg.mpiwg.berlin.mpdl.util.Util;

public class Constants {
  private static Constants instance;
  private Properties properties;

  public static Constants getInstance() {
    if (instance == null) {
      instance = new Constants();
      instance.init();
    }
    return instance;
  }
  
  private void init() {
    URL url = Constants.class.getClassLoader().getResource("constants.properties"); 
    if (url != null) {
      String propertiesFileName = url.toString().substring(5);
      properties = (new Util()).getProperties(propertiesFileName);
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

  public String getConfDir() {
    if (properties != null)
      return properties.getProperty("confDir");
    else 
      return "no properties file";
  }
}
