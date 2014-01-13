package jcertdevtest.db;

import java.io.IOException;
import java.util.Map;

/**
 * Adapt a {@link DataFile} to the {@link DataPersistence} interface.
 * @author Ken Goh
 *
 */
public class DataPersistenceFileAdapter implements DataPersistence {

	private final DataFile file;
	
	public DataPersistenceFileAdapter(DataFile file) {
		this.file = file;
	}
	
	public Map<Integer, Record> load() {
		try {
			return file.load();
		} catch (IOException e) {
			throw new DataPersistenceException(e);
		}
	}

	public int create(String[] data) {
		try {
			return file.create(data);
		} catch (IOException e) {
			throw new DataPersistenceException(e);
		}
	}

	public void update(int recNo, String[] data) {
		try {
			file.update(recNo, data);
		} catch (IOException e) {
			throw new DataPersistenceException(e);
		}
	}

	public void delete(int recNo) {
		try {
			file.delete(recNo);
		} catch (IOException e) {
			throw new DataPersistenceException(e);
		}
	}
}