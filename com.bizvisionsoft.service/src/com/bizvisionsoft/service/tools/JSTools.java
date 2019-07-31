package com.bizvisionsoft.service.tools;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSTools {

	private static ScriptEngine engine;

	private static Logger logger = LoggerFactory.getLogger(JSTools.class);

	public static String eval(String script, String[] paramemterNames, Object[] parameterValues) {
		SimpleBindings simpleBindings = new SimpleBindings();
		for (int i = 0; i < paramemterNames.length; i++) {
			simpleBindings.put(paramemterNames[i], parameterValues[i]);
		}
		ScriptEngine se = getEngine();
		try {
			Object value = se.eval(script, simpleBindings);
			if (value != null) {
				return value.toString();
			}
		} catch (Exception e) {
			logger.error("JS执行出错", e);
		}
		return null;
	}

	public static Object invoke(String script, String function, Object... input) {
		ScriptEngine se = getEngine();
		try {
			Object value = se.eval(script);
			if (function != null && !function.isEmpty()) {
				value = ((Invocable) se).invokeFunction(function.trim(), input);
			}
			return value;
		} catch (Exception e) {
			logger.error("JS执行出错", e);
		}
		return null;
	}

	/**
	 * 调用src作为JavaScript, binding用于绑定传入的对象，返回脚本的执行值
	 * @param src
	 * @param binding
	 * @return
	 */
	public static Object invoke(String src, Map<String, Object> binding) {
		return invoke(src, null, null, binding);
	}

	public static Object invoke(String src, String function, String outputVarName, Map<String, Object> binding, Object... functionInput) {
		if (Check.isNotAssigned(src)) {
			logger.error("没有可执行的JS脚本");
			return null;
		}
		SimpleBindings b = new SimpleBindings();
		b.putAll(binding);

		ScriptEngine se = getEngine();
		try {
			Object value = se.eval(src, b);
			if (Check.isAssigned(function)) {
				value = ((Invocable) se).invokeFunction(function.trim(), functionInput);
			} else if (Check.isAssigned(outputVarName)) {
				value = se.get(outputVarName);
			}
			return value;
		} catch (Exception e) {
			logger.error("JS执行出错", e);
		}
		return null;
	}

	private static ScriptEngine getEngine() {
		if (engine == null)
			engine = new ScriptEngineManager(null).getEngineByName("nashorn");
		return engine;
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

		ScriptEngine nashorn = getEngine();
		try {
			Number result = (Number) nashorn.eval(formula);
			return result.doubleValue();
		} catch (ScriptException e) {
			logger.error(e.getMessage(), e);
		}
		return 0;
	}
}
