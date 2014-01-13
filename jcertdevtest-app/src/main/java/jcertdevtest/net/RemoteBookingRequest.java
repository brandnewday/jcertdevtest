package jcertdevtest.net;

import jcertdevtest.Booking;

public class RemoteBookingRequest extends RemoteRequest {

	private static final long serialVersionUID = 7418043129343181705L;
	
	private Booking booking;

	public Booking getBooking() {
		return booking;
	}

	public void setBooking(Booking booking) {
		this.booking = booking;
	}
	
}