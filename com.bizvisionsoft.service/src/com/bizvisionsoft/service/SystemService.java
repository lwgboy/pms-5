package com.bizvisionsoft.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.bizvisionsoft.service.model.ServerInfo;

@Path("/sysman")
public interface SystemService {

	@GET
	@Path("/{req}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public ServerInfo getServerInfo(@PathParam("req") String req);

	@POST
	@Path("/dump/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public String mongodbDump();


}