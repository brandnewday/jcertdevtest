package jcertdevtest;

import java.io.Serializable;

public class Room implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public static final int NumFields = 7;
	public static final int NameFieldNum = 0;
	public static final int LocationFieldNum = 1;

	
	private int recNo;
	private String name;
	private String location;
	private int occupancy;
	private boolean smoking;
	private String price;
	private String date;
	private String customer;

	public static Room fromRecord(int recNo, String[] data) {
		Room room = new Room();
		room.recNo = recNo;
		room.name = data[FieldNums.NAME];
		room.location = data[FieldNums.LOCATION];
		room.occupancy = Integer.parseInt(data[FieldNums.OCCUPANCY]);
		room.smoking = "Y".equals(data[FieldNums.SMOKING]) ? true : false;
		room.price = data[FieldNums.PRICE];
		room.date = data[FieldNums.DATE];
		room.customer = data[FieldNums.CUSTOMER];
		return room;
	}
	public String[] toRecord() {
		String[] data = new String[NumFields];
		data[FieldNums.NAME] = name;
		data[FieldNums.LOCATION] = location;
		data[FieldNums.OCCUPANCY] = Integer.toString(occupancy);
		data[FieldNums.SMOKING] = smoking ? "Y" : "N";
		data[FieldNums.PRICE] = price;
		data[FieldNums.DATE] = date;
		data[FieldNums.CUSTOMER] = customer;
		return data;
	}
	
	public int getRecNo() {
		return recNo;
	}

	public void setRecNo(int recNo) {
		this.recNo = recNo;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public int getOccupancy() {
		return occupancy;
	}

	public void setOccupancy(int occupancy) {
		this.occupancy = occupancy;
	}

	public boolean isSmoking() {
		return smoking;
	}

	public void setSmoking(boolean smoking) {
		this.smoking = smoking;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getCustomer() {
		return customer;
	}

	public void setCustomer(String customer) {
		this.customer = customer;
	}

	public static class FieldNums {
		public static final int NAME = 0;
		public static final int LOCATION = 1;
		public static final int OCCUPANCY = 2;
		public static final int SMOKING = 3;
		public static final int PRICE = 4;
		public static final int DATE = 5;
		public static final int CUSTOMER = 6;
	}
}
