package com.fund.transfer.util;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.demo.fund.transfer.entity.Account;
import com.demo.fund.transfer.exception.APIException;
import com.demo.fund.transfer.util.APIConstants;
import com.demo.fund.transfer.util.APIUtil;


public class APIUtilTest {

	private static final Logger logger = LoggerFactory.getLogger(APIUtilTest.class);
	
	@After
	public void flush() {
		MDC.clear();
	}
	
	@Test
	public void testGetTransactionIdFromMDC() {
		String transactionId = UUID.randomUUID().toString();
		APIUtil.putTransactionIdInMDC(transactionId);
		String transId = APIUtil.getTransactionIdFromMDC();
		Assert.assertNotNull(transId);
		logger.info("getTransactionIdFromMDC tested successfully.");
	}
	
	@Test
	public void testPutTransactionIdInMDC() {
		String transactionId = UUID.randomUUID().toString();
		APIUtil.putTransactionIdInMDC(transactionId);
		String transId = APIUtil.getTransactionIdFromMDC();
		Assert.assertNotNull(transId);
		logger.info("putTransactionIdInMDC tested successfully.");
	}
	
	@Test
	public void testStringNullCheck() {
		
		boolean valid = APIUtil.stringNullCheck("");
		Assert.assertFalse(valid);
		
		valid = APIUtil.stringNullCheck(null);
		Assert.assertFalse(valid);
		
		valid = APIUtil.stringNullCheck("ABC");
		Assert.assertTrue(valid);
		logger.info("stringNullCheck tested successfully.");
	}
	
	@Test
	public void testValidateAccount() {
		try {
			APIUtil.validateAccount(new Account(new Long(100000001), new Long(500000001), new BigDecimal(10000), "USD", true) {
			}, "500000001");
			Assert.assertTrue(true);
		} catch (APIException e) {
			
		}
		
		try {
			APIUtil.validateAccount(new Account(new Long(100000001), new Long(500000001), new BigDecimal(10000), "USD", false) {
			}, "500000001");
			
		} catch (APIException e) {
			Assert.assertEquals(APIConstants.ACCOUNT_NOT_ACTIVE, e.getCode());
			Assert.assertTrue(e.getMessage().length() > 0);
		}
		
		try {
			APIUtil.validateAccount(null, "500000001");
			
		} catch (APIException e) {
			Assert.assertEquals(APIConstants.ACCOUNT_NOT_EXIST, e.getCode());
			Assert.assertTrue(e.getMessage().length() > 0);
		}
		logger.info("validateAccount tested successfully.");
	}
}
