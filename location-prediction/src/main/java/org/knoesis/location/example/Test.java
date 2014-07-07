package org.knoesis.location.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.knoesis.location.kb.KnowledgebaseGenerator;
import org.knoesis.location.kb.WikiKnowledgebaseGenerator;
import org.knoesis.location.kb.score.ScoreType;

/**
 * This class shows how to create a knowledge-base for a given list of locations
 * @author revathy
 *
 */
public class Test {

	public static void main(String[] args) {
		int option = Integer.parseInt(args[0]);
		
		URL cityURL = Test.class.getClassLoader().getResource("US_Cities.csv");
		String cityFile = cityURL.getPath();
		BufferedReader br = null;
		List<String> cities = new ArrayList<String>();
		System.out.println("Reading list of cities from: " + cityFile);		
		try {
			br = new BufferedReader(new FileReader(new File(cityFile)));
			String line;
			while((line=br.readLine()) != null) 
				cities.add(line.trim());					
		} catch (IOException ioe) {
			ioe.printStackTrace();
			System.exit(0);
		}
		
		KnowledgebaseGenerator kb = new WikiKnowledgebaseGenerator();
		switch(option) {
		case 1:
			System.out.println("Creating the Wikipedia Knowledgebase");
			try {
				kb.extractHyperlinkStructure();
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		
		case 2:
			System.out.println("Computing Tversky Scores");
			try {
				kb.createKnowledgebase(cities);
			}catch (Exception e) {
				e.printStackTrace();
			}	
			System.out.println("Finished computing Tversky Scores");
			break;
		
		case 3:
			System.out.println("Computing Jaccard Scores");
			try {
				kb.createKnowledgebase(cities,ScoreType.JACCARD_SCORE);
			}catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("Finished computing Jaccard Scores");
			break;
		
		case 4:
			System.out.println("Computing PMI Scores");
			try {
				kb.createKnowledgebase(cities, ScoreType.PMI_SCORE);			
			}catch (Exception e) {
				e.printStackTrace();
			}
			break;
		
		case 5:
			System.out.println("Computing BC Scores");
			try {
				kb.createKnowledgebase(cities, ScoreType.BC_SCORE);
			}catch (Exception e) {
				e.printStackTrace();
			}
			break;
			
		default:
			System.out.println("Enter a valid option!");
			break;		
	
		}			
	}	
}
