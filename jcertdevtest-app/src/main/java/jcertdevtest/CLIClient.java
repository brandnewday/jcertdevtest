package jcertdevtest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Command-line client instead of the GUI client mentioned in the requirement.
 * The two user features required are available by entering text commands:
 * book (with customer id and record number), and search.
 * Command terms are all delimited by comma, just for simplicity of parsing.
 * 
 *  
 * @author Ken Goh
 *
 */
public class CLIClient {
	
	private final BookingService service;
	private BufferedWriter writer;

	public CLIClient(BookingService service) {
		this.service = service;
	}
	
	public void start() throws IOException {
		writer = new BufferedWriter(new OutputStreamWriter(System.out));
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		
		String command;
		writer.write("Enter command: (type help to show commands)\n");
		writer.flush();
		while((command = reader.readLine()) != null) {
			try {
				if(command.equals("help")) {
					handleHelp();
				}
				else if(command.startsWith("find")) {
					handleFind(command);
				} else if(command.startsWith("book")){
					handleBook(command);
				}
			} catch(ServiceException | IllegalArgumentException e) {
				writer.write("Error " + e.toString() + "\n");
			}

			writer.write("\n\nEnter command: (type help to show commands)\n");
			writer.flush();
		}
	}
	
	private void handleHelp() throws IOException {
		String helpText = "Available commands:\n"
				+ "Booking:\n"
				+ "    book,<record number>,<customer id>\n"
				+ "    E.g. book,3,54891209\n\n"
				+ "Searching:\n"
				+ "    find\n"
				+ "    find,{name|loc}=XX\n"
				+ "    find,name=XX,{and|or},loc=YY\n\n"
				+ "Show this text:\n"
				+ "    help\n";
		writer.write(helpText);
		writer.flush();
	}
	
	private void handleFind(String command) throws IOException, ServiceException {
		String[] args = command.split(",");
		SearchCriteria criteria;
		if(args.length == 1) {
			criteria = new SearchCriteriaAll();
		} else if(args.length == 2) {
			criteria = new SearchCriteriaExactOr();
			setNameOrLocation((SearchCriteriaExactOr)criteria, args[1]);
		} else if(args.length == 4) {
			String concat = args[2];
			if("or".equals(concat)) {
				criteria = new SearchCriteriaExactOr();
				setNameOrLocation((SearchCriteriaExactOr)criteria, args[1]);
				setNameOrLocation((SearchCriteriaExactOr)criteria, args[3]);
			} else if("and".equals(concat)) {
				criteria = new SearchCriteriaExactAnd();
				setNameOrLocation((SearchCriteriaExactAnd)criteria, args[1]);
				setNameOrLocation((SearchCriteriaExactAnd)criteria, args[3]);
			} else {
				throw new IllegalArgumentException("Invalid command");
			}
		} else {
			throw new IllegalArgumentException("Invalid command");
		}
		
		Room[] rooms = service.search(criteria);
		String header = String.format("|%-5s|%-50s|%-30s|%-10s|\n",
							"RecNo", "Name", "Location", "Customer");
		writer.write(header);
		for(Room room : rooms) {
			String display = 
					String.format("|%5d|%-50s|%-30s|%-10s|\n",
							room.getRecNo(),
							room.getName(),
							room.getLocation(),
							room.getCustomer());
			writer.write(display);
		}

		writer.flush();
	}
	
	private void setNameOrLocation(SearchCriteriaExact criteria, String term) {
		String[] keyValue = term.split("=");
		if("name".equals(keyValue[0]))
			criteria.setName(keyValue[1]);
		else if("loc".equals(keyValue[0]))
			criteria.setLocation(keyValue[1]);
		else
			throw new IllegalArgumentException("Invalid command");
	}
	
	private void handleBook(String command) throws IOException {
		String[] args = command.split(",");
		int recNo = Integer.parseInt(args[1]);
		String customer = args[2];
		Booking booking = new Booking();
		booking.setCustomer(customer);
		booking.setRecNo(recNo);
		try {
			service.book(booking);
			writer.write("Success\n");
		} catch (ServiceException e) {
			writer.write("Error " + e.toString() + "\n");
		}
		writer.flush();
	}
}
