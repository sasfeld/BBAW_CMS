package org.bbaw.wsp.cms.mdsystem.metadata.general.extractor;

import java.util.HashMap;

import org.bbaw.wsp.cms.mdsystem.metadata.general.WSPMetadataRecord;

import de.mpg.mpiwg.berlin.mpdl.exception.ApplicationException;

/**
 * This class is able to parse a MODS file that has the specified values of the
 * old knowledge store.
 * 
 * @author Sascha Feldmann (wsp-shk1)
 * 
 */
public class ModsMetadataExtractor extends MetadataExtractor {

  /**
   * Create a new ModsMetadataParser instance.
   * 
   * @param uri
   *          - the URI to the knowledge store metadata record.
   * @throws ApplicationException
   * @throws IllegalArgumentException
   *           if the uri is null, empty or doesn't refer to an existing file.
   */
  public ModsMetadataExtractor(final String uri, final HashMap<String, String> namespaces) throws ApplicationException {
    super(uri, namespaces);
  }

  /**
   * Parse the record and return an {@link WSPMetadataRecord} object.
   * 
   * @return an {@link WSPMetadataRecord} object.
   */
  public WSPMetadataRecord parse() {
    final WSPMetadataRecord modsObject = new WSPMetadataRecord();

    final String id = parseID();
    modsObject.setID(id);

    final String url = parseURL();
    modsObject.setUrl(url);

    final String title = parseTitle();
    modsObject.setTitle(title);

    final String mods_abstract = parseAbstract();
    modsObject.setMods_abstract(mods_abstract);

    final String publisher = parsePublisher();
    modsObject.setPublisher(publisher);

    final String dateIssued = parseDateIssued();
    modsObject.setDateIssued(dateIssued);

    final String[] persIds = parsePersonalIds();
    modsObject.setPersonals(persIds);

    final String placeTerm = parsePlaceTerm();
    modsObject.setPlaceTerm(placeTerm);

    final String[] topics = parseTopics();
    modsObject.setTopics(topics);

    final String[] geographics = parseGeographics();
    modsObject.setGeographics(geographics);

    final String temporalStart = parseTemporalStart();
    modsObject.setTemporalStart(temporalStart);

    final String temporalEnd = parseTemporalEnd();
    modsObject.setTemporalEnd(temporalEnd);

    return modsObject;
  }

  private String parseTemporalEnd() {
    return (String) buildXPath("//mods:subject/mods:temporal[@point='end']/text()", false);
  }

  private String parseTemporalStart() {
    return (String) buildXPath("//mods:subject/mods:temporal[@point='start']/text()", false);
  }

  private String[] parseGeographics() {
    return (String[]) buildXPath("//mods:subject/mods:geographic/text()", true);
  }

  private String[] parseTopics() {
    return (String[]) buildXPath("//mods:subject/mods:topic/text()", true);
  }

  private String parseDateIssued() {
    return (String) buildXPath("//mods:originInfo/mods:dateIssued/text()", false);
  }

  private String parsePublisher() {
    return (String) buildXPath("//mods:originInfo/mods:publisher/text()", false);
  }

  private String parseAbstract() {
    return (String) buildXPath("//mods:abstract/text()", false);
  }

  private String parseTitle() {
    return (String) buildXPath("//mods:titleInfo/mods:title/text()", false);
  }

  private String parsePlaceTerm() {
    return (String) buildXPath("//mods:originInfo/mods:place/mods:placeTerm/text()", false);
  }

  private String parseID() {
    return (String) buildXPath("//mods:recordIdentifier/text()", false);
  }

  private String[] parsePersonalIds() {
    return (String[]) buildXPath("//mods:name[@type='personal']/@ID", true);
  }

  private String parseURL() {
    return (String) buildXPath("//mods:url/text()", false);
  }
}
