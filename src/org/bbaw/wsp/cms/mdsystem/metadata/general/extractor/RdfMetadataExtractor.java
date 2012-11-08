package org.bbaw.wsp.cms.mdsystem.metadata.general.extractor;

import java.util.HashMap;

import de.mpg.mpiwg.berlin.mpdl.exception.ApplicationException;

/**
 * This class is able to parse a rdf file that is used as input for the triple
 * store.
 * 
 * @author Sascha Feldmann (wsp-shk1)
 * @date 25.10.2012
 * 
 * Last change: 08.11.2012
 * - added getXmlBaseValue()-method to extract the first matching xml base attribut (which contains the real URL)
 */
public class RdfMetadataExtractor extends MetadataExtractor {

  /**
   * Create a new ModsMetadataParser instance.
   * 
   * @param uri
   *          - the URI to the knowledge store metadata record.
   * @throws ApplicationException
   *           if the resource to be parsed is not validated by Saxon.
   * @throws IllegalArgumentException
   *           if the uri is null, empty or doesn't refer to an existing file.
   */
  public RdfMetadataExtractor(final String uri, final HashMap<String, String> namespaces) throws ApplicationException {
    super(uri, namespaces);
  }

  /**
   * Build path to the rdf:about attribute. It contains the identifier of the
   * described resource.
   * 
   * @return {@link String} the attribute's value (the uri of the described
   *         resource)
   */
  public String getRdfAboutValue() {
    String erg = (String) buildXPath("//rdf:Description[1]/@rdf:about", false); // First
                                                                             // node
    return erg;
  }
  
  /**
   * Build path to the xml:base attribute. The first matching attribute will be returned.
   * @return {@link String} the xml:base attribute's value (the uri of the described
   *         resource)
   */
  public String getXmlBaseValue() {
    String erg = (String) buildXPath("//*/@xml:base", false); 
    return erg;
  }

}
