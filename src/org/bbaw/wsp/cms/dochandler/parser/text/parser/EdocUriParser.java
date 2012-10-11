package org.bbaw.wsp.cms.dochandler.parser.text.parser;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.mpg.mpiwg.berlin.mpdl.exception.ApplicationException;

/**
 * A (static) class which offers methods to parse an special eDoc uri. It's used
 * by the {@link EdocUriParser}.
 * 
 * @author Sascha Feldmann (wsp-shk1)
 * @date 15.08.2012
 * 
 */
public class EdocUriParser {

  /**
   * Return the {@link URL} to the associated index.html file. This contains the
   * metadata.
   * 
   * @param docURI
   *          the (input) uri (the uri to be parsed) of the eDoc.
   * @return the {@link URL} to the index.html file
   * @throws ApplicationException
   *           if the resulting URL is invalid.
   */
  public static URL getIndexURI(final URL docURI) throws ApplicationException {
    int lastSlash = docURI.toString().lastIndexOf("pdf/");
    String newString = docURI.toString().substring(0, lastSlash) + "index.html";

    URL indexURI;
    try {
      indexURI = new URL(newString);
      return indexURI;
    } catch (MalformedURLException e) {
      throw new ApplicationException(e.getMessage());
    }
  }

  /**
   * Fetch the reference to the underlaying eDoc within an eDoc index.html file.
   * 
   * @param textOrig
   *          - the (HTML parsed) index.html content
   * @return the URL as String to the underlaying eDoc. May return null, if not
   *         defined.
   */
  public static String getDocURI(String textOrig) {
    Pattern p = Pattern.compile("(?i)URL: ([/.:\\p{Alnum}]+)");
    for (Matcher m = p.matcher(textOrig); m.find();) {
      if (m.group(1).contains("http")) {
        return m.group(1) + "pdf";
      }
    }
    return null;
  }
}
