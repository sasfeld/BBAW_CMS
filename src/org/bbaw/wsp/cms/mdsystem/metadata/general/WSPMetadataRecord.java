package org.bbaw.wsp.cms.mdsystem.metadata.general;

import java.util.Arrays;

/**
 * Instances of this class represent a MODS record in the old knowledge store.
 * @author Sascha Feldmann (wsp-shk1)
 *
 */
public class WSPMetadataRecord {
	protected String id;
	
	protected String url;
	
	protected String title;
	
	protected String mods_abstract;
	
	protected String[] personals;
	
	protected String placeTerm;
	
	protected String publisher;
	
	protected String dateIssued;
	
	protected String[] topics;
	
	protected String[] geographics;
	
	protected String temporalStart;
	
	protected String temporalEnd;
	
	
	
	
	public WSPMetadataRecord() {
		this.url = "";
		this.id = "";
		this.placeTerm = "";
		this.title = "";
		this.mods_abstract = "";
		this.publisher = "";
		this.dateIssued = "";
		this.temporalStart = "";
		this.temporalEnd = "";
	}

	/**
	 * 
	 * @return the URL to which the metafile refers to.
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Set the URL.
	 * Consider that the URI can be set only one time.
	 * @param url the URL to set.
	 * @throws {@link IllegalStateException} if the URL is already set.
	 */
	public void setUrl(final String url) {
		if(!this.url.isEmpty()) {
			throw new IllegalStateException("The URL has already a value.");
		}
		this.url = url;
	}

	/**
	 * 
	 * @return a List containing all IDs.
	 */
	public String[] getPersonals() {
		return personals;
	}	
	
	/**
	 * Set the personal IDs.
	 * Consider that those can be set only one time.
	 * @param personals an array of Strings that contains the IDs.
	 * @throws {@link IllegalStateException} if the list is already set.
	 */
	public void setPersonals(String[] persIds) {
		if(this.personals != null) {
			throw new IllegalStateException("The personals has already a value.");
		}
		this.personals = persIds;
		
	}

	/**
	 * 
	 * @return the metadata record ID
	 */
	public String getID() {
		return id;
	}

	/**
	 * Set the record IDs.
	 * Consider that those can be set once.
	 * @param the record ID.
	 * @throws {@link IllegalStateException} if the ID is already set.
	 */
	public void setID(String id) {
		if(!this.id.isEmpty()) {
			throw new IllegalStateException("The ID has already a value.");
		}
		this.id = id;
	}


	public String getPlaceTerm() {
		return placeTerm;
	}

	/**
	 * Set the placeTerm.
	 * Consider that those can be set only one time.
	 * @param the placeTerm.
	 * @throws {@link IllegalStateException} if the placeTerm is already set.
	 */
	public void setPlaceTerm(String placeTerm) {
		if(!this.placeTerm.isEmpty()) {
			throw new IllegalStateException("The placeTerm has already a value.");
		}
		this.placeTerm = placeTerm;
	}

	/**
	 * 
	 * @return the mods:title - leaf
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Set the title.
	 * Consider that those can be set only one time.
	 * @param the mods:title.
	 * @throws {@link IllegalStateException} if the title is already set.
	 */
	public void setTitle(String title) {
		if(!this.title.isEmpty()) {
			throw new IllegalStateException("The title has already a value.");
		}
		this.title = title;
	}

	/**
	 * 
	 * @return the mods:abstract - text
	 */
	public String getMods_abstract() {
		return mods_abstract;
	}

	/**
	 * Set the mods:abstract text.
	 * Consider that those can be set only one time.
	 * @param the mods:abstract text.
	 * @throws {@link IllegalStateException} if the abstract is already set.
	 */
	public void setMods_abstract(String mods_abstract) {
		if(!this.mods_abstract.isEmpty()) {
			throw new IllegalStateException("The abstract has already a value.");
		}
		this.mods_abstract = mods_abstract;
	}

