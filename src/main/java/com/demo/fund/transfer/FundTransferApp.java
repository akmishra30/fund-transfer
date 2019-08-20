package com.demo.fund.transfer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.demo.fund.transfer.config.DBConfig;
import com.demo.fund.transfer.db.RepositoryFactory;
import com.demo.fund.transfer.server.JettyServer;

public class FundTransferApp {
	private static final Logger logger = LoggerFactory.getLogger(FundTransferApp.class);
	
	public static void main(String[] args) {
		logger.info("Starting fund-transfer REST service.");
		
		RepositoryFactory factory = RepositoryFactory.getRepositoryFactory(DBConfig.DEFAULT_DB);
		factory.doTestDataSetup();
		
		JettyServer server = new JettyServer();
		server.startServer();
	}
}
