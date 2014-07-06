package org.knoesis.location.lp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.knoesis.location.exceptions.StorageException;
import org.knoesis.location.models.LocalEntity;
import org.knoesis.location.storage.LocationStore;

/**
 * Predict the possible locations of a user based on the Wikipedia Entities in their tweets
 * @author revathy
 */
public class LocationPrediction {
	 
	private List<String> wikiEntities;
	private List<LocalEntity> localEntities;

	public LocationPrediction(List<String> wikiEntities) {
		this.wikiEntities = new ArrayList<String>(wikiEntities);
		this.localEntities = new ArrayList<LocalEntity>();
	}
	
	/**
	 * Predict the location of a user using the scoreType
	 * @param scoreType Score used to rank the local entities i.e. Tversky Score, Jaccard Score, PMI Score or BC Score
	 * @throws StorageException
	 */
	public Set<String> predictLocation(String scoreType) throws StorageException {
		LocationStore ls = new LocationStore();		
		localEntities = ls.getScoreForUser(wikiEntities, scoreType);
		
		/**
		 * Aggregate the score of each location using the local entities of that location
		 */
		Map<String, Double> scoresPerLocation = new HashMap<String, Double>();
		for(LocalEntity le : localEntities) {
			if(scoresPerLocation.containsKey(le.getLocation())) 
				scoresPerLocation.put(le.getLocation(), scoresPerLocation.get(le) + le.getScore());
			else 
				scoresPerLocation.put(le.getLocation(),le.getScore());			
		}	
		
		return orderLocationsByScore(scoresPerLocation);
	}
	
	/**
	 * Sort the possible locations in the descending order of their aggregate scores
	 * @param scoresPerLocation
	 * @return List of locations in the descending order of their scores
	 */
	private Set<String> orderLocationsByScore(Map<String, Double> scoresPerLocation) {
		
		List<Map.Entry<String, Double>> scoresPerLoc = new LinkedList<>(scoresPerLocation.entrySet());
		Collections.sort(scoresPerLoc, Collections.reverseOrder(new Comparator<Map.Entry<String,Double>>() {
            @Override
            public int compare(Entry<String,Double> o1, Entry<String,Double> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        }));
		
		Set<String> sortedLocations = new LinkedHashSet<String>();		
		for(Map.Entry<String,Double> entry: scoresPerLoc)
            sortedLocations.add(entry.getKey());        
        return sortedLocations;
        
	}
	
	/**
	 * Return the local entities of a given location. These entities are a subset of the Wikipedia Entities used
	 * to create an object of type LocationPrediction. The local entities returned by this function represent the 
	 * Wikipedia Entities that are considered local to the given location. This function can be used to determine
	 * the local entities that were used to predict their location. If there are no local entities from the given
	 * location then this function returns an empty list.
	 * @param location
	 * @return
	 */
	public List<String> getLocalEntitiesOfLocation(String location) {
		List<String> localEntitiesOfLoc = new ArrayList<String>();
		for(LocalEntity le : localEntities) {
			if(le.getLocation().equals(location))
				localEntitiesOfLoc.add(le.getLocalEntity());
		}
		return localEntitiesOfLoc;
	}
	
}