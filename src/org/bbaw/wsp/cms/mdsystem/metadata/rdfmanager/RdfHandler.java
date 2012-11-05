package org.bbaw.wsp.cms.mdsystem.metadata.rdfmanager;

import java.util.Map;
import java.util.Set;

import org.bbaw.wsp.cms.mdsystem.metadata.general.extractor.RdfMetadataExtractor;
import org.bbaw.wsp.cms.mdsystem.metadata.general.extractor.factory.MetadataExtractorFactory;
import org.openjena.riot.Lang;
import org.openjena.riot.RiotLoader;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.GraphStoreFactory;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;
import com.hp.hpl.jena.util.FileManager;

import de.mpg.mpiwg.berlin.mpdl.exception.ApplicationException;

public class RdfHandler {

	
	/**
	 * 
	 * @param store
	 * @return
	 */
	public String getAllTriples(Model model) {
        StringBuffer dump = new StringBuffer();

        StmtIterator statements = model.listStatements();
        while (statements.hasNext()) {
            Statement statement = statements.nextStatement();
            RDFNode object = statement.getObject();
            dump.append(statement.getSubject().getLocalName())
                .append(' ')
                .append(statement.getPredicate().getLocalName())
                .append(' ')
                .append((object instanceof Resource || object instanceof Literal ?
                     object.toString() : '"' + object.toString() + "\""))
                .append('\n');
        }
        return dump.toString();
    }
	
	/**
	 * 
	 * 
	 * @param store
	 * @return
	 */
	public String getAllTriplesAsDot(Model model) {
        StringBuffer dump = new StringBuffer();

        StmtIterator statements = model.listStatements();
        dump.append("digraph G { ");
        dump.append('\n');
        dump.append("edge [len=2];");
        dump.append('\n');
        while (statements.hasNext()) {
            Statement statement = statements.nextStatement();
        	System.out.println("statements.nextStatement() "+statement);
            RDFNode object = statement.getObject();
            dump.append("\"")
            	.append(statement.getSubject().getLocalName())
                .append("\" ")
                .append("-> \"")
                .append((object instanceof Resource || object instanceof Literal ?
                     object.toString() : '"' + object.toString() + "\""))
                .append("\"")
                .append(" [label=\"")
                .append(statement.getPredicate().getLocalName())
                .append("\"]")
                .append(";")
                .append('\n');
        }
        dump.append('}');
        return dump.toString();
    }
	
	
	/**
	 * INSERT
	 * @param store
	 * @param queryString
	 */
	public void performUpdate(Dataset dataset, String queryString){
		GraphStore graphStore = GraphStoreFactory.create(dataset) ;
        
		  UpdateRequest request = UpdateFactory.create(queryString) ;
	        UpdateProcessor proc = UpdateExecutionFactory.create(request, graphStore) ;
	        proc.execute() ;
		
//		System.out.println("model : "+model);
//		System.out.println("query : "+queryString);
		
	}
	
	/**
	 *  counts triple in a defaultgraph of the dataset
	 * @param dataset
	 */
	public void count(Dataset dataset){
		QueryExecution qExec = QueryExecutionFactory.create(
        		"SELECT (count(*) AS ?count) { ?s ?p ?o} LIMIT 10", dataset) ;
        ResultSet rs = qExec.execSelect() ;
        System.out.println("");
        System.out.println("_____________triples im defaultgraph_______________");
        try {
            ResultSetFormatter.out(rs) ;
        } finally { qExec.close() ; }
		
	}
	
	/**
	 * 
	 * @param sparqlSelect
	 * @param dataset
	 * @return
	 */
	public QueryExecution selectSomething(String sparqlSelect, Dataset dataset){
		QueryExecution queExec = QueryExecutionFactory.create(sparqlSelect, dataset) ;
        return queExec;
	}
	
	/**
	 * fill Graph
	 * @param store
	 */
	public Model fillModelFromFile(Model model, String loc){
		Model moodel = FileManager.get().readModel(model, loc);
		return moodel;
	}
	
	/**
	* takes a file and scans it for the about id
	*
	* @param file
	* @return String as like as ID of the file
	*/

