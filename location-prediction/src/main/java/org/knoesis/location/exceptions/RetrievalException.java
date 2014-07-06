package org.knoesis.location.exceptions;

/**
 * This exception is thrown when there is an error in reading the knowledgebase from the sqlite database
 * @author revathy
 */
public class RetrievalException extends Exception {

	private static final long serialVersionUID = 1L;
	private String msg;
	
	public RetrievalException (String message, Exception e) {
			super(message, e);
			this.msg = message;
	}
	
	public RetrievalException (String message) {
			super(message);
			this.msg = message;
	}
	
	@Override
	public String toString() {
			return "EXCEPTION " + msg + "\n" + super.toString();
	}
		
}
