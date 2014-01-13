package jcertdevtest.net;

import java.io.Serializable;

public abstract class RemoteRequest implements Serializable{
	private static final long serialVersionUID = 1L;
	private long corrId;
	
	public void setCorrId(long corrId) {
		this.corrId = corrId;
	}
	
	public long getCorrId() {
		return corrId;
	}
}
