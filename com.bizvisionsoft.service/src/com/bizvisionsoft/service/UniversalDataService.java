package com.bizvisionsoft.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import com.bizvisionsoft.annotations.UniversalCommand;
import com.bizvisionsoft.annotations.UniversalResult;
import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.ui.common.MethodParam;

import io.swagger.annotations.Api;

@Path("/uds")
@Api("/uds")
public interface UniversalDataService {

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	@POST
	@Path("/{domain}/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet(DataSet.LIST)
	public UniversalResult list(@MethodParam(MethodParam.COMMAND) UniversalCommand command,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet(DataSet.COUNT)
	public UniversalResult count(@MethodParam(MethodParam.COMMAND) UniversalCommand command,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/get")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet(DataSet.GET)
	public UniversalResult get(@MethodParam(MethodParam.COMMAND) UniversalCommand command,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/insert")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet(DataSet.INSERT)
	public UniversalResult insert(@MethodParam(MethodParam.COMMAND) UniversalCommand command,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/delete")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet(DataSet.DELETE)
	public UniversalResult delete(@MethodParam(MethodParam.COMMAND) UniversalCommand command,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/update")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet(DataSet.UPDATE)
	public UniversalResult update(@MethodParam(MethodParam.COMMAND) UniversalCommand command,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

}
