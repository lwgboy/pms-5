package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.WorkReportService;
import com.bizvisionsoft.service.model.WorkReport;
import com.bizvisionsoft.service.model.WorkReportItem;
import com.bizvisionsoft.service.model.WorkResourceAssignment;
import com.bizvisionsoft.service.model.WorkResourceInWorkReport;
import com.bizvisionsoft.serviceimpl.query.JQ;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Aggregates;

public class WorkReportServiceImpl extends BasicServiceImpl implements WorkReportService {

	@SuppressWarnings("unchecked")
	@Override
	public List<WorkReport> createWorkReportDailyDataSet(BasicDBObject condition, String userid) {
		List<Bson> pipeline = (List<Bson>) new JQ("查询工作报告")
				.set("match", new Document("reporter", userid).append("type", WorkReport.TYPE_DAILY)).array();

		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter != null)
			pipeline.add(Aggregates.match(filter));

		BasicDBObject sort = (BasicDBObject) condition.get("sort");
		if (sort != null)
			pipeline.add(Aggregates.sort(sort));

		Integer skip = (Integer) condition.get("skip");
		if (skip != null)
			pipeline.add(Aggregates.skip(skip));

		Integer limit = (Integer) condition.get("limit");
		if (limit != null)
			pipeline.add(Aggregates.limit(limit));

		return c(WorkReport.class).aggregate(pipeline).into(new ArrayList<WorkReport>());
	}

	@Override
	public long countWorkReportDailyDataSet(BasicDBObject filter, String userid) {
		if (filter == null)
			filter = new BasicDBObject();

		filter.put("reporter", userid);

		filter.put("type", WorkReport.TYPE_DAILY);
		return count(filter, WorkReport.class);
	}

	@Override
	public WorkReport insert(WorkReport workReport) {
		WorkReport newWorkReport = super.insert(workReport);
		List<? extends Bson> pipeline = new JQ("查询工作报告-日报工作").set("workReport_id", newWorkReport.get_id()).array();
		c("work").aggregate(pipeline).forEach((Document doc) -> {
			c("workReportItem").insertOne(
					new Document().append("work_id", doc.get("_id")).append("report_id", newWorkReport.get_id()));
		});
		return listInfo(newWorkReport.get_id()).get(0);
	}

	@Override
	public long delete(ObjectId _id) {
		return delete(_id, WorkReport.class);
	}

	@Override
	public long update(BasicDBObject filterAndUpdate) {
		return update(filterAndUpdate, WorkReport.class);
	}

	@Override
	public List<WorkReport> listInfo(ObjectId _id) {
		return c(WorkReport.class).aggregate(new JQ("查询工作报告").set("match", new Document("_id", _id)).array())
				.into(new ArrayList<>());
	}

	@Override
	public List<WorkReportItem> listDailyReportItem(ObjectId report_id) {
		List<? extends Bson> pipeline = Arrays.asList(
				new Document().append("$match", new Document().append("report_id", report_id)),
				new Document().append("$lookup",
						new Document().append("from", "work").append("localField", "work_id")
								.append("foreignField", "_id").append("as", "work")),
				new Document().append("$unwind",
						new Document().append("path", "$work").append("preserveNullAndEmptyArrays", false)));

		ArrayList<WorkReportItem> result = c(WorkReportItem.class).aggregate(pipeline).into(new ArrayList<>());
		return result;
	}

	@Override
	public long countDailyReportItem(ObjectId workReport_id) {
		return count(new BasicDBObject("type", WorkReport.TYPE_DAILY).append("report_id", workReport_id),
				WorkReport.class);
	}

	@Override
	public long updateWorkInReport(BasicDBObject filterAndUpdate) {
		return update(filterAndUpdate, WorkReportItem.class);
	}

	@Override
	public List<WorkResourceInWorkReport> createWorkResourceInWorkReportDataSet(ObjectId work_id,
			ObjectId workReport_id) {
		List<? extends Bson> pipeline = new JQ("查询工作报告-资源用量")
				.set("match", new Document("work_id", work_id).append("workReport_id", workReport_id)).array();
		ArrayList<WorkResourceInWorkReport> into = c(WorkResourceInWorkReport.class).aggregate(pipeline)
				.into(new ArrayList<WorkResourceInWorkReport>());
		return into;
	}

	@Override
	public long countWorkResourceInWorkReportDataSet(ObjectId work_id, ObjectId workReport_id) {
		List<? extends Bson> pipeline = new JQ("查询工作报告-资源用量")
				.set("match", new Document("work_id", work_id).append("workReport_id", workReport_id)).array();
		return c(WorkResourceInWorkReport.class).aggregate(pipeline).into(new ArrayList<WorkResourceInWorkReport>())
				.size();
	}

	@Override
	public List<WorkResourceInWorkReport> listSubWorkResourceInWorkReport(
			WorkResourceAssignment workResourceAssignment) {
		// TODO Auto-generated method stub
		return c(WorkResourceInWorkReport.class).find(workResourceAssignment.getQuery())
				.into(new ArrayList<WorkResourceInWorkReport>());
	}

	@Override
	public long countSubWorkResourceInWorkReport(WorkResourceAssignment workResourceAssignment) {
		// TODO Auto-generated method stub
		return c(WorkResourceInWorkReport.class).count(workResourceAssignment.getQuery());
	}

}
