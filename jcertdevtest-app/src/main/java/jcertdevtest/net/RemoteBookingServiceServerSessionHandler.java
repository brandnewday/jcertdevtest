package jcertdevtest.net;

import java.io.EOFException;
import java.util.logging.Level;
import java.util.logging.Logger;

import jcertdevtest.BookingService;
import jcertdevtest.Room;
import jcertdevtest.ServiceException;

/**
 * Handles messages and events per session in the {@link NetworkServer}
 * 
 * @author Ken Goh
 *
 */
class RemoteBookingServiceServerSessionHandler implements SessionEventListener {
	private static final Logger log = Logger.getLogger(RemoteBookingServiceServerSessionHandler.class.getName());
	private final NetworkSession session;
	private final BookingService service;
	private final NetworkServer server;
	
	public RemoteBookingServiceServerSessionHandler(NetworkSession session, NetworkServer server, BookingService service) {
		this.session = session;
		this.service = service;
		this.server = server;
	}
	
	public void onReceive(Object request) {
		if(request.getClass().equals(RemoteHeartbeat.class)) {
			;	// NO OP
		}
		else if(request.getClass().equals(RemoteBookingRequest.class)) {
			handleBooking((RemoteBookingRequest)request);
		}
		else if(request.getClass().equals(RemoteSearchRequest.class)) {
			handleSearch((RemoteSearchRequest)request);
		}
	}
	
	public void onError(Exception e) {
		if(e instanceof EOFException
				|| e instanceof RemoteNoActivityException) {
			session.signalStop();
		} else {
			log.log(Level.SEVERE, "Error occurred", e);
		}
	}
	
	public void onStopped() {
		server.onSessionStopped(session);
	}
	
	private void handleBooking(RemoteBookingRequest bookingRequest) {
		RemoteResponse response = new RemoteResponse();
		response.setCorrId(bookingRequest.getCorrId());
		try {
			service.book(bookingRequest.getBooking());
			response.setSuccess(true);
		} catch (ServiceException e) {
			response.setSuccess(false);
			response.setMessage(e.getMessage());
		}
		session.send(response);
	}
	
	private void handleSearch(RemoteSearchRequest searchRequest) {
		RemoteSearchResponse response = new RemoteSearchResponse();
		response.setCorrId(searchRequest.getCorrId());
		try {
			Room[] result = service.search(searchRequest.getCriteria());
			response.setSuccess(true);
			response.setResult(result);
		} catch (ServiceException e) {
			response.setSuccess(false);
			response.setMessage(e.getMessage());
		}
		session.send(response);
	}
}