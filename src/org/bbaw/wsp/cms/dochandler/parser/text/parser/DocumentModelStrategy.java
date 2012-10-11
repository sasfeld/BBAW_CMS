package org.bbaw.wsp.cms.dochandler.parser.text.parser;

import java.util.List;

import org.bbaw.wsp.cms.dochandler.parser.document.GeneralDocument;
import org.bbaw.wsp.cms.dochandler.parser.document.IDocument;
import org.bbaw.wsp.cms.dochandler.parser.document.PdfDocument;

/**
 * This class realizes a DocumentModel - Strategy. That means the strategy
 * creates a new document model which is accessible by the {@link IDocument}
 * interface for each parsed document. Last change: saveFile() now uses a
 * {@link StringBuilder} to concatenate the fulltext String.
 * 
 * @author Sascha Feldmann (wsp-shk1)
 * @date 16.08.2012
 * 
 */
public class DocumentModelStrategy {

  /**
   * Generate a {@link GeneralDocument} which keeps information of any kind of
   * parsed document.
   * 
   * @param startURI
   *          - the URI where the parsing job was started.
   * @param uri
   *          - the URI of the parsed document.
   * @param text
   *          - the fetched fulltext as String.
   * @return an {@link IDocument}
   */
  public Object generateDocumentModel(final String startURI, final String uri, final String text) {
    IDocument document = new GeneralDocument(uri, text);
    return document;
  }

  /**
   * Generate a {@link PdfDocument} which keeps information of a parsed PDF
   * document.
   * 
   * @param - the URI where the parsing job was started.
   * @param uri
   *          - the URI of the parsed document.
   * @param textPages
   *          - a list of String. Each entry represents a page of the parsed
   *          document.
   * @return an {@link IDocument}. You can extract pages by using the
   *         {@link PdfDocument} instance.
   */
  public Object generateDocumentModel(final String startURI, final String uri, final List<String> textPages) {
    StringBuilder textBuilder = new StringBuilder();
    for (int i = 1; i <= textPages.size(); i++) {
      textBuilder.append("[page=" + i + "]\n" + textPages.get(i - 1));
    }
    IDocument document = new PdfDocument(uri, textBuilder.toString(), textPages);
    return document;
  }

}
