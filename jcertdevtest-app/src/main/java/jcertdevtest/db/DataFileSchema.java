package jcertdevtest.db;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Reads schema info from file. Immutable. 
 * @author Ken Goh
 *
 */
final class DataFileSchema {
	public final int RecordLength;
	public final int DataStartPos;
	public final int NumFields;
	public final int[] FieldLengths;
	
	public DataFileSchema(RandomAccessFile in) throws IOException {
		int headerLength = 0;
		int cookie = in.readInt();
		headerLength += 4;
		RecordLength = in.readInt() + 1;	// length here is only for data part. 1 byte extra for delete flag
		headerLength += 4;
		NumFields = in.readShort();
		headerLength += 2;
		FieldLengths = new int[NumFields];
		
		for(int fieldNum = 0; fieldNum < NumFields; ++fieldNum) {
			short fieldNameLength = in.readShort();
			headerLength += 2;
			in.skipBytes(fieldNameLength);
			headerLength += fieldNameLength;
			FieldLengths[fieldNum] = in.readShort();
			headerLength += 2;
		}
		DataStartPos = headerLength;
	}
}