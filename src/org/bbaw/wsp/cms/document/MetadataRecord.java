package org.bbaw.wsp.cms.document;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

import de.mpg.mpiwg.berlin.mpdl.lt.text.tokenize.XmlTokenizerContentHandler;

public class MetadataRecord {
  private String docId; // local id: document identifier in index system, e.g. /echo/la/Benedetti_1585.xml
  private String identifier; // local id: identifier field in documents metadata: e.g. /echo:echo/echo:metadata/dcterms:identifier
  private String uri; // global id: document URI (uniform resource identifier), e.g. http://de.wikipedia.org/wiki/Ramones
  private String webUri; // web url, e.g. "http://telota.bbaw.de/ig/IG I3 501"
  private String language;
  private String creator; // author
  private String title;
  private String description; // abstract etc.
  private String subject; // subject keywords from the title or description or content or subject lists (thesaurus etc.)
  private String ddc; // Dewey Decimal Classification
  private String swd; // Schlagwortnormdatei
  private String publisher; // publisher with place: e.g. Springer, New York
  private String type; // mime type: e.g. text/xml // TODO ist eigentlich das Feld "format" --> zus. instance variable "format" definieren
  private String rights; // e.g. open access
  private Date date; // creation date, modification date, etc.
  private String license; // e.g. http://echo.mpiwg-berlin.mpg.de/policy/oa_basics/declaration
  private String accessRights; // e.g. free
  private String urn; // e.g. the KOBV urn, e.g. urn:nbn:de:kobv:b4360-10020
  private String documentType; // e.g. the KOBV "Dokumentenart"
  private String isbn; // e.g. the KOBV ISBN
  private Date creationDate; // e.g. the KOBV "Erstellungsjahr"
  private Date publishingDate; // e.g. the KOBV "Publikationsdatum"
  private String collectionNames; // e.g. "edoc"
  private String tokenOrig; // original fulltext token of the documents fulltext
  private String tokenReg; // regularized fulltext token of the documents fulltext
  private String tokenNorm; // normalized fulltext token of the documents fulltext
  private String tokenMorph; // morphological fulltext token of the documents fulltext
  private String contentXml; // original xml content of the documents file
  private String content; // original text content of the documents file (without xml tags)
  private ArrayList<XmlTokenizerContentHandler.Element> xmlElements;  // elements of the xml documents
  private String schemaName; // e.g. TEI, echo, html, or archimedes
  private Date lastModified;
  private int pageCount;
  private String persons;
  private String places;
  private Hashtable<String, XQuery> xQueries; // dynamic xQueries of xml documents (with name, code, result)
  private String realDocUrl; // e.g. the URL to the eDoc (not the index.html)
  private String inPublication; // e.g. the KOBV publication (in: ...)

  public String getRealDocUrl() {
    return realDocUrl;
  }

  public void setRealDocUrl(String realDocUrl) {
    this.realDocUrl = realDocUrl;
  }

  public String getDocumentType() {
    return documentType;
  }

  public void setDocumentType(String documentType) {
    this.documentType = documentType;
  }

  public String getIsbn() {
    return isbn;
  }

  public void setIsbn(String isbn) {
    this.isbn = isbn;
  }

  public Date getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  public Date getPublishingDate() {
    return publishingDate;
  }

  public void setPublishingDate(Date publishingDate) {
    this.publishingDate = publishingDate;
  }

  public String getSwd() {
    return swd;
  }

  public void setSwd(String swd) {
    this.swd = swd;
  }

  public String getDdc() {
    return ddc;
  }

  public void setDdc(String ddc) {
    this.ddc = ddc;
  }

  public String getUrn() {
    return urn;
  }

  public void setUrn(String urn) {
    this.urn = urn;
  }

  public String getDocId() {
    return docId;
  }

  public void setDocId(String docId) {
    this.docId = docId;
  }

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public String getRights() {
    return rights;
  }

  public void setRights(String rights) {
    this.rights = rights;
  }

