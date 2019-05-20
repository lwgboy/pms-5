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
import com.bizvisionsoft.service.model.FuncPermission;
import com.mongodb.BasicDBObject;

import io.swagger.annotations.Api;

@Path("/perm")
@Api("/perm")
public interface PermissionService {

	@POST
	@Path("/{domain}/func/")
	@Consumes("application/json")
	@Produces("application/json; charset=UTF-8")
	@DataSet("功能权限设置/" + DataSet.INSERT)
	public FuncPermission insertFunctionPermission(@MethodParam(MethodParam.OBJECT) FuncPermission fp,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/func")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("功能权限设置/" + DataSet.UPDATE)
	public long updateFunctionPermission(@MethodParam(MethodParam.FILTER_N_UPDATE) BasicDBObject fu,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/func/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("功能权限设置/" + DataSet.LIST)
	public List<FuncPermission> listFunctionPermission(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/func/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("功能权限设置/" + DataSet.COUNT)
	public long countFunctionPermission(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@DELETE
	@Path("/{domain}/func/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("功能权限设置/" + DataSet.DELETE)
	public long deleteFunctionPermission(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@GET
	@Path("/{domain}/func/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("功能权限设置/" + DataSet.GET)
	public FuncPermission getFunctionPermission(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id,
			@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

}
