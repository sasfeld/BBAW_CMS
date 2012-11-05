package org.bbaw.wsp.cms.dochandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.FileNameMap;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.logging.Logger;

import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmSequenceIterator;

import org.apache.commons.io.FileUtils;
import org.bbaw.wsp.cms.dochandler.parser.document.IDocument;
import org.bbaw.wsp.cms.dochandler.parser.text.parser.DocumentParser;
import org.bbaw.wsp.cms.document.MetadataRecord;
import org.bbaw.wsp.cms.document.XQuery;
import org.bbaw.wsp.cms.general.Constants;
import org.bbaw.wsp.cms.lucene.IndexHandler;
import org.bbaw.wsp.cms.scheduler.CmsDocOperation;
import org.bbaw.wsp.cms.transform.GetFragmentsContentHandler;
import org.bbaw.wsp.cms.transform.XslResourceTransformer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.sun.org.apache.xerces.internal.parsers.SAXParser;

import de.mpg.mpiwg.berlin.mpdl.exception.ApplicationException;
import de.mpg.mpiwg.berlin.mpdl.lt.general.Language;
import de.mpg.mpiwg.berlin.mpdl.lt.text.tokenize.Token;
import de.mpg.mpiwg.berlin.mpdl.lt.text.tokenize.XmlTokenizer;
import de.mpg.mpiwg.berlin.mpdl.lt.text.tokenize.XmlTokenizerContentHandler;
import de.mpg.mpiwg.berlin.mpdl.util.StringUtils;
import de.mpg.mpiwg.berlin.mpdl.util.Util;
import de.mpg.mpiwg.berlin.mpdl.xml.xquery.XQueryEvaluator;

/**
 * Handler for documents (singleton). 
 */
public class DocumentHandler {
  private static Logger LOGGER = Logger.getLogger(DocumentHandler.class.getName());
  
  public void doOperation(CmsDocOperation docOperation) throws ApplicationException{
    String operationName = docOperation.getName();  
    if (operationName.equals("create")) {
      create(docOperation);
    } else if (operationName.equals("delete")) {
      delete(docOperation);
    }
  }
  
