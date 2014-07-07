package org.knoesis.location.kb;

import java.util.List;
import java.util.Map;

import org.knoesis.location.exceptions.StorageException;
import org.knoesis.location.exceptions.WikipediaParserException;
import org.knoesis.location.kb.score.BCScore;
import org.knoesis.location.kb.score.JaccardScore;
import org.knoesis.location.kb.score.Score;
import org.knoesis.location.kb.score.ScoreType;
import org.knoesis.location.kb.score.TverskyScore;
import org.knoesis.location.kb.wiki.WikipediaParser;
import org.knoesis.location.storage.LocationStore;
import org.knoesis.location.util.PropertyLoader;

/**
 * Create the knowledgebase by parsing the given Wikipedia dump and for the list of locations
 * @author revathy
 */

public class WikiKnowledgebaseGenerator implements KnowledgebaseGenerator {

	private String wikiDumpFileName;
	public WikiKnowledgebaseGenerator() {
		init();		
	}
	
	private void init() {
		PropertyLoader pl = PropertyLoader.getInstanceOfPropertyLoader();
		wikiDumpFileName = pl.getProperty("WIKIPEDIA_DUMP");		
	}
	
	/**
	 * Extract the hyperlink structure of Wikipedia from the latest XML dump of Wikipedia
	 * The latest dump (pages-articles.xml.bz2) can be downloaded at:
	 * http://en.wikipedia.org/wiki/Wikipedia:Database_download
	 * The file name is specified in config.properties file
	 */
	@Override
	public void extractHyperlinkStructure() throws WikipediaParserException {
		WikipediaParser.serializeWikiLinks(wikiDumpFileName);			
	}
	
	/**
	 * Create a knowledgebase for a given list of locations using the default score type - Tversky Index
	 * @param locations
	 * @throws StorageException
	 */
	@Override
	public void createKnowledgebase(List<String> locations) throws StorageException {
		createKnowledgebase(locations, ScoreType.TVERSKY_SCORE);			
	}

	/**
	 * Create a knowledgebase for a given list of locations using one of the following score types:
	 * Jaccard_Score
	 * @param locations
	 * @param scoreType
	 * @throws StorageException
	 */
	@Override
	public void createKnowledgebase(List<String> locations, ScoreType scoreType) throws StorageException {
		
		LocationStore ls = new LocationStore();
		Score score = null;
		
		if(scoreType.equals(ScoreType.TVERSKY_SCORE)) 
			score = new TverskyScore();
		else if(scoreType.equals(ScoreType.JACCARD_SCORE))
			score = new JaccardScore();
		else if(scoreType.equals(ScoreType.BC_SCORE))
			score = new BCScore();
		
		if(score != null) {		
			for(String loc : locations) {				
					/**
					 * Compute the score for all the local entities of a location
					 */
					Map<String,Double> scores = score.scoreLocalEntity(loc);
					/**
					 * Insert the location, local entities and their Tversky Scores in the knowledgebase
					 */
					ls.upsertScore(loc, scores, scoreType.toString().toLowerCase());			
			} 				
		} else {
			/**
			 * Compute the PMI scores
			 */
			RunnableWikiKnowledgebaseGenerator.runThreads(locations, scoreType);
		}
		
	}
}
