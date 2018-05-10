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
public interface WorkSpaceService {

	@POST
	@Path("/tasks/nextwbsidx")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public int nextWBSIndex(BasicDBObject condition);

	@GET
	@Path("/taskInfo/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet(DataSet.INPUT)
	public WorkInfo getWorkInfo(@PathParam("_id") @ServiceParam(ServiceParam._ID) ObjectId _id);

	@POST
	@Path("/gantt/tasksspace")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("data")
	public List<WorkInfo> createTaskDataSet(@ServiceParam(ServiceParam.FILTER) BasicDBObject condition);

	@POST
	@Path("/gantt/linksspace")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("links")
	public List<WorkLinkInfo> createLinkDataSet(@ServiceParam(ServiceParam.FILTER) BasicDBObject condition);

	@GET
	@Path("/_id/{_id}/daterangespace")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Date> getPlanDateRange(@PathParam("_id") ObjectId _id);

	@POST
	@Path("/taskspace/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public WorkInfo insertWork(WorkInfo work);

	@POST
	@Path("/linkspace/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public WorkLinkInfo insertLink(WorkLinkInfo link);

	@PUT
	@Path("/taskspace/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long updateWork(BasicDBObject filterAndUpdate);

	@PUT
	@Path("/linkspace/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long updateLink(BasicDBObject filterAndUpdate);

	@DELETE
	@Path("/taskspace/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long deleteWork(@PathParam("_id") ObjectId _id);

	@DELETE
	@Path("/linkspace/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long deleteLink(@PathParam("_id") ObjectId _id);

	@PUT
	@Path("/_id/{_id}/checkoutuserid/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public String getCheckOutUserId(@PathParam("_id") ObjectId _id);

	@PUT
	@Path("/_id/{_id}/getspaceid/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public ObjectId getSpaceId(@PathParam("_id") ObjectId _id);

	@POST
	@Path("/scheduleplan/checkout/{userId}/{cancelCheckOutSubSchedule}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Result checkOutSchedulePlan(@ServiceParam(ServiceParam.OBJECT) BasicDBObject wbsScope,
			@PathParam("userId") String userId,
			@PathParam("cancelCheckOutSubSchedule") boolean cancelCheckOutSubSchedule);

	@POST
	@Path("/scheduleplan/check/{userId}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Result schedulePlanCheck(@ServiceParam(ServiceParam.OBJECT) BasicDBObject wbsScope,
			@PathParam("userId") String userId);

	@POST
	@Path("/scheduleplan/checkin/{userId}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Result checkInSchedulePlan(@ServiceParam(ServiceParam.OBJECT) BasicDBObject wbsScope,
			@PathParam("userId") String userId);

	@POST
	@Path("/scheduleplan/cancelcheckon/{userId}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Result cancelCheckOutSchedulePlan(@ServiceParam(ServiceParam.OBJECT) BasicDBObject wbsScope,
			@PathParam("userId") String userId);

}
