package org.knoesis.location.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.sqlite.SQLiteConfig;
import org.knoesis.location.exceptions.StorageException;
/**
 * Return a connection to the database requested
 * @author revathy
 */

public class DBManager {
	
	/**
	 * Connect to the SQLite database
	 * @param connectionString
	 * @param readOnly
	 * @return
	 */
	public static Connection getConnection(String connectionString, boolean readOnly) {
		Connection connection = null;
		try {
			Class.forName("org.sqlite.JDBC");
			SQLiteConfig config = new SQLiteConfig();
			config.setReadOnly(readOnly);			
			connection = DriverManager.getConnection(connectionString,config.toProperties());
		} catch(ClassNotFoundException|SQLException e) {
			e.printStackTrace();
		}	
		return connection;
	}
	
	/**
	 * Clean up the database connection
	 * @param connection
	 * @param stmt
	 */
	public static void cleanUpDatabase(Connection connection, Statement stmt) {			
		try {
			stmt.close();
			if(!connection.getAutoCommit())
				connection.setAutoCommit(true);	
			connection.close();								
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}			
	}

	/**
	 * Clean up the database connection
	 * @param connection
	 * @param ps
	 * @throws StorageException
	 */
	public static void cleanUpDatabase(Connection connection, PreparedStatement ps) throws StorageException {
		try {
			ps.close();	
			if(!connection.getAutoCommit())
				connection.setAutoCommit(true);	
			connection.close();
		} catch (SQLException sqle) {
			sqle.printStackTrace();			
		}
	}
	
}
