/**
 * This package contains the transformation classes to transform to our destination OAI/ORE rdf file.
 */
package org.bbaw.wsp.cms.mdsystem.metadata.convert2rdf.transformer;

import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

import org.bbaw.wsp.cms.dochandler.parser.text.reader.ResourceReaderImpl;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import de.mpg.mpiwg.berlin.mpdl.exception.ApplicationException;

/**
 * This abstract class is the central api for all transformations to RDF.
 * 
 * @author Sascha Feldmann (wsp-shk1)
 * @date 08.10.12
 * 
 */
public abstract class ToRdfTransformer {
  /**
   * Use this option to do a direct conversion (e.g.: from eDoc to RDF).
   */
  public static final String MODE_DIRECT = "hard-coded conversion";

  /**
   * Use this option to do an XSLT transformation
   */
  public static final String MODE_XSLT = "XSLT transformation";

  public static final String TRANSFORMER_CREATOR_NAME = "Wissensspeicher";

  public static final String TRANSFORMER_CREATOR_URL = "http://wsp.bbaw.de";

  private String xslInput;

  private String transformMode;

  protected ResourceReaderImpl resourceReader;

  protected ResourceWriter resourceWriter;

  /**
   * Create a new instance.
   * @param transformMode - the mode of the transformer (XSLT transformation or hard-coded conversion).
   * @throws ApplicationException if the entered mode isn't valid.
   */
  public ToRdfTransformer(final String transformMode) throws ApplicationException {
    if(!transformMode.equals(MODE_DIRECT) && !transformMode.equals(MODE_XSLT)) {
      throw new ApplicationException("The mode isn't available in ToRdfTransformer.");
    }
    this.transformMode = transformMode;
    this.resourceReader = new ResourceReaderImpl();
    this.resourceWriter = new ResourceWriter();
  }
  
  /**
   * Do an transformation job. 
   * The kind of job (XSLT transformation or direct parsing) depends on the subclass. 
   * @param inputUrl - the url of the resource to be transformed.
   * @throws ApplicationException
   */
  public void doTransformation(final String inputUrl, final String outputUrl) throws ApplicationException {
    if (inputUrl == null || inputUrl.isEmpty()) {
      throw new ApplicationException("The value for the parameter inputUrl in ToRdfTransformer.doTransformation() mustn't be null or empty!");
    }
    
    final InputStream inputFileStream = this.resourceReader.read(inputUrl);   
    
    
    if(this.transformMode.equals(MODE_XSLT)) {
      if(this.getXslInput() == null) {
        throw new ApplicationException("You must specify an XSLT stylesheet before transforming in XSLT mode!");
      }
      final InputStream xslInput = this.resourceReader.read(this.getXslInput());
      
      final OutputStream xmlOutput = this.resourceWriter.write(outputUrl);
      this.transform(inputFileStream, xslInput, xmlOutput );
    }
    else if(this.transformMode.equals(MODE_DIRECT)) {
      // Do a direct transformation - this is specified by the subclass
    }
        
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * bbaw.wsp.parser.metadata.transformer.IXsltTransformable#transform(java.
   * io.InputStream, java.io.InputStream, java.io.InputStream)
   */
  public void transform(final InputStream xmlInput, final InputStream xslStylesheet, final OutputStream xmlOutput) throws ApplicationException {
    if (xmlInput == null || xslStylesheet == null || xmlOutput == null) {
      throw new ApplicationException("The values for the parameters in ToRdfTransformer.transform() mustn't be null.");
    }

    try {
      XMLReader xmlReader = XMLReaderFactory.createXMLReader();
      // Xslt file
      Source xsltSource = new SAXSource(xmlReader, new InputSource(xslStylesheet));

      Processor processor = new Processor(false);
      XsltCompiler compiler = processor.newXsltCompiler();
      compiler.setErrorListener(SaxonCompilerErrorListener.getInstance());

      XsltExecutable executable = compiler.compile(xsltSource);

      // Input file
      final StreamSource inputSource = new StreamSource(xmlInput);

      // Serializer for output file
      final Serializer serializer = new Serializer();
      serializer.setOutputProperty(Serializer.Property.METHOD, "xml");
      serializer.setOutputStream(xmlOutput);

      // Do transformation
      XsltTransformer transformer = executable.load();
      transformer.setSource(inputSource);
      transformer.setDestination(serializer);
      transformer.transform();
    } catch (SaxonApiException | SAXException e) {
      throw new ApplicationException("Problem while transforming -- " + e.getMessage());
    }    
  }

  /**
   * Check if an file is XML valid.
   * @param xmlOutput - the url as String.
   * @return 
   * @throws ApplicationException if the source isn't available.
   */
  public boolean checkValidation(final String xmlOutput) throws ApplicationException {
    return XmlValidator.isValid(xmlOutput);
  }
  
  public String getXslInput() {
    return xslInput;
  }

  public void setXslInput(String xslInput) {
    this.xslInput = xslInput;
  }
}
