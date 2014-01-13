package jcertdevtest.net;

import jcertdevtest.SearchCriteria;

public class RemoteSearchRequest extends RemoteRequest {
	private static final long serialVersionUID = 1L;
	private SearchCriteria criteria;

	public SearchCriteria getCriteria() {
		return criteria;
	}

	public void setCriteria(SearchCriteria criteria) {
		this.criteria = criteria;
	}
}