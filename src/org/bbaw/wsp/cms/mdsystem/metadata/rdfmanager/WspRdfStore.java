package org.bbaw.wsp.cms.mdsystem.metadata.rdfmanager;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB;

public class WspRdfStore {

	final String directory = "/home/juergens/wspTripleStore";
	private Dataset dataset;
	private Model defaultModel; 
	private InfModel rdfsModel;
	ModelMaker modelmaker;
	private List<String> modelList;
	DatasetGraphTDB dsdt;
	
	public WspRdfStore(){
		
	}
	
	public void createStore(){
    	System.out.println("cretae Store");
//    	Location loc = new Location(directory);
//    	StoreConnection sc = StoreConnection.make(loc);
//    	GraphTDB graphTdb = sc.begin(ReadWrite.WRITE).getDefaultGraphTDB();
//    	dataset = sc.begin(ReadWrite.WRITE).toDataset();
//    	 dsdt = sc.getBaseDataset();
//    	dataset = dsdt.toDataset();
    	dataset = TDBFactory.createDataset(directory);
    	defaultModel = dataset.getDefaultModel();
//    	defaultModel.removeAll();
    	modelList = new ArrayList<String>();
    	TDB.getContext().set(TDB.symUnionDefaultGraph, true);
//    	System.out.println("namedModel : ");
//    	StmtIterator iter = namedModel.listStatements();
//        while (iter.hasNext()) {
//            System.out.println("    " + iter.nextStatement());
//        }
//		model.removeAll();
    }
    
	public void createModelFactory(){
    	modelmaker = ModelFactory.createMemModelMaker();
	}
	
	public Model getFreshModel(){
		Model model = modelmaker.createFreshModel();
		return model;
	}
	
	public void addNamedModelToWspStore(String name, Model model){
		if (!this.dataset.containsNamedModel(name)) {
			if (model != null) {
				this.dataset.addNamedModel(name, model);
				modelList.add(name);
			}
		}
	}
	
	public void openDataset(){
		dataset.begin(ReadWrite.WRITE);
	}
	
	public void closeDataset(){
    	dataset.commit();
    	dataset.end();
	}
	
	public Model getMergedModelofAllGraphs(){
    	return dataset.getNamedModel("urn:x-arq:UnionGraph");
	}
	
	public void createInfModel(Model model){
		System.out.println("create Infmodel");
		if(model != null)
			rdfsModel = ModelFactory.createRDFSModel(model);
	}
	
	 public Model getRdfsModel() {
		 return this.rdfsModel;
	 }
	 
	 public Dataset getDataset() {
	     return this.dataset;
	 }
	 
	 public String getTripleCountOfDefaultModel() {
	        return "Default Graph contains: " + this.defaultModel.size() + " triples";
	    }
}
