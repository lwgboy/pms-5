package com.bizvisionsoft.service;

import java.util.Collection;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

public interface LoggerLoader {

	public static LogService getLogService(BundleContext bc) {
		try {
			Collection<ServiceReference<LogService>> srs;
			srs = bc.getServiceReferences(LogService.class, "(name=bizvision)");
			if (srs != null && srs.size() != 0) {
				ServiceReference<LogService> sr = srs.iterator().next();
				return bc.getService(sr);
			}
		} catch (Exception e) {
		}
		return null;
	}

	public static void log(BundleContext bc, int level, String message) {
		getLogService(bc).log(level, message);
	}

	public static void log(BundleContext bc, int level, String message, Throwable exception) {
		getLogService(bc).log(level, message, exception);
	}

}
