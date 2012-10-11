package org.bbaw.wsp.cms.dochandler.parser.text.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.tika.parser.pdf.PDFParser;
import org.bbaw.wsp.cms.dochandler.parser.document.PdfDocument;
import org.bbaw.wsp.cms.document.MetadataRecord;

import de.mpg.mpiwg.berlin.mpdl.exception.ApplicationException;

/**
 * This class parses a PDF file. It now uses Apache PDFBox. It uses the
 * Singleton pattern. Only one instance can exist.
 * 
 * @author Sascha Feldmann (wsp-shk1)
 * @date 08.08.2012
 * @version 2.0
 * 
 */
public class PdfParserImpl extends ResourceParser {
  private static PdfParserImpl instance;

  /**
   * Return the only existing instance. The instance uses an Apache PdfBox
   * stripper.
   * 
   * @return
   */
  public static PdfParserImpl getInstance() {
    if (instance == null) {
      return new PdfParserImpl();
    }
    return instance;
  }

  // Protected because this parser may get extended
  protected PdfParserImpl() {
    super(new PDFParser());
  }

  /**
   * Parse a pdf-document and return the object returned by the
   * {@link ISaveStrategy} .
   * 
   * @return Object returned by the {@link ISaveStrategy}
   * @throws ApplicationException
   * @throws IllegalArgumentException
   *           if the uri is null or empty.
   * @throws IllegalStateException
   *           if the {@link ISaveStrategy} wasn't set before.
   */
  public Object parse(final String startUri, final String uri) throws ApplicationException {
    if (uri == null || uri.isEmpty()) {
      throw new IllegalArgumentException("The value for the parameter parser in the method parse() in PdfParserImpl mustn't be empty.");
    }
    if (this.saveStrategy == null) {
      throw new IllegalStateException("You must define a saveStategy before calling the parse()-method in ResourceParser.");
    }
    try {
      PDDocument document;
      InputStream input = this.resourceReader.read(uri);
      document = PDDocument.load(input);
      List<String> pagesTexts = new ArrayList<String>();
      String text = "";

      PDFTextStripper stripper = new PDFTextStripper();
      for (int i = 1; i <= document.getNumberOfPages(); i++) {        
        stripper.setStartPage(i);
        stripper.setEndPage(i);
        text = stripper.getText(document);;
        pagesTexts.add(text);       
      }
      
      document.close();

      input.close();
      PdfDocument doc = (PdfDocument) this.saveStrategy.generateDocumentModel(uri, uri, pagesTexts);
      doc.setMetadata(new MetadataRecord()); // Set the standard metadata (page
                                             // count, mimetype,...)

      return doc;
    } catch (IOException e) {
      throw new ApplicationException("Problem while parsing file " + uri + "  -- exception: " + e.getMessage() + "\n");
    }

  }

}
