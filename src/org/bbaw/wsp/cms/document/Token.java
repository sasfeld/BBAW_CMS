package org.bbaw.wsp.cms.document;

import org.apache.lucene.index.Term;

public class Token {
  private Term term;
  private int freq = -1;
  
  public Token(Term term) {
    this.term = term;
  }

  public Term getTerm() {
    return term;
  }

  public void setTerm(Term term) {
    this.term = term;
  }

  public int getFreq() {
    return freq;
  }

  public void setFreq(int freq) {
    this.freq = freq;
  }


}
