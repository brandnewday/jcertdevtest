package jcertdevtest.db;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of the mandatory interface {@link DB}.
 * Methods can throw runtime {@link DataPersistenceException}.
 * 
 * @author Ken Goh
 *
 */
public class Data implements DB {

	private final Set<RecordInfo.Number> deleted = Collections.newSetFromMap(new ConcurrentHashMap<RecordInfo.Number, Boolean>());
	private final ConcurrentHashMap<RecordInfo.Number, RecordInfo> cache;
	private final DataPersistence persistence;
	
	public Data(String filePath) throws IOException {
		this(new DataPersistenceFileAdapter(new DataFile(filePath)));
	}
	
	public Data(DataPersistence persistence) throws IOException  {
		this.persistence = persistence;

		Map<Integer, Record> data = persistence.load();
		this.cache = new ConcurrentHashMap<>(data.size());
		for(Map.Entry<Integer, Record> entry : data.entrySet()) {
			RecordInfo.Number recNo = RecordInfo.Number.fromValue(entry.getKey());
			cache.put(recNo,
						new RecordInfo(recNo, entry.getValue()));
			if(entry.getValue().isDeleted()) {
				deleted.add(recNo);
			}
		}
	}

	/**
	 * Caller must NOT modify the content of the returned array.
	 * 
	 * Due to the mandatory interface, we have to return array here, so
	 * cannot make use of unmodifiable collection types. Alternative is
	 * make defensive copy every time, but that is expensive as this
	 * method is called frequently.
	 * 
	 * Regarding concurrent modification, result of this is optimistic.
	 * Details see {@link find}.
	 */
	public String[] read(int recNo) throws RecordNotFoundException {
		RecordInfo record = cache.get(RecordInfo.Number.fromValue(recNo));
		if(record == null || record.isDeleted())
			throw new RecordNotFoundException();
		return record.getData();
	}

	public void update(int recNo, String[] data, long lockCookie)
			throws RecordNotFoundException, SecurityException {
		RecordInfo record = cache.get(RecordInfo.Number.fromValue(recNo));
		if(record == null || record.isDeleted())
			throw new RecordNotFoundException();
		if(!record.getLock().checkCookie(lockCookie))
			throw new SecurityException();
		
		// store with a copy to avoid caller modifying the array afterwards
		String[] copy = Arrays.copyOf(data, data.length);
		persistence.update(recNo, copy);
		record.setData(copy);
	}

	public void delete(int recNo, long lockCookie)
			throws RecordNotFoundException, SecurityException {
		RecordInfo.Number recNum = RecordInfo.Number.fromValue(recNo);
		RecordInfo record = cache.get(recNum);
		if(record == null || record.isDeleted())
			throw new RecordNotFoundException();
		if(!record.getLock().checkCookie(lockCookie))
			throw new SecurityException();
		
		persistence.delete(recNum.getValue());
		deleted.add(recNum);
		record.setDeleted(true);
	}

	/**
	 * Due to the lack of record versioning in the interface, result of
	 * this is optimistic and does not guarantee to be valid. 
	 * It is possible that while reading a record, another thread comes in 
	 * and updates or deletes it. But since the update is atomic (all record 
	 * fields set in one go), the match per record is guaranteed to be valid.
	 * 
	 * Note that even if all instance methods are set to synchronized so no  
	 * other thread can update/delete while this method is running, the moment 
	 * it returns result, other thread can immediately do update, still
	 * rendering the result of this find invalid.
	 * 
	 * Although a {@link lock} method is provided in the interface, this is
	 * per record, so if caller really need to guarantee consistency, it has
	 * to call {@link lock} on all records first, do this find, perform any
	 * other action, then unlock all. This is too pessimistic for the general
	 * use case.
	 */
	public int[] find(String[] criteria) {
		ArrayList<RecordInfo.Number> matches = new ArrayList<RecordInfo.Number>(); 
		for(RecordInfo record : cache.values()) {
			if(record.isDeleted())
				continue;
			boolean matched = true;
			for(int fieldNum = 0; fieldNum < criteria.length; ++fieldNum) {
				if(criteria[fieldNum] == null
						|| record.getData()[fieldNum].startsWith(criteria[fieldNum])) {
					continue;
				}
				matched = false;
				break;
			}
			if(matched) {
				matches.add(record.getRecNo());
			}
		}
		int[] result = new int[matches.size()];
		for(int i = 0; i < result.length; ++i) {
			result[i] = matches.get(i).getValue();
		}
		return result;
	}


	public int create(String[] data) throws DuplicateKeyException {
		// work and store using a copy to avoid caller modifying the array content
		String[] copy = Arrays.copyOf(data, data.length);

		RecordInfo.Number recNo = tryCreateReuseDeleted(copy);
		if(recNo == null) {
			recNo = RecordInfo.Number.fromValue(persistence.create(copy));
			cache.put(recNo, new RecordInfo(recNo, new Record(copy, false)));
		}
		return recNo.getValue();
	}

	/**
	 * Due to the way the interface exposes the locking control as public,
	 * reusing a deleted record as part of {@link create} needs special
	 * consideration.
	 * 
	 * Consider this scenario: thread A locks record 1, deletes it, but BEFORE 
	 * it unlocks, thread B creates new record, sees record 1 is deleted, so 
	 * reuses it. This violates the expectation that when record 1 is locked,
	 * it should not be modified.
	 * 
	 * But {@link lock} cannot be called ahead of {@link create}, so we must 
	 * check internally whether a lock is held on the record we are trying to 
	 * reuse.
	 * 
	 * @return recNo of new data added using a reused entry; null if no reuse 
	 * could be done.
	 */
	private synchronized RecordInfo.Number tryCreateReuseDeleted(String[] data) {
		RecordInfo.Number recNo = null;
		// no need to lock on the deleted collection. the delete() method
		// only adds to it, the only place that removes is this method, which
		// is already synchronized
		if(!deleted.isEmpty()) {
			recNo = deleted.iterator().next();
			RecordInfo record = cache.get(recNo);
			long cookie = -1;
			try {
				cookie = record.getLock().lock();
				// this is expected as record is indicated by the deleted
				// collection, and this is the only method that resets it.
				if(record.isDeleted()) {
					persistence.update(recNo.getValue(), data);
					deleted.remove(recNo);
					record.setDeleted(false);
				}
			} finally {
				record.getLock().unlock(cookie);
			}
		}
		return recNo;
	}

	public long lock(int recNo) throws RecordNotFoundException {
		RecordInfo record = cache.get(RecordInfo.Number.fromValue(recNo));
		if(record == null || record.isDeleted())
			throw new RecordNotFoundException();
		return record.getLock().lock();
	}

	public void unlock(int recNo, long cookie) throws RecordNotFoundException,
			SecurityException {
		RecordInfo record = cache.get(RecordInfo.Number.fromValue(recNo));
		// should NOT check for isDeleted status here because it is valid 
		// action that a record is locked, deleted, then unlocked.
		if(record == null)
			throw new RecordNotFoundException();
		record.getLock().unlock(cookie);
	}
}