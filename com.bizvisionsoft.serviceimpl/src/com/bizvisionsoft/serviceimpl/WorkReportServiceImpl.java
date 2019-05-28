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
import com.bizvisionsoft.service.common.Domain;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.Role;
import com.bizvisionsoft.service.model.WorkReport;
import com.bizvisionsoft.service.model.WorkReportItem;
import com.bizvisionsoft.service.model.WorkReportSummary;
import com.bizvisionsoft.serviceimpl.exception.ServiceException;
import com.bizvisionsoft.serviceimpl.renderer.ReportRenderer;
import com.mongodb.BasicDBObject;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.result.UpdateResult;

public class WorkReportServiceImpl extends BasicServiceImpl implements WorkReportService {

	private List<WorkReport> query(BasicDBObject condition, List<Bson> pipeline,String domain){
		return interateReport(condition, pipeline, domain).into(new ArrayList<WorkReport>());
	}

	private AggregateIterable<WorkReport> interateReport(BasicDBObject condition, List<Bson> pipeline,String domain){
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

		appendUserInfo(pipeline, "reporter", "reporterInfo", domain);

		appendUserInfo(pipeline, "verifier", "verifierInfo", domain);
		
		appendUserInfo(pipeline, "pmId", "pmInfo", domain);

		AggregateIterable<WorkReport> iter = c(WorkReport.class,domain).aggregate(pipeline);
		return iter;
	}

	@Override
	public List<WorkReport> createWorkReportDailyDataSet(BasicDBObject condition, String userid,String domain){
		return query(condition,
				Domain.getJQ(domain, "查询-报告").set("match", new Document("type", WorkReport.TYPE_DAILY).append("reporter", userid)).array(), domain);
	}

	@Override
	public long countWorkReportDailyDataSet(BasicDBObject filter, String userid,String domain){
		if (filter == null)
			filter = new BasicDBObject();

		filter.put("reporter", userid);

		filter.put("type", WorkReport.TYPE_DAILY);
		return count(filter, WorkReport.class,domain);
	}

	@Override
	public List<WorkReport> createWorkReportWeeklyDataSet(BasicDBObject condition, String userid,String domain){
		return query(condition,
				(List<Bson>) Domain.getJQ(domain, "查询-报告").set("match", new Document("reporter", userid).append("type", WorkReport.TYPE_WEEKLY)).array(), domain);
	}

	@Override
	public long countWorkReportWeeklyDataSet(BasicDBObject filter, String userid,String domain){
		if (filter == null)
			filter = new BasicDBObject();

		filter.put("reporter", userid);

		filter.put("type", WorkReport.TYPE_WEEKLY);
		return count(filter, WorkReport.class,domain);
	}

	@Override
	public List<WorkReport> createWorkReportMonthlyDataSet(BasicDBObject condition, String userid,String domain){
		return query(condition, (List<Bson>) Domain.getJQ(domain, "查询-报告")
				.set("match", new Document("reporter", userid).append("type", WorkReport.TYPE_MONTHLY)).array(), domain);
	}

	@Override
	public long countWorkReportMonthlyDataSet(BasicDBObject filter, String userid,String domain){
		if (filter == null)
			filter = new BasicDBObject();

		filter.put("reporter", userid);

		filter.put("type", WorkReport.TYPE_MONTHLY);
		return count(filter, WorkReport.class,domain);
	}

	@Override
	public List<WorkReport> createWorkReportProjectDailyDataSet(BasicDBObject condition, String userid, ObjectId project_id,String domain){
		return query(condition,
				(List<Bson>) Domain.getJQ(domain, "查询-报告")
						.set("match",
								new Document("type", WorkReport.TYPE_DAILY).append("project_id", project_id).append("status",
										new BasicDBObject("$in", Arrays.asList(WorkReport.STATUS_SUBMIT, WorkReport.STATUS_CONFIRM))))
						.array(), domain);
	}

	@Override
	public long countWorkReportProjectDailyDataSet(BasicDBObject filter, String userid, ObjectId project_id,String domain){
		if (filter == null)
			filter = new BasicDBObject();

		filter.put("project_id", project_id);
		filter.put("type", WorkReport.TYPE_DAILY);
		filter.put("status", new BasicDBObject("$in", Arrays.asList(WorkReport.STATUS_SUBMIT, WorkReport.STATUS_CONFIRM)));
		return count(filter, WorkReport.class,domain);
	}

