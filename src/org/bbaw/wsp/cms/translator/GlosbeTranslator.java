package org.bbaw.wsp.cms.translator;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;

import de.mpg.mpiwg.berlin.mpdl.exception.ApplicationException;
import de.mpg.mpiwg.berlin.mpdl.xml.xquery.XQueryEvaluator;

public class GlosbeTranslator {
  private static GlosbeTranslator instance;
	private String[] DEST_LANGUAGES = {"eng", "deu", "fra"};
	private String protocol = "http"; 
  private String host = "glosbe.com"; 
  private int port = 80; 
  private String servlet = "/gapi/translate";
  private HttpClient httpClient;  // local http client
  private XQueryEvaluator xQueryEvaluator;

  public static GlosbeTranslator getInstance() throws ApplicationException {
    if (instance == null) {
      instance = new GlosbeTranslator();
      instance.init();
    }
    return instance;
  }

  private void init() throws ApplicationException {
    httpClient = new HttpClient();
    xQueryEvaluator = new XQueryEvaluator();  }

	public String[] getDestLanguages() {
    return DEST_LANGUAGES;
  }

  public ArrayList<String> translate(String[] query, String fromLanguageStr, String[] toLanguagesStr) throws ApplicationException {
    ArrayList<String> translations = new ArrayList<String>();
    for (int i=0; i<toLanguagesStr.length; i++) {
      String toLanguageStr = toLanguagesStr[i];
      String[] translationsArray = translate(query, fromLanguageStr, toLanguageStr);
      for (int j=0; j<translationsArray.length; j++) {
        String translation = translationsArray[j];
        translations.add(translation);
      }
    }
    return translations;
  }
  
  public String[] translate(String[] query, String fromLanguageStr, String toLanguageStr) throws ApplicationException {
    if (fromLanguageStr == null)
      throw new ApplicationException("Translator: toLanguageStr is null");
    if (toLanguageStr == null)
      throw new ApplicationException("Translator: fromLanguageStr is null");
    String[] translations = null;
    try {
      ArrayList<String> translationsArrayList = new ArrayList<String>();
      for (int i=0; i<query.length; i++) {
        String queryStr = query[i].toLowerCase();
        String request = servlet + "?from=" + fromLanguageStr + "&dest=" + toLanguageStr + "&phrase=" + queryStr + "&format=" + "xml";
        String translationsXml = performGetRequest(request);
        ArrayList<String> translationsTmp = getTranslations(translationsXml);
        translationsArrayList.addAll(translationsTmp);
      }
      translations = new String[translationsArrayList.size()];
      translationsArrayList.toArray(translations); 
    } catch (Exception e) {
      throw new ApplicationException(e);
    }
    return translations;
  }

  /**
   * TODO  hack, very slow and too much requests 
   * @param query
   * @return
   * @throws ApplicationException
   */
  public String detectLanguageCode(String query) throws ApplicationException {
	  String langCode = null;
	  String[] languages = {"eng", "deu", "fra"};
    for (int i=0; i<languages.length; i++) {
      String lang = languages[i];
      String request = servlet + "?from=" + lang + "&dest=" + lang + "&phrase=" + query + "&format=" + "xml"; // translate into the same language to get the word if it exists
      String translationsXml = performGetRequest(request);
      langCode = getLanguageCode(translationsXml);
      if (langCode != null)
        return langCode;
    }
	  return langCode;
	}
	 
  private String getLanguageCode(String glosbeXmlStr) throws ApplicationException {
    String languageCode = null;
    String translationsStr = xQueryEvaluator.evaluateAsStringValueJoined(glosbeXmlStr, "/map/entry/string[text() = 'tuc']/../list/map/entry/string[text() = 'phrase']/../text/languageCode", "###");
    if (translationsStr != null) {
      String[] translationsArray = translationsStr.split("###");
      if (translationsArray != null) {
        languageCode = translationsArray[0];  // delivers the first language code of all found TODO 
      }
    }
    return languageCode;
  }

  private ArrayList<String> getTranslations(String glosbeXmlStr) throws ApplicationException {
	  ArrayList<String> translations = new ArrayList<String>();
	  String translationsStr = xQueryEvaluator.evaluateAsStringValueJoined(glosbeXmlStr, "/map/entry/string[text() = 'tuc']/../list/map/entry/string[text() = 'phrase']/../text/text", "###");
	  if (translationsStr != null) {
	    String[] translationsArray = translationsStr.split("###");
	    if (translationsArray != null) {
        for (int i=0; i<translationsArray.length; i++) {
          String t = translationsArray[i];
          translations.add(t);
        }
	    }
	  }
	  return translations;
	}
	
  private String performGetRequest(String requestName) throws ApplicationException {
    String resultStr = null;
    try {
      String portPart = ":" + port;
      String urlStr = protocol + "://" + host + portPart + requestName;
      GetMethod method = new GetMethod(urlStr);
      httpClient.executeMethod(method);
      byte[] resultBytes = method.getResponseBody();
      resultStr = new String(resultBytes, "utf-8");
      method.releaseConnection();
    } catch (HttpException e) {
      throw new ApplicationException(e);
    } catch (IOException e) {
      throw new ApplicationException(e);
    }
    return resultStr;
  }

  private String performPostRequest(String requestName, NameValuePair[] data) throws ApplicationException {
    String resultStr = null;
    try {
      String portPart = ":" + port;
      String urlStr = protocol + "://" + host + portPart + requestName;
      PostMethod method = new PostMethod(urlStr);
      for (int i=0; i<data.length; i++) {
        NameValuePair param = data[i];
        method.addParameter(param);
      }
      httpClient.executeMethod(method);
      byte[] resultBytes = method.getResponseBody();
      resultStr = new String(resultBytes, "utf-8");
      method.releaseConnection();
    } catch (HttpException e) {
      throw new ApplicationException(e);
    } catch (IOException e) {
      throw new ApplicationException(e);
    }
    return resultStr;
  }


	
}
