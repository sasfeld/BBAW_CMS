/**
 * 
 */
package org.bbaw.wsp.cms.mdsystem.metadata.convert2rdf.transformer;

import de.mpg.mpiwg.berlin.mpdl.exception.ApplicationException;


/**
 * Instances of this (singleton) class transform mods files of the old WSP to OAI/ORE files.
 * @author Sascha Feldmann (wsp-shk1)
 * @date 08.10.12
 *
 */
public class ModsToRdfTransformer extends ToRdfTransformer implements IXsltTransformable {

  private static ModsToRdfTransformer instance;

  private ModsToRdfTransformer() throws ApplicationException {
    super(ToRdfTransformer.MODE_XSLT); // this is an XSLT transformer
  }
  
  /**
   * 
   * @return the only existing instance.
   * @throws ApplicationException if the mode wasn't specified correctly.
   */
  public static ModsToRdfTransformer getInstance() throws ApplicationException {
    if(instance == null) {
      return new ModsToRdfTransformer();
    }
    return instance;
  }
  
 
  
}
  
 