	@Override
	public List<WorkReport> createWorkReportProjectWeeklyDataSet(BasicDBObject condition, String userid, ObjectId project_id,String domain){
		return query(condition,
				(List<Bson>) Domain.getJQ(domain, "查询-报告")
						.set("match",
								new Document("type", WorkReport.TYPE_WEEKLY).append("project_id", project_id).append("status",
										new BasicDBObject("$in", Arrays.asList(WorkReport.STATUS_SUBMIT, WorkReport.STATUS_CONFIRM))))
						.array(), domain);
	}

	@Override
	public long countWorkReportProjectWeeklyDataSet(BasicDBObject filter, String userid, ObjectId project_id,String domain){
		if (filter == null)
			filter = new BasicDBObject();

		filter.put("project_id", project_id);
		filter.put("type", WorkReport.TYPE_WEEKLY);
		filter.put("status", new BasicDBObject("$in", Arrays.asList(WorkReport.STATUS_SUBMIT, WorkReport.STATUS_CONFIRM)));
		return count(filter, WorkReport.class,domain);
	}

	@Override
	public List<WorkReport> createWorkReportProjectMonthlyDataSet(BasicDBObject condition, String userid, ObjectId project_id,String domain){
		return query(condition,
				(List<Bson>) Domain.getJQ(domain, "查询-报告")
						.set("match",
								new Document("type", WorkReport.TYPE_MONTHLY).append("project_id", project_id).append("status",
										new BasicDBObject("$in", Arrays.asList(WorkReport.STATUS_SUBMIT, WorkReport.STATUS_CONFIRM))))
						.array(), domain);
	}

	@Override
	public long countWorkReportProjectMonthlyDataSet(BasicDBObject filter, String userid, ObjectId project_id,String domain){
		if (filter == null)
			filter = new BasicDBObject();

		filter.put("project_id", project_id);
		filter.put("type", WorkReport.TYPE_MONTHLY);
		filter.put("status", new BasicDBObject("$in", Arrays.asList(WorkReport.STATUS_SUBMIT, WorkReport.STATUS_CONFIRM)));
		return count(filter, WorkReport.class,domain);
	}

	@Override
	public List<WorkReport> createWorkReportStageDailyDataSet(BasicDBObject condition, String userid, ObjectId stage_id,String domain){
		ObjectId project_id = c("work",domain).distinct("project_id", new Document("_id", stage_id), ObjectId.class).first();

		return query(condition, (List<Bson>) Domain.getJQ(domain, "查询-报告")
				.set("match",
						new Document("reporter", userid).append("type", WorkReport.TYPE_DAILY).append("project_id", project_id).append(
								"status", new BasicDBObject("$in", Arrays.asList(WorkReport.STATUS_SUBMIT, WorkReport.STATUS_CONFIRM))))
				.array(), domain);
	}

	@Override
	public long countWorkReportStageDailyDataSet(BasicDBObject filter, String userid, ObjectId stage_id,String domain){
		if (filter == null)
			filter = new BasicDBObject();

		ObjectId project_id = c("work",domain).distinct("project_id", new Document("_id", stage_id), ObjectId.class).first();
		filter.put("reporter", userid);
		filter.put("project_id", project_id);
		filter.put("type", WorkReport.TYPE_DAILY);
		filter.put("status", new BasicDBObject("$in", Arrays.asList(WorkReport.STATUS_SUBMIT, WorkReport.STATUS_CONFIRM)));
		return count(filter, WorkReport.class,domain);
	}

	@Override
	public List<WorkReport> createWorkReportStageWeeklyDataSet(BasicDBObject condition, String userid, ObjectId stage_id,String domain){
		ObjectId project_id = c("work",domain).distinct("project_id", new Document("_id", stage_id), ObjectId.class).first();
		return query(condition, (List<Bson>) Domain.getJQ(domain, "查询-报告")
				.set("match",
						new Document("reporter", userid).append("type", WorkReport.TYPE_WEEKLY).append("project_id", project_id).append(
								"status", new BasicDBObject("$in", Arrays.asList(WorkReport.STATUS_SUBMIT, WorkReport.STATUS_CONFIRM))))
				.array(), domain);
	}

