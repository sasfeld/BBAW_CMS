package org.bbaw.wsp.cms.dochandler.parser.document;

/**
 * This class realizes an {@link IDocument} and saves the data for a special PDF
 * page. It's part of an {@link PdfDocument}.
 * 
 * @author Sascha Feldmann (wsp-shk1)
 * @date 08.08.2012
 * 
 */
public class PdfPage {

  private int pageNumber;
  private String textOrig;

  /**
   * Create a new PDFPage.
   * 
   * @param pageNumber
   *          - the page number.
   * @param fulltext
   *          - the page's (parsed) fulltext.
   */
  public PdfPage(final int pageNumber, final String fulltext) {
    if (fulltext == null) {
      throw new IllegalArgumentException("The value for the parameter fulltext in PDFPage mustn't be null.");
    }
    this.pageNumber = pageNumber;
    this.textOrig = fulltext;
  }

  /**
   * 
   * @return the page number.
   */
  public int getPageNumber() {
    return this.pageNumber;
  }

  /**
   * 
   * @return the fulltext.
   */
  public String getTextOrig() {
    return this.textOrig;
  }

  @Override
  public String toString() {
    return "PDFPage [pageNumber=" + pageNumber + ", fulltext=" + textOrig + "]";
  }

}
