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

import org.bson.Document;
import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.service.model.Command;
import com.bizvisionsoft.service.model.DateMark;
import com.bizvisionsoft.service.model.Period;
import com.bizvisionsoft.service.model.ResourceActual;
import com.bizvisionsoft.service.model.ResourceAssignment;
import com.bizvisionsoft.service.model.ResourcePlan;
import com.bizvisionsoft.service.model.ResourceTransfer;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.UpdateWorkPackages;
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
	public List<Work> createTaskDataSet(@MethodParam(MethodParam.FILTER) BasicDBObject condition);

	@POST
	@Path("/gantt/links")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<WorkLink> createLinkDataSet(@MethodParam(MethodParam.FILTER) BasicDBObject condition);

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
	public Work getWork(@PathParam("_id") @MethodParam(MethodParam._ID) ObjectId _id);

	@GET
	@Path("/link/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public WorkLink getLink(@PathParam("_id") ObjectId _id);

	@GET
	@Path("/task/_id/{_id}/project_id/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public ObjectId getProjectId(@PathParam("_id") ObjectId _id);

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

	@POST
	@Path("/command/startstage/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Result> startStage(Command command);

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
	@Path("/userid/{userid}/processing/datemark")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("我的待处理工作日历选择器/list")
	public List<DateMark> listMyWorksDateMark(@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid);

	@POST
	@Path("/userid/{userid}/processing/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "我的待处理工作（工作抽屉）/list" })
	public List<Work> listMyProcessingWork(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid);

	@POST
	@Path("/userid/{userid}/processing/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "我的待处理工作（工作抽屉）/count" })
	public long countMyProcessingWork(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid);

	@POST
	@Path("/userid/{userid}/planned/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("工作看板-已计划/list")
	public List<Work> listMyPlannedWork(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid);

	@POST
	@Path("/userid/{userid}/planned/card/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("工作看板（已计划）/list")
	public List<Document> listMyPlannedWorkCard(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid);

	@POST
	@Path("/userid/{userid}/planned/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "工作看板-已计划/count", "工作看板（已计划）/count" })
	public long countMyPlannedWork(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid);

	@POST
	@Path("/userid/{userid}/exec/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("工作看板-处理中/list")
	public List<Work> listMyExecutingWork(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid);

	@POST
	@Path("/userid/{userid}/exec/card/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("工作看板（处理中）/list")
	public List<Document> listMyExecutingWorkCard(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid);

	@POST
	@Path("/userid/{userid}/exec/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "工作看板-处理中/count", "工作看板（处理中）/count" })
	public long countMyExecutingWork(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid);

	@POST
	@Path("/userid/{userid}/finished/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "我的工作（已完成）/list", "工作看板-已完成/list" })
	public List<Work> listMyFinishedWork(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid);

	@POST
	@Path("/userid/{userid}/finished/card/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "工作看板（已完成）/list" })
	public List<Document> listMyFinishedWorkCard(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid);

	@POST
	@Path("/userid/{userid}/finished/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "我的工作（已完成）/count", "工作看板-已完成/count", "工作看板（已完成）/count" })
	public long countMyFinishedWork(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid);

	@POST
	@Path("/userid/{userid}/assigner/processing/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "我的待指派工作/list" })
	public List<Work> listMyAssignmentWork(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid);

	@POST
	@Path("/userid/{userid}/assigner/processing/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "我的待指派工作/count" })
	public long countMyAssignmentWork(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid);

	@POST
	@Path("/userid/{userid}/unassigner/processing/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "工作看板-待指派/list" })
	public List<Work> listMyUnAssignmentWork(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid);

	@POST
	@Path("/userid/{userid}/unassigner/processing/card/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "工作看板（待指派）/list" })
	public List<Document> listMyUnAssignmentWorkCard(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid);

	@POST
	@Path("/userid/{userid}/unassigner/processing/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "指派工作/budget", "工作看板-待指派/count", "工作看板（待指派）/count" })
	public long countMyUnAssignmentWork(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid);

	@POST
	@Path("/package/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<WorkPackage> listWorkPackage(BasicDBObject condition);

	@POST
	@Path("/package/{_id}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public WorkPackage getWorkPackage(@PathParam("_id") ObjectId _id);

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

	@POST
	@Path("/command/startwork/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Result> startWork(Command command);

	@POST
	@Path("/command/finishwork/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Result> finishWork(Command command);

	@POST
	@Path("/command/finishstage/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Result> finishStage(Command command);

	@POST
	@Path("/command/closestage/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Result> closeStage(Command command);

	@POST
	@Path("/resourceplan/add/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<ResourcePlan> addResourcePlan(List<ResourceAssignment> resas);

	@POST
	@Path("/resourceplan/insert/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public ResourcePlan insertResourcePlan(ResourcePlan rp);

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
	@Path("/_id/{_id}/resourceactual/ty/{resId}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long deleteTypedResourceActual(@PathParam("_id") ObjectId work_id, @PathParam("resId") String tyResId);

	@DELETE
	@Path("/_id/{_id}/resourceactual/hr/{resId}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long deleteHumanResourceActual(@PathParam("_id") ObjectId work_id, @PathParam("resId") String hrResId);

	@DELETE
	@Path("/_id/{_id}/resourceactual/eq/{resId}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long deleteEquipmentResourceActual(@PathParam("_id") ObjectId work_id, @PathParam("resId") String eqResId);

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
	public List<Work> listWorkPackageForScheduleInProject(@PathParam("project_id") ObjectId project_id,
			@PathParam("catagory") String catagory);

	@POST
	@Path("/track/project/{project_id}/{catagory}/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countWorkPackageForScheduleInProject(@PathParam("project_id") ObjectId project_id, @PathParam("catagory") String catagory);

	@POST
	@Path("/track/stage/{stage_id}/{catagory}/ds/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Work> listWorkPackageForScheduleInStage(@PathParam("stage_id") ObjectId stage_id, @PathParam("catagory") String catagory);

	@POST
	@Path("/track/stage/{stage_id}/{catagory}/count/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countWorkPackageForScheduleInStage(@PathParam("stage_id") ObjectId stage_id, @PathParam("catagory") String catagory);

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
	@Path("/resourceactual/add/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<ResourceActual> addResourceActual(List<ResourceAssignment> resas);

	@POST
	@Path("/resourceactual/insert/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public ResourceActual insertResourceActual(ResourceActual ra);

	@POST
	@Path("/_id/{_id}/resourceactual/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<ResourceActual> listResourceActual(@PathParam("_id") ObjectId _id);

	@PUT
	@Path("/resourceactual/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long updateResourceActual(BasicDBObject filterAndUpdate);

	@POST
	@Path("/resourcePlan/conflict/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<WorkResourcePlanDetail> listConflictWorks(ResourcePlan rp);

	@POST
	@Path("/assignRoleToProject/{project_id}/{cover}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void assignRoleToProject(@PathParam("project_id") ObjectId _id, @PathParam("cover") boolean cover);

	@POST
	@Path("/checkCoverWork/{project_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public boolean checkCoverWork(@PathParam("project_id") ObjectId project_id);

	@POST
	@Path("/assignRoleToStage/{work_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public void assignRoleToStage(@PathParam("work_id") ObjectId _id);

	@POST
	@Path("/resource/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Document> getResource(ResourceTransfer ra);

	@POST
	@Path("/resourceplananalysis/project_id/{project_id}/{year}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document getResourcePlanAnalysis(@PathParam("project_id") ObjectId project_id, @PathParam("year") String year);

	@POST
	@Path("/resourceactualanalysis/project_id/{project_id}/{year}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document getResourceActualAnalysis(@PathParam("project_id") ObjectId project_id, @PathParam("year") String year);

	@POST
	@Path("/resourceallanalysis/project_id/{project_id}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document getResourceAllAnalysis(@PathParam("project_id") ObjectId project_id);

	@POST
	@Path("/chart/project_id/{project_id}/resPlanAndUsage")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("项目资源计划用量对比/list")
	public Document getProjectResourcePlanAndUsageChart(
			@PathParam("project_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId project_id);

	@POST
	@Path("/chart/project_id/{project_id}/workScore")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("项目各类工作评分/list")
	public Document getProjectWorkScoreChart(
			@PathParam("project_id") @MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) ObjectId project_id);

	@POST
	@Path("/chart/{managerId}/workScore")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("我管理的项目各类工作评分/list")
	public Document getAdministratedProjectWorkScoreChart(
			@PathParam("managerId") @MethodParam(MethodParam.CURRENT_USER_ID) String managerId);

	@POST
	@Path("/resourceallanalysis/{year}/{userid}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document getResourceAllAnalysisByDept(@PathParam("year") String year, @PathParam("userid") String userid);

	@POST
	@Path("/resource/project/{project_id}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Document> getProjectResource(@PathParam("project_id") ObjectId project_id);

	@POST
	@Path("/resource/dept/{chargerId}/period/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Document> getResourceOfChargedDept(Period period, @PathParam("chargerId") String chargerId);

	@POST
	@Path("/resourceactual/add/{workReportItemId}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Document> addWorkReportResourceActual(List<ResourceAssignment> resas,
			@PathParam("workReportItemId") ObjectId workReportItemId);

	@POST
	@Path("/resourceactual/insert/{workReportItemId}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Document insertWorkReportResourceActual(ResourceActual ra, @PathParam("workReportItemId") ObjectId workReportItemId);

	@PUT
	@Path("/resourceactual/workreport/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long updateWorkReportResourceActual(BasicDBObject filterAndUpdate);

	@POST
	@Path("/baseline_id/{baseline_id}/gantttasks")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("项目基线甘特图/data")
	public List<Work> createBaselineTaskDataSet(
			@MethodParam(MethodParam.CONTEXT_INPUT_OBJECT_ID) @PathParam("baseline_id") ObjectId baseline_id);

	@POST
	@Path("/baseline_id/{baseline_id}/ganttlinks")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("项目基线甘特图/links")
	public List<WorkLink> createBaselineLinkDataSet(
			@MethodParam(MethodParam.CONTEXT_INPUT_OBJECT_ID) @PathParam("baseline_id") ObjectId baseline_id);

	@POST
	@Path("/userid/{userid}/charger/processing/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "我的工作/list", "我的工作（日历牌）/list" })
	public List<Work> createChargerProcessingWorkDataSet(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid);

	@POST
	@Path("/userid/{userid}/charger/processing/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "我的工作/count", "处理工作/budget" })
	public long countChargerProcessingWorkDataSet(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid);

	@POST
	@Path("/package/update/purchase")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<WorkPackage> updatePurchaseWorkPackage(UpdateWorkPackages updateWorkPackages);

	@POST
	@Path("/package/update/production")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<WorkPackage> updateProductionWorkPackage(UpdateWorkPackages updateWorkPackages);

	@POST
	@Path("/package/update/development")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<WorkPackage> updateDevelopmentWorkPackage(UpdateWorkPackages updateWorkPackages);

	@PUT
	@Path("/packageinfo/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public ObjectId updateWorkPackageInfo(Document info);

	@POST
	@Path("/projectid/{projectid}/planned/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "项目工作看板-已计划/" + DataSet.LIST })
	public List<Work> listProjectPlannedWork(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) @PathParam("projectid") ObjectId project_id);

	@POST
	@Path("/projectid/{projectid}/planned/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "项目工作看板-已计划/" + DataSet.COUNT, "项目工作看板（已计划）/" + DataSet.COUNT })
	public long countProjectPlannedWork(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) @PathParam("projectid") ObjectId project_id);

	@POST
	@Path("/projectid/{projectid}/exec/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "项目工作看板-处理中/" + DataSet.LIST })
	public List<Work> listProjectExecutingWork(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) @PathParam("projectid") ObjectId project_id);

	@POST
	@Path("/projectid/{projectid}/exec/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "项目工作看板-处理中/" + DataSet.COUNT, "项目工作看板（处理中）/" + DataSet.COUNT })
	public long countProjectExecutingWork(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) @PathParam("projectid") ObjectId project_id);

	@POST
	@Path("/projectid/{projectid}/finished/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "项目工作看板-已完成/" + DataSet.LIST })
	public List<Work> listProjectFinishedWork(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) @PathParam("projectid") ObjectId project_id);

	@POST
	@Path("/projectid/{projectid}/finished/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "项目工作看板-已完成/" + DataSet.COUNT, "项目工作看板（已完成）/" + DataSet.COUNT })
	public long countProjectFinishedWork(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) @PathParam("projectid") ObjectId project_id);

	@POST
	@Path("/projectid/{projectid}/unassigner/processing/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "项目工作看板-待指派/" + DataSet.LIST })
	public List<Work> listProjectUnAssignmentWork(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) @PathParam("projectid") ObjectId project_id);

	@POST
	@Path("/projectid/{projectid}/unassigner/processing/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "项目工作看板-待指派/" + DataSet.COUNT, "项目工作看板（待指派）/" + DataSet.COUNT })
	public long countProjectUnAssignmentWork(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) @PathParam("projectid") ObjectId project_id);

	@POST
	@Path("/projectid/{projectid}/{userid}/planned/card/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "项目工作看板（已计划）/" + DataSet.LIST })
	public List<Document> listProjectPlannedWorkCard(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) @PathParam("projectid") ObjectId project_id,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid);

	@POST
	@Path("/projectid/{projectid}/{userid}/exec/card/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "项目工作看板（处理中）/" + DataSet.LIST })
	public List<Document> listProjectExecutingWorkCard(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) @PathParam("projectid") ObjectId project_id,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid);

	@POST
	@Path("/projectid/{projectid}/{userid}/finished/card/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "项目工作看板（已完成）/" + DataSet.LIST })
	public List<Document> listProjectFinishedWorkCard(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) @PathParam("projectid") ObjectId project_id,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid);

	@POST
	@Path("/projectid/{projectid}/{userid}/unassigner/processing/card/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "项目工作看板（待指派）/" + DataSet.LIST })
	public List<Document> listProjectUnAssignmentWorkCard(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) @PathParam("projectid") ObjectId project_id,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid);

}
