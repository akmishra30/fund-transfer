package com.demo.fund.transfer.entity;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class AccountRequest {
	private String transactionId;
	private String customerName;
	private String email;
	private String initialDeposit;
	//private String currencyCode;
	
	public AccountRequest() {
		this.transactionId = UUID.randomUUID().toString();
	}
	
	

	public AccountRequest(String customerName, String email, String initialDeposit) {
		this.transactionId = UUID.randomUUID().toString();
		this.customerName = customerName;
		this.email = email;
		this.initialDeposit = initialDeposit;
	}



	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getInitialDeposit() {
		return initialDeposit;
	}

	public void setInitialDeposit(String initialDeposit) {
		this.initialDeposit = initialDeposit;
	}

	@Override
	public String toString() {
		return "AccountRequest [transactionId=" + transactionId + ", customerName=" + customerName + ", email=" + email
				+ ", initialDeposit=" + initialDeposit + "]";
	}
}
