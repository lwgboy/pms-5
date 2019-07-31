package com.bizvisionsoft.serviceimpl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.ExportCommand;
import com.bizvisionsoft.service.ReportService;
import com.bizvisionsoft.service.common.Domain;
import com.bizvisionsoft.service.common.JQ;
import com.bizvisionsoft.service.common.Service;
import com.bizvisionsoft.service.dps.ReportCreator;
import com.bizvisionsoft.service.exporter.Form2DocxExporter;
import com.bizvisionsoft.service.exportvalueextract.FieldExportValueExtracter;
import com.bizvisionsoft.service.provider.BasicDBObjectAdapter;
import com.bizvisionsoft.service.provider.DateAdapter;
import com.bizvisionsoft.service.provider.DocumentAdapter;
import com.bizvisionsoft.service.provider.ObjectIdAdapter;
import com.bizvisionsoft.service.tools.Check;
import com.google.gson.GsonBuilder;
import com.mongodb.BasicDBObject;

public class ReportServiceImpl extends BasicServiceImpl implements ReportService {

	public Response generateReport(InputStream template, Document parameter, String type, String fileName) {
		ReportCreator rc = Service.get(ReportCreator.class);
		if (rc != null) {
			try {
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				Map<String, String> params = new HashMap<String, String>();
				if (parameter != null)
					parameter.entrySet().iterator().forEachRemaining(e -> params.put(e.getKey(), e.getValue().toString()));

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
	public Response generateReport(String rptParam, String templateName, String outputType, String fileName, String domain) {
		String filePath = Domain.getReportTemplateFile(domain, templateName);
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
	public Response commandReport(String commandParam, String domain) {
		Document cmd = Document.parse(commandParam);
		String outputType = cmd.getString("outputType");
		String template = cmd.getString("template");
		String fileName = cmd.getString("fileName");

		JQ jq = Domain.getJQ(domain, cmd.getString("jq"));
		Arrays.asList("input_obj_id", "selected_id", "page_input_id", "root_input_id")
				.forEach(s -> Check.isAssigned(cmd.getString(s), i -> jq.set(s, new ObjectId(i))));

		String filePath = Domain.getReportTemplateFile(domain, template);
		try {
			FileInputStream is = new FileInputStream(filePath);
			// TODO
			// 使用GsonBuilder存在转换存在问题。1.ObjectId转换错误。2."from":"organization"会转换成{"value":
			// "organization"}；从而造成无法生成PDF。
			String json = new GsonBuilder()//
					.serializeNulls().registerTypeAdapter(ObjectId.class, new ObjectIdAdapter())//
					.registerTypeAdapter(Date.class, new DateAdapter())//
					.registerTypeAdapter(BasicDBObject.class, new BasicDBObjectAdapter())//
					.registerTypeAdapter(Document.class, new DocumentAdapter())//
					.create().toJson(jq.list());

			return generateReport(is, new Document("pipeline", json), outputType, fileName);
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
		return Response.ok().header("Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Credentials", "true")
				.header("Access-Control-Allow-Methods", "POST, GET, PUT, UPDATE, OPTIONS")
				.header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With");
	}

	@Override
	public void export(ExportCommand command) {
		try {
			FieldExportValueExtracter ext = new FieldExportValueExtracter(command.form, command.data);
			new Form2DocxExporter(command.form, ext::getExportValue).export(command.fileName, command.properties);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

	}

}
