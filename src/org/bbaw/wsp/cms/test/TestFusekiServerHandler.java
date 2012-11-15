package org.bbaw.wsp.cms.test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.bbaw.wsp.cms.mdsystem.metadata.general.extractor.factory.MetadataExtractorFactory;
import org.bbaw.wsp.cms.mdsystem.metadata.rdfmanager.fuseki.FusekiClient;
import org.bbaw.wsp.cms.mdsystem.metadata.rdfmanager.fuseki.SparQlStore;


import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import de.mpg.mpiwg.berlin.mpdl.exception.ApplicationException;

/**
 * Several tests of the {@link FusekiHandler}.
 * @author Sascha Feldmann (wsp-shk1)
 *
 */
public class TestFusekiServerHandler {
   public static void main(String[] args) throws ApplicationException, MalformedURLException, IOException {
//     eraseDataset();
//     putEdocRdfsToServer();
//     executeQueries();
     executeQueriesWithDefaultGraph();
   }

  private static void executeQueriesWithDefaultGraph() {
    final String url = "http://localhost:3030/ds";
    String defaultGraph = "";
//    String defaultGraph = "http://edoc.bbaw.de/volltexte/2006/1/pdf/29kstnGPLz2IM.pdf";
    String selectQuery = SparQlStore.SELECT_DEFAULT.getSelectQueryString("*", "", "?s ?p ?o");
    ResultSet results = FusekiClient.getInstance().performSelect(url, selectQuery, defaultGraph );
//    Model m = results.getResourceModel();
    ResultSetFormatter.out(results);    
  }
//
  private static void executeQueries() {
    final String url = "http://localhost:3030/ds";
    String query = SparQlStore.SELECT_NAMED.getSelectQueryString("*", "http://edoc.bbaw.de/volltexte/2006/1/pdf/29kstnGPLz2IM.pdf", "?s ?p ?o");
    ResultSet results = FusekiClient.getInstance().performSelect(url, query);
//    ResultSet results = FusekiHandler.getInstance().executeSelectQuery(url, "SELECT * { ?s ?p ?o }");
//    ResultSet results = FusekiServerHandler.getInstance().executeSelectQuery(url, "SELECT * { ?s ?p ?o FILTER(STR(?o) = \"Englisch\" )}");
    ResultSetFormatter.out(results);
  }

  private static void eraseDataset() {
    final String url = "http://localhost:3030/ds";
    String command = SparQlStore.CLEAR_DATASET.getUpdateCommandString();
    FusekiClient.getInstance().performUpdate(url, command);    
  }

  private static void putEdocRdfsToServer() throws ApplicationException, MalformedURLException, IOException {
    final String url = "http://localhost:3030/ds";
    final String eDocDir = "C:/Dokumente und Einstellungen/wsp-shk1/Eigene Dateien/ParserTest/XSLTTest/outputs/eDocToRdfTest";    
    for (File f : new File(eDocDir).listFiles()) {
      System.out.println("adding model from file: "+f);
      Model model = ModelFactory.createDefaultModel();
      model.read(f.toURL().openStream(), null);
      String modelName = MetadataExtractorFactory.newRdfMetadataParser(f.toString()).getXmlBaseValue();
      FusekiClient.getInstance().putModel(url, model , modelName );
    }
  }
}
