package com.axa.api.model.response.openam;

/**
 * Model : Response gotten when asking the validity time of a new admin token (OpenAMDAO.retrieveUserAttributes())
 *
 */
public class RetrieveMaxTime {

	private Integer maxtime;

	public Integer getMaxtime() {
		return maxtime;
	}

	public void setMaxtime(Integer maxtime) {
		this.maxtime = maxtime;
	}
}
