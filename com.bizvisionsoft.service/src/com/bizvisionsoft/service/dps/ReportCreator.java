package com.bizvisionsoft.service.dps;

import java.io.File;
import java.io.OutputStream;
import java.util.HashMap;

public interface ReportCreator {

	public static final String OUTPUT_HTML = "html";

	public static final String OUTPUT_PDF = "pdf";

	public static final String OUTPUT_DOCX = "docx";

	public static final String OUTPUT_EXCEL = "excel";

	public static final String OUTPUT_PPTX = "pptx";

	public default void createReportFromXML(String dataURL, File templateFile, OutputStream os) throws Exception {
		HashMap<String, String> parameter = new HashMap<String, String>();
		parameter.put("FILELIST", dataURL);
		createReport(parameter, OUTPUT_PDF, templateFile, os);
	}

	public void createReport(HashMap<String, String> parameter, String outputType, File templateFile, OutputStream os)
			throws Exception;

}
