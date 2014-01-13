package jcertdevtest;

import java.io.IOException;

import jcertdevtest.db.Data;
import jcertdevtest.db.DataFile;
import jcertdevtest.db.DataPersistenceFileAdapter;
import jcertdevtest.net.NetworkServer;
import jcertdevtest.net.RemoteBookingServiceClient;

/**
 * Application entry point.
 * Similar to the requirement, this allows startup in one of three modes:
 * 1. client with network;
 * 2. server with network;
 * 3. standalone (no network).
 * 
 * Note that this is a test version and does not follow the cert requirements.
 * E.g. a CLI client is used instead of a Swing GUI client; we get config
 * parameters from command line properties etc.
 * 
 * @author Ken Goh
 *
 */
public class TestAppMain 
{
    public static void main( String[] args ) throws NumberFormatException, IOException, InterruptedException
    {
    	String mode = args[0];
    	switch(mode) {
    	case "server": {
        	NetworkServer server = new NetworkServer(
					System.getProperty("filePath"),
					Integer.parseInt(System.getProperty("port")));
			server.start();
			System.in.read();
			server.stop();    		
    	}
			break;
    	case "client": {
    		CLIClient client = new CLIClient(
					new RemoteBookingServiceClient(
							System.getProperty("host"),
							Integer.parseInt(System.getProperty("port"))));
    		client.start();
    	}
    		break;
    	case "standalone": {
    		CLIClient client = new CLIClient(
					new BookingServiceImpl(
						new Data(
							new DataPersistenceFileAdapter(
								new DataFile(
									System.getProperty("filePath"))))));
    		client.start();
    	}
    		break;
    	}
    }
}
