package jcertdevtest.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import jcertdevtest.BookingService;
import jcertdevtest.BookingServiceImpl;
import jcertdevtest.db.Data;

/**
 * Socket server for accepting client connections for accessing the booking 
 * service over network.
 * 
 * @author Ken Goh
 *
 */
public class NetworkServer {
	private final static Logger log = Logger.getLogger(NetworkServer.class.getName());
	private final ExecutorService exec = Executors.newFixedThreadPool(50);
	private final int port;
	private final BookingService service;
	private final ConcurrentHashMap<Integer, NetworkSession> sessions = new ConcurrentHashMap<Integer, NetworkSession>();
	private volatile int newSessionId = 1;
	private ServerSocket socket;
	private final AtomicBoolean stopFlag = new AtomicBoolean(false);

	public NetworkServer(String filePath, int port) throws IOException {
		this.port = port;
		Data db = new Data(filePath); 
		this.service = new BookingServiceImpl(db);
	}
	
	public void start() throws IOException {
		socket = new ServerSocket(port);
		exec.execute(new Runnable() {
			@Override
			public void run() {
				log.info("Listening for client connections on port " + port);
				while(!stopFlag.get()) {
					try {
						final Socket conn = socket.accept();
						log.info("Received client connection");
						NetworkSession session = new NetworkSession(newSessionId, exec, conn);
						RemoteBookingServiceServerSessionHandler handler = 
								new RemoteBookingServiceServerSessionHandler(session, NetworkServer.this, service);
						session.setEventListener(handler);
						sessions.put(newSessionId, session);
						session.start();
						log.info("Started session " + newSessionId);
						++newSessionId;
					} catch (SocketException e) {
						log.log(Level.SEVERE, "SocketException received - stopping server", e);
						stopFlag.set(true);
					} catch (IOException e) {
						log.log(Level.SEVERE, "Failed to create client session", e);
					}
				}
				log.info("Client listening thread stopped");
			}
		});
	}
	
	public void stop() throws IOException, InterruptedException {
		log.info("Stopping server");
		stopFlag.set(true);
		socket.close();
		for(NetworkSession session : sessions.values()) {
			session.stop();
		}
		exec.shutdownNow();
		if(!exec.awaitTermination(10, TimeUnit.SECONDS)) {
			log.warning("ExecutorService await termination timed out.");
		}
		log.info("Server stopped");
	}
	
	public void onSessionStopped(NetworkSession session) {
		sessions.remove(session.getSessionId());
	}
}
