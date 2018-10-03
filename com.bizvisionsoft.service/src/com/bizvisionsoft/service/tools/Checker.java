package com.bizvisionsoft.service.tools;

import java.util.List;
import java.util.function.Consumer;

public class Checker {

	public static boolean equals(Object v1, Object v2) {
		return v1 != null && v1.equals(v2) || v1 == null && v2 == null;
	}

	public static boolean isNotAssigned(String str) {
		return str == null || str.trim().isEmpty();
	}

	public static boolean isAssigned(String str) {
		return str != null && !str.trim().isEmpty();
	}

	public static boolean isAssigned(String s, Consumer<String> then) {
		if (!isNotAssigned(s)) {
			if (then != null)
				then.accept(s);
			return true;
		} else {
			return false;
		}
	}

	public static boolean isNotAssigned(List<?> s) {
		return s == null || s.isEmpty();
	}

	@SuppressWarnings("unchecked")
	public static <T> void ifInstance(Object obj, Class<T> clazz, Consumer<T> then) {
		if (clazz.isAssignableFrom(obj.getClass())) {
			then.accept((T) obj);
		}
	}

}
