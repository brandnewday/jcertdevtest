package jcertdevtest;

public interface BookingService {

	public abstract Room[] search(SearchCriteria criteria)
			throws ServiceException;

	public abstract void book(Booking booking) throws ServiceException;

}