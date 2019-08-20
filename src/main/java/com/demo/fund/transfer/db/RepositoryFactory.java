package com.demo.fund.transfer.db;

import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.demo.fund.transfer.config.DBConfig;

public abstract class RepositoryFactory {
	
	private static final Logger logger = LoggerFactory.getLogger(RepositoryFactory.class);
	
	public abstract void doTestDataSetup();
	
	public abstract Connection getDBConnection() throws SQLException;
	
	public static RepositoryFactory getRepositoryFactory(String dbType){
		try {
			switch (dbType) {
			case DBConfig.H2_DB_TYPE:
				return new H2DBRepository();
			default:
				return new H2DBRepository();
		}
		} catch (Exception e) {
			logger.error("There was a problem while getting the DB repository factory instance.");
		}
		return null;
	}
	
}
