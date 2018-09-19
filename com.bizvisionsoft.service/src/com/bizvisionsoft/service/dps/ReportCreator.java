package com.bizvisionsoft.service.dps;

import java.io.File;
import java.io.OutputStream;

public interface ReportCreator {

	void createReport(String dataURL, File templateFile, OutputStream os) throws Exception;


}
