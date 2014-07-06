package org.knoesis.location.kb;

import java.util.List;

import org.knoesis.location.exceptions.StorageException;
import org.knoesis.location.exceptions.WikipediaParserException;
import org.knoesis.location.kb.score.ScoreType;

/**
 * Create a knowledgebase
 * @author revathy
 */
public interface KnowledgebaseGenerator {

	/**
	 * Extract the hyperlink structure of Wikipedia
	 * This can be done using the Wikipedia web-service or from the sql dump of Wikipedia
	 * or the XML dump of Wikipedia
	 */
	public void extractHyperlinkStructure() throws WikipediaParserException;
	
	/**
	 * Create a knowledgebase of given list of locations by extracting local entities 
	 * of the locations from the hyperlink structure and scoring them by a given score
	 * type. Score type can be Tversky Index, Jaccard Index, Betweenness Centrality or
	 * Pointwise Mutual Information. 
	 * @param locations
	 * @param scoreType
	 * @throws StorageException
	 */
	public void createKnowledgebase(List<String> locations, ScoreType scoreType) throws StorageException;
	
	/**
	 * Create a knowledgebase of given list of locations by using the optimum performing 
	 * score type - Tversky Index
	 * @param locations
	 * @throws StorageException
	 */
	public void createKnowledgebase(List<String> locations) throws StorageException;
}
