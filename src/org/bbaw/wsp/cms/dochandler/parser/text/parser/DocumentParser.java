/**
 * This package contains all fulltext parser classes, such as the parser classes for special formats and strategy classes which handle the parsers' results.
 */
package org.bbaw.wsp.cms.dochandler.parser.text.parser;

import org.bbaw.wsp.cms.dochandler.parser.document.IDocument;

import de.mpg.mpiwg.berlin.mpdl.exception.ApplicationException;

/**
 * Instances of this class parse heterogeneous documents including KOBV eDocs.
 * 
 * @author Sascha Feldmann (wsp-shk1)
 * @date 28.08.2012
 * 
 *       Last change: - isEdoc() now checks the eDoc via HTTP
 * 
 */
public class DocumentParser {
  /**
   * PDF file extension.
   */
  public static final String EXT_PDF = ".pdf";
  /**
   * DOC file extension.
   */
  public static final String EXT_DOC = ".doc";
  /**
   * Open document text extension.
   */
  public static final String EXT_ODT = ".odt";
  /**
   * XML extension.
   */
  public static final String EXT_XML = ".xml";
  /**
   * JPG extension.
   */
  public static final String EXT_JPG = ".jpg";
  /**
   * TIFF extension.
   */
  public static final String EXT_TIFF = ".tiff";
  /**
   * PNG extension.
   */
  public static final String EXT_PNG = ".png";
  /**
   * HTML extension.
   */
  public static final String EXT_HTML = ".html";
  /**
   * XHTML extension.
   */
  public static final String EXT_XHTML = ".xhtml";
  /**
   * HTM extension.
   */
  public static final String EXT_HTM = ".htm";
  /**
   * TXT extension.
   */
  public static final String EXT_TXT = ".txt";

  protected DocumentModelStrategy documentModelBuilder;

  /**
   * Create a new DocumentParser instance. An instance will offer a
   * parse()-method which gets a URL to be parsed.
   */
  public DocumentParser() {
    this.documentModelBuilder = new DocumentModelStrategy();
  }

  /**
   * Parse any kind of document.
   * 
   * @throws ApplicationException
   *           if there's no parser available for the type of resource.
   * @param url
   *          - the URL to the document.
   * @return an {@link IDocument} containing the fulltext and maybe metadata for
   *         the parsed document.
   */
  public IDocument parse(final String url) throws ApplicationException {
    ResourceParser parser = null;

    if (EdocIndexMetadataFetcherTool.isEDocIndex(url)) {      
      parser = EdocParserImpl.getInstance();
    } else if (getExtension(url).equals(EXT_PDF)) {     
      parser = PdfParserImpl.getInstance();
    } else if (getExtension(url).equals(EXT_DOC)) {
      parser = DocParserImpl.getInstance();
    } else if (getExtension(url).equals(EXT_ODT)) {
      parser = OdfParserImpl.getInstance();
    } else if (getExtension(url).equals(EXT_XML)) {
      parser = XmlParserImpl.getInstance();
    } else if (getExtension(url).equals(EXT_HTM) || getExtension(url).equals(EXT_HTML) || getExtension(url).equals(EXT_XHTML)) {
      parser = HtmlParserImpl.getInstance();
    } else if (getExtension(url).equals(EXT_TXT)) {
      parser = TxtParserImpl.getInstance();
    }

    if (parser != null) {
      IDocument result = (IDocument) parser.parse("", url);
      return result;
    } else {
      throw new ApplicationException("There's no parser available for this type of resource: " + getExtension(url));
    }
  }

  /**
   * Fetch the URI's extension.
   */
  public String getExtension(final String uri) {
    final int extPos = uri.lastIndexOf(".");
    final String extension = uri.substring(extPos, uri.length());

    return extension;
  }

  /**
   * Check if the resource is an image.
   * 
   * @param uri
   *          - the resource's URI.
   * @return true if the resource is an image.
   */
  public boolean isImage(String uri) {
    if (getExtension(uri).equals(EXT_JPG) || getExtension(uri).equals(EXT_TIFF) || getExtension(uri).equals(EXT_PNG)) {
      return true;
    }
    return false;
  }

}
