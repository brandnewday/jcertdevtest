package jcertdevtest.net;

import java.io.IOException;
import java.net.UnknownHostException;

import jcertdevtest.Booking;
import jcertdevtest.BookingService;
import jcertdevtest.Room;
import jcertdevtest.SearchCriteria;
import jcertdevtest.ServiceException;

/**
 * Provide the {@link BookingService} interface on the client side going over
 * the network.
 * 
 * @author Ken Goh
 *
 */
public class RemoteBookingServiceClient implements BookingService {
	
	private final NetworkClient network;

	public RemoteBookingServiceClient(String host, int port) throws UnknownHostException, IOException {
		network = new NetworkClient(host, port);
		network.start();
	}
	
	@Override
	public Room[] search(SearchCriteria criteria) throws ServiceException {
		RemoteSearchRequest request = new RemoteSearchRequest();
		request.setCriteria(criteria);
		RemoteResponse response;
		try {
			response = network.send(request);
		} catch (InterruptedException e) {
			throw new ServiceException("Error calling Booking Service", e);
		}
		if(response == null|| !(response instanceof RemoteSearchResponse))
			throw new ServiceException("Error calling Booking Service");
		if(!response.isSuccess())
			throw new ServiceException("Error performing search: " + response.getMessage());
		return ((RemoteSearchResponse)response).getResult();
	}

	@Override
	public void book(Booking booking) throws ServiceException {
		RemoteBookingRequest request = new RemoteBookingRequest();
		request.setBooking(booking);
		RemoteResponse response;
		try {
			response = network.send(request);
		} catch (InterruptedException e) {
			throw new ServiceException("Error calling Booking Service", e);
		}
		if(response == null)
			throw new ServiceException("Error calling Booking Service");
		if(!response.isSuccess())
			throw new ServiceException("Booking failed: " + response.getMessage());
	}
}
