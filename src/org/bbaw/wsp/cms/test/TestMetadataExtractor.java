package org.bbaw.wsp.cms.test;

import java.io.File;

import org.bbaw.wsp.cms.mdsystem.metadata.general.extractor.factory.MetadataExtractorFactory;

import de.mpg.mpiwg.berlin.mpdl.exception.ApplicationException;
import org.bbaw.wsp.cms.general.Constants;

public class TestMetadataExtractor {

//  private static final String RDF_FILE = "C:/Dokumente und Einstellungen/wsp-shk1/Eigene Dateien/ParserTest/XSLTTest/outputs/eDocToRdfTest/2.rdf";
  private static final String RDF_FILE = "C:/Dokumente und Einstellungen/wsp-shk1/Eigene Dateien/ParserTest/XSLTTest/outputs/ModsToRdfTest/20090709-1-WS-BBAW.xml.rdf";

  public static void main(String[] args) throws ApplicationException {
//    System.out.println("extracted xml base: "+MetadataExtractorFactory.newRdfMetadataParser(RDF_FILE).getXmlBaseValue());
//    System.out.println(Constants.getInstance().getDocumentsDir());
    testAll();
  }
  
  public static void testAll() throws ApplicationException {
    File dir = new File("C:/Dokumente und Einstellungen/wsp-shk1/Eigene Dateien/ParserTest/XSLTTest/outputs/eDocToRdfTest");
    
    for (File f : dir.listFiles()) {
      System.out.println("File "+f+ ": "+MetadataExtractorFactory.newRdfMetadataParser(f.toString()).getXmlBaseValue());
    }
  }
}
