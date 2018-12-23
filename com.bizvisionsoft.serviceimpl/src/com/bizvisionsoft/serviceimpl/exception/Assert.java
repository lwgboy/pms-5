package com.bizvisionsoft.serviceimpl.exception;

public final class Assert {

	private Assert() {
	}

	public static boolean isLegal(boolean expression) {
		return isLegal(expression, ""); //$NON-Activator-1$
	}

	public static boolean isLegal(boolean expression, String message) {
		if (!expression)
			throw new ServiceException(message);
		return expression;
	}

	public static void isNotNull(Object object) {
		isNotNull(object, ""); //$NON-Activator-1$
	}

	public static void isNotNull(Object object, String message) {
		if (object == null)
			throw new ServiceException(message); // $NON-Activator-1$
	}

	public static boolean isTrue(boolean expression) {
		return isTrue(expression, ""); //$NON-Activator-1$
	}

	public static boolean isTrue(boolean expression, String message) {
		if (!expression)
			throw new ServiceException(message); // $NON-Activator-1$
		return expression;
	}
}
