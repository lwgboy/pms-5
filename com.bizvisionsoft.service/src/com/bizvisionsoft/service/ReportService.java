package com.bizvisionsoft.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/report")
public interface ReportService {

	@POST
	@Path("/{domain}/")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response generateReport(@FormParam("rptParam") String rptParam, @FormParam("template") String templateName,
			@FormParam("outputType") String outputType, @FormParam("fileName") String downloadableFileName,
			@FormParam("domain") String domain);

	@OPTIONS
	@Path("/")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response generateReport();

	@POST
	@Path("/{domain}/command/")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response commandReport(@FormParam("command") String rptParam, @FormParam("domain") String domain);

	@OPTIONS
	@Path("/command/")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response commandReport();

}
