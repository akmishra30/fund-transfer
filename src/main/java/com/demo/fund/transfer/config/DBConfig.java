package com.demo.fund.transfer.config;

import com.demo.fund.transfer.util.PropertyReader;
import com.zaxxer.hikari.HikariConfig;

public class DBConfig {
	
	public static final String H2_DB_TYPE = "H2";
	public static final String MYSQL_DB_TYPE = "MYSQL";
	public static final String  ORACLE_DB_TYPE = "ORACLE";
	public static final String  SCHEMA_FILE_NAME = "DB-SCHEMA.sql";
	public static final String SCHEMA_RECREATE = PropertyReader.getPropertyKeyValue("db.schema.recreate", "true");
	public static final String  SCHEMA_FILE_LOCATION = PropertyReader.getPropertyKeyValue("db.schema.file.location", "src/main/resources/");
	public static final String  DEFAULT_DB = PropertyReader.getPropertyKeyValue("fund.transfer.default.db", H2_DB_TYPE);
	
	public static HikariConfig getDBConfig(String dbType) {
		switch (dbType) {
		case H2_DB_TYPE:
			return getH2Config();
		default:
			return getH2Config();
		}
	}
	
	private static HikariConfig getH2Config() {
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(PropertyReader.getPropertyKeyValue("db.jdbc.h2.url"));
		config.setDriverClassName(PropertyReader.getPropertyKeyValue("db.jdbc.h2.driver.class"));
		config.setUsername(PropertyReader.getPropertyKeyValue("db.jdbc.h2.user"));
		config.setPassword(PropertyReader.getPropertyKeyValue("db.jdbc.h2.pass"));
		config.addDataSourceProperty("cachePrepStmts", PropertyReader.getPropertyKeyValue("db.jdbc.h2.cachePrepStmts"));
		config.addDataSourceProperty("prepStmtCacheSize", PropertyReader.getPropertyKeyValue("db.jdbc.h2.prepStmtCacheSize"));
		config.addDataSourceProperty("prepStmtCacheSqlLimit", PropertyReader.getPropertyKeyValue("db.jdbc.h2.prepStmtCacheSqlLimit"));
		config.setPoolName("h2-db-pool");
		config.setMaximumPoolSize(Integer.parseInt(PropertyReader.getPropertyKeyValue("db.jdbc.h2.maxPoolConn", "10")));
		return config;
	}
}
