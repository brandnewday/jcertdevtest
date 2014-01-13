package jcertdevtest.webapp;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jcertdevtest.Booking;
import jcertdevtest.BookingService;
import jcertdevtest.ServiceException;

public class BookingServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private BookingService service;

    public void init() {
    	this.service = (BookingService)getServletContext().getAttribute(Constants.BOOKING_SERVICE);
    }

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String recNoStr = request.getParameter("recNo");
		String customer = request.getParameter("customer");
		if(recNoStr != null && customer != null) {
			int recNo = Integer.parseInt(recNoStr);
			Booking booking = new Booking();
			booking.setRecNo(recNo);
			booking.setCustomer(customer);
			try {
				service.book(booking);
				request.setAttribute("Message", "Booking completed");
			} catch (ServiceException e) {
				
				request.setAttribute("Failure", null);
				request.setAttribute("Message", e.getMessage());
			}
		}
		request.getRequestDispatcher("/booking.jsp").forward(request, response);
	}
}
