package org.bbaw.wsp.cms.mdsystem.metadata.rdfmanager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bbaw.wsp.cms.mdsystem.metadata.rdfmanager.WspRdfStore;
import org.bbaw.wsp.cms.mdsystem.metadata.rdfmanager.RdfHandler;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.query.* ;


public class JenaMain {
    
	/**
	 * examples for test purposes
	 */
	static final String oreTestBriefe = "/home/juergens/WspEtc/rdfData/Briefe.rdf";
	static final String oreTestSaschas = "/home/juergens/WspEtc/rdfData/AvH-Briefwechsel-Ehrenberg-sascha.rdf";
	static final String oreBiblio = "/home/juergens/WspEtc/rdfData/AvHBiblio.rdf";
	static final String oreBiblioNeu = "/home/juergens/WspEtc/rdfData/BiblioNeu.rdf";
	
    private Model model;
	private RdfHandler manager;
	private WspRdfStore wspStore;
	private Dataset dataset; 
	
	/**
	 * needs to be instantiated from outside e.g. org.bbaw.wsp.cms.test.TestLocal
	 */
    public JenaMain(){
    	
    }
    
    public void initStore(){
    	wspStore = new WspRdfStore();
    	wspStore.createStore();
    	wspStore.createModelFactory();
    	//removeAll() performed?
    	dataset = wspStore.getDataset();
    	manager = new RdfHandler();
    	
    	doYourWork();
    }
    
    /**
     * call methods from here
     */
    private void doYourWork(){
//    	createNamedModelsFromOreSets("/home/juergens/WspEtc/rdfData/eDocToRdfTest");
    	getAllNamedModelsInDataset();
    	
    	model.close();
		dataset.close();
    }
    
    /**
     * creates single namedmodel from code
     * this should later be done by fuseki over http 
     * 
     */
    private void createNewModelFromSingleOre(String location){
    	
    	wspStore.openDataset();
    	Model freshModel = wspStore.getFreshModel();
    	Model model = manager.fillModelFromFile(freshModel, location);
    	String rdfAbout = manager.scanID(oreBiblioNeu);
    	wspStore.addNamedModelToWspStore(rdfAbout, model); 
    	wspStore.closeDataset();
    	
    }
    
    /**
     * creates a namedmodel from code
     * this should later be done by fuseki over http 
     * @param location
     */
    private void createNamedModelsFromOreSets(String location){
    	//read all mods rdf
    	List<String> pathList = extractPathLocally(location);
		System.out.println("pathlist : "+pathList.size());
		
		final long start = System.currentTimeMillis();
		wspStore.openDataset();
		for (String string : pathList) {
			Model freshsModel = wspStore.getFreshModel();
	    	Model m = manager.fillModelFromFile(freshsModel, string);
	    	String modsRdfAbout = manager.scanID(string);
	    	wspStore.addNamedModelToWspStore(modsRdfAbout, m); 	
		}
		System.out.println("set read in time elapsed : "+(System.currentTimeMillis()-start)/1000);
		wspStore.closeDataset();
		}
		
    /**
     * simply prints all named Models in a dataset
     */
    private void getAllNamedModelsInDataset(){
		wspStore.openDataset();
		Iterator<String> ite = dataset.listNames();
		while(ite.hasNext()){
			String s = ite.next();
			System.out.println("name : "+s);
		}
		wspStore.closeDataset();
    }

//    	wspStore.openDataset();
//
//    	dataset = wspStore.getDataset();
//		manager.count(dataset);
//    	Model model = dataset.getDefaultModel();
//	    wspStore.closeDataset();
	    
    /**
     * TODO
     */
    private void createInferenceModel(){
		//create inference model
    	wspStore.createInfModel(dataset.getDefaultModel());
    	System.out.println("infmodel : "+wspStore.getRdfsModel());
//    	 list the nicknames
        StmtIterator iter = wspStore.getRdfsModel().listStatements();
        while (iter.hasNext()) {
            System.out.println("    " + iter.nextStatement());
        }
    }
    
//        dataset = wspStore.getDataset();
//    	wspStore.openDataset();
//    	manager.count(dataset);
//        wspStore.closeDataset();
//		
    /**
     * try your sparql SELECT here
     * this should later be done by fuseki over http 
     */
    private void queryPerSparqlSelect(){
		wspStore.openDataset();
		String sparqlSelect = "PREFIX foaf:<http://xmlns.com/foaf/0.1/> SELECT ?familyName FROM NAMED <http://wsp.bbaw.de/oreTestBriefe> WHERE {?x foaf:familyName  ?familyName}";
		QueryExecution quExec = manager.selectSomething(sparqlSelect, dataset);
//        QueryExecution quExec = QueryExecutionFactory.create(
//        		"PREFIX foaf:<http://xmlns.com/foaf/0.1/> SELECT ?familyName FROM NAMED <http://wsp.bbaw.de/oreTestBriefe> WHERE {?x foaf:familyName  ?familyName}"
//        		, dataset) ;
        ResultSet res = quExec.execSelect() ;
        System.out.println("");
        System.out.println("_____________check_reults_______________");
        try{
        	ResultSetFormatter.out(res) ;
        } finally{quExec.close(); }
        wspStore.closeDataset();
    }
   
