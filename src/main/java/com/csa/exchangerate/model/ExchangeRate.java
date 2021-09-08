package com.csa.exchangerate.model;

import javax.validation.constraints.Min;

public class ExchangeRate {

	@CurrencyCode
	private String from;
	@CurrencyCode
	private String to;
	@Min(0)
	private float rate;
	@ISODate
	private String reportedOn;
	
	public ExchangeRate(String from, String to, float rate, String reportedOn) {
		this.from = from;
		this.to = to;
		this.rate = rate;
		this.reportedOn = reportedOn;
	}
	
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	public float getRate() {
		return rate;
	}
	public void setRate(float rate) {
		this.rate = rate;
	}
	public String getReportedOn() {
		return reportedOn;
	}
	public void setReportedOn(String reportedOn) {
		this.reportedOn = reportedOn;
	}
	
	
}
