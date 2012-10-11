package org.bbaw.wsp.cms.test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.Term;
import org.bbaw.wsp.cms.collections.Collection;
import org.bbaw.wsp.cms.collections.CollectionManager;
import org.bbaw.wsp.cms.collections.CollectionReader;
import org.bbaw.wsp.cms.dochandler.DocumentHandler;
import org.bbaw.wsp.cms.document.Hits;
import org.bbaw.wsp.cms.lucene.IndexHandler;
import org.bbaw.wsp.cms.scheduler.CmsDocOperation;
import org.bbaw.wsp.cms.transform.GetFragmentsContentHandler;
import org.bbaw.wsp.cms.transform.HighlightContentHandler;
import org.bbaw.wsp.cms.translator.GlosbeTranslator;
import org.bbaw.wsp.cms.translator.MicrosoftTranslator;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.memetix.mst.language.Language;
import com.sun.jndi.toolkit.url.Uri;
import com.sun.org.apache.xerces.internal.parsers.SAXParser;

import de.mpg.mpiwg.berlin.mpdl.exception.ApplicationException;
import de.mpg.mpiwg.berlin.mpdl.lt.dict.db.LexHandler;
import de.mpg.mpiwg.berlin.mpdl.lt.morph.app.MorphologyCache;
import de.mpg.mpiwg.berlin.mpdl.lt.text.tokenize.WordContentHandler;
import de.mpg.mpiwg.berlin.mpdl.lt.text.tokenize.XmlTokenizer;
import de.mpg.mpiwg.berlin.mpdl.lt.text.tokenize.XmlTokenizerContentHandler;

public class TestLocal {
  private IndexHandler indexer;

