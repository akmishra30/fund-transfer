package com.demo.fund.transfer.persistence;

import com.demo.fund.transfer.entity.Account;
import com.demo.fund.transfer.entity.FundTransfer;
import com.demo.fund.transfer.exception.APIException;

public interface AccountRepository {
	public Account getAccount(long accountNo) throws APIException;
	
	public Boolean fundTransfer(FundTransfer transfer) throws APIException;
	
	public Account createAccount(Account newAccount) throws APIException;
}
