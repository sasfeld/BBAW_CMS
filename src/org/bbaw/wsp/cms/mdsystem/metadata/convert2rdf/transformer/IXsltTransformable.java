package org.bbaw.wsp.cms.mdsystem.metadata.convert2rdf.transformer;

import java.io.InputStream;
import java.io.OutputStream;

import de.mpg.mpiwg.berlin.mpdl.exception.ApplicationException;

/**
 * This interface offers methods to offer an XSLT transformation.
 * @author Sascha Feldmann (wsp-shk1)
 * @date 08.10.12
 *
 */
public interface IXsltTransformable {
  
  /**
   * Do an XSLT transformation using Saxon.
   * @param xmlInput - the xml input stream.
   * @param xslStylesheet - the xslt stylesheet.
   * @param xmlOutput - the xml output stream.
   * @throws ApplicationException if one of the streams is invalid or represents an invalid XML file.
   */
  void transform(final InputStream xmlInput, final InputStream xslStylesheet, final OutputStream xmlOutput) throws ApplicationException;
}
