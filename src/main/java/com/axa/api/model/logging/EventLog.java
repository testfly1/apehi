package com.axa.api.model.logging;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Model : Generation of an EventLog
 *
 */
public class EventLog {
	
	private TimeZone tz = TimeZone.getTimeZone("UTC");
	private DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	ObjectMapper mapper = new ObjectMapper();

	private String timestamp;
	private String level;
	private String eventType;
	private Object input;
	private Integer resultCode;
	private Object details;
	
	public EventLog(String timestamp, String level, String eventType, String input, Integer resultCode, String details) {
		this.timestamp = timestamp;
		this.level = level;
		this.eventType = eventType;
		this.input = input;
		this.resultCode = resultCode;
		this.details = details;
	}
	
	public EventLog(String level, String eventType, String input, Integer resultCode, String details) {
		df.setTimeZone(tz);
		this.timestamp = df.format(new Date());
		this.level = level;
		this.eventType = eventType;
		this.input = input;
		this.resultCode = resultCode;
		this.details = details;
	}
	
	public EventLog(String level, String eventType, Object input, Integer resultCode, Object details) {
		df.setTimeZone(tz);
		this.timestamp = df.format(new Date());
		this.level = level;
		this.eventType = eventType;
		this.input = input;
		this.resultCode = resultCode;
		this.details = details;
	}
	
	public String getLevel() {
		return level;
	}
	public void setLevel(String level) {
		this.level = level;
	}
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public String getEventType() {
		return eventType;
	}
	public void setEventType(String eventType) {
		this.eventType = eventType;
	}
	public Object getInput() {
		return input;
	}
	public void setInput(Object input) {
		this.input = input;
	}
	public Integer getResultCode() {
		return resultCode;
	}
	public void setResultCode(Integer resultCode) {
		this.resultCode = resultCode;
	}
	public Object getDetails() {
		return details;
	}
	public void setDetails(Object details) {
		this.details = details;
	}

	@Override
	public String toString() {
		try {
			return mapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}