  private void create(CmsDocOperation docOperation) throws ApplicationException {
    try {
      String operationName = docOperation.getName();  
      String srcUrlStr = docOperation.getSrcUrl(); 
      String docId = docOperation.getDocIdentifier();
      String mainLanguage = docOperation.getMainLanguage();
      String[] elementNames = docOperation.getElementNames();
      if (elementNames == null) {
        String[] defaultElementNames = {"persName", "placeName", "p", "s", "head"};
        docOperation.setElementNames(defaultElementNames); // default
      }
      String docDirName = getDocDir(docId);
      String docDestFileName = getDocFullFileName(docId); 
      boolean docIsXml = isDocXml(docId); 
      URL srcUrl = null;
      String protocol = null;
      if (srcUrlStr != null && ! srcUrlStr.equals("empty")) {
        srcUrl = new URL(srcUrlStr);
        protocol = srcUrl.getProtocol();
      }
      File docDestFile = new File(docDestFileName);
      // perform operation on file system
      if (protocol.equals("file")) {
        docOperation.setStatus("upload file: " + srcUrlStr + " to CMS");
      } else {
        docOperation.setStatus("download file from: " + srcUrlStr + " to CMS");
      }
      FileUtils.copyURLToFile(srcUrl, docDestFile, 100000, 100000);
      MetadataRecord mdRecord = docOperation.getMdRecord();
      mdRecord.setLastModified(new Date());
      String mimeType = getMimeType(docId);
      mdRecord.setType(mimeType);
      // document is of type XML
      if (docIsXml) {
        // parse validation on file
        XQueryEvaluator xQueryEvaluator = new XQueryEvaluator();
        XdmNode docNode = xQueryEvaluator.parse(srcUrl); // if it is not parseable an exception with a detail message is thrown 
        String docType = getNodeType(docNode);
        docType = docType.trim();
        if (docType == null) {
          FileUtils.deleteQuietly(docDestFile);
          docOperation.setErrorMessage("file type of: " + srcUrlStr + "is not supported");
          return;
        }
        // replace anchor in echo documents and also add the number attribute to figures
        String docDestFileNameUpgrade = docDestFileName + ".upgrade";
        File docDestFileUpgrade = new File(docDestFileNameUpgrade);
        XslResourceTransformer replaceAnchorTransformer = new XslResourceTransformer("replaceAnchor.xsl");
        String docDestFileUrlStr = docDestFile.getPath();
        String result = replaceAnchorTransformer.transform(docDestFileUrlStr);
        FileUtils.writeStringToFile(docDestFileUpgrade, result, "utf-8");
        
        // generate toc file (toc, figure, handwritten, persons, places, pages)
        XslResourceTransformer tocTransformer = new XslResourceTransformer("toc.xsl");
        File tocFile = new File(docDirName + "/toc.xml");
        String tocResult = tocTransformer.transform(docDestFileNameUpgrade);
        FileUtils.writeStringToFile(tocFile, tocResult, "utf-8");
        String persons = getPersons(tocResult, xQueryEvaluator); 
        String places = getPlaces(tocResult, xQueryEvaluator);
  
        // Get metadata info out of the xml document
        mdRecord.setPersons(persons);
        mdRecord.setPlaces(places);
        docOperation.setStatus("extract metadata of: " + srcUrlStr + " to CMS");
        mdRecord = getMetadataRecord(docDestFileUpgrade, docType, mdRecord, xQueryEvaluator);
        String mdRecordLanguage = mdRecord.getLanguage();
        String langId = Language.getInstance().getLanguageId(mdRecordLanguage); // test if language code is supported
        if (langId == null)
          mdRecordLanguage = null;
        if (mdRecordLanguage == null && mainLanguage != null)
          mdRecord.setLanguage(mainLanguage);
          
        // save all pages as single xml files (untokenized and tokenized)
        docOperation.setStatus("extract page fragments of: " + srcUrlStr + " to CMS");
        File docDir = new File(docDirName + "/pages");
        FileUtils.deleteQuietly(docDir);  // first delete pages directory
        Hashtable<Integer, StringBuilder> pageFragments = getFragments(docDestFileNameUpgrade, "pb");
        int pageCount = pageFragments.size();
        if (pageCount == 0) {
          // no pb element is found: then the whole document is the first page
          String docXmlStr = FileUtils.readFileToString(docDestFileUpgrade, "utf-8");
          docXmlStr = docXmlStr.replaceAll("<\\?xml.*?\\?>", "");  // remove the xml declaration if it exists
          pageFragments = new Hashtable<Integer, StringBuilder>();
          pageFragments.put(new Integer(1), new StringBuilder(docXmlStr));
          pageCount = 1;
        }
        for (int page=1; page<=pageCount; page++) {
          String fragment = pageFragments.get(new Integer(page)).toString();
          fragment = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + fragment;
          String docPageFileName = docDirName + "/pages/page-" + page + ".xml";
          File docPageFile = new File(docPageFileName);
          FileUtils.writeStringToFile(docPageFile, fragment, "utf-8");
          String language = mdRecord.getLanguage();
          String tokenizedXmlStr = tokenizeWithLemmas(fragment, language);
          tokenizedXmlStr = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + tokenizedXmlStr;
          String docPageTokenizedFileName = docDirName + "/pages/page-" + page + "-morph.xml";
          File docPageTokenizedFile = new File(docPageTokenizedFileName);
          FileUtils.writeStringToFile(docPageTokenizedFile, tokenizedXmlStr, "utf-8");
        }
      } 
      // build the documents fulltext fields
      buildFulltextFields(docOperation);
      // perform operation on Lucene
      docOperation.setStatus(operationName + " document: " + docId + " in CMS");
      IndexHandler indexHandler = IndexHandler.getInstance();
      indexHandler.indexDocument(docOperation);
    } catch (IOException e) {
      throw new ApplicationException(e);
    }
  }
  
