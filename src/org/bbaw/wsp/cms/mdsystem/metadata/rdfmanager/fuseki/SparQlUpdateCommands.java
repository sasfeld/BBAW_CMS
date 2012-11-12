package org.bbaw.wsp.cms.mdsystem.metadata.rdfmanager.fuseki;

/**
 * This (helper-)enum encapsules special sparql commands and returns the commands as String for often required commands.
 * @author Sascha Feldmann (wsp-shk1)
 * @date 12.11.2012
 *
 */
public enum SparQlUpdateCommands {
  CLEAR_DATASET, CLEAR_GRAPH, CLEAR_DEFAULT;
  
  /**
   * @return the spaql command as {@link String}.
   */
  public String getCommandString() {
    switch (this) {
    case CLEAR_DATASET:
      return "CLEAR ALL";      
    case CLEAR_GRAPH:
      return "CLEAR GRAPH ";
    case CLEAR_DEFAULT:
      return "CLEAR DEFAULT";   
    default:
      return "";
    }
  }
}
