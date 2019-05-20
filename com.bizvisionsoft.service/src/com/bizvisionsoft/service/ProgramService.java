package com.bizvisionsoft.service;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.service.model.Program;
import com.mongodb.BasicDBObject;

import io.swagger.annotations.Api;

@Path("/program")
@Api("/program")
public interface ProgramService {

	@POST
	@Path("/{domain}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "项目集管理/insert" })
	public Program insert(@MethodParam(MethodParam.OBJECT) Program program,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/_id/{_id}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void addProjects(List<ObjectId> pjIds, @PathParam("_id") ObjectId _id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "项目集管理/update" })
	public long update(@MethodParam(MethodParam.FILTER_N_UPDATE) BasicDBObject filterAndUpdate,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@GET
	@Path("/{domain}/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet(DataSet.INPUT)
	public Program get(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "项目集选择列表/count" })
	public long count(@MethodParam(MethodParam.FILTER) BasicDBObject filter,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "项目集选择列表/list" })
	public List<Program> list(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/root/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "项目集管理/count" })
	public long countRoot(@MethodParam(MethodParam.FILTER) BasicDBObject filter,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/root/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "项目集管理/list" })
	public List<Program> listRoot(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@DELETE
	@Path("/{domain}/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "项目集管理/delete" })
	public long delete(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId get_id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/project/{project_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void unsetProgram(@PathParam("project_id") ObjectId project_id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

}