    /**
     * TODO
     * still considering if we really need this
     */
    private void reify(){ 
        //turn triples into quads
        wspStore.openDataset();
		Model modsModel = dataset.getNamedModel("http://wsp.bbaw.de/oreTestBriefe");
		StmtIterator stit = modsModel.listStatements();
		while(stit.hasNext()){
			Statement state = stit.next();
//		    System.out.println("statements : "+state);
			ReifiedStatement reifSt = state.createReifiedStatement("http://wsp.bbaw.de/oreTestBriefe");
			System.out.println("is reified : "+state.isReified());
			System.out.println("reified statement : "+reifSt.toString());
		}
		System.out.println(manager.getAllTriples(model));
    }
    
    /**
     * updates a namedModel
     */
    private void updateModelBySparqlInsert(){
        String updateIntoNamed = "PREFIX dc:  <http://purl.org/dc/elements/1.1/> " +
        		"INSERT DATA INTO <http://wsp.bbaw.de/oreTestBriefe>"+
        		" { <http://www.bbaw.de/posterDh> dc:title  \"Digital Knowledge Store\" } ";
        String updateToDefault = "PREFIX dc:  <http://purl.org/dc/elements/1.1/> " +
        		"INSERT DATA "+
        		" { <http://www.bbaw.de/posterDh> dc:title  \"Digital Knowledge Store\" } ";
        wspStore.openDataset();
        manager.performUpdate(dataset, updateToDefault);
        wspStore.closeDataset();
    }
    
    /**
     * get all statements by sparql SELECT from a single named Graph
     * this should later be done by fuseki over http 
     */
    private void queryAllBySelect(){
        wspStore.openDataset();
        //select all FROM
        QueryExecution quecExec = QueryExecutionFactory.create(
        		"SELECT * FROM NAMED <http://pom.bbaw.de:8085/exist/rest/db/wsp/avh/avhBib.xml> WHERE { GRAPH <http://pom.bbaw.de:8085/exist/rest/db/wsp/avh/avhBib.xml> { ?s ?p ?o } }", dataset) ;
        ResultSet resu = quecExec.execSelect() ;
        System.out.println("_____________check_reults_______________");
        try {
            ResultSetFormatter.out(resu) ;
        } finally { quecExec.close() ; }
        wspStore.closeDataset();
    }
    
    /**
     * same as queryAllBySelect() but without Sparql
     */
	private void queryAllStatementsFromJenaCode(){
        wspStore.openDataset();
        Model briefe = dataset.getNamedModel("http://wsp.bbaw.de/oreTestBriefe");
        StmtIterator erator = briefe.listStatements();
        while(erator.hasNext()){
        	Statement st = erator.next();
        	System.out.println("st : "+st);
        }
        wspStore.closeDataset();
	}
    	
	
    public void buildLuceneIndex(){
    	
    }
	
    /**
     * TODO
     * writes statement to dot format for visualization in graphviz
     * @param res
     */
	private void writeToDotLang(String res) {	
		try {
			OutputStream outputStream = new FileOutputStream(new File("oreTestBriefe.dot"));
			Writer writer = new OutputStreamWriter(outputStream);

			writer.write(res);

			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * helper method to get all paths from a set of rdf files
	 * @param startUrl
	 * @return
	 */
	public List<String> extractPathLocally(String startUrl) {
	    List<String> pathList = new ArrayList<String>();
	    // home verzeichnis pfad über system variable
	    // String loc = System.getenv("HOME")+"/wsp/configs";
	    // out.println("hom variable + conf datei : "+loc);
	    File f = new File(startUrl);
	    // out.println("readable : "+Boolean.toString(f.canRead()));
	    // out.println("readable : "+f.isDirectory());
	    if (f.isDirectory()) {
	      File[] filelist = f.listFiles();
	      for (File file : filelist) {
	        if (file.getName().toLowerCase().contains("rdf")) {
	        	if (!startUrl.endsWith("/")) {
	                pathList.add(startUrl + "/" + file.getName());
	              } else {
	                pathList.add(startUrl + file.getName());
	              }
	        }
	      }
	    }
	    return pathList;
	  }
	
}