  private void delete(CmsDocOperation docOperation) throws ApplicationException {
    String operationName = docOperation.getName();  
    String docIdentifier = docOperation.getDocIdentifier();
    if (docIdentifier == null || docIdentifier.trim().equals(""))
      throw new ApplicationException("Your document identifier is empty. Please specify a document identifier for your document.");
    String docDirStr = getDocDir(docIdentifier);
    File docDir = new File(docDirStr);
    boolean docExists = docDir.exists();
    if (! docExists) {
      throw new ApplicationException("Document:" + docIdentifier + " does not exists. Please use a name that exists and perform the operation \"Delete\" again.");
    }
    // perform operation on file system
    docOperation.setStatus(operationName + " document: " + docIdentifier + " in CMS");
    FileUtils.deleteQuietly(docDir);
      
    // perform operation on Lucene
    IndexHandler indexHandler = IndexHandler.getInstance();
    indexHandler.deleteDocument(docOperation);
  }
  
  private void buildFulltextFields(CmsDocOperation docOperation) throws ApplicationException {
    try {
      MetadataRecord mdRecord = docOperation.getMdRecord();
      String docId = mdRecord.getDocId();
      String language = mdRecord.getLanguage();
      boolean docIsXml = isDocXml(docId);
      String docTokensOrig = null;
      String docTokensNorm = null;
      String docTokensMorph = null;
      String contentXml = null;
      String content = null;
      String docFileName = getDocFullFileName(docId);
      XmlTokenizer docXmlTokenizer = null;
      if (docIsXml) {
        docFileName = getDocFullFileName(docId) + ".upgrade";
        InputStreamReader docFileReader = new InputStreamReader(new FileInputStream(docFileName), "utf-8");
        // to guarantee that utf-8 is used (if not done, it does not work on Tomcat which has another default charset)
        docXmlTokenizer = new XmlTokenizer(docFileReader);
        docXmlTokenizer.setDocIdentifier(docId);
        docXmlTokenizer.setLanguage(language);
        docXmlTokenizer.setOutputFormat("string");
        String[] outputOptionsWithLemmas = { "withLemmas" }; // so all tokens are
        // fetched with lemmas (costs performance)
        docXmlTokenizer.setOutputOptions(outputOptionsWithLemmas);
        String[] normFunctionNone = { "none" };
        docXmlTokenizer.setNormFunctions(normFunctionNone);
        docXmlTokenizer.tokenize();
  
        int pageCount = docXmlTokenizer.getPageCount();
        if (pageCount <= 0)
          pageCount = 1;  // each document at least has one page
  
        String[] outputOptionsEmpty = {};
        docXmlTokenizer.setOutputOptions(outputOptionsEmpty); 
        // must be set to null so that the normalization function works
        docTokensOrig = docXmlTokenizer.getStringResult();
        String[] normFunctionNorm = { "norm" };
        docXmlTokenizer.setNormFunctions(normFunctionNorm);
        docTokensNorm = docXmlTokenizer.getStringResult();
        docXmlTokenizer.setOutputOptions(outputOptionsWithLemmas);
        docTokensMorph = docXmlTokenizer.getStringResult();
        // fetch original xml content of the documents file
        File docFile = new File(docFileName);
        contentXml = FileUtils.readFileToString(docFile, "utf-8");
        // fetch original content of the documents file (without xml tags)
        XslResourceTransformer charsTransformer = new XslResourceTransformer("chars.xsl");
        content = charsTransformer.transform(docFileName);
        // get elements from xml tokenizer 
        String[] elementNamesArray = docOperation.getElementNames();
        String elementNames = "";
        for (int i = 0; i < elementNamesArray.length; i++) {
          String elemName = elementNamesArray[i];
          elementNames = elementNames + elemName + " ";
        }
        elementNames = elementNames.substring(0, elementNames.length() - 1);
        ArrayList<XmlTokenizerContentHandler.Element> xmlElements = docXmlTokenizer.getElements(elementNames);
        // fill mdRecord
        mdRecord.setTokenOrig(docTokensOrig);
        mdRecord.setTokenNorm(docTokensNorm);
        mdRecord.setTokenMorph(docTokensMorph);
        mdRecord.setContentXml(contentXml);
        mdRecord.setContent(content);
        mdRecord.setXmlElements(xmlElements);
        mdRecord.setPageCount(pageCount);
      } else {
        DocumentParser tikaParser = new DocumentParser();
        try {
          IDocument tikaDoc = tikaParser.parse(docFileName);
          docTokensOrig = tikaDoc.getTextOrig();
          MetadataRecord tikaMDRecord = tikaDoc.getMetadata();
          if (tikaMDRecord != null) {
            int pageCount = tikaMDRecord.getPageCount();
            if (pageCount <= 0)
              pageCount = 1;  // each document at least has one page
            mdRecord.setPageCount(pageCount);
          }
        } catch (ApplicationException e) {
          LOGGER.severe(e.getLocalizedMessage());
        }
        String mdRecordLanguage = mdRecord.getLanguage();
        String mainLanguage = docOperation.getMainLanguage();
        if (mdRecordLanguage == null && mainLanguage != null)
          mdRecord.setLanguage(mainLanguage);
        String lang = mdRecord.getLanguage();
        DocumentTokenizer docTokenizer = DocumentTokenizer.getInstance();
        String[] normFunctions = {"norm"};
        ArrayList<Token> normTokens = docTokenizer.getToken(docTokensOrig, lang, normFunctions);
        docTokensNorm = docTokenizer.buildStr(normTokens, lang, "norm");
        docTokensMorph = docTokenizer.buildStr(normTokens, lang, "morph");
        content = docTokensOrig;  // content is the same as docTokensOrig
        // fill mdRecord
        mdRecord.setTokenOrig(docTokensOrig);
        mdRecord.setTokenNorm(docTokensNorm);
        mdRecord.setTokenMorph(docTokensMorph);
        mdRecord.setContent(content);
      }
    } catch (Exception e) {
      throw new ApplicationException(e);
    }
  }
  
