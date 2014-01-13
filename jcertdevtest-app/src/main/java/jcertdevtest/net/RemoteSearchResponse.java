package jcertdevtest.net;

import jcertdevtest.Room;

public class RemoteSearchResponse extends RemoteResponse {
	private static final long serialVersionUID = 1L;
	private Room[] Result;

	public Room[] getResult() {
		return Result;
	}

	public void setResult(Room[] result) {
		Result = result;
	}
}