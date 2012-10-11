/**
 * This package contains all classes which are used to save information about a parsed document (e.g. extracted metadata) and the fulltext.
 */
package org.bbaw.wsp.cms.dochandler.parser.document;

import org.bbaw.wsp.cms.document.MetadataRecord;
import org.bbaw.wsp.cms.dochandler.parser.text.parser.ResourceParser;

/**
 * This interface gives access to all kinds of parsed documents. Those documents
 * were parsed by the {@link ResourceParser} before.
 * 
 * @author Sascha Feldmann (wsp-shk1)
 * @date 16.08.2012
 * 
 */
public interface IDocument {

  /**
   * Recieve the fulltext of any kind of parsed document as String.
   * 
   * @return the fulltext as String. Never returns null.
   */
  String getTextOrig();

  /**
   * Recieve the URL to a parsed document.
   * 
   * @return the URL as String. Never returns null.
   */
  String getURL();

  /**
   * Fetch the Metadata.
   * 
   * @return {@link MetadataRecord}. May return null if the metadata wasn't set.
   */
  MetadataRecord getMetadata();

  /**
   * Set the {@link MetadataRecord}.
   * 
   * @param metadata
   *          the {@link MetadataRecord}.
   */
  void setMetadata(final MetadataRecord metadata);
}
