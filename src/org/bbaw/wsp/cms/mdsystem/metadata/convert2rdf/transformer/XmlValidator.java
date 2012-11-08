package org.bbaw.wsp.cms.mdsystem.metadata.convert2rdf.transformer;

import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import org.bbaw.wsp.cms.dochandler.parser.text.reader.ResourceReaderImpl;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import de.mpg.mpiwg.berlin.mpdl.exception.ApplicationException;

/**
 * This class offers methods to check the validation of an transformed XML. 
 * It uses the saxon library. 
 * 
 * @author Sascha Feldmann (wsp-shk1)
 * @date 22.10.2012
 *
 */
public class XmlValidator {
  
  private static ResourceReaderImpl resourceReader = new ResourceReaderImpl();
  /**
   * Set debug mode. If true, console logs will be generated.
   */
  public static boolean debug = true;

  /**
   * Check whether an xml file is valid using Saxon.
   * @param uri - the url as {@link String} of the file to be checked (XML).
   * @return true if the xml file is XML valid.
   * @throws ApplicationException if the resource/the inputstream isn't available.
   */
  public static boolean isValid(final String uri) throws ApplicationException {
    Processor processor = new Processor(false);
     
    DocumentBuilder builder = processor.newDocumentBuilder();
    XMLReader xmlReader;
    try {
      xmlReader = XMLReaderFactory.createXMLReader();
      Source source = new SAXSource(xmlReader, new InputSource(resourceReader.read(uri)));
      @SuppressWarnings("unused")
      XdmNode contextItem = builder.build(source );      
    } catch (SAXException e) {
      if(debug) {
        e.printStackTrace();
      }     
      return false;
    } catch (SaxonApiException e) {
      if(debug) {
        e.printStackTrace();
      }     
      return false;
    }       
    return true;
  }
  
}
