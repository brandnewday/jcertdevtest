package jcertdevtest.db;

/**
 * Group all record related info.
 * 
 * @author Ken Goh
 *
 */
class RecordInfo {
	private final Number num;
	private final Record data;
	private final RecordLock lock = new RecordLock();
	public RecordInfo(Number recNo, Record data) {
		this.num = recNo;
		this.data = data;
	}
	public String[] getData() {
		return data.getData();
	}
	public void setData(String[] data) {
		this.data.setData(data);
	}
	public RecordLock getLock() {
		return lock;
	}
	public Number getRecNo() {
		return num;
	}
	public boolean isDeleted() {
		return data.isDeleted();
	}
	public void setDeleted(boolean deleted) {
		this.data.setDeleted(deleted);
	}
	
	/**
	 * Immutable value class to represent a recNo. More explicit than passing
	 *  int around.
	 *
	 */
	public final static class Number {
		private final int recNo;
		public Number(int recNo) {
			this.recNo = recNo;
		}
		public static Number fromValue(int recNo) {
			return new Number(recNo);
		}
		public int getValue() {
			return recNo;
		}
		@Override
		public boolean equals(Object o) {
			if(o != null && o instanceof Number) {
				return ((Number)o).recNo == this.recNo;
			}
			return false;
		}
		@Override
		public int hashCode() {
			return recNo;
		}
	}
}