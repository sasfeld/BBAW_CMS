package org.bbaw.wsp.cms.dochandler.parser.text.parser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bbaw.wsp.cms.document.MetadataRecord;
import org.bbaw.wsp.cms.dochandler.parser.text.reader.IResourceReader;
import org.bbaw.wsp.cms.dochandler.parser.text.reader.ResourceReaderImpl;

import de.mpg.mpiwg.berlin.mpdl.exception.ApplicationException;

/**
 * This tool class provides methods to fetch DC fields into a given
 * {@link MetadataRecord}. Last change: - Added fields documentType, isbn,
 * creationDate, publishingDate - 06.09.12: throws {@link ApplicationException}
 * now - Added methods to check if a file is an eDoc index.html file
 * 
 * @author Sascha Feldmann (wsp-shk1)
 * 
 */
public class EdocIndexMetadataFetcherTool {
  private static IResourceReader reader = new ResourceReaderImpl();

  /**
   * This class reads from an URL and fetches the DC tags directly (with String
   * operations).
   * 
   * It's designed for the eDoc server.
   * 
   * @param srcUrl
   *          - the basic Url as String
   * @param mdRecord
   *          - the {@link MetadataRecord} to fill
   *          
   * Last change: bugfixed the publishingDate 
   * @return the complete {@link MetadataRecord}
   * @throws ApplicationException
   */
  public static MetadataRecord fetchHtmlDirectly(final String srcUrl, final MetadataRecord mdRecord) throws ApplicationException {
    InputStream in;
    try {
      in = reader.read(srcUrl);
      Scanner scanner = new Scanner(in);
      scanner.useDelimiter("\n"); // delimiter via line break
      StringBuilder builder = new StringBuilder();
      while (scanner.hasNext()) {
        builder.append(scanner.next()); // concat to one String
      }
      String line = builder.toString();
      StringBuilder creatorBuilder = new StringBuilder(); // fix: more than one
                                                          // creator
      Pattern p = Pattern.compile("(?i)<META NAME=\"(.*?)\" CONTENT=\"(.*?)\">(?i)"); // meta
      // pattern
      for (Matcher m = p.matcher(line); m.find();) {
        String tag = m.group(1);
        String content = m.group(2);
        if (tag.equals("DC.Date.Creation_of_intellectual_content")) { // creation
                                                                      // date
          Calendar cal = new GregorianCalendar();
          cal.set(Calendar.YEAR, Integer.parseInt(content));
          cal.set(Calendar.DAY_OF_YEAR, 1);
          cal.set(Calendar.HOUR, 0);
          cal.set(Calendar.MINUTE, 0);
          cal.set(Calendar.SECOND, 0);
          cal.set(Calendar.MILLISECOND, 0);
          mdRecord.setCreationDate(cal.getTime());
        } else if (tag.equals("DC.Title")) {
          mdRecord.setTitle(content);
        } else if (tag.equals("DC.Creator")) {
          if (creatorBuilder.toString().length() == 0) {
            creatorBuilder.append(content);
          } else {
            creatorBuilder.append(" ; " + content);
          }
          mdRecord.setCreator(creatorBuilder.toString());
        } else if (tag.equals("DC.Subject")) {
          mdRecord.setSwd(content); // DC.Subject follows the
                                    // Schlagwortnormdatei
        } else if (tag.equals("DC.Description")) {
          mdRecord.setDescription(content);
        } else if (tag.equals("DC.Identifier")) {
          if (content.contains("http://")) {
            mdRecord.setUri(content);
          } else if (content.contains("urn:")) {
            mdRecord.setUrn(content);
          }
        }
      }

      Pattern p2 = Pattern.compile("(?i)<TD class=\"frontdoor\" valign=\"top\"><B>(.*?)</B></TD>.*?<TD class=\"frontdoor\" valign=\"top\">(.*?)</TD><");
      for (Matcher m = p2.matcher(line); m.find();) {
        String key = m.group(1);
        String value = m.group(2).trim();
        if (key.contains("pdf-Format")) {
          Pattern pLink = Pattern.compile("(?i)<a href=\"(.*?)(\".*?)\">.*?</a>");
          Matcher mLink = pLink.matcher(key);
          mLink.find();
          mdRecord.setRealDocUrl(mLink.group(1));
          // System.out.println(mLink.group(1));
        } else if (key.contains("Freie Schlagwörter")) {
          mdRecord.setSubject(value);
        } else if (key.contains("DDC-Sachgruppe")) {
          mdRecord.setDdc(value);
        } else if (key.contains("Sprache")) {
          mdRecord.setLanguage(value);
        } else if (key.contains("Dokumentart")) {
          mdRecord.setDocumentType(value);
        } else if (key.contains("Publikationsdatum")) {
          final int day = Integer.parseInt(value.substring(0, value.indexOf(".")));
          final int month = Integer.parseInt(value.substring(value.indexOf(".") + 1, value.lastIndexOf(".")));
          final int year = Integer.parseInt(value.substring(value.lastIndexOf(".") + 1));

          Calendar cal = new GregorianCalendar();
          cal.set(year, month-1, day); // bugfixed: month is 0 based!          
          mdRecord.setPublishingDate(cal.getTime());
        } else if (key.contains("ISBN")) {
          mdRecord.setIsbn(value);
        } else if (key.contains("Institut")) {
          mdRecord.setPublisher(value);
        } else if (key.contains("Collection")) {
          Pattern pColl = Pattern.compile("(?i)<a.*?>(.*?)</a>");
          Matcher mColl = pColl.matcher(value);
          mColl.find();
          String collections = mColl.group(1);

          mdRecord.setCollectionNames(collections);
        }
      }
      // Bugfix: Institut
      Pattern p3 = Pattern.compile("(?i)<TD class=\"frontdoor\" valign=\"top\"><B>Institut:</B></TD>.*?<TD class=\"frontdoor\" valign=\"top\">(.*?)</TD><");
      for (Matcher m = p3.matcher(line); m.find();) {
        mdRecord.setPublisher(m.group(1));
      }

      in.close();
      return mdRecord;
    } catch (IOException e) {
      throw new ApplicationException("Problem while parsing " + srcUrl + " for DC tags " + e.getMessage());
    }

  }

