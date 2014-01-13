package jcertdevtest.db;

class Record {
	private volatile String[] data;
	private volatile boolean deleted;
	public Record(String[] data, boolean deleted) {
		this.data = data;
		this.deleted = deleted;
	}
	public String[] getData() {
		return data;
	}
	public void setData(String[] data) {
		this.data = data;
	}
	public boolean isDeleted() {
		return deleted;
	}
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
}