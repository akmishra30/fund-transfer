package com.fund.transfer.util;

import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.demo.fund.transfer.entity.ErrorDetail;
import com.demo.fund.transfer.util.APIConstants;
import com.demo.fund.transfer.util.APIUtil;


@RunWith(PowerMockRunner.class)
@PrepareForTest(APIUtil.class)
public class APIUtilTest {

	private static final Logger logger = LoggerFactory.getLogger(APIUtilTest.class);
	
	private APIUtil apiUtil;
	
	private static final String METHOD_VALIDATE_PAYLOAD = "validatePayload";
	private static final String METHOD_VALIDATE_ACCOUNT_NO = "validateAccountNo";
	private static final String METHOD_VALIDATE_AMOUNT = "validateAmount";
	
	@Before
	public void setUp() {
		apiUtil = new APIUtil();
		String transactionId = UUID.randomUUID().toString();
		MDC.put(APIConstants.KEY_TRAN_ID, transactionId);
	}
	
	@After
	public void flush() {
		MDC.clear();
		apiUtil = null;
	}
	/*
	@Test
	public void testValidatePayload() {
		FundTransfer transfer = new FundTransfer("10000000", "10000001", "10000");
		try {
			logger.info("Unit testing for validatePayload with valid data.");
			apiUtil.validatePayload(transfer);
			Assert.assertTrue(true);
		} catch (APIException e) {
			logger.error("Failed in validation");
		}
		
		try {
			logger.info("Unit testing for validatePayload with invalid format from account no.");
			transfer = new FundTransfer("100-000-000", "10000001", "10000");
			apiUtil.validatePayload(transfer);
		} catch (APIException e) {
			logger.info("Failed in validation");
			Assert.assertEquals(APIConstants.INVALID_DATA_FORMAT, e.getCode());
		}
		
		try {
			logger.info("Unit testing for validatePayload with invalid format to account no.");
			transfer = new FundTransfer("10000000", "CASA978978987", "10000");
			apiUtil.validatePayload(transfer);
		} catch (APIException e) {
			logger.info("Failed in validation");
			Assert.assertEquals(APIConstants.INVALID_DATA_FORMAT, e.getCode());
		}
		
		try {
			logger.info("Unit testing for validatePayload with invalid amount data.");
			transfer = new FundTransfer("10000000", "10000001", "$10000");
			apiUtil.validatePayload(transfer);
		} catch (APIException e) {
			logger.info("Failed in validation");
			Assert.assertEquals(APIConstants.INVALID_DATA_FORMAT, e.getCode());
		}
		
		try {
			logger.info("Unit testing for validatePayload with 16 digit from account and 8 digit to account no.");
			transfer = new FundTransfer("1234567890123456", "12345678", "10,000");
			apiUtil.validatePayload(transfer);
			Assert.assertTrue(true);
		} catch (APIException e) {
			logger.info("Failed in validation");
		}
		
		try {
			logger.info("Unit testing for validatePayload with with thousand seperator and decimal point");
			transfer = new FundTransfer("123456789012", "1234567800", "10,000.000");
			apiUtil.validatePayload(transfer);
			Assert.assertTrue(true);
		} catch (APIException e) {
			logger.info("Failed in validation");
		}
		
		try {
			logger.info("Unit testing for validatePayload with fromaccount more than 16 digit and to account less than 8 digits");
			transfer = new FundTransfer("123456789012345699", "123456", "10,000.0000");
			apiUtil.validatePayload(transfer);
		} catch (APIException e) {
			logger.info("Failed in validation");
			Assert.assertEquals(APIConstants.INVALID_DATA_FORMAT, e.getCode());
		}
		
	}
	*/
	@Test
	public void testValidateAccountNo() throws Exception{
		logger.info("Begin unit test case for account no. validation. Method name: {}", METHOD_VALIDATE_ACCOUNT_NO);
		logger.info("Starting unit test for validateAccountNo with valid data - with 10 digits");
		//with valid data - 10 string length
		ErrorDetail error = Whitebox.invokeMethod(apiUtil, METHOD_VALIDATE_ACCOUNT_NO, APIConstants.FIELD_FROM_ACCOUNT, "3453453454");
		Assert.assertNull(error);
		logger.info("Starting unit test for validateAccountNo with valid data - with 8 digits");
		error = Whitebox.invokeMethod(apiUtil, METHOD_VALIDATE_ACCOUNT_NO, APIConstants.FIELD_FROM_ACCOUNT, "34534534");
		Assert.assertNull(error);
		logger.info("Starting unit test for validateAccountNo with valid data - with 16 digits");
		error = Whitebox.invokeMethod(apiUtil, METHOD_VALIDATE_ACCOUNT_NO, APIConstants.FIELD_FROM_ACCOUNT, "3453453454123456");
		Assert.assertNull(error);
		
		logger.info("Starting unit test with empty data");
		//with empty account no.
		error = Whitebox.invokeMethod(apiUtil, METHOD_VALIDATE_ACCOUNT_NO, APIConstants.FIELD_FROM_ACCOUNT, "");
		Assert.assertNotNull(error);
		Assert.assertEquals(APIConstants.FIELD_FROM_ACCOUNT, error.getName());
		Assert.assertEquals(APIConstants.EMPTY_ACCOUNT_NO_MSG, error.getDesc());
		
		logger.info("Starting unit test with invalid data - less than 8 length");
		//with invalid account no.
		error = Whitebox.invokeMethod(apiUtil, METHOD_VALIDATE_ACCOUNT_NO, APIConstants.FIELD_FROM_ACCOUNT, "235443");
		Assert.assertNotNull(error);
		Assert.assertEquals(APIConstants.FIELD_FROM_ACCOUNT, error.getName());
		Assert.assertEquals(APIConstants.INVALID_ACCOUNT_NO_FORMAT_MSG, error.getDesc());
		
		logger.info("Starting unit test with invalid data - more than 16 length");
		error = Whitebox.invokeMethod(apiUtil, METHOD_VALIDATE_ACCOUNT_NO, APIConstants.FIELD_FROM_ACCOUNT, "24234234234245679");
		Assert.assertNotNull(error);
		Assert.assertEquals(APIConstants.FIELD_FROM_ACCOUNT, error.getName());
		Assert.assertEquals(APIConstants.INVALID_ACCOUNT_NO_FORMAT_MSG, error.getDesc());
		
		logger.info("Starting unit test with invalid data - negative no");
		error = Whitebox.invokeMethod(apiUtil, METHOD_VALIDATE_ACCOUNT_NO, APIConstants.FIELD_FROM_ACCOUNT, "-24234234234");
		Assert.assertNotNull(error);
		Assert.assertEquals(APIConstants.FIELD_FROM_ACCOUNT, error.getName());
		Assert.assertEquals(APIConstants.INVALID_ACCOUNT_NO_FORMAT_MSG, error.getDesc());
		
		logger.info("Starting unit test with invalid data - with thousand seperator");
		error = Whitebox.invokeMethod(apiUtil, METHOD_VALIDATE_ACCOUNT_NO, APIConstants.FIELD_FROM_ACCOUNT, "12,345,566,789");
		Assert.assertNotNull(error);
		Assert.assertEquals(APIConstants.FIELD_FROM_ACCOUNT, error.getName());
		Assert.assertEquals(APIConstants.INVALID_ACCOUNT_NO_FORMAT_MSG, error.getDesc());
		
		logger.info("Starting unit test with invalid data - with decimal point");
		error = Whitebox.invokeMethod(apiUtil, METHOD_VALIDATE_ACCOUNT_NO, APIConstants.FIELD_FROM_ACCOUNT, "12345.78");
		Assert.assertNotNull(error);
		Assert.assertEquals(APIConstants.FIELD_FROM_ACCOUNT, error.getName());
		Assert.assertEquals(APIConstants.INVALID_ACCOUNT_NO_FORMAT_MSG, error.getDesc());
		
		logger.info("Starting unit test with invalid data - with character string");
		error = Whitebox.invokeMethod(apiUtil, METHOD_VALIDATE_ACCOUNT_NO, APIConstants.FIELD_FROM_ACCOUNT, "ABCDE1234");
		Assert.assertNotNull(error);
		Assert.assertEquals(APIConstants.FIELD_FROM_ACCOUNT, error.getName());
		Assert.assertEquals(APIConstants.INVALID_ACCOUNT_NO_FORMAT_MSG, error.getDesc());
		
		logger.info("All the possible test cases executed successfully. Method name: {}", METHOD_VALIDATE_ACCOUNT_NO);
	}
	
