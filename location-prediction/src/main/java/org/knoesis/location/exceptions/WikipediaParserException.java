package org.knoesis.location.exceptions;

public class WikipediaParserException extends Exception {

	private static final long serialVersionUID = 1L;
			
	public WikipediaParserException(String message, Exception e) {
		super(message,e);		
	}
	
	public WikipediaParserException(String message) {	
		super(message);
	}
	
	public WikipediaParserException(Exception e) {
		super(e);
	}
	
}
