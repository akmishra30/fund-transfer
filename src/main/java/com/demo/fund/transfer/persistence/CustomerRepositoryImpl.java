package com.demo.fund.transfer.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.demo.fund.transfer.db.RepositoryFactory;
import com.demo.fund.transfer.entity.Customer;
import com.demo.fund.transfer.exception.APIException;
import com.demo.fund.transfer.util.APIConstants;
import com.demo.fund.transfer.util.APIUtil;

public class CustomerRepositoryImpl implements CustomerRepository {

	private static final Logger logger = LoggerFactory.getLogger(CustomerRepositoryImpl.class);
	
	private RepositoryFactory repositoryFactory;
	
	private static final String SELECT_CUST_SQL = "select * from Customer where CustomerId = ?";
	private static final String INSERT_CUST_SQL = "INSERT INTO Customer (CustomerName, Email) VALUES (?, ?);";
	
	
	public CustomerRepositoryImpl(RepositoryFactory repositoryFactory) {
		this.repositoryFactory = repositoryFactory;
	}
	
	@Override
	public Customer getCustomer(Long customerId) throws APIException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		Customer customer = null;
		try {
			conn = repositoryFactory.getDBConnection();
			
			stmt = conn.prepareStatement(SELECT_CUST_SQL);
			stmt.setLong(1, customerId);
			
			resultSet = stmt.executeQuery();
			
			while (resultSet.next()) {
				customer = new Customer(resultSet.getLong("CustomerId"), 
						resultSet.getString("CustomerName"), 
						resultSet.getString("Email"));
			}
			
			logger.info("Retrieved customer information: {}", customer);
			
		} catch (Exception e) {
			logger.error("There is a problem while fetching information from DB. {}", e);
			throw new APIException(APIConstants.API_INTERNAL_ERROR, "There was an internal API problem.");
		}
		
		finally {
			APIUtil.closeDbObjects(conn, stmt, resultSet);
		}
		
		return customer;
	}
	
	@Override
	public Customer saveCustomer(Customer customer) throws APIException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		
		try {
			conn = repositoryFactory.getDBConnection();
			
			stmt = conn.prepareStatement(INSERT_CUST_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, customer.getCustomerName());
			stmt.setString(2, customer.getEmail());
			
			int result = stmt.executeUpdate();
			if(result > 0) {
				resultSet = stmt.getGeneratedKeys();
				if(resultSet.next())
					customer.setCustomerId(resultSet.getLong(1));
			} else {
				throw new APIException(APIConstants.API_INTERNAL_ERROR, "There was a problem while saving an customer.");
			}
			
			logger.info("Created customer information: {}", customer);
			
		} catch (Exception e) {
			logger.error("There is a problem while inserting information from DB. {}", e);
			throw new APIException(APIConstants.API_INTERNAL_ERROR, "There was an internal API problem.");
		}
		
		finally {
			APIUtil.closeDbObjects(conn, stmt, resultSet);
		}
		
		return customer;
	}

}
