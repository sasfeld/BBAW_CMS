/**
 * The document package contains global model classes for the resources.
 */
package org.bbaw.wsp.cms.dochandler.parser.document;

import org.bbaw.wsp.cms.dochandler.parser.text.parser.ResourceParser;
import org.bbaw.wsp.cms.document.MetadataRecord;

/**
 * This class realizes an {@link IDocument} and saves the data for all kinds of
 * parsed documents (pdf, html, odt,...)
 * 
 * @author Sascha Feldmann (wsp-shk1)
 * @date 16.08.2012
 * 
 *       Last change: 06.09.12 - Standard metadata
 * 
 */
public class GeneralDocument implements IDocument {

  private String textOrig;
  private String url;
  protected MetadataRecord metadata;

  /**
   * Create a new GeneralDocument model class.
   * 
   * @param url
   *          - URL of the parsed document.
   * @param fulltext
   *          - the parsed fulltext.
   * @throws IllegalArgumentException
   *           if one of the parameters is null.
   */
  public GeneralDocument(final String url, final String fulltext) {
    if (url == null) {
      throw new IllegalArgumentException("The value for the parameter URL in GeneralDocument mustn't be null.");
    }
    if (fulltext == null) {
      throw new IllegalArgumentException("The value for the parameter fulltext in GeneralDocument mustn't be null.");
    }

    this.url = url;
    this.textOrig = fulltext;
  }

  /*
   * (non-Javadoc)
   * 
   * @see bbaw.wsp.parser.fulltext.document.IDocument#getFulltext()
   */
  public String getTextOrig() {
    return this.textOrig;
  }

  /*
   * (non-Javadoc)
   * 
   * @see bbaw.wsp.parser.fulltext.document.IDocument#getURL()
   */
  public String getURL() {
    return this.url;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return "GeneralDocument [fulltext=" + textOrig + ", url=" + url + "]";
  }

  /*
   * (non-Javadoc)
   * @see org.bbaw.wsp.cms.dochandler.parser.document.IDocument#getMetadata()
   */
  public MetadataRecord getMetadata() {
    return this.metadata;
  }

  public void setMetadata(final MetadataRecord metadata) {
    if (metadata.getUri() == null) {
      metadata.setUri(this.getURL());
    }
    this.metadata = metadata;
  }

  /**
   * Set the textOrig. This is necessary if the sub classes of {@link ResourceParser} need to change an recieved fulltext.
   * @param textOrig
   */
  public void setTextOrig(final String textOrig) {
    this.textOrig = textOrig;
  }
}
