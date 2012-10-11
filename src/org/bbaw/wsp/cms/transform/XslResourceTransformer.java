package org.bbaw.wsp.cms.transform;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;

import javax.xml.transform.stream.StreamSource;

import de.mpg.mpiwg.berlin.mpdl.exception.ApplicationException;


import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

public class XslResourceTransformer {
  private Processor processor;
  private XsltCompiler xsltCompiler;
  private Serializer serializer;
  private XsltTransformer xsltTransformer;
  
  public XslResourceTransformer(String resourceName) throws ApplicationException {
    init(resourceName);
  }
  
  private void init(String resourceName) throws ApplicationException {
    try {
      processor = new Processor(false); 
      xsltCompiler = processor.newXsltCompiler();
      URL xslUrl = XslResourceTransformer.class.getResource(resourceName);
      StreamSource xslStreamSource = new StreamSource(xslUrl.openStream());
      XsltExecutable xsltExecutable = xsltCompiler.compile(xslStreamSource);
      xsltTransformer = xsltExecutable.load();
    } catch (SaxonApiException e) {
      throw new ApplicationException(e);
    } catch (IOException e) {
      throw new ApplicationException(e);
    }
  }
  
  public String transform(String xmlFileName) throws ApplicationException {
    String result = null;
    try {
      StreamSource xmlDoc = new StreamSource(xmlFileName); 
      serializer = new Serializer();
      serializer.setOutputWriter(new StringWriter());
      xsltTransformer.setSource(xmlDoc);  // needs some time for bigger documents
      xsltTransformer.setDestination(serializer);
      xsltTransformer.transform();  // needs some time for bigger documents
      result = serializer.getOutputDestination().toString();
    } catch (Exception e) {
      throw new ApplicationException(e);
    }
    return result;
  }

  public String transformStr(String xmlStr) throws ApplicationException {
    String retStr = null;
    try {
      StringReader inputStrReader = new StringReader(xmlStr);
      StreamSource xmlDoc = new StreamSource(inputStrReader);
      serializer = new Serializer();
      serializer.setOutputWriter(new StringWriter());
      xsltTransformer.setSource(xmlDoc);  // needs some time for bigger documents
      xsltTransformer.setDestination(serializer);
      xsltTransformer.transform();  // needs some time for bigger documents
      retStr = serializer.getOutputDestination().toString();
    } catch (Exception e) {
      throw new ApplicationException(e);
    }
    return retStr;
  }

  public void setParameter(QName name, XdmValue value) throws ApplicationException {
    try {
      xsltTransformer.setParameter(name, value); 
    } catch (Exception e) {
      throw new ApplicationException(e);
    }
  }

  public void setOutputProperty(Serializer.Property property, String value) throws ApplicationException {
    try {
      serializer.setOutputProperty(property, value);
    } catch (Exception e) {
      throw new ApplicationException(e);
    }
  }
}
