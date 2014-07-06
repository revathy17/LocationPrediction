package org.knoesis.location.kb.score;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.knoesis.location.exceptions.RetrievalException;
import org.knoesis.location.exceptions.StorageException;
import org.knoesis.location.kb.wiki.LinkExtractor;
import org.knoesis.location.storage.WikipediaStore;
/**
 * Rank the local entities of a location using Pointwise Mutual Information
 * @author revathy
 */
public class PMIScore implements Score {
	
		static Logger log = Logger.getLogger(PMIScore.class.getName());	
		private int countAllInternalLinks;
		private WikipediaStore ws;	
		private LinkExtractor le;	
	
		
		public PMIScore() throws RetrievalException {	
			log.info("Creating PMI Object");
			ws = new WikipediaStore();
			le = new LinkExtractor();		
			countAllInternalLinks = ws.getInternalLinksCount();						
		}
		
		@Override
		public Map<String, Double> scoreLocalEntity(String location) {					
			log.info("Computing PMI of location " + location);
			
			Map<String, Double> scoredLocalEntities = new HashMap<String, Double>();
			Map<String, List<String>> mainPages = new HashMap<String, List<String>>();
			List<String> locLinks = new ArrayList<String>();
			
			/**
			 * Read the internal links of the location. For example: San Francisco
			 */			
			try {
				locLinks = le.getInternalLinks(location);
			} catch (StorageException se) {
				log.error("Error in reading internal links of the location: " + location, se); 
			}
			locLinks.add(location);				
			
			/**
			 * The location itself is an internal link with respect to the city
			 */			
			try {
				mainPages = ws.getMainPages(locLinks);			
			} catch (RetrievalException re) {
				log.error("Error in fetching main pages of local entities for location: " + location, re);
			}			
			
			/**
			 * Determine all the count of the Wikipedia pages that the location appears in, as an internal link  
			 */			
			List<String> mainPagesOfLocation = mainPages.get(location);
			/**
			 * Loop through all the local entities of the location
			 * For each local entity, fetch the count of the Wikipedia pages that the local entity appears in as an internal link
			 */
			for(String localEntity : locLinks) {				
				List<String> mainPagesOfLocalEntity = mainPages.get(localEntity);				 			
				if(mainPagesOfLocalEntity != null) {
					double pmi_score = pointwiseMI(mainPagesOfLocation, mainPagesOfLocalEntity);
					scoredLocalEntities.put(localEntity, pmi_score);
				}
			}
			return scoredLocalEntities;			
		}
		
		/**
		 * Compute pointwise mutual information of a location and a local entity as:
		 * 	PMI(c,e) = log (base 2) [ P(c,e) / P(c) . P(e)] 
		 * @param setOne
		 * @param setTwo
		 * @return
		 */
		public double pointwiseMI(List<String> setOne, List<String> setTwo) {
			
			Set<String> intersection = new HashSet<String>(setOne);
			intersection.retainAll(setTwo);
			double pmi_score = 0;
			if(!intersection.isEmpty()) {
					/**
					 * P(c,e): Probability of joint occurrence of location and entity together
					 */
					double num = (double)intersection.size()/countAllInternalLinks;	
					/**
					 * P(c): Probability of individual occurrence of the location
					 */
					double denom1 = (double)setOne.size()/countAllInternalLinks; 
					/**
					 * P(e): Probability of individual occurrence of the local entity
					 */
					double denom2 = (double)setTwo.size()/countAllInternalLinks;
					pmi_score = num/(denom1*denom2);							
					pmi_score = Math.log(pmi_score) / Math.log(2);
			}				
			return pmi_score;
			
		}	
}
