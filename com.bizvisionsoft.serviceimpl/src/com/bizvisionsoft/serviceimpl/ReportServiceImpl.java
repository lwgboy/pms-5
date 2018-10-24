package com.bizvisionsoft.serviceimpl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.ReportService;
import com.bizvisionsoft.service.dps.ReportCreator;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.serviceimpl.query.JQ;

public class ReportServiceImpl extends BasicServiceImpl implements ReportService {

	public Response generateReport(InputStream template, Document parameter, String type, String fileName) {
		ReportCreator rc = Service.get(ReportCreator.class);
		if (rc != null) {
			try {
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				Map<String, String> params = new HashMap<String, String>();
				if (parameter != null)
					parameter.entrySet().iterator()
							.forEachRemaining(e -> params.put(e.getKey(), e.getValue().toString()));

				rc.createReport(params, type, template, os);
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
				return responseBuilder().header("Content-Disposition", "attachment; filename=" + downloadableFileName)
						.header("Content-Type", contentType).entity(is).build();
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
	public Response generateReport(String rptParam, String templateName, String outputType, String fileName) {
		String filePath = Service.rptDesignFolder.getPath() + "/" + templateName;
		try {
			FileInputStream is = new FileInputStream(filePath);
			return generateReport(is, Document.parse(rptParam), outputType, fileName);
		} catch (FileNotFoundException e) {
			String msg = "没有报表模板文件" + filePath;
			logger.error(msg);
			return Response.status(404).build();
		}
	}

	@Override
	public Response commandReport(String commandParam) {
		Document command = Document.parse(commandParam);
		String outputType = command.getString("outputType");
		String template = command.getString("template");
		String fileName = command.getString("fileName");
		String input_obj_id = command.getString("input_obj_id");
		String selected_id = command.getString("selected_id");
		String page_input_id = command.getString("page_input_id");
		String root_input_id = command.getString("root_input_id");
		JQ jq = new JQ(command.getString("jq"));
		if (Check.isAssigned(input_obj_id))
			jq.set("input_obj_id", new ObjectId(input_obj_id));
		if (Check.isAssigned(selected_id))
			jq.set("selected_id", new ObjectId(selected_id));
		if (Check.isAssigned(page_input_id))
			jq.set("page_input_id", new ObjectId(page_input_id));
		if (Check.isAssigned(root_input_id))
			jq.set("root_input_id", new ObjectId(root_input_id));

		String filePath = Service.rptDesignFolder.getPath() + "/" + template;
		try {
			FileInputStream is = new FileInputStream(filePath);
			return generateReport(is, jq.doc(), outputType, fileName);
		} catch (FileNotFoundException e) {
			String msg = "没有报表模板文件" + filePath;
			logger.error(msg);
			return Response.status(404).build();
		}
	}

	@Override
	public Response generateReport() {
		return responseBuilder().build();
	}

	@Override
	public Response commandReport() {
		return responseBuilder().build();
	}

	private ResponseBuilder responseBuilder() {
		return Response.ok().header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Credentials", "true")
				.header("Access-Control-Allow-Methods", "POST, GET, PUT, UPDATE, OPTIONS")
				.header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With");
	}

}
