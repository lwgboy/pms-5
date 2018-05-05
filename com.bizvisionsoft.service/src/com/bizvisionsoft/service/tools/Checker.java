package com.bizvisionsoft.service.tools;

public class Checker {

	public static void checkNull(Object obj) {
		if (obj == null)
			throw new IllegalArgumentException("不允许为空值(null)");
	}

	public static void checkNullorEmpty(String obj) {
		if (obj == null || obj.isEmpty())
			throw new IllegalArgumentException("不允许为空值(null)或空字符串");
	}

}
