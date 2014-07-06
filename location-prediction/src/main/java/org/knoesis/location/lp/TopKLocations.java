package org.knoesis.location.lp;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.knoesis.location.exceptions.StorageException;

/**
 * Return the top k locations of a user
 * @author revathy
 */
public class TopKLocations {
	
	static Logger log = Logger.getLogger(TopKLocations.class.getName());

	/**
	 * Predict the location of a user, given a set of a user's tweets
	 * @param tweets
	 */
	public static Set<String> predictLocationUsingTweets(List<String> tweets, String scoreType, int top_k) {
		Set<String> topk_locations = new LinkedHashSet<String>();		
		return topk_locations;
	}
	
	/**
	 * Predict the location of a user, given a list of Wikipedia Entities spotted from a set of tweets
	 * @param entities
	 */
	public static Set<String> predictLocationUsingWikiEntities(List<String> entities, 
		String scoreType, int top_k) throws StorageException {
		Set<String> topk_locations = new LinkedHashSet<String>();
		LocationPrediction lp = new LocationPrediction(entities);
		Set<String> allLocations = lp.predictLocation(scoreType);
		int k=0;
		
		for(String loc : allLocations) {
			k++;
			topk_locations.add(loc);
			if(k==top_k)
				break;
		}
		return topk_locations;
	}
}
