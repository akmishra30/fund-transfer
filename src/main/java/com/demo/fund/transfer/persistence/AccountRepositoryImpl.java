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
		Account debitAccount = null;
		Account creditAccount = null;
		PreparedStatement lockStmt1 = null;
		ResultSet rs1 = null;
		PreparedStatement lockStmt2 = null;
		ResultSet rs2 = null;
		PreparedStatement updateStmt = null;

		try {
			conn = repositoryFactory.getDBConnection();
			conn.setAutoCommit(false);

			long fromAccountId = Long.parseLong(transfer.getFromAccount());
			long toAccountId = Long.parseLong(transfer.getToAccount());
			Account tempAcct1, tempAcct2;

			if (fromAccountId < toAccountId) {
				logger.info("Locking order for transfer {}: Account {} then Account {}", transfer.getTransactionId(), fromAccountId, toAccountId);
				lockStmt1 = conn.prepareStatement(SELECT_ACCT_FOR_LOCK_SQL);
				lockStmt1.setLong(1, fromAccountId);
				rs1 = lockStmt1.executeQuery();
				if (rs1.next()) {
					tempAcct1 = new Account(rs1.getLong("CustomerId"), rs1.getLong("AccountId"), rs1.getBigDecimal("Balance"), rs1.getString("CurrencyCode"), rs1.getBoolean("Status"));
					logger.info("Account {} locked (as first account) for transfer {}", fromAccountId, transfer.getTransactionId());
				} else {
					logger.error("Account {} not found during transfer lock (expected to be first). Transfer ID: {}", fromAccountId, transfer.getTransactionId());
					throw new APIException(APIConstants.ACCOUNT_NOT_EXIST, "Account not found during transfer lock: " + fromAccountId);
				}

				lockStmt2 = conn.prepareStatement(SELECT_ACCT_FOR_LOCK_SQL);
				lockStmt2.setLong(1, toAccountId);
				rs2 = lockStmt2.executeQuery();
				if (rs2.next()) {
					tempAcct2 = new Account(rs2.getLong("CustomerId"), rs2.getLong("AccountId"), rs2.getBigDecimal("Balance"), rs2.getString("CurrencyCode"), rs2.getBoolean("Status"));
					logger.info("Account {} locked (as second account) for transfer {}", toAccountId, transfer.getTransactionId());
				} else {
					logger.error("Account {} not found during transfer lock (expected to be second). Transfer ID: {}", toAccountId, transfer.getTransactionId());
					throw new APIException(APIConstants.ACCOUNT_NOT_EXIST, "Account not found during transfer lock: " + toAccountId);
				}
				debitAccount = tempAcct1;
				creditAccount = tempAcct2;
			} else if (fromAccountId > toAccountId) {
				logger.info("Locking order for transfer {}: Account {} then Account {}", transfer.getTransactionId(), toAccountId, fromAccountId);
				lockStmt1 = conn.prepareStatement(SELECT_ACCT_FOR_LOCK_SQL);
				lockStmt1.setLong(1, toAccountId);
				rs1 = lockStmt1.executeQuery();
				if (rs1.next()) {
					tempAcct1 = new Account(rs1.getLong("CustomerId"), rs1.getLong("AccountId"), rs1.getBigDecimal("Balance"), rs1.getString("CurrencyCode"), rs1.getBoolean("Status"));
					logger.info("Account {} locked (as first account) for transfer {}", toAccountId, transfer.getTransactionId());
				} else {
					logger.error("Account {} not found during transfer lock (expected to be first). Transfer ID: {}", toAccountId, transfer.getTransactionId());
					throw new APIException(APIConstants.ACCOUNT_NOT_EXIST, "Account not found during transfer lock: " + toAccountId);
				}

				lockStmt2 = conn.prepareStatement(SELECT_ACCT_FOR_LOCK_SQL);
				lockStmt2.setLong(1, fromAccountId);
				rs2 = lockStmt2.executeQuery();
				if (rs2.next()) {
					tempAcct2 = new Account(rs2.getLong("CustomerId"), rs2.getLong("AccountId"), rs2.getBigDecimal("Balance"), rs2.getString("CurrencyCode"), rs2.getBoolean("Status"));
					logger.info("Account {} locked (as second account) for transfer {}", fromAccountId, transfer.getTransactionId());
				} else {
					logger.error("Account {} not found during transfer lock (expected to be second). Transfer ID: {}", fromAccountId, transfer.getTransactionId());
					throw new APIException(APIConstants.ACCOUNT_NOT_EXIST, "Account not found during transfer lock: " + fromAccountId);
				}
				creditAccount = tempAcct1; // tempAcct1 is toAccount (locked first)
				debitAccount = tempAcct2;  // tempAcct2 is fromAccount (locked second)
			} else {
				// Accounts are the same
				logger.error("FromAccount and ToAccount are the same for transfer {}: {}", transfer.getTransactionId(), fromAccountId);
				throw new APIException(APIConstants.DEBIT_CREDIT_ACT_SAME, APIConstants.DEBIT_CREDIT_ACT_SAME_MSG);
			}

			APIUtil.validateAccount(debitAccount, transfer.getFromAccount());
			APIUtil.validateAccount(creditAccount, transfer.getToAccount());

			//checking sufficient fund in debit account
			BigDecimal leftFund = debitAccount.getBalance().subtract(new BigDecimal(transfer.getAmount()));
			if(leftFund.compareTo(new BigDecimal(0)) < 0) {
				logger.warn("Debit account {} doesn't have sufficient fund. Current balance: {}. Transfer ID: {}", debitAccount.getAccountNo(), debitAccount.getBalance(), transfer.getTransactionId());
				throw new APIException(APIConstants.NOT_SUFFICIENT_FUND, APIConstants.NOT_SUFFICIENT_FUND_MSG);
			}
			
			logger.info("Total left fund {} in account {} for transfer {}", leftFund, debitAccount.getAccountNo(), transfer.getTransactionId());
			
			updateStmt = conn.prepareStatement(UPDATE_FUND_ACCT_SQL);
			updateStmt.setBigDecimal(1, leftFund);
			updateStmt.setLong(2, debitAccount.getAccountNo());
			updateStmt.addBatch();

			BigDecimal latestFund = creditAccount.getBalance().add(new BigDecimal(transfer.getAmount()));
			logger.info("New updated fund {} in account {} for transfer {}", latestFund, creditAccount.getAccountNo(), transfer.getTransactionId());
			
			updateStmt.setBigDecimal(1, latestFund);
			updateStmt.setLong(2, creditAccount.getAccountNo());
			updateStmt.addBatch();
			
			int updatedRows[] = updateStmt.executeBatch();
			logger.info("Balance update batch executed for transfer {}. Total rows updated: {}. Debit rows: {}, Credit rows: {}", transfer.getTransactionId(), updatedRows.length, updatedRows.length > 0 ? updatedRows[0] : 0, updatedRows.length > 1 ? updatedRows[1] : 0);
			
			conn.commit();
			logger.info("Transaction committed for transfer {}", transfer.getTransactionId());
			
		} catch (SQLException e) {
			logger.error("Fund transfer failed with internal error for transaction ID: {}", transfer.getTransactionId(), e);
			if (conn != null) {
				try {
					logger.info("Transaction is being rolled back for transfer: {}", transfer.getTransactionId());
					conn.rollback();
				} catch (SQLException e1) {
					logger.error("Error during transaction rollback for transfer: {}", transfer.getTransactionId(), e1);
				}
			}
			throw new APIException(APIConstants.API_INTERNAL_ERROR, APIConstants.API_INTERNAL_ERROR_MSG, e);
		} catch (APIException e) {
			// Log message for APIException is already contextual (e.g. "Debit account not found...", "Not sufficient fund...")
			// Adding a generic one here might be redundant if the specific exception is already logged.
			// However, if the APIException was thrown from APIUtil.validateAccount, this log would be useful.
			logger.warn("Fund transfer failed with APIException for transaction ID: {}. Message: {}", transfer.getTransactionId(), e.getMessage(), e);
			if (conn != null) {
				try {
					logger.info("Transaction is being rolled back due to APIException for transfer: {}", transfer.getTransactionId());
					conn.rollback();
				} catch (SQLException e1) {
					logger.error("Error during transaction rollback (APIException context) for transfer: {}", transfer.getTransactionId(), e1);
				}
			}
			throw e;
		} finally {
            if (rs1 != null) try { rs1.close(); } catch (SQLException e) { logger.warn("Error closing rs1 for transfer {}", transfer.getTransactionId(), e); }
            if (rs2 != null) try { rs2.close(); } catch (SQLException e) { logger.warn("Error closing rs2 for transfer {}", transfer.getTransactionId(), e); }
            if (lockStmt1 != null) try { lockStmt1.close(); } catch (SQLException e) { logger.warn("Error closing lockStmt1 for transfer {}", transfer.getTransactionId(), e); }
            if (lockStmt2 != null) try { lockStmt2.close(); } catch (SQLException e) { logger.warn("Error closing lockStmt2 for transfer {}", transfer.getTransactionId(), e); }
            if (updateStmt != null) try { updateStmt.close(); } catch (SQLException e) { logger.warn("Error closing updateStmt for transfer {}", transfer.getTransactionId(), e); }
            if (conn != null) {
                try {
                    if (!conn.getAutoCommit()) { 
                        conn.setAutoCommit(true); 
                    }
                } catch (SQLException e) {
                    logger.warn("Error resetting auto-commit for transfer {}", transfer.getTransactionId(), e);
                }
                try { conn.close(); } catch (SQLException e) { logger.warn("Error closing connection for transfer {}", transfer.getTransactionId(), e); }
            }
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
			stmt.setBoolean(4, newAccount.getActive());
			
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