  private MetadataRecord getMetadataRecord(File xmlFile, String schemaName, MetadataRecord mdRecord, XQueryEvaluator xQueryEvaluator) throws ApplicationException {
    if (schemaName == null)
      return mdRecord;
    try {
      URL srcUrl = xmlFile.toURI().toURL();
      if (schemaName.equals("TEI"))
        mdRecord = getMetadataRecordTei(xQueryEvaluator, srcUrl, mdRecord);
      else if (schemaName.equals("html"))
        mdRecord = getMetadataRecordHtml(xQueryEvaluator, srcUrl, mdRecord);
      else
        mdRecord.setSchemaName(schemaName); // all other cases: set docType to schemaName
      evaluateXQueries(xQueryEvaluator, srcUrl, mdRecord);
    } catch (MalformedURLException e) {
      throw new ApplicationException(e);
    }
    return mdRecord;
  }

  private MetadataRecord evaluateXQueries(XQueryEvaluator xQueryEvaluator, URL srcUrl, MetadataRecord mdRecord) {
    Hashtable<String, XQuery> xqueriesHashtable = mdRecord.getxQueries();
    if (xqueriesHashtable != null) {
      Enumeration<String> keys = xqueriesHashtable.keys();
      while (keys != null && keys.hasMoreElements()) {
        String key = keys.nextElement();
        XQuery xQuery = xqueriesHashtable.get(key);
        String xQueryCode = xQuery.getCode();
        try {
          String xQueryResult = xQueryEvaluator.evaluateAsString(srcUrl, xQueryCode);
          xQuery.setResult(xQueryResult);
        } catch (Exception e) {
          // nothing
        }
      }
    }
    return mdRecord;
  }
  
