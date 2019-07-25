package com.bizvisionsoft.service;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.bson.Document;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Path("/db")
@Api("/db")
public interface DataService {

	@POST
	@Path("/query/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "数据查询服务", response = Document.class, responseContainer = "List")
	public List<Document> query(QueryCommand command);

}
