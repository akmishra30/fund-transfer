package com.demo.fund.transfer.validator;

import com.demo.fund.transfer.exception.APIException;
import com.demo.fund.transfer.util.APIConstants;

public class DataValidator {
	
	public DataValidator(){
		
	}
	
	public static void validateData(Object data) throws APIException{
		if(data == null)
			throw new APIException(APIConstants.INVALID_PAYLOAD, APIConstants.EMPTY_PAYLOAD_MSG);
		
		BeanValidator validator = ValidatorFactory.getBeanValidatorInstance(data);
		validator.validateBean();
	}
}
