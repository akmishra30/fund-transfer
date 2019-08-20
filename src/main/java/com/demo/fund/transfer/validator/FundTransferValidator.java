package com.demo.fund.transfer.validator;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.demo.fund.transfer.entity.ErrorDetail;
import com.demo.fund.transfer.entity.FundTransfer;
import com.demo.fund.transfer.exception.APIException;
import com.demo.fund.transfer.util.APIConstants;
import com.demo.fund.transfer.util.APIUtil;

public class FundTransferValidator extends BeanValidator{

	private static final Logger logger = LoggerFactory.getLogger(FundTransferValidator.class);
	// min account no length 8 and max is 16
	private final static Pattern ACCOUNT_PATTERN = Pattern.compile("^\\d{8,16}$");
	private final static Pattern AMOUNT_PATTERN = Pattern.compile("^\\d+(\\.\\d+)?");
	
	private FundTransfer fundTransfer;
	
	public FundTransferValidator(Object bean) {
		fundTransfer = (FundTransfer) bean;
	}
	
	@Override
	public void validateBean() throws APIException {
		APIException exception = null;
		if(fundTransfer.getFromAccount().contentEquals(fundTransfer.getToAccount())) {
			throw new APIException(APIConstants.DEBIT_CREDIT_ACT_SAME, 
					APIConstants.DEBIT_CREDIT_ACT_SAME_MSG);
		}
		
		List<ErrorDetail> errors = new LinkedList<ErrorDetail>();
		addErrorDetail(errors, validateAccountNo(APIConstants.FIELD_FROM_ACCOUNT, fundTransfer.getFromAccount()));
		addErrorDetail(errors, validateAccountNo(APIConstants.FIELD_TO_ACCOUNT, fundTransfer.getToAccount()));
		
		if(fundTransfer.getAmount() != null)
			fundTransfer.setAmount(fundTransfer.getAmount().replaceAll(",", ""));
		addErrorDetail(errors, validateAmount(APIConstants.FIELD_AMOUNT, fundTransfer.getAmount()));
		
		if(errors != null && !errors.isEmpty()) {
			exception = new APIException(APIConstants.INVALID_DATA_FORMAT, APIConstants.INVALID_DATA_FORMAT_MSG);
			exception.setErrors(errors);
			throw exception;
		}
		logger.error("Request payload failed in validation. {}", exception);
				
	}

	private ErrorDetail validateAccountNo(String field, String account) {
		ErrorDetail detail = null;
		
		if(!APIUtil.stringNullCheck(account)) {
			logger.warn("Empty/null data found for field {}, value: {}", field, account);
			detail = new ErrorDetail(field, APIConstants.EMPTY_ACCOUNT_NO_MSG);
		} else {
			Matcher match = ACCOUNT_PATTERN.matcher(account);
			if(!match.matches()) {
				logger.warn("Invalid data found for field {}, value: {}", field, account);
				detail = new ErrorDetail(field, APIConstants.INVALID_ACCOUNT_NO_FORMAT_MSG);
			}
		}
		
		return detail;
	}
	
	private ErrorDetail validateAmount(String field, String amount) {
		ErrorDetail detail = null;
		
		if(!APIUtil.stringNullCheck(amount)) {
			detail = new ErrorDetail(field, APIConstants.EMPTY_AMOUNT_MSG);
		} else {
			Matcher match = AMOUNT_PATTERN.matcher(amount.replaceAll(",", ""));
			if(!match.matches()) {
				logger.warn("Invalid data found for field {}, value: {}", field, amount);
				detail = new ErrorDetail(field, APIConstants.INVALID_AMOUNT_FORMAT_MSG);
			}
		}
		return detail;
	}
	
	private void addErrorDetail(List<ErrorDetail> errors, ErrorDetail detail) {
		if(detail != null)
			errors.add(detail);
	}
}
