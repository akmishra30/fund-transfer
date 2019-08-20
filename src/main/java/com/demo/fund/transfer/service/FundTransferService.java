package com.demo.fund.transfer.service;

import com.demo.fund.transfer.entity.APISuccess;
import com.demo.fund.transfer.entity.AccountRequest;
import com.demo.fund.transfer.entity.FundTransfer;
import com.demo.fund.transfer.exception.APIException;

public interface FundTransferService {
	public APISuccess fundTransfer(FundTransfer transfer) throws APIException;

	public APISuccess createAccount(AccountRequest accountRequest) throws APIException;
}
