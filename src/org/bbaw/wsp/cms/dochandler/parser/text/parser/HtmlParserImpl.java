package org.bbaw.wsp.cms.dochandler.parser.text.parser;

import org.apache.tika.parser.html.HtmlParser;

/**
 * This class parses an HTML file. It uses the Singleton pattern. Only one
 * instance can exist.
 * 
 * @author Sascha Feldmann (wsp-shk1)
 * @date 08.08.2012
 * 
 */
public class HtmlParserImpl extends ResourceParser {
  private static HtmlParserImpl instance;

  /**
   * Return the only existing instance. The instance uses an Apache TIKA HTML
   * parser.
   * 
   * @return
   */
  public static HtmlParserImpl getInstance() {
    if (instance == null) {
      return new HtmlParserImpl();
    }
    return instance;
  }

  protected HtmlParserImpl() {
    super(new HtmlParser());
  }

}
