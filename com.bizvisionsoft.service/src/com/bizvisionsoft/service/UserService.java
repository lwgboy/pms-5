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
import com.bizvisionsoft.service.model.TraceInfo;
import com.bizvisionsoft.service.model.User;
import com.mongodb.BasicDBObject;

@Path("/user")
public interface UserService {

	@PUT
	@Path("/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long update(BasicDBObject filterAndUpdate);

	@PUT
	@Path("/consign/userId/{userId}/consignerId/{consignerId}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void consign(@PathParam("userId") String userId, @PathParam("consignerId") String consignerId);

	@PUT
	@Path("/consign/userId/{userId}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void disconsign(@PathParam("userId") String userId);

	@PUT
	@Path("/trace/userId/{userId}/trace/{trace}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void trace(@PathParam("userId") String userId,@PathParam("trace") boolean trace);

	@POST
	@Path("/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public User insert(User user);

	@POST
	@Path("/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "用户管理/" + DataSet.LIST, "用户选择列表/" + DataSet.LIST, "账户管理/" + DataSet.LIST })
	public List<User> createDataSet(@MethodParam(MethodParam.CONDITION) BasicDBObject condition);

	@POST
	@Path("/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "用户管理/" + DataSet.COUNT, "用户选择列表/" + DataSet.COUNT, "账户管理/" + DataSet.COUNT })
	public long count(@MethodParam(MethodParam.FILTER) BasicDBObject filter);

	@GET
	@Path("/check/{userId}/{password}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public User check(@PathParam("userId") String userId, @PathParam("password") String password);

	@GET
	@Path("/userId/{userId}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public User get(@PathParam("userId") String userId);

	@DELETE
	@Path("/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet(DataSet.DELETE)
	public long delete(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId get_id);

	@POST
	@Path("/dept/{userId}/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("指派用户选择列表/" + DataSet.LIST)
	public List<User> createDeptUserDataSet(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@PathParam("userId") @MethodParam(MethodParam.CURRENT_USER_ID) String userId);

	@POST
	@Path("/dept/{userId}/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("指派用户选择列表/" + DataSet.COUNT)
	public long countDeptUser(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@PathParam("userId") @MethodParam(MethodParam.CURRENT_USER_ID) String userId);

	@GET
	@Path("/consigned/{userId}/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<User> listConsigned(@PathParam("userId") String userId);

	@POST
	@Path("/traceInfo/")
	@Consumes("application/json; charset=UTF-8")
	public void insertTraceInfo(TraceInfo traceInfo);
	
	@POST
	@Path("/traceInfo/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("用户操作审计/" + DataSet.LIST)
	public List<TraceInfo> listTraceInfo(@MethodParam(MethodParam.CONDITION) BasicDBObject condition);

	@POST
	@Path("/traceInfo/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("用户操作审计/" + DataSet.COUNT)
	public long countTraceInfo(@MethodParam(MethodParam.FILTER) BasicDBObject filter);

}