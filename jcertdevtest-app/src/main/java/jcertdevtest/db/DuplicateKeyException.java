package jcertdevtest.db;

/**
 * Specified in the mandatory interface, but not actually used because there
 * is no concept of "key" in the data.
 * 
 * @author Ken Goh
 *
 */
public class DuplicateKeyException extends Exception {
	private static final long serialVersionUID = 1L;
	public DuplicateKeyException() {
		super();
	}
	public DuplicateKeyException(String message) {
		super(message);
	}
}
