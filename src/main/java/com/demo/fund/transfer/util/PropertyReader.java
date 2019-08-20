package com.demo.fund.transfer.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyReader {

	private static final Logger logger = LoggerFactory.getLogger(PropertyReader.class);

	private static Properties properties;
	
	static {
		String appPropFile = System.getProperty("application.properties");
		
		if (appPropFile == null) {
			appPropFile = "application.properties";
        }
		
		try {
			loadProperty(appPropFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void loadProperty(String fileName) throws Exception {
		logger.info("Loading application property file.");
		properties = new Properties();
		InputStream inputStream = null;
		try {

			inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);

			properties.load(inputStream);

		} catch (FileNotFoundException fe) {
			logger.error("File name not found {} : {}", fileName, fe);
			throw fe;
		} catch (IOException ioe) {
			logger.error("Error while reading the property file {} : {} ", fileName, ioe);
			throw ioe;
		}

		finally {
			if (inputStream != null)
				inputStream.close();
		}
	}

	public static String getPropertyKeyValue(String key) {
		
		String value = properties.getProperty(key);
		
		if(value == null)
			value = System.getProperty(key);
		
		return value;
	}
	
	public static String getPropertyKeyValue(String key, String defaultValue) {
		
		String value = properties.getProperty(key);
		
		if(value == null)
			value = System.getProperty(key);
		
		return value != null ? value : defaultValue;
	}
}
