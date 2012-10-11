package org.bbaw.wsp.cms.lucene;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.SetBasedFieldSelector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.TextFragment;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.search.similar.MoreLikeThis;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.bbaw.wsp.cms.collections.Collection;
import org.bbaw.wsp.cms.collections.CollectionReader;
import org.bbaw.wsp.cms.dochandler.DocumentHandler;
import org.bbaw.wsp.cms.document.Hits;
import org.bbaw.wsp.cms.document.MetadataRecord;
import org.bbaw.wsp.cms.document.Token;
import org.bbaw.wsp.cms.general.Constants;
import org.bbaw.wsp.cms.scheduler.CmsDocOperation;
import org.bbaw.wsp.cms.translator.MicrosoftTranslator;

import de.mpg.mpiwg.berlin.mpdl.exception.ApplicationException;
import de.mpg.mpiwg.berlin.mpdl.lt.dict.db.LexHandler;
import de.mpg.mpiwg.berlin.mpdl.lt.morph.app.Form;
import de.mpg.mpiwg.berlin.mpdl.lt.morph.app.Lemma;
import de.mpg.mpiwg.berlin.mpdl.lt.text.norm.Normalizer;
import de.mpg.mpiwg.berlin.mpdl.lt.text.tokenize.XmlTokenizer;
import de.mpg.mpiwg.berlin.mpdl.lt.text.tokenize.XmlTokenizerContentHandler;
import de.mpg.mpiwg.berlin.mpdl.util.StringUtils;
import de.mpg.mpiwg.berlin.mpdl.util.Util;

public class IndexHandler {
  private static IndexHandler instance;
  private static Logger LOGGER = Logger.getLogger(IndexHandler.class.getName());
  private IndexWriter documentsIndexWriter;
  private IndexWriter nodesIndexWriter;
  private SearcherManager documentsSearcherManager;
  private SearcherManager nodesSearcherManager;
  private IndexReader documentsIndexReader;
  private PerFieldAnalyzerWrapper documentsPerFieldAnalyzer;
  private PerFieldAnalyzerWrapper nodesPerFieldAnalyzer;

  public static IndexHandler getInstance() throws ApplicationException {
    if (instance == null) {
      instance = new IndexHandler();
      instance.init();
    }
    return instance;
  }

  private void init() throws ApplicationException {
    documentsIndexWriter = getDocumentsWriter();
    documentsIndexWriter.setMaxFieldLength(1000000);
    nodesIndexWriter = getNodesWriter();
    nodesIndexWriter.setMaxFieldLength(1000000);
    documentsSearcherManager = getNewSearcherManager(documentsIndexWriter);
    nodesSearcherManager = getNewSearcherManager(nodesIndexWriter);
    documentsIndexReader = getDocumentsReader();
  }

  public void indexDocument(CmsDocOperation docOperation) throws ApplicationException {
    try {
      // first delete document in documentsIndex and nodesIndex
      deleteDocumentLocal(docOperation);
      indexDocumentLocal(docOperation);
      documentsIndexWriter.commit();
      nodesIndexWriter.commit();
    } catch (Exception e) {
      try {
        documentsIndexWriter.rollback();
        nodesIndexWriter.rollback();
      } catch (Exception ex) {
        // nothing
      }
      throw new ApplicationException(e);
    }
  }

