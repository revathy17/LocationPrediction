package org.knoesis.location.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.knoesis.location.exceptions.RetrievalException;
import org.knoesis.location.exceptions.StorageException;
import org.knoesis.location.models.Wiki;

/**
 * Use this class to store the hyperlink structure of Wikipedia in a sqlite database and retrieve the subset 
 * required to create the knowledgebase of locations.
 * @author revathy
 */

public class WikipediaStore {

		private Connection connection = null;
		private String database = null;
		static Logger log = Logger.getLogger(WikipediaStore.class.getName());	
		
		public WikipediaStore() {
			database = "jdbc:sqlite::resource:wikipedia.db";	
		}
		
		/**
		 * Check if the Wikipedia hyperlink structure has been extracted from the XML dump yet
		 **/
		public boolean checkIfWikiDumpProcessed() throws RetrievalException {
			
			String sql = "SELECT COUNT(page_name) AS count FROM wiki_hyperlink";
			Statement stmt = null;
			connection = DBManager.getConnection(database, false);
			boolean empty = false;
			try {
				stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery(sql);
				while(rs.next()) 
					empty = (rs.getInt("count") > 0) ? true : false;				
			} catch (SQLException sqle) {
				throw new RetrievalException("Error in fetching data from wiki_hyperlink",sqle);				
			} finally {
				DBManager.cleanUpDatabase(connection, stmt);
			}			
			return empty;
			
		}
		
		/**
		 * Delete the Wikipedia Hyperlink Structure from the database
		 * @throws StorageException
		 */
		public void cleanWikiStore() throws StorageException {
			String sql = "DELETE FROM wiki_hyperlink";
			Statement stmt = null;
			connection = DBManager.getConnection(database, true);
			
			try {
				stmt = connection.createStatement();
				stmt.executeUpdate(sql);			
			} catch (SQLException sqle) {
				throw new StorageException("Error in cleaning up Wikipedia Hyperlink Structure from the database");
			} finally {
				DBManager.cleanUpDatabase(connection, stmt);
			}
		}
		
		/**
		 * Insert the hyperlink structure of entire wikipedia dump in the database
		 * @param internalLinks
		 */
		public void insertWikiLinks(Map<String, Wiki> internalLinks)  throws StorageException {
			
			String sql = "INSERT INTO wiki_hyperlink (page_name, disambiguation, stub, redirect, redirect_text, internal_links) VALUES (?,?,?,?,?,?)";
			PreparedStatement ps = null;
			connection = DBManager.getConnection(database, false);
		
			try {				
				connection.createStatement().execute("PRAGMA synchronous=OFF;");
				connection.setAutoCommit(false);				
				ps = connection.prepareStatement(sql);
				for(Map.Entry<String,Wiki> il : internalLinks.entrySet()) {				
					Wiki wiki = il.getValue();
					ps.setString(1, il.getKey());
					ps.setBoolean(2, wiki.getIsDisambiguation());
					ps.setBoolean(3, wiki.getIsStub());
					ps.setBoolean(4, wiki.getIsRedirect());
					ps.setString(5, wiki.getRedirectText());
					ps.setString(6, join(wiki.getInternalLinks()));			
					ps.addBatch();
				}				
				ps.executeBatch();
				connection.commit();
			} catch (SQLException sqle) {
				throw new StorageException("Error in inserting data in wiki_hyperlink",sqle);				
			} finally {
				DBManager.cleanUpDatabase(connection, ps);
			}
			
		}
		
		/**
		 * Save the internal links as a single string
		 * @param list
		 * @return
		 */
		private String join(List<String> list) {
			String il = "";
			if(!list.isEmpty()) {
				for(String l : list) 
					il += l + ";";		
			}
			return il;
		}
		
		/**
		 * Lookup internal links of a wikipedia page
		 * @param wikipage
		 * @return internal links of a wikipedia page
		 */
		public Wiki lookupLink(String wikipage) throws StorageException {
			String sql = "SELECT page_name, disambiguation, stub, redirect, redirect_text, internal_links FROM wiki_hyperlink " +
						"WHERE page_name = ?";		
			PreparedStatement ps = null;
			Connection connection = DBManager.getConnection(database, true);
			Wiki wiki = null;
			
			try {
				ps = connection.prepareStatement(sql);
				ps.setString(1,wikipage);
				ResultSet rs = ps.executeQuery();
				while(rs.next()) {
					String redirect_text = rs.getString("redirect_text");
					wiki = new Wiki(rs.getString("page_name"), rs.getBoolean("disambiguation"), rs.getBoolean("stub"), rs.getBoolean("redirect"),
							redirect_text, split(rs.getString("internal_links")));
				}					
			} catch (SQLException sqle) {
				throw new StorageException ("Error in looking up internal links of " + wikipage,sqle);
			} finally {
				DBManager.cleanUpDatabase(connection, ps);
			}		
			return wiki;
		}
		
		private List<String> split(String internalLinks) {
			String[] ils = internalLinks.split(";");
			return new ArrayList<String>(Arrays.asList(ils));
		}
		
		/**
		 * For a given list of pages, find all the Wikipedia pages that they occur in as internal links
		 * @param pages
		 * @throws StorageException
		 */
		public Map<String, List<String>> getMainPages(List<String> pages) throws RetrievalException {
			String sql = "SELECT page_name, disambiguation, stub, redirect, redirect_text, internal_links FROM wiki_hyperlink";
		
			Statement stmt = null;
			Connection connection = DBManager.getConnection(database, true);
			Map<String, List<String>> mainPages = new HashMap<String, List<String>>();
			for(String pg : pages) 
				mainPages.put(pg, new ArrayList<String>());
			log.info("Querying the database for main pages");
			try {
				stmt = connection.createStatement();	
				ResultSet rs = stmt.executeQuery(sql);
				while(rs.next()) {
					List<String> internalLinks = split(rs.getString("internal_links"));
					for(String pg : pages) {
						if(internalLinks.contains(pg)) {
							List<String> temp = mainPages.get(pg);
							temp.add(rs.getString("page_name"));
							mainPages.put(pg, temp);						
						} 							
					}			
				}
				log.info("WikipediaStore: Main Pages size: " + mainPages.size());		
			} catch (SQLException sqle) {				
				throw new RetrievalException("Error in looking up wikipedia for main pages",sqle);
			} finally {
				DBManager.cleanUpDatabase(connection, stmt);
			}	
			return mainPages;
		}
		
		/**
		 * Get the total number of internal links in the Wikipedia dump.
		 * This is used to compute the Pointwise Mutual Information of a local entity to a city.
		 * @return
		 * @throws StorageException
		 */
		public int getInternalLinksCount() throws RetrievalException {
			log.info("Getting internal links count from wikipedia database");
			String sql = "SELECT page_name, internal_links FROM wiki_hyperlink";
			Connection connection = DBManager.getConnection(database, true);			
			Statement stmt = null;
			int count = 0;
			try {
				stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery(sql);
				while(rs.next()) {
					List<String> internalLinks = split(rs.getString("internal_links"));
					count += internalLinks.size();
				}		
				log.info("Count of internal links: " + count);
			} catch(SQLException sqle) {
				throw new RetrievalException("Error in looking up wikipedia for internal links",sqle);
			} finally {
				DBManager.cleanUpDatabase(connection, stmt);
			}
			return count;
		}
		
}
