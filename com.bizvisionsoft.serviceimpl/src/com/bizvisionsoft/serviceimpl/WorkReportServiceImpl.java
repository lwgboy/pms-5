package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.WorkReportService;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.WorkReport;
import com.bizvisionsoft.service.model.WorkReportItem;
import com.bizvisionsoft.serviceimpl.exception.ServiceException;
import com.bizvisionsoft.serviceimpl.query.JQ;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.result.UpdateResult;

public class WorkReportServiceImpl extends BasicServiceImpl implements WorkReportService {

	@SuppressWarnings("unchecked")
	private List<WorkReport> query(BasicDBObject condition, Document match) {
		List<Bson> pipeline = (List<Bson>) new JQ("查询工作报告").set("match", match).array();

		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter != null)
			pipeline.add(Aggregates.match(filter));

		pipeline.add(Aggregates.sort(new Document("period", -1)));

		Integer skip = (Integer) condition.get("skip");
		if (skip != null)
			pipeline.add(Aggregates.skip(skip));

		Integer limit = (Integer) condition.get("limit");
		if (limit != null)
			pipeline.add(Aggregates.limit(limit));

		return c(WorkReport.class).aggregate(pipeline).into(new ArrayList<WorkReport>());
	}

	@Override
	public List<WorkReport> createWorkReportDailyDataSet(BasicDBObject condition, String userid) {
		Document match = new Document("reporter", userid).append("type", WorkReport.TYPE_DAILY);
		return query(condition, match);
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
	public List<WorkReport> createWorkReportWeeklyDataSet(BasicDBObject condition, String userid) {
		Document match = new Document("reporter", userid).append("type", WorkReport.TYPE_WEEKLY);
		return query(condition, match);
	}

	@Override
	public long countWorkReportWeeklyDataSet(BasicDBObject filter, String userid) {
		if (filter == null)
			filter = new BasicDBObject();

		filter.put("reporter", userid);

		filter.put("type", WorkReport.TYPE_WEEKLY);
		return count(filter, WorkReport.class);
	}

	@Override
	public List<WorkReport> createWorkReportMonthlyDataSet(BasicDBObject condition, String userid) {
		Document match = new Document("reporter", userid).append("type", WorkReport.TYPE_MONTHLY);
		return query(condition, match);
	}

	@Override
	public long countWorkReportMonthlyDataSet(BasicDBObject filter, String userid) {
		if (filter == null)
			filter = new BasicDBObject();

		filter.put("reporter", userid);

		filter.put("type", WorkReport.TYPE_MONTHLY);
		return count(filter, WorkReport.class);
	}

	@Override
	public List<WorkReport> createWorkReportProjectDailyDataSet(BasicDBObject condition, String userid,
			ObjectId project_id) {
		Document match = new Document("reporter", userid).append("type", WorkReport.TYPE_DAILY)
				.append("project_id", project_id).append("status",
						new BasicDBObject("$in", Arrays.asList(WorkReport.STATUS_SUBMIT, WorkReport.STATUS_CONFIRM)));
		return query(condition, match);
	}

	@Override
	public long countWorkReportProjectDailyDataSet(BasicDBObject filter, String userid, ObjectId project_id) {
		if (filter == null)
			filter = new BasicDBObject();

		filter.put("reporter", userid);
		filter.put("project_id", project_id);
		filter.put("type", WorkReport.TYPE_DAILY);
		filter.put("status",
				new BasicDBObject("$in", Arrays.asList(WorkReport.STATUS_SUBMIT, WorkReport.STATUS_CONFIRM)));
		return count(filter, WorkReport.class);
	}

	@Override
	public List<WorkReport> createWorkReportProjectWeeklyDataSet(BasicDBObject condition, String userid,
			ObjectId project_id) {
		Document match = new Document("reporter", userid).append("type", WorkReport.TYPE_WEEKLY)
				.append("project_id", project_id).append("status",
						new BasicDBObject("$in", Arrays.asList(WorkReport.STATUS_SUBMIT, WorkReport.STATUS_CONFIRM)));
		return query(condition, match);
	}

	@Override
	public long countWorkReportProjectWeeklyDataSet(BasicDBObject filter, String userid, ObjectId project_id) {
		if (filter == null)
			filter = new BasicDBObject();

		filter.put("reporter", userid);
		filter.put("project_id", project_id);
		filter.put("type", WorkReport.TYPE_WEEKLY);
		filter.put("status",
				new BasicDBObject("$in", Arrays.asList(WorkReport.STATUS_SUBMIT, WorkReport.STATUS_CONFIRM)));
		return count(filter, WorkReport.class);
	}

	@Override
	public List<WorkReport> createWorkReportProjectMonthlyDataSet(BasicDBObject condition, String userid,
			ObjectId project_id) {
		Document match = new Document("reporter", userid).append("type", WorkReport.TYPE_MONTHLY)
				.append("project_id", project_id).append("status",
						new BasicDBObject("$in", Arrays.asList(WorkReport.STATUS_SUBMIT, WorkReport.STATUS_CONFIRM)));
		return query(condition, match);
	}

	@Override
	public long countWorkReportProjectMonthlyDataSet(BasicDBObject filter, String userid, ObjectId project_id) {
		if (filter == null)
			filter = new BasicDBObject();

		filter.put("reporter", userid);
		filter.put("project_id", project_id);
		filter.put("type", WorkReport.TYPE_MONTHLY);
		filter.put("status",
				new BasicDBObject("$in", Arrays.asList(WorkReport.STATUS_SUBMIT, WorkReport.STATUS_CONFIRM)));
		return count(filter, WorkReport.class);
	}

	@Override
	public List<WorkReport> createWorkReportStageDailyDataSet(BasicDBObject condition, String userid,
			ObjectId stage_id) {
		ObjectId project_id = c("work").distinct("project_id", new Document("_id", stage_id), ObjectId.class).first();

		Document match = new Document("reporter", userid).append("type", WorkReport.TYPE_DAILY)
				.append("project_id", project_id).append("status",
						new BasicDBObject("$in", Arrays.asList(WorkReport.STATUS_SUBMIT, WorkReport.STATUS_CONFIRM)));
		return query(condition, match);
	}

	@Override
	public long countWorkReportStageDailyDataSet(BasicDBObject filter, String userid, ObjectId stage_id) {
		if (filter == null)
			filter = new BasicDBObject();

		ObjectId project_id = c("work").distinct("project_id", new Document("_id", stage_id), ObjectId.class).first();
		filter.put("reporter", userid);
		filter.put("project_id", project_id);
		filter.put("type", WorkReport.TYPE_DAILY);
		filter.put("status",
				new BasicDBObject("$in", Arrays.asList(WorkReport.STATUS_SUBMIT, WorkReport.STATUS_CONFIRM)));
		return count(filter, WorkReport.class);
	}

	@Override
	public List<WorkReport> createWorkReportStageWeeklyDataSet(BasicDBObject condition, String userid,
			ObjectId stage_id) {
		ObjectId project_id = c("work").distinct("project_id", new Document("_id", stage_id), ObjectId.class).first();

		Document match = new Document("reporter", userid).append("type", WorkReport.TYPE_WEEKLY)
				.append("project_id", project_id).append("status",
						new BasicDBObject("$in", Arrays.asList(WorkReport.STATUS_SUBMIT, WorkReport.STATUS_CONFIRM)));
		return query(condition, match);
	}

	@Override
	public long countWorkReportStageWeeklyDataSet(BasicDBObject filter, String userid, ObjectId stage_id) {
		if (filter == null)
			filter = new BasicDBObject();

		ObjectId project_id = c("work").distinct("project_id", new Document("_id", stage_id), ObjectId.class).first();
		filter.put("reporter", userid);
		filter.put("project_id", project_id);
		filter.put("type", WorkReport.TYPE_WEEKLY);
		filter.put("status",
				new BasicDBObject("$in", Arrays.asList(WorkReport.STATUS_SUBMIT, WorkReport.STATUS_CONFIRM)));
		return count(filter, WorkReport.class);
	}

	@Override
	public List<WorkReport> createWorkReportStageMonthlyDataSet(BasicDBObject condition, String userid,
			ObjectId stage_id) {
		ObjectId project_id = c("work").distinct("project_id", new Document("_id", stage_id), ObjectId.class).first();

		Document match = new Document("reporter", userid).append("type", WorkReport.TYPE_MONTHLY)
				.append("project_id", project_id).append("status",
						new BasicDBObject("$in", Arrays.asList(WorkReport.STATUS_SUBMIT, WorkReport.STATUS_CONFIRM)));
		return query(condition, match);
	}

	@Override
	public long countWorkReportStageMonthlyDataSet(BasicDBObject filter, String userid, ObjectId stage_id) {
		if (filter == null)
			filter = new BasicDBObject();

		ObjectId project_id = c("work").distinct("project_id", new Document("_id", stage_id), ObjectId.class).first();
		filter.put("reporter", userid);
		filter.put("project_id", project_id);
		filter.put("type", WorkReport.TYPE_MONTHLY);
		filter.put("status",
				new BasicDBObject("$in", Arrays.asList(WorkReport.STATUS_SUBMIT, WorkReport.STATUS_CONFIRM)));
		return count(filter, WorkReport.class);
	}

	@Override
	public WorkReport insert(WorkReport workReport) {
		if (c("workReport").count(new Document("project_id", workReport.getProject_id())
				.append("period", workReport.getPeriod()).append("type", workReport.getType())) > 0) {
			throw new ServiceException("已经创建报告。");
		}
		Date period = workReport.getPeriod();

		Document query = new Document();
		query.append("summary", false);
		query.append("project_id", workReport.getProject_id());
		query.append("actualStart", new Document("$ne", null));
		if (WorkReport.TYPE_DAILY.equals(workReport.getType())) {
			query.append("$and", Arrays.asList(
					new Document("$or",
							Arrays.asList(new Document("chargerId", workReport.getReporter()),
									new Document("assignerId", workReport.getReporter()))),
					new Document("$or",
							Arrays.asList(new Document("actualFinish", null), new Document("actualFinish", period)))));
		} else if (WorkReport.TYPE_WEEKLY.equals(workReport.getType())) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(period);
			cal.add(Calendar.DAY_OF_MONTH, 8);
			Date end = cal.getTime();
			query.append("$and", Arrays.asList(
					new Document("$or",
							Arrays.asList(new Document("chargerId", workReport.getReporter()),
									new Document("assignerId", workReport.getReporter()))),

					new Document("$or",
							Arrays.asList(new Document("actualFinish", null), new Document("$and", Arrays.asList(

									new Document("actualFinish", new Document("$gte", period)),
									new Document("actualFinish", new Document("$lt", end))))))

			));
		} else if (WorkReport.TYPE_MONTHLY.equals(workReport.getType())) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(period);
			cal.add(Calendar.MONTH, 1);
			Date end = cal.getTime();
			query.append("$and", Arrays.asList(
					new Document("$or",
							Arrays.asList(new Document("chargerId", workReport.getReporter()),
									new Document("assignerId", workReport.getReporter()))),
					new Document("$or",
							Arrays.asList(new Document("actualFinish", null), new Document("$and", Arrays.asList(

									new Document("actualFinish", new Document("$gte", period)),
									new Document("actualFinish", new Document("$lt", end))))))

			));
		}

		WorkReport newWorkReport = super.insert(workReport);

		List<WorkReportItem> into = c("work", WorkReportItem.class)
				.aggregate(new JQ("查询工作报告-工作").set("project_id", workReport.getProject_id())
						.set("actualFinish", workReport.getPeriod()).set("report_id", newWorkReport.get_id())
						.set("reportorId", workReport.getReporter()).set("chargerid", workReport.getReporter()).array())
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
	public List<WorkReportItem> listReportItem(ObjectId report_id) {
		List<? extends Bson> pipeline = new JQ("查询工作报告-报告工作").set("report_id", report_id).array();
		return c(WorkReportItem.class).aggregate(pipeline).into(new ArrayList<WorkReportItem>());
	}

	@Override
	public long countReportItem(ObjectId workReport_id) {
		return count(new BasicDBObject("report_id", workReport_id), WorkReportItem.class);
	}

	@Override
	public long updateWorkReportItem(BasicDBObject filterAndUpdate) {
		return update(filterAndUpdate, WorkReportItem.class);
	}

	@Override
	public WorkReport getWorkReport(ObjectId _id) {
		Document match = new Document("_id", _id);
		return c(WorkReport.class).aggregate(new JQ("查询工作报告").set("match", match).array()).first();
	}

	@Override
	public List<Result> submitWorkReport(List<ObjectId> workReportIds) {
		List<Result> result = submitWorkReportCheck(workReportIds);
		if (!result.isEmpty()) {
			return result;
		}

		UpdateResult ur = c(WorkReport.class).updateMany(new Document("_id", new Document("$in", workReportIds)),
				new Document("$set",
						new Document("submitDate", new Date()).append("status", WorkReport.STATUS_SUBMIT)));
		if (ur.getModifiedCount() == 0) {
			result.add(Result.updateFailure("没有满足提交条件的报告。"));
			return result;
		}

		return result;
	}

	private List<Result> submitWorkReportCheck(List<ObjectId> workReportIds) {
		List<Result> result = new ArrayList<Result>();
		Document doc = new Document("statement", null).append("report_id", new Document("$in", workReportIds));
		long count = c(WorkReportItem.class).count(doc);
		if (count > 0) {
			result.add(Result.submitWorkReportError("存在为填写完成情况的工作"));
		}
		return result;
	}

	@Override
	public List<Result> confirmWorkReport(List<ObjectId> workReportIds, String userId) {
		List<Result> result = confirmWorkReportCheck(workReportIds);
		if (!result.isEmpty()) {
			return result;
		}

		UpdateResult ur = c(WorkReport.class).updateMany(new Document("_id", new Document("$in", workReportIds)),
				new Document("$set", new Document("verifyDate", new Date()).append("status", WorkReport.STATUS_CONFIRM)
						.append("verifier", userId)));
		if (ur.getModifiedCount() == 0) {
			result.add(Result.updateFailure("没有满足确认条件的报告。"));
			return result;
		}

		ur = c(WorkReportItem.class).updateMany(new Document("report_id", new Document("$in", workReportIds)),
				new Document("$set", new Document("confirmed", true)));

		// TODO 更新工作预计完成时间
		c(WorkReportItem.class).find(new Document("report_id", new Document("$in", workReportIds)))
				.forEach((WorkReportItem wri) -> {
					if (wri.getEstimatedFinish() != null) {
						c("work").updateOne(new Document("_id", wri.getWork_id()),
								new Document("$set", new Document("estimatedFinish", wri.getEstimatedFinish())));
					}
				});

		return result;
	}

	private List<Result> confirmWorkReportCheck(List<ObjectId> workReportIds) {
		// TODO 检查是否可以进行确认
		return new ArrayList<Result>();
	}

}