  private void indexDocumentLocal(CmsDocOperation docOperation) throws ApplicationException {
    try {
      MetadataRecord mdRecord = docOperation.getMdRecord();
      String docId = mdRecord.getDocId();
      // add document to documentsIndex
      Document doc = new Document();
      Field docIdField = new Field("docId", docId, Field.Store.YES, Field.Index.ANALYZED);
      doc.add(docIdField);
      String docIdSortedStr = docId.toLowerCase();  // so that sorting is lower case
      Field docIdFieldSorted = new Field("docIdSorted", docIdSortedStr, Field.Store.YES, Field.Index.NOT_ANALYZED); 
      doc.add(docIdFieldSorted);
      String identifier = mdRecord.getIdentifier();
      if (identifier != null) {
        Field identifierField = new Field("identifier", identifier, Field.Store.YES, Field.Index.ANALYZED);
        doc.add(identifierField);
      }
      String uri = mdRecord.getUri();
      if (uri == null)
        uri = docOperation.getSrcUrl();
      if (uri != null) {
        Field uriField = new Field("uri", uri, Field.Store.YES, Field.Index.ANALYZED);
        doc.add(uriField);
      }
      String collectionNames = mdRecord.getCollectionNames();
      if (collectionNames != null) {
        Field collectionNamesField = new Field("collectionNames", collectionNames, Field.Store.YES, Field.Index.ANALYZED);
        doc.add(collectionNamesField);
      }
      if (mdRecord.getCreator() != null) {
        Field authorField = new Field("author", mdRecord.getCreator(), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS);
        doc.add(authorField);
        String authorStr = mdRecord.getCreator();
        if (authorStr != null)
          authorStr = authorStr.toLowerCase();  // so that sorting is lower case
        Field authorFieldSorted = new Field("authorSorted", authorStr, Field.Store.YES, Field.Index.NOT_ANALYZED);
        doc.add(authorFieldSorted);
      }
      if (mdRecord.getTitle() != null) {
        Field titleField = new Field("title", mdRecord.getTitle(), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS);
        doc.add(titleField);
        String titleStr = mdRecord.getTitle();
        if (titleStr != null)
          titleStr = titleStr.toLowerCase();  // so that sorting is lower case
        Field titleFieldSorted = new Field("titleSorted", titleStr, Field.Store.YES, Field.Index.NOT_ANALYZED);
        doc.add(titleFieldSorted);
      }
      if (mdRecord.getPublisher() != null) {
        Field publisherField = new Field("publisher", mdRecord.getPublisher(), Field.Store.YES, Field.Index.ANALYZED);
        doc.add(publisherField);
        String publisherStr = mdRecord.getPublisher();
        if (publisherStr != null)
          publisherStr = publisherStr.toLowerCase();  // so that sorting is lower case
        Field publisherFieldSorted = new Field("publisherSorted", publisherStr, Field.Store.YES, Field.Index.NOT_ANALYZED);
        doc.add(publisherFieldSorted);
      }
      String yearStr = mdRecord.getYear();
      if (yearStr == null) {
        Date pubDate = mdRecord.getPublishingDate();
        if (pubDate != null) {
          Calendar cal = Calendar.getInstance();
          cal.setTime(pubDate);
          int year = cal.get(Calendar.YEAR);
          yearStr = String.valueOf(year);
        }
      }
      if (yearStr != null) {
        Field dateField = new Field("date", yearStr, Field.Store.YES, Field.Index.ANALYZED);
        doc.add(dateField);
        Field dateFieldSorted = new Field("dateSorted", yearStr, Field.Store.YES, Field.Index.NOT_ANALYZED);
        doc.add(dateFieldSorted);
      }
      if (mdRecord.getSubject() != null) {
        Field subjectField = new Field("subject", mdRecord.getSubject(), Field.Store.YES, Field.Index.ANALYZED);
        doc.add(subjectField);
      }
      if (mdRecord.getRights() != null) {
        Field rightsField = new Field("rights", mdRecord.getRights(), Field.Store.YES, Field.Index.ANALYZED);
        doc.add(rightsField);
      }
      if (mdRecord.getLicense() != null) {
        Field licenseField = new Field("license", mdRecord.getLicense(), Field.Store.YES, Field.Index.ANALYZED);
        doc.add(licenseField);
      }
      if (mdRecord.getAccessRights() != null) {
        Field accessRightsField = new Field("accessRights", mdRecord.getAccessRights(), Field.Store.YES, Field.Index.ANALYZED);
        doc.add(accessRightsField);
      }
      if (mdRecord.getLastModified() != null) {
        Date lastModified = mdRecord.getLastModified();
        String xsDateStr = new Util().toXsDate(lastModified);
        Field lastModifiedField = new Field("lastModified", xsDateStr, Field.Store.YES, Field.Index.ANALYZED);
        doc.add(lastModifiedField);
        long time = lastModified.getTime();
        String timeStr = String.valueOf(time);
        Field lastModifiedFieldSorted = new Field("lastModifiedSorted", timeStr, Field.Store.YES, Field.Index.NOT_ANALYZED);
        doc.add(lastModifiedFieldSorted);
      }
      if (mdRecord.getSchemaName() != null) {
        Field schemaField = new Field("schemaName", mdRecord.getSchemaName(), Field.Store.YES, Field.Index.ANALYZED);
        doc.add(schemaField);
        String schemaStr = mdRecord.getSchemaName();
        if (schemaStr != null)
          schemaStr = schemaStr.toLowerCase();  // so that sorting is lower case
        Field schemaFieldSorted = new Field("schemaNameSorted", schemaStr, Field.Store.YES, Field.Index.NOT_ANALYZED);
        doc.add(schemaFieldSorted);
      }
      if (mdRecord.getPersons() != null) {
        Field personsField = new Field("persons", mdRecord.getPersons(), Field.Store.YES, Field.Index.ANALYZED);
        doc.add(personsField);
      }
      if (mdRecord.getPlaces() != null) {
        Field placesField = new Field("places", mdRecord.getPlaces(), Field.Store.YES, Field.Index.ANALYZED);
        doc.add(placesField);
      }
      String language = mdRecord.getLanguage();
      if (language != null) {
        Field languageField = new Field("language", mdRecord.getLanguage(), Field.Store.YES, Field.Index.ANALYZED);
        doc.add(languageField);
        String langStr = mdRecord.getLanguage();
        if (langStr != null)
          langStr = langStr.toLowerCase();  // so that sorting is lower case
        Field languageFieldSorted = new Field("languageSorted", langStr, Field.Store.YES, Field.Index.NOT_ANALYZED);
        doc.add(languageFieldSorted);
      }
      int pageCount = mdRecord.getPageCount();
      if (pageCount != -1) {
        String pageCountStr = String.valueOf(pageCount);
        Field pageCountField = new Field("pageCount", pageCountStr, Field.Store.YES, Field.Index.ANALYZED);
        doc.add(pageCountField);
      }
      String docTokensOrig = mdRecord.getTokenOrig();
      if (docTokensOrig != null) {
        Field tokenOrigField = new Field("tokenOrig", docTokensOrig, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS);
        doc.add(tokenOrigField);
      }
      String docTokensReg = mdRecord.getTokenReg();
      if (docTokensReg != null) {
        Field tokenRegField = new Field("tokenReg", docTokensReg, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS);
        doc.add(tokenRegField);
      }
      String docTokensNorm = mdRecord.getTokenNorm();
      if (docTokensNorm != null) {
        Field tokenNormField = new Field("tokenNorm", docTokensNorm, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS);
        doc.add(tokenNormField);
      }
      String docTokensMorph = mdRecord.getTokenMorph();
      if (docTokensMorph != null) {
        Field tokenMorphField = new Field("tokenMorph", docTokensMorph, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS);
        doc.add(tokenMorphField);
      }
      String contentXml = mdRecord.getContentXml();
      if (contentXml != null) {
        Field contentXmlField = new Field("xmlContent", contentXml, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS);
        doc.add(contentXmlField);
      }
      String content = mdRecord.getContent();
      if (content != null) {
        Field contentField = new Field("content", content, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS);
        doc.add(contentField);
      }

      documentsIndexWriter.addDocument(doc);

      DocumentHandler docHandler = new DocumentHandler();
      boolean docIsXml = docHandler.isDocXml(docId);
      if (docIsXml) {
        // add all elements with the specified names of the document to nodesIndex
        ArrayList<XmlTokenizerContentHandler.Element> xmlElements = mdRecord.getXmlElements();
        for (int i = 0; i < xmlElements.size(); i++) {
          XmlTokenizerContentHandler.Element element = xmlElements.get(i);
          Document nodeDoc = new Document();
          nodeDoc.add(docIdField);
          String nodeLanguage = element.lang;
          if (nodeLanguage == null)
            nodeLanguage = language;
          String nodePageNumber = String.valueOf(element.pageNumber);
          String nodeLineNumber = String.valueOf(element.lineNumber);
          String nodeElementName = String.valueOf(element.name);
          String nodeElementDocPosition = String.valueOf(element.docPosition);
          String nodeElementAbsolutePosition = String.valueOf(element.position);
          String nodeElementPagePosition = String.valueOf(element.pagePosition);
          String nodeElementPosition = String.valueOf(element.elemPosition);
          String nodeXmlId = element.xmlId;
          String nodeXpath = element.xpath;
          String nodeXmlContent = element.toXmlString();
          String nodeTokensOrig = element.getTokensStr("orig");
          String nodeTokensReg = element.getTokensStr("reg");
          String nodeTokensNorm = element.getTokensStr("norm");
          String nodeTokensMorph = element.getTokensStr("morph");
          if (nodeLanguage != null) {
            Field nodeLanguageField = new Field("language", nodeLanguage, Field.Store.YES, Field.Index.ANALYZED);
            nodeDoc.add(nodeLanguageField);
          }
          Field nodePageNumberField = new Field("pageNumber", nodePageNumber, Field.Store.YES, Field.Index.ANALYZED);
          nodeDoc.add(nodePageNumberField);
          Field nodeLineNumberField = new Field("lineNumber", nodeLineNumber, Field.Store.YES, Field.Index.ANALYZED);
          nodeDoc.add(nodeLineNumberField);
          Field nodeElementNameField = new Field("elementName", nodeElementName, Field.Store.YES, Field.Index.ANALYZED);
          nodeDoc.add(nodeElementNameField);
          Field nodeElementDocPositionField = new Field("elementDocPosition", nodeElementDocPosition, Field.Store.YES, Field.Index.ANALYZED);
          nodeDoc.add(nodeElementDocPositionField);
          Field nodeElementDocPositionFieldSorted = new Field("elementDocPositionSorted", nodeElementDocPosition, Field.Store.YES, Field.Index.NOT_ANALYZED);
          nodeDoc.add(nodeElementDocPositionFieldSorted);
          Field nodeElementAbsolutePositionField = new Field("elementAbsolutePosition", nodeElementAbsolutePosition, Field.Store.YES, Field.Index.ANALYZED);
          nodeDoc.add(nodeElementAbsolutePositionField);
          Field nodeElementPagePositionField = new Field("elementPagePosition", nodeElementPagePosition, Field.Store.YES, Field.Index.ANALYZED);
          nodeDoc.add(nodeElementPagePositionField);
          Field nodeElementPositionField = new Field("elementPosition", nodeElementPosition, Field.Store.YES, Field.Index.ANALYZED);
          nodeDoc.add(nodeElementPositionField);
          if (nodeXmlId != null) {
            Field nodeXmlIdField = new Field("xmlId", nodeXmlId, Field.Store.YES, Field.Index.ANALYZED);
            nodeDoc.add(nodeXmlIdField);
          }
          if (nodeXpath != null) {
            Field nodeXpathField = new Field("xpath", nodeXpath, Field.Store.YES, Field.Index.ANALYZED);
            nodeDoc.add(nodeXpathField);
          }
          if (nodeXmlContent != null) {
            Field nodeXmlContentField = new Field("xmlContent", nodeXmlContent, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS);
            nodeDoc.add(nodeXmlContentField);
          }
          if (nodeXmlContent != null) {
            String nodeXmlContentTokenized = toTokenizedXmlString(nodeXmlContent, nodeLanguage);
            Field nodeXmlContentTokenizedField = new Field("xmlContentTokenized", nodeXmlContentTokenized, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS);
            nodeDoc.add(nodeXmlContentTokenizedField);
          }
          if (nodeTokensOrig != null) {
            Field nodeTokenOrigField = new Field("tokenOrig", nodeTokensOrig, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS);
            nodeDoc.add(nodeTokenOrigField);
          }
          if (nodeTokensReg != null) {
            Field nodeTokenRegField = new Field("tokenReg", nodeTokensReg, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS);
            nodeDoc.add(nodeTokenRegField);
          }
          if (nodeTokensNorm != null) {
            Field nodeTokenNormField = new Field("tokenNorm", nodeTokensNorm, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS);
            nodeDoc.add(nodeTokenNormField);
          }
          if (nodeTokensMorph != null) {
            Field nodeTokenMorphField = new Field("tokenMorph", nodeTokensMorph, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS);
            nodeDoc.add(nodeTokenMorphField);
          }
  
          nodesIndexWriter.addDocument(nodeDoc);
        }
      }
    } catch (Exception e) {
      throw new ApplicationException(e);
    }
  }

