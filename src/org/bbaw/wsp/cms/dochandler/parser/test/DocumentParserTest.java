/**
 * This package contains several test classes.
 */
package org.bbaw.wsp.cms.dochandler.parser.test;

import org.bbaw.wsp.cms.dochandler.parser.document.IDocument;
import org.bbaw.wsp.cms.dochandler.parser.document.PdfDocument;
import org.bbaw.wsp.cms.dochandler.parser.text.parser.DocumentParser;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import de.mpg.mpiwg.berlin.mpdl.exception.ApplicationException;

/**
 * Test of the {@link DocumentParser} which saves the fulltexts and maybe
 * extracted metadata in an {@link IDocument}.
 * 
 * @author Sascha Feldmann (wsp-shk1)
 * 
 */
public class DocumentParserTest {

  private static final String EXAMPLE_EDOC_URL = "http://edoc.bbaw.de/volltexte/2010/1486/index.html";
  private static final String EXAMPLE_EDOC_HTTP_URL = "http://edoc.bbaw.de/volltexte/2010/1486/pdf/Heft_17.pdf";
  private static final String EXAMPLE_PDF_URL = "C:/Dokumente und Einstellungen/wsp-shk1/Eigene Dateien/opus32_bbaw_volltexte_20120607/volltexte/2010/1314/pdf/DOKUMENTATION_Symposium_Wissenschaft_und_Wiedervereinigung.pdf";
  private static final String EULER_DOCS_FOLDER = "C:/Dokumente und Einstellungen/wsp-shk1/Eigene Dateien/ParserTest/TestDokumente/Vorhaben/Euler";

  public static void main(String[] args) throws UnsupportedEncodingException, ApplicationException {
    DocumentParser parser = new DocumentParser();

    // testEDoc(parser);
//    testPdf(parser);
    // testEDocHttp(parser);
    // allFormatTest(parser);
    testEulerDocs(parser);
  }

  /*
   * Test of all euler.bbaw.de *.docs.
   */
  private static void testEulerDocs(DocumentParser parser) throws ApplicationException {   
    File docFolder = new File(EULER_DOCS_FOLDER);
    for (File f : docFolder.listFiles()) {
      System.out.println("Parsing "+f);
      IDocument documentModel = parser.parse(EXAMPLE_EDOC_URL);
      System.out.println("DocumentModel: \n\n" + documentModel);
      System.out.println("-----------------------\n\n\n");
    }
   
    
  }

  /*
   * Test of an eDoc.
   */
  public static void testEDoc(DocumentParser parser) throws UnsupportedEncodingException {
    try {
      IDocument documentModel = parser.parse(EXAMPLE_EDOC_URL);
      System.out.println("DocumentModel: \n\n" + documentModel);
    } catch (ApplicationException e) {
      System.out.println(e);
    }
  }

  /*
   * Test of an eDoc via HTTP.
   */
  public static void testEDocHttp(DocumentParser parser) {
    try {
      IDocument documentModel = parser.parse(EXAMPLE_EDOC_HTTP_URL);
      System.out.println("DocumentModel: \n\n" + documentModel);
    } catch (ApplicationException e) {
      System.out.println(e);
    }
  }

  /*
   * Test of a "normal" pdf (not an eDoc).
   */
  public static void testPdf(DocumentParser parser) throws ApplicationException {

    IDocument documentModel = parser.parse(EXAMPLE_PDF_URL);

    // Test Ligatur:
    PdfDocument pdfDoc = (PdfDocument) documentModel;
    String result = pdfDoc.getTextOrig();
    System.out.println(result);

  }

  public static void allFormatTest(DocumentParser parser) throws ApplicationException {
    List<String> urls = new ArrayList<String>();
    // urls.add("//192.168.1.203/wsp-web-test/090210_Konzept.pdf");
    urls.add("//192.168.1.203/wsp-web-test/Czmiel_Juergens_proposal_DH2012_TheAcademysDigitalStoreOfKnowledge.doc");
    // urls.add("//192.168.1.203/wsp-web-test/Czmiel_Juergens_proposal_DH2012_TheAcademysDigitalStoreOfKnowledge.odt");
    // urls.add("//192.168.1.203/wsp-web-test/Czmiel_Juergens_proposal_DH2012_TheAcademysDigitalStoreOfKnowledge.pdf");
    // urls.add("//192.168.1.203/wsp-web-test/DH2012ReviewCzmielJuergens.txt");

    for (String url : urls) {
      IDocument doc = parser.parse(url);

      System.out.println("Parsed a document");
      System.out.println("DocumentType: " + doc.getURL());
      System.out.println("Fulltext:\n\n################\n" + doc.getTextOrig());
      System.out.println("\n#################\n\n");
    }
  }
}
