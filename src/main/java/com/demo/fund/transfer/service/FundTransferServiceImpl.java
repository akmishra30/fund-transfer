package com.demo.fund.transfer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.demo.fund.transfer.config.DBConfig;
import com.demo.fund.transfer.db.RepositoryFactory;
import com.demo.fund.transfer.entity.APISuccess;
import com.demo.fund.transfer.entity.Account;
import com.demo.fund.transfer.entity.FundTransfer;
import com.demo.fund.transfer.exception.APIException;
import com.demo.fund.transfer.persistence.AccountRepository;
import com.demo.fund.transfer.persistence.AccountRepositoryImpl;
import com.demo.fund.transfer.util.APIUtil;
import com.demo.fund.transfer.util.PropertyReader;

public class FundTransferServiceImpl implements FundTransferService{

	private static final Logger logger = LoggerFactory.getLogger(FundTransferServiceImpl.class);
	
	private static final String DB_TYPE = PropertyReader.getPropertyKeyValue("fund.transfer.default.db", DBConfig.H2_DB_TYPE);
	
	private AccountRepository  accuntRepository;
	
	private RepositoryFactory repositoryFactory;
	
	public FundTransferServiceImpl() {
		logger.info("Setting up DB Repositories");
		repositoryFactory = RepositoryFactory.getRepositoryFactory(DB_TYPE);
		setAccuntRepository();
	}
	
	public RepositoryFactory getRepositoryFactory() {
		return repositoryFactory;
	}
	
	public void setAccuntRepository() {
		this.accuntRepository = new AccountRepositoryImpl(repositoryFactory);
	}
	
	public AccountRepository getAccuntRepository() {
		return accuntRepository;
	}
	
	@Override
	public APISuccess fundTransfer(FundTransfer transfer) throws APIException {
		APISuccess apiResponse = null;
		
		logger.info("Received request for fund transfer. {}", transfer);
		
		Account fromAccount = accuntRepository.getAccount(Long.parseLong(transfer.getFromAccount()));
		
		APIUtil.validateAccount(fromAccount, transfer.getFromAccount()); //validating debit account
		
		Account toAccount = accuntRepository.getAccount(Long.parseLong(transfer.getToAccount()));
		
		APIUtil.validateAccount(toAccount, transfer.getToAccount());  //validating credit account
		
		accuntRepository.fundTransfer(transfer);
	
		apiResponse = new APISuccess("Fund transfer has been successful.");
			
		return apiResponse;
	}
	
	
}
