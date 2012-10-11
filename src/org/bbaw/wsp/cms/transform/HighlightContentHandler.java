package org.bbaw.wsp.cms.transform;

import java.util.ArrayList;

import org.bbaw.wsp.cms.lucene.IndexHandler;
import org.xml.sax.*;

import de.mpg.mpiwg.berlin.mpdl.exception.ApplicationException;
import de.mpg.mpiwg.berlin.mpdl.util.StringUtils;

public class HighlightContentHandler implements ContentHandler {
  private String xmlnsString = "";
  private String highlightElemName;  
  private int highlightElemPos = 1; 
  private int currentHighlightElemPos = 0;
  private boolean highlightElemMode = false;
  private int highlightElemModeOpenTags = 0;
  private String highlightQueryType = "orig";  // orig, reg, norm or morph
  private String highlightQuery;  // complex Lucene query
  private String highlightQueryForms;  // highlight terms separated by a blank
  private boolean highlightHitMode = false;
  private int highlightHitModeOpenTags = 0;
  private boolean firstPageBreakReachedMode = false;  // in a page fragment: if a page break element is surrounded by an element (e.g. "s") then this element should not increment the currentHighlightElemPos 
  private boolean firstPageBreakReached = true;  
  private StringBuilder result = new StringBuilder();
  
  public HighlightContentHandler() throws ApplicationException {
  }

  public HighlightContentHandler(String highlightElemName, int highlightElemPos) throws ApplicationException {
    this.highlightElemName = highlightElemName;
    this.highlightElemPos = highlightElemPos;
  }

  public HighlightContentHandler(String highlightElemName, int highlightElemPos, String highlightQueryType, String highlightQuery, String language) throws ApplicationException {
    this.highlightElemName = highlightElemName;
    this.highlightElemPos = highlightElemPos;
    this.highlightQueryType = highlightQueryType;
    this.highlightQuery = highlightQuery;
    if (highlightQuery != null) {
      IndexHandler indexHandler = IndexHandler.getInstance();
      ArrayList<String> queryTerms = indexHandler.fetchTerms(highlightQuery, language); // all query terms in query (also morphological terms)
      highlightQueryForms = toString(queryTerms);
    }
  }

  public void setFirstPageBreakReachedMode(boolean firstPageBreakReachedMode) {
    this.firstPageBreakReachedMode = firstPageBreakReachedMode; 
    if (firstPageBreakReachedMode)
      this.firstPageBreakReached = false;  // is first set to false and later if a page break is found (by startElement) it is set to true
  }
  
  public StringBuilder getResult() {
    return result;  
  }

  public void startDocument() throws SAXException {
  }

  public void endDocument() throws SAXException {
  }
  
  public void characters(char[] c, int start, int length) throws SAXException {
    char[] cCopy = new char[length];
    System.arraycopy(c, start, cCopy, 0, length);
    String charactersStr = String.valueOf(cCopy);
    if (charactersStr != null && ! charactersStr.equals("")) {
      charactersStr = StringUtils.deresolveXmlEntities(charactersStr);
      write(charactersStr);
    }
  }

  public void ignorableWhitespace(char[] c, int start, int length) throws SAXException {
  }

  public void processingInstruction(String target, String data) throws SAXException {
  }

  public void setDocumentLocator(Locator locator) {
  }

  public void startPrefixMapping(String prefix, String uri) throws SAXException {
    xmlnsString += "xmlns:" + prefix + "=\"" + uri + "\" ";
    if (prefix != null && prefix.equals(""))  
      xmlnsString = "xmlns" + "=\"" + uri + "\" ";
  }
  
  public void endPrefixMapping(String prefix) throws SAXException {
  }

  public void skippedEntity(String name) throws SAXException {
  }

