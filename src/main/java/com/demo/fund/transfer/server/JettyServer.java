package com.demo.fund.transfer.server;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.demo.fund.transfer.controller.AccountController;
import com.demo.fund.transfer.controller.FundTransferController;
import com.demo.fund.transfer.exception.APIExceptionMapper;
import com.demo.fund.transfer.util.PropertyReader;

public class JettyServer {
	
	private static final Logger logger = LoggerFactory.getLogger(JettyServer.class);
	
	private Server server;
	
	public JettyServer() {
		jettyServerSetup();
	}
	
	private void jettyServerSetup() {
		logger.info("Jetty server initialization has begin.");
		QueuedThreadPool threadPool = new QueuedThreadPool(
				Integer.parseInt(PropertyReader.getPropertyKeyValue("jetty.server.maxThreads", "100")), 
				Integer.parseInt(PropertyReader.getPropertyKeyValue("jetty.server.minThreads", "10")), 
				Integer.parseInt(PropertyReader.getPropertyKeyValue("jetty.server.idleTimeout", "120")));
		
		server = new Server(threadPool);
		
		ServerConnector connector = new ServerConnector(server);
        connector.setPort(Integer.parseInt(PropertyReader.getPropertyKeyValue("jetty.server.port", "8080")));
        connector.setIdleTimeout(3000);
        server.setConnectors(new Connector[] { connector });
        
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath(PropertyReader.getPropertyKeyValue("jetty.server.uri.context", "/api"));
		server.setHandler(context);
		
		ServletHolder servletHolder = context.addServlet(ServletContainer.class, "/*");
		servletHolder.setInitParameter(
				"jersey.config.server.provider.classnames",
				FundTransferController.class.getCanonicalName() + "," + 
				AccountController.class.getCanonicalName() + "," + 
				APIExceptionMapper.class.getCanonicalName());
		logger.info("Jetty server initialization has been completed successfuly.");
	}
	
	public void startServer() {
		try {
			server.start();
			server.join();
		} catch (Exception e) {
			logger.error("There was a problem while starting the jetty server. {}", e);
		}
		
		finally {
			if (server != null) {
				logger.info("Destroying Jetty server...");
				server.destroy();
			}
		}
	}
}
