package com.bizvisionsoft.serviceimpl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.bizvisionsoft.service.ReportService;
import com.bizvisionsoft.service.dps.ReportCreator;

public class ReportServiceImpl extends BasicServiceImpl implements ReportService {

	@Override
	public Response generateReport(InputStream template, Map<String, String> parameter, String type, String fileName) {
		ReportCreator rc = Service.get(ReportCreator.class);
		if (rc != null) {
			try {
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				rc.createReport(parameter, type, template, os);
				String contentType = "application/octet-stream";

				String downloadableFileName;
				try {
					downloadableFileName = new String(fileName.getBytes(), "ISO8859-1");
				} catch (UnsupportedEncodingException e) {
					downloadableFileName = fileName;
				}

				ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
				os.close();
				template.close();
				return Response.ok().entity(is)
						.header("Content-Disposition", "attachment; filename=" + downloadableFileName)
						.header("Content-Type", contentType).build();
			} catch (Exception e) {
				logger.error("调用DPS报表服务出错", e);
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		} else {
			logger.error("无法获得报表服务");
			return Response.status(404).build();
		}

	}

	@Override
	public Response generateReport(String templateFileName, Map<String, String> parameter, String type,
			String fileName) {
		String filePath = Service.rptDesignFolder.getPath() + "/" + templateFileName;
		try {
			FileInputStream is = new FileInputStream(filePath);
			return generateReport(is, parameter, type, fileName);
		} catch (FileNotFoundException e) {
			String msg = "没有报表模板文件" + filePath;
			logger.error(msg);
			return Response.status(404).build();
		}
	}

}
