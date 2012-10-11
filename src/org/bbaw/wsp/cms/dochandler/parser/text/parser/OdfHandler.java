package org.bbaw.wsp.cms.dochandler.parser.text.parser;

import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * This special implementation of a Tika {@link ContentHandler} finds footnotes
 * and adds them to a {@link TreeMap}.
 * 
 * @author Sascha Feldmann (wsp-shk1)
 * 
 * @date 18.09.2012
 * 
 */
public class OdfHandler implements ContentHandler {

  private StringBuilder buildedString;
  private TreeMap<Integer, String> footnotes;

  /**
   * Create a new special OdfHandler who finds all footnotes and add those to a
   * (sorted) tree map.
   * 
   * @param footnotes
   *          an {@link TreeMap} which the footnotes will be added to.
   */
  public OdfHandler(final TreeMap<Integer, String> footnotes) {
    if (footnotes == null) {
      throw new IllegalArgumentException("The value for the parameter footnotes in the constructor of OdfHandler mustn't be empty.");
    }
    this.footnotes = footnotes;
  }

  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
    for (char c : ch) {
      buildedString.append(c); // build to a string
    }
  }

  @Override
  public void endDocument() throws SAXException {

  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    // Find footnotes and put them to the map
    final Pattern p = Pattern.compile("(?i)<text:note.*?text:note-class=\"footnote\".*?><text:note-citation>(.*?)</text:note-citation><text:note-body>(.*?)</text:note-body></text:note>");
    // pattern
    for (Matcher m = p.matcher(buildedString.toString()); m.find();) {
      final int noteCitation = Integer.parseInt(m.group(1));
      final String noteBody = m.group(2).replaceAll("<.*?>", "");
      this.footnotes.put(noteCitation, noteBody);
    }
  }

  @Override
  public void endPrefixMapping(String prefix) throws SAXException {
    // TODO Auto-generated method stub

  }

  @Override
  public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
    // TODO Auto-generated method stub

  }

  @Override
  public void processingInstruction(String target, String data) throws SAXException {
    // TODO Auto-generated method stub

  }

  @Override
  public void setDocumentLocator(Locator locator) {
    // TODO Auto-generated method stub

  }

  @Override
  public void skippedEntity(String name) throws SAXException {
    // TODO Auto-generated method stub

  }

  @Override
  public void startDocument() throws SAXException {

  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
    buildedString = new StringBuilder(); // we define a string builder for each
                                         // element
  }

  @Override
  public void startPrefixMapping(String prefix, String uri) throws SAXException {
    // TODO Auto-generated method stub

  }

}
