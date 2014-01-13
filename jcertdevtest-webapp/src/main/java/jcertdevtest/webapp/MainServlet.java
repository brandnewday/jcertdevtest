package jcertdevtest.webapp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jcertdevtest.BookingService;
import jcertdevtest.Room;
import jcertdevtest.SearchCriteria;
import jcertdevtest.SearchCriteriaAll;
import jcertdevtest.SearchCriteriaExact;
import jcertdevtest.SearchCriteriaExactAnd;
import jcertdevtest.SearchCriteriaExactOr;
import jcertdevtest.ServiceException;

/**
 * Handle the main default page allowing search.
 * 
 * @author Ken Goh
 *
 */
public class MainServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final Logger log = Logger.getLogger(MainServlet.class.getName());
	
	private BookingService service;

    public void init() {
    	this.service = (BookingService)getServletContext().getAttribute(Constants.BOOKING_SERVICE);
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(request.getParameter("search") != null) {
			SearchCriteria criteria = getSearchCritera(request);
			if(criteria != null) {
				try {
					Room[] rooms = service.search(criteria);
					List<DisplayRecord> records = new ArrayList<>(rooms.length);
					for(Room room : rooms) {
						records.add(new DisplayRecord(room));
					}
					if(records.isEmpty()) {
						request.setAttribute("notice", "No match found");						
					} else {
						request.setAttribute("colHeaders", DisplayRecord.getHeaders());
						request.setAttribute("rooms", records);
					}
				} catch (ServiceException e) {
					log.log(Level.SEVERE, "Error calling BookingService.search", e);
					request.setAttribute("notice", "Error displaying data");
				}				
			}
		}
		request.getRequestDispatcher("/index.jsp").forward(request, response);
	}
	
	private SearchCriteria getSearchCritera(HttpServletRequest request) {
		SearchCriteria criteria;
		if(request.getParameter("all") != null) {
			criteria = new SearchCriteriaAll();
		} else {
			String name = request.getParameter("name");
			String location = request.getParameter("location");
			if ("AND".equals(request.getParameter("concat"))) {
				criteria = new SearchCriteriaExactAnd();
				if(name == null || name.length() == 0
						|| location == null || location.length() == 0) {
					request.setAttribute("notice", "Both Name and Location should be specified for AND search");
					return null;
				}
				((SearchCriteriaExact)criteria).setName(name);
				((SearchCriteriaExact)criteria).setLocation(location);
			} else {
				criteria = new SearchCriteriaExactOr();
				if((name == null || name.length() == 0)
						&& (location == null || location.length() == 0)) {
					request.setAttribute("notice", "At least one of Name and Location should be specified for OR search");
					return null;
				}
				if(name != null && name.length() > 0)
					((SearchCriteriaExact)criteria).setName(name);
				if(location != null && location.length() > 0)
					((SearchCriteriaExact)criteria).setLocation(location);
			}
		}
		return criteria;
	}
}
