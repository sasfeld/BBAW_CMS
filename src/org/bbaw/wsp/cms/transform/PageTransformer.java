package org.bbaw.wsp.cms.transform;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;

import javax.xml.transform.stream.StreamSource;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.bbaw.wsp.cms.document.MetadataRecord;

import de.mpg.mpiwg.berlin.mpdl.exception.ApplicationException;


import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

public class PageTransformer {
  private Processor processor;
  private XsltCompiler xsltCompiler;
  private XsltTransformer pageTeiTransformer;
  private XsltTransformer pageEchoTransformer;
  private XsltTransformer pageArchimedesTransformer;
  private XsltTransformer pageXhtmlTransformer;
  private XsltTransformer pageXmlTransformer;
  
  public PageTransformer() throws ApplicationException {
    init();
  }
  
  private void init() throws ApplicationException {
    try {
      processor = new Processor(false); 
      xsltCompiler = processor.newXsltCompiler();
      URL getFragmentXslUrl = PageTransformer.class.getResource("pageTei.xsl");
      StreamSource xslStreamSource = new StreamSource(getFragmentXslUrl.openStream());
      XsltExecutable xsltExecutable = xsltCompiler.compile(xslStreamSource);
      pageTeiTransformer = xsltExecutable.load();
      getFragmentXslUrl = PageTransformer.class.getResource("pageEcho.xsl");
      xslStreamSource = new StreamSource(getFragmentXslUrl.openStream());
      xsltExecutable = xsltCompiler.compile(xslStreamSource);
      pageEchoTransformer = xsltExecutable.load();
      getFragmentXslUrl = PageTransformer.class.getResource("pageArchimedes.xsl");
      xslStreamSource = new StreamSource(getFragmentXslUrl.openStream());
      xsltExecutable = xsltCompiler.compile(xslStreamSource);
      pageArchimedesTransformer = xsltExecutable.load();
      getFragmentXslUrl = PageTransformer.class.getResource("pageXhtml.xsl");
      xslStreamSource = new StreamSource(getFragmentXslUrl.openStream());
      xsltExecutable = xsltCompiler.compile(xslStreamSource);
      pageXhtmlTransformer = xsltExecutable.load();
      getFragmentXslUrl = PageTransformer.class.getResource("pageXml.xsl");
      xslStreamSource = new StreamSource(getFragmentXslUrl.openStream());
      xsltExecutable = xsltCompiler.compile(xslStreamSource);
      pageXmlTransformer = xsltExecutable.load();
    } catch (SaxonApiException e) {
      throw new ApplicationException(e);
    } catch (IOException e) {
      throw new ApplicationException(e);
    }
  }
  
  public String transform(String inputStr, MetadataRecord mdRecord, String mode, String page, String normMethod, String outputFormat) throws ApplicationException {
    String pageFragment = null;
    String docId = mdRecord.getDocId();
    String schemaName = mdRecord.getSchemaName();
    try {
      XsltTransformer transformer = null;
      if (schemaName != null && schemaName.equals("tei") && outputFormat.equals("html"))
        transformer = pageTeiTransformer;
      else if (schemaName != null && schemaName.equals("echo") && outputFormat.equals("html"))
        transformer = pageEchoTransformer;
      else if (schemaName != null && schemaName.equals("archimedes") && outputFormat.equals("html"))
        transformer = pageArchimedesTransformer;
      else if (schemaName != null && schemaName.equals("xhtml") && outputFormat.equals("html"))
        transformer = pageXhtmlTransformer;
      else if (outputFormat.equals("xmlDisplay"))
        transformer = pageXmlTransformer;
      else
        transformer = pageTeiTransformer;
      StringReader inputStrReader = new StringReader(inputStr);
      StreamSource xmlDoc = new StreamSource(inputStrReader);
      Serializer serializer = new Serializer();
      serializer.setOutputWriter(new StringWriter());
      serializer.setOutputProperty(Serializer.Property.SAXON_STYLESHEET_VERSION, "2.0");
      serializer.setOutputProperty(Serializer.Property.METHOD, "xml");
      serializer.setOutputProperty(Serializer.Property.MEDIA_TYPE, "text/html");
      serializer.setOutputProperty(Serializer.Property.INDENT, "no");
      serializer.setOutputProperty(Serializer.Property.OMIT_XML_DECLARATION, "yes");
      serializer.setOutputProperty(Serializer.Property.ENCODING, "utf-8");
      transformer.setSource(xmlDoc);  
      transformer.setDestination(serializer);
      QName docIdQName = new QName("docId");
      XdmValue docIdXdmValue = new XdmAtomicValue(docId);
      QName modeQName = new QName("mode");
      XdmValue modeXdmValue = new XdmAtomicValue(mode);
      QName pageQName = new QName("page");
      XdmValue pageXdmValue = new XdmAtomicValue(page);
      QName normalizationQName = new QName("normalization");
      XdmValue normalizationXdmValue = new XdmAtomicValue(normMethod);
      transformer.setParameter(docIdQName, docIdXdmValue);
      transformer.setParameter(modeQName, modeXdmValue);
      transformer.setParameter(pageQName, pageXdmValue);
      transformer.setParameter(normalizationQName, normalizationXdmValue);
      transformer.transform(); 
      pageFragment = serializer.getOutputDestination().toString();
    } catch (Exception e) {
      throw new ApplicationException(e);
    }
    return pageFragment;
  }

  private boolean checkUri(URL url, int timeoutMilliseconds) throws ApplicationException {
    boolean isOk = true;
    HttpClient httpClient = new HttpClient();
    GetMethod method = null;
    try {
      String uriStr = url.toExternalForm();
      httpClient.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, new Integer(timeoutMilliseconds));
      httpClient.getParams().setParameter(HttpClientParams.CONNECTION_MANAGER_TIMEOUT, new Long(timeoutMilliseconds));
      method = new GetMethod(uriStr);
      method.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, new Integer(timeoutMilliseconds));
      method.setFollowRedirects(true); 
      httpClient.executeMethod(method); 
    } catch (IOException e) {
      isOk = false;  // if timeout exception is thrown
    } finally {
      if (method != null) {
        method.releaseConnection();
      }
    }
    return isOk;
  }

}
