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
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.WorkInfo;
import com.bizvisionsoft.service.model.WorkLinkInfo;
import com.mongodb.BasicDBObject;

@Path("/work")
public interface WorkService {

	@POST
	@Path("/gantt/tasks")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("data")
	public List<WorkInfo> createTaskDataSet(@ServiceParam(ServiceParam.FILTER) BasicDBObject condition);

	@POST
	@Path("/gantt/links")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("links")
	public List<WorkLinkInfo> createLinkDataSet(@ServiceParam(ServiceParam.FILTER) BasicDBObject condition);

	@POST
	@Path("/tasks/nextwbsidx")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public int nextWBSIndex(BasicDBObject condition);

	@POST
	@Path("/task/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public WorkInfo insertWork(WorkInfo work);

	@POST
	@Path("/link/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public WorkLinkInfo insertLink(WorkLinkInfo link);

	@PUT
	@Path("/task/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long updateWork(BasicDBObject filterAndUpdate);

	@PUT
	@Path("/link/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long updateLink(BasicDBObject filterAndUpdate);

	@DELETE
	@Path("/task/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long deleteWork(@PathParam("_id") ObjectId _id);

	@DELETE
	@Path("/link/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long deleteLink(@PathParam("_id") ObjectId _id);

	@GET
	@Path("/task/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet(DataSet.INPUT)
	public WorkInfo getWork(@PathParam("_id") @ServiceParam(ServiceParam._ID) ObjectId _id);

	@GET
	@Path("/link/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public WorkLinkInfo getLink(@PathParam("_id") ObjectId _id);

	@GET
	@Path("/project_id/{project_id}/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<WorkInfo> listProjectRootTask(@PathParam("project_id") ObjectId project_id);

	@GET
	@Path("/project_id/{project_id}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countProjectRootTask(@PathParam("project_id") ObjectId project_id);

	@GET
	@Path("/parent_id/{parent_id}/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<WorkInfo> listChildren(@PathParam("parent_id") ObjectId parent_id);

	@GET
	@Path("/parent_id/{parent_id}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countChildren(@PathParam("parent_id") ObjectId parent_id);

	@GET
	@Path("/_id/{_id}/action/start/{executeBy}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Result> start(@PathParam("_id") ObjectId _id, @PathParam("executeBy") String executeBy);

	@GET
	@Path("/_id/{_id}/daterange")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Date> getPlanDateRange(@PathParam("_id") ObjectId _id);

	@GET
	@Path("/_id/{_id}/scheduleplan/checkout/{userId}/{sessionId}/{cancelCheckOutSubSchedule}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Result checkOutSchedulePlan(@PathParam("_id") ObjectId _id, @PathParam("userId") String userId,
			@PathParam("sessionId") String sessionId,
			@PathParam("cancelCheckOutSubSchedule") boolean cancelCheckOutSubSchedule);

	@GET
	@Path("/_id/{_id}/scheduleplan/checkoutunauthorized/{userId}/{sessionId}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public boolean checkOutSchedulePlanUnauthorized(@PathParam("_id") ObjectId _id, @PathParam("userId") String userId,
			@PathParam("sessionId") String sessionId);

	@POST
	@Path("/gantt/tasksspace")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("data")
	public List<WorkInfo> createTaskDataSetBySpace(@ServiceParam(ServiceParam.FILTER) BasicDBObject condition);

	@POST
	@Path("/gantt/linksspace")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("links")
	public List<WorkLinkInfo> createLinkDataSetBySpace(@ServiceParam(ServiceParam.FILTER) BasicDBObject condition);

	@GET
	@Path("/_id/{_id}/daterangespace")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Date> getPlanDateRangeBySpace(@PathParam("_id") ObjectId _id);

	@POST
	@Path("/taskspace/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public WorkInfo insertWorkBySpace(WorkInfo work);

	@POST
	@Path("/linkspace/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public WorkLinkInfo insertLinkBySpace(WorkLinkInfo link);

	@PUT
	@Path("/taskspace/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long updateWorkBySpace(BasicDBObject filterAndUpdate);

	@PUT
	@Path("/linkspace/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long updateLinkBySpace(BasicDBObject filterAndUpdate);

	@DELETE
	@Path("/taskspace/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long deleteWorkBySpace(@PathParam("_id") ObjectId _id);

	@DELETE
	@Path("/linkspace/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long deleteLinkBySpace(@PathParam("_id") ObjectId _id);

}
