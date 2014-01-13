package jcertdevtest.net;

import java.io.Serializable;

public class RemoteResponse implements Serializable {
	private static final long serialVersionUID = 1L;
	private long CorrId;
	private boolean Success;
	private String Message;
	
	public long getCorrId() {
		return CorrId;
	}
	public void setCorrId(long corrId) {
		CorrId = corrId;
	}
	public boolean isSuccess() {
		return Success;
	}
	public void setSuccess(boolean success) {
		Success = success;
	}
	public String getMessage() {
		return Message;
	}
	public void setMessage(String message) {
		Message = message;
	}
}