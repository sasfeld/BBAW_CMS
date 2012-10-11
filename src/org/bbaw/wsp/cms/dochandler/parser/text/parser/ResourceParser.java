package org.bbaw.wsp.cms.dochandler.parser.text.parser;

import java.io.InputStream;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.bbaw.wsp.cms.dochandler.parser.document.GeneralDocument;
import org.bbaw.wsp.cms.dochandler.parser.text.reader.IResourceReader;
import org.bbaw.wsp.cms.dochandler.parser.text.reader.ResourceReaderImpl;
import org.bbaw.wsp.cms.document.MetadataRecord;
import org.xml.sax.ContentHandler;

import de.mpg.mpiwg.berlin.mpdl.exception.ApplicationException;

/**
 * This class is the API for all parsers.
 * 
 * @author Sascha Feldmann (wsp-shk1)
 * @date 06.09.12
 * 
 *       Last change: - ApplicationException instead of log file - Uses
 *       {@link DocumentModelStrategy} only.
 * 
 */
public abstract class ResourceParser {
  protected Parser parser;
  protected IResourceReader resourceReader;
  protected DocumentModelStrategy saveStrategy;

  /**
   * Create a new PdfParser instance.
   * 
   * @param uri
   *          - the URI to the document.
   * @throws IllegalArgumentException
   *           if the uri is null, empty or doesn't refer to an existing file.
   */
  public ResourceParser(final Parser parser) {
    if (parser == null) {
      throw new IllegalArgumentException("The value for the parameter parser in the constructor of PdfParserImpl mustn't be empty.");
    }

    this.parser = parser;
    this.resourceReader = new ResourceReaderImpl();
    this.saveStrategy = new DocumentModelStrategy();
  }

  /**
   * Parse a document and return the fulltext.
   * 
   * @param startUri
   *          - the harvesting URI.
   * @param uri
   *          - the URI to the document.
   * @return a String - the fulltext
   * @throws ApplicationException
   *           if the were errors while parsing.
   * @throws IllegalArgumentException
   *           if the uri is null or empty
   * @throws IllegalStateException
   *           if the {@link ISaveStrategy} wasn't set before.
   */
  public Object parse(final String startUri, final String uri) throws ApplicationException {
    if (uri == null || uri.isEmpty()) {
      throw new IllegalArgumentException("The value for the parameter parser in the method parse() in ResourceParser mustn't be empty.");
    }
    if (this.saveStrategy == null) {
      throw new IllegalStateException("You must define a saveStategy before calling the parse()-method in ResourceParser.");
    }
    InputStream input;
    try {
      input = this.resourceReader.read(uri);

      // Don't limit the amount of characters -> -1 as argument
      ContentHandler textHandler = new BodyContentHandler(-1);
      Metadata metadata = new Metadata();
      ParseContext context = new ParseContext();
      this.parser.parse(input, textHandler, metadata, context);
      input.close();
      textHandler.endDocument();

      final MetadataRecord mdRecord = new MetadataRecord();
      this.matchMetadata(metadata, mdRecord);
      final GeneralDocument doc = (GeneralDocument) this.saveStrategy.generateDocumentModel(uri, uri, textHandler.toString());
     
      doc.setMetadata(mdRecord);

      return doc;
    } catch (Exception e) {
      throw new ApplicationException("Problem while parsing file " + uri + "  -- exception: " + e.getMessage() + "\n");
    }
  }

  /**
   * Match the extracted metadata from TIKA to the {@link MetadataRecord}.
   * @param metadata the {@link Metadata} of TIKA.
   * @param mdRecord the {@link MetadataRecord}.
   */
  private void matchMetadata(Metadata metadata, MetadataRecord mdRecord) {
    @SuppressWarnings("deprecation")
    final String pageCount = metadata.get(Metadata.PAGE_COUNT);
    if(pageCount != null) {
      mdRecord.setPageCount(Integer.parseInt(pageCount));
    }      
    final String type = metadata.get(Metadata.CONTENT_TYPE);
    if(type != null) {
      mdRecord.setType(type);
    }             
  }
}