  public int getPageCount() {
    return pageCount;
  }

  public void setPageCount(int pageCount) {
    this.pageCount = pageCount;
  }

  public String getLicense() {
    return license;
  }

  public void setLicense(String license) {
    this.license = license;
  }

  public String getAccessRights() {
    return accessRights;
  }

  public void setAccessRights(String accessRights) {
    this.accessRights = accessRights;
  }

  public String getCreator() {
    return creator;
  }

  public void setCreator(String creator) {
    this.creator = creator;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public String getYear() {
    String year = null;
    if (date != null) {
      Calendar cal = Calendar.getInstance();
      cal.setTime(date);
      int iYear = cal.get(Calendar.YEAR);
      year = "" + iYear;
    }
    return year;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public String getCollectionNames() {
    return collectionNames;
  }

  public void setCollectionNames(String collectionNames) {
    this.collectionNames = collectionNames;
  }

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public String getPublisher() {
    return publisher;
  }

  public void setPublisher(String publisher) {
    this.publisher = publisher;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getTokenOrig() {
    return tokenOrig;
  }

  public void setTokenOrig(String tokenOrig) {
    this.tokenOrig = tokenOrig;
  }

  public String getTokenReg() {
    return tokenReg;
  }

  public void setTokenReg(String tokenReg) {
    this.tokenReg = tokenReg;
  }

  public String getTokenNorm() {
    return tokenNorm;
  }

  public void setTokenNorm(String tokenNorm) {
    this.tokenNorm = tokenNorm;
  }

  public String getTokenMorph() {
    return tokenMorph;
  }

  public void setTokenMorph(String tokenMorph) {
    this.tokenMorph = tokenMorph;
  }

  public String getContentXml() {
    return contentXml;
  }

  public void setContentXml(String contentXml) {
    this.contentXml = contentXml;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public ArrayList<XmlTokenizerContentHandler.Element> getXmlElements() {
    return xmlElements;
  }

  public void setXmlElements(
      ArrayList<XmlTokenizerContentHandler.Element> xmlElements) {
    this.xmlElements = xmlElements;
  }

  public String getSchemaName() {
    return schemaName;
  }

  public void setSchemaName(String schemaName) {
    this.schemaName = schemaName;
  }

  public Date getLastModified() {
    return lastModified;
  }

  public void setLastModified(Date lastModified) {
    this.lastModified = lastModified;
  }

  public String getPersons() {
    return persons;
  }

  public void setPersons(String persons) {
    this.persons = persons;
  }

  public String getPlaces() {
    return places;
  }

  public void setPlaces(String places) {
    this.places = places;
  }

  public String getWebUri() {
    return webUri;
  }

  public void setWebUri(String webUri) {
    this.webUri = webUri;
  }

  public Hashtable<String, XQuery> getxQueries() {
    return xQueries;
  }

  public void setxQueries(Hashtable<String, XQuery> xQueries) {
    this.xQueries = xQueries;
  }

  public String getInPublication() {
    return inPublication;
  }

  public void setInPublication(String inPublication) {
    this.inPublication = inPublication;
  }
  
  @Override
  public String toString() {
    return "MetadataRecord [docId=" + docId + ", identifier=" + identifier + ", uri=" + uri + ", language=" + language + ", creator=" + creator + ", title=" + title + ", description=" + description + ", subject=" + subject + ", ddc=" + ddc + ", swd=" + swd + ", publisher=" + publisher + ", type=" + type + ", rights=" + rights + ", date=" + date + ", license=" + license + ", accessRights=" + accessRights + ", collectionNames=" + collectionNames + ", schemaName=" + schemaName + ", lastModified=" + lastModified + ", pageCount=" + pageCount + ", persons=" + persons + ", places=" + places + ", urn=" + urn + ", documentType=" + documentType + ", isbn=" + isbn + ", creationDate=" + creationDate + ", publishingDate=" + publishingDate + "]";
  }
}