package com.bizvisionsoft.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/topsreport")
public interface TOPSReportService {

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response generateReport(@FormParam("rptParam") String rptParam, @FormParam("template") String templateName,
			@FormParam("fileName") String downloadableFileName, @FormParam("serverPath") String serverPath);

	@OPTIONS
	@Path("/")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response generateReport();

	@POST
	@Path("/command/")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response commandReport(@FormParam("command") String rptParam);

	@OPTIONS
	@Path("/command/")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response commandReport();

}
