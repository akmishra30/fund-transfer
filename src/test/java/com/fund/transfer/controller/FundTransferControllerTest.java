package com.fund.transfer.controller;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.demo.fund.transfer.config.DBConfig;
import com.demo.fund.transfer.controller.FundTransferController;
import com.demo.fund.transfer.db.RepositoryFactory;
import com.demo.fund.transfer.entity.FundTransfer;
import com.demo.fund.transfer.exception.APIExceptionMapper;
import com.demo.fund.transfer.util.APIUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FundTransferControllerTest {
	private static final Logger logger = LoggerFactory.getLogger(APIUtil.class);
	
	private Server server = null;
	
	private RepositoryFactory factory;
	
	@Before
	public void setUp() {
		try {
			factory = RepositoryFactory.getRepositoryFactory(DBConfig.DEFAULT_DB);
			factory.dbSchemaSetup();
			startServer();
			
		} catch (Exception e) {
			logger.error("There was a problem while setting up server. {}", e);
		}
	}
	
	@After
	public void tearDown() {
//		if(server != null)
//			server.destroy();
		factory = null;
	}
	
	private void startServer() throws Exception {
        if (server == null) {
            server = new Server(0); //Jetty will pick available port automatically
            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath("/api");
            server.setHandler(context);
            ServletHolder servletHolder = context.addServlet(ServletContainer.class, "/*");
            servletHolder.setInitParameter("jersey.config.server.provider.classnames",
            		FundTransferController.class.getCanonicalName() + "," +
                            APIExceptionMapper.class.getCanonicalName());
            server.start();
        }
	 }
	
	@Test
	public void testSuccessfulFundTransfer() throws IOException {
		logger.info("Running fund transfer with valid data.");
		Response response = postData(new FundTransfer("10000000", "10000001", "50.00"));
		Assert.assertNotNull(response);
		Assert.assertEquals(200, response.getCode());
		logger.info("testSuccessfulFundTransfer: fund transfer with valid data has been successful.");
	}
	
	@Test
	public void testInvalidAccountDataFundTransfer() throws IOException {
		logger.info("Running fund transfer with invalid data.");
		Response response = postData(new FundTransfer("1123-123-233", "10000001", "50.00"));
		Assert.assertNotNull(response);
		Assert.assertEquals(400, response.getCode());
		logger.info("testInvalidAccountDataFundTransfer: fund transfer with invalid data has been successful.");
	}
	
	@Test
	public void testInvalidAmountDataFundTransfer() throws IOException {
		logger.info("Running fund transfer with invalid data.");
		Response response = postData(new FundTransfer("1123-123-233", "10000001", "10,000,000"));
		Assert.assertNotNull(response);
		Assert.assertEquals(400, response.getCode());
		logger.info("testInvalidAmountDataFundTransfer: fund transfer with invalid data has been successful.");
	}
	
	private Response postData(FundTransfer payload) throws IOException {
		Response response = new Response();
		DataOutputStream outStream = null;
		BufferedReader in = null;
		HttpURLConnection conn = null;
		String httpEndpoint  = "http://localhost:" + server.getURI().getPort() + "/api/fund/transfer";
		try {
			logger.info("Sending data for fund transfer: {}", payload.toString());
			URL url = new URL(httpEndpoint);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setDoOutput(true);
			
			ObjectMapper mapper = new ObjectMapper();
			
			outStream = new DataOutputStream(conn.getOutputStream());
			outStream.writeBytes(mapper.writeValueAsString(payload));
			outStream.flush();
			outStream.close();
			
			response.setCode(conn.getResponseCode());
			in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
           
			String inputLine;
            StringBuffer responseText = new StringBuffer();
 
            while ((inputLine = in.readLine()) != null) {
            	responseText.append(inputLine);
            }
            in.close();
            response.setMessage(responseText.toString());
            
            logger.info("Recieved response : {}", response.toString());
            
		} catch (MalformedURLException e) {
            logger.warn("URL is wrong. message : {}", e.getMessage());
        } catch (IOException e) {
        	 logger.warn("Some exception : {}", e.getMessage());
        }
		
		finally {
			if(outStream != null) outStream.close();
			if(in != null) in.close();
			if(conn != null) conn.disconnect();
		}
		
		return response;
	}
}