  public void deleteDocument(CmsDocOperation docOperation) throws ApplicationException {
    try {
      deleteDocumentLocal(docOperation);
      documentsIndexWriter.commit();
      nodesIndexWriter.commit();
    } catch (Exception e) {
      try {
        documentsIndexWriter.rollback();
        nodesIndexWriter.rollback();
      } catch (Exception ex) {
        // nothing
      }
      throw new ApplicationException(e);
    }
  }

  private void deleteDocumentLocal(CmsDocOperation docOperation) throws ApplicationException {
    String docId = docOperation.getDocIdentifier();
    try {
      Term termIdentifier = new Term("docId", docId);
      documentsIndexWriter.deleteDocuments(termIdentifier);
      nodesIndexWriter.deleteDocuments(termIdentifier);
    } catch (Exception e) {
      throw new ApplicationException(e);
    }
  }

  public Hits queryDocuments(String queryStr, String[] sortFieldNames, String language, int from, int to, boolean withHitFragments, boolean translate) throws ApplicationException {
    Hits hits = null;
    IndexSearcher searcher = null;
    try {
      makeDocumentsSearcherManagerUpToDate();
      searcher = documentsSearcherManager.acquire();
      String defaultQueryFieldName = "tokenOrig";
      QueryParser queryParser = new QueryParser(Version.LUCENE_35, defaultQueryFieldName, documentsPerFieldAnalyzer);
      Query query = null;
      if (queryStr.equals("*")) {
        query = new MatchAllDocsQuery();
      } else {
        query = queryParser.parse(queryStr);
      }
      Query morphQuery = buildMorphQuery(query, language, false, translate);
      Query highlighterQuery = buildMorphQuery(query, language, true, translate);
      if (query instanceof PhraseQuery || query instanceof PrefixQuery || query instanceof FuzzyQuery || query instanceof TermRangeQuery) {
        highlighterQuery = query;  // TODO wenn sie rekursiv enthalten sind 
      }
      SimpleHTMLFormatter htmlFormatter = new SimpleHTMLFormatter();
      QueryScorer queryScorer = new QueryScorer(highlighterQuery);
      Highlighter highlighter = new Highlighter(htmlFormatter, queryScorer);
      TopDocs resultDocs = null;
      if (sortFieldNames != null) {
        Sort sort = buildSort(sortFieldNames, "doc");  // build sort criteria 
        resultDocs = searcher.search(morphQuery, 10000, sort);
      } else {
        resultDocs = searcher.search(morphQuery, 10000);
      }
      resultDocs.setMaxScore(1);
      int toTmp = to;
      if (resultDocs.scoreDocs.length <= to)
        toTmp = resultDocs.scoreDocs.length - 1;
      if (resultDocs != null) {
        ArrayList<org.bbaw.wsp.cms.document.Document>  docs = new ArrayList<org.bbaw.wsp.cms.document.Document>();
        for (int i=from; i<=toTmp; i++) {
          int docID = resultDocs.scoreDocs[i].doc;
          FieldSelector docFieldSelector = getDocFieldSelector();
          Document luceneDoc = searcher.doc(docID, docFieldSelector);
          org.bbaw.wsp.cms.document.Document doc = new org.bbaw.wsp.cms.document.Document(luceneDoc);
          if (withHitFragments) {
            ArrayList<String> hitFragments = new ArrayList<String>();
            Fieldable docContentField = luceneDoc.getFieldable("content");
            if (docContentField != null) {
              String docContent = docContentField.stringValue();
              TokenStream tokenStream = TokenSources.getAnyTokenStream(this.documentsIndexReader, docID, docContentField.name(), documentsPerFieldAnalyzer);
              TextFragment[] textfragments = highlighter.getBestTextFragments(tokenStream, docContent, true, 3);
              if (textfragments.length > 0) {
                for (int j=0; j<textfragments.length; j++) {
                  hitFragments.add(checkHitFragment(textfragments[j].toString()));
                }
              }
            }
            if (! hitFragments.isEmpty())
              doc.setHitFragments(hitFragments);
          }
          docs.add(doc);
        }
        if (docs != null) {
          hits = new Hits(docs, from, to);
          hits.setSize(resultDocs.scoreDocs.length);
          hits.setQuery(morphQuery);
        }
      }
    } catch (Exception e) {
      throw new ApplicationException(e);
    } finally {
      try {
        if (searcher != null)
          documentsSearcherManager.release(searcher);
      } catch (IOException e) {
        // nothing
      }
    }
    // Do not use searcher after this!
    searcher = null;
    return hits;
  }

