package jcertdevtest.webapp;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import jcertdevtest.BookingService;
import jcertdevtest.BookingServiceImpl;
import jcertdevtest.db.Data;
import jcertdevtest.db.DataFile;
import jcertdevtest.db.DataPersistenceFileAdapter;

public class MainContextHandler implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext context = sce.getServletContext();
		String dbFilePath = context.getInitParameter("filePath");
		BookingService service;
		try {
			service = new BookingServiceImpl(
										new Data(
											new DataPersistenceFileAdapter(
												new DataFile(dbFilePath))));
		} catch (IOException e) {
			throw new RuntimeException("Failed to initialise BookingService", e);
		}
		
		context.setAttribute(Constants.BOOKING_SERVICE, service);
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
	}
}
