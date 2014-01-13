package jcertdevtest;

import static org.junit.Assert.*;
import jcertdevtest.db.DB;
import jcertdevtest.db.RecordNotFoundException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.mockito.Mockito.*;

public class BookingServiceImplTest {

	private BookingServiceImpl sut;
	private DB data;
	
	@Before
	public void setUp() throws Exception {
		
		data = mock(DB.class);
		sut = new BookingServiceImpl(data);
		
		Room room1 = new Room();
		room1.setRecNo(1);
		room1.setName("N1");
		room1.setLocation("L1");
		when(data.read(1)).thenReturn(room1.toRecord());
		Room room2 = new Room();
		room2.setRecNo(2);
		room2.setName("N2");
		room2.setLocation("L2");
		room2.setCustomer("C2");
		when(data.read(2)).thenReturn(room2.toRecord());
	}

	@Test
	public void testSearchAll() throws ServiceException {
		SearchCriteriaAll criteria = new SearchCriteriaAll();
		when(data.find(new String[Room.NumFields])).thenReturn(new int[] {1,2});
		Room[] rooms = sut.search(criteria);
		assertEquals(2, rooms.length);			
	}
		
	@Test
	public void testSearchAnd1() throws ServiceException {
		SearchCriteriaExactAnd criteria = new SearchCriteriaExactAnd();
		criteria.setLocation("L2");
		criteria.setName("N2");
		ArgumentCaptor<String[]> dataArg = ArgumentCaptor.forClass(String[].class);
		when(data.find(dataArg.capture())).thenReturn(new int[] {2});
		
		Room[] rooms = sut.search(criteria);
		
		String[] expected = new String[Room.NumFields];
		expected[Room.FieldNums.NAME] = criteria.getName();
		expected[Room.FieldNums.LOCATION] = criteria.getLocation();
		assertArrayEquals(expected, dataArg.getValue());
		assertEquals(2, rooms[0].getRecNo());
		assertEquals(1, rooms.length);
	}
	
	@Test
	public void testSearchAnd2() throws ServiceException {
		SearchCriteriaExactAnd criteria = new SearchCriteriaExactAnd();
		criteria.setLocation("L1");
		criteria.setName("N2");
		when(data.find(any(String[].class))).thenReturn(new int[0]);
		
		Room[] rooms = sut.search(criteria);
		
		assertEquals(0, rooms.length);			
	}
		
	@Test
	public void testSearchOr1() throws ServiceException {
		SearchCriteriaExactOr criteria = new SearchCriteriaExactOr();
		criteria.setLocation("L2");
		criteria.setName("N2");

		String[] search1 = new String[Room.NumFields];
		search1[Room.FieldNums.NAME] = criteria.getName();
		String[] search2 = new String[Room.NumFields];
		search2[Room.FieldNums.LOCATION] = criteria.getLocation();
		
		when(data.find(search1)).thenReturn(new int[] {2});
		when(data.find(search2)).thenReturn(new int[] {2});
		
		Room[] rooms = sut.search(criteria);
		
		assertEquals(2, rooms[0].getRecNo());
		assertEquals(1, rooms.length);
	}
	
	@Test
	public void testSearchOr2() throws ServiceException {
		SearchCriteriaExactOr criteria = new SearchCriteriaExactOr();
		criteria.setLocation("L1");
		criteria.setName("N2");

		String[] search1 = new String[Room.NumFields];
		search1[Room.FieldNums.NAME] = criteria.getName();
		String[] search2 = new String[Room.NumFields];
		search2[Room.FieldNums.LOCATION] = criteria.getLocation();

		when(data.find(search1)).thenReturn(new int[] {2});
		when(data.find(search2)).thenReturn(new int[] {1});
		
		Room[] rooms = sut.search(criteria);
		
		assertEquals(2, rooms.length);
	}

	@Test
	public void testBook() throws ServiceException, SecurityException, RecordNotFoundException {
		Booking booking = new Booking();
		booking.setCustomer("111111");
		booking.setRecNo(1);
		sut.book(booking);

		ArgumentCaptor<String[]> dataArg = ArgumentCaptor.forClass(String[].class);
		verify(data).update(eq(1), dataArg.capture(), anyLong());
		assertEquals(booking.getCustomer(), dataArg.getValue()[Room.FieldNums.CUSTOMER]);
		
		booking = new Booking();
		booking.setCustomer("111111");
		booking.setRecNo(2);
		
		try {
			sut.book(booking);
			fail("Should not be able to book on a room that has customer set");
		} catch(ServiceException e) {
			assertTrue(e.getMessage().indexOf("already booked") != -1);
		}
	}

}