  public Hits queryDocument(String docId, String queryStr, int from, int to) throws ApplicationException {
    Hits hits = null;
    IndexSearcher searcher = null;
    MetadataRecord docMetadataRecord = getDocMetadata(docId);
    if (docMetadataRecord == null)
      return null;  // no document with that docId is in index
    try {
      makeNodesSearcherManagerUpToDate();
      searcher = nodesSearcherManager.acquire();
      String fieldNameDocId = "docId";
      Query queryDocId = new QueryParser(Version.LUCENE_35, fieldNameDocId, nodesPerFieldAnalyzer).parse(docId);
      String defaultQueryFieldName = "tokenOrig";
      Query query = new QueryParser(Version.LUCENE_35, defaultQueryFieldName, nodesPerFieldAnalyzer).parse(queryStr);
      String language = docMetadataRecord.getLanguage();
      if (language == null || language.equals("")) {
        String collectionNames = docMetadataRecord.getCollectionNames();
        Collection collection = CollectionReader.getInstance().getCollection(collectionNames);
        if (collection != null) {
          String mainLang = collection.getMainLanguage();
          if (mainLang != null)
            language = mainLang;
        } 
      }
      Query morphQuery = buildMorphQuery(query, language);
      BooleanQuery queryDoc = new BooleanQuery();
      queryDoc.add(queryDocId, BooleanClause.Occur.MUST);
      queryDoc.add(morphQuery, BooleanClause.Occur.MUST);
      Sort sortByPosition = new Sort(new SortField("position", SortField.INT));
      TopDocs topDocs = searcher.search(queryDoc, 100000, sortByPosition);
      topDocs.setMaxScore(1);
      int toTmp = to;
      if (topDocs.scoreDocs.length <= to)
        toTmp = topDocs.scoreDocs.length - 1;
      if (topDocs != null) {
        ArrayList<org.bbaw.wsp.cms.document.Document>  docs = new ArrayList<org.bbaw.wsp.cms.document.Document>();
        for (int i=from; i<=toTmp; i++) {
          int docID = topDocs.scoreDocs[i].doc;
          FieldSelector nodeFieldSelector = getNodeFieldSelector();
          Document luceneDoc = searcher.doc(docID, nodeFieldSelector);
          org.bbaw.wsp.cms.document.Document doc = new org.bbaw.wsp.cms.document.Document(luceneDoc);
          docs.add(doc);
        }
        if (docs != null) {
          hits = new Hits(docs, from, to);
          hits.setSize(topDocs.scoreDocs.length);
        }
      }
      searcher.close();
    } catch (Exception e) {
      throw new ApplicationException(e);
    } finally {
      try {
        if (searcher != null)
          documentsSearcherManager.release(searcher);
      } catch (IOException e) {
        // nothing
      }
    }
    // Do not use searcher after this!
    searcher = null;
    return hits;
  }

  public MetadataRecord getDocMetadata(String docId) throws ApplicationException {
    MetadataRecord mdRecord = null;
    Document doc = getDocument(docId);
    if (doc != null) {
      String identifier = null;
      Fieldable identifierField = doc.getFieldable("identifier");
      if (identifierField != null)
        identifier = identifierField.stringValue();
      String uri = null;
      Fieldable uriField = doc.getFieldable("uri");
      if (uriField != null)
        uri = uriField.stringValue();
      String collectionNames = null;
      Fieldable collectionNamesField = doc.getFieldable("collectionNames");
      if (collectionNamesField != null)
        collectionNames = collectionNamesField.stringValue();
      String author = null;
      Fieldable authorField = doc.getFieldable("author");
      if (authorField != null)
        author = authorField.stringValue();
      String title = null;
      Fieldable titleField = doc.getFieldable("title");
      if (titleField != null)
        title = titleField.stringValue();
      String language = null;
      Fieldable languageField = doc.getFieldable("language");
      if (languageField != null)
        language = languageField.stringValue();
      else {
        Collection collection = CollectionReader.getInstance().getCollection(collectionNames);
        if (collection != null) {
          String mainLang = collection.getMainLanguage();
          if (mainLang != null)
            language = mainLang;
        } 
      }
      Date yearDate = null;
      Fieldable dateField = doc.getFieldable("date");
      if (dateField != null) {
        String dateStr = dateField.stringValue();
        if (dateStr != null && !dateStr.equals("")) {
          dateStr = StringUtils.deresolveXmlEntities(dateStr);
          String yearStr = new Util().toYearStr(dateStr); // test if possible
          // etc
          if (yearStr != null) {
            yearDate = new Util().toDate(yearStr + "-01-01T00:00:00.000Z");
          }
        }
      }
      String rights = null;
      Fieldable rightsField = doc.getFieldable("rights");
      if (rightsField != null)
        rights = rightsField.stringValue();
      String license = null;
      Fieldable licenseField = doc.getFieldable("license");
      if (licenseField != null)
        license = licenseField.stringValue();
      String accessRights = null;
      Fieldable accessRightsField = doc.getFieldable("accessRights");
      if (accessRightsField != null)
        accessRights = accessRightsField.stringValue();
      int pageCount = -1;
      Fieldable pageCountField = doc.getFieldable("pageCount");
      if (pageCountField != null) {
        String pageCountStr = pageCountField.stringValue();
        pageCount = Integer.valueOf(pageCountStr);
      }
      String schemaName = null;
      Fieldable schemaNameField = doc.getFieldable("schemaName");
      if (schemaNameField != null)
        schemaName = schemaNameField.stringValue();
      Date lastModified = null;
      Fieldable lastModifiedField = doc.getFieldable("lastModified");
      if (lastModifiedField != null) {
        String lastModifiedXSDateStr = lastModifiedField.stringValue();
        lastModified = new Util().toDate(lastModifiedXSDateStr);
      }
      mdRecord = new MetadataRecord();
      mdRecord.setDocId(docId);
      mdRecord.setUri(uri);
      mdRecord.setIdentifier(identifier);
      mdRecord.setCollectionNames(collectionNames);
      mdRecord.setCreator(author);
      mdRecord.setTitle(title);
      mdRecord.setDate(yearDate);
      mdRecord.setLanguage(language);
      mdRecord.setLicense(license);
      mdRecord.setRights(rights);
      mdRecord.setAccessRights(accessRights);
      mdRecord.setPageCount(pageCount);
      mdRecord.setSchemaName(schemaName);
      mdRecord.setLastModified(lastModified);
    }
    return mdRecord;
  }

  public ArrayList<Token> getToken(String fieldName, String value, int count) throws ApplicationException {
    ArrayList<Token> retToken = null;
    int counter = 0;
    TermEnum terms = null;
    try {
      if (value == null)
        value = "";
      Term term = new Term(fieldName, value);
      makeIndexReaderUpToDate();
      terms = documentsIndexReader.terms(term);
      while (terms != null && fieldName != null && fieldName.equals(terms.term().field()) && counter < count) {
        if (retToken == null)
          retToken = new ArrayList<Token>();
        Term termContent = terms.term();
        Token token = new Token(termContent);
        retToken.add(token);
        counter++;
        if (!terms.next())
          break;
      }
    } catch (Exception e) {
      throw new ApplicationException(e);
    } finally {
      if (terms != null) {
        try {
          terms.close();
        } catch (IOException e) {
          // nothing
        }
      }
    }
    return retToken;
  }

  public ArrayList<Token> getToken(String docId, String fieldName, String value, int count) throws ApplicationException {
    ArrayList<Token> retToken = null;
    if (value == null)
      value = "";
    int counter = 0;
    IndexSearcher searcher = null;
    try {
      makeDocumentsSearcherManagerUpToDate();
      makeIndexReaderUpToDate();
      searcher = documentsSearcherManager.acquire();
      Query queryDocId = new TermQuery(new Term("docId", docId));
      TopDocs topDocs = searcher.search(queryDocId, 1);
      if (topDocs != null) {
        int docIdInt = topDocs.scoreDocs[0].doc;
        TermFreqVector termFreqVector = documentsIndexReader.getTermFreqVector(docIdInt, fieldName);
        if (termFreqVector != null) {
          String[] terms = termFreqVector.getTerms();
          int[] freqs = termFreqVector.getTermFrequencies();
          boolean success = false;
          if (terms != null) {
            retToken = new ArrayList<Token>();
            for (int i = 0; i < terms.length; i++) {
              String termStr = terms[i];
              if (termStr.startsWith(value))
                success = true;
              if (success) {
                counter++;
                int freq = freqs[i];
                Term t = new Term(fieldName, termStr);
                Token tok = new Token(t);
                tok.setFreq(freq);
                retToken.add(tok);
              }
              if (counter >= count)
                break;
            }
          }
        }
      }
    } catch (Exception e) {
      throw new ApplicationException(e);
    } finally {
      try {
        if (searcher != null)
          documentsSearcherManager.release(searcher);
      } catch (IOException e) {
        // nothing
      }
    }
    // Do not use searcher after this!
    searcher = null;
    return retToken;
  }

