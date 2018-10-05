package com.bizvisionsoft.service.tools;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

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
	
	public static double calculate(String formula, Function<String, Double> func) {
		Map<String, Double> vars = new HashMap<>();
		Matcher m = Pattern.compile("(\\[[^\\]]*\\])").matcher(formula);
		while (m.find()) {
			String name = m.group().substring(1, m.group().length() - 1);
			if (!vars.containsKey(name)) {
				vars.put(name, func.apply(name));
			}
		}

		Iterator<String> iter = vars.keySet().iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			Double value = vars.get(key);
			formula = formula.replaceAll("\\[" + key + "\\]", "" + value);
		}

		ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
		ScriptEngine nashorn = scriptEngineManager.getEngineByName("nashorn");
		try {
			Number result = (Number) nashorn.eval(formula);
			return result.doubleValue();
		} catch (ScriptException e) {
			logger.error(e.getMessage(), e);
		}
		return 0;
	}

	public static String generateId() {
		return Long.toHexString(System.currentTimeMillis());
	}

}
