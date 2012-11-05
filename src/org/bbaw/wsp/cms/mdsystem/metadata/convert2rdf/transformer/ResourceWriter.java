package org.bbaw.wsp.cms.mdsystem.metadata.convert2rdf.transformer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import de.mpg.mpiwg.berlin.mpdl.exception.ApplicationException;

/**
 * This class offers a method to create an {@link OutputStream} for an url. If the file doesn't exist on the target system, it will be created automaticly.
 * @author Sascha Feldmann (wsp-shk1)
 * @date 08.10.12
 *
 */
public class ResourceWriter {

  public OutputStream write(final String outputUrl) throws ApplicationException {
    File outputFile = new File(outputUrl);   
    File dir = outputFile.getParentFile();
    try {
      dir.mkdirs();
      outputFile.createNewFile();
      return new FileOutputStream(outputFile);
    } catch (IOException e) {
      throw new ApplicationException("Problem while creating output stream for the specified file "+outputUrl);
    }
    
  }

}
