package com.bizvisionsoft.service;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.md.service.ServiceParam;
import com.bizvisionsoft.service.model.WorkReport;
import com.bizvisionsoft.service.model.WorkReportItem;
import com.bizvisionsoft.service.model.WorkResourceAssignment;
import com.bizvisionsoft.service.model.WorkResourceInWorkReport;
import com.mongodb.BasicDBObject;

@Path("/workreport")
public interface WorkReportService {

	@POST
	@Path("/userid/{userid}/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "日报/" + DataSet.LIST })
	public List<WorkReport> createWorkReportDailyDataSet(@ServiceParam(ServiceParam.CONDITION) BasicDBObject condition,
			@ServiceParam(ServiceParam.CURRENT_USER_ID) @PathParam("userid") String userid);

	@POST
	@Path("/userid/{userid}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "日报/" + DataSet.COUNT })
	public long countWorkReportDailyDataSet(@ServiceParam(ServiceParam.FILTER) BasicDBObject filter,
			@ServiceParam(ServiceParam.CURRENT_USER_ID) @PathParam("userid") String userid);

	@POST
	@Path("/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "日报/" + DataSet.INSERT })
	public WorkReport insert(WorkReport workReport);

	@DELETE
	@Path("/_id/{_id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "日报/" + DataSet.DELETE })
	public long delete(@ServiceParam(ServiceParam._ID) @PathParam("_id") ObjectId _id);

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
	@DataSet("日报/" + DataSet.UPDATE)
	public long update(BasicDBObject filterAndUpdate);

	@POST
	@Path("/_id/{_id}/item/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "日报-工作/" + DataSet.LIST })
	public List<WorkReportItem> listDailyReportItem(
			@ServiceParam(ServiceParam.CONTEXT_INPUT_OBJECT_ID) @PathParam("_id") ObjectId workReport_id);

	@POST
	@Path("/_id/{_id}/item/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@DataSet({ "日报-工作/" + DataSet.COUNT })
	public long countDailyReportItem(
			@ServiceParam(ServiceParam.CONTEXT_INPUT_OBJECT_ID) @PathParam("_id") ObjectId workReport_id);

	@PUT
	@Path("/item/")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long updateWorkInReport(BasicDBObject filterAndUpdate);

	@POST
	@Path("/_id/{_id}/{work_id}/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<WorkResourceInWorkReport> createWorkResourceInWorkReportDataSet(@PathParam("work_id") ObjectId work_id,
			@PathParam("_id") ObjectId _id);

	@POST
	@Path("/_id/{_id}/{work_id}/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countWorkResourceInWorkReportDataSet(@PathParam("work_id") ObjectId work_id,
			@PathParam("workReport_id") ObjectId workReport_id);

	@POST
	@Path("/subworkresource/ds")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public List<WorkResourceInWorkReport> listSubWorkResourceInWorkReport(
			WorkResourceAssignment workResourceAssignment);

	@POST
	@Path("/subworkresource/count")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public long countSubWorkResourceInWorkReport(WorkResourceAssignment workResourceAssignment);
}
