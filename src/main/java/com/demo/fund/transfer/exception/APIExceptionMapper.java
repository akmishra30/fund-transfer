package com.demo.fund.transfer.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.demo.fund.transfer.util.APIConstants;

@Provider
public class APIExceptionMapper implements ExceptionMapper<APIException>{

	private static final Logger logger = LoggerFactory.getLogger(APIExceptionMapper.class);
	
	public APIExceptionMapper() {
		
	}
	
	@Override
	public Response toResponse(APIException exception) {
		logger.info("The exception happened while processing a request. {}", exception);
		
		Status statusCode = Response.Status.INTERNAL_SERVER_ERROR;
		
		switch (exception.getCode()) {
		
			case APIConstants.INVALID_PAYLOAD:
			case APIConstants.INVALID_DATA_FORMAT:
			case APIConstants.DEBIT_CREDIT_ACT_SAME:
				statusCode = Response.Status.BAD_REQUEST;
				break;
			case APIConstants.ACCOUNT_NOT_EXIST:
				statusCode = Response.Status.NOT_FOUND;
				break;
			case APIConstants.ACCOUNT_NOT_ACTIVE:
			case APIConstants.NOT_SUFFICIENT_FUND:
				statusCode = Response.Status.PRECONDITION_FAILED;
				break;
			case APIConstants.API_INTERNAL_ERROR:
				statusCode = Response.Status.INTERNAL_SERVER_ERROR;
				break;	
			default:
				statusCode = Response.Status.INTERNAL_SERVER_ERROR;
				break;
		}
		
		logger.error("Final API response, http response: {}, response: {}", statusCode, exception);
		return Response.status(statusCode).entity(exception).tag(MediaType.APPLICATION_JSON).build();
	}

}
