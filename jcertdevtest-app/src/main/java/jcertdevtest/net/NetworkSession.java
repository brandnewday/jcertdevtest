package jcertdevtest.net;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles the socket connection after accepting from server socket. 
 * Uses 2 threads to allow non-blocking input and output to socket. 
 * Uses java serialization for socket IO. Calling class can pass in 
 * {@link SessionEventListener} for receiving messages and events.
 * Internally also does heartbeat to keep connection alive and checks
 * for timeout.
 * 
 * @author Ken Goh
 *
 */
class NetworkSession {
	private static final Logger log = Logger.getLogger(NetworkSession.class.getName());
	private static final int HEARTBEAT_INTERVAL = 30;
	private static final long RECV_TIMEOUT_SEC = 120;
	private static final RemoteHeartbeat heartbeat = new RemoteHeartbeat();
	private final Socket conn;
	private final ObjectInputStream inStream;
	private final ObjectOutputStream outStream;
	private final ExecutorService exec;
	private final BlockingQueue<Object> sendQueue = new LinkedBlockingQueue<>();
	private Future<Object> inputTask;
	private Future<Object> outputTask;
	private SessionEventListener handler;
	private volatile Date lastRecvTime;
	private final int sessionId;
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
	private final AtomicBoolean stopFlag = new AtomicBoolean(false);
	private final CountDownLatch tasksStopWaitLatch = new CountDownLatch(2);

	public NetworkSession(int id, ExecutorService exec, Socket conn) throws IOException {
		this.sessionId = id;
		this.exec = exec;
		this.conn = conn;
		this.outStream = new ObjectOutputStream(conn.getOutputStream());
		outStream.flush();
		this.inStream = new ObjectInputStream(conn.getInputStream());
		log.info("Creating session " + id + " connected to " + conn.getRemoteSocketAddress());
	}
	public void setEventListener(SessionEventListener handler) {
		this.handler = handler;
	}

	public void start() {
		log.info("Starting session " + sessionId);
		
		startInput();
		startOutput();
		startHeartbeat();
		startTimeoutCheck();
		
		log.info("Session started " + sessionId);
	}
	
	private void startInput() {
		inputTask = exec.submit(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				log.info("Input thread started");
				while(!stopFlag.get()) {
					try {
						Object request = inStream.readObject();
						lastRecvTime = new Date();
						handler.onReceive(request);
					} catch(EOFException e) {
						log.log(Level.WARNING, "Socket closed", e);
						handler.onError(e);
					} catch (Exception e) {
						handler.onError(e);
					}
				}
				log.info("Input thread shut down");
				tasksStopWaitLatch.countDown();
				return null;
			}
		});
	}
	
	private void startOutput() {
		outputTask = exec.submit(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				log.info("Output thread started");
				while(!stopFlag.get()) {
					try {
						Object o = sendQueue.take();
						outStream.writeObject(o);
						outStream.flush();
					} catch(InterruptedException e) {
						log.info("Output thread interrupted");
					} catch(IOException e) {
						handler.onError(e);
					} catch (Exception e) {
						handler.onError(e);
					}
				}
				log.info("Output thread shut down");
				tasksStopWaitLatch.countDown();
				return null;
			}
		});
	}
	
	private void startHeartbeat() {
		scheduler.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				log.info("Sending heartbeat");
				send(heartbeat);
			}
		}, HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, TimeUnit.SECONDS);
	}
	
	private void startTimeoutCheck() {
		scheduler.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				long elapsedSeconds = (new Date().getTime() - getLastRecvTime().getTime()) / (1000);
				if(elapsedSeconds > RECV_TIMEOUT_SEC) {
					log.severe("No message received from remote for " + RECV_TIMEOUT_SEC + "s. Disconnecting.");
					handler.onError(new RemoteNoActivityException());
				}
			}
		}, HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, TimeUnit.SECONDS);
	}
	
	public void send(Object o) {
		sendQueue.offer(o);
	}
	
	public void signalStop() {
		stop(false);
	}
	
	public void stop() {
		stop(true);
	}
	
	private void stop(boolean wait) {
		log.info("Stopping session " + sessionId);
		stopFlag.set(true);
		scheduler.shutdownNow();
		try {
			conn.close();
		} catch (IOException e) {
			log.log(Level.SEVERE, "Exception when closing socket", e);
		}
		outputTask.cancel(true);
		
		if(wait) {
			awaitTermination();
		} else {
			exec.execute(new Runnable() {
				@Override
				public void run() {
					log.info("Starting stop session thread");
					awaitTermination();
				}
			});
		}
	}
	
	private void awaitTermination() {
		try {
			scheduler.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			log.log(Level.SEVERE, "Interrupted when waiting for tasks to stop", e);
		}
		try {
			// cannot use Future.get() to wait for task complete because if it
			// was cancelled, it get() immediately throws exception.
			tasksStopWaitLatch.await();
		} catch (InterruptedException e) {
			log.log(Level.SEVERE, "Interrupted when waiting for tasks to stop", e);
		}
		handler.onStopped();
		log.info("Session stopped " + sessionId);
	}

	private Date getLastRecvTime() {
		return lastRecvTime;
	}
	public int getSessionId() {
		return sessionId;
	}
}

/**
 * Raised by {@link NetworkSession} when no message received from far end of
 * socket after a specific time.
 * 
 * @author Ken Goh
 *
 */
class RemoteNoActivityException extends Exception {

	private static final long serialVersionUID = 1L;	
}