package org.bbaw.wsp.cms.dochandler.parser.text.parser;

import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tika.parser.microsoft.OfficeParser;
import org.bbaw.wsp.cms.dochandler.parser.document.CharCodeManager;
import org.bbaw.wsp.cms.dochandler.parser.document.GeneralDocument;

import de.mpg.mpiwg.berlin.mpdl.exception.ApplicationException;

/**
 * This class parses a DOC file. It uses the Singleton pattern. Only one
 * instance can exist. Last change: Improved handling of footnotes. They are now
 * marked by superscripts and appended to the textOrig (fulltext). See
 * {@link OdfParserImpl}.
 * 
 * @author Sascha Feldmann (wsp-shk1)
 * @date 20.09.2012
 * 
 */
public class DocParserImpl extends ResourceParser {
  /**
   * UTF 16 REPLACEMENT CHARACTER (which TIKA uses to mark footnotes).
   */
  public static final char CODE_FOOTNOTE = 0xFFFD;
  /*
   * This value defines the minimum distance to the last footnote.
   */
  private static final int FOOTNOTES_TOLERANCE = 2;
  
  private static DocParserImpl instance;

  /**
   * Return the only existing instance. The instance uses an Apache TIKA Doc
   * parser.
   * 
   * @return
   */
  public static DocParserImpl getInstance() {
    if (instance == null) {
      return new DocParserImpl();
    }
    return instance;
  }

  private DocParserImpl() {
    super(new OfficeParser());
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.bbaw.wsp.cms.dochandler.parser.text.parser.ResourceParser#parse(java
   * .lang.String, java.lang.String)
   */
  public Object parse(final String startUri, final String uri) throws ApplicationException {
    GeneralDocument doc = (GeneralDocument) super.parse(startUri, uri);

    String[] lines = doc.getTextOrig().split("\n"); // parse lines
    TreeMap<Integer, String> footnotes = new TreeMap<Integer, String>();
    StringBuilder newTextOrigBuilder = new StringBuilder(); // StringBuilder to
                                                            // cut the footnotes
                                                            // from the original
                                                            // String
    int linesSinceLastNote = -1; // If a footnote is identified, this int number
                                 // saves the number of lines until the next
                                 // footnote
    int lineNumber = 0; // identify the current line number
    int footnoteNumber = 0; // identify the current footnote number
    for (String line : lines) { // Find footnotes
      if (footnoteNumber == 0) { // cut textOrig, remove footnotes
        newTextOrigBuilder.append(line + "\n");
      }
      lineNumber++;
      // identifiy footnotes after the first half of the document
      if (lineNumber > (lines.length / 2)) {
        boolean match = false;
        final Pattern p = Pattern.compile(CODE_FOOTNOTE + "\t(.*)");
        for (Matcher m = p.matcher(line); m.find();) {
          match = true;
          linesSinceLastNote = 0;

          /*
           * configure tolerance if conditions change (e.g. if the parser
           * seperates footnotes by three empty lines instead of two)
           */
          if (linesSinceLastNote < FOOTNOTES_TOLERANCE) {
            footnoteNumber++;
            final String footnote = m.group(1);
            if (footnoteNumber == 1) { // remove first footnote from
                                       // StringBuilder
              newTextOrigBuilder.delete(newTextOrigBuilder.toString().indexOf(CODE_FOOTNOTE + "\t" + footnote), newTextOrigBuilder.length());
            }
            footnotes.put(footnoteNumber, footnote);
          }
        }
        if (!match) {
          linesSinceLastNote++;
        }
      }

    }

    doc.setTextOrig(newTextOrigBuilder.toString());

    // Find superscripts and replace them and concatenate to textOrig
    for (int key : footnotes.keySet()) {
      doc.setTextOrig(doc.getTextOrig().replaceFirst(CODE_FOOTNOTE + "", CharCodeManager.returnNumberSuperscript(key)));
      doc.setTextOrig(doc.getTextOrig().concat(CharCodeManager.returnNumberSuperscript(key) + " " + footnotes.get(key)) + "\n");
    }
    
    

    return doc;
  }
}
