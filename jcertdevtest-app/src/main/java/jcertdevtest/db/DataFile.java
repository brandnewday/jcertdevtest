package jcertdevtest.db;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles all the persistence actions to the data file.
 * Access to this class is synchronized as access to the underlying file 
 * resource can only be one at a time. 
 * 
 * @author Ken Goh
 *
 */
public class DataFile {
	
	private File file;
	private final RandomAccessFile raf;
	private DataFileSchema schema;
	private int newRecNo = 1;
	private static final Charset ASCII = Charset.forName("US-ASCII");
	
	public DataFile(String filePath) throws IOException {
		file = new File(filePath);
		raf = new RandomAccessFile(file, "rw");
		schema = new DataFileSchema(raf);
	}

	public synchronized Map<Integer, Record> load() throws IOException {
		raf.seek(schema.DataStartPos);
		byte[] recordBytes = new byte[schema.RecordLength];
		HashMap<Integer, Record> records = new HashMap<>();
		while(true) {
			int read = raf.read(recordBytes);
			if(read == -1)	// EOF
				break;
			if(read != schema.RecordLength) {
				throw new IOException("Unexpected file format when "
						+ " loading record " + newRecNo);				
			}
			ByteArrayInputStream in = new ByteArrayInputStream(recordBytes);
			boolean deleted = in.read() != 0;
			Record record = new Record(new String[schema.NumFields], deleted);
			for(int fieldNum = 0; fieldNum < schema.NumFields; ++fieldNum) {
				byte[] fieldBytes = new byte[schema.FieldLengths[fieldNum]];
				int fieldRead = in.read(fieldBytes);
				if(fieldRead != fieldBytes.length) {
					throw new IOException("Unexpected file format when "
							+ " loading record " + newRecNo + " field " + fieldNum);
				}
				record.getData()[fieldNum] = new String(fieldBytes, ASCII).trim();
			}
			records.put(newRecNo++, record);
		}
		return records;
	}

	public synchronized int create(String[] data) throws IOException {
		if(data.length != schema.NumFields)
			throw new IOException("input data array does not match fields in file");
		
		raf.seek(raf.length());
		raf.writeBoolean(false);
		writeData(data);
		return newRecNo++;
	}

	/**
	 * Update data for the given recNo. If record for that recNo has deleted
	 * flag set, it will be reset to not deleted.
	 */
	public synchronized void update(int recNo, String[] data) throws IOException {
		if(data.length != schema.NumFields)
			throw new IOException("input data array does not match fields in file");
		
		raf.seek(getFilePos(recNo));
		raf.writeBoolean(false); 
		writeData(data);
	}
	
	private void writeData(String[] data) throws IOException {
		for(int fieldNum = 0; fieldNum < schema.NumFields; ++fieldNum) {
			String value = data[fieldNum];
			if(value == null)
				value = "";
			byte[] valueBytes = value.getBytes(ASCII);
			if(valueBytes.length > schema.FieldLengths[fieldNum]) {
				throw new IOException("Update value " + value
						+ " for field " + fieldNum + " longer than allowed");				
			}
			raf.write(valueBytes);
			int padLength = schema.FieldLengths[fieldNum] - valueBytes.length;
			byte[] padBytes = new byte[padLength];
			Arrays.fill(padBytes, (byte)32);	// ASCII space character in decimal
			raf.write(padBytes);
		}
	}

	public synchronized void delete(int recNo) throws IOException {
		raf.seek(getFilePos(recNo));
		raf.writeBoolean(true);
	}
	
	private long getFilePos(int recNo) {
		return schema.DataStartPos + schema.RecordLength * (recNo - 1);
	}

}