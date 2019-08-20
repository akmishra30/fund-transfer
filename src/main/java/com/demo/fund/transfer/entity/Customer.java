package com.demo.fund.transfer.entity;

public class Customer {
	
	private Long customerId;
	
	private String customerName;
	
	private String email;
	
	public Customer() {
	
	}
	
	public Customer(String customerName, String email) {
		this.customerName = customerName;
		this.email = email;
	}
	
	public Customer(Long customerId, String customerName, String email) {
		this.customerId = customerId;
		this.customerName = customerName;
		this.email = email;
	}

	public Long getCustomerId() {
		return customerId;
	}

	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
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

	@Override
	public String toString() {
		return "Customer [customerId=" + customerId + ", customerName=" + customerName + ", email=" + email + "]";
	}
	
	
}
