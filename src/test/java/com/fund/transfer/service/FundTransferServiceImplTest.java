package com.fund.transfer.service;

import java.math.BigDecimal;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.demo.fund.transfer.config.DBConfig;
import com.demo.fund.transfer.db.RepositoryFactory;
import com.demo.fund.transfer.entity.APISuccess;
import com.demo.fund.transfer.entity.Account;
import com.demo.fund.transfer.entity.Customer;
import com.demo.fund.transfer.entity.FundTransfer;
import com.demo.fund.transfer.exception.APIException;
import com.demo.fund.transfer.persistence.AccountRepository;
import com.demo.fund.transfer.persistence.AccountRepositoryImpl;
import com.demo.fund.transfer.persistence.CustomerRepository;
import com.demo.fund.transfer.persistence.CustomerRepositoryImpl;
import com.demo.fund.transfer.service.FundTransferService;
import com.demo.fund.transfer.service.FundTransferServiceImpl;
import com.demo.fund.transfer.util.APIConstants;

public class FundTransferServiceImplTest {

	private static final Logger logger = LoggerFactory.getLogger(FundTransferServiceImplTest.class);

	private FundTransferService transferService;

	private RepositoryFactory factory;

	private AccountRepository accountRepository;

	private CustomerRepository customerRepository;

	@Before
	public void setUp() {
		factory = RepositoryFactory.getRepositoryFactory(DBConfig.DEFAULT_DB);
		factory.doTestDataSetup();
		transferService = new FundTransferServiceImpl();
		accountRepository = new AccountRepositoryImpl(factory);
		customerRepository = new CustomerRepositoryImpl(factory);
	}

	@After
	public void flush() {
		factory = null;
		transferService = null;
		accountRepository = null;
		customerRepository = null;
	}

	@Test
	public void testFundTransferWithValidData() throws APIException {
		logger.info("Testing fundTransfer method with valid account and fund.");
		
		Customer customer = new Customer("AAA-ZZZZ", "aaa@test.com");
		customer = customerRepository.saveCustomer(customer);
		
		Account debitaccount = new Account(customer.getCustomerId(), new BigDecimal(100000), "USD", true);
		debitaccount = accountRepository.createAccount(debitaccount);
		
		String fromAcct = String.valueOf(debitaccount.getAccountNo());
		
		customer = new Customer("LLL-YYY", "lll@test.com");
		customer = customerRepository.saveCustomer(customer);
		
		Account creditAccount = new Account(customer.getCustomerId(), new BigDecimal(100000), "USD", true);
		creditAccount = accountRepository.createAccount(creditAccount);
		String toAcct = String.valueOf(creditAccount.getAccountNo());
		
		FundTransfer transfer = new FundTransfer(fromAcct, toAcct, "1000");
		APISuccess success = transferService.fundTransfer(transfer);
		Assert.assertNotNull(success);
		Assert.assertTrue(success.getMessage().length() > 0);
		
		logger.info("Checking the respective account go modified or not.");
		BigDecimal leftBalance = accountRepository.getAccount(debitaccount.getAccountNo()).getBalance();
		logger.info("## Left balance : {}", leftBalance);
		
		Assert.assertTrue(leftBalance.compareTo(debitaccount.getBalance().subtract(new BigDecimal(1000))) == 0);
		
		BigDecimal updatedBalance = accountRepository.getAccount(creditAccount.getAccountNo()).getBalance();
		logger.info("## updated balance : {}", updatedBalance);
		Assert.assertTrue(updatedBalance.compareTo(creditAccount.getBalance().add(new BigDecimal(1000))) == 0);
		
		logger.info("Balance transfer test case pass successfully.");
	}
	
