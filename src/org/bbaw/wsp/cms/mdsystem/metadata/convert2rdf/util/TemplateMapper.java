package org.bbaw.wsp.cms.mdsystem.metadata.convert2rdf.util;



import java.io.InputStream;
import java.util.HashMap;
import java.util.Scanner;

import org.bbaw.wsp.cms.dochandler.parser.text.reader.IResourceReader;
import org.bbaw.wsp.cms.dochandler.parser.text.reader.ResourceReaderImpl;

import de.mpg.mpiwg.berlin.mpdl.exception.ApplicationException;

/**
 * This class provides an easy to use template system.
 * @author Sascha Feldmann (wsp-shk1)
 * 
 * Last change: 05.11.2012 - integration to wspcms
 *
 */
public class TemplateMapper {
  private static IResourceReader reader = new ResourceReaderImpl();
  private String templateUrl;
  private String mappedTemplate;

  /**
   * Create a new TemplateMapper for an input template.
   * @param templateUrl - the url (local fileysystem or http) to the template.
   * @throws ApplicationException if the value of the templateUrl is empty or null.
   */
  public TemplateMapper(final String templateUrl) throws ApplicationException {
    if(templateUrl == null || templateUrl.isEmpty()) {
      throw new ApplicationException("The value for the templateUrl musn't be null or empty.");
    }
    this.templateUrl = templateUrl;
    this.mappedTemplate = "";
  }
  
  /**
   * Map the placeholders in the template with the given value.
   * @param placeholderMap - a map with the placeholder as key and the value to be set as the value.
   * @throws ApplicationException if the input stream for the template is not valid / available.
   */
  public void mapPlaceholder(final HashMap<String,String> placeholderMap) throws ApplicationException {
    InputStream in = reader.read(this.templateUrl);
    Scanner scanner = new Scanner(in);   
    scanner.useDelimiter("\n");
    StringBuilder builder = new StringBuilder();
    while (scanner.hasNext()) {
      builder.append(scanner.next()+"\n"); 
    }
    String templateString = builder.toString();   
    
    for (String placeholder : placeholderMap.keySet()) {   
      System.out.println("replacing "+placeholder+"by "+placeholderMap.get(placeholder));
      
     templateString = templateString.replaceAll(placeholder, placeholderMap.get(placeholder));
    }   
    System.out.println("Mapped template");
    this.mappedTemplate = templateString;
  }
  
  /**
   * Get the resulting string after mapping.
   * @return the string with the mapped placeholders or an empty string if the map method wasn't called.
   */
  public String getMappedTemplate() {
    return this.mappedTemplate;
  }
}
