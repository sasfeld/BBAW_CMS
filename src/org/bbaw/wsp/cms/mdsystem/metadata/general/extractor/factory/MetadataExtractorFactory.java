package org.bbaw.wsp.cms.mdsystem.metadata.general.extractor.factory;

import java.util.HashMap;

import org.bbaw.wsp.cms.mdsystem.metadata.general.extractor.MetadataExtractor;
import org.bbaw.wsp.cms.mdsystem.metadata.general.extractor.ModsMetadataExtractor;
import org.bbaw.wsp.cms.mdsystem.metadata.general.extractor.RdfMetadataExtractor;

import de.mpg.mpiwg.berlin.mpdl.exception.ApplicationException;

/**
 * This class offers the {@link MetadataExtractor} instances on an easy way. You
 * don't need to define the namespaces.
 * 
 * @author Sascha Feldmann (wsp-shk1)
 * @date 25.10.2012
 * 
 */
public class MetadataExtractorFactory {

  /**
   * Create a new {@link RdfMetadataExtractor}
   * 
   * @return the {@link RdfMetadataExtractor} instance.
   * @throws ApplicationException
   *           if the resource to be parsed is not validated by Saxon.
   */
  public static RdfMetadataExtractor newRdfMetadataParser(final String uri) throws ApplicationException {
    final HashMap<String, String> namespaces = new HashMap<String, String>();
    namespaces.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
    namespaces.put("dc", "http://purl.org/elements/1.1/");
    return new RdfMetadataExtractor(uri, namespaces);
  }

  /**
   * Create a new {@link ModsMetadataExtractor}.
   * 
   * @param uri
   *          the xml resource to be parsed.
   * @return the {@link ModsMetadataExtractor} instance.
   * @throws ApplicationException
   *           if the resource to be parsed is not validated by Saxon.
   */
  public static ModsMetadataExtractor newModsMetadataParser(final String uri) throws ApplicationException {
    final HashMap<String, String> namespaces = new HashMap<String, String>();
    namespaces.put("mods", "http://www.loc.gov/mods/v3");
    return new ModsMetadataExtractor(uri, namespaces);
  }
}
