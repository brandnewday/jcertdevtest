package jcertdevtest.db;

import java.util.Map;

/**
 * Persistence layer interface.
 * Runtime exception can be thrown as {@link DataPersistenceException}.
 * @author Ken Goh
 *
 */
public interface DataPersistence {

	/**
	 * Must be called after constructor and before any other methods
	 */
	public abstract Map<Integer, Record> load();

	public abstract int create(String[] data);

	public abstract void update(int recNo, String[] data);

	public abstract void delete(int recNo);

}