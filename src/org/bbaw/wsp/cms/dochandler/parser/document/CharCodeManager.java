package org.bbaw.wsp.cms.dochandler.parser.document;

/**
 * This class provides UNICODE char codes for special characters.
 * @author Sascha Feldmann (wsp-shk1)
 *
 */
public class CharCodeManager {
  /*
   * UNICODE-Block Superscripts and Subscripts
   */
  public static final char SUPERSCRIPT_ZERO = 2070;
  public static final char SUPERSCRIPT_FOUR = 0x2074;
  public static final char SUPERSCRIPT_FIVE = 0x2075;
  public static final char SUPERSCRIPT_SIX = 0x2076;
  public static final char SUPERSCRIPT_SEVEN = 0x2077;
  public static final char SUPERSCRIPT_EIGHT = 0x2078;
  public static final char SUPERSCRIPT_NINE = 0x2079;
  
  /*
   * UNICODE-Block Latin-1 Supplement
   */
  public static final char LATIN1_SUPERSCRIPT_ONE = 0x00B9;
  public static final char LATIN1_SUPERSCRIPT_TWO = 0x00B2;
  public static final char LATIN1_SUPERSCRIPT_THREE = 0x00B3;
  
  /**
   * Return the superscript String for an input number which is not superscripted.
   * If the number is greater than 9, the method will generate a string which consists of many single characters.
   * example: input 2 -> returns 2 superscripted (UNICODE)
   * @param number the input char, a number as integer
   * @return the String containing the superscripted character. May be empty if the number is not valid.
   */
  public static String returnNumberSuperscript(final int number) {
    switch (number) {
    case 0:
      return ""+SUPERSCRIPT_ZERO;     
    case 1:
      return ""+LATIN1_SUPERSCRIPT_ONE; 
    case 2:
      return ""+LATIN1_SUPERSCRIPT_TWO; 
    case 3:
      return ""+LATIN1_SUPERSCRIPT_THREE; 
    case 4:
      return ""+SUPERSCRIPT_FOUR; 
    case 5:
      return ""+SUPERSCRIPT_FIVE; 
    case 6:
      return ""+SUPERSCRIPT_SIX; 
    case 7:
      return ""+SUPERSCRIPT_SEVEN; 
    case 8:
      return ""+SUPERSCRIPT_EIGHT; 
    case 9:
      return ""+SUPERSCRIPT_NINE;       
    default:
      if(number >= 10) {
        final int einer = number % 10;
        final int zehner = number / 10;        
        return returnNumberSuperscript(zehner)+returnNumberSuperscript(einer);
      }
      return "";
    }
  }
}
