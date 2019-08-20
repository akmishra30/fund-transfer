package com.demo.fund.transfer.exception;

import java.util.Date;
import java.util.List;

import com.demo.fund.transfer.entity.ErrorDetail;
import com.demo.fund.transfer.util.APIUtil;

public class APIException extends Exception{

	private static final long serialVersionUID = 1L;
	
	private String transactionId;
	private String code;
	private Date timestamp;
	private String message;
	private List<ErrorDetail> errors;
	
	public APIException() {}
	
	public APIException(String code, String message) {
		this.transactionId = APIUtil.getTransactionIdFromMDC();
		this.timestamp = new Date(System.currentTimeMillis());
		this.code = code;
		this.message = message;
	}
	
	public APIException(String code, String message, List<ErrorDetail> errors) {
		this.transactionId = APIUtil.getTransactionIdFromMDC();
		this.timestamp = new Date(System.currentTimeMillis());
		this.code = code;
		this.message = message;
		this.errors = errors;
	}
	
	public String getTransactionId() {
		return transactionId;
	}
	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
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
	public List<ErrorDetail> getErrors() {
		return errors;
	}
	public void setErrors(List<ErrorDetail> errors) {
		this.errors = errors;
	}

	@Override
	public String toString() {
		return "APIException [transactionId=" + transactionId + ", code=" + code + ", timestamp=" + timestamp
				+ ", message=" + message + ", errors=" + errors + "]";
	}
	
	
}
