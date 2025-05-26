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
import com.demo.fund.transfer.util.APIConstants; // Ensure this is present
import com.demo.fund.transfer.util.APIUtil;


public class APIUtilTest {

	private static final Logger logger = LoggerFactory.getLogger(APIUtilTest.class);
	
	@After
	public void flush() {
		MDC.clear(); // Good practice to clear MDC after each test
	}
	
	@Test
	public void testIsStringPopulated() {
		boolean valid = APIUtil.isStringPopulated("");
		Assert.assertFalse(valid); // "" is not populated

		valid = APIUtil.isStringPopulated(null);
		Assert.assertFalse(valid); // null is not populated

		valid = APIUtil.isStringPopulated(" "); // Contains a space
		Assert.assertTrue(valid); // " " is populated (not empty)

		valid = APIUtil.isStringPopulated("ABC");
		Assert.assertTrue(valid); // "ABC" is populated
		
		logger.info("isStringPopulated tested successfully.");
	}

	@Test
    public void testGetTransactionIdFromMDC_whenMdcHasId() {
        String expectedTransactionId = UUID.randomUUID().toString();
        MDC.put(APIConstants.KEY_TRAN_ID, expectedTransactionId);
        String actualTransactionId = APIUtil.getTransactionIdFromMDC();
        Assert.assertEquals(expectedTransactionId, actualTransactionId);
        logger.info("getTransactionIdFromMDC with existing ID tested successfully.");
    }

    @Test
    public void testGetTransactionIdFromMDC_whenMdcIsEmptyOrNoId() {
        MDC.remove(APIConstants.KEY_TRAN_ID); // Ensure it's not set
        String generatedTransactionId = APIUtil.getTransactionIdFromMDC();
        Assert.assertNotNull(generatedTransactionId);
        try {
            UUID.fromString(generatedTransactionId); // Validate it's a UUID
            Assert.assertTrue(true); // If parsing succeeds
        } catch (IllegalArgumentException e) {
            Assert.fail("Generated transaction ID is not a valid UUID: " + generatedTransactionId);
        }
        logger.info("getTransactionIdFromMDC with no existing ID (generation) tested successfully.");
    }
	
	@Test
	public void testValidateAccount() {
		try {
			// Using an anonymous inner class for Account to satisfy the constructor if it's abstract or for testing purposes
			APIUtil.validateAccount(new Account(100000001L, 500000001L, new BigDecimal(10000), "USD", true) {
			}, "500000001");
			Assert.assertTrue(true); // Expect no exception
		} catch (APIException e) {
			Assert.fail("Should not throw APIException for valid active account: " + e.getMessage());
		}
		
		try {
			APIUtil.validateAccount(new Account(100000001L, 500000001L, new BigDecimal(10000), "USD", false) {
			}, "500000001");
			Assert.fail("Should have thrown APIException for inactive account.");
		} catch (APIException e) {
			Assert.assertEquals(APIConstants.ACCOUNT_NOT_ACTIVE, e.getCode());
			Assert.assertTrue(e.getMessage().contains(APIConstants.ACCOUNT_NOT_ACTIVE_MSG));
		}
		
		try {
			APIUtil.validateAccount(null, "500000001");
			Assert.fail("Should have thrown APIException for null account.");
		} catch (APIException e) {
			Assert.assertEquals(APIConstants.ACCOUNT_NOT_EXIST, e.getCode());
			Assert.assertTrue(e.getMessage().contains(APIConstants.ACCOUNT_NOT_EXIST_MSG));
		}
		logger.info("validateAccount tested successfully.");
	}
}
