package jcertdevtest.webapp;

import jcertdevtest.Room;

/**
 * Represent a room record as a bean for easy display.
 * 
 * @author Kgn Goh
 *
 */
public class DisplayRecord {
	
	private static final String[] headers = new String[Room.NumFields];
	
	static {
		headers[Room.FieldNums.NAME] = "Name";
		headers[Room.FieldNums.LOCATION] = "Location";
		headers[Room.FieldNums.OCCUPANCY] = "Occupancy";
		headers[Room.FieldNums.SMOKING] = "Smoking";
		headers[Room.FieldNums.PRICE] = "Price";
		headers[Room.FieldNums.DATE] = "Available";
		headers[Room.FieldNums.CUSTOMER] = "Booked By";
	}
	
	public static String[] getHeaders() {
		return headers;
	}
	
	private final String[] data;
	private final boolean booked;
	private final int recNo;
	
	public DisplayRecord(Room room) {
		recNo = room.getRecNo();
		booked = room.getCustomer() != null && room.getCustomer().length() > 0;
		data = room.toRecord();
	}
	
	public int getRecNo() {
		return recNo;
	}
	public boolean isBooked() {
		return booked;
	}
	public String[] getData() {
		return data;
	}
}