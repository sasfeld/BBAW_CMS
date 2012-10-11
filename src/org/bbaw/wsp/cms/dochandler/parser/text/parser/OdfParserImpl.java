package org.bbaw.wsp.cms.dochandler.parser.text.parser;

import java.io.InputStream;
import java.util.TreeMap;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.odf.OpenDocumentParser;
import org.bbaw.wsp.cms.dochandler.parser.document.CharCodeManager;
import org.bbaw.wsp.cms.dochandler.parser.document.GeneralDocument;
import org.xml.sax.ContentHandler;

import de.mpg.mpiwg.berlin.mpdl.exception.ApplicationException;

/**
 * The ODFParser. It uses the Singleton pattern. Only one instance can exist.
 * 
 * @author Sascha Feldmann (wsp-shk1)
 * @date 08.08.2012
 * 
 */
public class OdfParserImpl extends ResourceParser {
  private static OdfParserImpl instance;

  /**
   * Return the only existing instance. The instance uses an Apache TIKA
   * OpenDocument parser.
   * 
   * @return
   */
  public static OdfParserImpl getInstance() {
    if (instance == null) {
      return new OdfParserImpl();
    }
    return instance;
  }

  private OdfParserImpl() {
    super(new OpenDocumentParser());
  }
  
  public Object parse(final String startUri, final String uri) throws ApplicationException {
    if (uri == null || uri.isEmpty()) {
      throw new IllegalArgumentException("The value for the parameter parser in the method parse() in ResourceParser mustn't be empty.");
    }
    if (this.saveStrategy == null) {
      throw new IllegalStateException("You must define a saveStategy before calling the parse()-method in ResourceParser.");
    }
    InputStream input;
    try {
      input = this.resourceReader.read(uri);

      Metadata metadata = new Metadata();
      TreeMap<Integer, String> footnotes = new TreeMap<Integer, String>();
      ContentHandler footnoteHandler = new OdfHandler(footnotes);

      ParseContext context = new ParseContext();
      this.parser.parse(input, footnoteHandler, metadata, context); // Recieve
                                                                    // footnotes
      input.close();

      final GeneralDocument doc = (GeneralDocument) super.parse(startUri, uri);
      String textOrig = doc.getTextOrig(); // recieve fulltext through a
                                           // BodyContentHandler

      for (final int key : footnotes.keySet()) {
        final String toReplace = (key + footnotes.get(key));
        textOrig = textOrig.replace(toReplace, "" + CharCodeManager.returnNumberSuperscript(key)); // Replace
                                                                                                   // footnote
                                                                                                   // marks
                                                                                                   // thorugh
                                                                                                   // superscript
                                                                                                   // characters
      }

      final StringBuilder newTextOrigBuilder = new StringBuilder();
      newTextOrigBuilder.append(textOrig + "\n\n");
      for (final int key : footnotes.keySet()) { // append footnotes
        newTextOrigBuilder.append(CharCodeManager.returnNumberSuperscript(key) + " " + footnotes.get(key) + "\n");
      }

      doc.setTextOrig(newTextOrigBuilder.toString());

      return doc;
    } catch (Exception e) {
      throw new ApplicationException("Problem while parsing file " + uri + "  -- exception: " + e.getMessage() + "\n");
    }
  }
}
