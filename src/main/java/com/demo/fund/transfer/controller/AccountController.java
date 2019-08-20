package com.demo.fund.transfer.controller;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.demo.fund.transfer.entity.APISuccess;
import com.demo.fund.transfer.entity.AccountRequest;
import com.demo.fund.transfer.exception.APIException;
import com.demo.fund.transfer.service.FundTransferService;
import com.demo.fund.transfer.service.FundTransferServiceImpl;
import com.demo.fund.transfer.util.APIUtil;
import com.demo.fund.transfer.validator.DataValidator;

@Path("/account")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AccountController {
	private static final Logger logger = LoggerFactory.getLogger(AccountController.class);
	
	private FundTransferService transferService;
	
	public AccountController() {
		setTransferService();
	}
	
	public void setTransferService() {
		this.transferService = new FundTransferServiceImpl();
	}
	
	@POST
	public Response createAccount(AccountRequest accountRequest) throws APIException{
		
		APIUtil.putTransactionIdInMDC(accountRequest.getTransactionId());
		
		logger.info("Fund transfer request has been received and initiated.");
		
		DataValidator.validateData(accountRequest);
		
		APISuccess response = transferService.createAccount(accountRequest);
		
		logger.info("Fund transfer request has been completed. response: {}", response);
		return Response.status(Response.Status.OK).entity(response).build();
	}
}
