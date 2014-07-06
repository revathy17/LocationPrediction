package org.knoesis.location.kb.score;

/**
 * This class computes the Jaccard Index of the local entities of a given location
 * @author revathy
 */
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.knoesis.location.exceptions.StorageException;
import org.knoesis.location.kb.wiki.LinkExtractor;

public class JaccardScore implements Score {
	
	static Logger log = Logger.getLogger(JaccardScore.class.getName());			
	private LinkExtractor le;
	
	public JaccardScore() {
		le = new LinkExtractor();	
	}	
	
	@Override
	public Map<String,Double> scoreLocalEntity(String location) {		
		log.info("Computing Jaccard Score for " + location);
		
		Map<String, Double> scoredLocalEntities = new HashMap<String, Double>();
		Set<String> locLinks = null;
		
		/**
		 * Read the internal links of the location. For example: San Francisco
		 */
		try {
			locLinks = new HashSet<String>(le.getInternalLinks(location)); 
		} catch (StorageException se) {
			log.error("Error in reading internal links of the location : " + location, se);	
			return scoredLocalEntities;
		}
		
		/**
		 * The location itself is a local entity with respect to the city
		 */
		locLinks.add(location);	
		
		/**
		 * Loop through all the local entities of the location (e.g.: Golden Gate Bridge) and compute the Jaccard Index of each local entity with respect to the city
		 */
		for(String localEntity : locLinks) {		
			Set<String> localEntityLinks = null;
			try {
				localEntityLinks = new HashSet<String>(le.getInternalLinks(localEntity));  
			} catch (StorageException se) {
				/**
				 * If no internal links are found for the local entity then ignore that entity 
				 */
				log.error("Error in reading internal links of the local entity : " + localEntity + " of the location " + location, se); 
			}
			scoredLocalEntities.put(localEntity, getJaccardScore(locLinks,localEntityLinks));
		}	
		
		return scoredLocalEntities;		
	 }
	
	/**
	 * Compute the Jaccard Index of two sets
	 * @param setOne
	 * @param setTwo
	 * @return Jaccard Index
	 */
	 private double getJaccardScore(Set<String> setOne, Set<String> setTwo) {
		double jaccardScore = 0.0;
		Set<String> intersectionOfSets = new HashSet<String>(setOne);
		intersectionOfSets.retainAll(setTwo);		
			
		Set<String> unionOfSets = new HashSet<String>(setOne);
		unionOfSets.addAll(setTwo);				
		jaccardScore = (double)intersectionOfSets.size() / unionOfSets.size();	
		return jaccardScore;
	 }

}
