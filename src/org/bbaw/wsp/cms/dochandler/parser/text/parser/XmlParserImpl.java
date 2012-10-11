package org.bbaw.wsp.cms.dochandler.parser.text.parser;

import org.apache.tika.parser.xml.XMLParser;

/**
 * This class parses an XML file. It uses the Singleton pattern. Only one
 * instance can exist.
 * 
 * @author Sascha Feldmann (wsp-shk1)
 * @date 08.08.2012
 * 
 */
public class XmlParserImpl extends ResourceParser {
  private static XmlParserImpl instance;

  /**
   * Return the only existing instance. The instance uses an Apache XML parser.
   * 
   * @return
   */
  public static XmlParserImpl getInstance() {
    if (instance == null) {
      return new XmlParserImpl();
    }
    return instance;
  }

  private XmlParserImpl() {
    super(new XMLParser());
  }

}
