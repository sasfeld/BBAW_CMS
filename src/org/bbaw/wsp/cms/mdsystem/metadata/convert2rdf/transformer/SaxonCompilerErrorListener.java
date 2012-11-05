package org.bbaw.wsp.cms.mdsystem.metadata.convert2rdf.transformer;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

/**
 * This (singleton) class is the central {@link ErrorListener} for all {@link IXsltTransformable} transformers.
 * @author Sascha Feldmann (wsp-shk1)
 *
 */
public class SaxonCompilerErrorListener implements ErrorListener {

  private static SaxonCompilerErrorListener instance;
  /**
   * 
   * @return {@link SaxonCompilerErrorListener} the only existing instance.
   */
  public static SaxonCompilerErrorListener getInstance() {
    if(instance == null) {
      return new SaxonCompilerErrorListener();
    }
    return instance;
  }
  
  @Override
  public void error(TransformerException exception) throws TransformerException {
    // TODO Auto-generated method stub
    
  }
  @Override
  public void fatalError(TransformerException exception) throws TransformerException {
    // TODO Auto-generated method stub
    
  }
  @Override
  public void warning(TransformerException exception) throws TransformerException {
    // TODO Auto-generated method stub
    
  }

}
