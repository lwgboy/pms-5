package com.bizvisionsoft.service;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bizvisionsoft.service.tools.NLS;

public class ServicesLoader implements BundleActivator {

	private static BundleContext bundleContext;
	
	final static Logger logger = LoggerFactory.getLogger(ServicesLoader.class);

	private static final List<ServiceReference<?>> references = new ArrayList<>();

	public static String url;


	@Override
	public void start(BundleContext bc) throws Exception {
		bundleContext = bc;
		url = (String) bc.getProperty("com.bizvisionsoft.service.url");
		String langFolder = (String) bc.getProperty("com.bizvisionsoft.service.Lang");
		NLS.load(langFolder);
		
		configureSwagger();
	}
	
	private void configureSwagger() throws IOException {
		try {
			String filePath = bundleContext.getProperty("com.bizvisionsoft.service.Swagger");
			if (filePath != null && !filePath.trim().isEmpty()) {
				FileInputStream fis = new FileInputStream(filePath); // $NON-Activator-1$
				InputStream is = new BufferedInputStream(fis);
				Properties props = new Properties();
				props.load(is);
				Dictionary<String, Object> properties = new Hashtable<>();
				props.forEach((k,v)->properties.put("swagger."+k, v));
				
				ServiceReference<?> reference = bundleContext.getServiceReference(ConfigurationAdmin.class.getName());
				ConfigurationAdmin configAdmin = (ConfigurationAdmin) bundleContext.getService(reference);
				Configuration configuration = configAdmin.getConfiguration("com.eclipsesource.jaxrs.swagger.config", null);
				configuration.update(properties);
				bundleContext.ungetService(reference);
				is.close();
				logger.info("swagger url:"+props.getProperty("host")+props.getProperty("basePath"));
				logger.info("swagger ui:http://"+props.getProperty("host")+"/swagger-ui/index.html");
			}
		} catch (Exception e) {
			logger.error("swaggerÅäÖÃ´íÎó", e);
		}

	}


	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		references.forEach(reference -> bundleContext.ungetService(reference));
	}

	public static <T> T get(Class<T> clazz) {
		ServiceReference<T> reference = bundleContext.getServiceReference(clazz);
		if (!references.contains(reference))
			references.add(reference);
		return bundleContext.getService(reference);
	}

	public static BundleContext getBundleContext() {
		return bundleContext;
	}
	
	public static Bundle getBundle() {
		return bundleContext.getBundle();
	}

}