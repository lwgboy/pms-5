package com.bizvisionsoft.service.tools;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Generator {
	
	public static Logger logger = LoggerFactory.getLogger(Generator.class);

	private static HashMap<String, Integer> nameNumber = new HashMap<String, Integer>();

	public static String generateName(String text, String key) {
		Integer number = nameNumber.get(key);
		if (number == null) {
			number = 1;
		}
		nameNumber.put(key, number + 1);
		return text + number;
	}

	public static String generateName(String text) {
		return generateName(text, text);
	}
	
	public static String generateId() {
		return Long.toHexString(System.currentTimeMillis());
	}

}
