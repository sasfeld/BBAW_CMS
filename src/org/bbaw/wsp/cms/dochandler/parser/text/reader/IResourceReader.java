/**
 * This package contains reader classes which are able to open documents from different location systems (HTTP or file systems).
 */
package org.bbaw.wsp.cms.dochandler.parser.text.reader;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import de.mpg.mpiwg.berlin.mpdl.exception.ApplicationException;

/**
 * This interface offers a method to get an input stream by a different resource
 * type (e.g.: HTTP resource, local file...)
 * 
 * @author Sascha Feldmann (wsp-shk1)
 * 
 */
public interface IResourceReader {

  /**
   * Get the input stream for a URI. This uri is given by a {@link Harvester}.
   * 
   * @param uri
   *          - the URI given by the {@link Harvester}
   * @return the {@link InputStream} for the parser
   * @throws ApplicationException
   */
  InputStream read(final String uri) throws ApplicationException;

  /**
   * Get the resource type for a URI. A resource type can be a {@link File} or
   * an {@link URL}.
   * 
   * @param uri
   *          a URI to a {@link File} or a {@link URL}
   * @return a URI to a {@link File} or a {@link URL}
   * @throws ApplicationException
   */
  URL getURI(final String uri) throws ApplicationException;
}