  public void end() throws ApplicationException {
    try {
      if (documentsIndexWriter != null)
        documentsIndexWriter.close();
      if (nodesIndexWriter != null)
        nodesIndexWriter.close();
      if (documentsSearcherManager != null)
        documentsSearcherManager.close();
      if (nodesSearcherManager != null)
        nodesSearcherManager.close();
      if (documentsIndexReader != null)
        documentsIndexReader.close();
    } catch (IOException e) {
      throw new ApplicationException(e);
    }
  }

  private Query buildMorphQuery(Query query, String language) throws ApplicationException {
    return buildMorphQuery(query, language, false, false);
  }

  private Query buildMorphQuery(Query query, String language, boolean withAllForms, boolean translate) throws ApplicationException {
    Query morphQuery = null;
    if (query instanceof TermQuery) {
      TermQuery termQuery = (TermQuery) query;
      morphQuery = buildMorphQuery(termQuery, language, withAllForms, translate);
    } else if (query instanceof BooleanQuery) {
      BooleanQuery booleanQuery = (BooleanQuery) query;
      morphQuery = buildMorphQuery(booleanQuery, language, withAllForms, translate);
    } else {
      morphQuery = query; // all other cases: PrefixQuery, PhraseQuery, FuzzyQuery, TermRangeQuery, ...
    }
    return morphQuery;
  }

  private Query buildMorphQuery(TermQuery inputTermQuery, String fromLang, boolean withAllForms, boolean translate) throws ApplicationException {
    String[] toLanguages = {"deu", "eng", "fra"};  // TODO
    String fromLanguage = null;
    String inputTerm = inputTermQuery.getTerm().text();
    if (fromLang == null || fromLang.isEmpty()) {
      String detectedLang = MicrosoftTranslator.detectLanguageCode(inputTerm);
      if (detectedLang != null)
        fromLanguage = detectedLang;
    } else {
      fromLanguage = fromLang;
    }
    LexHandler lexHandler = LexHandler.getInstance();
    String fieldName = inputTermQuery.getTerm().field();
    ArrayList<TermQuery> queryTerms = new ArrayList<TermQuery>();
    if (fieldName != null && fieldName.equals("tokenMorph")) {
      ArrayList<Lemma> lemmas = lexHandler.getLemmas(inputTerm, "form", fromLanguage, Normalizer.DICTIONARY, true);
      if (lemmas == null) {  // if no lemmas are found then do a query in tokenOrig TODO should this really be done ?
        if (translate) {
          String[] terms = {inputTerm};
          ArrayList<String> translatedTerms = MicrosoftTranslator.translate(terms, fromLanguage, toLanguages);
          for (int i=0; i<translatedTerms.size(); i++) {
            String translatedTerm = translatedTerms.get(i);
            Term translatedTermTokenOrig = new Term("tokenOrig", translatedTerm);
            TermQuery translatedTermQueryInTokenOrig = new TermQuery(translatedTermTokenOrig);
            queryTerms.add(translatedTermQueryInTokenOrig);
          }
        } else {
          Term termTokenOrig = new Term("tokenOrig", inputTerm);
          TermQuery termQueryInTokenOrig = new TermQuery(termTokenOrig);
          queryTerms.add(termQueryInTokenOrig);
        }
      } else {
        if (translate) {
          ArrayList<String> morphTerms = new ArrayList<String>();
          for (int i=0; i<lemmas.size(); i++) {
            Lemma lemma = lemmas.get(i);
            if (withAllForms) { // all word forms are put into the query as boolean or clauses: needed in fragments search when all forms should be highlighted
              ArrayList<Form> forms = lemma.getFormsList();
              for (int j=0; j<forms.size(); j++) {
                Form form = forms.get(j);
                String formName = form.getFormName();
                morphTerms.add(formName);
              } 
            } else {
              String lemmaName = lemma.getLemmaName();
              morphTerms.add(lemmaName);
            }
          }
          String[] morphTermsArray = morphTerms.toArray(new String[morphTerms.size()]);
          ArrayList<String> translatedMorphTerms = MicrosoftTranslator.translate(morphTermsArray, fromLanguage, toLanguages);
          for (int i=0; i<translatedMorphTerms.size(); i++) {
            String translatedMorphTermStr = translatedMorphTerms.get(i);
            Term translatedMorphTerm = new Term(fieldName, translatedMorphTermStr);
            TermQuery translatedMorphTermQuery = new TermQuery(translatedMorphTerm);
            queryTerms.add(translatedMorphTermQuery);
          }
        } else {
          for (int i = 0; i < lemmas.size(); i++) {
            Lemma lemma = lemmas.get(i);
            if (withAllForms) { // all word forms are put into the query as boolean or clauses: needed in fragments search when all forms should be highlighted
              ArrayList<Form> forms = lemma.getFormsList();
              for (int j=0; j<forms.size(); j++) {
                Form form = forms.get(j);
                Term formTerm = new Term(fieldName, form.getFormName());
                TermQuery morphTermQuery = new TermQuery(formTerm);
                queryTerms.add(morphTermQuery);
              } 
            } else {
              Term lemmaTerm = new Term(fieldName, lemma.getLemmaName());
              TermQuery morphTermQuery = new TermQuery(lemmaTerm);
              queryTerms.add(morphTermQuery);
            }
          }
        }
      }
    } else {
      // if it is not the morph field then do a normal query 
      if (translate) {
        String inputTermQueryField = inputTermQuery.getTerm().field();
        String inputTermQueryStr = inputTermQuery.getTerm().text();
        String[] terms = {inputTermQueryStr};
        ArrayList<String> translatedTerms = MicrosoftTranslator.translate(terms, fromLanguage, toLanguages);
        for (int i=0; i<translatedTerms.size(); i++) {
          String translatedTerm = translatedTerms.get(i);
          Term translatedTermTokenOrig = new Term(inputTermQueryField, translatedTerm);
          TermQuery translatedTermQueryInTokenOrig = new TermQuery(translatedTermTokenOrig);
          queryTerms.add(translatedTermQueryInTokenOrig);
        }
      } else {
        queryTerms.add(inputTermQuery);
      }
      //TODO ?? perhaps other fields should also be queried morphological e.g. title etc.
    }
    Query retQuery = buildBooleanShouldQuery(queryTerms);
    return retQuery;
  }

  private Query buildBooleanShouldQuery(ArrayList<TermQuery> queryTerms) throws ApplicationException {
    BooleanQuery retBooleanQuery = new BooleanQuery();
    for (int i = 0; i < queryTerms.size(); i++) {
      TermQuery termQuery = queryTerms.get(i);
      retBooleanQuery.add(termQuery, BooleanClause.Occur.SHOULD);
    }
    return retBooleanQuery;
  }
  
  private Query buildMorphQuery(BooleanQuery query, String language, boolean withAllForms, boolean translate) throws ApplicationException {
    BooleanQuery morphBooleanQuery = new BooleanQuery();
    BooleanClause[] booleanClauses = query.getClauses();
    for (int i = 0; i < booleanClauses.length; i++) {
      BooleanClause boolClause = booleanClauses[i];
      Query q = boolClause.getQuery();
      Query morphQuery = buildMorphQuery(q, language, withAllForms, translate);
      BooleanClause.Occur occur = boolClause.getOccur();
      morphBooleanQuery.add(morphQuery, occur);
    }
    return morphBooleanQuery;
  }

