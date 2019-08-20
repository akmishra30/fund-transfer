package com.demo.fund.transfer.db;

import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.demo.fund.transfer.config.DBConfig;
import com.demo.fund.transfer.util.APIUtil;

public abstract class RepositoryFactory {
	
	private static final Logger logger = LoggerFactory.getLogger(RepositoryFactory.class);
	
	public abstract void dbSchemaSetup();
	
	public abstract Connection getDBConnection() throws SQLException;
	
	public static RepositoryFactory getRepositoryFactory(String dbType){
		dbType = APIUtil.stringNullCheck(dbType) ? dbType : DBConfig.H2_DB_TYPE;
		
		String dbSchema = DBConfig.SCHEMA_FILE_LOCATION + dbType + DBConfig.SCHEMA_FILE_NAME;
		boolean recreateSchema = Boolean.parseBoolean(DBConfig.SCHEMA_RECREATE.toLowerCase());
		
		try {
			switch (dbType.toUpperCase()) {
			case DBConfig.H2_DB_TYPE:
				return new H2DBRepository(dbSchema, recreateSchema);
			default:
				return new H2DBRepository(dbSchema, recreateSchema);
		}
		} catch (Exception e) {
			logger.error("There was a problem while getting the DB repository factory instance.");
		}
		return null;
	}
	
}
