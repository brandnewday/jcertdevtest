package jcertdevtest.db;

/**
 * Exception thrown from {@link DataPersistence}.
 * Derives from {@link RuntimeException} to avoid breaking the mandatory 
 * interface of {@link DB}.
 * @author Ken Goh
 *
 */
public class DataPersistenceException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DataPersistenceException() {
		super();
	}
	
	public DataPersistenceException(Throwable e) {
		super(e);
	}
	
	public DataPersistenceException(String message, Throwable e) {
		super(message, e);
	}
}
