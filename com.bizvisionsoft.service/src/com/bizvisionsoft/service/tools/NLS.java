package com.bizvisionsoft.service.tools;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NLS {

	public static Logger logger = LoggerFactory.getLogger(NLS.class);
	private static List<NLS> nlsset;

	public static void load(String path) {
		if (path == null) {
			logger.warn("语言文件的位置不正确。请设置参数：com.bizvisionsoft.service.Lang");
			return;
		}
		File file = new File(path);
		if (!file.isDirectory()) {
			logger.warn("语言文件的位置不正确。请设置参数：com.bizvisionsoft.service.Lang");
		}
		File[] files = file.listFiles(f -> f.isFile() && f.getName().endsWith(".js"));
		nlsset = Arrays.asList(files).stream().map(NLS::createLanguage).collect(Collectors.toList());
	}

	private Document message;
	private String lang;

	public static NLS createLanguage(File f) {
		NLS nls = new NLS();
		nls.lang = f.getName().substring(0, f.getName().length() - 3);
		try {
			String text = FileTools.readFile(f.getPath(), "utf-8");
			nls.message = Document.parse(text);
		} catch (Exception e) {
			logger.error("读取语言文件失败", e);
		}
		return nls;
	}

	public static String get(String lang, String text) {
		return nlsset.stream().filter(n -> lang.equalsIgnoreCase(n.lang)).findFirst().map(nls -> nls.message)
				.map(msg -> msg.getString(text)).orElse(text);
	}

	public static NLS get(String lang) {
		return nlsset.stream().filter(n -> lang.equalsIgnoreCase(n.lang)).findFirst().orElse(new NLS());
	}

	public String msg(String text) {
		return Optional.ofNullable(message).map(m -> m.getString(text)).orElse(text);
	}

}
