package com.demo.fund.transfer.entity;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class FundTransfer {
	
	private String transactionId;
	@JsonProperty(required = true)
	private String fromAccount;
	@JsonProperty(required = true)
	private String toAccount;
	@JsonProperty(required = true)
	private String amount;
	
	
	public FundTransfer() {
		this.transactionId = UUID.randomUUID().toString();
	}
	
	public FundTransfer(String fromAcct, String toAcct, String amount) {
		this.transactionId = UUID.randomUUID().toString();
		this.fromAccount = fromAcct;
		this.toAccount = toAcct;
		this.amount = amount;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public String getFromAccount() {
		return fromAccount;
	}

	public void setFromAccount(String fromAccount) {
		this.fromAccount = fromAccount;
	}

	public String getToAccount() {
		return toAccount;
	}

	public void setToAccount(String toAccount) {
		this.toAccount = toAccount;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	@Override
	public String toString() {
		return "FundTransfer [transactionId=" + transactionId + ", fromAccount=" + fromAccount + ", toAccount="
				+ toAccount + ", amount=" + amount + "]";
	}
}
