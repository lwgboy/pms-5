package com.bizvisionsoft.service;

import java.util.Date;
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
import com.bizvisionsoft.annotations.md.service.ServiceParam;
import com.bizvisionsoft.service.model.Command;
import com.bizvisionsoft.service.model.News;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.SalesItem;
import com.bizvisionsoft.service.model.Stockholder;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.model.Workspace;
import com.mongodb.BasicDBObject;

@Path("/project")
public interface ProjectService {

	@POST
	@Path("/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Project insert(Project project);

	@PUT
	@Path("/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long update(BasicDBObject filterAndUpdate);

	@GET
	@Path("/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet(DataSet.INPUT)
	public Project get(@PathParam("_id") @ServiceParam("_id") ObjectId _id);

	@POST
	@Path("/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long count(@ServiceParam(ServiceParam.FILTER) BasicDBObject filter);

	@POST
	@Path("/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Project> createDataSet(@ServiceParam(ServiceParam.CONDITION) BasicDBObject condition);

	@GET
	@Path("/_id/{_id}/daterange")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Date> getPlanDateRange(@PathParam("_id") ObjectId _id);

	@POST
	@Path("/_id/{_id}/stage/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Work> listStage(@PathParam("_id") ObjectId _id);

	@POST
	@Path("/_id/{_id}/stage/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countStage(@PathParam("_id") ObjectId _id);

	@POST
	@Path("/command/start/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Result> startProject(Command command);

	@POST
	@Path("/command/distribute/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Result> distributeProjectPlan(Command command);

	@POST
	@Path("/_id/{_id}/stockholder/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "项目干系人/list", "项目干系人（查看）/list" })
	public List<Stockholder> getStockholders(@ServiceParam(ServiceParam.CONDITION) BasicDBObject condition,
			@ServiceParam(ServiceParam.ROOT_CONTEXT_INPUT_OBJECT_ID) @PathParam("_id") ObjectId _id);

	
	@Path("/_id/{_id}/stockholder/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "项目干系人/count", "项目干系人（查看）/count" })
	public long countStockholders(@ServiceParam(ServiceParam.FILTER) BasicDBObject filter,
			@ServiceParam(ServiceParam.ROOT_CONTEXT_INPUT_OBJECT_ID) @PathParam("_id") ObjectId _id);

	@POST
	@Path("/stockholder")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Stockholder insertStockholder(Stockholder c);
	
	@DELETE
	@Path("/stockholder/id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("项目干系人/" + DataSet.DELETE)
	public long deleteStockholder(@PathParam("_id") @ServiceParam(ServiceParam._ID) ObjectId id);

	@POST
	@Path("/pm/{userid}/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Project> listManagedProjects(@ServiceParam(ServiceParam.CONDITION) BasicDBObject condition,
			@ServiceParam(ServiceParam.CURRENT_USER_ID) @PathParam("userid") String userid);

	@POST
	@Path("/pm/{userid}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countManagedProjects(@ServiceParam(ServiceParam.FILTER) BasicDBObject filter,
			@ServiceParam(ServiceParam.CURRENT_USER_ID) @PathParam("userid") String userid);

	@POST
	@Path("/member/{userid}/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "我的项目/list", "我的项目（首页小组件）/list","我的项目选择列表/list" })
	public List<Project> listParticipatedProjects(@ServiceParam(ServiceParam.CONDITION) BasicDBObject condition,
			@ServiceParam(ServiceParam.CURRENT_USER_ID) @PathParam("userid") String userid);

	@POST
	@Path("/member/{userid}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "我的项目/count", "我的项目（首页小组件）/count","我的项目选择列表/count" })
	public long countParticipatedProjects(@ServiceParam(ServiceParam.FILTER) BasicDBObject filter,
			@ServiceParam(ServiceParam.CURRENT_USER_ID) @PathParam("userid") String userid);

	@DELETE
	@Path("/id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("我的项目/" + DataSet.DELETE)
	public long delete(@PathParam("_id") @ServiceParam(ServiceParam._ID) ObjectId id);

	@GET
	@Path("/workspace/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Workspace getWorkspace(@PathParam("_id") ObjectId _id);

	@POST
	@Path("/command/finish/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Result> finishProject(Command command);

	@POST
	@Path("/command/close/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Result> closeProject(Command command);

	@POST
	@Path("/genworkorder/{catalog}/{parentproject_id}/{impunit_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public String generateWorkOrder(@PathParam("catalog") String catalog,
			@PathParam("parentproject_id") ObjectId parentproject_id, @PathParam("impunit_id") ObjectId impunit_id);

	@POST
	@Path("/_id/{_id}/news/{count}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<News> getRecentNews(@PathParam("_id") ObjectId _id, @PathParam("count") int count);
	
	@POST
	@Path("/salesItem/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public SalesItem insertSalesItem(SalesItem salesItem);

}
