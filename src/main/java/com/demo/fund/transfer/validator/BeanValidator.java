package com.demo.fund.transfer.validator;

import com.demo.fund.transfer.exception.APIException;

public abstract class BeanValidator {
	public abstract void validateBean() throws APIException;
}
