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
		ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
		ScriptEngine se = scriptEngineManager.getEngineByName("nashorn");
		try {
			Object value = se.eval(script, simpleBindings);
			if (value != null) {
				return value.toString();
			}
		} catch (ScriptException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Object invoke(String script, String function, Object input) {
		SimpleBindings simpleBindings = new SimpleBindings();
		simpleBindings.put("input", input);
		ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
		ScriptEngine se = scriptEngineManager.getEngineByName("nashorn");
		try {
			Object value = se.eval(script, simpleBindings);
			if (function != null && !function.isEmpty()) {
				value = ((Invocable) se).invokeFunction(function, input);
			}
			return value;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
