package com.demo.fund.transfer.validator;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.demo.fund.transfer.entity.AccountRequest;
import com.demo.fund.transfer.entity.ErrorDetail;
import com.demo.fund.transfer.exception.APIException;
import com.demo.fund.transfer.util.APIConstants;
import com.demo.fund.transfer.util.APIUtil;

public class AccountRequestValidator extends BeanValidator{

	private static final Logger logger = LoggerFactory.getLogger(AccountRequestValidator.class);
	
	private final static Pattern AMOUNT_PATTERN = Pattern.compile("^\\d+(\\.\\d+)?");
	
	private AccountRequest accountRequest;
	
	public AccountRequestValidator(Object bean) {
		accountRequest = (AccountRequest) bean;
	}
	
	@Override
	public void validateBean() throws APIException {
		APIException exception = null;
		
		List<ErrorDetail> errors = new LinkedList<ErrorDetail>();
		addErrorDetail(errors, validateAmount(APIConstants.FIELD_DEPOSIT, accountRequest.getInitialDeposit()));
		
		if(errors != null && !errors.isEmpty()) {
			exception = new APIException(APIConstants.INVALID_DATA_FORMAT, APIConstants.INVALID_DATA_FORMAT_MSG);
			exception.setErrors(errors);
			throw exception;
		}
		logger.error("Request payload failed in validation. {}", exception);
				
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
