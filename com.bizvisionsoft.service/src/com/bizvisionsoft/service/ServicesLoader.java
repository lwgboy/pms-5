package com.bizvisionsoft.service;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.bizvisionsoft.mongocodex.tools.IValueGenerateService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.values.ProjectId;

public class ServicesLoader implements BundleActivator {

	private static BundleContext bundleContext;

	private static final List<ServiceReference<?>> references = new ArrayList<>();

	public static String url;

	@Override
	public void start(BundleContext bc) throws Exception {
		bundleContext = bc;
		url = (String) bc.getProperty("com.bizvisionsoft.service.url");
		// 注册取值服务
		registerValueGenerateService(Project.class, "id", new ProjectId());
	}

	private void registerValueGenerateService(Class<?> claz, String field, IValueGenerateService so) {
		Hashtable<String, String> properties = new Hashtable<>();
		properties.put("value.class", claz.getName());
		properties.put("value.field", field);
		bundleContext.registerService(IValueGenerateService.class, so, properties);
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

}
