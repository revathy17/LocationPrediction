package org.knoesis.location.kb.score;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.knoesis.location.exceptions.StorageException;
import org.knoesis.location.kb.graph.WikiGraph;
import org.knoesis.location.kb.wiki.LinkExtractor;

/**
 * Use this class to compute the betweenness centrality of each local entity of a city
 * We use the Jung 2.0.1 API to create a directed graph for each city and compute the betweenness centrality of each node 
 * in the graph.For each city, the nodes are the local entities of the city we draw an edge from one node to another if 
 * there exists an internal link from the Wikipedia page of the former node to the Wikipedia page of the latter
 * @author revathy
 */

public class BCScore implements Score {
	
	static Logger log = Logger.getLogger(BCScore.class.getName());
	private LinkExtractor le;
	
	public BCScore() {
		le = new LinkExtractor();
	}
	
	@Override
	public Map<String, Double> scoreLocalEntity(String location) {
		log.info("Computing BC score of " + location);
		
		Map<String, Double> scoredLocalEntities = new HashMap<String, Double>();
		Map<String, List<String>> edges = new HashMap<String, List<String>>();
		Set<String> locLinks = null;
		
		/**
		 * Read the internal links of the location. E.g.: San Francisco
		 */		
		try {
			locLinks = new HashSet<String>(le.getInternalLinks(location));  
		} catch (StorageException se) {
			log.error("Error in reading internal links of the location : " + location, se);	
			return scoredLocalEntities;
		}
		
		/**
		 * The location itself is an internal link with respect to the city
		 */
		locLinks.add(location);	
		
		/**
		 * Loop through all the local entities (e.g.:Golden Gate Bridge) and determine the edges between the local entities in the graph
		 */
		for(String localEntity : locLinks) {
			List<String> localEntityLinks = null;
			try {
				localEntityLinks = le.getInternalLinks(localEntity);				
			} catch (StorageException se) {
				/**
				 * If the links of a local entity are not found, then ignore this local entity and loop through the rest
				 */
				log.error("Error in reading internal links of the local entity: ", se);
			}
			
			/**
			 * For every internal link in localEntity draw an edge to the entity in its Wikipedia page also found in the location page
			 */
			List<String> secondaryLinks = new ArrayList<String>();
			for(String link : localEntityLinks) {
				if(locLinks.contains(link)) 
					secondaryLinks.add(link);				
			}
			edges.put(localEntity, secondaryLinks);			
		}	
		
		/**
		 * Create a graph for this location
		 */
		WikiGraph wg = new WikiGraph(edges);
		/**
		 * Compute betweenness centrality of each node in the graph
		 */
		return wg.getBetweennessCentralityScores();
		
	}
	
	
}
