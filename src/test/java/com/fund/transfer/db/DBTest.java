package com.fund.transfer.db;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.demo.fund.transfer.config.DBConfig;
import com.demo.fund.transfer.db.H2DBRepository;
import com.demo.fund.transfer.db.RepositoryFactory;
import com.demo.fund.transfer.entity.Account;
import com.demo.fund.transfer.entity.Customer;
import com.demo.fund.transfer.exception.APIException;
import com.demo.fund.transfer.persistence.AccountRepository;
import com.demo.fund.transfer.persistence.AccountRepositoryImpl;
import com.demo.fund.transfer.persistence.CustomerRepository;
import com.demo.fund.transfer.persistence.CustomerRepositoryImpl;

public class DBTest {
	private static final Logger logger = LoggerFactory.getLogger(DBTest.class);
	
	public static void main(String[] args) {
		try {
			RepositoryFactory factory = RepositoryFactory.getRepositoryFactory(DBConfig.DEFAULT_DB);
			factory.dbSchemaSetup();
			
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
	
	private RepositoryFactory factory;
	private AccountRepository accRepo;
	private CustomerRepository custRepo;
	
	@Before
	public void setUp() {
		factory = RepositoryFactory.getRepositoryFactory(DBConfig.DEFAULT_DB);
		accRepo = new AccountRepositoryImpl(factory);
		custRepo = new CustomerRepositoryImpl(factory);
		
		factory.dbSchemaSetup();
	}
	
	@After
	public void TearDown() {
		factory = null;
		accRepo = null;
		custRepo = null;
	}
	
	@Test
	public void testGetRepositoryFactory() {
		RepositoryFactory factory = RepositoryFactory.getRepositoryFactory("");
		Assert.assertNotNull(factory);
		Assert.assertTrue(factory instanceof H2DBRepository);
		
		factory = RepositoryFactory.getRepositoryFactory(DBConfig.H2_DB_TYPE);
		Assert.assertNotNull(factory);
		Assert.assertTrue(factory instanceof H2DBRepository);
		
		factory = RepositoryFactory.getRepositoryFactory(null);
		Assert.assertNotNull(factory);
		Assert.assertTrue(factory instanceof H2DBRepository);
		
		logger.info("RepositoryFactory.getRepositoryFactory tested successfully.");
	}
	
	@Test
	public void testGetDBConnection() throws SQLException {
		Connection connection = null;
		try {
			connection = factory.getDBConnection();
			Assert.assertNotNull(connection);
		} catch (SQLException e) {
			
		}
		
		finally {
			if(connection != null)
				connection.close();
		}
		logger.info("H2DBRepository.GetDBConnection tested successfully.");
	}
	
	@Test
	public void testSaveCustomer() throws APIException {
		try {
			Customer customer = new Customer("ABCD", "test@sbc.com");
			customer = custRepo.saveCustomer(customer);
			Assert.assertNotNull(customer);
			Assert.assertTrue(customer.getCustomerId() > 0);
		} catch (APIException e) {
			
		}
		
		logger.info("CustomerRepository.SaveCustomer tested successfully.");
	}
	
	@Test
	public void testGetCustomer() throws APIException {
		try {
			Customer customer = new Customer("ABCD", "test@sbc.com");
			customer = custRepo.saveCustomer(customer);
			
			Customer newCust = custRepo.getCustomer(customer.getCustomerId());
			Assert.assertNotNull(newCust);
			Assert.assertTrue(newCust.getCustomerId() > 0);
			Assert.assertTrue(newCust.getCustomerId().compareTo(customer.getCustomerId()) == 0);
			
			newCust = custRepo.getCustomer(new Long(10000));
			Assert.assertNull(newCust);
		} catch (APIException e) {
			
		}
		
		logger.info("CustomerRepository.GetCustomer tested successfully.");
	}
	
	@Test
	public void testCreateAccount() throws APIException {
		try {
			Customer customer = new Customer("ABCD", "test@sbc.com");
			customer = custRepo.saveCustomer(customer);
			Account account = new Account(customer.getCustomerId(), new BigDecimal(1000), "USD", true);
			account = accRepo.createAccount(account);
			Assert.assertNotNull(account);
			Assert.assertTrue(account.getAccountNo() > 0);
		} catch (APIException e) {
			
		}
		
		logger.info("AccountRepository.CreateAccount tested successfully.");
	}
	
	@Test
	public void testGetAccount() throws APIException {
		try {
			Customer customer = new Customer("ABCD", "test@sbc.com");
			customer = custRepo.saveCustomer(customer);
			Account account = new Account(customer.getCustomerId(), new BigDecimal(1000), "USD", true);
			account = accRepo.createAccount(account);
			
			Account newAcct = accRepo.getAccount(account.getAccountNo());
			
			Assert.assertNotNull(newAcct);
			Assert.assertTrue(newAcct.getAccountNo().compareTo(account.getAccountNo()) == 0);
			
			newAcct = accRepo.getAccount(new Long(10000));
			Assert.assertNull(newAcct);
		} catch (APIException e) {
			
		}
		
		logger.info("AccountRepository.getAccount tested successfully.");
	}
}
