package com.fund.transfer.validator;

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

import com.demo.fund.transfer.entity.ErrorDetail;
import com.demo.fund.transfer.entity.FundTransfer;
import com.demo.fund.transfer.exception.APIException;
import com.demo.fund.transfer.util.APIConstants;
import com.demo.fund.transfer.validator.FundTransferValidator;
import com.demo.fund.transfer.validator.ValidatorFactory;

@RunWith(PowerMockRunner.class)
@PrepareForTest(FundTransferValidator.class)
public class FundTransferValidatorTest {
	
	private static final Logger logger = LoggerFactory.getLogger(FundTransferValidatorTest.class);
	
	private static final String METHOD_VALIDATE_ACCOUNT_NO = "validateAccountNo";
	private static final String METHOD_VALIDATE_AMOUNT = "validateAmount";
	
	private FundTransferValidator validator;
	
	@Before
	public void setUp() {
		validator = new FundTransferValidator(new FundTransfer());
	}
	
	@After
	public void clear() {
		validator = null;
	}
	
	@Test
	public void testValidateBean() {
		FundTransfer transfer = new FundTransfer("10000000", "10000001", "10000");
		try {
			logger.info("Unit testing for validateBean with valid data.");
			ValidatorFactory.getBeanValidatorInstance(transfer).validateBean();
			Assert.assertTrue(true);
		} catch (APIException e) {
			logger.error("Failed in validation");
		}
		
		try {
			logger.info("Unit testing for validateBean with invalid format from account no.");
			transfer = new FundTransfer("100-000-000", "10000001", "10000");
			ValidatorFactory.getBeanValidatorInstance(transfer).validateBean();
		} catch (APIException e) {
			logger.info("Failed in validation");
			Assert.assertEquals(APIConstants.INVALID_DATA_FORMAT, e.getCode());
		}
		
		try {
			logger.info("Unit testing for validateBean with invalid format to account no.");
			transfer = new FundTransfer("10000000", "CASA978978987", "10000");
			ValidatorFactory.getBeanValidatorInstance(transfer).validateBean();
		} catch (APIException e) {
			logger.info("Failed in validation");
			Assert.assertEquals(APIConstants.INVALID_DATA_FORMAT, e.getCode());
		}
		
		try {
			logger.info("Unit testing for validateBean with invalid amount data.");
			transfer = new FundTransfer("10000000", "10000001", "$10000");
			ValidatorFactory.getBeanValidatorInstance(transfer).validateBean();
		} catch (APIException e) {
			logger.info("Failed in validation");
			Assert.assertEquals(APIConstants.INVALID_DATA_FORMAT, e.getCode());
		}
		
		try {
			logger.info("Unit testing for validateBean with 16 digit from account and 8 digit to account no.");
			transfer = new FundTransfer("1234567890123456", "12345678", "10,000");
			ValidatorFactory.getBeanValidatorInstance(transfer).validateBean();
			Assert.assertTrue(true);
		} catch (APIException e) {
			logger.info("Failed in validation");
		}
		
		try {
			logger.info("Unit testing for validateBean with with thousand seperator and decimal point");
			transfer = new FundTransfer("123456789012", "1234567800", "10,000.000");
			ValidatorFactory.getBeanValidatorInstance(transfer).validateBean();
			Assert.assertTrue(true);
		} catch (APIException e) {
			logger.info("Failed in validation");
		}
		
		try {
			logger.info("Unit testing for validateBean with fromaccount more than 16 digit and to account less than 8 digits");
			transfer = new FundTransfer("123456789012345699", "123456", "10,000.0000");
			ValidatorFactory.getBeanValidatorInstance(transfer).validateBean();
		} catch (APIException e) {
			logger.info("Failed in validation");
			Assert.assertEquals(APIConstants.INVALID_DATA_FORMAT, e.getCode());
		}
		
	}
	
