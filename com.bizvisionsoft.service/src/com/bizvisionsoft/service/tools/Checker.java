package com.bizvisionsoft.service.tools;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

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

	public static boolean isAllAssigned(String... str) {
		if (str == null || str.length == 0)
			return false;
		for (int i = 0; i < str.length; i++) {
			if (isNotAssigned(str[i])) {
				return false;
			}
		}
		return true;
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

	public static <T> Optional<T> isAssignedThen(String s, Function<String, T> then) {
		return Optional.ofNullable(!isNotAssigned(s) && then != null ? then.apply(s) : null);
	}

	public static boolean isNotAssigned(List<?> s) {
		return s == null || s.isEmpty();
	}

	@SuppressWarnings("unchecked")
	public static <T> void instanceThen(Object obj, Class<T> clazz, Consumer<T> then) {
		if (clazz.isAssignableFrom(obj.getClass()))
			then.accept((T) obj);
	}

}
