package org.bbaw.wsp.cms.transform;

import java.util.ArrayList;
import java.util.Hashtable;

import org.xml.sax.*;

import de.mpg.mpiwg.berlin.mpdl.exception.ApplicationException;
import de.mpg.mpiwg.berlin.mpdl.util.StringUtils;

public class GetFragmentsContentHandler implements ContentHandler {
  private String xmlnsString = "";
  private int currentMilestonePosition = 0;
  private Element currentElement;
  private Element currentMilestoneElement;
  private ArrayList<Element> elementQueue; // queue of opened elements (before they were closed): to connect them to a parent hierarchy 
  private Hashtable<Integer, StringBuilder> resultFragments = new Hashtable<Integer, StringBuilder>();
  private String milestoneElemenName = "pb";  // default is pb
  
  public GetFragmentsContentHandler() throws ApplicationException {
  }

  public GetFragmentsContentHandler(String milestoneElemenName) throws ApplicationException {
    this.milestoneElemenName = milestoneElemenName;
  }

  public Hashtable<Integer, StringBuilder> getResultPages() {
    return resultFragments;  
  }

  public int getMilestoneCount() {
    return currentMilestonePosition;  
  }

  public void startDocument() throws SAXException {
  }

  public void endDocument() throws SAXException {
    // write the closePath after the last minus 1 milestone element (the closing path after the last milestone element is automatically written by the normal closing tags)
    if (currentMilestoneElement != null) {
      String msClosePath = currentMilestoneElement.getClosePath();
      write(msClosePath, currentMilestoneElement.milestonePosition - 1);
    }
    resultFragments.remove(new Integer(0));  // this fragment is filled but should not
  }
  
  public void characters(char[] c, int start, int length) throws SAXException {
    char[] cCopy = new char[length];
    System.arraycopy(c, start, cCopy, 0, length);
    String charactersStr = String.valueOf(cCopy);
    if (charactersStr != null && ! charactersStr.equals("")) {
      if (currentMilestonePosition > 0) {
        charactersStr = StringUtils.deresolveXmlEntities(charactersStr);
        write(charactersStr);
      }
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
    if (elementQueue == null)
      elementQueue = new ArrayList<Element>();
    Element newElement = new Element(name);
    if (currentElement != null) {
      newElement.parent = currentElement;
    }
    currentElement = newElement;
    if (localName != null && localName.equals(milestoneElemenName)) {
      currentMilestonePosition++;
      if (currentMilestoneElement != null) {
        String msClosePath = currentMilestoneElement.getClosePath();
        write(msClosePath, currentMilestoneElement.milestonePosition - 1);
      }
      currentMilestoneElement = currentElement;
      currentMilestoneElement.milestonePosition = currentMilestonePosition;
      String msOpenPath = currentMilestoneElement.getOpenPath();
      write(msOpenPath);
    }
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
      currentElement.attrString = attrString;
    }
    if (xmlnsString != null && ! xmlnsString.isEmpty()) {
      xmlnsString = xmlnsString.trim();
      currentElement.xmlnsString = xmlnsString;
    }
    if (currentMilestonePosition > 0) {
      write("<" + name);
      if (xmlnsString != null && ! xmlnsString.isEmpty())
        write(" " + xmlnsString);
      if (attrString != null && ! attrString.isEmpty())
        write(" " + attrString);
      write(">");
    }
    xmlnsString = "";
    elementQueue.add(currentElement);
  }

  public void endElement(String uri, String localName, String name) throws SAXException {
    if (currentMilestonePosition > 0) {
      write("</" + name + ">");
    }
    if (elementQueue != null && elementQueue.size() > 0) {
      int lastIndex = elementQueue.size() - 1;
      elementQueue.remove(lastIndex);
    }
    if (elementQueue != null && elementQueue.size() > 0) {
      int lastIndex = elementQueue.size() - 1;
      currentElement = elementQueue.get(lastIndex);
    } else {
      currentElement = null;
    }
  }

  private void write(String outStr) throws SAXException {
    StringBuilder resultFragment = resultFragments.get(new Integer(currentMilestonePosition));
    if (resultFragment == null) {
      resultFragment = new StringBuilder();
      resultFragments.put(new Integer(currentMilestonePosition), resultFragment);
    }
    resultFragment.append(outStr);
  }
  
  private void write(String outStr, int milestoneNumber) throws SAXException {
    StringBuilder resultFragment = resultFragments.get(new Integer(milestoneNumber));
    if (resultFragment == null) {
      resultFragment = new StringBuilder();
      resultFragments.put(new Integer(milestoneNumber), resultFragment);
    }
    resultFragment.append(outStr);
  }
  
  public class Element {
    public String name;
    private String xmlnsString;
    private String attrString;
    private int milestonePosition;
    private Element parent;
    
    private Element(String name) {
      this.name = name;
    }

    private String getOpenPath() {
      StringBuilder ancestorsStrBuilder = new StringBuilder();
      if (parent != null) {
        ancestorsStrBuilder.append(parent.getOpenPath()); 
        ancestorsStrBuilder.append("<");
        ancestorsStrBuilder.append(parent.name);
        if (parent.xmlnsString != null && ! parent.xmlnsString.isEmpty()) {
          ancestorsStrBuilder.append(" ");
          ancestorsStrBuilder.append(parent.xmlnsString);
        }
        if (parent.attrString != null && ! parent.attrString.isEmpty()) {
          ancestorsStrBuilder.append(" " + parent.attrString);
        }
        ancestorsStrBuilder.append(">");
      }
      return ancestorsStrBuilder.toString();
    }
 
    private String getClosePath() {
      StringBuilder ancestorsStrBuilder = new StringBuilder();
      if (parent != null) {
        ancestorsStrBuilder.append("</");
        ancestorsStrBuilder.append(parent.name);
        ancestorsStrBuilder.append(">");
        ancestorsStrBuilder.append(parent.getClosePath()); 
      }
      return ancestorsStrBuilder.toString();
    }

  }
}
