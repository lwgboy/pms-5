package com.bizvisionsoft.service.dps;

import java.io.File;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public interface TOPSReportCreator {

	public default void createReportFromXML(String dataURL, File templateFile, OutputStream os, String serverPath,
			String fileName) throws Exception {
		HashMap<String, String> parameter = new HashMap<String, String>();
		parameter.put("FILELIST", dataURL);
		createReport(parameter, templateFile, os, serverPath, fileName);
	}

	public void createReport(Map<String, String> parameter, Object templateFile, OutputStream os, String serverPath,
			String fileName) throws Exception;

}