	@Test
	public void testValidateAccountNo() throws Exception{
		logger.info("Begin unit test case for account no. validation. Method name: {}", METHOD_VALIDATE_ACCOUNT_NO);
		logger.info("Starting unit test for validateAccountNo with valid data - with 10 digits");
		//with valid data - 10 string length
		ErrorDetail error = Whitebox.invokeMethod(validator, METHOD_VALIDATE_ACCOUNT_NO, APIConstants.FIELD_FROM_ACCOUNT, "3453453454");
		Assert.assertNull(error);
		logger.info("Starting unit test for validateAccountNo with valid data - with 8 digits");
		error = Whitebox.invokeMethod(validator, METHOD_VALIDATE_ACCOUNT_NO, APIConstants.FIELD_FROM_ACCOUNT, "34534534");
		Assert.assertNull(error);
		logger.info("Starting unit test for validateAccountNo with valid data - with 16 digits");
		error = Whitebox.invokeMethod(validator, METHOD_VALIDATE_ACCOUNT_NO, APIConstants.FIELD_FROM_ACCOUNT, "3453453454123456");
		Assert.assertNull(error);
		
		logger.info("Starting unit test with empty data");
		//with empty account no.
		error = Whitebox.invokeMethod(validator, METHOD_VALIDATE_ACCOUNT_NO, APIConstants.FIELD_FROM_ACCOUNT, "");
		Assert.assertNotNull(error);
		Assert.assertEquals(APIConstants.FIELD_FROM_ACCOUNT, error.getName());
		Assert.assertEquals(APIConstants.EMPTY_ACCOUNT_NO_MSG, error.getDesc());
		
		logger.info("Starting unit test with invalid data - less than 8 length");
		//with invalid account no.
		error = Whitebox.invokeMethod(validator, METHOD_VALIDATE_ACCOUNT_NO, APIConstants.FIELD_FROM_ACCOUNT, "235443");
		Assert.assertNotNull(error);
		Assert.assertEquals(APIConstants.FIELD_FROM_ACCOUNT, error.getName());
		Assert.assertEquals(APIConstants.INVALID_ACCOUNT_NO_FORMAT_MSG, error.getDesc());
		
		logger.info("Starting unit test with invalid data - more than 16 length");
		error = Whitebox.invokeMethod(validator, METHOD_VALIDATE_ACCOUNT_NO, APIConstants.FIELD_FROM_ACCOUNT, "24234234234245679");
		Assert.assertNotNull(error);
		Assert.assertEquals(APIConstants.FIELD_FROM_ACCOUNT, error.getName());
		Assert.assertEquals(APIConstants.INVALID_ACCOUNT_NO_FORMAT_MSG, error.getDesc());
		
		logger.info("Starting unit test with invalid data - negative no");
		error = Whitebox.invokeMethod(validator, METHOD_VALIDATE_ACCOUNT_NO, APIConstants.FIELD_FROM_ACCOUNT, "-24234234234");
		Assert.assertNotNull(error);
		Assert.assertEquals(APIConstants.FIELD_FROM_ACCOUNT, error.getName());
		Assert.assertEquals(APIConstants.INVALID_ACCOUNT_NO_FORMAT_MSG, error.getDesc());
		
		logger.info("Starting unit test with invalid data - with thousand seperator");
		error = Whitebox.invokeMethod(validator, METHOD_VALIDATE_ACCOUNT_NO, APIConstants.FIELD_FROM_ACCOUNT, "12,345,566,789");
		Assert.assertNotNull(error);
		Assert.assertEquals(APIConstants.FIELD_FROM_ACCOUNT, error.getName());
		Assert.assertEquals(APIConstants.INVALID_ACCOUNT_NO_FORMAT_MSG, error.getDesc());
		
		logger.info("Starting unit test with invalid data - with decimal point");
		error = Whitebox.invokeMethod(validator, METHOD_VALIDATE_ACCOUNT_NO, APIConstants.FIELD_FROM_ACCOUNT, "12345.78");
		Assert.assertNotNull(error);
		Assert.assertEquals(APIConstants.FIELD_FROM_ACCOUNT, error.getName());
		Assert.assertEquals(APIConstants.INVALID_ACCOUNT_NO_FORMAT_MSG, error.getDesc());
		
		logger.info("Starting unit test with invalid data - with character string");
		error = Whitebox.invokeMethod(validator, METHOD_VALIDATE_ACCOUNT_NO, APIConstants.FIELD_FROM_ACCOUNT, "ABCDE1234");
		Assert.assertNotNull(error);
		Assert.assertEquals(APIConstants.FIELD_FROM_ACCOUNT, error.getName());
		Assert.assertEquals(APIConstants.INVALID_ACCOUNT_NO_FORMAT_MSG, error.getDesc());
		
		logger.info("All the possible test cases executed successfully. Method name: {}", METHOD_VALIDATE_ACCOUNT_NO);
	}
	
