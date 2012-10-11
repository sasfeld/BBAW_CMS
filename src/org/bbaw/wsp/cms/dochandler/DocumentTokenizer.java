package org.bbaw.wsp.cms.dochandler;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import de.mpg.mpiwg.berlin.mpdl.exception.ApplicationException;
import de.mpg.mpiwg.berlin.mpdl.lt.morph.app.Lemma;
import de.mpg.mpiwg.berlin.mpdl.lt.morph.app.MorphologyCache;
import de.mpg.mpiwg.berlin.mpdl.lt.text.norm.Normalizer;
import de.mpg.mpiwg.berlin.mpdl.lt.text.tokenize.Token;
import de.mpg.mpiwg.berlin.mpdl.lt.text.tokenize.Tokenizer;

public class DocumentTokenizer {
  private static DocumentTokenizer instance;
  private MorphologyCache morphCache;

  public static DocumentTokenizer getInstance() throws ApplicationException {
    if (instance == null) {
      instance = new DocumentTokenizer();
      instance.init();
    }
    return instance;
  }
  
  private void init() throws ApplicationException {
    morphCache = MorphologyCache.getInstance();
  }
  
  public ArrayList<Token> getToken(String inputString, String language, String[] normFunctions) throws ApplicationException {
    ArrayList<Token> retTokens = null;
    if (inputString == null || language == null)
      return null;
    try {
      StringReader reader = new StringReader(inputString);
      Tokenizer tokenizer = new Tokenizer(reader);
      tokenizer.setLanguage(language);
      tokenizer.setNormFunctions(normFunctions);
      retTokens = tokenizer.getTokens();
      tokenizer.end();
      tokenizer.close();
    } catch (IOException e) {
      throw new ApplicationException(e);
    }
    return retTokens;
  }

  public String buildStr(ArrayList<Token> tokens, String language, String type) throws ApplicationException {
    if (tokens == null)
      return null;
    StringBuilder strBuilder = new StringBuilder();
    if (tokens != null && ! tokens.isEmpty()) {
      for (int i=0; i<tokens.size(); i++) {
        Token token = tokens.get(i);
        String tokenStr = null;
        if (type.equals("orig")) {
          tokenStr = token.getContentOrig();
        } else if (type.equals("norm")) {
          tokenStr = token.getContentNorm();
        } else if (type.equals("morph")) {
          String tokenNorm = token.getContentNorm();
          ArrayList<Lemma> lemmas = morphCache.getLemmasByFormName(language, tokenNorm, Normalizer.NONE);
          if (lemmas != null && ! lemmas.isEmpty()) {
            tokenStr = "";
            for (int j=0; j<lemmas.size(); j++) {
              Lemma l = lemmas.get(j);
              String lemmaName = l.getLemmaName();
              tokenStr = tokenStr + lemmaName + " ";
            }
          } else {
            tokenStr = tokenNorm;
          }
        }
        if (tokenStr != null)
          strBuilder.append(tokenStr.trim() + " ");
      }
    }
    String result = strBuilder.toString();
    if (result.isEmpty())
      return null;
    else
     return result;
  }  

}
