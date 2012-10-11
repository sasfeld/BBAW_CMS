package org.bbaw.wsp.cms.dochandler.parser.text.parser;

import org.apache.tika.parser.txt.TXTParser;

/**
 * The TXTParser. It uses the Singleton pattern. Only one instance can exist.
 * 
 * @author Sascha Feldmann (wsp-shk1)
 * @date 08.08.2012
 * 
 */
public class TxtParserImpl extends ResourceParser {
  private static TxtParserImpl instance;

  /**
   * Return the only existing instance. The instance uses an Apache TIKA
   * OpenDocument parser.
   * 
   * @return
   */
  public static TxtParserImpl getInstance() {
    if (instance == null) {
      return new TxtParserImpl();
    }
    return instance;
  }

  private TxtParserImpl() {
    super(new TXTParser());
  }

}
