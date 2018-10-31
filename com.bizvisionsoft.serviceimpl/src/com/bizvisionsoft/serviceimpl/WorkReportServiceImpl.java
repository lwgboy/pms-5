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
import com.bizvisionsoft.service.model.Role;
import com.bizvisionsoft.service.model.WorkReport;
import com.bizvisionsoft.service.model.WorkReportItem;
import com.bizvisionsoft.service.model.WorkReportSummary;
import com.bizvisionsoft.serviceimpl.exception.ServiceException;
import com.bizvisionsoft.serviceimpl.query.JQ;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.result.UpdateResult;

public class WorkReportServiceImpl extends BasicServiceImpl implements WorkReportService {

	private List<WorkReport> query(BasicDBObject condition, List<Bson> pipeline) {
		if (pipeline == null)
			pipeline = new ArrayList<Bson>();

		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter != null)
			pipeline.add(Aggregates.match(filter));
		// yangjun 2018/10/31
		pipeline.add(Aggregates.sort(new Document("period", -1).append("_id", -1)));

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
		return query(condition, new JQ("��ѯ-����")
				.set("match", new Document("type", WorkReport.TYPE_DAILY).append("reporter", userid)).array());
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
		return query(condition, (List<Bson>) new JQ("��ѯ-����")
				.set("match", new Document("reporter", userid).append("type", WorkReport.TYPE_WEEKLY)).array());
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
		return query(condition, (List<Bson>) new JQ("��ѯ-����")
				.set("match", new Document("reporter", userid).append("type", WorkReport.TYPE_MONTHLY)).array());
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
		return query(condition,
				(List<Bson>) new JQ("��ѯ-����")
						.set("match",
								new Document("type", WorkReport.TYPE_DAILY).append("project_id", project_id)
										.append("status", new BasicDBObject("$in",
												Arrays.asList(WorkReport.STATUS_SUBMIT, WorkReport.STATUS_CONFIRM))))
						.array());
	}

	@Override
	public long countWorkReportProjectDailyDataSet(BasicDBObject filter, String userid, ObjectId project_id) {
		if (filter == null)
			filter = new BasicDBObject();

		filter.put("project_id", project_id);
		filter.put("type", WorkReport.TYPE_DAILY);
		filter.put("status",
				new BasicDBObject("$in", Arrays.asList(WorkReport.STATUS_SUBMIT, WorkReport.STATUS_CONFIRM)));
		return count(filter, WorkReport.class);
	}

	@Override
	public List<WorkReport> createWorkReportProjectWeeklyDataSet(BasicDBObject condition, String userid,
			ObjectId project_id) {
		return query(condition,
				(List<Bson>) new JQ("��ѯ-����")
						.set("match",
								new Document("type", WorkReport.TYPE_WEEKLY).append("project_id", project_id)
										.append("status", new BasicDBObject("$in",
												Arrays.asList(WorkReport.STATUS_SUBMIT, WorkReport.STATUS_CONFIRM))))
						.array());
	}

	@Override
	public long countWorkReportProjectWeeklyDataSet(BasicDBObject filter, String userid, ObjectId project_id) {
		if (filter == null)
			filter = new BasicDBObject();

		filter.put("project_id", project_id);
		filter.put("type", WorkReport.TYPE_WEEKLY);
		filter.put("status",
				new BasicDBObject("$in", Arrays.asList(WorkReport.STATUS_SUBMIT, WorkReport.STATUS_CONFIRM)));
		return count(filter, WorkReport.class);
	}

	@Override
	public List<WorkReport> createWorkReportProjectMonthlyDataSet(BasicDBObject condition, String userid,
			ObjectId project_id) {
		return query(condition,
				(List<Bson>) new JQ("��ѯ-����")
						.set("match",
								new Document("type", WorkReport.TYPE_MONTHLY).append("project_id", project_id)
										.append("status", new BasicDBObject("$in",
												Arrays.asList(WorkReport.STATUS_SUBMIT, WorkReport.STATUS_CONFIRM))))
						.array());
	}

	@Override
	public long countWorkReportProjectMonthlyDataSet(BasicDBObject filter, String userid, ObjectId project_id) {
		if (filter == null)
			filter = new BasicDBObject();

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

		return query(condition,
				(List<Bson>) new JQ("��ѯ-����")
						.set("match",
								new Document("reporter", userid).append("type", WorkReport.TYPE_DAILY)
										.append("project_id", project_id)
										.append("status", new BasicDBObject("$in",
												Arrays.asList(WorkReport.STATUS_SUBMIT, WorkReport.STATUS_CONFIRM))))
						.array());
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
		return query(condition,
				(List<Bson>) new JQ("��ѯ-����")
						.set("match",
								new Document("reporter", userid).append("type", WorkReport.TYPE_WEEKLY)
										.append("project_id", project_id)
										.append("status", new BasicDBObject("$in",
												Arrays.asList(WorkReport.STATUS_SUBMIT, WorkReport.STATUS_CONFIRM))))
						.array());
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
		return query(condition,
				(List<Bson>) new JQ("��ѯ-����")
						.set("match",
								new Document("reporter", userid).append("type", WorkReport.TYPE_MONTHLY)
										.append("project_id", project_id)
										.append("status", new BasicDBObject("$in",
												Arrays.asList(WorkReport.STATUS_SUBMIT, WorkReport.STATUS_CONFIRM))))
						.array());
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
		if (c("workReport").countDocuments(
				new Document("project_id", workReport.getProject_id()).append("period", workReport.getPeriod())
						.append("type", workReport.getType()).append("reporter", workReport.getReporter())) > 0) {
			throw new ServiceException("�Ѿ��������档");
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
				.aggregate(new JQ("��ѯ-������-��Ŀ").set("project_id", workReport.getProject_id())
						.set("actualFinish", workReport.getPeriod()).set("report_id", newWorkReport.get_id())
						.set("reportorId", workReport.getReporter()).set("chargerid", workReport.getReporter()).array())
				.into(new ArrayList<WorkReportItem>());
		if (into.size() == 0) {
			delete(newWorkReport.get_id(), WorkReport.class);
			throw new ServiceException("û����Ҫ��д����Ĺ�����");
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
		List<? extends Bson> pipeline = new JQ("��ѯ-������").set("report_id", report_id).array();
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
		return c(WorkReport.class).aggregate(new JQ("��ѯ-����").set("match", match).array()).first();
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
			result.add(Result.updateFailure("û�������ύ�����ı��档"));
			return result;
		}

		return result;
	}

	private List<Result> submitWorkReportCheck(List<ObjectId> workReportIds) {
		List<Result> result = new ArrayList<Result>();
		Document doc = new Document("statement", null).append("report_id", new Document("$in", workReportIds));
		long count = c(WorkReportItem.class).countDocuments(doc);
		if (count > 0) {
			result.add(Result.submitWorkReportError("����Ϊ��д�������Ĺ���"));
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
			result.add(Result.updateFailure("û������ȷ�������ı��档"));
			return result;
		}

		ur = c(WorkReportItem.class).updateMany(new Document("report_id", new Document("$in", workReportIds)),
				new Document("$set", new Document("confirmed", true)));

		// ���¹���Ԥ�����ʱ��
		c(WorkReportItem.class).find(new Document("report_id", new Document("$in", workReportIds)))
				.forEach((WorkReportItem wri) -> {
					Date estFinish = wri.getEstimatedFinish();
					if (estFinish != null) {
						Double progress = null;
						double d = estFinish.getTime() - wri.getActualStart().getTime();
						double d1 = wri.get_id().getDate().getTime() - wri.getActualStart().getTime();
						if (d > 0 && d1 > 0) {
							progress = (double) Math.round(d1 * 10000 / d) / 10000;
						}
						c("work").updateOne(new Document("_id", wri.getWork_id()), new Document("$set",
								new Document("estimatedFinish", estFinish).append("progress", progress)));

					}
				});

		List<ObjectId> itemIds = c(WorkReportItem.class)
				.distinct("_id", new Document("report_id", new Document("$in", workReportIds)), ObjectId.class)
				.into(new ArrayList<ObjectId>());
		// ���Ʊ����е���Դ������resourceActual������
		c("workReportResourceActual").find(new Document("workReportItemId", new Document("$in", itemIds)))
				.forEach((Document doc) -> {
					Document first = c("resourceActual").find(new Document("id", doc.get("id"))
							.append("work_id", doc.get("work_id")).append("usedHumanResId", doc.get("usedHumanResId"))
							.append("usedEquipResId", doc.get("usedEquipResId"))
							.append("usedTypedResId", doc.get("usedTypedResId"))
							.append("resTypeId", doc.get("resTypeId"))).first();
					if (first != null) {
						Object actualBasicQty = first.get("actualBasicQty");
						if (actualBasicQty != null && doc.get("actualBasicQty") != null) {
							actualBasicQty = ((Number) actualBasicQty).doubleValue() + doc.getDouble("actualBasicQty");
						} else if (actualBasicQty == null && doc.get("actualBasicQty") != null) {
							actualBasicQty = doc.get("actualBasicQty");
						}

						Object actualOverTimeQty = first.get("actualOverTimeQty");
						if (actualOverTimeQty != null && doc.get("actualOverTimeQty") != null) {
							actualOverTimeQty = ((Number) actualOverTimeQty).doubleValue()
									+ doc.getDouble("actualOverTimeQty");
						} else if (actualOverTimeQty == null && doc.get("actualOverTimeQty") != null) {
							actualOverTimeQty = doc.get("actualOverTimeQty");
						}

						c("resourceActual").updateOne(new Document("_id", first.get("_id")),
								new Document("$set", new Document("actualBasicQty", actualBasicQty)
										.append("actualOverTimeQty", actualOverTimeQty)));
					} else {
						doc.remove("workReportItemId");
						c("resourceActual").insertOne(doc);
					}
				});

		return result;
	}

	private List<Result> confirmWorkReportCheck(List<ObjectId> workReportIds) {
		// TODO ����Ƿ���Խ���ȷ��
		return new ArrayList<Result>();
	}

	@Override
	public List<WorkReportSummary> listWeeklyAdministeredProjectReportSummary(BasicDBObject condition,
			String managerId) {
		List<ObjectId> ids = getAdministratedProjects(managerId);
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);// ����һ
		Date to = cal.getTime();
		cal.add(Calendar.DATE, -7);// ����һ
		Date from = cal.getTime();
		List<Bson> pipeline = new JQ("��ѯ-����-�ܱ�").set("project_id", new Document("$in", ids)).set("from", from)
				.set("to", to).array();

		if (condition != null) {
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

		}

		return c("workReport").aggregate(pipeline, WorkReportSummary.class).into(new ArrayList<>());
	}

	@Override
	public long countWeeklyAdministeredProjectReportSummary(BasicDBObject filter, String managerId) {
		List<ObjectId> ids = getAdministratedProjects(managerId);
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);// ����һ
		Date to = cal.getTime();
		cal.add(Calendar.DATE, -7);// ����һ
		Date from = cal.getTime();

		if (filter == null) {
			filter = new BasicDBObject();
		}

		return c("workReport")
				.countDocuments(filter.append("status", "ȷ��").append("project_id", new Document("$in", ids))
						.append("reportDate", new Document("$gte", from).append("$lte", to)));
	}

	@Override
	public List<WorkReport> createWorkReportDataSet(BasicDBObject condition, String userid) {
		List<ObjectId> projectIds = c("project").distinct("_id", new Document("pmId", userid), ObjectId.class)
				.into(new ArrayList<ObjectId>());
		return query(condition,
				(List<Bson>) new JQ("��ѯ-����").set("match", new Document("status", WorkReport.STATUS_SUBMIT)
						.append("project_id", new BasicDBObject("$in", projectIds))).array());
	}

	@Override
	public long countWorkReportDataSet(BasicDBObject filter, String userid) {
		if (filter == null)
			filter = new BasicDBObject();

		List<ObjectId> projectIds = c("project").distinct("_id", new Document("pmId", userid), ObjectId.class)
				.into(new ArrayList<ObjectId>());

		filter.put("status", WorkReport.STATUS_SUBMIT);
		filter.put("project_id", new BasicDBObject("$in", projectIds));

		return count(filter, WorkReport.class);
	}

	@Override
	public List<WorkReport> createAllWorkReportDailyDataSet(BasicDBObject condition, String userid) {
		List<Bson> pipeline = new ArrayList<Bson>();

		appendQueryUserInProjectPMO(userid, pipeline);

		pipeline.addAll(new JQ("��ѯ-����")
				.set("match", new Document("type", WorkReport.TYPE_DAILY).append("status", WorkReport.STATUS_CONFIRM))
				.array());
		return query(condition, pipeline);
	}

	@Override
	public long countAllWorkReportDailyDataSet(BasicDBObject filter, String userid) {
		// ����û�������Ŀ�ܼ�Ȩ��ʱ����ʾȫ����ȷ�ϵ���Ŀ�ձ�
		if (checkUserRoles(userid, Role.SYS_ROLE_PD_ID))
			return c("workReport").countDocuments(
					new BasicDBObject("type", WorkReport.TYPE_DAILY).append("status", WorkReport.STATUS_CONFIRM));
		else {
			// ����ֻ��ʾ��ǰ�û�����ĿPMO�Ŷ��е���Ŀ����ȷ�ϵ���Ŀ�ձ�
			List<Bson> pipeline = new JQ("��ѯ-��ĿPMO��Ա").set("scopeIdName", "$project_id").set("userId", userid).array();
			pipeline.add(Aggregates
					.match(new Document("type", WorkReport.TYPE_DAILY).append("status", WorkReport.STATUS_CONFIRM)));
			return c("workReport").aggregate(pipeline).into(new ArrayList<>()).size();
		}
	}

	@Override
	public List<WorkReport> createAllWorkReportWeeklyDataSet(BasicDBObject condition, String userid) {
		List<Bson> pipeline = new ArrayList<Bson>();
		appendQueryUserInProjectPMO(userid, pipeline);

		pipeline.addAll(new JQ("��ѯ-����")
				.set("match", new Document("type", WorkReport.TYPE_WEEKLY).append("status", WorkReport.STATUS_CONFIRM))
				.array());
		return query(condition, pipeline);
	}

	@Override
	public long countAllWorkReportWeeklyDataSet(BasicDBObject filter, String userid) {
		// ����û�������Ŀ�ܼ�Ȩ��ʱ����ʾȫ����ȷ�ϵ���Ŀ�ܱ�
		if (checkUserRoles(userid, Role.SYS_ROLE_PD_ID))
			return c("workReport").countDocuments(
					new BasicDBObject("type", WorkReport.TYPE_WEEKLY).append("status", WorkReport.STATUS_CONFIRM));
		else {
			// ����ֻ��ʾ��ǰ�û�����ĿPMO�Ŷ��е���Ŀ����ȷ�ϵ���Ŀ�ܱ�
			List<Bson> pipeline = new JQ("��ѯ-��ĿPMO��Ա").set("scopeIdName", "$project_id").set("userId", userid).array();
			pipeline.add(Aggregates
					.match(new Document("type", WorkReport.TYPE_WEEKLY).append("status", WorkReport.STATUS_CONFIRM)));
			return c("workReport").aggregate(pipeline).into(new ArrayList<>()).size();
		}
	}

	@Override
	public List<WorkReport> createAllWorkReportMonthlyDataSet(BasicDBObject condition, String userid) {
		List<Bson> pipeline = new ArrayList<Bson>();
		// ��ǰ�û�������ʾȫ���ձ�������ǰ�û�������Ŀ����Ա��ɫʱ��ֻ��ʾ����Ϊ��Ŀ�Ŷӳ�Ա�ı���
		appendQueryUserInProjectPMO(userid, pipeline);

		pipeline.addAll(new JQ("��ѯ-����")
				.set("match", new Document("type", WorkReport.TYPE_MONTHLY).append("status", WorkReport.STATUS_CONFIRM))
				.array());
		return query(condition, pipeline);
	}

	@Override
	public long countALLWorkReportMonthlyDataSet(BasicDBObject filter, String userid) {
		// ����û�������Ŀ�ܼ�Ȩ��ʱ����ʾȫ����ȷ�ϵ���Ŀ�±�
		if (checkUserRoles(userid, Role.SYS_ROLE_PD_ID))
			return c("workReport").countDocuments(
					new BasicDBObject("type", WorkReport.TYPE_MONTHLY).append("status", WorkReport.STATUS_CONFIRM));
		else {
			// ����ֻ��ʾ��ǰ�û�����ĿPMO�Ŷ��е���Ŀ����ȷ�ϵ���Ŀ�±�
			List<Bson> pipeline = new JQ("��ѯ-��ĿPMO��Ա").set("scopeIdName", "$project_id").set("userId", userid).array();
			pipeline.add(Aggregates
					.match(new Document("type", WorkReport.TYPE_MONTHLY).append("status", WorkReport.STATUS_CONFIRM)));
			return c("workReport").aggregate(pipeline).into(new ArrayList<>()).size();
		}
	}

	/**
	 * ��ӻ�ȡ��Ŀʱ����ǰ�û�������Ŀ�ܼ�ʱ��ֻ��ȡ��ǰ�û�����ĿPMO�Ŷ��е���Ŀ�Ĳ�ѯ
	 * 
	 * @param userid
	 * @param pipeline
	 */
	private void appendQueryUserInProjectPMO(String userid, List<Bson> pipeline) {
		if (!checkUserRoles(userid, Role.SYS_ROLE_PD_ID))
			pipeline.addAll(new JQ("��ѯ-��ĿPMO��Ա").set("scopeIdName", "$project_id").set("userId", userid).array());
	}

}