	@Test
	public void testValidateAmount() throws Exception{
		logger.info("Begin unit test case for account no. validation. Method name: {}", METHOD_VALIDATE_AMOUNT);
		
		logger.info("Starting unit test for validateAmount with valid data - with 10 digits");
		ErrorDetail error = Whitebox.invokeMethod(apiUtil, METHOD_VALIDATE_AMOUNT, APIConstants.FIELD_AMOUNT, "3453453454");
		Assert.assertNull(error);
		
		logger.info("Starting unit test for validateAmount with valid data - with 8 digits");
		error = Whitebox.invokeMethod(apiUtil, METHOD_VALIDATE_AMOUNT, APIConstants.FIELD_AMOUNT, "34534534");
		Assert.assertNull(error);
		logger.info("Starting unit test for validateAmount with valid data - with 16 digits");
		error = Whitebox.invokeMethod(apiUtil, METHOD_VALIDATE_AMOUNT, APIConstants.FIELD_AMOUNT, "3453453454123456");
		Assert.assertNull(error);
		
		logger.info("Starting unit test with empty data");
		//with empty account no.
		error = Whitebox.invokeMethod(apiUtil, METHOD_VALIDATE_AMOUNT, APIConstants.FIELD_AMOUNT, "");
		Assert.assertNotNull(error);
		Assert.assertEquals(APIConstants.FIELD_AMOUNT, error.getName());
		Assert.assertEquals(APIConstants.EMPTY_AMOUNT_MSG, error.getDesc());
		
		logger.info("Starting unit test with invalid data - negative no");
		error = Whitebox.invokeMethod(apiUtil, METHOD_VALIDATE_AMOUNT, APIConstants.FIELD_AMOUNT, "-24234234234");
		Assert.assertNotNull(error);
		Assert.assertEquals(APIConstants.FIELD_AMOUNT, error.getName());
		Assert.assertEquals(APIConstants.INVALID_AMOUNT_FORMAT_MSG, error.getDesc());
		
		logger.info("Starting unit test with valid data - with thousand seperator");
		error = Whitebox.invokeMethod(apiUtil, METHOD_VALIDATE_AMOUNT, APIConstants.FIELD_AMOUNT, "12,345,566,789");
		Assert.assertNull(error);
	
		logger.info("Starting unit test with invalid data - with decimal point");
		error = Whitebox.invokeMethod(apiUtil, METHOD_VALIDATE_AMOUNT, APIConstants.FIELD_AMOUNT, "12345.78");
		Assert.assertNull(error);
		
		logger.info("Starting unit test with invalid data - with character string");
		error = Whitebox.invokeMethod(apiUtil, METHOD_VALIDATE_AMOUNT, APIConstants.FIELD_AMOUNT, "ABCDE1234");
		Assert.assertNotNull(error);
		Assert.assertEquals(APIConstants.FIELD_AMOUNT, error.getName());
		Assert.assertEquals(APIConstants.INVALID_AMOUNT_FORMAT_MSG, error.getDesc());
		
		logger.info("All the possible test cases executed successfully. Method name: {}", METHOD_VALIDATE_AMOUNT);
	}
}
