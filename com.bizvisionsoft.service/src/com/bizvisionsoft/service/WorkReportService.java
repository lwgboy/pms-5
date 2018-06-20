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
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.WorkReport;
import com.bizvisionsoft.service.model.WorkReportItem;
import com.mongodb.BasicDBObject;

@Path("/workreport")
public interface WorkReportService {

	@POST
	@Path("/userid/{userid}/daily/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "日报/" + DataSet.LIST })
	public List<WorkReport> createWorkReportDailyDataSet(@ServiceParam(ServiceParam.CONDITION) BasicDBObject condition,
			@ServiceParam(ServiceParam.CURRENT_USER_ID) @PathParam("userid") String userid);

	@POST
	@Path("/userid/{userid}/daily/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "日报/" + DataSet.COUNT })
	public long countWorkReportDailyDataSet(@ServiceParam(ServiceParam.FILTER) BasicDBObject filter,
			@ServiceParam(ServiceParam.CURRENT_USER_ID) @PathParam("userid") String userid);

	@POST
	@Path("/userid/{userid}/weekly/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "周报/" + DataSet.LIST })
	public List<WorkReport> createWorkReportWeeklyDataSet(@ServiceParam(ServiceParam.CONDITION) BasicDBObject condition,
			@ServiceParam(ServiceParam.CURRENT_USER_ID) @PathParam("userid") String userid);

	@POST
	@Path("/userid/{userid}/weekly/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "周报/" + DataSet.COUNT })
	public long countWorkReportWeeklyDataSet(@ServiceParam(ServiceParam.FILTER) BasicDBObject filter,
			@ServiceParam(ServiceParam.CURRENT_USER_ID) @PathParam("userid") String userid);

	@POST
	@Path("/userid/{userid}/monthly/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "月报/" + DataSet.LIST })
	public List<WorkReport> createWorkReportMonthlyDataSet(
			@ServiceParam(ServiceParam.CONDITION) BasicDBObject condition,
			@ServiceParam(ServiceParam.CURRENT_USER_ID) @PathParam("userid") String userid);

	@POST
	@Path("/userid/{userid}/monthly/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "月报/" + DataSet.COUNT })
	public long countWorkReportMonthlyDataSet(@ServiceParam(ServiceParam.FILTER) BasicDBObject filter,
			@ServiceParam(ServiceParam.CURRENT_USER_ID) @PathParam("userid") String userid);

	@POST
	@Path("/userid/{userid}/daily/project/{project_id}/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "项目日报/" + DataSet.LIST })
	public List<WorkReport> createWorkReportProjectDailyDataSet(
			@ServiceParam(ServiceParam.CONDITION) BasicDBObject condition,
			@ServiceParam(ServiceParam.CURRENT_USER_ID) @PathParam("userid") String userid,
			@ServiceParam(ServiceParam.ROOT_CONTEXT_INPUT_OBJECT_ID) @PathParam("project_id") ObjectId project_id);

	@POST
	@Path("/userid/{userid}/daily/project/{project_id}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "项目日报/" + DataSet.COUNT })
	public long countWorkReportProjectDailyDataSet(@ServiceParam(ServiceParam.FILTER) BasicDBObject filter,
			@ServiceParam(ServiceParam.CURRENT_USER_ID) @PathParam("userid") String userid,
			@ServiceParam(ServiceParam.ROOT_CONTEXT_INPUT_OBJECT_ID) @PathParam("project_id") ObjectId project_id);

	@POST
	@Path("/userid/{userid}/weekly/project/{project_id}/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "项目周报/" + DataSet.LIST })
	public List<WorkReport> createWorkReportProjectWeeklyDataSet(
			@ServiceParam(ServiceParam.CONDITION) BasicDBObject condition,
			@ServiceParam(ServiceParam.CURRENT_USER_ID) @PathParam("userid") String userid,
			@ServiceParam(ServiceParam.ROOT_CONTEXT_INPUT_OBJECT_ID) @PathParam("project_id") ObjectId project_id);

	@POST
	@Path("/userid/{userid}/weekly/project/{project_id}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "项目周报/" + DataSet.COUNT })
	public long countWorkReportProjectWeeklyDataSet(@ServiceParam(ServiceParam.FILTER) BasicDBObject filter,
			@ServiceParam(ServiceParam.CURRENT_USER_ID) @PathParam("userid") String userid,
			@ServiceParam(ServiceParam.ROOT_CONTEXT_INPUT_OBJECT_ID) @PathParam("project_id") ObjectId project_id);

	@POST
	@Path("/userid/{userid}/monthly/project/{project_id}/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "项目月报/" + DataSet.LIST })
	public List<WorkReport> createWorkReportProjectMonthlyDataSet(
			@ServiceParam(ServiceParam.CONDITION) BasicDBObject condition,
			@ServiceParam(ServiceParam.CURRENT_USER_ID) @PathParam("userid") String userid,
			@ServiceParam(ServiceParam.ROOT_CONTEXT_INPUT_OBJECT_ID) @PathParam("project_id") ObjectId project_id);

	@POST
	@Path("/userid/{userid}/monthly/project/{project_id}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "项目月报/" + DataSet.COUNT })
	public long countWorkReportProjectMonthlyDataSet(@ServiceParam(ServiceParam.FILTER) BasicDBObject filter,
			@ServiceParam(ServiceParam.CURRENT_USER_ID) @PathParam("userid") String userid,
			@ServiceParam(ServiceParam.ROOT_CONTEXT_INPUT_OBJECT_ID) @PathParam("project_id") ObjectId project_id);

	@POST
	@Path("/userid/{userid}/daily/stage/{stage_id}/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "阶段日报/" + DataSet.LIST })
	public List<WorkReport> createWorkReportStageDailyDataSet(
			@ServiceParam(ServiceParam.CONDITION) BasicDBObject condition,
			@ServiceParam(ServiceParam.CURRENT_USER_ID) @PathParam("userid") String userid,
			@ServiceParam(ServiceParam.ROOT_CONTEXT_INPUT_OBJECT_ID) @PathParam("stage_id") ObjectId stage_id);

	@POST
	@Path("/userid/{userid}/daily/stage/{stage_id}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "阶段日报/" + DataSet.COUNT })
	public long countWorkReportStageDailyDataSet(@ServiceParam(ServiceParam.FILTER) BasicDBObject filter,
			@ServiceParam(ServiceParam.CURRENT_USER_ID) @PathParam("userid") String userid,
			@ServiceParam(ServiceParam.ROOT_CONTEXT_INPUT_OBJECT_ID) @PathParam("stage_id") ObjectId stage_id);

	@POST
	@Path("/userid/{userid}/weekly/stage/{stage_id}/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "阶段周报/" + DataSet.LIST })
	public List<WorkReport> createWorkReportStageWeeklyDataSet(
			@ServiceParam(ServiceParam.CONDITION) BasicDBObject condition,
			@ServiceParam(ServiceParam.CURRENT_USER_ID) @PathParam("userid") String userid,
			@ServiceParam(ServiceParam.ROOT_CONTEXT_INPUT_OBJECT_ID) @PathParam("stage_id") ObjectId stage_id);

	@POST
	@Path("/userid/{userid}/weekly/stage/{stage_id}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "阶段周报/" + DataSet.COUNT })
	public long countWorkReportStageWeeklyDataSet(@ServiceParam(ServiceParam.FILTER) BasicDBObject filter,
			@ServiceParam(ServiceParam.CURRENT_USER_ID) @PathParam("userid") String userid,
			@ServiceParam(ServiceParam.ROOT_CONTEXT_INPUT_OBJECT_ID) @PathParam("stage_id") ObjectId stage_id);

	@POST
	@Path("/userid/{userid}/monthly/stage/{stage_id}/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "阶段月报/" + DataSet.LIST })
	public List<WorkReport> createWorkReportStageMonthlyDataSet(
			@ServiceParam(ServiceParam.CONDITION) BasicDBObject condition,
			@ServiceParam(ServiceParam.CURRENT_USER_ID) @PathParam("userid") String userid,
			@ServiceParam(ServiceParam.ROOT_CONTEXT_INPUT_OBJECT_ID) @PathParam("stage_id") ObjectId stage_id);

	@POST
	@Path("/userid/{userid}/monthly/stage/{stage_id}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "阶段月报/" + DataSet.COUNT })
	public long countWorkReportStageMonthlyDataSet(@ServiceParam(ServiceParam.FILTER) BasicDBObject filter,
			@ServiceParam(ServiceParam.CURRENT_USER_ID) @PathParam("userid") String userid,
			@ServiceParam(ServiceParam.ROOT_CONTEXT_INPUT_OBJECT_ID) @PathParam("stage_id") ObjectId stage_id);

	@POST
	@Path("/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "日报/" + DataSet.INSERT, "周报/" + DataSet.INSERT, "月报/" + DataSet.INSERT, "项目日报/" + DataSet.INSERT,
			"项目周报/" + DataSet.INSERT, "项目月报/" + DataSet.INSERT, "阶段日报/" + DataSet.INSERT, "阶段周报/" + DataSet.INSERT,
			"阶段月报/" + DataSet.INSERT })
	public WorkReport insert(WorkReport workReport);

	@DELETE
	@Path("/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "日报/" + DataSet.DELETE, "周报/" + DataSet.DELETE, "月报/" + DataSet.DELETE, "项目日报/" + DataSet.DELETE,
			"项目周报/" + DataSet.DELETE, "项目月报/" + DataSet.DELETE, "阶段日报/" + DataSet.DELETE, "阶段周报/" + DataSet.DELETE,
			"阶段月报/" + DataSet.DELETE })
	public long delete(@ServiceParam(ServiceParam._ID) @PathParam("_id") ObjectId _id);

	@GET
	@Path("/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public WorkReport getWorkReport(@PathParam("_id") ObjectId _id);

	@POST
	@Path("/_id/{_id}/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("工作报告基本信息面板/list")
	public List<WorkReport> listInfo(
			@ServiceParam(ServiceParam.CONTEXT_INPUT_OBJECT_ID) @PathParam("_id") ObjectId _id);

	@PUT
	@Path("/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "日报/" + DataSet.UPDATE, "周报/" + DataSet.UPDATE, "月报/" + DataSet.UPDATE, "项目日报/" + DataSet.UPDATE,
			"项目周报/" + DataSet.UPDATE, "项目月报/" + DataSet.UPDATE, "阶段日报/" + DataSet.UPDATE, "阶段周报/" + DataSet.UPDATE,
			"阶段月报/" + DataSet.UPDATE })
	public long update(BasicDBObject filterAndUpdate);

	@POST
	@Path("/_id/{_id}/item/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "报告-工作/" + DataSet.LIST })
	public List<WorkReportItem> listReportItem(
			@ServiceParam(ServiceParam.CONTEXT_INPUT_OBJECT_ID) @PathParam("_id") ObjectId workReport_id);

	@POST
	@Path("/_id/{_id}/item/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "报告-工作/" + DataSet.COUNT })
	public long countReportItem(
			@ServiceParam(ServiceParam.CONTEXT_INPUT_OBJECT_ID) @PathParam("_id") ObjectId workReport_id);

	@PUT
	@Path("/item/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long updateWorkReportItem(BasicDBObject filterAndUpdate);


	@POST
	@Path("/submitworkreport")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Result> submitWorkReport(List<ObjectId> workReportIds);

	@POST
	@Path("/confirmworkreport/{userId}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Result> confirmWorkReport(List<ObjectId> workReportIds, @PathParam("userId") String userId);
}
