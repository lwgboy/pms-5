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
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.WorkReport;
import com.bizvisionsoft.service.model.WorkReportItem;
import com.bizvisionsoft.service.model.WorkReportSummary;
import com.mongodb.BasicDBObject;

@Path("/workreport")
public interface WorkReportService {

	@POST
	@Path("/{domain}/userid/{userid}/daily/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "日报/" + DataSet.LIST })
	public List<WorkReport> createWorkReportDailyDataSet(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/userid/{userid}/daily/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "日报/" + DataSet.COUNT })
	public long countWorkReportDailyDataSet(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/userid/{userid}/weekly/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "周报/" + DataSet.LIST })
	public List<WorkReport> createWorkReportWeeklyDataSet(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/userid/{userid}/weekly/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "周报/" + DataSet.COUNT })
	public long countWorkReportWeeklyDataSet(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/userid/{userid}/monthly/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "月报/" + DataSet.LIST })
	public List<WorkReport> createWorkReportMonthlyDataSet(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/userid/{userid}/monthly/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "月报/" + DataSet.COUNT })
	public long countWorkReportMonthlyDataSet(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/userid/{userid}/daily/project/{project_id}/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "项目日报/" + DataSet.LIST })
	public List<WorkReport> createWorkReportProjectDailyDataSet(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid,
			@MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) @PathParam("project_id") ObjectId project_id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/userid/{userid}/daily/project/{project_id}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "项目日报/" + DataSet.COUNT })
	public long countWorkReportProjectDailyDataSet(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid,
			@MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) @PathParam("project_id") ObjectId project_id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/userid/{userid}/weekly/project/{project_id}/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "项目周报/" + DataSet.LIST })
	public List<WorkReport> createWorkReportProjectWeeklyDataSet(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid,
			@MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) @PathParam("project_id") ObjectId project_id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/userid/{userid}/weekly/project/{project_id}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "项目周报/" + DataSet.COUNT })
	public long countWorkReportProjectWeeklyDataSet(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid,
			@MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) @PathParam("project_id") ObjectId project_id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/userid/{userid}/monthly/project/{project_id}/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "项目月报/" + DataSet.LIST })
	public List<WorkReport> createWorkReportProjectMonthlyDataSet(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid,
			@MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) @PathParam("project_id") ObjectId project_id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/userid/{userid}/monthly/project/{project_id}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "项目月报/" + DataSet.COUNT })
	public long countWorkReportProjectMonthlyDataSet(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid,
			@MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) @PathParam("project_id") ObjectId project_id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/userid/{userid}/daily/stage/{stage_id}/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "阶段日报/" + DataSet.LIST })
	@Deprecated
	public List<WorkReport> createWorkReportStageDailyDataSet(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid,
			@MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) @PathParam("stage_id") ObjectId stage_id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/userid/{userid}/daily/stage/{stage_id}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "阶段日报/" + DataSet.COUNT })
	@Deprecated
	public long countWorkReportStageDailyDataSet(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid,
			@MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) @PathParam("stage_id") ObjectId stage_id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/userid/{userid}/weekly/stage/{stage_id}/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "阶段周报/" + DataSet.LIST })
	@Deprecated
	public List<WorkReport> createWorkReportStageWeeklyDataSet(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid,
			@MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) @PathParam("stage_id") ObjectId stage_id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/userid/{userid}/weekly/stage/{stage_id}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "阶段周报/" + DataSet.COUNT })
	@Deprecated
	public long countWorkReportStageWeeklyDataSet(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid,
			@MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) @PathParam("stage_id") ObjectId stage_id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/userid/{userid}/monthly/stage/{stage_id}/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "阶段月报/" + DataSet.LIST })
	@Deprecated
	public List<WorkReport> createWorkReportStageMonthlyDataSet(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid,
			@MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) @PathParam("stage_id") ObjectId stage_id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/userid/{userid}/monthly/stage/{stage_id}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "阶段月报/" + DataSet.COUNT })
	@Deprecated
	public long countWorkReportStageMonthlyDataSet(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid,
			@MethodParam(MethodParam.ROOT_CONTEXT_INPUT_OBJECT_ID) @PathParam("stage_id") ObjectId stage_id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "日报/" + DataSet.INSERT, "周报/" + DataSet.INSERT, "月报/" + DataSet.INSERT, "项目日报/" + DataSet.INSERT, "项目周报/" + DataSet.INSERT,
			"项目月报/" + DataSet.INSERT })
	public WorkReport insert(WorkReport workReport,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@DELETE
	@Path("/{domain}/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "日报/" + DataSet.DELETE, "周报/" + DataSet.DELETE, "月报/" + DataSet.DELETE, "项目日报/" + DataSet.DELETE, "项目周报/" + DataSet.DELETE,
			"项目月报/" + DataSet.DELETE })
	public long delete(@MethodParam(MethodParam._ID) @PathParam("_id") ObjectId _id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@GET
	@Path("/{domain}/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public WorkReport getWorkReport(@PathParam("_id") ObjectId _id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/_id/{_id}/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet("工作报告基本信息面板/list")
	public List<WorkReport> listInfo(@MethodParam(MethodParam.CONTEXT_INPUT_OBJECT_ID) @PathParam("_id") ObjectId _id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "日报/" + DataSet.UPDATE, "周报/" + DataSet.UPDATE, "月报/" + DataSet.UPDATE, "项目日报/" + DataSet.UPDATE, "项目周报/" + DataSet.UPDATE,
			"项目月报/" + DataSet.UPDATE })
	public long update(BasicDBObject filterAndUpdate,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/_id/{_id}/item/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "报告-工作/" + DataSet.LIST })
	public List<WorkReportItem> listReportItem(@MethodParam(MethodParam.CONTEXT_INPUT_OBJECT_ID) @PathParam("_id") ObjectId workReport_id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/_id/{_id}/item/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "报告-工作/" + DataSet.COUNT })
	public long countReportItem(@MethodParam(MethodParam.CONTEXT_INPUT_OBJECT_ID) @PathParam("_id") ObjectId workReport_id,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@PUT
	@Path("/{domain}/item/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long updateWorkReportItem(BasicDBObject filterAndUpdate,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/submitworkreport")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Result> submitWorkReport(List<ObjectId> workReportIds,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/confirmworkreport/{userId}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<Result> confirmWorkReport(List<ObjectId> workReportIds, @PathParam("userId") String userId,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/summary/project/manageBy/{managerId}/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "我管理的项目本周报告摘要/" + DataSet.LIST })
	public List<WorkReportSummary> listWeeklyAdministeredProjectReportSummary(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("managerId") String managerId,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/summary/project/manageBy/{managerId}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "我管理的项目本周报告摘要/" + DataSet.COUNT })
	public long countWeeklyAdministeredProjectReportSummary(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("managerId") String managerId,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/userid/{userid}/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "待确认的报告/" + DataSet.LIST })
	public List<WorkReport> createWorkReportDataSet(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/userid/{userid}/card/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "待确认的项目报告/" + DataSet.LIST })
	public List<Document> listWorkReportToConfirm(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/userid/{userid}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "待确认的报告/" + DataSet.COUNT, "确认报告/budget", "待确认的项目报告/" + DataSet.COUNT })
	public long countWorkReportDataSet(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/userid/{userid}/all/daily/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "日报管理/" + DataSet.LIST })
	public List<WorkReport> createAllWorkReportDailyDataSet(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/userid/{userid}/all/daily/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "日报管理/" + DataSet.COUNT })
	public long countAllWorkReportDailyDataSet(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/userid/{userid}/all/weekly/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "周报管理/" + DataSet.LIST })
	public List<WorkReport> createAllWorkReportWeeklyDataSet(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/userid/{userid}/all/weekly/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "周报管理/" + DataSet.COUNT })
	public long countAllWorkReportWeeklyDataSet(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/userid/{userid}/all/monthly/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "月报管理/" + DataSet.LIST })
	public List<WorkReport> createAllWorkReportMonthlyDataSet(@MethodParam(MethodParam.CONDITION) BasicDBObject condition,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);

	@POST
	@Path("/{domain}/userid/{userid}/all/monthly/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "月报管理/" + DataSet.COUNT })
	public long countALLWorkReportMonthlyDataSet(@MethodParam(MethodParam.FILTER) BasicDBObject filter,
			@MethodParam(MethodParam.CURRENT_USER_ID) @PathParam("userid") String userid,@MethodParam(MethodParam.DOMAIN) @PathParam("domain") String domain);
}
