package com.bizvisionsoft.service;

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class ServicesLoader implements BundleActivator {

	private static BundleContext bundleContext;

	private static final List<ServiceReference<?>> references = new ArrayList<>();

	public static String url;

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		ServicesLoader.bundleContext = bundleContext;
		url = (String) bundleContext.getProperty("com.bizvisionsoft.service.url");

	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		references.forEach(reference->bundleContext.ungetService(reference));
	}

	public static <T> T get(Class<T> clazz) {
		ServiceReference<T> reference = bundleContext.getServiceReference(clazz);
		if (!references.contains(reference))
			references.add(reference);
		return bundleContext.getService(reference);
	}

}
