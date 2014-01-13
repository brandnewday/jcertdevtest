package jcertdevtest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import jcertdevtest.db.DB;
import jcertdevtest.db.DataPersistenceException;
import jcertdevtest.db.RecordNotFoundException;

/**
 * The main server class for handling data requirements by the user interface:
 * search and booking.
 * 
 * Note that the mandatory interface {@link DB} does not completely match the
 * UI's data requirements (e.g. search by exact match). {@link DB} also needs
 * to support more features (e.g. create entry) that is not required by the
 * UI.
 * 
 * @author Ken Goh
 *
 */
public class BookingServiceImpl implements BookingService {
	private static final Logger log = Logger.getLogger(BookingServiceImpl.class.getName());
	private DB data;
	
	public BookingServiceImpl(DB data) {
		this.data = data;
	}
	
	@Override
	public Room[] search(SearchCriteria criteria) throws ServiceException {
		
		if(criteria instanceof SearchCriteriaExactAnd) {
			return search((SearchCriteriaExactAnd)criteria);
		} else if(criteria instanceof SearchCriteriaExactOr) {
			return search((SearchCriteriaExactOr)criteria);
		} else if(criteria instanceof SearchCriteriaAll) {
			return search((SearchCriteriaAll)criteria);
		}
		throw new ServiceException("Unknown search type " + criteria);
	}
	
	private Room[] search(SearchCriteriaExactAnd criteria) {
		final String[] rawCriteria = new String[Room.NumFields]; 
		rawCriteria[Room.NameFieldNum] = criteria.getName();
		rawCriteria[Room.LocationFieldNum] = criteria.getLocation();
		final int[] recNos = data.find(rawCriteria);
		final ArrayList<Room> matches = new ArrayList<Room>(recNos.length);
		for(int recNo : recNos) {
			String[] roomData;
			try {
				roomData = data.read(recNo);
				if(roomData[Room.NameFieldNum].equals(criteria.getName())
						&& roomData[Room.LocationFieldNum].equals(criteria.getLocation())) {
					matches.add(Room.fromRecord(recNo, roomData));
				}
			} catch (RecordNotFoundException e) {
				// ignore. no lock requirement for search so this is possible.
			}
		}
		return matches.toArray(new Room[matches.size()]);
	}
	
	/**
	 * To do OR search of 2 fields, we have to do 2 searches and combine
	 * results.
	 * Also, the mandatory interface for find gives "begin with" matches.
	 * But we need exact match, so need to retrieve data again and filter
	 * the result. 
	 */
	private Room[] search(SearchCriteriaExactOr criteria) {
		// search for 1st OR condition
		final String[] rawCriteria1 = new String[Room.NumFields]; 
		rawCriteria1[Room.NameFieldNum] = criteria.getName();
		final int[] recNos1 = data.find(rawCriteria1);
		
		// key the matches by recNo to ensure no dups if match in both searches
		final HashMap<Integer, Room> matches = new HashMap<>(recNos1.length);
		
		// filter by exact match and add to result
		for(int recNo : recNos1) {
			String[] roomData;
			try {
				roomData = data.read(recNo);
				if(roomData[Room.NameFieldNum].equals(criteria.getName())) {
					matches.put(recNo, Room.fromRecord(recNo, roomData));
				}
			} catch (RecordNotFoundException e) {
				// ignore. no lock requirement for search so this is possible.
			}
		}
		
		// search for 2nd OR condition
		final String[] rawCriteria2 = new String[Room.NumFields]; 
		rawCriteria2[Room.LocationFieldNum] = criteria.getLocation();
		final int[] recNos2 = data.find(rawCriteria2);
		
		// filter by exact match and add to result
		for(int recNo : recNos2) {
			// if already matched in first search, no need check again
			if(matches.containsKey(recNo)) {
				continue;
			}
			
			String[] roomData;
			try {
				roomData = data.read(recNo);
				if(roomData[Room.LocationFieldNum].equals(criteria.getLocation())) {
					matches.put(recNo, Room.fromRecord(recNo, roomData));
				}
			} catch (RecordNotFoundException e) {
				// ignore. no lock requirement for search so this is possible.
			}
		}
		
		return matches.values().toArray(new Room[matches.size()]);
	}
	
	private Room[] search(SearchCriteriaAll criteria) {
		final String[] rawCriteria = new String[Room.NumFields];
		int[] recNos = data.find(rawCriteria);
		final ArrayList<Room> matches = new ArrayList<Room>(recNos.length);
		for(int recNo : recNos) {
			String[] roomData;
			try {
				roomData = data.read(recNo);
				matches.add(Room.fromRecord(recNo, roomData));
			} catch (RecordNotFoundException e) {
				// ignore. no lock requirement for search so this is possible.
			}
		}
		return matches.toArray(new Room[matches.size()]);
	}
	
	@Override
	public void book(Booking booking) throws ServiceException {
		long cookie = -1;
		try {
			cookie = data.lock(booking.getRecNo());
			
			String[] rawData = data.read(booking.getRecNo());
			// make copy since we must not modify the retrieved data directly
			String[] roomData = Arrays.copyOf(rawData, rawData.length);
			Room room = Room.fromRecord(booking.getRecNo(), roomData);
			
			if(room.getCustomer() != null && room.getCustomer().length() > 0) {
				throw new ServiceException("Room already booked");
			}
			roomData[Room.FieldNums.CUSTOMER] = booking.getCustomer();
			data.update(booking.getRecNo(), roomData, cookie);
			
		} catch (RecordNotFoundException e) {
			throw new ServiceException("Room not found", e);
		} catch(SecurityException | DataPersistenceException e) {
			log.log(Level.SEVERE, "Error when processing " + booking, e);
			throw new ServiceException("System error.", e);
		} finally {
			if(cookie != -1) {
				try {
					data.unlock(booking.getRecNo(), cookie);
				} catch (SecurityException | RecordNotFoundException e) {
					log.log(Level.SEVERE, "Error when unlocking for " + booking, e);
					throw new ServiceException("System error.", e);
				}				
			}
		}
		
	}
}
