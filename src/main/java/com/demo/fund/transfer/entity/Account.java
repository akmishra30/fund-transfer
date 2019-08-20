package com.demo.fund.transfer.entity;

import java.math.BigDecimal;

public class Account {
	private Long customerId;
	
	private Long accountNo;
	
	private BigDecimal balance;
	
	private Boolean active;
	
	private String currency;
	
	public Account() {
		
	}
	
	public Account(Long customerId, BigDecimal balance, String currency, Boolean active) {
		this.customerId = customerId;
		this.balance = balance;
		this.currency = currency;
		this.active = active;
	}
	
	public Account(Long customerId, Long accountNo, BigDecimal balance, String currency, Boolean active) {
		this.customerId = customerId;
		this.accountNo = accountNo;
		this.balance = balance;
		this.currency = currency;
		this.active = active;
	}
	
	public Long getCustomerId() {
		return customerId;
	}

	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}

	public Long getAccountNo() {
		return accountNo;
	}

	public void setAccountNo(Long accountNo) {
		this.accountNo = accountNo;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	@Override
	public String toString() {
		return "Account [customerId=" + customerId + ", accountNo=" + accountNo + ", balance=" + balance + ", active="
				+ active + ", currency=" + currency + "]";
	}
}
