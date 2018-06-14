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
import com.bizvisionsoft.serviceimpl.exception.ServiceException;
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
		if (c("workReport").count(new Document("project_id", workReport.getProject_id())
				.append("period", workReport.getPeriod()).append("type", workReport.getType())) > 0) {
			throw new ServiceException("已经创建报告。");
		}

		Document query = new Document();
		query.append("summary", false);
		query.append("project_id", workReport.getProject_id());
		query.append("actualStart", new Document("$ne", null));
		query.append("$and",
				Arrays.asList(
						new Document("$or",
								Arrays.asList(new Document("chargerId", workReport.getReporter()),
										new Document("assignerId", workReport.getReporter()))),
						new Document("$or", Arrays.asList(new Document("actualFinish", null),
								new Document("actualFinish", workReport.getPeriod())))));

		WorkReport newWorkReport = super.insert(workReport);

		List<WorkReportItem> into = c("work", WorkReportItem.class).aggregate(new JQ("查询工作报告-日报工作")
				.set("project_id", workReport.getProject_id()).set("actualFinish", workReport.getPeriod())
				.set("report_id", newWorkReport.get_id()).set("reportorId", workReport.getReporter()).array())
				.into(new ArrayList<WorkReportItem>());
		if (into.size() == 0) {
			delete(newWorkReport.get_id(), WorkReport.class);
			throw new ServiceException("没有需要填写报告的工作。");
		}
		c(WorkReportItem.class).insertMany(into);
		return getWorkReport(newWorkReport.get_id());
	}

	@Override
	public long delete(ObjectId _id) {
		c("workReportItem").deleteMany(new Document("report_id", _id));
		return delete(_id, WorkReport.class);
	}

	@Override
	public long update(BasicDBObject filterAndUpdate) {
		return update(filterAndUpdate, WorkReport.class);
	}

	@Override
	public List<WorkReport> listInfo(ObjectId _id) {
		return Arrays.asList(getWorkReport(_id));
	}

	@Override
	public List<WorkReportItem> listDailyReportItem(ObjectId report_id) {
		List<? extends Bson> pipeline = new JQ("查询工作报告-工作").set("report_id", report_id).array();
		return c(WorkReportItem.class).aggregate(pipeline).into(new ArrayList<>());
	}

	@Override
	public long countDailyReportItem(ObjectId workReport_id) {
		return count(new BasicDBObject("type", WorkReport.TYPE_DAILY).append("report_id", workReport_id),
				WorkReport.class);
	}

	@Override
	public long updateWorkReportItem(BasicDBObject filterAndUpdate) {
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
		return c(WorkResourceInWorkReport.class).find(workResourceAssignment.getQuery())
				.into(new ArrayList<WorkResourceInWorkReport>());
	}

	@Override
	public long countSubWorkResourceInWorkReport(WorkResourceAssignment workResourceAssignment) {
		return c(WorkResourceInWorkReport.class).count(workResourceAssignment.getQuery());
	}

	@Override
	public WorkReport getWorkReport(ObjectId _id) {
		return c(WorkReport.class).aggregate(new JQ("查询工作报告").set("match", new Document("_id", _id)).array()).first();
	}

}