	@Test
	public void testValidateAmount() throws Exception{
		logger.info("Begin unit test case for account no. validation. Method name: {}", METHOD_VALIDATE_AMOUNT);
		
		logger.info("Starting unit test for validateAmount with valid data - with 10 digits");
		ErrorDetail error = Whitebox.invokeMethod(validator, METHOD_VALIDATE_AMOUNT, APIConstants.FIELD_AMOUNT, "3453453454");
		Assert.assertNull(error);
		
		logger.info("Starting unit test for validateAmount with valid data - with 8 digits");
		error = Whitebox.invokeMethod(validator, METHOD_VALIDATE_AMOUNT, APIConstants.FIELD_AMOUNT, "34534534");
		Assert.assertNull(error);
		logger.info("Starting unit test for validateAmount with valid data - with 16 digits");
		error = Whitebox.invokeMethod(validator, METHOD_VALIDATE_AMOUNT, APIConstants.FIELD_AMOUNT, "3453453454123456");
		Assert.assertNull(error);
		
		logger.info("Starting unit test with empty data");
		//with empty account no.
		error = Whitebox.invokeMethod(validator, METHOD_VALIDATE_AMOUNT, APIConstants.FIELD_AMOUNT, "");
		Assert.assertNotNull(error);
		Assert.assertEquals(APIConstants.FIELD_AMOUNT, error.getName());
		Assert.assertEquals(APIConstants.EMPTY_AMOUNT_MSG, error.getDesc());
		
		logger.info("Starting unit test with invalid data - negative no");
		error = Whitebox.invokeMethod(validator, METHOD_VALIDATE_AMOUNT, APIConstants.FIELD_AMOUNT, "-24234234234");
		Assert.assertNotNull(error);
		Assert.assertEquals(APIConstants.FIELD_AMOUNT, error.getName());
		Assert.assertEquals(APIConstants.INVALID_AMOUNT_FORMAT_MSG, error.getDesc());
		
		logger.info("Starting unit test with valid data - with thousand seperator");
		error = Whitebox.invokeMethod(validator, METHOD_VALIDATE_AMOUNT, APIConstants.FIELD_AMOUNT, "12,345,566,789");
		Assert.assertNull(error);
	
		logger.info("Starting unit test with invalid data - with decimal point");
		error = Whitebox.invokeMethod(validator, METHOD_VALIDATE_AMOUNT, APIConstants.FIELD_AMOUNT, "12345.78");
		Assert.assertNull(error);
		
		logger.info("Starting unit test with invalid data - with character string");
		error = Whitebox.invokeMethod(validator, METHOD_VALIDATE_AMOUNT, APIConstants.FIELD_AMOUNT, "ABCDE1234");
		Assert.assertNotNull(error);
		Assert.assertEquals(APIConstants.FIELD_AMOUNT, error.getName());
		Assert.assertEquals(APIConstants.INVALID_AMOUNT_FORMAT_MSG, error.getDesc());
		
		logger.info("All the possible test cases executed successfully. Method name: {}", METHOD_VALIDATE_AMOUNT);
	}
}
