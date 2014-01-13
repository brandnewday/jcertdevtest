package jcertdevtest.net;

/**
 * Pass into {@link NetworkSession} for listening to events.
 * 
 * @author Ken Goh
 *
 */
interface SessionEventListener {
	public void onReceive(Object message);
	public void onError(Exception e);
	public void onStopped();
}