	@Override
	public long countWorkReportStageWeeklyDataSet(BasicDBObject filter, String userid, ObjectId stage_id,String domain){
		if (filter == null)
			filter = new BasicDBObject();

		ObjectId project_id = c("work",domain).distinct("project_id", new Document("_id", stage_id), ObjectId.class).first();
		filter.put("reporter", userid);
		filter.put("project_id", project_id);
		filter.put("type", WorkReport.TYPE_WEEKLY);
		filter.put("status", new BasicDBObject("$in", Arrays.asList(WorkReport.STATUS_SUBMIT, WorkReport.STATUS_CONFIRM)));
		return count(filter, WorkReport.class,domain);
	}

	@Override
	public List<WorkReport> createWorkReportStageMonthlyDataSet(BasicDBObject condition, String userid, ObjectId stage_id,String domain){
		ObjectId project_id = c("work",domain).distinct("project_id", new Document("_id", stage_id), ObjectId.class).first();
		return query(condition, (List<Bson>) Domain.getJQ(domain, "查询-报告")
				.set("match",
						new Document("reporter", userid).append("type", WorkReport.TYPE_MONTHLY).append("project_id", project_id).append(
								"status", new BasicDBObject("$in", Arrays.asList(WorkReport.STATUS_SUBMIT, WorkReport.STATUS_CONFIRM))))
				.array(), domain);
	}

	@Override
	public long countWorkReportStageMonthlyDataSet(BasicDBObject filter, String userid, ObjectId stage_id,String domain){
		if (filter == null)
			filter = new BasicDBObject();

		ObjectId project_id = c("work",domain).distinct("project_id", new Document("_id", stage_id), ObjectId.class).first();
		filter.put("reporter", userid);
		filter.put("project_id", project_id);
		filter.put("type", WorkReport.TYPE_MONTHLY);
		filter.put("status", new BasicDBObject("$in", Arrays.asList(WorkReport.STATUS_SUBMIT, WorkReport.STATUS_CONFIRM)));
		return count(filter, WorkReport.class,domain);
	}

	@Override
	public WorkReport insert(WorkReport workReport,String domain){
		if (c("workReport",domain).countDocuments(new Document("project_id", workReport.getProject_id()).append("period", workReport.getPeriod())
				.append("type", workReport.getType()).append("reporter", workReport.getReporter())) > 0){
			throw new ServiceException("已经创建报告。");
		}
		ObjectId workReport_id = new ObjectId();

		List<WorkReportItem> into = c("work", WorkReportItem.class,domain).aggregate(Domain.getJQ(domain, "查询-报告项-项目")
				.set("project_id", workReport.getProject_id()).set("actualFinish", workReport.getPeriod()).set("report_id", workReport_id)
				.set("reportorId", workReport.getReporter()).set("chargerid", workReport.getReporter()).array())
				.into(new ArrayList<WorkReportItem>());
		if (into.size() == 0){
			throw new ServiceException("没有需要填写报告的工作。");
		}
		workReport.set_id(workReport_id);
		WorkReport newWorkReport = super.insert(workReport,domain);
		c(WorkReportItem.class,domain).insertMany(into);
		return getWorkReport(newWorkReport.get_id(), domain);
	}

	@Override
	public long delete(ObjectId _id,String domain){
		c("workReportItem",domain).deleteMany(new Document("report_id", _id));
		return delete(_id, WorkReport.class,domain);
	}

	@Override
	public long update(BasicDBObject filterAndUpdate,String domain){
		return update(filterAndUpdate, WorkReport.class,domain);
	}

	@Override
	public List<WorkReport> listInfo(ObjectId _id,String domain){
		return Arrays.asList(getWorkReport(_id, domain));
	}

	@Override
	public List<WorkReportItem> listReportItem(ObjectId report_id,String domain){
		List<? extends Bson> pipeline = Domain.getJQ(domain, "查询-报告项").set("report_id", report_id).array();
		return c(WorkReportItem.class,domain).aggregate(pipeline).into(new ArrayList<WorkReportItem>());
	}

	@Override
	public long countReportItem(ObjectId workReport_id,String domain){
		return count(new BasicDBObject("report_id", workReport_id), WorkReportItem.class,domain);
	}

	@Override
	public long updateWorkReportItem(BasicDBObject filterAndUpdate,String domain){
		return update(filterAndUpdate, WorkReportItem.class,domain);
	}

