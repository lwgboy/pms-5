package com.bizvisionsoft.service.tools;

import java.util.Map;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
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

}
