package com.demo.fund.transfer.db;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.SQLException;

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
	
	private boolean DB_SCHEMA_RESET;
	private String DB_SCHEMA_FILE;
	
	public H2DBRepository(String schemaFile, boolean schemaRecreate) throws SQLException {
		this.DB_SCHEMA_FILE = schemaFile;
		this.DB_SCHEMA_RESET = schemaRecreate;
		initializeDataSource();
	}
	
	private void initializeDataSource() throws SQLException{
		
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
	
	
	public void dbSchemaSetup() {
		Connection connection = null;
		try {
			connection = getDBConnection();
			
			if(DB_SCHEMA_RESET)
				RunScript.execute(connection, new FileReader(DB_SCHEMA_FILE));
			
			logger.info("H2 DB Schema setup completed.");
		} catch (SQLException | FileNotFoundException e) {
			logger.error("Some problem while setting up test data. {}", e);
		}
		
		finally {
			try {
				if(connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
}
