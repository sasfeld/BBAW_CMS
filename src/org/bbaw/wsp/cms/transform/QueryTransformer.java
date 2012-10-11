package org.bbaw.wsp.cms.transform;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;

import javax.xml.transform.stream.StreamSource;

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

public class QueryTransformer {
  private Processor processor;
  private XsltCompiler xsltCompiler;
  private XsltTransformer xsltQueryDocument;
  
  public QueryTransformer() throws ApplicationException {
    init();
  }
  
  private void init() throws ApplicationException {
    try {
      processor = new Processor(false); 
      xsltCompiler = processor.newXsltCompiler();
      URL queryDocumentXslUrl = QueryTransformer.class.getResource("queryDocument.xsl");
      StreamSource xslStreamSource = new StreamSource(queryDocumentXslUrl.openStream());
      XsltExecutable xsltExecutable = xsltCompiler.compile(xslStreamSource);
      xsltQueryDocument = xsltExecutable.load();
    } catch (SaxonApiException e) {
      throw new ApplicationException(e);
    } catch (IOException e) {
      throw new ApplicationException(e);
    }
  }
  
  public String queryDocument(String xmlFileName, String query, String flags, String outputFormat) throws ApplicationException {
    String pageFragment = null;
    try {
      StreamSource xmlDoc = new StreamSource(xmlFileName); 
      Serializer serializer = new Serializer();
      serializer.setOutputWriter(new StringWriter());
      serializer.setOutputProperty(Serializer.Property.INDENT, "yes");
      xsltQueryDocument.setSource(xmlDoc);  // needs some time for bigger documents
      xsltQueryDocument.setDestination(serializer);
      QName queryQName = new QName("query");
      XdmValue queryXdmValue = new XdmAtomicValue(query);
      QName flagsQName = new QName("flags");
      XdmValue flagsXdmValue = new XdmAtomicValue(flags);
      QName outputFormatQName = new QName("outputFormat");
      XdmValue outputFormatXdmValue = new XdmAtomicValue(outputFormat);
      xsltQueryDocument.setParameter(queryQName, queryXdmValue);
      xsltQueryDocument.setParameter(flagsQName, flagsXdmValue);
      xsltQueryDocument.setParameter(outputFormatQName, outputFormatXdmValue);
      xsltQueryDocument.transform();  // needs some time for bigger documents
      pageFragment = serializer.getOutputDestination().toString();
    } catch (Exception e) {
      throw new ApplicationException(e);
    }
    return pageFragment;
  }
}
