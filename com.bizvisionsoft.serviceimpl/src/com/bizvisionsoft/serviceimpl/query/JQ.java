package com.bizvisionsoft.serviceimpl.query;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.service.tools.FileTools;
import com.bizvisionsoft.serviceimpl.Service;

public class JQ {

	private static Map<String, String> query = new ConcurrentHashMap<String, String>();
	
	private static Map<String, Long> lastModify = new ConcurrentHashMap<String, Long>();

	private Map<String, Object> parameters = new HashMap<String, Object>();

	private String queryName;

	public JQ(String queryName) {
		this.queryName = queryName;
	}

	public JQ set(String key, Object value) {
		parameters.put(key, value);
		return this;
	}

	public List<Bson> array() {
		String js = readJS(queryName);
		BsonArray ba = BsonArray.parse(js);
		return inputDocumentArrayParameters(ba);
	}

	public List<Document> list() {
		String js = readJS(queryName);
		BsonArray ba = BsonArray.parse(js);
		return inputDocumentArrayParameters2(ba);
	}

	private List<Document> inputDocumentArrayParameters2(BsonArray ba) {
		List<Document> result = new ArrayList<Document>();
		for (int i = 0; i < ba.size(); i++) {
			result.add(inputDocumentParameters(ba.get(i).asDocument()));
		}
		return result;
	}

	public Document doc() {
		String js = readJS(queryName);
		BsonDocument doc = BsonDocument.parse(js);
		return inputDocumentParameters(doc);
	}

	private Document inputDocumentParameters(BsonDocument doc) {
		Document document = new Document();
		String[] keys = doc.keySet().toArray(new String[0]);
		for (int i = 0; i < keys.length; i++) {
			BsonValue bv = doc.get(keys[i]);
			Object v = inputValueParameters(bv);

			String parameter = getParameter(keys[i]);
			String k;
			if (parameter != null && parameters.containsKey(parameter)) {
				k = (String) parameters.get(parameter);
			} else {
				k = keys[i];
			}

			document.put(k, v);
		}
		return document;
	}

	private List<Bson> inputDocumentArrayParameters(BsonArray ba) {
		List<Bson> result = new ArrayList<Bson>();
		for (int i = 0; i < ba.size(); i++) {
			result.add(inputDocumentParameters(ba.get(i).asDocument()));
		}
		return result;
	}

	private List<Object> inputArrayParameters(BsonArray ba) {
		List<Object> result = new ArrayList<Object>();
		for (int i = 0; i < ba.size(); i++) {
			BsonValue elem = ba.get(i);
			result.add(inputValueParameters(elem));
		}
		return result;
	}

	private Object inputValueParameters(BsonValue value) {
		if (value.isDocument()) {
			return inputDocumentParameters(value.asDocument());
		} else if (value.isArray()) {
			return inputArrayParameters(value.asArray());
		} else if (value.isString()) {
			String parameter = getParameter(value.asString().getValue());
			if (parameter != null && parameters.containsKey(parameter)) {
				return parameters.get(parameter);
			}
		}
		return value;
	}

	private static String getParameter(String key) {
		if (key.startsWith("<") && key.endsWith(">")) {
			return key.substring(1, key.indexOf(">"));
		} else {
			return null;
		}
	}

	private String readJS(String queryName) {
		if (Check.isNotAssigned(queryName)) {
			throw new RuntimeException("查询文件名不可为空。");
		}

		String queryFileName = queryName.toLowerCase() + ".js";

		File[] files = Service.queryFolder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().equals(queryFileName);
			}
		});
		if (files == null || files.length == 0) {
			throw new RuntimeException("无法获得请求的文件。" + queryFileName);
		}

		Long lm = lastModify.get(queryName);
		if (lm == null || files[0].lastModified() != lm.longValue()) {// 没有文件或者文件已经更改
			load(files[0]);
		}
		return query.get(queryName);
	}

	public static void reloadJS() {
		File[] files = Service.queryFolder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".js");
			}
		});
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				load(files[i]);
			}
		}
	}

	private static void load(File file) {
		try {
			String text = FileTools.readFile(file.getPath(), "utf-8");
			// 去掉块注释
			text = text.replaceAll("/\\*{1,2}[\\s\\S]*?\\*/", "");
			// 去掉行注释
			text = text.replaceAll("//[\\s\\S]*?\\n", "");
			String name = file.getName();
			String key = name.substring(0, name.indexOf("."));
			query.put(key, text);
			lastModify.put(key, file.lastModified());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

}
