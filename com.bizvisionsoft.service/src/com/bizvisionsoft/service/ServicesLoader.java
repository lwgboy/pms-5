package com.bizvisionsoft.service;

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.bizvisionsoft.service.tools.NLS;

public class ServicesLoader implements BundleActivator {

	private static BundleContext bundleContext;

	private static final List<ServiceReference<?>> references = new ArrayList<>();

	public static String url;


	@Override
	public void start(BundleContext bc) throws Exception {
		bundleContext = bc;
		url = (String) bc.getProperty("com.bizvisionsoft.service.url");
		String langFolder = (String) bc.getProperty("com.bizvisionsoft.service.Lang");
		NLS.load(langFolder);
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