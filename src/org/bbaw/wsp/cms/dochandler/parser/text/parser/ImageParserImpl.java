package org.bbaw.wsp.cms.dochandler.parser.text.parser;

import org.apache.tika.parser.image.ImageParser;

/**
 * This class parses an image. It uses the Singleton pattern. Only one instance
 * can exist.
 * 
 * @author Sascha Feldmann (wsp-shk1)
 * @date 08.08.2012
 * 
 */
public class ImageParserImpl extends ResourceParser {
  private static ImageParserImpl instance;

  /**
   * Return the only existing instance. The instance uses an Apache TIKA Image
   * parser.
   * 
   * @return
   */
  public static ImageParserImpl getInstance() {
    if (instance == null) {
      return new ImageParserImpl();
    }
    return instance;
  }

  private ImageParserImpl() {
    super(new ImageParser());
  }

}
