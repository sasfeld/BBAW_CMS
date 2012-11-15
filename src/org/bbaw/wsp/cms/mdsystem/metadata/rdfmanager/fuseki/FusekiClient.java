package org.bbaw.wsp.cms.mdsystem.metadata.rdfmanager.fuseki;


import org.apache.jena.fuseki.DatasetAccessor;
import org.apache.jena.fuseki.DatasetAccessorFactory;


import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;

/**
 * This (singleton) class offers methods to communicate with a Fuseki server. 
 * @author Sascha Feldmann (wsp-shk1)
 * @date 08.11.2012
 *
 */
public class FusekiClient {
  /**
   * The select-mode for a SparQl query.
   */
  public static final String MODE_SELECT = "SELECT";
  /**
   * This is the standard query, no named graph is specified within the incoming query.
   */
  private static final String NONE_GRAPH_SPECIFIED = "";
  /**
   * The fuseki endpoint to do a SparQl query. Will be concatenated to the dataset URL.
   */
  public static String ENDPOINT_QUERY = "/query";
  /**
   * The fuseki endpoint to do an update on a dataset. Will be concatenated to the dataset URL.
   */
  public static String ENDPOINT_UPDATE = "/update";
  /**
   * The fuseki endpoint to do a manipulate the dataset. Will be concatenated to the dataset URL.
   */
  public static String ENDPOINT_DATA = "/data";
  /**
   * The name of the default model (if not specifying a name for the graph).
   */
  public static String DEFAULT_NAMED_MODEL = "default";
  private static FusekiClient instance;
  
  /**
   * 
   * @return the only existing instance.
   */
  public static FusekiClient getInstance() {
    if(instance == null) {
      return new FusekiClient();
    }
    return instance;
  }
  /**
   * Execute a SparQl query on a remote fuseki server.
   * @param datasetUrl - the URL to the dataset on which the query will be done.
   * @param queryCommand - the SparQl query.
   * @return a {@link ResultSet} or null, e.g. if the mode doesn't return anything
   */
  public ResultSet performSelect(final String datasetUrl, final String queryCommand) {
    // perform a query on an unspecified (default) graph
    return performSelect(datasetUrl, queryCommand, NONE_GRAPH_SPECIFIED);   
  }
  
  /**
   * Execute a SparQl query on a remote fuseki server.
   * @param datasetUrl - the URL to the dataset on which the query will be done.
   * @param queryCommand - the SparQl query.
   * @param defaultGraphUri - the Uri of the default graph which will be queried.
   * @return a {@link ResultSet} or null, e.g. if the mode doesn't return anything
   */
  public ResultSet performSelect(final String datasetUrl, final String queryCommand, final String defaultGraphUri) {
     final String pathToQueryEndpoint = datasetUrl+ENDPOINT_QUERY;
     return queryServerWithDefaultGraph(pathToQueryEndpoint, queryCommand, MODE_SELECT, defaultGraphUri);        
  }

  /**
   * Perform the select query.
   * @param pathToQueryEndpoint
   * @param queryCommand
   * @param resultFormat
   * @param defaultGraph
   * @return a {@link ResultSet} containing the specified response.
   */
  private ResultSet queryServerWithDefaultGraph(final String pathToQueryEndpoint, final String queryCommand, final String resultFormat, final String defaultGraph) {
    Query q = QueryFactory.create(queryCommand);
    QueryExecution queryEx = QueryExecutionFactory.sparqlService(pathToQueryEndpoint, q, defaultGraph);
    if(resultFormat.equals(MODE_SELECT)) {
      ResultSet results = queryEx.execSelect(); // SELECT returns a ResultSet
      return results;    
    } 
    return null;    
  }

  /**
   * Put a model to a remote dataset. Consider, that an existing dataset will be replaced if you don't specify a modelName (for a named model).
   * If you don't prefer a named model, use {@link FusekiClient}.DEFAULT_NAMED_MODEL.
   * @param url - the URL to the dataset on which the model will be putted.
   * @param model - the Jena model 
   * @param modelName - the name of the model.
   */
  public void putModel(final String url, final Model model, final String modelName) {
    String pathToQueryEndpoint = url+ENDPOINT_DATA;
    DatasetAccessor accessor = DatasetAccessorFactory.createHTTP(pathToQueryEndpoint);
    accessor.putModel(modelName, model);
  }
  
  /**
   * Execute an update (manipulation) on a remote fuseki server.
   * @param datasetUrl - the URL to the dataset on which the update will be done.
   * @param updateCommand - the update Command (e.g. CLEAR DEFAULT)
   */
  public void performUpdate(final String datasetUrl, final String updateCommand) {
    String pathToUpdateEndpoint = datasetUrl+ENDPOINT_UPDATE;
    UpdateRequest request = UpdateFactory.create(updateCommand);
    UpdateProcessor proc = UpdateExecutionFactory.createRemote(request, pathToUpdateEndpoint);
    proc.execute(); // perform the update
  }
  
  public void deleteRecord(Model model, String command) {
    
  }
}
