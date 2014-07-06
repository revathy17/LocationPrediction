package org.knoesis.location.kb.wiki;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.knoesis.location.exceptions.StorageException;
import org.knoesis.location.models.Wiki;
import org.knoesis.location.storage.WikipediaStore;

/**
 * Use this class to extract the internal links of a given Wikipedia page
 * @author revathy
 */
public class LinkExtractor {
	static Logger log = Logger.getLogger(LinkExtractor.class.getName());		
	WikipediaStore ws;
	
	public LinkExtractor() {
		ws = new WikipediaStore();
	}
	
	/**
	 * Get the internal links of the given wikipedia page
	 * @param wikiPageName 
	 * @return internal links
	 */
	public List<String> getInternalLinks(String wikiPageName) throws StorageException {
		List<String> wpInternalLinks = new ArrayList<String>();
		Wiki wiki = null;
		wiki = ws.lookupLink(wikiPageName);
		if(wiki != null) {
			if(wiki.getIsRedirect()) {
				/**
				 * If the page is redirected then fetch internal links from redirected page
				 */
				log.info("Redirected from " + wiki.getTitle() + " to " + wiki.getRedirectText());
				wiki = ws.lookupLink(wiki.getRedirectText());
			} else if(wiki.getIsDisambiguation()) { 
				/**
				 * If the page is a disambiguation page then ask user to choose the location 
				 */
				log.info(wikiPageName + " is a disambiguation page. Choose from the following pages.." + "\n");
				List<String> disambPages = wiki.getInternalLinks(); 
				for(String disambPage : disambPages) 
					log.info("\t" + disambPage);
			} else if(wiki.getIsStub()) { 
				/**
				 * If the page is a stub then inform the user and return no links
				 */
				log.info(wikiPageName + " is a stub");
			} else {
				wpInternalLinks.addAll(wiki.getInternalLinks());
			}
		} else {
			log.info("No page found for: " + wikiPageName);
		}
		
		return wpInternalLinks;
	}

}