  private MetadataRecord getMetadataRecordTei(XQueryEvaluator xQueryEvaluator, URL srcUrl, MetadataRecord mdRecord) throws ApplicationException {
    String metadataXmlStr = xQueryEvaluator.evaluateAsString(srcUrl, "/*:TEI/*:teiHeader");
    if (metadataXmlStr != null) {
      String identifier = xQueryEvaluator.evaluateAsStringValueJoined(metadataXmlStr, "/*:teiHeader/*:fileDesc/*:publicationStmt/*:idno");
      if (identifier != null)
        identifier = StringUtils.deresolveXmlEntities(identifier);
      String creator = xQueryEvaluator.evaluateAsStringValueJoined(metadataXmlStr, "/*:teiHeader/*:fileDesc/*:titleStmt/*:author");
      if (creator != null)
        creator = StringUtils.deresolveXmlEntities(creator);
      String title = xQueryEvaluator.evaluateAsStringValueJoined(metadataXmlStr, "/*:teiHeader/*:fileDesc/*:titleStmt/*:title");
      if (title != null)
        title = StringUtils.deresolveXmlEntities(title);
      String language = xQueryEvaluator.evaluateAsStringValueJoined(metadataXmlStr, "string(/*:teiHeader/*:profileDesc/*:langUsage/*:language[1]/@ident)");
      if (language != null && language.isEmpty())
        language = null;
      if (language != null) {
        language = StringUtils.deresolveXmlEntities(language);
        language = Language.getInstance().getISO639Code(language);
      }
      String place = xQueryEvaluator.evaluateAsStringValueJoined(metadataXmlStr, "/*:teiHeader/*:fileDesc/*:publicationStmt/*:pubPlace");
      if (place != null)
        place = StringUtils.deresolveXmlEntities(place);
      String yearStr = xQueryEvaluator.evaluateAsStringValueJoined(metadataXmlStr, "/*:teiHeader/*:fileDesc/*:publicationStmt/*:date");
      Date date = null; 
      if (yearStr != null && ! yearStr.equals("")) {
        yearStr = StringUtils.deresolveXmlEntities(yearStr);
        yearStr = new Util().toYearStr(yearStr);  // test if possible etc
        if (yearStr != null) {
          try {
            date = new Util().toDate(yearStr + "-01-01T00:00:00.000Z");
          } catch (Exception e) {
            // nothing
          }
        }
      }
      String subject = xQueryEvaluator.evaluateAsStringValueJoined(metadataXmlStr, "string(/*:teiHeader/*:profileDesc/*:textClass/*:keywords/*:term)");
      if (subject != null)
        subject = StringUtils.deresolveXmlEntities(subject);
      String rights = xQueryEvaluator.evaluateAsStringValueJoined(metadataXmlStr, "/*:teiHeader/*:fileDesc/*:publicationStmt/*:availability");
      if (rights == null)
        rights = "open access";
      rights = StringUtils.deresolveXmlEntities(rights);
      String license = "http://echo.mpiwg-berlin.mpg.de/policy/oa_basics/declaration";
      String accessRights = xQueryEvaluator.evaluateAsStringValueJoined(metadataXmlStr, "string(/*:teiHeader/*:fileDesc/*:publicationStmt/*:availability/@status)");
      if (accessRights == null) 
        accessRights = "free";
      accessRights = StringUtils.deresolveXmlEntities(accessRights);
      mdRecord.setIdentifier(identifier);
      mdRecord.setLanguage(language);
      mdRecord.setCreator(creator);
      mdRecord.setTitle(title);
      mdRecord.setPublisher(place);
      mdRecord.setRights(rights);
      mdRecord.setDate(date);
      mdRecord.setSubject(subject);
      mdRecord.setLicense(license);
      mdRecord.setAccessRights(accessRights);
    }
    String pageCountStr = xQueryEvaluator.evaluateAsString(srcUrl, "count(//*:pb)");
    int pageCount = Integer.valueOf(pageCountStr);
    mdRecord.setPageCount(pageCount);
    mdRecord.setSchemaName("TEI");
    return mdRecord;
  }

