package org.bbaw.wsp.cms.test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.bbaw.wsp.cms.mdsystem.metadata.general.extractor.factory.MetadataExtractorFactory;
import org.bbaw.wsp.cms.mdsystem.metadata.rdfmanager.fuseki.FusekiServerHandler;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import de.mpg.mpiwg.berlin.mpdl.exception.ApplicationException;

public class TestFusekiServerHandler {
   public static void main(String[] args) throws ApplicationException, MalformedURLException, IOException {
//     eraseDataset();
//     putEdocRdfsToServer();
     executeQueries();
//     executeQueriesWithDefaultGraph();
   }

  private static void executeQueriesWithDefaultGraph() {
    final String url = "http://localhost:3030/ds";
    String defaultGraph = "http://edoc.bbaw.de/volltexte/2006/2/pdf/2565N0u5kOc.pdf";
//    String defaultGraph = "http://edoc.bbaw.de/volltexte/2006/1/pdf/29kstnGPLz2IM.pdf";
    ResultSet results = FusekiServerHandler.getInstance().executeSelectQuery(url, "SELECT * { ?s ?p ?o }", defaultGraph );
    ResultSetFormatter.out(results);    
  }

  private static void executeQueries() {
    final String url = "http://localhost:3030/ds";
//    ResultSet results = FusekiServerHandler.getInstance().executeSelectQuery(url, "SELECT * FROM NAMED <http://edoc.bbaw.de/volltexte/2006/1/pdf/29kstnGPLz2IM.pdf> { ?s ?p ?o }");
//    ResultSet results = FusekiServerHandler.getInstance().executeSelectQuery(url, "SELECT * { ?s ?p ?o }");
    ResultSet results = FusekiServerHandler.getInstance().executeSelectQuery(url, "SELECT * { ?s ?p ?o FILTER(STR(?o) = \"Englisch\" )}");
    ResultSetFormatter.out(results);
  }

  private static void eraseDataset() {
    final String url = "http://localhost:3030/ds";
    FusekiServerHandler.getInstance().executeUpdate(url, "CLEAR");    
  }

  private static void putEdocRdfsToServer() throws ApplicationException, MalformedURLException, IOException {
    final String url = "http://localhost:3030/ds";
    final String eDocDir = "C:/Dokumente und Einstellungen/wsp-shk1/Eigene Dateien/ParserTest/XSLTTest/outputs/eDocToRdfTest";    
    for (File f : new File(eDocDir).listFiles()) {
      System.out.println("adding model from file: "+f);
      Model model = ModelFactory.createDefaultModel();
      model.read(f.toURL().openStream(), null);
      String modelName = MetadataExtractorFactory.newRdfMetadataParser(f.toString()).getXmlBaseValue();
      FusekiServerHandler.getInstance().putModel(url, model , modelName );
    }
  }
}
