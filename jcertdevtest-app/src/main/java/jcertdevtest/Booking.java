package jcertdevtest;

import java.io.Serializable;

/**
 * Represents a request to book to {@link BookingServiceImpl}.
 * 
 * With the given requirement, there is a length limit on the customer value.
 * Caller must validate it or {@link BookingServiceImpl} will throw exception.
 * 
 * @author Ken Goh
 *
 */
public class Booking implements Serializable {
	private static final long serialVersionUID = 1L;
	private int recNo;
	private String customer;
	public int getRecNo() {
		return recNo;
	}
	public void setRecNo(int recNo) {
		this.recNo = recNo;
	}
	public String getCustomer() {
		return customer;
	}
	public void setCustomer(String customer) {
		this.customer = customer;
	}
	@Override
	public String toString() {
		return "RecNo:" + recNo + " Customer:" + customer;
	}
}