  public ArrayList<String> fetchTerms(String queryStr) throws ApplicationException {
    ArrayList<String> terms = null;
    String defaultQueryFieldName = "tokenOrig";
    try {
      Query query = new QueryParser(Version.LUCENE_35, defaultQueryFieldName, nodesPerFieldAnalyzer).parse(queryStr);
      terms = fetchTerms(query);
    } catch (Exception e) {
      throw new ApplicationException(e);
    }
    return terms;
  }

  /**
   * recursively fetch all terms of the query
   * 
   * @param query
   * @return
   */
  private ArrayList<String> fetchTerms(Query query) throws ApplicationException {
    ArrayList<String> terms = new ArrayList<String>();
    if (query instanceof TermQuery) {
      TermQuery termQuery = (TermQuery) query;
      String termQueryStr = termQuery.getTerm().text();
      terms.add(termQueryStr);
    } else if (query instanceof BooleanQuery) {
      BooleanQuery booleanQuery = (BooleanQuery) query;
      terms = fetchTerms(booleanQuery);
    } else {
      String queryStr = query.toString();
      terms.add(queryStr); // all other cases: PrefixQuery, PhraseQuery,
      // FuzzyQuery, TermRangeQuery, ...
    }
    return terms;
  }

  private ArrayList<String> fetchTerms(BooleanQuery query) throws ApplicationException {
    ArrayList<String> terms = new ArrayList<String>();
    BooleanClause[] booleanClauses = query.getClauses();
    for (int i = 0; i < booleanClauses.length; i++) {
      BooleanClause boolClause = booleanClauses[i];
      Query q = boolClause.getQuery();
      ArrayList<String> qTerms = fetchTerms(q);
      BooleanClause.Occur occur = boolClause.getOccur();
      if (occur == BooleanClause.Occur.SHOULD || occur == BooleanClause.Occur.MUST)
        terms.addAll(qTerms);
    }
    return terms;
  }

  public ArrayList<String> fetchTerms(String queryStr, String language) throws ApplicationException {
    ArrayList<String> terms = null;
    String defaultQueryFieldName = "tokenOrig";
    try {
      Query query = new QueryParser(Version.LUCENE_35, defaultQueryFieldName, nodesPerFieldAnalyzer).parse(queryStr);
      terms = fetchTerms(query, language);
    } catch (Exception e) {
      throw new ApplicationException(e);
    }
    return terms;
  }

  /**
   * recursively fetch all terms of the query
   * 
   * @param query
   * @return
   */
  private ArrayList<String> fetchTerms(Query query, String language) throws ApplicationException {
    ArrayList<String> terms = new ArrayList<String>();
    if (query instanceof TermQuery) {
      TermQuery termQuery = (TermQuery) query;
      terms = fetchTerms(termQuery, language);
    } else if (query instanceof BooleanQuery) {
      BooleanQuery booleanQuery = (BooleanQuery) query;
      terms = fetchTerms(booleanQuery, language);
    } else {
      String queryStr = query.toString();
      terms.add(queryStr); 
      // all other cases: PrefixQuery, PhraseQuery, FuzzyQuery, TermRangeQuery, ...
    }
    return terms;
  }

  private ArrayList<String> fetchTerms(TermQuery termQuery, String language) throws ApplicationException {
    if (language == null)
      language = "eng";
    ArrayList<String> terms = new ArrayList<String>();
    Term termQueryTerm = termQuery.getTerm();
    String term = termQuery.getTerm().text();
    String fieldName = termQueryTerm.field();
    if (fieldName != null && fieldName.equals("tokenMorph")) {
      LexHandler lexHandler = LexHandler.getInstance();
      ArrayList<Lemma> lemmas = lexHandler.getLemmas(term, "form", language, Normalizer.DICTIONARY, true); 
      // TODO : language über den translator service holen
      if (lemmas == null) {
        terms.add(term);
      } else {
        for (int i = 0; i < lemmas.size(); i++) {
          Lemma lemma = lemmas.get(i);
          ArrayList<Form> forms = lemma.getFormsList();
          for (int j = 0; j < forms.size(); j++) {
            Form form = forms.get(j);
            String formName = form.getFormName();
            terms.add(formName);
          }
        }
      }
    } else {
      terms.add(term);
    }
    return terms;
  }

  private ArrayList<String> fetchTerms(BooleanQuery query, String language) throws ApplicationException {
    ArrayList<String> terms = new ArrayList<String>();
    BooleanClause[] booleanClauses = query.getClauses();
    for (int i = 0; i < booleanClauses.length; i++) {
      BooleanClause boolClause = booleanClauses[i];
      Query q = boolClause.getQuery();
      ArrayList<String> qTerms = fetchTerms(q, language);
      BooleanClause.Occur occur = boolClause.getOccur();
      if (occur == BooleanClause.Occur.SHOULD || occur == BooleanClause.Occur.MUST)
        terms.addAll(qTerms);
    }
    return terms;
  }

  private Document getDocument(String docId) throws ApplicationException {
    Document doc = null;
    IndexSearcher searcher = null;
    try {
      makeDocumentsSearcherManagerUpToDate();
      searcher = documentsSearcherManager.acquire();
      String fieldNameDocId = "docId";
      Query queryDocId = new QueryParser(Version.LUCENE_35, fieldNameDocId, documentsPerFieldAnalyzer).parse(docId);
      TopDocs topDocs = searcher.search(queryDocId, 100000);
      topDocs.setMaxScore(1);
      if (topDocs != null && topDocs.scoreDocs != null && topDocs.scoreDocs.length > 0) {
        int docID = topDocs.scoreDocs[0].doc;
        FieldSelector docFieldSelector = getDocFieldSelector();
        doc = searcher.doc(docID, docFieldSelector);
      }
      searcher.close();
    } catch (Exception e) {
      throw new ApplicationException(e);
    } finally {
      try {
        if (searcher != null)
          documentsSearcherManager.release(searcher);
      } catch (IOException e) {
        // nothing
      }
    }
    // Do not use searcher after this!
    searcher = null;
    return doc;
  }