	public String scanID(final String file) {
		try {
			RdfMetadataExtractor fac = MetadataExtractorFactory.newRdfMetadataParser(file);
			String test = fac.getRdfAboutValue();
			return test;

		} catch (ApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
	
	public void readQuadsViaRiot(String filename, DatasetGraph dataset, Lang lang, String baseURI){
		RiotLoader.read(filename, dataset, lang, baseURI);
	}
	
	public void readFileViaRiot(String filename, DatasetGraph dataset, Lang lang, String baseURI){
		RiotLoader.read(filename, dataset, lang, baseURI);
	}

	/**
	 * TODO
	 * DELETE
	 * @param store
	 * @param queryString
	 */
	public void deleteRecord(Model model, String queryString){

		GraphStore graphStore = GraphStoreFactory.create(model);
		queryString = "DELETE WHERE { <http://avh.bbaw.de/biblio/Aggr> ?p ?o }";
				
				
		/**
		 * 	PREFIX dc: <http://purl.org/dc/elements/1.1/>

			DELETE DATA FROM <http://example/bookStore>
			{ <http://example/book3>  dc:title  "Fundamentals of Compiler Desing" }
		 */
		
	}
	
	/**
	 * TODO
	 * DELETE all
	 */
	public void deleteAll(){
		/**
		 * 
		 * 	DELETE
			 { ?book ?p ?v }
			WHERE
			  { ?book dc:date ?date .
			    FILTER ( ?date < "2000-01-01T00:00:00"^^xsd:dateTime )
			    ?book ?p ?v
			  }
		 * 
		 */
	}
	
	/**
	 * TODO
	 * copy
	 */
	public void copyRecords(){
		/**
		 * von einem namedGraph zu einem anderen
		 * 
		 * 	PREFIX dc:  <http://purl.org/dc/elements/1.1/>
			PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
			
			INSERT INTO <http://example/bookStore2>
			 { ?book ?p ?v }
			WHERE
			  { GRAPH  <http://example/bookStore>
			       { ?book dc:date ?date .
			         FILTER ( ?date < "2000-01-01T00:00:00"^^xsd:dateTime )
			         ?book ?p ?v
			  } }	
		 * 
		 */
	}
	
	/**
	 * TODO
	 */
	public void moveRecords(){
		/**
		 * 
		 * 	PREFIX dc:  <http://purl.org/dc/elements/1.1/>
			PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
			
			INSERT INTO <http://example/bookStore2>
			 { ?book ?p ?v }
			WHERE
			  { GRAPH  <http://example/bookStore>
			     { ?book dc:date ?date . 
			       FILTER ( ?date < "2000-01-01T00:00:00"^^xsd:dateTime )
			       ?book ?p ?v
			     }
			  }
			
			DELETE FROM <http://example/bookStore>
			 { ?book ?p ?v }
			WHERE
			  { GRAPH  <http://example/bookStore>
			      { ?book dc:date ?date . 
			        FILTER ( ?date < "2000-01-01T00:00:00"^^xsd:dateTime ) 
			        ?book ?p ?v
			      } 
			  }   	
		 * 
		 */
	}
	
	
	/**
	* Helper method that splits up a URI into a namespace and a local part.
	* It uses the prefixMap to recognize namespaces, and replaces the
	* namespace part by a prefix.
	*
	* @param prefixMap
	* @param resource
	*/
	public static String[] split(PrefixMapping prefixMap, Resource resource) {
	    String uri = resource.getURI();
	    if (uri == null) {
	        return new String[] {null, null};
	    }
	    Map<String,String> prefixMapMap = prefixMap.getNsPrefixMap();
	    Set<String> prefixes = prefixMapMap.keySet();
	    String[] split = { null, null };
	    for (String key : prefixes){
	        String ns = prefixMapMap.get(key);
	        if (uri.startsWith(ns)) {
	            split[0] = key;
	            split[1] = uri.substring(ns.length());
	            return split;
	        }
	    }
	    split[1] = uri;
	    return split;
    }

//	public void navigateTest(){
//		// create an empty model
//        Model model = ModelFactory.createDefaultModel();
//       
//        // use the FileManager to find the input file
//        InputStream in = FileManager.get().open(oreTest);
//        if (in == null) {
//            throw new IllegalArgumentException( "File: " + oreTest + " not found");
//        }
//        
//        // read the RDF/XML file
//        model.read(new InputStreamReader(in), "");
//        
//        // retrieve the Adam Smith vcard resource from the model
//        Resource descr = model.getResource("http://avh.bbaw.de/biblio/Aggr");
//        
//        Resource statem = (Resource )descr.getRequiredProperty(DC.title).getObject();
//        
//        System.out.println("statem : "+statem);
//        
//        // retrieve the value of the N property
////        Resource name = (Resource) descr.getRequiredProperty(DCTerms.creator).getObject();
//        
//        Statement s = descr.getProperty(DCTerms.creator);
//        System.out.println("as triple  : "+s.asTriple());
//        System.out.println("creator s ? : "+s.getSubject());
//        System.out.println("creator p ? : "+s.getPredicate());
//        System.out.println("creator o ? : "+s);
//        
//        if (s instanceof Resource) {
//        	System.out.println("creator o ? : "+s);
//         } else {
//             // object is a literal
//             System.out.println(" \"" + s.toString() + "\"");
//         }
//        
//        // retrieve the given name property
//        String rights = descr.getRequiredProperty(DC.rights).getString();
//        
//        // add two nick name properties to vcard
//        descr.addProperty(DCTerms.format, "PDF");
//        
//        // set up the output
//        System.out.println("The rights of \"" + rights + "\" are:");
//        // list the nicknames
//        StmtIterator iter = descr.listProperties(DCTerms.format);
//        while (iter.hasNext()) {
//            System.out.println("    " + iter.nextStatement().getObject().toString());
//        }
//	}
	   
}
