package jcertdevtest;

/**
 * Handles the requirement for search where "the name and/or location fields 
 * exactly match values specified"
 * 
 * @author Ken Goh
 *
 */
public abstract class SearchCriteriaExact implements SearchCriteria {
	private static final long serialVersionUID = 1L;
	private String name;
	private String location;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
}