  /**
   * Check if the file is an index.html file to an eDoc.
   * 
   * LastChange: Performance optimation - check URI before reading from input stream
   * 
   * @param uri
   *          - the URL as string to the index.html file.
   * @return true if the index.html file belongs to an eDoc.
   * @throws ApplicationException
   *           if the stream couldn't get opened.
   */
  public static boolean isEDocIndex(final String uri) throws ApplicationException {
    if((uri.contains("edoc.bbaw.de") || uri.endsWith(".html")) && !uri.endsWith(".pdf")) {
      InputStream in = reader.read(uri);
      if (in != null) {
        Scanner scanner = new Scanner(in);
        scanner.useDelimiter("\n");
        StringBuilder builder = new StringBuilder();
        while (scanner.hasNext()) {
          builder.append(scanner.next());
        }
        String content = builder.toString();
        Pattern p = Pattern.compile("(?i)<META NAME=\"(.*?)\" CONTENT=\"(.*?)\">(?i)");
        for (Matcher m = p.matcher(content); m.find();) {
          String tag = m.group(1);
          String value = m.group(2);
          if (tag.equals("DC.Identifier") && value.contains("edoc.bbaw.de/")) {
            return true;
          }
        }
      }    
    }    
    return false;
  }

  /**
   * Check if the resource is an eDoc.
   * 
   * @param uri
   *          - the resource's URI.
   * @return true if the resource is an (KOBV) eDoc.
   */
  public static boolean isEDoc(String uri) {
    // test local file system
    File f = new File(uri);

    if (f.getParentFile().getName().equals("pdf") && new File(f.getParentFile().getParentFile(), "index.html").exists()) {
      return true;
    } else { // test HTTP
      try {
        URL url = new URL(uri);
        int pos = url.toExternalForm().lastIndexOf("/pdf");
        if (pos != -1) {
          String newUrl = url.toExternalForm().substring(0, pos) + "/index.html";

          URL indexUrl = new URL(newUrl);
          @SuppressWarnings("unused")
          URLConnection conn = indexUrl.openConnection();

          return true;
        }
        return false;
      } catch (MalformedURLException e) {
        return false;
      } catch (IOException e) {
        return false;
      }
    }
  }

  /**
   * Fetch the eDoc's id as it's stored on the file system.
   * This id can be used for an OAI/ORE aggregation for example.
   * @param eDocUrl {@link String} the URL to the eDoc. This will be parsed for the id.
   * @return {@link Integer} the docID or -1 if the ID couldn'T be parsed.
   * @throws ApplicationException 
   */
  public static int getDocId(final String eDocUrl) throws ApplicationException {
    if(eDocUrl == null || eDocUrl.isEmpty()) {
      throw new ApplicationException("The value for the eDocUrl in getDocId mustn't be null or empty.");
    }
      Pattern p = Pattern.compile(".*/(.*?)/pdf/.*");
      Matcher m = p.matcher(eDocUrl);
      if(m.find()) {
        return Integer.parseInt(m.group(1));
      }
      return -1;
  }
}
