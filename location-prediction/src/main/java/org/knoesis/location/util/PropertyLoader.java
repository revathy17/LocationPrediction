package org.knoesis.location.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
/**
 * Read the properties file
 * @author revathy
 */
public class PropertyLoader {

	static Logger log = Logger.getLogger(PropertyLoader.class.getName());
	private String configFile;
	private Map<String,String> props;
	private static PropertyLoader propertyLoader = null;
	
	private PropertyLoader() {		
		this.configFile = "config.properties";
		readProperties();
	}
	
	private void readProperties() {
		Properties properties = new Properties();
		this.props = new HashMap<String,String>();
		InputStream in = null;
		try {
			in = PropertyLoader.class.getClassLoader().getResourceAsStream(this.configFile);
			properties.load(in);
			for(Map.Entry<Object,Object> prop : properties.entrySet())
				props.put((String)prop.getKey(),(String)prop.getValue());
			in.close();
		} catch (IOException ioe) {			
			log.error("Error in loading properties file",ioe);
			ioe.printStackTrace();
		}
	}	
	
	public String getProperty(String property) {
		return props.get(property);
	}
	
	public static PropertyLoader getInstanceOfPropertyLoader() {
		if(propertyLoader == null)
			propertyLoader = new PropertyLoader();
		return propertyLoader;
	}	
	
}