	@Override
	public WorkReport getWorkReport(ObjectId _id,String domain){
		Document match = new Document("_id", _id);
		return c(WorkReport.class,domain).aggregate(Domain.getJQ(domain, "查询-报告").set("match", match).array()).first();
	}

	@Override
	public List<Result> submitWorkReport(List<ObjectId> workReportIds,String domain){
		List<Result> result = submitWorkReportCheck(workReportIds, domain);
		if (!result.isEmpty()){
			return result;
		}

		UpdateResult ur = c(WorkReport.class,domain).updateMany(new Document("_id", new Document("$in", workReportIds)),
				new Document("$set", new Document("submitDate", new Date()).append("status", WorkReport.STATUS_SUBMIT)));
		if (ur.getModifiedCount() == 0){
			result.add(Result.updateFailure("没有满足提交条件的报告。"));
			return result;
		}

		return result;
	}

	private List<Result> submitWorkReportCheck(List<ObjectId> workReportIds,String domain){
		List<Result> result = new ArrayList<Result>();
		Document doc = new Document("statement", null).append("report_id", new Document("$in", workReportIds));
		long count = c(WorkReportItem.class,domain).countDocuments(doc);
		if (count > 0){
			result.add(Result.submitWorkReportError("存在为填写完成情况的工作"));
		}
		return result;
	}

	@Override
	public List<Result> confirmWorkReport(List<ObjectId> workReportIds, String userId,String domain){
		List<Result> result = confirmWorkReportCheck(workReportIds, domain);
		if (!result.isEmpty()){
			return result;
		}

		UpdateResult ur = c(WorkReport.class,domain).updateMany(new Document("_id", new Document("$in", workReportIds)), new Document("$set",
				new Document("verifyDate", new Date()).append("status", WorkReport.STATUS_CONFIRM).append("verifier", userId)));
		if (ur.getModifiedCount() == 0){
			result.add(Result.updateFailure("没有满足确认条件的报告。"));
			return result;
		}

		ur = c(WorkReportItem.class,domain).updateMany(new Document("report_id", new Document("$in", workReportIds)),
				new Document("$set", new Document("confirmed", true)));

		// 更新工作预计完成时间
		c(WorkReportItem.class,domain).find(new Document("report_id", new Document("$in", workReportIds))).forEach((WorkReportItem wri) -> {
			Date estFinish = wri.getEstimatedFinish();
			if (estFinish != null){
				Double progress = null;
				double d = estFinish.getTime() - wri.getActualStart().getTime();
				double d1 = wri.get_id().getDate().getTime() - wri.getActualStart().getTime();
				if (d > 0 && d1 > 0){
					progress = (double) Math.round(d1 * 10000 / d) / 10000;
				}
				c("work",domain).updateOne(new Document("_id", wri.getWork_id()),
						new Document("$set", new Document("estimatedFinish", estFinish).append("progress", progress)));

			}
		});

		List<ObjectId> itemIds = c(WorkReportItem.class,domain)
				.distinct("_id", new Document("report_id", new Document("$in", workReportIds)), ObjectId.class)
				.into(new ArrayList<ObjectId>());
		// 复制报告中的资源用量到resourceActual集合中
		c("workReportResourceActual",domain).find(new Document("workReportItemId", new Document("$in", itemIds))).forEach((Document doc) -> {
			Document first = c("resourceActual",domain).find(new Document("id", doc.get("id")).append("work_id", doc.get("work_id"))
					.append("usedHumanResId", doc.get("usedHumanResId")).append("usedEquipResId", doc.get("usedEquipResId"))
					.append("usedTypedResId", doc.get("usedTypedResId")).append("resTypeId", doc.get("resTypeId"))).first();
			if (first != null){
				Object actualBasicQty = first.get("actualBasicQty");
				if (actualBasicQty != null && doc.get("actualBasicQty") != null){
					actualBasicQty = ((Number) actualBasicQty).doubleValue() + doc.getDouble("actualBasicQty");
				} else if (actualBasicQty == null && doc.get("actualBasicQty") != null){
					actualBasicQty = doc.get("actualBasicQty");
				}

				Object actualOverTimeQty = first.get("actualOverTimeQty");
				if (actualOverTimeQty != null && doc.get("actualOverTimeQty") != null){
					actualOverTimeQty = ((Number) actualOverTimeQty).doubleValue() + doc.getDouble("actualOverTimeQty");
				} else if (actualOverTimeQty == null && doc.get("actualOverTimeQty") != null){
					actualOverTimeQty = doc.get("actualOverTimeQty");
				}

				c("resourceActual",domain).updateOne(new Document("_id", first.get("_id")), new Document("$set",
						new Document("actualBasicQty", actualBasicQty).append("actualOverTimeQty", actualOverTimeQty)));
			} else {
				doc.remove("workReportItemId");
				c("resourceActual",domain).insertOne(doc);
			}
		});

		return result;
	}

