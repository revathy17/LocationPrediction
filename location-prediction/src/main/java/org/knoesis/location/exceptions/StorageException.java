package org.knoesis.location.exceptions;
/**
 * This exception is thrown when there is an error in creating the knowledgebase
 * @author revathy
 */
public class StorageException extends Exception {

		private static final long serialVersionUID = 1L;
		private String msg;
		
		public StorageException(String message, Exception e) {
				super(message, e);
				this.msg = message;
		}
		
		public StorageException(String message) {
				super(message);
				this.msg = message;
		}
		
		@Override
		public String toString() {
				return "EXCEPTION " + msg + "\n" + super.toString();
		}
			
}