  public void startElement(String uri, String localName, String name, Attributes attrs) throws SAXException {
    int attrSize = attrs.getLength();
    String attrString = "";
    for (int i=0; i<attrSize; i++) {
      String attrQName = attrs.getQName(i);
      String attrValue = attrs.getValue(i);
      attrValue = StringUtils.forXML(attrValue);
      attrString = attrString + " " + attrQName + "=\"" + attrValue + "\"";
    }
    if (attrString != null && ! attrString.isEmpty()) {
      attrString = attrString.trim();
    }
    if (xmlnsString != null && ! xmlnsString.isEmpty()) {
      xmlnsString = xmlnsString.trim();
    }
    if (localName.equals("pb"))
      firstPageBreakReached = true;
    // start highlight element at position
    if (highlightElemName != null && highlightElemName.equals(localName) && firstPageBreakReached) {
      currentHighlightElemPos++;
      if (currentHighlightElemPos == highlightElemPos && highlightElemModeOpenTags == 0) {
        highlightElemMode = true;
        write("<hi type=\"elem\">");
      }
    }
    if (highlightElemMode) {
      highlightElemModeOpenTags++;
    }
    // start highlight query 
    if (highlightQuery != null && localName.equals("w")) {
      boolean matched = false;
      String attrQName = "form";
      if (highlightQueryType.equals("orig"))
        attrQName = "form";
      else if (highlightQueryType.equals("reg"))
        attrQName = "formRegularized";
      else if (highlightQueryType.equals("norm"))
        attrQName = "formNormalized";
      else if (highlightQueryType.equals("morph"))
        attrQName = "lemmas";
      String attrValue = getAttrValue(attrs, attrQName);
      if (attrValue != null) {
        String[] forms = highlightQueryForms.split(" "); 
        for (int i=0; i<forms.length; i++) {
          if (! matched) {
            String form = forms[i];
            if (form.endsWith("*")) {  // TODO support middle wildcard queries: bla*bla bla?bla 
              form = form.replace("*", "");
              matched = attrValue.startsWith(form);
            } else {
              matched = attrValue.equals(form);  
            }
          }
        }
      }
      if ((highlightElemName == null && matched && highlightHitModeOpenTags == 0) || (highlightElemName != null && highlightElemMode && matched && highlightHitModeOpenTags == 0)) {
        highlightHitMode = true;
        write("<hi type=\"hit\">");
      }
    }
    if (highlightHitMode) {
      highlightHitModeOpenTags++;
    }
    write("<" + name);
    if (xmlnsString != null && ! xmlnsString.isEmpty())
      write(" " + xmlnsString);
    if (attrString != null && ! attrString.isEmpty())
      write(" " + attrString);
    write(">");
    xmlnsString = "";
  }

  public void endElement(String uri, String localName, String name) throws SAXException {
    write("</" + name + ">");
    // end highlight element at position
    if (highlightElemMode) {
      if (highlightElemModeOpenTags == 1) {
        highlightElemMode = false;
        write("</hi>");
      }
      highlightElemModeOpenTags--;
    }
    // end highlight query 
    if (highlightHitMode) {
      if (highlightHitModeOpenTags == 1) {
        highlightHitMode = false;
        write("</hi>");
      }
      highlightHitModeOpenTags--;
    }
  }

  private String toString(ArrayList<String> queryForms) {
    String queryFormsStr = "";
    for (int i=0; i<queryForms.size(); i++) {
      String form = queryForms.get(i);
      queryFormsStr = queryFormsStr + form + " ";
    }
    if (queryForms == null || queryForms.size() == 0)
      return null;
    else
      return queryFormsStr.substring(0, queryFormsStr.length() -1); 
  }
  
  private void write(String outStr) throws SAXException {
    result.append(outStr);
  }
  
  private String getAttrValue(Attributes attrs, String attrQName) {
    String retValue = null;
    int attrSize = attrs.getLength();
    for (int i=0; i<attrSize; i++) {
      String attrQNameTmp = attrs.getQName(i);
      String attrValue = attrs.getValue(i);
      if (attrQNameTmp.equals(attrQName))
        return attrValue;
    }
    return retValue;
  }
}
