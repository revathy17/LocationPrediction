package org.knoesis.location.kb;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.knoesis.location.exceptions.RetrievalException;
import org.knoesis.location.exceptions.StorageException;
import org.knoesis.location.kb.score.BCScore;
import org.knoesis.location.kb.score.PMIScore;
import org.knoesis.location.kb.score.Score;
import org.knoesis.location.kb.score.ScoreType;
import org.knoesis.location.storage.LocationStore;
import org.knoesis.location.util.PropertyLoader;

/**
 * To create the knowledgebase using Pointwise Mutual Information or Betweenness Centrality
 * implement the Runnable interface. 
 * @author revathy
 */

public class RunnableWikiKnowledgebaseGenerator implements Runnable {
	static Logger log = Logger.getLogger(RunnableWikiKnowledgebaseGenerator.class.getName());
	private static int NUMBER_OF_THREADS;
	private Score score;
	private String typeOfScore;
	private List<String> locations;	
	private Thread thread;
	
	
	public RunnableWikiKnowledgebaseGenerator(ScoreType scoreType, List<String> locations) {
		this.typeOfScore = scoreType.toString().toLowerCase();
		this.locations = new ArrayList<String>(locations);
		if(scoreType.equals(ScoreType.PMI_SCORE))
			try {
				score = new PMIScore();
			} catch (RetrievalException re) {
				re.printStackTrace();
			}
		else if(scoreType.equals(ScoreType.BC_SCORE))
			score = new BCScore();
	}
		
	@Override
	public void run() {
		log.info("Starting a new thread ...");
		LocationStore ls = new LocationStore();
		try{						
			for(String loc : locations) {
				Map<String, Double> pmiScore = score.scoreLocalEntity(loc);			
				ls.insertScore(loc, pmiScore, typeOfScore);
			}
		} catch (StorageException se) {
			se.printStackTrace();
		}
	}
	
	public void start() {
		if(thread == null) {
			thread = new Thread(this);
			thread.start();
		}			
	}

	public static void runThreads(List<String> locations, ScoreType scoreType) {
		int count = 0;
		PropertyLoader pl = PropertyLoader.getInstanceOfPropertyLoader();
		NUMBER_OF_THREADS = Integer.parseInt(pl.getProperty("NUMBER_OF_THREADS"));	
		
		int locations_per_thread = locations.size()/NUMBER_OF_THREADS;
		System.out.println("Locations per thread: " + locations_per_thread);
		List<String> temp = new ArrayList<String>();
		/**
		 * Create the threads for each run
		 */		
		for(String loc : locations) {
			temp.add(loc);
			count++;
			if(count == locations_per_thread) {
				RunnableWikiKnowledgebaseGenerator rkb = new RunnableWikiKnowledgebaseGenerator(scoreType, temp);
				rkb.start();
				temp.clear();
				count = 0;
			}			
		}
		/**
		 * Rest of the locations
		 */
		RunnableWikiKnowledgebaseGenerator rkb = new RunnableWikiKnowledgebaseGenerator(scoreType, temp);
		rkb.start();
	}	
}