  public Hits moreLikeThis(String docId, int from, int to) throws ApplicationException {
    Hits hits = null;
    ArrayList<org.bbaw.wsp.cms.document.Document>  wspDocs = null;
    IndexSearcher searcher1 = null;
    IndexSearcher searcher2 = null;
    try {
      makeDocumentsSearcherManagerUpToDate();
      searcher1 = documentsSearcherManager.acquire();
      String fieldNameDocId = "docId";
      Query queryDocId = new QueryParser(Version.LUCENE_35, fieldNameDocId, documentsPerFieldAnalyzer).parse(docId);
      TopDocs topDocs = searcher1.search(queryDocId, 100000);
      topDocs.setMaxScore(1);
      int docID = -1;
      if (topDocs != null && topDocs.scoreDocs != null && topDocs.scoreDocs.length > 0) {
        docID = topDocs.scoreDocs[0].doc;
      }
      makeDocumentsSearcherManagerUpToDate();
      searcher2 = documentsSearcherManager.acquire();
      MoreLikeThis mlt = new MoreLikeThis(documentsIndexReader);  // TODO documentsIndexReader is ok ?
      mlt.setFieldNames(new String[]{"content"});  // similarity function works against these fields
      mlt.setMinWordLen(2);
      mlt.setBoost(true);
      Query queryMoreLikeThis = mlt.like(docID);
      TopDocs moreLikeThisDocs = searcher2.search(queryMoreLikeThis, 10);
      moreLikeThisDocs.setMaxScore(10);
      if (moreLikeThisDocs != null) { 
        if (wspDocs == null)
          wspDocs = new ArrayList<org.bbaw.wsp.cms.document.Document>();
        for (int i=0; i<moreLikeThisDocs.scoreDocs.length; i++) {
          int docIdent = moreLikeThisDocs.scoreDocs[i].doc;
          Document luceneDoc = searcher2.doc(docIdent);
          org.bbaw.wsp.cms.document.Document wspDoc = new org.bbaw.wsp.cms.document.Document(luceneDoc);
          wspDocs.add(wspDoc);
        }
      }
      if (wspDocs != null) {
        hits = new Hits(wspDocs, from, to);
        hits.setSize(moreLikeThisDocs.scoreDocs.length);
      }
    } catch (Exception e) {
      throw new ApplicationException(e);
    } finally {
      try {
        if (searcher1 != null)
          documentsSearcherManager.release(searcher1);
        if (searcher2 != null)
          documentsSearcherManager.release(searcher2);
      } catch (IOException e) {
        // nothing
      }
    }
    // Do not use searcher after this!
    searcher1 = null;
    searcher2 = null;
    
    return hits;
  }
  
