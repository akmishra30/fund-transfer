package com.demo.fund.transfer.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.demo.fund.transfer.entity.Account;
import com.demo.fund.transfer.exception.APIException;

public class APIUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(APIUtil.class);
	
	public static String getTransactionIdFromMDC() {
		String transactionID = MDC.get(APIConstants.KEY_TRAN_ID);
		return isStringPopulated(transactionID) ? transactionID : UUID.randomUUID().toString();
	}
	
	public static void putTransactionIdInMDC(String transactionId) {
		MDC.put(APIConstants.KEY_TRAN_ID, transactionId);
	}
	
	public static boolean isStringPopulated(String value) {
    return value != null && !value.isEmpty();
	}
	
	public static void closeDbObjects(Connection conn, PreparedStatement stmt, ResultSet resultSet) {
		try {
			if(resultSet != null) resultSet.close();
			if(stmt != null) stmt.closeOnCompletion();
			if(conn != null) conn.close();
			
		} catch (Exception e) {
			logger.info("There was a problem while closing the DB objects. {}", e);
		}
	}
	
	public static void validateAccount(Account account, String acctNo) throws APIException{
		if(account == null) {
			throw new APIException(APIConstants.ACCOUNT_NOT_EXIST, 
					APIConstants.ACCOUNT_NOT_EXIST_MSG + acctNo);
		} else if(account != null && !account.getActive()) {
			throw new APIException(APIConstants.ACCOUNT_NOT_ACTIVE, 
					APIConstants.ACCOUNT_NOT_ACTIVE_MSG + acctNo);
		}
	}
}
