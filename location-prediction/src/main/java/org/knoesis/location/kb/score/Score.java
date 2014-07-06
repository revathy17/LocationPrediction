package org.knoesis.location.kb.score;

import java.util.Map;

/**
 * Interface to compute the score of a local entity
 * @author revathy
 */

public interface Score {
		
	/**
	 * For a given location, score all the local entities 
	 * Return a map representation of local entities and their scores
	 * @param location
	 * @return
	 */
	public Map<String,Double> scoreLocalEntity(String location);
}