  private MetadataRecord getMetadataRecordHtml(XQueryEvaluator xQueryEvaluator, URL srcUrl, MetadataRecord mdRecord) throws ApplicationException {
    String metadataXmlStr = xQueryEvaluator.evaluateAsString(srcUrl, "/html/head");
    if (metadataXmlStr != null) {
      String identifier = xQueryEvaluator.evaluateAsStringValueJoined(metadataXmlStr, "string(/meta[@name = 'DC.identifier']/@content)");
      if (identifier != null && ! identifier.isEmpty())
        identifier = StringUtils.deresolveXmlEntities(identifier);
      String creator = xQueryEvaluator.evaluateAsStringValueJoined(metadataXmlStr, "string(/meta[@name = 'DC.creator']/@content)");
      if (creator != null && ! creator.isEmpty())
        creator = StringUtils.deresolveXmlEntities(creator);
      String title = xQueryEvaluator.evaluateAsStringValueJoined(metadataXmlStr, "string(/meta[@name = 'DC.title']/@content)");
      if (title != null && ! title.isEmpty())
        title = StringUtils.deresolveXmlEntities(title);
      String language = xQueryEvaluator.evaluateAsStringValueJoined(metadataXmlStr, "string(/meta[@name = 'DC.language']/@content)");
      if (language != null && language.isEmpty())
        language = null;
      if (language != null && ! language.isEmpty()) {
        language = StringUtils.deresolveXmlEntities(language);
        language = Language.getInstance().getISO639Code(language);
      }
      String publisher = xQueryEvaluator.evaluateAsStringValueJoined(metadataXmlStr, "string(/meta[@name = 'DC.publisher']/@content)");
      if (publisher != null)
        publisher = StringUtils.deresolveXmlEntities(publisher);
      String yearStr = xQueryEvaluator.evaluateAsStringValueJoined(metadataXmlStr, "string(/meta[@name = 'DC.date']/@content)");
      Date date = null; 
      if (yearStr != null && ! yearStr.equals("")) {
        yearStr = StringUtils.deresolveXmlEntities(yearStr);
        yearStr = new Util().toYearStr(yearStr);  // test if possible etc
        if (yearStr != null) {
          try {
            date = new Util().toDate(yearStr + "-01-01T00:00:00.000Z");
          } catch (Exception e) {
            // nothing
          }
        }
      }
      String subject = xQueryEvaluator.evaluateAsStringValueJoined(metadataXmlStr, "string(/meta[@name = 'DC.subject']/@content)");
      if (subject != null)
        subject = StringUtils.deresolveXmlEntities(subject);
      String rights = xQueryEvaluator.evaluateAsStringValueJoined(metadataXmlStr, "string(/meta[@name = 'DC.rights']/@content)");
      if (rights != null && ! rights.isEmpty())
        rights = StringUtils.deresolveXmlEntities(rights);
      String license = xQueryEvaluator.evaluateAsStringValueJoined(metadataXmlStr, "string(/meta[@name = 'DC.license']/@content)");
      if (license != null && ! license.isEmpty())
        license = StringUtils.deresolveXmlEntities(license);
      String accessRights = xQueryEvaluator.evaluateAsStringValueJoined(metadataXmlStr, "string(/meta[@name = 'DC.accessRights']/@content)");
      if (accessRights != null && ! accessRights.isEmpty())
        accessRights = StringUtils.deresolveXmlEntities(accessRights);
      mdRecord.setIdentifier(identifier);
      mdRecord.setLanguage(language);
      mdRecord.setCreator(creator);
      mdRecord.setTitle(title);
      mdRecord.setPublisher(publisher);
      mdRecord.setRights(rights);
      mdRecord.setDate(date);
      mdRecord.setSubject(subject);
      mdRecord.setLicense(license);
      mdRecord.setAccessRights(accessRights);
    }
    String pageCountStr = xQueryEvaluator.evaluateAsString(srcUrl, "count(//pb)");
    int pageCount = Integer.valueOf(pageCountStr);
    mdRecord.setPageCount(pageCount);
    mdRecord.setSchemaName("html");
    return mdRecord;
  }

  private String getPersons(String tocString, XQueryEvaluator xQueryEvaluator) throws ApplicationException {
    String persons = xQueryEvaluator.evaluateAsStringValueJoined(tocString, "/list/list[@type='persons']/item[not(. = preceding::item)]", "###"); // [not(. = preceding::item)] removes duplicates
    return persons;
  }
  
