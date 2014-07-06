package org.knoesis.location.kb.wiki;

import info.bliki.wiki.dump.IArticleFilter;
import info.bliki.wiki.dump.Siteinfo;
import info.bliki.wiki.dump.WikiArticle;
import info.bliki.wiki.dump.WikiPatternMatcher;
import info.bliki.wiki.dump.WikiXMLParser;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.knoesis.location.exceptions.RetrievalException;
import org.knoesis.location.exceptions.StorageException;
import org.knoesis.location.exceptions.WikipediaParserException;
import org.knoesis.location.models.Wiki;
import org.knoesis.location.storage.WikipediaStore;
import org.xml.sax.SAXException;

/**
 * Extract the hyperlink structure of Wikipedia from the XML dump using the gwtwiki API
 * Save it in a sqlite database
 * @author revathy
 */
public class WikipediaParser implements IArticleFilter {
	
	static Logger log = Logger.getLogger(WikipediaParser.class.getName());
	private static final int BATCH_SIZE = 50000;
	private Map<String, Wiki> wikiInternalLinks;
	private WikipediaStore ws;
	
	public WikipediaParser() throws StorageException {
		ws = new WikipediaStore();
		wikiInternalLinks = new HashMap<String, Wiki>();
		init();		
	}	
	
	public void init() {
		/**
		 * If the hyperlink structure has been extracted then clear the database
		 */
		try {
			if(ws.checkIfWikiDumpProcessed())
				ws.cleanWikiStore();
		} catch (RetrievalException|StorageException e) {
			e.printStackTrace();
		}
	}
		
	@Override
	public void process(WikiArticle page, Siteinfo sinfo) throws SAXException {		
			/**
			 * Only parse articles that have content i.e. ignore templates, category pages etc.
			 */
			if(page.isMain() && page.getText() != null) { 	
				WikiPatternMatcher wpm = new WikiPatternMatcher(page.getText());
				/**
				 * Get the internal links of this page
				 */
				List<String> internalLinks = wpm.getLinks();			
				String title = page.getTitle();
				/**
				 * Check if it is a disambiguation page
				 */
				boolean disam = wpm.isDisambiguationPage(); 
				/**
				 * Check if it is redirected to another page. For example "San Francisco, California" is redirected to "San Francisco"
				 */
				boolean redirect = wpm.isRedirect();	
				String redirectText = "";
				/**
				 * If it is a redirect, then get the url of the redirected page
				 */
				if(redirect)
					redirectText = wpm.getRedirectText(); 
				/**
				 * Check if it is a stub
				 */
				boolean stub = wpm.isStub();			
				wikiInternalLinks.put(title, new Wiki(title, disam, stub, redirect, redirectText, internalLinks));	
				/**
				 * Batch insert 50000 records in SQLite database, in one operation
				 */
				if(wikiInternalLinks.size() == BATCH_SIZE) {
					try {					
						ws.insertWikiLinks(wikiInternalLinks);
						wikiInternalLinks.clear();					
					} catch(StorageException se) {
						log.error("Error in inserting wikipedia links:" , se);
						se.printStackTrace();
					}
				}					
			} 
	}
		
	/**
	 * Parse the Wikipedia XML dump and extract the hyperlink structure and save it in a SQLite database
	 * @param source
	 * @throws WikipediaParserException
	 */
	public static void serializeWikiLinks(String source) throws WikipediaParserException {
			URL srcUrl = WikipediaParser.class.getClassLoader().getResource(source);
			String bz2File = srcUrl.getPath();			
		
			IArticleFilter handler = null;
			try {
				handler = new WikipediaParser();										
				WikiXMLParser wxp = new WikiXMLParser(bz2File, handler);
				wxp.parse();							
			} catch (IOException ioe) {						
				throw new WikipediaParserException("IOException in reading the Wikipedia dump", ioe);
			} catch (SAXException sxe) {	
				throw new WikipediaParserException("SAXException in parsing the Wikipedia dump", sxe);
			} catch (StorageException se) {
				throw new WikipediaParserException("StorageException in storing the hyperlink structure of Wikipedia in the database", se);
			}
	}		
	
	public static void main(String[] args) {				
			try {
				WikipediaParser.serializeWikiLinks("enwiki-latest-pages-articles.xml.bz2");
			} catch (WikipediaParserException wpe) {
				wpe.printStackTrace();
				System.exit(0);
			}
	}
}