  public static void main(String[] args) throws ApplicationException {
    try {
      TestLocal test = new TestLocal();
      test.init();
      // test.testXml();
      // test.tokenizeXmlFragment();
      // test.getFragments("/Users/jwillenborg/tmp/writeFragments/Benedetti_1585.xml");
      // File srcFile = new File("/Users/jwillenborg/mpdl/data/xml/documents/echo/la/Benedetti_1585/pages/page-13-morph.xml");
      // String page13 = FileUtils.readFileToString(srcFile, "utf-8");
      // test.highlight(page13, "s", 6, "reg", "relatiuum");
      // test.queries();
      // test.translator2();
      // test.testCollectionReader();
      // test.createAllDocuments();
      // test.testCalls();
      // test.bla();
      test.end();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void init() throws ApplicationException {
    indexer = IndexHandler.getInstance();
  }
  
  private void end() throws ApplicationException {
    indexer.end();
  }

  private void bla() {
     String blabla = "huhu###haha###";
     String[] places = blabla.split("xxx");
     String bla = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<html><bla></bla></html>";
     bla = bla.replaceAll("<\\?xml.*?\\?>", "");
     bla = "<persName name=\"Friedrich Wilhelm III.\">König</persName>";
     bla = bla.replaceAll("<persName name=\"(.+)\".*>", "$1");
     bla = "<persName name=\"Friedrich Wilhelm III.\">König</persName>";
     bla = bla.replaceAll("<persName.*?>(.*)</persName>", "$1");
     String b = "";
     String url = "http://bla.com/test?bla bla";
     try {
       String encodedUri = URIUtil.encodeQuery(url, "utf-8");
       String c = "";
     } catch (Exception e) {
       e.printStackTrace();
     }
  }
  
  private void testXml() throws ApplicationException {
    try {
      File srcFile = new File("/Users/jwillenborg/mpdl/data/xml/documents/tei/de/dt-ptolemaeus-tei-merge2.xml");
      FileReader docFileReader = new FileReader(srcFile);
      XmlTokenizer docXmlTokenizer = new XmlTokenizer(docFileReader);
      docXmlTokenizer.setDocIdentifier("/tei/de/dt-ptolemaeus-tei-merge2.xml");
      docXmlTokenizer.tokenize();  
      ArrayList<XmlTokenizerContentHandler.Element> elements = docXmlTokenizer.getElements("s");
      String bla = "";
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  private void testCollectionReader() throws ApplicationException {
    CollectionReader confReader =  CollectionReader.getInstance();
    Collection registres = confReader.getCollection("registres");
    CollectionReader collReader = CollectionReader.getInstance();
    Collection avh = collReader.getCollection("AvH");
    String bla = "";
  }
  
  private void createAllDocuments() throws ApplicationException {
    CollectionManager collectionManager =  CollectionManager.getInstance();
    // collectionManager.updateCollections();
    // collectionManager.updateCollection("test", true);
    // collectionManager.updateCollection("AvH", true);
    // collectionManager.updateCollection("registres", true);
    // collectionManager.updateCollection("mes", true);
    // collectionManager.updateCollection("MEGA", true);
    collectionManager.updateCollection("IG", true);
  }
  
  private void testCalls() throws ApplicationException {
    Date before = new Date();
    System.out.println("Indexing start: " + before.getTime());
    DocumentHandler docHandler = new DocumentHandler();
    
    String docIdAvhNew = "/test/Dok280E18xml.xml";
    String docSrcUrlStr = "http://telota.bbaw.de:8085/exist/rest/db/AvHBriefedition/Briefe/Dok280E18xml.xml";
    CmsDocOperation docOperation = new CmsDocOperation("create", docSrcUrlStr, null, docIdAvhNew);
    docOperation.setCollectionNames("test");
    docOperation.setMainLanguage("deu");
    String[] elemNames = {"p", "s", "head"};
    docOperation.setElementNames(elemNames);
    // docHandler.doOperation(docOperation);
    
    String docIdMega = "/mega/docs/MEGA_A2_B001-01_ETX.xml";
    docSrcUrlStr = "http://telota.bbaw.de:8085/exist/rest/db/mega/docs/MEGA_A2_B001-01_ETX.xml";
    docOperation = new CmsDocOperation("create", docSrcUrlStr, null, docIdMega);
    docOperation.setCollectionNames("mega");
    docOperation.setMainLanguage("deu");
    docOperation.setElementNames(elemNames);
    // docHandler.doOperation(docOperation);
    
    String docIdGoerz = "/tei/de/dt-ptolemaeus-tei-merge2.xml";
    docSrcUrlStr = "http://mpdl-system.mpiwg-berlin.mpg.de/mpdl/getDoc?doc=" + docIdGoerz;
    docOperation = new CmsDocOperation("create", docSrcUrlStr, null, docIdGoerz);
    // docHandler.doOperation(docOperation);
    String docIdBenedetti = "/echo/la/Benedetti_1585.xml";
    docSrcUrlStr = "http://mpdl-system.mpiwg-berlin.mpg.de/mpdl/getDoc?doc=" + docIdBenedetti;
    docOperation = new CmsDocOperation("create", docSrcUrlStr, null, docIdBenedetti);
    // docHandler.doOperation(docOperation);
    String docIdAdams = "/echo/de/Adams_1785_S7ECRGW8.xml";
    docSrcUrlStr = "http://mpdl-system.mpiwg-berlin.mpg.de/mpdl/getDoc?doc=" + docIdAdams;
    docOperation = new CmsDocOperation("create", docSrcUrlStr, null, docIdAdams);
    // docHandler.doOperation(docOperation);
    String docIdMonte = "/archimedes/la/monte_mecha_036_la_1577.xml";
    docSrcUrlStr = "http://mpdl-system.mpiwg-berlin.mpg.de/mpdl/getDoc?doc=" + docIdMonte;
    docOperation = new CmsDocOperation("create", docSrcUrlStr, null, docIdMonte);
    // docHandler.doOperation(docOperation);
    String docIdEinstein = "/diverse/de/Einst_Antwo_de_1912.xml";
    docSrcUrlStr = "http://mpdl-system.mpiwg-berlin.mpg.de:30060/mpdl/getDoc?doc=" + docIdEinstein;
    docOperation = new CmsDocOperation("create", docSrcUrlStr, null, docIdEinstein);
    // docHandler.doOperation(docOperation);
    String docIdAvh = "/tei/de/Dok280E18xml.xml";
    docSrcUrlStr = "http://telota.bbaw.de:8085/exist/rest/db/AvHBriefedition/Briefe/Dok280E18xml.xml";
    docOperation = new CmsDocOperation("create", docSrcUrlStr, null, docIdAvh);
    // docHandler.doOperation(docOperation);
    // indexer.deleteDocument(docIdGoerz);
    // indexer.deleteDocument(docIdBenedetti);
    /*
    Date end = new Date();
    System.out.println("Indexing end: : " + end.getTime());
    String queryStr = "tokenOrig:tempore";
    System.out.println("Query: " + queryStr);
    // ArrayList<Document> docs = indexer.queryDocuments(queryField, queryStr);
    ArrayList<Document> docs = indexer.queryDocument(docIdMonte, queryStr);
    for (int i=0; i<docs.size(); i++) {
      Document doc = docs.get(i);
      Fieldable f = doc.getFieldable("docId");
      if (f != null) {
        String id = f.stringValue();
        System.out.print("<doc>" + id + "</doc>");
      }
      Fieldable fContent = doc.getFieldable("xmlContent");
      if (fContent != null) {
        String content = fContent.stringValue();
        System.out.print("<doc>" + content + "</doc>");
      }
    }
    System.out.println("");
    System.out.print("Browse documents: ");
    ArrayList<Term> terms = indexer.getTerms("docId", "", 1000);
    for (int i=0; i<terms.size(); i++) {
      Term term = terms.get(i);
      System.out.print(term + ", ");
    }
    System.out.print("Tokens in tokenOrig: ");
    ArrayList<Term> tokenTerms = indexer.getTerms("tokenOrig", "a", 100);
    for (int i=0; i<tokenTerms.size(); i++) {
      Term term = tokenTerms.get(i);
      System.out.print(term + ", ");
    }
    */
    MorphologyCache.getInstance().end();
    LexHandler.getInstance().end();
  }

  private Hashtable<Integer, StringBuilder> getFragments(String fileName) throws ApplicationException {
    try {
      GetFragmentsContentHandler getFragmentsContentHandler = new GetFragmentsContentHandler();
      XMLReader xmlParser = new SAXParser();
      xmlParser.setContentHandler(getFragmentsContentHandler);
      InputSource inputSource = new InputSource(fileName);
      xmlParser.parse(inputSource);
      Hashtable<Integer, StringBuilder> resultFragments = getFragmentsContentHandler.getResultPages();
      return resultFragments;
    } catch (SAXException e) {
      throw new ApplicationException(e);
    } catch (IOException e) {
      throw new ApplicationException(e);
    }
  }

  private String tokenizeXmlFragment() throws ApplicationException {
    String result = null;
    try {
      String xmlFragment = new String(FileUtils.readFileToByteArray(new File("/Users/jwillenborg/tmp/testFragment2.xml")), "utf-8");
      String srcUrlStr = "http://mpdl-system.mpiwg-berlin.mpg.de/mpdl/page-query-result.xql?document=/echo/la/Benedetti_1585.xml&mode=pureXml&pn=13";
      URL srcUrl = new URL(srcUrlStr);
      InputStream inputStream = srcUrl.openStream();
      BufferedInputStream in = new BufferedInputStream(inputStream);
      xmlFragment = IOUtils.toString(in, "utf-8");
      in.close();

      XmlTokenizer xmlTokenizer = new XmlTokenizer(new StringReader(xmlFragment));
      xmlTokenizer.setLanguage("lat");
      String[] stopElements = {"var"};
      // xmlTokenizer.setOutputFormat("string");
      String[] outputOptions = {"withLemmas"};
      xmlTokenizer.setOutputOptions(outputOptions);
      xmlTokenizer.setStopElements(stopElements);
      xmlTokenizer.tokenize();
      result = xmlTokenizer.getXmlResult();
      System.out.println(result);
    } catch (Exception e) {
      throw new ApplicationException(e);
    }
    return result;
  }
  
  private String normalizeWords(String xmlStr) throws ApplicationException {
    try {
      WordContentHandler wordContentHandler = new WordContentHandler("norm");
      XMLReader xmlParser = new SAXParser();
      xmlParser.setContentHandler(wordContentHandler);
      StringReader strReader = new StringReader(xmlStr);
      InputSource inputSource = new InputSource(strReader);
      xmlParser.parse(inputSource);
      String result = wordContentHandler.getResult();
      return result;
    } catch (SAXException e) {
      throw new ApplicationException(e);
    } catch (IOException e) {
      throw new ApplicationException(e);
    }
  }
  
  private String highlight(String xmlStr, String highlightElem, int highlightElemPos, String highlightQueryType, String highlightQuery, String language) throws ApplicationException {
    String result = null;
    try {
      xmlStr = normalizeWords(xmlStr);
      HighlightContentHandler highlightContentHandler = new HighlightContentHandler(highlightElem, highlightElemPos, highlightQueryType, highlightQuery, language);
      highlightContentHandler.setFirstPageBreakReachedMode(true);
      XMLReader xmlParser = new SAXParser();
      xmlParser.setContentHandler(highlightContentHandler);
      StringReader stringReader = new StringReader(xmlStr);
      InputSource inputSource = new InputSource(stringReader);
      xmlParser.parse(inputSource);
      result = highlightContentHandler.getResult().toString();
    } catch (SAXException e) {
      throw new ApplicationException(e);
    } catch (IOException e) {
      throw new ApplicationException(e);
    }
    return result;
  }
  
  private void translator() throws ApplicationException {
    try {
      Language lang = Language.fromString("de");
      String languageCode = MicrosoftTranslator.detectLanguageName("café car");
      String bla = "";
    } catch (Exception e) {
      throw new ApplicationException(e);
    }
  }
  
  private void translator2() throws ApplicationException {
    try {
      String[] query = {"haus", "moor"};
      String[] translations = GlosbeTranslator.getInstance().translate(query, "deu", "eng");
      String[] translations2 = GlosbeTranslator.getInstance().translate(query, "deu", "fra");
      String lang = GlosbeTranslator.getInstance().detectLanguageCode("haus");
      lang = GlosbeTranslator.getInstance().detectLanguageCode("house");
      lang = GlosbeTranslator.getInstance().detectLanguageCode("maison");
      lang = GlosbeTranslator.getInstance().detectLanguageCode("ZZZZZZZZ");
      String bla = "";
    } catch (Exception e) {
      throw new ApplicationException(e);
    }
  }

  private void queries() throws ApplicationException {
    Hits docsss = indexer.queryDocuments("tokenMorph:haus", null, "eng", 1, 10, true, true);
    String docId = "/mega/docs/MEGA_A2_B001-01_ETX.xml";
    // String query = "mod_date:[20020101 TO 20030101]";
    // String query = "tokenOrig:\"Haben beide\"~2";
    // String query = "tokenOrig:Habe~";
    // String query = "tokenOrig:\"Haben Sie beide\"";
    // String query = "+tokenMorph:gebrauchen +tokenMorph:schmutzig";
    // String query = "title:a*";
    // String query = "tokenMorph:gebrauchen AND tokenMorph:schmutzig";
    String query = "tokenMorph:wird";
    // Hits docsss = indexer.queryDocuments("tokenOrig:\"politischen Oekonomie\"", null, 1, 10, false, false);
    // Hits docsss = indexer.queryDocuments("tokenMorph:wird", null, 1, 10, true, false);
    Hits persHits = indexer.queryDocument("/mes/mes/data/MzE_7_2.xml", "elementName:persName", 0, 100);
    ArrayList<org.bbaw.wsp.cms.document.Document> namesList = persHits.getHits();
    for (org.bbaw.wsp.cms.document.Document nameDoc : namesList) {
      Fieldable docPersNameField = nameDoc.getFieldable("xmlContent");
      String docPersName = docPersNameField.stringValue();
      docPersName = docPersName.replaceAll("\\n", "");
      String persNameAttribute = docPersName; 
      if(persNameAttribute.contains("persName nymRef"))
        persNameAttribute = docPersName.replaceAll("<persName nymRef=\"(.+?)\".+?</persName>", "$1");
      if(persNameAttribute.contains("persName name="))
        persNameAttribute = docPersName.replaceAll("<persName name=\"(.+?)\".+?</persName>", "$1");
      if(persNameAttribute.contains("persName key="))
        persNameAttribute = docPersName.replaceAll("<persName.*?>(.*?)</persName>", "$1");
      persNameAttribute = persNameAttribute.replaceAll("<persName.*?>(.*?)</persName>", "$1");
      persNameAttribute = persNameAttribute.trim();
    }    
    // ArrayList<String> terms = indexer.fetchTerms(query, "de");
    Hits docs = indexer.queryDocument(docId, query, 1, 1000);
    // docs = indexer.queryDocument("/tei/de/Dok280E18xml.xml", "+elementName:persName +tokenOrig:alexander", 0, 1000);
    String bla = "";
  }
}