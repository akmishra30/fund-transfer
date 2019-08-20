package com.fund.transfer.db;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.demo.fund.transfer.config.DBConfig;
import com.demo.fund.transfer.db.RepositoryFactory;
import com.demo.fund.transfer.entity.Account;
import com.demo.fund.transfer.entity.Customer;
import com.demo.fund.transfer.persistence.AccountRepository;
import com.demo.fund.transfer.persistence.AccountRepositoryImpl;
import com.demo.fund.transfer.persistence.CustomerRepository;
import com.demo.fund.transfer.persistence.CustomerRepositoryImpl;

public class DBTest {
	private static final Logger logger = LoggerFactory.getLogger(DBTest.class);
	
	public static void main(String[] args) {
		try {
			RepositoryFactory factory = RepositoryFactory.getRepositoryFactory(DBConfig.DEFAULT_DB);
			factory.doTestDataSetup();
			
			AccountRepository accRepo = new AccountRepositoryImpl(factory);
			CustomerRepository custRepo = new CustomerRepositoryImpl(factory);
			
			Customer customer = new Customer("ABCD", "test@sbc.com");
			customer = custRepo.saveCustomer(customer);
			logger.info("Saved customer information: {}", customer);
			
			Account account = new Account(customer.getCustomerId(), new BigDecimal(100000), "USD", true);
			account = accRepo.createAccount(account);
			logger.info("Created account information: {}", account);
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
