package com.demo.fund.transfer.entity;

import java.util.Date;

import com.demo.fund.transfer.util.APIUtil;

public class APISuccess {
	private String transactionId;
	private Date timestamp;
	private String message;
	
	public APISuccess() {
		
	}
	
	public APISuccess(String message) {
		this.transactionId = APIUtil.getTransactionIdFromMDC();
		this.timestamp = new Date(System.currentTimeMillis());
		this.message = message;
	}
	
	public String getTransactionId() {
		return transactionId;
	}
	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return "APISuccess [transactionId=" + transactionId + ", timestamp=" + timestamp + ", message=" + message + "]";
	}
}
