package org.bbaw.wsp.cms.dochandler.parser.text.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import de.mpg.mpiwg.berlin.mpdl.exception.ApplicationException;

/**
 * The Adapter class which builds an InputStream depending on the kind of
 * resource (e.g. HTTP or file system resource)
 * 
 * @author Sascha Feldmann (wsp-shk1)
 * @date 07.08.2012
 * 
 */
public class ResourceReaderImpl implements IResourceReader {

  /*
   * (non-Javadoc)
   * 
   * @see bbaw.wsp.parser.fulltext.IResourceReader#read(java.lang.String)
   */
  public InputStream read(final String uri) throws ApplicationException {
    try {
      if (uri.contains("http://")) {
        URL url;
        url = new URL(uri);
        InputStream in = url.openStream();
        return in;
      } else {
        InputStream in = new FileInputStream(new File(uri));
        return in;
      }
    } catch (IOException e) {
      throw new ApplicationException("The type of resource for this URI " + uri + " isn't supported: " + e.getMessage());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see bbaw.wsp.parser.fulltext.readers.IResourceReader#getResourceType(java
   * .lang.String)
   */
  @SuppressWarnings("deprecation")
  public URL getURI(String uri) throws ApplicationException {
    try {
      if (uri.contains("http://")) {
        URL url;
        url = new URL(uri);
        return url;
      } else {
        File file = new File(uri);
        URL url;
        url = file.toURL();
        return url;
      }
    } catch (IOException e) {
      throw new ApplicationException("The type of resource for this URI " + uri + " isn't supported: " + e.getMessage());
    }
  }
}
