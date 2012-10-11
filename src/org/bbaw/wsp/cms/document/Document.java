package org.bbaw.wsp.cms.document;

import java.util.ArrayList;

import org.apache.lucene.document.Fieldable;

public class Document {
  private org.apache.lucene.document.Document document;
  private ArrayList<String> hitFragments;
  
  public Document(org.apache.lucene.document.Document luceneDocument) {
    this.document = luceneDocument;
  }

  public org.apache.lucene.document.Document getDocument() {
    return document;
  }

  public Fieldable getFieldable(String field) {
    if (document != null)
      return document.getFieldable(field);
    else 
      return null;
  }
  
  public void setDocument(org.apache.lucene.document.Document document) {
    this.document = document;
  }

  public ArrayList<String> getHitFragments() {
    return hitFragments;
  }

  public void setHitFragments(ArrayList<String> hitFragments) {
    this.hitFragments = hitFragments;
  }

}
