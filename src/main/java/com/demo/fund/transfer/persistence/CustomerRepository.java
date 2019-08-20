package com.demo.fund.transfer.persistence;

import com.demo.fund.transfer.entity.Customer;
import com.demo.fund.transfer.exception.APIException;

public interface CustomerRepository {
	public Customer getCustomer(Long customerId) throws APIException ;

	Customer saveCustomer(Customer customer) throws APIException ;
}
