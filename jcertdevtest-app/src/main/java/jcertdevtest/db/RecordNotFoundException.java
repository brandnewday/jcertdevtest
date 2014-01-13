package jcertdevtest.db;

/**
 * Specified record does not exist or is marked as deleted
 * 
 * @author Ken Goh
 *
 */
public class RecordNotFoundException extends Exception {

	private static final long serialVersionUID = 1L;

	public RecordNotFoundException() {
		super();
	}
	
	public RecordNotFoundException(String message) {
		super(message);
	}
}
