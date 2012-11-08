package org.bbaw.wsp.cms.mdsystem.metadata.general.extractor;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import javax.xml.xpath.XPath;

import net.sf.saxon.om.ValueRepresentation;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.trans.XPathException;

import org.bbaw.wsp.cms.mdsystem.metadata.general.MetadataParserHelper;

import de.mpg.mpiwg.berlin.mpdl.exception.ApplicationException;

/**
 * This is the API class for all metadata parsers.
 * @author Sascha Feldmann (wsp-shk1)
 * @date 25.10.2012
 *
 */
public abstract class MetadataExtractor {
  /**
   * The uri to be read by the saxon compiler.
   */
  protected String uri;
  protected XPathCompiler xPathCompiler;
  protected XdmNode contextItem;

  /**
   * Create a new {@link MetadataExtractor} instance.
   * @param uri - the URI of the xml document to be parsed.
   * @throws ApplicationException if the ressource cannot be validated by Saxon.
   * @throws IllegalArgumentException
   *             if the uri is null, empty or doesn't refer to an existing
   *             file.
   */
  public MetadataExtractor(final String uri, final HashMap<String, String> namespaces) throws ApplicationException {
    if (uri == null || uri.isEmpty()) {
      throw new IllegalArgumentException(
          "The value for the parameter uri in the constructor of ModsMetadataParser mustn't be empty.");
    }
    this.uri = uri;
    
    // define the Saxon processor
    Processor processor = new Processor(false);
    xPathCompiler = processor.newXPathCompiler();
    // declare each namespace
    for (String namespace : namespaces.keySet()) {
      xPathCompiler.declareNamespace(namespace, namespaces.get(namespace));
    }
    DocumentBuilder builder = processor.newDocumentBuilder();
    try {
      contextItem = builder.build(new File(uri));
    } catch (SaxonApiException e) {
      throw new ApplicationException("Error while trying to access file using Saxon: "+uri);      
    }
  }
  
  /**
   * Compile and execute and XPath query. 
   * @param query - the {@link XPath} expression.
   * @param moreNodes - set true, if you want to fetch more nodes in a {@link List} of {@link String}.
    * @return an {@link Object} (String if moreNodes is false, String[] if moreNodes is set true)
   */
  protected Object buildXPath(final String query, final boolean moreNodes) {
    try {
      XPathExecutable x = xPathCompiler.compile(query);

      XPathSelector selector = x.load();
      selector.setContextItem(this.contextItem);
      XdmValue value = selector.evaluate();
      if (moreNodes) {
        String[] list = new String[value.size()];
        int i = 0;
        for (XdmItem xdmItem : value) {         
          list[i] = xdmItem.toString();
          ++i;
        }
        // Replace attribute chars
        return MetadataParserHelper.removeAttributeChars(list);
      }      
     
      ValueRepresentation rep = value.getUnderlyingValue();
      // Replace attribute chars
      return MetadataParserHelper.removeAttributeChars(rep.getStringValue());   
    } catch (SaxonApiException e) {
      e.printStackTrace();
      return null;
    } catch (XPathException e) {
      e.printStackTrace();
      return null;
    }
  }
}
