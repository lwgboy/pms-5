package com.bizvisionsoft.service;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.bson.Document;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Path("/db")
@Api("/db")
public interface DataService {

	@POST
	@Path("/aggregate")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "数据查询服务", response = Document.class, responseContainer = "List")
	public List<Document> query(@FormParam("collection") String collection, //
			@FormParam("pipeline") List<Document> pipeline, //
			@FormParam("sort") Document sort, //
			@FormParam("skip") Integer skip, //
			@FormParam("limit") Integer limit, //
			@FormParam("domain") String domain);

	@POST
	@Path("/jq")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "数据查询服务", response = Document.class, responseContainer = "List")
	public List<Document> queryJQ(@FormParam("collection") String collection, //
			@FormParam("jq") String jq, //
			@FormParam("parameter") Document parameter, //
			@FormParam("domain") String domain);

}
