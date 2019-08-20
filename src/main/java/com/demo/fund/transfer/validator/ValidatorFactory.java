package com.demo.fund.transfer.validator;

import com.demo.fund.transfer.entity.FundTransfer;

public class ValidatorFactory {
	private ValidatorFactory() {
		
	}
	
	public static BeanValidator getBeanValidatorInstance(Object bean) {
		BeanValidator validator = null;
		
		if(bean.getClass().equals(FundTransfer.class))
			validator = new FundTransferValidator(bean);
		
		return validator;
	}
}
