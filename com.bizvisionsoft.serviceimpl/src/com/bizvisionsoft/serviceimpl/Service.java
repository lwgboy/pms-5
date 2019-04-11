package com.bizvisionsoft.serviceimpl;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bizvisionsoft.mongocodex.tools.IValueGenerateService;
import com.bizvisionsoft.mongocodex.tools.IValueGenerateServiceFactory;
import com.bizvisionsoft.serviceimpl.valuegen.DocumentValueGeneratorFactory;

public class Service implements BundleActivator {

	private static Logger logger = LoggerFactory.getLogger(Service.class);

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext )
	 */
	public void start(BundleContext bundleContext) throws Exception {
		context = bundleContext;
		// ע��ȡֵ����
		context.registerService(IValueGenerateServiceFactory.class, new DocumentValueGeneratorFactory(), null);
	}

	/**
	 * ע��ȡֵ����
	 * 
	 * @param className
	 * @param fieldName
	 * @param so
	 */
	public static void registerValueGenerateService(String className, String fieldName, IValueGenerateService so) {
		Hashtable<String, String> properties = new Hashtable<>();
		properties.put("value.class", className);
		properties.put("value.field", fieldName);
		context.registerService(IValueGenerateService.class, so, properties);
	}

	public static <T> T get(Class<T> clazz) {
		ServiceReference<T> sr = context.getServiceReference(clazz);
		if (sr == null) {
			logger.warn("�޷���ȡע�����:" + clazz);
			return null;
		}
		try {
			T service = context.getService(sr);
			logger.debug("��ȡ����:" + service);
			return service;
		} catch (Exception e) {
			logger.error("��ȡ����ʵ��ʧ��:" + clazz, e);
			return null;
		}
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
	}
	

}
