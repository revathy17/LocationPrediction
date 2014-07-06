package org.knoesis.location.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.knoesis.location.exceptions.StorageException;
import org.knoesis.location.models.LocalEntity;
/**
 * Use this class to store the knowledgebase for a list of cities supplied by the end user
 * @author revathy
 */

public class LocationStore {
	private Connection connection = null;
	private String database = null;
	static Logger log = Logger.getLogger(LocationStore.class.getName());	
	
	public LocationStore() {
		database = "jdbc:sqlite::resource:location_prediction.db";		
	}	
	
	/**
	 * If the knowledgebase contains the location and its local entities then update them with the score information
	 * Else insert the location and score information in the database
	 * @param locationName
	 * @param scores
	 * @param score_type
	 * @throws StorageException
	 */
	public void upsertScore(String locationName, Map<String, Double> scores, String score_type) throws StorageException {
		if(checkIfLocExists(locationName))
			updateScore(locationName, scores, score_type);
		else
			insertScore(locationName, scores, score_type);		
	}
	
	/**
	 * Check if the given location already exists in the database
	 * @param locationName
	 * @return true if the location exists in the database else false
	 * @throws StorageException
	 */
	public boolean checkIfLocExists(String locationName) throws StorageException {
		
		String sql = "SELECT count(local_entity) AS count FROM knowledgebase WHERE location_name = ?";
		PreparedStatement ps = null;		
		int count = 0;
		try {
			connection = DBManager.getConnection(database, true);
			ps = connection.prepareStatement(sql);
			ps.setString(1,locationName);
			ResultSet rs = ps.executeQuery();
			while(rs.next()) 
				count = rs.getInt("count");			
		} catch (SQLException sqle) {
			throw new StorageException("Error in reading knowledgebase for " + locationName, sqle);
		} finally {
			DBManager.cleanUpDatabase(connection, ps);
		}
		
		return count>0 ? true:false;
		
	}
	
	/**
	 * Insert a location, its local entities and their scores in the knowledge-base
	 * @param locationName
	 * @param scores
	 * @param score_type
	 * @throws StorageException
	 */
	public void insertScore(String locationName, Map<String, Double> scores, String score_type) throws StorageException {
		
		String sql = "INSERT OR IGNORE INTO knowledgebase (location_name, local_entity, " + score_type + ") VALUES (?,?,?)";
		PreparedStatement ps = null;
		connection = DBManager.getConnection(database, false);
		try {							
			connection.setAutoCommit(false);
			ps = connection.prepareStatement(sql);
			ps.setString(1, locationName);			
			for(Map.Entry<String,Double> score : scores.entrySet()) {
				ps.setString(2, score.getKey());
				ps.setDouble(3, score.getValue());
				ps.addBatch();
			}
			ps.executeBatch();
			connection.commit();
		} catch (SQLException se) {
			throw new StorageException("Error in inserting local entities and scores of " + locationName + " in the database", se);
		} finally {
			DBManager.cleanUpDatabase(connection, ps);
		}
		
	}
	
	/**
	 * Update the scores of local entities that are already in the knowledge-base
	 * @param locationName
	 * @param scores
	 * @param score_type
	 * @throws StorageException
	 */
	public void updateScore(String locationName, Map<String,Double> scores, String score_type) throws StorageException {
		
		String sql = "UPDATE knowledgebase SET " + score_type + " = ? WHERE location_name = ? AND local_entity = ?";
		PreparedStatement ps = null;
		connection = DBManager.getConnection(database, false);
		try {
			connection.setAutoCommit(false);
			ps = connection.prepareStatement(sql);
			for(Map.Entry<String,Double> score : scores.entrySet()) {
				ps.setDouble(1,score.getValue());
				ps.setString(2,locationName);
				ps.setString(3,score.getKey());
				ps.addBatch();
			}			
			ps.executeBatch();
			connection.commit();
		} catch (SQLException se) {
			throw new StorageException("Error in updating " + score_type + " scores of the local entities of " + locationName + " in the database", se);
		} finally {
			DBManager.cleanUpDatabase(connection, ps);
		}
		
	}
	
	/**
	 * For a list of Wikipedia entities, return the subset which are local entities of locations in the knowledgebase
	 * and their corresponding requested scores
	 * @param scoreType
	 * @param entities
	 * @throws StorageException
	 */
	public List<LocalEntity> getScoreForUser(List<String> entities, String scoreType) throws StorageException {
		
		String sql = "SELECT location_name, local_entity, " + scoreType + " AS score FROM knowledgebase WHERE local_entity IN (";
		for(int i=0;i<entities.size();i++) 
			sql += (i==0) ? "\"" + entities.get(i) + "\"" : ", \"" + entities.get(i) + "\"";		
		sql += ")";
		log.info("SQL for location prediction: " + sql);
		
		Statement stmt = null;
		connection = DBManager.getConnection(database, true);
		List<LocalEntity> localEntities = new ArrayList<LocalEntity>();
		try {
			stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while(rs.next()) 
				localEntities.add(new LocalEntity(rs.getString("location_name"), rs.getString("local_entity"), rs.getDouble("score")));			
		} catch (SQLException sqle) {
			throw new StorageException("Error in reading knowledgebase", sqle);
		} finally {
			DBManager.cleanUpDatabase(connection, stmt);
		}	
		
		return localEntities;
	}
	
}