  private IndexWriter getDocumentsWriter() throws ApplicationException {
    IndexWriter writer = null;
    String luceneDocsDirectoryStr = Constants.getInstance().getLuceneDocumentsDir();
    File luceneDocsDirectory = new File(luceneDocsDirectoryStr);
    try {
      Map<String, Analyzer> documentsFieldAnalyzers = new HashMap<String, Analyzer>();
      documentsFieldAnalyzers.put("docId", new KeywordAnalyzer());
      documentsFieldAnalyzers.put("identifier", new KeywordAnalyzer()); 
      documentsFieldAnalyzers.put("uri", new KeywordAnalyzer());
      documentsFieldAnalyzers.put("collectionNames", new StandardAnalyzer(Version.LUCENE_35));
      documentsFieldAnalyzers.put("author", new StandardAnalyzer(Version.LUCENE_35));
      documentsFieldAnalyzers.put("title", new StandardAnalyzer(Version.LUCENE_35));
      documentsFieldAnalyzers.put("language", new StandardAnalyzer(Version.LUCENE_35));
      documentsFieldAnalyzers.put("publisher", new StandardAnalyzer(Version.LUCENE_35));
      documentsFieldAnalyzers.put("date", new StandardAnalyzer(Version.LUCENE_35));
      documentsFieldAnalyzers.put("subject", new StandardAnalyzer(Version.LUCENE_35));
      documentsFieldAnalyzers.put("rights", new StandardAnalyzer(Version.LUCENE_35));
      documentsFieldAnalyzers.put("license", new StandardAnalyzer(Version.LUCENE_35));
      documentsFieldAnalyzers.put("accessRights", new StandardAnalyzer(Version.LUCENE_35));
      documentsFieldAnalyzers.put("type", new KeywordAnalyzer()); // e.g. mime type "text/xml"
      documentsFieldAnalyzers.put("pageCount", new KeywordAnalyzer());
      documentsFieldAnalyzers.put("schemaName", new StandardAnalyzer(Version.LUCENE_35));
      documentsFieldAnalyzers.put("lastModified", new KeywordAnalyzer());
      documentsFieldAnalyzers.put("tokenOrig", new StandardAnalyzer(Version.LUCENE_35));
      documentsFieldAnalyzers.put("tokenReg", new StandardAnalyzer(Version.LUCENE_35));
      documentsFieldAnalyzers.put("tokenNorm", new StandardAnalyzer(Version.LUCENE_35));
      documentsFieldAnalyzers.put("tokenMorph", new StandardAnalyzer(Version.LUCENE_35));
      documentsFieldAnalyzers.put("xmlContent", new StandardAnalyzer(Version.LUCENE_35));
      documentsFieldAnalyzers.put("content", new StandardAnalyzer(Version.LUCENE_35));
      documentsPerFieldAnalyzer = new PerFieldAnalyzerWrapper(new StandardAnalyzer(Version.LUCENE_35), documentsFieldAnalyzers);
      IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_35, documentsPerFieldAnalyzer);
      conf.setOpenMode(OpenMode.CREATE_OR_APPEND);
      conf.setRAMBufferSizeMB(300);  // 300 MB because some documents are big; 16 MB is default 
      FSDirectory fsDirectory = FSDirectory.open(luceneDocsDirectory);
      writer = new IndexWriter(fsDirectory, conf);
      writer.commit(); // when directory is empty this creates init files
    } catch (IOException e) {
      throw new ApplicationException(e);
    }
    return writer;
  }

  private IndexWriter getNodesWriter() throws ApplicationException {
    IndexWriter writer = null;
    String luceneNodesDirectoryStr = Constants.getInstance().getLuceneNodesDir();
    File luceneNodesDirectory = new File(luceneNodesDirectoryStr);
    try {
      Map<String, Analyzer> nodesFieldAnalyzers = new HashMap<String, Analyzer>();
      nodesFieldAnalyzers.put("docId", new KeywordAnalyzer());
      nodesFieldAnalyzers.put("language", new StandardAnalyzer(Version.LUCENE_35)); // language (through xml:id): e.g. "lat"
      nodesFieldAnalyzers.put("pageNumber", new KeywordAnalyzer()); // page number (through element pb): e.g. "13"
      nodesFieldAnalyzers.put("lineNumber", new KeywordAnalyzer()); // line number on the page (through element lb): e.g. "17"
      nodesFieldAnalyzers.put("elementName", new KeywordAnalyzer()); // element name: e.g. "tei:s"
      nodesFieldAnalyzers.put("elementDocPosition", new KeywordAnalyzer()); // absolute position of element in document: e.g. "4711"
      nodesFieldAnalyzers.put("elementPosition", new KeywordAnalyzer()); // position in parent node (in relation to other nodes of the same name): e.g. "5"
      nodesFieldAnalyzers.put("elementAbsolutePosition", new KeywordAnalyzer()); // absolute position in document (in relation to other nodes of the same name): e.g. "213"
      nodesFieldAnalyzers.put("elementPagePosition", new KeywordAnalyzer()); // position in relation to other nodes of the same name: e.g. "213"
      nodesFieldAnalyzers.put("xmlId", new KeywordAnalyzer()); // xml id: e.g. "4711bla"
      nodesFieldAnalyzers.put("xpath", new KeywordAnalyzer()); // xpath: e.g. "/echo[1]/text[1]/p[1]/s[5]"
      nodesFieldAnalyzers.put("tokenOrig", new StandardAnalyzer(Version.LUCENE_35));
      nodesFieldAnalyzers.put("tokenReg", new StandardAnalyzer(Version.LUCENE_35));
      nodesFieldAnalyzers.put("tokenNorm", new StandardAnalyzer(Version.LUCENE_35));
      nodesFieldAnalyzers.put("tokenMorph", new StandardAnalyzer(Version.LUCENE_35));
      nodesFieldAnalyzers.put("xmlContent", new StandardAnalyzer(Version.LUCENE_35));
      nodesPerFieldAnalyzer = new PerFieldAnalyzerWrapper(new StandardAnalyzer(Version.LUCENE_35), nodesFieldAnalyzers);
      IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_35, nodesPerFieldAnalyzer);
      conf.setOpenMode(OpenMode.CREATE_OR_APPEND);
      conf.setRAMBufferSizeMB(300);  // 300 MB because some documents are big; 16 MB is default 
      FSDirectory fsDirectory = FSDirectory.open(luceneNodesDirectory);
      writer = new IndexWriter(fsDirectory, conf);
      writer.commit();
    } catch (IOException e) {
      throw new ApplicationException(e);
    }
    return writer;
  }

  private Sort buildSort(String[] sortFieldNames, String type) {
    Sort sort = new Sort();
    ArrayList<SortField> sortFields = new ArrayList<SortField>();
    for (int i=0; i<sortFieldNames.length; i++) {
      String sortFieldName = sortFieldNames[i];
      int sortFieldType = getDocSortFieldType(sortFieldName);
      if (type.equals("node"))
        sortFieldType = getNodeSortFieldType(sortFieldName);
      String realSortFieldName = getDocSortFieldName(sortFieldName);
      SortField sortField = new SortField(realSortFieldName, sortFieldType);
      sortFields.add(sortField);
    }
    if (sortFieldNames.length == 1) {
      SortField sortField1 = sortFields.get(0);
      sort.setSort(sortField1);
    } else if (sortFieldNames.length == 2) {
      SortField sortField1 = sortFields.get(0);
      SortField sortField2 = sortFields.get(1);
      sort.setSort(sortField1, sortField2);
    } else if (sortFieldNames.length == 2) {
      SortField sortField1 = sortFields.get(0);
      SortField sortField2 = sortFields.get(1);
      SortField sortField3 = sortFields.get(2);
      sort.setSort(sortField1, sortField2, sortField3);
    }
    return sort;
  }

  private String getDocSortFieldName(String fieldName) {
    String sortFieldName = fieldName + "Sorted";
    return sortFieldName;
  }

  private int getDocSortFieldType(String fieldName) {
    int type = SortField.STRING;
    if (fieldName.equals("lastModified"))
      type = SortField.LONG;
    return type;
  }
  
  private int getNodeSortFieldType(String fieldName) {
    int type = SortField.STRING;
    if (fieldName.equals("pageNumber") || fieldName.equals("lineNumber") || fieldName.equals("elementDocPosition")) 
      type = SortField.INT;
    return type;
  }

  private FieldSelector getDocFieldSelector() {
    HashSet<String> fields = new HashSet<String>();
    fields.add("docId");
    fields.add("identifier");
    fields.add("uri");
    fields.add("collectionNames");
    fields.add("author");
    fields.add("title");
    fields.add("language");
    fields.add("publisher");
    fields.add("date");
    fields.add("subject");
    fields.add("rights");
    fields.add("license");
    fields.add("type");
    fields.add("pageCount");
    fields.add("schemaName");
    fields.add("lastModified");
    fields.add("persons");
    fields.add("places");
    fields.add("content");
    FieldSelector fieldSelector = new SetBasedFieldSelector(fields, fields);
    return fieldSelector;
  }
  
  private FieldSelector getNodeFieldSelector() {
    HashSet<String> fields = new HashSet<String>();
    fields.add("docId");
    fields.add("language");
    fields.add("pageNumber");
    fields.add("lineNumber");
    fields.add("elementName");
    fields.add("elementDocPosition");
    fields.add("elementPosition");
    fields.add("elementAbsolutePosition");
    fields.add("elementPagePosition");
    fields.add("xmlId");
    fields.add("xpath");
    fields.add("xmlContent");
    fields.add("xmlContentTokenized");
    FieldSelector fieldSelector = new SetBasedFieldSelector(fields, fields);
    return fieldSelector;
  }
  
  private SearcherManager getNewSearcherManager(IndexWriter indexWriter) throws ApplicationException {
    SearcherManager searcherManager = null;
    try {
      searcherManager = new SearcherManager(indexWriter, true, null, null);
    } catch (IOException e) {
      throw new ApplicationException(e);
    }
    return searcherManager;
  }

  private IndexReader getDocumentsReader() throws ApplicationException {
    IndexReader reader = null;
    String luceneDocsDirectoryStr = Constants.getInstance().getLuceneDocumentsDir();
    File luceneDocsDirectory = new File(luceneDocsDirectoryStr);
    try {
      FSDirectory fsDirectory = FSDirectory.open(luceneDocsDirectory);
      reader = IndexReader.open(fsDirectory, true);
    } catch (IOException e) {
      throw new ApplicationException(e);
    }
    return reader;
  }

  private void makeIndexReaderUpToDate() throws ApplicationException {
    try {
      boolean isCurrent = documentsIndexReader.isCurrent();
      if (!isCurrent) {
        documentsIndexReader = IndexReader.openIfChanged(documentsIndexReader);
      }
    } catch (IOException e) {
      throw new ApplicationException(e);
    }
  }

  private void makeDocumentsSearcherManagerUpToDate() throws ApplicationException {
    try {
      boolean isCurrent = documentsSearcherManager.isSearcherCurrent();
      if (!isCurrent) {
        documentsSearcherManager.maybeReopen();
      }
    } catch (IOException e) {
      throw new ApplicationException(e);
    }
  }

  private void makeNodesSearcherManagerUpToDate() throws ApplicationException {
    try {
      boolean isCurrent = nodesSearcherManager.isSearcherCurrent();
      if (!isCurrent) {
        nodesSearcherManager.maybeReopen();
      }
    } catch (IOException e) {
      throw new ApplicationException(e);
    }
  }

  private String toTokenizedXmlString(String xmlStr, String language) throws ApplicationException {
    String xmlPre = "<tokenized xmlns:xhtml=\"http://www.w3.org/1999/xhtml\" xmlns:mml=\"http://www.w3.org/1998/Math/MathML\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">";
    String xmlPost = "</tokenized>";
    String xmlStrTmp = xmlPre + xmlStr + xmlPost;
    StringReader xmlInputStringReader = new StringReader(xmlStrTmp);
    XmlTokenizer xmlTokenizer = new XmlTokenizer(xmlInputStringReader);
    xmlTokenizer.setLanguage(language);
    String[] outputOptions = { "withLemmas" };
    xmlTokenizer.setOutputOptions(outputOptions);
    xmlTokenizer.tokenize();
    String result = xmlTokenizer.getXmlResult();
    return result;
  }

  private String escapeLuceneChars(String inputStr) {
    String luceneCharsStr = "+-&|!(){}[]^~*?:\\"; // Lucene escape symbols
    StringBuilder retStrBuilder = new StringBuilder();
    for (int i = 0; i < inputStr.length(); i++) {
      char c = inputStr.charAt(i);
      if (luceneCharsStr.contains(String.valueOf(c)))
        retStrBuilder.append("\\");
      retStrBuilder.append(c);
    }
    return retStrBuilder.toString();
  }

  /**
   * sorgt für sinnvolle satzanfänge
   * 
   * @param fragment
   */
  private String checkHitFragment(String fragment) {
    if (fragment.startsWith(".") 
        || fragment.startsWith(":") 
        || fragment.startsWith(",") 
        || fragment.startsWith("-") 
        || fragment.startsWith(";") 
        || fragment.startsWith("?")
        || fragment.startsWith(")") 
        || fragment.startsWith("!")) {
      fragment = fragment.substring(1, fragment.length());
      // finds first occurence of a given string out.println("first index of point : "+StringUtils.indexOfAny(fragment, "."));
    }
    return fragment;
  }

}