package org.knoesis.location.userprofile.ner;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.knoesis.location.util.PropertyLoader;

/**
 * Use DBPedia Spotlight to annotate entities in a tweet
 * @author revathy
 */

public class SpotlightAnnotator implements Annotator {
	
	static Logger log = Logger.getLogger(SpotlightAnnotator.class.getName());	
	private static HttpClient client = new HttpClient();
	
	/**
	 * Default Spotlight Parameters
	 */
	private String API_URL = "http://spotlight.sztaki.hu:2222/rest/annotate"; 
	private double CONFIDENCE = 0.5;
	private int SUPPORT = 20;
	
	
	public SpotlightAnnotator() {
		init();
	}	
	
	/**
	 * Read the Spotlight parameters from the configuration file
	 */
	private void init() {
		PropertyLoader pl = PropertyLoader.getInstanceOfPropertyLoader();
		API_URL = pl.getProperty("API_URL");
		CONFIDENCE = Double.parseDouble(pl.getProperty("CONFIDENCE"));
		SUPPORT = Integer.parseInt(pl.getProperty("SUPPORT"));
	}
	
	@Override
	public Set<String> annotateTweet(String tweet) {
			Set<String> wikipediaEntities = new HashSet<String>();
			String spotlightResponse = null;
			try {
				GetMethod getMethod = new GetMethod(API_URL + "/?" +
					"confidence=" + CONFIDENCE
					+ "&support=" + SUPPORT					
					+ "&text=" + URLEncoder.encode(tweet, "utf-8"));
				getMethod.addRequestHeader(new Header("Accept", "application/json"));
				spotlightResponse = request(getMethod);
			} catch (UnsupportedEncodingException e) {
				log.debug("Could not encode text.", e);
			}

			assert spotlightResponse != null;

			JSONObject resultJSON = null;
			JSONArray entities = null;

			try {
				resultJSON = new JSONObject(spotlightResponse);
				entities = resultJSON.getJSONArray("Resources");
			} catch (JSONException e) {
				log.debug("Received invalid response from DBpedia Spotlight API.");
			} catch (NullPointerException ne) {
				log.debug("Null pointer exception for tweet: " + tweet);
			}						
		
			if (entities==null || entities.length()==0)
				return wikipediaEntities;
			
			for(int i = 0; i < entities.length(); i++) {
				try {
					JSONObject entity = entities.getJSONObject(i);
					wikipediaEntities.add(getTitleFromUrl(entity.getString("@URI")));							
				} catch (JSONException e) {
					log.error("JSON exception "+e);
				}	
			}
			return wikipediaEntities;
	}
	
	/**
	 * Get the title of the Wikipedia page from the URL
	 * @param method
	 * @return
	 */
	private String getTitleFromUrl(String url) {
			String title = "";
			try {
				String[] temp = url.split("/");
				title = temp[temp.length-1].replace("_", " ");
			} catch (PatternSyntaxException pse) {
				log.error("Error in extracting title from : " + url, pse);	
			}
			return title;
	}

	private String request(HttpMethod method) {
			String response = null;		
			method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,	new DefaultHttpMethodRetryHandler(3, false));

			try {				
				int statusCode = client.executeMethod(method);
				if (statusCode != HttpStatus.SC_OK) 
					log.error("Method failed: " + method.getStatusLine());
				
				byte[] responseBody = method.getResponseBody(); 
				response = new String(responseBody);

			} catch (HttpException e) {
				log.error("Fatal protocol violation: " + e.getMessage());
			} catch (IOException e) {
				log.error("Fatal transport error: " + e.getMessage());
				log.error(method.getQueryString());
			} finally {				
				method.releaseConnection();
			}
			return response;

	} 
		
	public static void main(String[] args) {				
			String tweet = "China launches crackdown on Ramadan";
			Annotator annotator = new SpotlightAnnotator();
			Set<String> wikipediaEntities = annotator.annotateTweet(tweet);
			for(String entity : wikipediaEntities)
				System.out.println(entity);
	}
}
