package com.demo.fund.transfer.db;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.h2.tools.RunScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.demo.fund.transfer.config.DBConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class H2DBRepository extends RepositoryFactory{
	
	private static final Logger logger = LoggerFactory.getLogger(H2DBRepository.class);
	
	private static HikariConfig config;
	
	private static volatile DataSource dataSource;
	
	private final Object lock = new Object();
	
	public H2DBRepository() throws SQLException {
		initializeDataSource();
	}
	
	private void initializeDataSource() throws SQLException{
		logger.info("Initializing a datasource");
		
		if(config == null)
			config = DBConfig.getDBConfig(DBConfig.H2_DB_TYPE);
		
		DataSource source = dataSource;
		
		if(source == null) {
			if(dataSource == null) {
				synchronized (lock) {
					source = dataSource;
					if(source == null) {
						source = new HikariDataSource(config);
						dataSource = source;
					}
				}
			}
		}
	}

	@Override
	public Connection getDBConnection() throws SQLException {
		return dataSource.getConnection();
	}
	
	public void doTestDataSetup() {
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;
		String schemaFile = DBConfig.SCHEMA_FILE_LOCATION + DBConfig.H2_DB_TYPE + "DB-SCHEMA.sql";
		try {
			connection = getDBConnection();
			
			RunScript.execute(connection, new FileReader(schemaFile));
			
			String GET_ALL_CUSTOMER_SQL = "select * from Customer";
			
			statement = connection.createStatement();
			rs = statement.executeQuery(GET_ALL_CUSTOMER_SQL);
			
			while (rs.next()) {
				logger.info("Test customerId: {}", rs.getString("CustomerId"));
			}
			rs.close();
			statement.close();
			
			String GET_ALL_ACCOUNT_SQL = "select * from Account";
			
			statement = connection.createStatement();
			rs = statement.executeQuery(GET_ALL_ACCOUNT_SQL);
			
			while (rs.next()) {
				logger.info("Test customerId: {}, accountId: {}", rs.getString("CustomerId"), rs.getString("AccountId"));
			}
		
			
		} catch (SQLException | FileNotFoundException e) {
			logger.error("Some problem while setting up test data. {}", e);
		}
		
		finally {
			try {
				if(connection != null) {
					if(rs != null) rs.close();
					if(statement != null) statement.close();
					connection.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
}
