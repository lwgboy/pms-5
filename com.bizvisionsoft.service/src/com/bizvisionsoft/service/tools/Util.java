package com.bizvisionsoft.service.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.service.model.RemoteFile;

public class Util {

	public static final String DATE_FORMAT_JS_FULL = "yyyy-MM-dd'T'HH:mm:ss.SSS Z";

	public static final String DATE_FORMAT_DATE = "yyyy-MM-dd";

	public static final String DATE_FORMAT_TIME = "HH:mm";

	public static final String DATE_FORMAT_DATETIME = "yyyy-MM-dd HH:mm:ss";

	public static Date str_date(String str) {
		if (str == null)
			return null;
		String _str = str.replace("Z", " UTC");
		SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT_JS_FULL);
		try {
			return format.parse(_str);
		} catch (ParseException e) {
			return null;
		}
	}

	public static boolean equals(Object v1, Object v2) {
		return v1 != null && v1.equals(v2) || v1 == null && v2 == null;
	}

	public static boolean isEmptyOrNull(String s) {
		return s == null || s.trim().isEmpty();
	}

	public static boolean notEmptyOrNull(String s, Consumer<String> c) {
		if (!isEmptyOrNull(s)) {
			if (c != null)
				c.accept(s);
			return true;
		} else {
			return false;
		}
	}

	public static boolean isEmptyOrNull(List<?> s) {
		return s == null || s.isEmpty();
	}

	public static int str_int(String text, String message) {
		if (isEmptyOrNull(text)) {
			return 0;
		}
		try {
			return Integer.parseInt(text.trim());
		} catch (Exception e) {
			throw new RuntimeException(message);
		}
	}

	public static double str_double(String text, String message) {
		if (isEmptyOrNull(text)) {
			return 0d;
		}
		try {
			return Double.parseDouble(text.trim());
		} catch (Exception e) {
			throw new RuntimeException(message);
		}
	}

	public static void writeFile(String content, String path, String encoding) throws IOException {
		File file = new File(path);
		file.delete();
		file.createNewFile();
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), encoding));
		writer.write(content);
		writer.close();
	}

	public static String readFile(String path, String encoding) throws IOException {
		String content = "";
		File file = new File(path);
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
		String line = null;
		while ((line = reader.readLine()) != null) {
			content += line + "\n";
		}
		reader.close();
		return content;
	}

	public static boolean deleteFolder(String sPath) {
		boolean flag = false;
		File file = new File(sPath);
		// 判断目录或文件是否存在
		if (!file.exists()) { // 不存在返回 false
			return flag;
		} else {
			// 判断是否为文件
			if (file.isFile()) { // 为文件时调用删除文件方法
				return deleteFile(sPath);
			} else { // 为目录时调用删除目录方法
				return deleteDirectory(sPath);
			}
		}
	}

	public static boolean deleteFile(String sPath) {
		boolean flag = false;
		File file = new File(sPath);
		// 路径为文件且不为空则进行删除
		if (file.isFile() && file.exists()) {
			file.delete();
			flag = true;
		}
		return flag;
	}

	public static boolean deleteDirectory(String sPath) {
		// 如果sPath不以文件分隔符结尾，自动添加文件分隔符
		if (!sPath.endsWith(File.separator)) {
			sPath = sPath + File.separator;
		}
		File dirFile = new File(sPath);
		// 如果dir对应的文件不存在，或者不是一个目录，则退出
		if (!dirFile.exists() || !dirFile.isDirectory()) {
			return false;
		}
		boolean flag = true;
		// 删除文件夹下的所有文件(包括子目录)
		File[] files = dirFile.listFiles();
		for (int i = 0; i < files.length; i++) {
			// 删除子文件
			if (files[i].isFile()) {
				flag = deleteFile(files[i].getAbsolutePath());
				if (!flag)
					break;
			} // 删除子目录
			else {
				flag = deleteDirectory(files[i].getAbsolutePath());
				if (!flag)
					break;
			}
		}
		if (!flag)
			return false;
		// 删除当前目录
		if (dirFile.delete()) {
			return true;
		} else {
			return false;
		}
	}

	public static String getFormatNumber(Object value) {
		return getFormatText(value, null, null);
	}

	public static String getFormatPercentage(Object value) {
		return getFormatText(value, "#0.0%", null);
	}

	public static String getFormatText(Object value, String format) {
		return getFormatText(value, format, null);
	}

	public static String getFormatDate(Object value) {
		return getFormatText(value, null, null);
	}

	public static String getFormatText(Object value, String format, Locale locale) {
		String text;
		if (value instanceof Date) {
			String sdf = isEmptyOrNull(format) ? DATE_FORMAT_DATE : format;
			return Optional.ofNullable(locale).map(l -> new SimpleDateFormat(sdf, l)).orElse(new SimpleDateFormat(sdf))
					.format(value);
		} else if (value instanceof Integer || value instanceof Long || value instanceof Short) {
			text = Optional.ofNullable(format)//
					.map(f -> {
						DecimalFormat df = new DecimalFormat(f);
						df.setRoundingMode(RoundingMode.HALF_UP);
						return df.format(value);
					}).orElse(value.toString());
		} else if (value instanceof Float || value instanceof Double) {
			DecimalFormat df = new DecimalFormat(Optional.ofNullable(format).orElse("0.0"));
			df.setRoundingMode(RoundingMode.HALF_UP);
			return df.format(value);
		} else if (value instanceof Boolean) {
			text = (boolean) value ? "是" : "否";
		} else if (value instanceof String) {
			text = (String) value;
		} else if (value instanceof List<?>) {
			text = "";
			for (int i = 0; i < ((List<?>) value).size(); i++) {
				if (i != 0) {
					text += ", ";
				}
				text += getFormatText(((List<?>) value).get(i), format, locale);
			}
		} else if (value instanceof RemoteFile) {
			text = ((RemoteFile) value).name;
		} else if (value instanceof Object) {
			text = Optional.ofNullable(AUtil.readLabel(value)).orElse("");
		} else {
			text = "";
		}
		return text;
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
			e.printStackTrace();
		}
		return 0;
	}
}