	private List<Result> confirmWorkReportCheck(List<ObjectId> workReportIds,String domain){
		// TODO 检查是否可以进行确认
		return new ArrayList<Result>();
	}

	@Override
	public List<WorkReportSummary> listWeeklyAdministeredProjectReportSummary(BasicDBObject condition, String managerId,String domain){
		List<ObjectId> ids = getAdministratedProjects(managerId, domain);
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);// 本周一
		Date to = cal.getTime();
		cal.add(Calendar.DATE, -7);// 上周一
		Date from = cal.getTime();
		List<Bson> pipeline = Domain.getJQ(domain, "查询-报告-周报").set("project_id", new Document("$in", ids)).set("from", from).set("to", to).array();

		if (condition != null){
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

		return c("workReport",domain).aggregate(pipeline, WorkReportSummary.class).into(new ArrayList<>());
	}

	@Override
	public long countWeeklyAdministeredProjectReportSummary(BasicDBObject filter, String managerId,String domain){
		List<ObjectId> ids = getAdministratedProjects(managerId, domain);
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);// 本周一
		Date to = cal.getTime();
		cal.add(Calendar.DATE, -7);// 上周一
		Date from = cal.getTime();

		if (filter == null){
			filter = new BasicDBObject();
		}

		return c("workReport",domain).countDocuments(filter.append("status", "确认").append("project_id", new Document("$in", ids))
				.append("reportDate", new Document("$gte", from).append("$lte", to)));
	}

	@Override
	public List<WorkReport> createWorkReportDataSet(BasicDBObject condition, String userid,String domain){
		List<ObjectId> projectIds = c("project",domain).distinct("_id", new Document("pmId", userid), ObjectId.class)
				.into(new ArrayList<ObjectId>());
		List<Bson> pipeline = Domain.getJQ(domain, "查询-报告")
				.set("match", new Document("status", WorkReport.STATUS_SUBMIT).append("project_id", new BasicDBObject("$in", projectIds)))
				.array();
		return query(condition, pipeline, domain);
	}

	@Override
	public List<Document> listWorkReportToConfirm(BasicDBObject condition, String userid,String domain){
		List<ObjectId> projectIds = c("project",domain).distinct("_id", new Document("pmId", userid), ObjectId.class)
				.into(new ArrayList<ObjectId>());
		List<Bson> pipeline = Domain.getJQ(domain, "查询-报告")
				.set("match", new Document("status", WorkReport.STATUS_SUBMIT).append("project_id", new BasicDBObject("$in", projectIds)))
				.array();
		return interateReport(condition, pipeline, domain).map(ReportRenderer::render).into(new ArrayList<>());
	}

	@Override
	public long countWorkReportDataSet(BasicDBObject filter, String userid,String domain){
		if (filter == null)
			filter = new BasicDBObject();

		List<ObjectId> projectIds = c("project",domain).distinct("_id", new Document("pmId", userid), ObjectId.class)
				.into(new ArrayList<ObjectId>());

		filter.put("status", WorkReport.STATUS_SUBMIT);
		filter.put("project_id", new BasicDBObject("$in", projectIds));

		return count(filter, WorkReport.class,domain);
	}

	@Override
	public List<WorkReport> createAllWorkReportDailyDataSet(BasicDBObject condition, String userid,String domain){
		List<Bson> pipeline = new ArrayList<Bson>();

		if (!checkUserRoles(userid, Role.SYS_ROLE_PD_ID, domain))
			appendQueryUserInProjectPMO(pipeline, userid, "$project_id", domain);

		pipeline.addAll(Domain.getJQ(domain, "查询-报告")
				.set("match", new Document("type", WorkReport.TYPE_DAILY).append("status", WorkReport.STATUS_CONFIRM)).array());
		return query(condition, pipeline, domain);
	}

	@Override
	public long countAllWorkReportDailyDataSet(BasicDBObject filter, String userid,String domain){
		// 如果用户具有项目总监权限时，显示全部已确认的项目日报
		if (checkUserRoles(userid, Role.SYS_ROLE_PD_ID, domain))
			return c("workReport",domain)
					.countDocuments(new BasicDBObject("type", WorkReport.TYPE_DAILY).append("status", WorkReport.STATUS_CONFIRM));
		else {
			// 否则只显示当前用户在项目PMO团队中的项目的已确认的项目日报
			List<Bson> pipeline = new ArrayList<>();
			appendQueryUserInProjectPMO(pipeline, userid, "$project_id", domain);
			pipeline.add(Aggregates.match(new Document("type", WorkReport.TYPE_DAILY).append("status", WorkReport.STATUS_CONFIRM)));
			return c("workReport",domain).aggregate(pipeline).into(new ArrayList<>()).size();
		}
	}

	@Override
	public List<WorkReport> createAllWorkReportWeeklyDataSet(BasicDBObject condition, String userid,String domain){
		List<Bson> pipeline = new ArrayList<Bson>();
		if (!checkUserRoles(userid, Role.SYS_ROLE_PD_ID, domain))
			appendQueryUserInProjectPMO(pipeline, userid, "$project_id", domain);

		pipeline.addAll(Domain.getJQ(domain, "查询-报告")
				.set("match", new Document("type", WorkReport.TYPE_WEEKLY).append("status", WorkReport.STATUS_CONFIRM)).array());
		return query(condition, pipeline, domain);
	}

	@Override
	public long countAllWorkReportWeeklyDataSet(BasicDBObject filter, String userid,String domain){
		// 如果用户具有项目总监权限时，显示全部已确认的项目周报
		if (checkUserRoles(userid, Role.SYS_ROLE_PD_ID, domain))
			return c("workReport",domain)
					.countDocuments(new BasicDBObject("type", WorkReport.TYPE_WEEKLY).append("status", WorkReport.STATUS_CONFIRM));
		else {
			// 否则只显示当前用户在项目PMO团队中的项目的已确认的项目周报
			List<Bson> pipeline = new ArrayList<>();
			appendQueryUserInProjectPMO(pipeline, userid, "$project_id", domain);
			pipeline.add(Aggregates.match(new Document("type", WorkReport.TYPE_WEEKLY).append("status", WorkReport.STATUS_CONFIRM)));
			return c("workReport",domain).aggregate(pipeline).into(new ArrayList<>()).size();
		}
	}

	@Override
	public List<WorkReport> createAllWorkReportMonthlyDataSet(BasicDBObject condition, String userid,String domain){
		List<Bson> pipeline = new ArrayList<Bson>();
		// 当前用户不是显示全部日报，但当前用户具有项目管理员角色时，只显示其作为项目团队成员的报告
		if (!checkUserRoles(userid, Role.SYS_ROLE_PD_ID, domain))
			appendQueryUserInProjectPMO(pipeline, userid, "$project_id", domain);

		pipeline.addAll(Domain.getJQ(domain, "查询-报告")
				.set("match", new Document("type", WorkReport.TYPE_MONTHLY).append("status", WorkReport.STATUS_CONFIRM)).array());
		return query(condition, pipeline, domain);
	}

	@Override
	public long countALLWorkReportMonthlyDataSet(BasicDBObject filter, String userid,String domain){//TODO Change ALL 2 All in method name
		// 如果用户具有项目总监权限时，显示全部已确认的项目月报
		if (checkUserRoles(userid, Role.SYS_ROLE_PD_ID, domain))
			return c("workReport",domain)
					.countDocuments(new BasicDBObject("type", WorkReport.TYPE_MONTHLY).append("status", WorkReport.STATUS_CONFIRM));
		else {
			// 否则只显示当前用户在项目PMO团队中的项目的已确认的项目月报
			List<Bson> pipeline = new ArrayList<>();
			appendQueryUserInProjectPMO(pipeline, userid, "$project_id", domain);
			pipeline.add(Aggregates.match(new Document("type", WorkReport.TYPE_MONTHLY).append("status", WorkReport.STATUS_CONFIRM)));
			return c("workReport",domain).aggregate(pipeline).into(new ArrayList<>()).size();
		}
	}

}
