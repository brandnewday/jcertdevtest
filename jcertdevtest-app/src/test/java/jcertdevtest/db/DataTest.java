package jcertdevtest.db;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class DataTest extends TestCase {
	private final static Logger log = Logger.getLogger(DataTest.class.getName());
	
	private Data sut;
	private DataPersistence persistence;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		persistence = mock(DataPersistence.class);
		
		HashMap<Integer, Record> data = new HashMap<Integer, Record>();
		data.put(1, new Record(new String[] {"1"}, false));
		data.put(2, new Record(new String[] {"2"}, false));
		when(persistence.load()).thenReturn(data);
		
		sut = new Data(persistence);
	}
	
	@Test
	public void testCreate() throws DuplicateKeyException {
		int expectedRecNo = 3;
		String[] newData = new String[] {"3"};
		when(persistence.create(any(String[].class))).thenReturn(expectedRecNo);
		int recNo = sut.create(newData);
		
		verify(persistence, times(1)).create(newData);
		assertEquals(expectedRecNo, recNo);
	}
	
	@Test
	public void testCreateReuseDeleted() throws DuplicateKeyException, SecurityException, RecordNotFoundException {
		int recNo = 2;
		String[] newData = new String[] {"3"};
		long cookie = sut.lock(recNo);
		sut.delete(recNo, cookie);
		sut.unlock(recNo, cookie);
		int newRecNo = sut.create(newData);
		
		verify(persistence, times(1)).update(recNo, newData);
		assertEquals(recNo, newRecNo);
	}

	@Test
	public void testDelete() throws SecurityException, RecordNotFoundException {
		int recNo = 1;
		long cookie = sut.lock(recNo);
		sut.delete(recNo, cookie);	
		sut.unlock(recNo, cookie);
		
		verify(persistence, times(1)).delete(recNo);
		
		try {
			sut.lock(recNo);
			fail("Should throw exception");
		} catch(RecordNotFoundException e) {
		}
	}
	
	@Test
	public void testUpdate() throws IOException, RecordNotFoundException {
		int recNo = 2;
		long cookie = sut.lock(recNo);
		sut.update(recNo, new String[] {"2e"}, cookie);	
		sut.unlock(recNo, cookie);
		
		verify(persistence, times(1)).update(recNo, new String[] {"2e"});
	}
	
	@Test
	public void testUpdateMultiThreads() throws IOException, InterruptedException {
		// prepare multiple threads trying to check on an existing value
		// in data, then update it to a different value.
		// if there is race condition, multiple threads will see
		// TRUE in the value check and do update. verify at the end that
		// only ONE call is made to persistence, i.e. only 1 thread succeeded,
		// so unlikely having race condition.
		// (do the above twice on two different records)
		
		int numThreads = 50;
		ExecutorService es = Executors.newFixedThreadPool(numThreads);
		final CountDownLatch latch = new CountDownLatch(numThreads);
		final ArrayList<Exception> taskExceptions = new ArrayList<>();
		for(int i = 0; i < numThreads; ++i) {
			final int taskNum = i;
			es.execute(new Runnable() {
				@Override
				public void run() {
					try {
						// wait for all threads to get here before proceeding
						latch.countDown();
						latch.await();
						
						// mix them up: some threads try 1 first, some try 2 first
						if(taskNum % 2 == 0) {
							updateTask(sut, 1, "1", "1e");
							updateTask(sut, 2, "2", "2e");
						} else {
							updateTask(sut, 2, "2", "2e");
							updateTask(sut, 1, "1", "1e");
						}						
					} catch(Exception e) {
						taskExceptions.add(e);
						log.log(Level.SEVERE, "Exception in task", e);
					}
				}				
			});
		}
		es.shutdown();
		es.awaitTermination(1, TimeUnit.MINUTES);
		
		assertTrue("Task has exception(s)", taskExceptions.isEmpty());
		
		verify(persistence, times(1)).update(1, new String[] {"1e"});
		verify(persistence, times(1)).update(2, new String[] {"2e"});
	}

	private void updateTask(Data sut, int recNo, String fromValue, String toValue) {
		try  {
			long cookie = sut.lock(recNo);
			String[] data = sut.read(recNo);
			if(data[0].equals(fromValue)) {
				Thread.sleep(500);	// add a delay to give wider window for race condition
				sut.update(recNo, new String[] {toValue}, cookie);	
				log.info("Updated " + recNo + " by task " + Thread.currentThread().getId());
			}
			sut.unlock(recNo, cookie);
		} catch(RecordNotFoundException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
