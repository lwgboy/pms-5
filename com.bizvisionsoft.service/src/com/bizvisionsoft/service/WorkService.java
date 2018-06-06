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
import com.bizvisionsoft.annotations.md.service.ServiceParam;
import com.bizvisionsoft.service.model.ResourceActual;
import com.bizvisionsoft.service.model.ResourceAssignment;
import com.bizvisionsoft.service.model.ResourcePlan;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.TrackView;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.model.WorkLink;
import com.bizvisionsoft.service.model.WorkPackage;
import com.bizvisionsoft.service.model.WorkPackageProgress;
import com.bizvisionsoft.service.model.WorkResourcePlanDetail;
import com.bizvisionsoft.service.model.Workspace;
import com.mongodb.BasicDBObject;

@Path("/work")
public interface WorkService {

	@POST
	@Path("/gantt/tasks")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Work> createTaskDataSet(@ServiceParam(ServiceParam.FILTER) BasicDBObject condition);

	@POST
	@Path("/gantt/links")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<WorkLink> createLinkDataSet(@ServiceParam(ServiceParam.FILTER) BasicDBObject condition);

	@POST
	@Path("/task/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Work insertWork(Work work);

	@POST
	@Path("/link/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public WorkLink insertLink(WorkLink link);

	@PUT
	@Path("/task/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("我的工作/" + DataSet.UPDATE)
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
	public Work getWork(@PathParam("_id") @ServiceParam(ServiceParam._ID) ObjectId _id);

	@GET
	@Path("/link/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public WorkLink getLink(@PathParam("_id") ObjectId _id);

	@POST
	@Path("/project_id/{project_id}/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Work> listProjectRootTask(@PathParam("project_id") ObjectId project_id);

	@POST
	@Path("/project_id/{project_id}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countProjectRootTask(@PathParam("project_id") ObjectId project_id);

	@POST
	@Path("/parent_id/{parent_id}/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Work> listChildren(@PathParam("parent_id") ObjectId parent_id);

	@POST
	@Path("/parent_id/{parent_id}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countChildren(@PathParam("parent_id") ObjectId parent_id);

	@PUT
	@Path("/_id/{_id}/startstage/{executeBy}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Result> startStage(@PathParam("_id") ObjectId _id, @PathParam("executeBy") String executeBy);

	@GET
	@Path("/workspace/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Workspace getWorkspace(@PathParam("_id") ObjectId _id);

	@POST
	@Path("/parent_id/{parent_id}/ganttlinks")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<WorkLink> createWorkLinkDataSet(@PathParam("parent_id") ObjectId parent_id);

	@POST
	@Path("/parent_id/{parent_id}/gantttasks")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Work> createWorkTaskDataSet(@PathParam("parent_id") ObjectId parent_id);

	@POST
	@Path("/project_id/{project_id}/ganttlinks")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<WorkLink> createProjectLinkDataSet(@PathParam("project_id") ObjectId project_id);

	@POST
	@Path("/project_id/{project_id}/gantttasks")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Work> createProjectTaskDataSet(@PathParam("project_id") ObjectId project_id);

	@POST
	@Path("/userid/{userid}/processing/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "我的工作/list", "我的工作（日历牌）/list", "我的待处理工作（首页小组件）/list" })
	public List<Work> createProcessingWorkDataSet(@ServiceParam(ServiceParam.CONDITION) BasicDBObject condition,
			@ServiceParam(ServiceParam.CURRENT_USER_ID) @PathParam("userid") String userid);

	@POST
	@Path("/userid/{userid}/processing/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "我的工作/count", "我的待处理工作（首页小组件）/count" })
	public long countProcessingWorkDataSet(@ServiceParam(ServiceParam.FILTER) BasicDBObject filter,
			@ServiceParam(ServiceParam.CURRENT_USER_ID) @PathParam("userid") String userid);

	@POST
	@Path("/userid/{userid}/finished/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("我的工作（已完成）/list")
	public List<Work> createFinishedWorkDataSet(@ServiceParam(ServiceParam.CONDITION) BasicDBObject condition,
			@ServiceParam(ServiceParam.CURRENT_USER_ID) @PathParam("userid") String userid);

	@POST
	@Path("/userid/{userid}/finished/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("我的工作（已完成）/count")
	public long countFinishedWorkDataSet(@ServiceParam(ServiceParam.FILTER) BasicDBObject filter,
			@ServiceParam(ServiceParam.CURRENT_USER_ID) @PathParam("userid") String userid);

	@POST
	@Path("/package/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<WorkPackage> listWorkPackage(BasicDBObject condition);
	
	@POST
	@Path("/templatepackage/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")	
	List<WorkPackage> listWorkInTemplatePackage(BasicDBObject condition);

	@POST
	@Path("/package/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countWorkPackage(BasicDBObject filter);

	@POST
	@Path("/package/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public WorkPackage insertWorkPackage(WorkPackage wp);

	@DELETE
	@Path("/package/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long deleteWorkPackage(@PathParam("_id") ObjectId _id);

	@PUT
	@Path("/package/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long updateWorkPackage(BasicDBObject filterAndUpdate);

	@POST
	@Path("/userid/{userid}/deptuserwork/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Work> createDeptUserWorkDataSet(@PathParam("userid") String userid);

	@PUT
	@Path("/_id/{_id}/distribute/{executeBy}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Result> distributeWorkPlan(@PathParam("_id") ObjectId _id, @PathParam("executeBy") String executeBy);

	@PUT
	@Path("/_id/{_id}/startwork")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Result> startWork(@PathParam("_id") ObjectId _id);

	@PUT
	@Path("/_id/{_id}/finishwork")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Result> finishWork(@PathParam("_id") ObjectId _id);

	@PUT
	@Path("/_id/{_id}/finishstage/{executeBy}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Result> finishStage(@PathParam("_id") ObjectId _id, @PathParam("executeBy") String executeBy);

	@PUT
	@Path("/_id/{_id}/closestage/{executeBy}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Result> closeStage(@PathParam("_id") ObjectId _id, @PathParam("executeBy") String executeBy);

	@POST
	@Path("/resourceplan/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<ResourcePlan> addResourcePlan(List<ResourceAssignment> resas);

	@PUT
	@Path("/resourceplan/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long updateResourcePlan(BasicDBObject filterAndUpdate);

	@DELETE
	@Path("/_id/{_id}/resourceplan/hr/{resId}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long deleteHumanResourcePlan(@PathParam("_id") ObjectId work_id, @PathParam("resId") String hrResId);

	@DELETE
	@Path("/_id/{_id}/resourceplan/eq/{resId}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long deleteEquipmentResourcePlan(@PathParam("_id") ObjectId work_id, @PathParam("resId") String eqResId);
	
	@DELETE
	@Path("/_id/{_id}/resourceplan/ty/{resId}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long deleteTypedResourcePlan(@PathParam("_id") ObjectId work_id, @PathParam("resId") String tyResId);

	
	@POST
	@Path("/_id/{_id}/resourceplan/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<ResourcePlan> listResourcePlan(@PathParam("_id") ObjectId _id);

	@POST
	@Path("/packageprogress/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public WorkPackageProgress insertWorkPackageProgress(WorkPackageProgress wpp);

	@DELETE
	@Path("/packageprogress/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long deleteWorkPackageProgress(ObjectId _id);

	@POST
	@Path("/packageprogress/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<WorkPackageProgress> listWorkPackageProgress(BasicDBObject condition);

	@POST
	@Path("/packageprogress/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countWorkPackageProgress(BasicDBObject filter);

	@PUT
	@Path("/packageprogress/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long updateWorkPackageProgress(BasicDBObject filterAndUpdate);

	@POST
	@Path("/track/project/{project_id}/{catagory}/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<TrackView> listWorkPackageForScheduleInProject(@PathParam("project_id") ObjectId project_id,
			@PathParam("catagory") String catagory);

	@POST
	@Path("/track/project/{project_id}/{catagory}/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countWorkPackageForScheduleInProject(@PathParam("project_id") ObjectId project_id,
			@PathParam("catagory") String catagory);

	@POST
	@Path("/track/stage/{stage_id}/{catagory}/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<TrackView> listWorkPackageForScheduleInStage(@PathParam("stage_id") ObjectId stage_id,
			@PathParam("catagory") String catagory);

	@POST
	@Path("/track/stage/{stage_id}/{catagory}/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countWorkPackageForScheduleInStage(@PathParam("stage_id") ObjectId stage_id,
			@PathParam("catagory") String catagory);

	@POST
	@Path("/track/{userid}/{catagory}/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Work> listWorkPackageForSchedule(BasicDBObject condition, @PathParam("userid") String userid,
			@PathParam("catagory") String catagory);

	@POST
	@Path("/track/{userid}/{catagory}/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countWorkPackageForSchedule(BasicDBObject filter, @PathParam("userid") String userid,
			@PathParam("catagory") String catagory);

	@POST
	@Path("/resourceactual/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<ResourceActual> addResourceActual(ResourceAssignment resa);

	@POST
	@Path("/_id/{_id}/resourceactual/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<ResourceActual> listResourceActual(@PathParam("_id") ObjectId _id);

	@POST
	@Path("/resourcePlan/conflict/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<WorkResourcePlanDetail> listConflictWorks(ResourcePlan rp);

	@GET
	@Path("/worktime/plan/{wbscode}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public double getPlanWorks(@PathParam("wbscode")String wbscode);

	@GET
	@Path("/worktime/actual/{wbscode}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public double getActualWorks(@PathParam("wbscode")String wbscode);

}
