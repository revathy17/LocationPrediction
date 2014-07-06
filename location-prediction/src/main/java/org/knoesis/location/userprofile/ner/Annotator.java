package org.knoesis.location.userprofile.ner;

import java.util.Set;

public interface Annotator {
	/**
	 * Return the annotations of a given tweet using some annotator
	 * @param tweet
	 * @return
	 */
	public Set<String> annotateTweet(String tweet); 
	
}
