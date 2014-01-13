package jcertdevtest;

/**
 * For the requirement of "search the data for all records"
 * 
 * Some uncertainty here whether this means "allow user to do some kind of 
 * search", or just "return all records". But the former is too vague and
 * considering the other requirement is "match name/location specified by
 * user", it is likely that this is simply return all, and actual "search"
 * to match some value is covered by the other requirement. Also, there is 
 * no requirement for "display all" but simply "present search result". So 
 * this is another hint that requirement means to "search all" for 
 * displaying all data. 
 * 
 * @author Ken Goh
 *
 */
public class SearchCriteriaAll implements SearchCriteria {
	private static final long serialVersionUID = 1L;
}