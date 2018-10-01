package com.bizvisionsoft.service;

import java.io.InputStream;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataParam;

@Path("/report")
public interface ReportService {

	@POST
	@Path("/file")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response generateReport(@FormDataParam("file") InputStream template,
			@FormDataParam("parameter") Map<String,String> parameter, @FormDataParam("outputType") String outputType,
			@FormDataParam("downloadableFileName") String downloadableFileName);

	@POST
	@Path("/template")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response generateReport(@FormDataParam("template") String templateName,
			@FormDataParam("parameter") Map<String,String> parameter, @FormDataParam("outputType") String outputType,
			@FormDataParam("downloadableFileName") String downloadableFileName);
	
}