	/**
	 * 
	 * @return the mods:publisher - text
	 */
	public String getPublisher() {
		return publisher;
	}

	/**
	 * Set the mods:publisher text.
	 * Consider that those can be set only one time.
	 * @param the mods:publisher text.
	 * @throws {@link IllegalStateException} if the publisher is already set.
	 */
	public void setPublisher(String publisher) {
		if(!this.publisher.isEmpty()) {
			throw new IllegalStateException("The publisher has already a value.");
		}
		this.publisher = publisher;
	}

	/**
	 * 
	 * @return the mods:dateIssued - text
	 * 
	 */
	public String getDateIssued() {
		return dateIssued;
	}

	/**
	 * Set the mods:dateIssued text.
	 * Consider that those can be set only one time.
	 * @param the mods:dateIssued text.
	 * @throws {@link IllegalStateException} if the dateIssued  is already set.
	 */
	public void setDateIssued(String dateIssued) {
		if(!this.dateIssued.isEmpty()) {
			throw new IllegalStateException("The dateIssued has already a value.");
		}
		this.dateIssued = dateIssued;
	}
	
	/**
	 * 
	 * @return a List containing all topics
	 */
	public String[] getTopics() {
		return this.topics;
	}	
	
	/**
	 * Set the topics.
	 * Consider that those can be set only one time.
	 * @param topics an array of Strings that contains the mods:topic - texts.
	 * @throws {@link IllegalStateException} if the list is already set.
	 */
	public void setTopics(String[] topics) {
		if(this.topics != null) {
			throw new IllegalStateException("The topics have already a value.");
		}
		this.topics = topics;
		
	}
	
	/**
	 * 
	 * @return a List containing all geographics
	 */
	public String[] getGeographics() {
		return this.topics;
	}	
	
	/**
	 * Set the geographics.
	 * Consider that those can be set only one time.
	 * @param topics an array of Strings that contains the mods:geographic - texts.
	 * @throws {@link IllegalStateException} if the list is already set.
	 */
	public void setGeographics(String[] geographics) {
		if(this.geographics != null) {
			throw new IllegalStateException("The geographics have already a value.");
		}
		this.geographics = geographics;
		
	}

	@Override
	public String toString() {
		return "WSPMetadataRecord [id=" + id + ", url=" + url + ", title="
				+ title + ", mods_abstract=" + mods_abstract + ", personals="
				+ Arrays.toString(personals) + ", placeTerm=" + placeTerm
				+ ", publisher=" + publisher + ", dateIssued=" + dateIssued
				+ ", topics=" + Arrays.toString(topics) + ", geographics="
				+ Arrays.toString(geographics) + ", temporalStart="
				+ temporalStart + ", temporalEnd=" + temporalEnd + "]";
	}

	/**
	 * 
	 * @return mods:temporal point = "start"
	 */
	public String getTemporalStart() {
		return temporalStart;
	}
	/**
	 * Set the mods:temporal point = "start" text.
	 * Consider that those can be set only one time.
	 * @param the mods:temporal point = "start" text.
	 * @throws {@link IllegalStateException} if the dateIssued  is already set.
	 */
	public void setTemporalStart(String temporalStart) {
		if(!this.temporalStart.isEmpty()) {
			throw new IllegalStateException("The temporalStart has already a value.");
		}
		this.temporalStart = temporalStart;
	}

	/**
	 * 
	 * @return mods:temporal point = "end"
	 */
	public String getTemporalEnd() {
		return temporalEnd;
	}
	/**
	 * Set the mods:temporal point = "end" text.
	 * Consider that those can be set only one time.
	 * @param the mods:temporal point = "end" text.
	 * @throws {@link IllegalStateException} if the dateIssued  is already set.
	 */
	public void setTemporalEnd(String temporalEnd) {
		if(!this.temporalEnd.isEmpty()) {
			throw new IllegalStateException("The temporalEnd has already a value.");
		}
		this.temporalEnd = temporalEnd;
	}
	
	
	
}