	@Test
	public void testFundTransferWithInvalidAccount() throws APIException{
		logger.info("Testing fundTransfer method with from/to account not exists.");
		try {
			FundTransfer transfer = new FundTransfer("1000000011", "10000001111", "10000");
			transferService.fundTransfer(transfer);
		} catch (APIException e) {
			Assert.assertEquals(APIConstants.ACCOUNT_NOT_EXIST, e.getCode());
		} catch (Exception e) {
			
		}
	}
	
	@Test
	public void testFundTransferWithFromInactiveAccount() throws APIException{
		logger.info("Testing fundTransfer method with inactive from account.");
		try {
			
			Customer customer = new Customer("ZZZ-ZZZZ", "zzz@test.com");
			customer = customerRepository.saveCustomer(customer);
			
			Account account = new Account(customer.getCustomerId(), new BigDecimal(100000), "USD", false);
			account = accountRepository.createAccount(account);
			
			String fromAcct = String.valueOf(account.getAccountNo());
			
			customer = new Customer("YYY-YYY", "yyy@test.com");
			customer = customerRepository.saveCustomer(customer);
			
			account = new Account(customer.getCustomerId(), new BigDecimal(100000), "USD", true);
			account = accountRepository.createAccount(account);
			String toAcct = String.valueOf(account.getAccountNo());
			
			FundTransfer transfer = new FundTransfer(fromAcct, toAcct, "10000");
			transferService.fundTransfer(transfer);
		} catch (APIException e) {
			logger.info("## Recieved exception: {}", e.toString());
			Assert.assertEquals(APIConstants.ACCOUNT_NOT_ACTIVE, e.getCode());
		} catch (Exception e) {
			
		}
	}
	
	@Test
	public void testFundTransferWithToInactiveAccount() throws APIException{
		logger.info("Testing fundTransfer method with inactive to account.");
		try {
			
			Customer customer = new Customer("MMM-MMM", "mmm@test.com");
			customer = customerRepository.saveCustomer(customer);
			
			Account account = new Account(customer.getCustomerId(), new BigDecimal(100000), "USD", true);
			account = accountRepository.createAccount(account);
			
			String fromAcct = String.valueOf(account.getAccountNo());
			
			customer = new Customer("NNN-NNN", "nnn@test.com");
			customer = customerRepository.saveCustomer(customer);
			
			account = new Account(customer.getCustomerId(), new BigDecimal(100000), "USD", false);
			account = accountRepository.createAccount(account);
			String toAcct = String.valueOf(account.getAccountNo());
			
			FundTransfer transfer = new FundTransfer(fromAcct, toAcct, "10000");
			transferService.fundTransfer(transfer);
		} catch (APIException e) {
			logger.info("## Recieved exception: {}", e.toString());
			Assert.assertEquals(APIConstants.ACCOUNT_NOT_ACTIVE, e.getCode());
		} catch (Exception e) {
			
		}
	}
	
	@Test
	public void testFundTransferWithUnsufficientFund() throws APIException{
		logger.info("Testing fundTransfer method with not sufficient fund in account.");
		try {
			
			Customer customer = new Customer("KKK-ZZZZ", "kkk@test.com");
			customer = customerRepository.saveCustomer(customer);
			
			Account account = new Account(customer.getCustomerId(), new BigDecimal(1000), "USD", true);
			account = accountRepository.createAccount(account);
			String fromAcct = String.valueOf(account.getAccountNo());
			
			customer = new Customer("TTT-TTT", "ttt@test.com");
			customer = customerRepository.saveCustomer(customer);
			
			account = new Account(customer.getCustomerId(), new BigDecimal(5000), "USD", true);
			account = accountRepository.createAccount(account);
			String toAcct = String.valueOf(account.getAccountNo());
			
			FundTransfer transfer = new FundTransfer(fromAcct, toAcct, "1500");
			transferService.fundTransfer(transfer);
			
		} catch (APIException e) {
			logger.info("## Recieved exception: {}", e.toString());
			Assert.assertEquals(APIConstants.NOT_SUFFICIENT_FUND, e.getCode());
		} catch (Exception e) {
			
		}
	}

}
