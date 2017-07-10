package com.axa.api.model.response.openam;

import java.util.List;

/**
 * Model : Response gotten when asking informations on a user (OpenAMDAO.retrieveUserAttributes())
 *
 */
public class RetrieveInfoResult {
	
	private List<UserInfo> result;
	private int resultCount;
	private String pagedResultsCookie;
	private String totalPagedResultsPolicy;
	private int totalPagedResults;
	private int remainingPagedResults;
	
	public List<UserInfo> getResult() {
		return result;
	}
	public void setResult(List<UserInfo> result) {
		this.result = result;
	}
	public int getResultCount() {
		return resultCount;
	}
	public void setResultCount(int resultCount) {
		this.resultCount = resultCount;
	}
	public String getPagedResultsCookie() {
		return pagedResultsCookie;
	}
	public void setPagedResultsCookie(String pagedResultsCookie) {
		this.pagedResultsCookie = pagedResultsCookie;
	}
	public String getTotalPagedResultsPolicy() {
		return totalPagedResultsPolicy;
	}
	public void setTotalPagedResultsPolicy(String totalPagedResultsPolicy) {
		this.totalPagedResultsPolicy = totalPagedResultsPolicy;
	}
	public int getTotalPagedResults() {
		return totalPagedResults;
	}
	public void setTotalPagedResults(int totalPagedResults) {
		this.totalPagedResults = totalPagedResults;
	}
	public int getRemainingPagedResults() {
		return remainingPagedResults;
	}
	public void setRemainingPagedResults(int remainingPagedResults) {
		this.remainingPagedResults = remainingPagedResults;
	}

}
