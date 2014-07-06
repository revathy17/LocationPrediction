package org.knoesis.location.exceptions;

public class EntityRecognitionException extends Exception {
	
	private static final long serialVersionUID = 1L;
	private String msg;
	
	public EntityRecognitionException(String message, Exception e) {
			super(message, e);
			this.msg = message;
	}
	
	public EntityRecognitionException(String message) {
			super(message);
			this.msg = message;
	}
	
	@Override
	public String toString() {
			return "EXCEPTION " + msg + "\n" + super.toString();
	}	
}