  private String getPlaces(String tocString, XQueryEvaluator xQueryEvaluator) throws ApplicationException {
    String places = xQueryEvaluator.evaluateAsStringValueJoined(tocString, "/list/list[@type='places']/item[not(. = preceding::item)]", "###"); 
    return places;
  }
  
  private String getNodeType(XdmNode node) {
    String nodeType = null;
    XdmSequenceIterator iter = node.axisIterator(Axis.CHILD);
    if (iter != null) {
      while (iter.hasNext()) {
        XdmNode firstChild = (XdmNode) iter.next();
        if (firstChild != null) {
          XdmNodeKind nodeKind = firstChild.getNodeKind();
          if (nodeKind.ordinal() == XdmNodeKind.ELEMENT.ordinal()) {
            QName nodeQName = firstChild.getNodeName();
            nodeType = nodeQName.getLocalName();
          }
        }
      }
    }
    return nodeType;
  }
  
  public String getDocFullFileName(String docId) {
    if (docId == null || docId.trim().isEmpty())
      return null;
    String docDir = getDocDir(docId);
    String docFileName = getDocFileName(docId);
    String docFullFileName = docDir + "/" + docFileName; 
    return docFullFileName;
  }
  
  public boolean isDocXml(String docId) {
    boolean isXml = false;
    String fileExt = getDocFileExtension(docId);
    if (fileExt != null) {
      fileExt = fileExt.toLowerCase();
      if (fileExt.equals("xml") || fileExt.equals("html") || fileExt.equals("xhtml"))
        isXml = true;
    }
    return isXml;
  }

  private String getMimeType(String docId) {
    String mimeType = null;
    String fileName = getDocFileName(docId);
    if (fileName != null) {
      fileName = fileName.toLowerCase();
      FileNameMap fileNameMap = URLConnection.getFileNameMap();
      mimeType = fileNameMap.getContentTypeFor(fileName);
    }
    return mimeType;
  }

  public String getDocDir(String docId) {
    if (docId == null || docId.trim().isEmpty())
      return null;
    String documentsDirectory = Constants.getInstance().getDocumentsDir();
    String docDir = documentsDirectory;
    String docFileName = getDocFileName(docId);
    String docFilePath = getDocFilePath(docId);
    if (docFilePath != null)
      docDir = docDir + docFilePath;
    if (docFileName != null)
      docDir = docDir + "/" + docFileName;
    else
      docDir = docDir + "/" + "XXXXX";
    return docDir;
  }
  
  private String getDocFileName(String docId) {
    String docFileName = docId.trim();
    int index = docId.lastIndexOf("/");
    if (index != -1) {
      docFileName = docId.substring(index + 1);
    }
    return docFileName;
  }
  
  private String getDocFileExtension(String docId) {
    docId = docId.trim();
    String fileExt = null;
    int index = docId.lastIndexOf(".");
    if (index != -1) {
      fileExt = docId.substring(index + 1);
    }
    return fileExt;
  }
  
  private String getDocFilePath(String docId) {
    String docFilePath = null;
    int index = docId.lastIndexOf("/");
    if (index >= 0) {
      docFilePath = docId.substring(0, index);
    }
    if (docFilePath != null && ! docFilePath.startsWith("/"))
      docFilePath = docFilePath + "/";
    return docFilePath;
  }

  private Hashtable<Integer, StringBuilder> getFragments(String fileName, String milestoneElementName) throws ApplicationException {
    try {
      GetFragmentsContentHandler getFragmentsContentHandler = new GetFragmentsContentHandler(milestoneElementName);
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

  private String tokenizeWithLemmas(String xmlStr, String language) throws ApplicationException {
    StringReader strReader = new StringReader(xmlStr);
    XmlTokenizer xmlTokenizer = new XmlTokenizer(strReader);
    xmlTokenizer.setLanguage(language);
    String[] outputOptionsWithLemmas = {"withLemmas"}; // so all tokens are fetched with lemmas (costs performance)
    xmlTokenizer.setOutputOptions(outputOptionsWithLemmas); 
    xmlTokenizer.tokenize();  
    String retStr = xmlTokenizer.getXmlResult();
    return retStr;
  }
  
}