package com.bizvisionsoft.service.tools;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

public class JSTools {

	public static String eval(String script, String[] paramemterNames, Object[] parameterValues) {
		SimpleBindings simpleBindings = new SimpleBindings();
		for (int i = 0; i < paramemterNames.length; i++) {
			simpleBindings.put(paramemterNames[i], parameterValues[i]);
		}
		ScriptEngine se = new ScriptEngineManager(null).getEngineByName("nashorn");
		try {
			Object value = se.eval(script, simpleBindings);
			if (value != null) {
				return value.toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Object invoke(String script, String function, Object... input) {
		ScriptEngine se = new ScriptEngineManager(null).getEngineByName("nashorn");
		try {
			Object value = se.eval(script);
			if (function != null && !function.isEmpty()) {
				value = ((Invocable) se).invokeFunction(function.trim(), input);
			}
			return value;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
