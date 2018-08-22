package com.bizvisionsoft.serviceimpl.query;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.bizvisionsoft.service.tools.Util;
import com.bizvisionsoft.serviceimpl.Service;

public class JQ {

	private static Map<String, String> query = new ConcurrentHashMap<String, String>();

	public static boolean forceReloadJSQuery;

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
			String key = Optional.ofNullable(getParameter(keys[i])).orElse(keys[i]);
			document.put(key, v);
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
		if (queryName == null || queryName.isEmpty()) {
			throw new RuntimeException("查询文件名不可为空。");
		}
		String js = query.get(queryName);
		if (forceReloadJSQuery || js == null) {
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
			load(files[0]);
		}
		js = query.get(queryName);
		return js;
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
		FileInputStream fis = null;
		BufferedInputStream is = null;
		try {
			fis = new FileInputStream(file);
			is = new BufferedInputStream(fis);
			String text = Util.getText(is);
			String name = file.getName();
			query.put(name.substring(0, name.indexOf(".")), text);
		} catch (IOException e) {
		} finally {
			if (fis != null)
				try {
					fis.close();
				} catch (IOException e) {
				}
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
				}

		}
	}

}
