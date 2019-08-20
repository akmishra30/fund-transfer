package com.demo.fund.transfer.persistence;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.demo.fund.transfer.db.RepositoryFactory;
import com.demo.fund.transfer.entity.Account;
import com.demo.fund.transfer.entity.FundTransfer;
import com.demo.fund.transfer.exception.APIException;
import com.demo.fund.transfer.util.APIConstants;
import com.demo.fund.transfer.util.APIUtil;

public class AccountRepositoryImpl implements AccountRepository {

	private static final Logger logger = LoggerFactory.getLogger(AccountRepositoryImpl.class);
	
	private RepositoryFactory repositoryFactory;
	
	private static final String SELECT_ACCT_SQL = "SELECT * FROM ACCOUNT WHERE ACCOUNTID = ?";
	private static final String SELECT_ACCT_FOR_LOCK_SQL = "SELECT * FROM ACCOUNT WHERE ACCOUNTID = ? FOR UPDATE";
	private static final String INSERT_ACCT_SQL = "INSERT INTO ACCOUNT (CUSTOMERID, BALANCE, CURRENCYCODE, STATUS) VALUES (?, ?, ?, ?);";
	private static final String UPDATE_FUND_ACCT_SQL = "UPDATE ACCOUNT SET BALANCE = ? WHERE ACCOUNTID = ?";
	
	public AccountRepositoryImpl(RepositoryFactory repositoryFactory) {
		this.repositoryFactory = repositoryFactory;
	}
	
	@Override
	public Account getAccount(long accountNo) throws APIException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		Account account = null;
		try {
			conn = repositoryFactory.getDBConnection();
			
			stmt = conn.prepareStatement(SELECT_ACCT_SQL);
			stmt.setLong(1, accountNo);
			
			resultSet = stmt.executeQuery();
			
			while (resultSet.next()) {
				account = new Account(resultSet.getLong("CustomerId"), 
						resultSet.getLong("AccountId"), 
						resultSet.getBigDecimal("Balance"), 
						resultSet.getString("CurrencyCode"), resultSet.getBoolean("Status"));
			}
			
			logger.info("Retrieved account information: {}", account);
			
		} catch (Exception e) {
			logger.error("There is a problem while fetching information from DB. {}", e);
			throw new APIException(APIConstants.API_INTERNAL_ERROR, APIConstants.API_INTERNAL_ERROR_MSG);
		}
		
		finally {
			APIUtil.closeDbObjects(conn, stmt, resultSet);
		}
		
		return account;
	}

	@Override
	public Boolean fundTransfer(FundTransfer transfer) throws APIException {
		Connection conn = null;
		PreparedStatement updateStmt = null, lockStmt = null;
		ResultSet resultSet = null;
		Account debitAcccount = null, creditAccount = null;
		try {
			
			conn = repositoryFactory.getDBConnection();
			conn.setAutoCommit(false);
			
			lockStmt = conn.prepareStatement(SELECT_ACCT_FOR_LOCK_SQL);
			lockStmt.setLong(1, Long.parseLong(transfer.getFromAccount()));
			
			resultSet = lockStmt.executeQuery();
			if(resultSet.next()) {
				logger.info("Debit Account has been lock for fund transfer update. account no. {}", transfer.getFromAccount());
				debitAcccount = new Account(resultSet.getLong("CustomerId"), 
						resultSet.getLong("AccountId"), 
						resultSet.getBigDecimal("Balance"), 
						resultSet.getString("CurrencyCode"), resultSet.getBoolean("Status"));
			}
			
			lockStmt = conn.prepareStatement(SELECT_ACCT_FOR_LOCK_SQL);
			lockStmt.setLong(1, Long.parseLong(transfer.getToAccount()));
			resultSet = lockStmt.executeQuery();
			
			if(resultSet.next()) {
				logger.info("Credit Account has been lock for fund transfer update. account no. {}", transfer.getToAccount());
				creditAccount = new Account(resultSet.getLong("CustomerId"), 
						resultSet.getLong("AccountId"), 
						resultSet.getBigDecimal("Balance"), 
						resultSet.getString("CurrencyCode"), resultSet.getBoolean("Status"));
			}
			
			//checking sufficient fund in debit account
			BigDecimal leftFund = debitAcccount.getBalance().subtract(new BigDecimal(transfer.getAmount()));
			if(leftFund.compareTo(new BigDecimal(0)) < 0) {
				logger.warn("Debit account {} doesn't have sufficient fund. {}", debitAcccount.getAccountNo(), debitAcccount.getBalance());
				throw new APIException(APIConstants.NOT_SUFFICIENT_FUND, APIConstants.NOT_SUFFICIENT_FUND_MSG);
			}
			
			logger.info("Total left fund {} in account {}", leftFund, debitAcccount.getAccountNo());
			
			updateStmt = conn.prepareStatement(UPDATE_FUND_ACCT_SQL);
			updateStmt.setBigDecimal(1, leftFund);
			updateStmt.setLong(2, debitAcccount.getAccountNo());
			updateStmt.addBatch();
			BigDecimal latestFund = creditAccount.getBalance().add(new BigDecimal(transfer.getAmount()));
			logger.info("New updated fund {} in account {}", latestFund, creditAccount.getAccountNo());
			
			updateStmt.setBigDecimal(1, latestFund);
			updateStmt.setLong(2, creditAccount.getAccountNo());
			updateStmt.addBatch();
			int updatedRows[] = updateStmt.executeBatch();
			logger.info("Total rows updated with balance: {}", updatedRows);
			
			conn.commit();
			
		} catch (SQLException e) {
			logger.error("Fund transfer failed with internal error. {}", e);
			if(conn != null) {
				try {
					conn.rollback();
				} catch (SQLException e1) {
					logger.error("There was a problem while rolling back the fund transfer transaction. {}", e);
				}
			}
			throw new APIException(APIConstants.API_INTERNAL_ERROR, APIConstants.API_INTERNAL_ERROR_MSG);
		} catch (APIException e) {
			if(conn != null) {
				try {
					conn.rollback();
				} catch (SQLException e1) {
					logger.error("There was a problem while rolling back the fund transfer transaction. {}", e);
				}
			}
			throw e;
		}
		
		finally {
			APIUtil.closeDbObjects(conn, lockStmt, resultSet);
			APIUtil.closeDbObjects(conn, updateStmt, resultSet);
		}
		
		return true;
	}

	

	@Override
	public Account createAccount(Account newAccount) throws APIException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		
		try {
			conn = repositoryFactory.getDBConnection();
			
			stmt = conn.prepareStatement(INSERT_ACCT_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, newAccount.getCustomerId());
			stmt.setBigDecimal(2, newAccount.getBalance());
			stmt.setString(3, newAccount.getCurrency());
			stmt.setString(4, String.valueOf(newAccount.getActive()));
			
			int result = stmt.executeUpdate();
			if(result > 0) {
				resultSet = stmt.getGeneratedKeys();
				if(resultSet.next())
					newAccount.setAccountNo(resultSet.getLong(1));
			} else {
				throw new APIException(APIConstants.API_INTERNAL_ERROR, "There was a problem while creating an account.");
			}
			
			logger.info("Created account information: {}", newAccount);
			
		} catch (Exception e) {
			logger.error("There is a problem while inserting information from DB. {}", e);
			throw new APIException(APIConstants.API_INTERNAL_ERROR, APIConstants.API_INTERNAL_ERROR_MSG);
		}
		
		finally {
			APIUtil.closeDbObjects(conn, stmt, resultSet);
		}
		
		return newAccount;
	}
}
