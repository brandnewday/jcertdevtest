package jcertdevtest.db;

import java.security.SecureRandom;
import java.util.concurrent.locks.ReentrantLock;

class RecordLock {
	private final ReentrantLock inner = new ReentrantLock();
	private volatile long cookie;
	private final SecureRandom rand = new SecureRandom();
	
	public long lock() {
		inner.lock();
		cookie = rand.nextLong();
		return cookie;
	}
	
	public boolean checkCookie(long cookie) {
		return this.cookie == cookie;
	}
	
	public void unlock(long cookie) throws SecurityException {
		if(this.cookie != cookie)
			throw new SecurityException();
		inner.unlock();
	}
}
