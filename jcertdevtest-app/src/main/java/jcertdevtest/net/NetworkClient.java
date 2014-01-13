package jcertdevtest.net;

import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Used by {@link RemoteBookingServiceClient} for handling the networking to 
 * server.
 * Exposes the request-response method call as normal blocking method call 
 * with return value, by internally managing the underlying session which is
 * non-blocking.
 * 
 * @author Ken Goh
 *
 */
public class NetworkClient implements SessionEventListener {
	private static final Logger log = Logger.getLogger(NetworkClient.class.getName());
	private final ExecutorService exec = Executors.newCachedThreadPool();
	private final Socket conn;
	private final NetworkSession session;
	private final ConcurrentHashMap<Long, RemoteResponse> responses = new ConcurrentHashMap<>();
	private AtomicLong newCorrId = new AtomicLong(1);
	private long timeout = 10000;
	
	public NetworkClient(String host, int port) throws UnknownHostException, IOException {
		conn = new Socket(host, port);
		session = new NetworkSession(-1, exec, conn);
	}
	
	public void start() {
		session.setEventListener(this);
		session.start();
	}
	
	public RemoteResponse send(RemoteRequest request) throws InterruptedException {
		long corrId = newCorrId.incrementAndGet();
		request.setCorrId(corrId);
		session.send(request);
		synchronized(responses) {
			while(!responses.containsKey(corrId)) {
				responses.wait(timeout);
			}
			return responses.remove(corrId);
		}
	}
	
	public void onReceive(Object o) {
		if(o instanceof RemoteHeartbeat) {
			// NO OP
		}
		else if(o instanceof RemoteResponse) {
			RemoteResponse response = (RemoteResponse)o;
			synchronized(responses) {
				responses.put(response.getCorrId(), response);
				responses.notifyAll();
			}
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
		
	}
}
