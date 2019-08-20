package com.demo.fund.transfer.util;

public interface APIConstants {
	public static final String KEY_TRAN_ID = "TRANSACTION_ID";
	
	//API Response/Error Code
	public static final String INVALID_PAYLOAD = "API400";
	public static final String INVALID_DATA_FORMAT = "API401";
	public static final String DEBIT_CREDIT_ACT_SAME = "API402";
	public static final String ACCOUNT_NOT_EXIST = "API100";
	public static final String ACCOUNT_NOT_ACTIVE = "API200";
	public static final String NOT_SUFFICIENT_FUND = "API300";
	public static final String API_INTERNAL_ERROR = "API500";
	
	
	//API Fields
	public static final String FIELD_FROM_ACCOUNT = "fromAccount";
	public static final String FIELD_TO_ACCOUNT = "toAccount";
	public static final String FIELD_AMOUNT = "amount";
	public static final String FIELD_DEPOSIT = "initialDeposit";
	public static final String FIELD_STATUS = "status";
	
	//API Validation Constants
	public static final String ACCOUNT_NOT_EXIST_MSG = "The given account not exist. Account no. ";
	public static final String DEBIT_CREDIT_ACT_SAME_MSG = "The debit and credit accounts are same.";
	public static final String ACCOUNT_NOT_ACTIVE_MSG = "The given account is not active. Account no. ";
	public static final String NOT_SUFFICIENT_FUND_MSG = "The debit account doesn't have sufficient fund.";
	public static final String EMPTY_PAYLOAD_MSG = "Payload is empty or null.";
	public static final String API_INTERNAL_ERROR_MSG = "There was an internal API problem.";
	public static final String INVALID_DATA_FORMAT_MSG = "Payload contains data in invalid format.";
	public static final String EMPTY_ACCOUNT_NO_MSG = "Field is empty or not present which is mandetory.";
	public static final String INVALID_ACCOUNT_NO_FORMAT_MSG = "Invalid account no. Valid account must contains digits only of min 8 and max 16 in length.";
	
	public static final String EMPTY_AMOUNT_MSG = "Field is empty or not present which is mandetory.";
	public static final String INVALID_AMOUNT_FORMAT_MSG = "Invalid amount data. It must contains digits along with decimal point.";
}
