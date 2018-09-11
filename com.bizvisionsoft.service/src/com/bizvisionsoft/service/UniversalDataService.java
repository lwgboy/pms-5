package com.bizvisionsoft.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.bizvisionsoft.annotations.UniversalCommand;
import com.bizvisionsoft.annotations.UniversalResult;
import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.ui.common.MethodParam;

@Path("/uds")
public interface UniversalDataService {

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	@POST
	@Path("/uds/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet(DataSet.LIST)
	public UniversalResult list(@MethodParam(MethodParam.COMMAND) UniversalCommand command);

	@POST
	@Path("/uds/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet(DataSet.COUNT)
	public UniversalResult conut(@MethodParam(MethodParam.COMMAND) UniversalCommand command);

	@POST
	@Path("/uds/get")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet(DataSet.GET)
	public UniversalResult get(@MethodParam(MethodParam.COMMAND) UniversalCommand command);

	@POST
	@Path("/uds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet(DataSet.INSERT)
	public UniversalResult insert(@MethodParam(MethodParam.COMMAND) UniversalCommand command);

	@DELETE
	@Path("/uds/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet(DataSet.DELETE)
	public UniversalResult delete(@MethodParam(MethodParam.COMMAND) UniversalCommand command);

	@PUT
	@Path("/uds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet(DataSet.UPDATE)
	public UniversalResult update(@MethodParam(MethodParam.COMMAND) UniversalCommand command);

}
