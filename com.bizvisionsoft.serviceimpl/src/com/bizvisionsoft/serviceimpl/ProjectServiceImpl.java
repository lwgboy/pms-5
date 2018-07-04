package com.bizvisionsoft.serviceimpl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Generator;
import com.bizvisionsoft.math.scheduling.Graphic;
import com.bizvisionsoft.math.scheduling.Route;
import com.bizvisionsoft.math.scheduling.Task;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.model.Baseline;
import com.bizvisionsoft.service.model.BaselineComparable;
import com.bizvisionsoft.service.model.CBSItem;
import com.bizvisionsoft.service.model.ChangeProcess;
import com.bizvisionsoft.service.model.Command;
import com.bizvisionsoft.service.model.Message;
import com.bizvisionsoft.service.model.News;
import com.bizvisionsoft.service.model.OBSItem;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.ProjectChange;
import com.bizvisionsoft.service.model.ProjectChangeTask;
import com.bizvisionsoft.service.model.ProjectStatus;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.SalesItem;
import com.bizvisionsoft.service.model.Stockholder;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.model.Workspace;
import com.bizvisionsoft.service.tools.Util;
import com.bizvisionsoft.serviceimpl.exception.ServiceException;
import com.bizvisionsoft.serviceimpl.query.JQ;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Field;
import com.mongodb.client.model.UnwindOptions;
import com.mongodb.client.result.UpdateResult;

public class ProjectServiceImpl extends BasicServiceImpl implements ProjectService {

	@Override
	public Project insert(Project input) {
		// TODO ��¼������
		Project project;
		if (input.getProjectTemplate_id() == null) {
			/////////////////////////////////////////////////////////////////////////////
			// ����׼��
			ObjectId obsRoot_id = new ObjectId();// ��֯��
			ObjectId cbsRoot_id = new ObjectId();// Ԥ���

			ObjectId projectSet_id = input.getProjectSet_id();
			ObjectId obsParent_id = null;// ��֯�ϼ�
			ObjectId cbsParent_id = null;// �ɱ��ϼ�
			if (projectSet_id != null) {
				// ����ϼ�obs_id
				Document doc = c("projectSet").find(new BasicDBObject("_id", projectSet_id))
						.projection(new BasicDBObject("obs_id", true).append("cbs_id", true)).first();
				obsParent_id = Optional.ofNullable(doc).map(d -> d.getObjectId("obs_id")).orElse(null);
				cbsParent_id = Optional.ofNullable(doc).map(d -> d.getObjectId("cbs_id")).orElse(null);

			}
			/////////////////////////////////////////////////////////////////////////////
			// 0. ������Ŀ
			project = insert(input.setOBS_id(obsRoot_id).setCBS_id(cbsRoot_id), Project.class);

			/////////////////////////////////////////////////////////////////////////////
			// 1. ��Ŀ�Ŷӳ�ʼ��
			OBSItem obsRoot = new OBSItem()// ��������Ŀ��OBS���ڵ�
					.set_id(obsRoot_id)// ����_id����Ŀ����
					.setScope_id(project.get_id())// ����scope_id��������֯�ڵ��Ǹ���Ŀ����֯
					.setParent_id(obsParent_id)// �����ϼ���id
					.setName(project.getName() + "��Ŀ��")// ���ø���֯�ڵ��Ĭ������
					.setRoleId(OBSItem.ID_PM)// ���ø���֯�ڵ�Ľ�ɫid
					.setRoleName(OBSItem.NAME_PM)// ���ø���֯�ڵ������
					.setManagerId(project.getPmId()) // ���ø���֯�ڵ�Ľ�ɫ��Ӧ����
					.setScopeRoot(true);// ��������ڵ��Ƿ�Χ�ڵĸ��ڵ�
			insert(obsRoot, OBSItem.class);

			/////////////////////////////////////////////////////////////////////////////
			// 2. �����Ŀ��ʼ��
			// ������
			CBSItem cbsRoot = CBSItem.getInstance(project, true);//
			cbsRoot.set_id(cbsRoot_id);//
			cbsRoot.setParent_id(cbsParent_id);//
			cbsRoot.setId(project.getId());
			cbsRoot.setName(project.getName());
			insert(cbsRoot, CBSItem.class);

		} else {
			// TODO ����ģ�崴��

			project = insert(input, Project.class);
		}

		return get(project.get_id());
	}

	@Override
	public Project get(ObjectId _id) {
		List<Project> ds = createDataSet(new BasicDBObject("filter", new BasicDBObject("_id", _id)));
		if (ds.size() == 0) {
			throw new ServiceException("û��_idΪ" + _id + "����Ŀ��");
		}
		return ds.get(0);
	}

	@Override
	public long count(BasicDBObject filter) {
		return count(filter, Project.class);
	}

	@Override
	public List<Project> createDataSet(BasicDBObject condition) {
		Integer skip = (Integer) condition.get("skip");
		Integer limit = (Integer) condition.get("limit");
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		return query(skip, limit, filter);
	}

	private List<Project> query(Integer skip, Integer limit, BasicDBObject filter) {
		ArrayList<Bson> pipeline = new ArrayList<Bson>();

		appendQueryPipeline(skip, limit, filter, pipeline);

		List<Project> result = new ArrayList<Project>();
		c(Project.class).aggregate(pipeline).into(result);
		return result;

	}

	private void appendQueryPipeline(Integer skip, Integer limit, BasicDBObject filter, List<Bson> pipeline) {
		if (filter != null)
			pipeline.add(Aggregates.match(filter));

		if (skip != null)
			pipeline.add(Aggregates.skip(skip));

		if (limit != null)
			pipeline.add(Aggregates.limit(limit));

		// TODO ����pipeline
		// 1. �е���֯
		appendOrgFullName(pipeline, "impUnit_id", "impUnitOrgFullName");

		appendUserInfo(pipeline, "pmId", "pmInfo");

		appendStage(pipeline, "stage_id", "stage");

		appendLookupAndUnwind(pipeline, "projectSet", "projectSet_id", "projectSet");

		appendLookupAndUnwind(pipeline, "eps", "eps_id", "eps");

		appendLookupAndUnwind(pipeline, "project", "parentProject_id", "parentProject");

		appendWorkTime(pipeline);

		pipeline.addAll(new JQ("��Ŀsar�ܵ�").array());

		// TODO ���ӳ����жϣ�
		// appendOverdue(pipeline);
	}

	/**
	 * ͨ���ų̹������
	 * 
	 * @param pipeline
	 */
	@Deprecated
	void appendOverdue(List<Bson> pipeline) {
		pipeline.addAll(
				Arrays.asList(
						new Document("$lookup", new Document("from", "work")
								.append("let",
										new Document("project_id", "$_id"))
								.append("pipeline",
										Arrays.asList(
												new Document("$match",
														new Document("$expr",
																new Document("$and", Arrays.asList(
																		new Document("$eq",
																				Arrays.asList("$project_id",
																						"$$project_id")),
																		new Document("$in",
																				Arrays.asList("$manageLevel",
																						Arrays.asList("1", "2"))),
																		new Document("$or", Arrays.asList(
																				new Document("$lt",
																						Arrays.asList("$planFinish",
																								new Date())),
																				new Document("$lt",
																						Arrays.asList("$planFinish",
																								"$actualFinish")))))))),
												new Document("$group",
														new Document("_id", null).append("count",
																new Document("$sum", 1)))))
								.append("as", "work")),
						new Document("$unwind",
								new Document("path", "$work").append("preserveNullAndEmptyArrays", true)),
						new Document("$addFields",
								new Document("overdueWarning", new BasicDBObject("$cond",
										new BasicDBObject("if", new Document("$gt", Arrays.asList("$work.count", 0)))
												.append("then", "Ԥ��").append("else", "")))),
						new Document("$project", new Document("work", false))));
	}

	private void appendWorkTime(List<Bson> pipeline) {
		pipeline.addAll(Arrays.asList(
				new Document("$lookup", new Document()
						.append("from", "work").append("let", new Document("project_id", "$_id")).append("pipeline",
								Arrays.asList(
										new Document("$match",
												new Document("$expr",
														new Document("$and",
																Arrays.asList(
																		new Document("$eq",
																				Arrays.asList("$project_id",
																						"$$project_id")),
																		new Document("$eq",
																				Arrays.asList("$summary", false)))))),
										new Document("$group",
												new Document("_id", null)
														.append("actualWorks", new Document("$sum", "$actualWorks"))
														.append("planWorks", new Document("$sum", "$planWorks")))))
						.append("as", "worktime")),
				new Document("$unwind", new Document("path", "$worktime").append("preserveNullAndEmptyArrays", true)),
				new Document("$addFields", new Document("summaryActualWorks", "$worktime.actualWorks")
						.append("summaryPlanWorks", "$worktime.planWorks")),
				new Document("$project", new Document("worktime", false))));

		pipeline.addAll(Arrays.asList(
				new Document("$lookup",
						new Document("from", "work")
								.append("let",
										new Document("project_id", "$_id"))
								.append("pipeline",
										Arrays.asList(
												new Document("$match",
														new Document("$expr",
																new Document("$and",
																		Arrays.asList(
																				new Document("$eq",
																						Arrays.asList("$project_id",
																								"$$project_id")),
																				new Document("$eq",
																						Arrays.asList("$summary",
																								false)))))),
												new Document().append("$addFields",
														new Document()
																.append("planDuration",
																		new Document("$divide", Arrays.asList(
																				new Document(
																						"$subtract",
																						Arrays.asList(
																								"$planFinish",
																								"$planStart")),
																				86400000)))
																.append("actualDuration", new Document("$divide", Arrays
																		.asList(new Document("$subtract", Arrays.asList(
																				new Document(
																						"$ifNull",
																						Arrays.asList("$actualFinish",
																								new Date())),
																				new Document("$ifNull",
																						Arrays.asList("$actualStart",
																								new Date())))),
																				86400000)))),
												new Document("$group",
														new Document("_id", null)
																.append("planDuration",
																		new Document("$sum", "$planDuration"))
																.append("actualDuration",
																		new Document("$sum", "$actualDuration")))))
								.append("as", "workDuration")),
				new Document("$unwind",
						new Document("path", "$workDuration").append("preserveNullAndEmptyArrays", true)),
				new Document("$addFields",
						new Document("summaryPlanDuration", "$workDuration.planDuration")
								.append("summaryActualDuration", "$workDuration.actualDuration")),
				new Document("$project", new Document("workDuration", false))));

		pipeline.addAll(Arrays.asList(
				new Document().append("$lookup", new Document().append("from", "work")
						.append("let", new Document().append("project_id", "$_id"))
						.append("pipeline", Arrays.asList(new Document().append("$match",
								new Document().append("$expr", new Document().append("$and",
										Arrays.asList(new Document().append("$eq", Arrays.asList("$stage", true)),
												new Document().append("$eq",
														Arrays.asList("$project_id", "$$project_id"))))))))
						.append("as", "stageWork")),
				new Document().append("$addFields", new Document().append("stage_ids", "$stageWork._id")),
				new Document().append("$project", new Document().append("stageWork", false))));
	}

	@Override
	public List<Date> getPlanDateRange(ObjectId _id) {
		Project data = c(Project.class).find(new BasicDBObject("_id", _id))
				.projection(new BasicDBObject().append("planStart", 1).append("planFinish", 1)).first();
		ArrayList<Date> result = new ArrayList<Date>();
		result.add(data.getPlanStart());
		result.add(data.getPlanFinish());
		return result;
	}

	@Override
	public long update(BasicDBObject fu) {
		return update(fu, Project.class);
	}

	@Override
	public List<Result> startProject(Command com) {
		List<Result> result = startProjectCheck(com._id, com.userId);
		if (!result.isEmpty()) {
			return result;
		}

		// �޸���Ŀ״̬
		UpdateResult ur = c(Project.class).updateOne(new BasicDBObject("_id", com._id),
				new BasicDBObject("$set", new BasicDBObject("status", ProjectStatus.Processing)
						.append("startOn", com.date).append("startBy", com.userId)));

		// ����ur��������Ľ��
		if (ur.getModifiedCount() == 0) {
			result.add(Result.updateFailure("û������������������Ŀ��"));
			return result;
		}

		// TODO CBS������

		// ֪ͨ��Ŀ�Ŷӳ�Ա����Ŀ�Ѿ�����
		List<String> memberIds = getProjectMembers(com._id);
		String name = getName("project", com._id);
		sendMessage("��Ŀ����֪ͨ",
				"���������Ŀ" + name + "����" + new SimpleDateFormat(Util.DATE_FORMAT_DATE).format(com.date) + "������",
				com.userId, memberIds, null);
		return result;
	}

	private List<String> getProjectMembers(ObjectId _id) {
		List<ObjectId> parentIds = c("obs").distinct("_id", new BasicDBObject("scope_id", _id), ObjectId.class)
				.into(new ArrayList<>());
		List<ObjectId> ids = getDesentItems(parentIds, "obs", "parent_id");
		ArrayList<String> memberIds = c("obs")
				.distinct("managerId", new BasicDBObject("_id", new BasicDBObject("$in", ids)).append("managerId",
						new BasicDBObject("$ne", null)), String.class)
				.into(new ArrayList<>());
		return memberIds;
	}

	private List<Result> startProjectCheck(ObjectId _id, String executeBy) {
		//////////////////////////////////////////////////////////////////////
		// �������Ϣ
		// 1. ����Ƿ񴴽��˵�һ���WBS�����мƻ������û�У���ʾ����
		// 2. �����֯�ṹ�Ƿ���ɣ����ֻ�и�������
		// 3. ����һ���WBS�Ƿ�ָ���˱�Ҫ�Ľ�ɫ�����û�и����˽�ɫ����ʾ���档
		// 4. ȱ�ٸ�ϵ�ˣ�����
		// 5. û����Ԥ�㣬����
		// 6. Ԥ��û���꣬����
		// 7. Ԥ��û�з��䣬����
		// 8. ����Ҫ�й�����ţ�����

		return new ArrayList<Result>();
	}

	@Override
	public List<Result> distributeProjectPlan(Command com) {
		List<Result> result = distributeProjectPlanCheck(com._id, com.userId);
		if (!result.isEmpty()) {
			return result;
		}

		Document query = new Document("project_id", com._id).append("parent_id", null)
				.append("chargerId", new Document("$ne", null)).append("distributed", new Document("$ne", true));

		final List<ObjectId> ids = new ArrayList<>();
		final List<Message> messages = new ArrayList<>();

		String pjName = getName("project", com._id);
		c("work").find(query).forEach((Document w) -> {
			ids.add(w.getObjectId("_id"));
			messages.add(Message.newInstance("�����ƻ��´�֪ͨ",
					"���������Ŀ " + pjName + "������ " + w.getString("fullName") + "�� Ԥ�ƴ� "
							+ new SimpleDateFormat(Util.DATE_FORMAT_DATE).format(w.getDate("planStart")) + "��ʼ��"
							+ new SimpleDateFormat(Util.DATE_FORMAT_DATE).format(w.getDate("planFinish")) + "����",
					com.userId, w.getString("chargerId"), null));
		});

		if (ids.isEmpty()) {
			result.add(Result.updateFailure("û����Ҫ�´�ļƻ���"));
			return result;
		}

		c(Work.class).updateMany(new Document("_id", new Document("$in", ids)), new Document("$set",
				new Document("distributed", true).append("distributeBy", com.userId).append("distributeOn", com.date)));

		sendMessages(messages);

		return new ArrayList<Result>();

	}

	private List<Result> distributeProjectPlanCheck(ObjectId _id, String distributeBy) {
		// TODO ����Ƿ�����´�
		return new ArrayList<Result>();
	}

	@Override
	public List<Work> listStage(ObjectId _id) {
		// TODO ����
		return new WorkServiceImpl().query(null, null, new BasicDBObject("project_id", _id).append("stage", true),
				Work.class);
	}

	@Override
	public long countStage(ObjectId _id) {
		return c("work").countDocuments(new BasicDBObject("project_id", _id).append("stage", true));
	}

	@Override
	public List<Work> listMyStage(String userId) {
		return new WorkServiceImpl().createTaskDataSet(new BasicDBObject("$or",
				Arrays.asList(new BasicDBObject("chargerId", userId), new BasicDBObject("assignerId", userId)))
						.append("stage", true));
	}

	@Override
	public long countMyStage(String userId) {
		return count(new BasicDBObject("$or",
				Arrays.asList(new BasicDBObject("chargerId", userId), new BasicDBObject("assignerId", userId)))
						.append("stage", true),
				"work");
	}

	@Override
	public List<Stockholder> getStockholders(BasicDBObject condition, ObjectId _id) {
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter == null) {
			filter = new BasicDBObject();
			condition.put("filter", filter);
		}
		filter.append("project_id", _id);
		return createDataSet(condition, Stockholder.class);
	}

	@Override
	public long countStockholders(BasicDBObject filter, ObjectId _id) {
		filter.append("project_id", _id);
		return count(filter, Stockholder.class);
	}

	@Override
	public Stockholder insertStockholder(Stockholder c) {
		return insert(c, Stockholder.class);
	}

	@Override
	public List<Project> listManagedProjects(BasicDBObject condition, String userid) {
		Integer skip = (Integer) condition.get("skip");
		Integer limit = (Integer) condition.get("limit");
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter == null) {
			filter = new BasicDBObject();
			condition.put("filter", filter);
		}
		filter.put("pmId", userid);
		return query(skip, limit, filter);
	}

	@Override
	public long countManagedProjects(BasicDBObject filter, String userid) {
		if (filter == null) {
			filter = new BasicDBObject();
		}
		filter.put("pmId", userid);
		return count(filter, Project.class);
	}

	@Override
	public List<Project> listParticipatedProjects(BasicDBObject condition, String userId) {
		Integer skip = (Integer) condition.get("skip");
		Integer limit = (Integer) condition.get("limit");
		BasicDBObject filter = (BasicDBObject) condition.get("filter");

		List<Bson> pipeline = new ArrayList<Bson>();

		appendParticipatedProjectQuery(userId, pipeline);

		appendQueryPipeline(skip, limit, filter, pipeline);

		return c("obs").aggregate(pipeline, Project.class).into(new ArrayList<Project>());
	}

	private void appendParticipatedProjectQuery(String userId, List<Bson> pipeline) {
		pipeline.add(new Document("$match",
				new Document("$or", Arrays.asList(new Document("member", userId), new Document("managerId", userId)))));

		pipeline.add(new Document("$lookup", new Document("from", "work").append("localField", "scope_id")
				.append("foreignField", "_id").append("as", "work")));

		pipeline.add(new Document("$lookup", new Document("from", "project").append("localField", "scope_id")
				.append("foreignField", "_id").append("as", "project")));

		pipeline.add(new Document("$lookup", new Document("from", "project").append("localField", "work.project_id")
				.append("foreignField", "_id").append("as", "project2")));

		pipeline.add(
				new Document("$unwind", new Document("path", "$project").append("preserveNullAndEmptyArrays", true)));

		pipeline.add(
				new Document("$unwind", new Document("path", "$project2").append("preserveNullAndEmptyArrays", true)));

		pipeline.add(
				new Document("$addFields", new Document("project_id", new Document("$cond", Arrays.asList("$project",
						"$project._id", new Document("$cond", Arrays.asList("$project2", "$project2._id", null)))))));

		pipeline.add(new Document("$group", new Document("_id", "$project_id")));

		pipeline.add(new Document("$lookup", new Document("from", "project").append("localField", "_id")
				.append("foreignField", "_id").append("as", "project")));

		pipeline.add(new Document("$replaceRoot",
				new Document("newRoot", new Document("$arrayElemAt", Arrays.asList("$project", 0)))));

		pipeline.add(new Document("$match", new Document("status", new Document("$ne", ProjectStatus.Closed))));
	}

	@Override
	public long countParticipatedProjects(BasicDBObject filter, String userId) {
		List<Bson> pipeline = new ArrayList<Bson>();

		appendParticipatedProjectQuery(userId, pipeline);

		if (filter != null)
			pipeline.add(new Document("$match", filter));

		return c("obs").aggregate(pipeline).into(new ArrayList<>()).size();
	}

	@Override
	public List<Project> listParticipatedProjectsInDaily(BasicDBObject condition, String userId) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return listParticipatedProjects(condition, userId, cal.getTime());
	}

	@Override
	public long countParticipatedProjectsInDaily(BasicDBObject filter, String userId) {
		Date startWorkFinish = new Date();
		return countParticipatedProjects(filter, userId, startWorkFinish);
	}

	@Override
	public List<Project> listParticipatedProjectsInWeekly(BasicDBObject condition, String userId) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.setFirstDayOfWeek(Calendar.SUNDAY);
		int i = cal.get(Calendar.DAY_OF_WEEK);

		cal.add(Calendar.DAY_OF_MONTH, cal.getFirstDayOfWeek() - i);
		return listParticipatedProjects(condition, userId, cal.getTime());
	}

	@Override
	public long countParticipatedProjectsInWeekly(BasicDBObject filter, String userId) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.setFirstDayOfWeek(Calendar.SUNDAY);
		int i = cal.get(Calendar.DAY_OF_WEEK);

		cal.add(Calendar.DAY_OF_MONTH, cal.getFirstDayOfWeek() - i);
		return countParticipatedProjects(filter, userId, cal.getTime());
	}

	@Override
	public List<Project> listParticipatedProjectsInMonthly(BasicDBObject condition, String userId) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		return listParticipatedProjects(condition, userId, cal.getTime());
	}

	@Override
	public long countParticipatedProjectsInMonthly(BasicDBObject filter, String userId) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		return countParticipatedProjects(filter, userId, cal.getTime());
	}

	private List<Project> listParticipatedProjects(BasicDBObject condition, String userId, Date startWorkFinish) {
		Integer skip = (Integer) condition.get("skip");
		Integer limit = (Integer) condition.get("limit");
		BasicDBObject filter = (BasicDBObject) condition.get("filter");

		List<ObjectId> project_ids = c("work")
				.distinct("project_id",
						new Document("summary", false)
								.append("actualStart",
										new Document("$ne", null))
								.append("$and",
										Arrays.asList(
												new Document("$or",
														Arrays.asList(new Document("actualFinish", null),
																new Document("actualFinish",
																		new Document("$gte", startWorkFinish)))),
												new Document("$or",
														Arrays.asList(new Document("chargerId", userId),
																new Document("assignerId", userId))))),
						ObjectId.class)
				.into(new ArrayList<ObjectId>());

		List<Bson> pipeline = new ArrayList<Bson>();
		pipeline.add(Aggregates.match(new Document("_id", new Document("$in", project_ids))));

		if (filter != null)
			pipeline.add(Aggregates.match(filter));

		if (skip != null)
			pipeline.add(Aggregates.skip(skip));

		if (limit != null)
			pipeline.add(Aggregates.limit(limit));

		appendOrgFullName(pipeline, "impUnit_id", "impUnitOrgFullName");

		appendUserInfo(pipeline, "pmId", "pmInfo");
		
		pipeline.forEach(a ->{
			System.out.println(getBson(a).toJson());
		});

		ArrayList<Project> into = c("project").aggregate(pipeline, Project.class).into(new ArrayList<Project>());
		return into;
	}

	private long countParticipatedProjects(BasicDBObject filter, String userId, Date startWorkFinish) {
		List<Bson> pipeline = new ArrayList<Bson>();
		List<ObjectId> project_ids = c("work")
				.distinct("project_id",
						new Document("summary", false)
								.append("actualStart",
										new Document("$ne", null))
								.append("$and",
										Arrays.asList(
												new Document("$or",
														Arrays.asList(new Document("actualFinish", null),
																new Document("actualFinish",
																		new Document("$gte", startWorkFinish)))),
												new Document("$or",
														Arrays.asList(new Document("chargerId", userId),
																new Document("assignerId", userId))))),
						ObjectId.class)
				.into(new ArrayList<ObjectId>());

		pipeline.add(Aggregates.match(new Document("_id", new Document("$in", project_ids))));
		if (filter != null)
			pipeline.add(new Document("$match", filter));

		return c("project").aggregate(pipeline).into(new ArrayList<>()).size();
	}

	@Override
	public long delete(ObjectId _id) {
		// TODO ɾ�����
		return delete(_id, Project.class);
	}

	@Override
	public Workspace getWorkspace(ObjectId _id) {
		BasicDBObject dbo = c("project").find(new BasicDBObject("_id", _id), BasicDBObject.class)
				.projection(new BasicDBObject("space_id", Boolean.TRUE).append("checkoutBy", Boolean.TRUE)).first();
		return Workspace.newInstance(_id, dbo.getObjectId("space_id"), dbo.getString("checkoutBy"));
	}

	@Override
	public List<Result> finishProject(Command com) {
		List<Result> result = finishProjectCheck(com._id, com.userId);
		if (!result.isEmpty()) {
			return result;
		}

		Document doc = c("work").find(new BasicDBObject("project_id", com._id))
				.projection(new BasicDBObject("actualFinish", true)).sort(new BasicDBObject("actualFinish", -1))
				.first();

		// �޸���Ŀ״̬
		UpdateResult ur = c("project").updateOne(new BasicDBObject("_id", com._id),
				new BasicDBObject("$set",
						new BasicDBObject("status", ProjectStatus.Closing).append("progress", 1d)
								.append("finishOn", com.date).append("finishBy", com.userId)
								.append("actualFinish", doc.get("actualFinish"))));

		// ����ur��������Ľ��
		if (ur.getModifiedCount() == 0) {
			result.add(Result.updateFailure("û�������깤��������Ŀ��"));
			return result;
		}

		// ֪ͨ��Ŀ�Ŷӳ�Ա����Ŀ�Ѿ�����
		List<String> memberIds = getProjectMembers(com._id);
		String name = getName("project", com._id);
		sendMessage("��Ŀ��β֪ͨ",
				"���������Ŀ" + name + "����" + new SimpleDateFormat(Util.DATE_FORMAT_DATE).format(com.date) + "������β��",
				com.userId, memberIds, null);

		return result;
	}

	private List<Result> finishProjectCheck(ObjectId _id, String executeBy) {
		//////////////////////////////////////////////////////////////////////
		// �������Ϣ
		// 1. �����������Ŀ�Ĺ����Ƿ�ȫ���깤����û�У�����
		ArrayList<Result> result = new ArrayList<Result>();
		long count = c("work").countDocuments(new BasicDBObject("project_id", _id).append("actualFinish", null));
		if (count > 0) {
			result.add(Result.finishError("��Ŀ����û���깤�Ĺ�����"));
		}
		return result;
	}

	@Override
	public List<Result> closeProject(Command com) {
		List<Result> result = closeProjectCheck(com._id, com.userId);
		if (!result.isEmpty()) {
			return result;
		}

		// �޸���Ŀ״̬
		UpdateResult ur = c(Project.class).updateOne(new BasicDBObject("_id", com._id),
				new BasicDBObject("$set", new BasicDBObject("status", ProjectStatus.Closed).append("closeOn", com.date)
						.append("closeBy", com.userId)));

		// ����ur��������Ľ��
		if (ur.getModifiedCount() == 0) {
			result.add(Result.updateFailure("û������ر���������Ŀ��"));
			return result;
		}

		// ֪ͨ��Ŀ�Ŷӳ�Ա����Ŀ�Ѿ��ر�
		List<String> memberIds = getProjectMembers(com._id);
		String name = getName("project", com._id);
		sendMessage("��Ŀ�ر�֪ͨ",
				"���������Ŀ" + name + "����" + new SimpleDateFormat(Util.DATE_FORMAT_DATE).format(com.date) + "�رա�",
				com.userId, memberIds, null);
		return result;
	}

	private List<Result> closeProjectCheck(ObjectId _id, String executeBy) {
		//////////////////////////////////////////////////////////////////////
		// �������Ϣ

		return new ArrayList<Result>();
	}

	@Override
	public String generateWorkOrder(String catalog, ObjectId parentproject_id, ObjectId impunit_id) {
		/**
		 * TODO ��Ҫ���ݾ��޶���
		 * 
		 * KG��-����-����-��������
		 * 
		 * ���Ų����ļ����롣
		 *
		 * ��1λ����2λΪ�����룺KG�����������Ŀ��YG����Ԥ������Ŀ��CG����CBB��Ŀ��
		 *
		 * ��3λΪ���в��ţ�1����ʶ����ҵ����2����̽����ҵ����3����չܹ�˾��5����ͨ����Կ���ҵ����6�����̲���7����Ԥ�в���8�����Բ���
		 *
		 * ��4λ����9λΪ����˳��ţ���Ϊ�����֣�
		 * 1.��4λ����6λ����ˮ������ÿ��ӡ�-01����ʼ������ţ�����ĿΪ����Ŀ�����4λ����6λΪ����Ŀ����˳��ţ�
		 * 2.��7λ����9λΪ����Ŀ��ˮ�ţ�����Ŀ�л�ȡ������Ŀ��ˮ�ţ��粻������Ŀ�����7λ����9λΪ�ա�
		 * 
		 * ��10λ����14λΪ������ݡ�
		 **/
		String workOrder;
		if ("Ԥ��".equals(catalog)) {
			workOrder = "YG";
		} else if ("CBB".equals(catalog)) {
			workOrder = "CG";
		} else {
			workOrder = "KG";
		}
		int year = Calendar.getInstance().get(Calendar.YEAR);

		String orgNo = c("organization").distinct("id", new Document("_id", impunit_id), String.class).first();
		workOrder += orgNo;

		if (parentproject_id != null) {
			String parentWorkOrder = c("project")
					.distinct("workOrder", new Document("_id", parentproject_id), String.class).first();
			String[] workorders = parentWorkOrder.split("-");
			workOrder += "-" + workorders[1];
			int index = generateCode(Generator.DEFAULT_NAME, "projectno" + workorders[1]);
			workOrder += "-" + String.format("%02d", index);

		} else {
			int index = generateCode(Generator.DEFAULT_NAME, "projectno" + year);
			workOrder += "-" + String.format("%02d", index);
		}

		workOrder += "-" + String.format("%04d", year);

		return workOrder;
	}

	@Override
	public List<News> getRecentNews(ObjectId _id, int count) {
		ArrayList<News> result = new ArrayList<News>();
		c("work")
				.aggregate(
						new JQ("��ѯʱ����").set("match",
								new Document("manageLevel", new Document("$in", Arrays.asList("1", "2")))
										.append("project_id", _id))
								.set("limit", count).array())
				.forEach((Document doc) -> {
					if (doc.get("date").equals(doc.get("actualStart"))) {
						result.add(new News().setDate(doc.getDate("date"))
								.setContent("" + doc.get("userInfo") + " " + doc.get("name") + " ���� "));
					} else if (doc.get("date").equals(doc.get("actualFinish"))) {
						result.add(new News().setDate(doc.getDate("date"))
								.setContent("" + doc.get("userInfo") + " " + doc.get("name") + " ���"));
					}
				});
		return result;
	}

	@Override
	public SalesItem insertSalesItem(SalesItem salesItem) {
		return insert(salesItem);
	}

	@Override
	public long deleteStockholder(ObjectId _id) {
		return delete(_id, Stockholder.class);
	}

	@Override
	public Integer schedule(ObjectId _id) {
		Document pj = c("project").find(new Document("_id", _id)).first();
		Date start = pj.getDate("planStart");
		Date end = pj.getDate("planFinish");

		ArrayList<Document> works = c("work").find(new Document("project_id", _id)).into(new ArrayList<>());
		ArrayList<Document> links = c("worklinks").find(new Document("project_id", _id)).into(new ArrayList<>());

		ArrayList<Task> tasks = new ArrayList<Task>();
		ArrayList<Route> routes = new ArrayList<Route>();
		convertGraphic(works, links, tasks, routes);

		Graphic gh = new Graphic(tasks, routes);

		setupStartDate(gh, works, start, tasks);
		gh.schedule();

		// �����Ŀ�Ƿ���
		int warningLevel = 999;
		Document message = null;
		// 0��Ԥ�����
		float overTime = gh.getT() - ((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24));
		if (overTime > 0) {
			warningLevel = 0;// 0��Ԥ������Ŀ���ܳ��ڡ�
			message = new Document("date", new Date()).append("overdue", (int) overTime)
					.append("finish", gh.getFinishDate()).append("msg", "��ĿԤ�Ƴ���").append("duration", (int) gh.getT());

		}
		for (int i = 0; i < works.size(); i++) {
			Document doc = works.get(i);
			Date planFinish = doc.getDate("planFinish");
			Date estFinish = gh.getTaskEFDate((doc.getObjectId("_id").toHexString()));
			Task task = gh.getTask((doc.getObjectId("_id").toHexString()));
			long overdue = ((estFinish.getTime() - planFinish.getTime()) / (1000 * 60 * 60 * 24));
			Document update = new Document("date", new Date()).append("duration", (int) task.getD().floatValue())
					.append("overdue", (int) overdue).append("finish", estFinish);

			if ("1".equals(doc.getString("manageLevel")) && overdue > 0) {
				warningLevel = warningLevel > 1 ? 1 : warningLevel;
				message = message == null ? new Document("date", new Date()).append("msg", "һ������Ԥ�Ƴ���") : message;
			}

			if ("2".equals(doc.getString("manageLevel")) && overdue > 0) {
				warningLevel = warningLevel > 2 ? 2 : warningLevel;
				message = message == null ? new Document("date", new Date()).append("msg", "��������Ԥ�Ƴ���") : message;
			}

			if (update == null) {
				update = new Document();
			}
			update.append("tf", (double) task.getTF()).append("ff", (double) task.getFF());
			c("work").updateOne(new Document("_id", doc.getObjectId("_id")),
					new Document("$set", new Document("scheduleEst", update)));
		}

		c("project").updateOne(new Document("_id", _id),
				new Document("$set", new Document("overdueIndex", warningLevel).append("scheduleEst", message)));
		return warningLevel;
	}

	@Override
	public List<Baseline> listBaseline(BasicDBObject condition, ObjectId _id) {
		Integer skip = (Integer) condition.get("skip");
		Integer limit = (Integer) condition.get("limit");
		BasicDBObject sort = (BasicDBObject) condition.get("sort");
		if (sort == null) {
			sort = new BasicDBObject("_id", -1);
		}
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter == null) {
			filter = new BasicDBObject();
		}
		filter.append("project_id", _id);
		return query(skip, limit, filter, sort, Baseline.class);
	}

	@Override
	public long countBaseline(BasicDBObject filter, ObjectId _id) {
		if (filter == null) {
			filter = new BasicDBObject();
		}
		filter.append("project_id", _id);

		return count(filter, Baseline.class);
	}

	@Override
	public Baseline createBaseline(Baseline baseline) {
		Baseline newBaseline = insert(baseline, Baseline.class);
		ObjectId project_id = baseline.getProject_id();
		// ��ȡҪ�洢�����ߵ���Ŀ
		Document projectDoc = c("project").find(new Document("_id", project_id)).first();
		ObjectId newBaseline_id = newBaseline.get_id();
		projectDoc.append("projectName", projectDoc.get("name"));
		projectDoc.append("projectDescription", projectDoc.get("description"));
		projectDoc.remove("description");
		projectDoc.remove("name");
		projectDoc.remove("_id");
		projectDoc.remove("checkoutBy");
		projectDoc.remove("space_id");

		// ��ȡҪ�洢�����ߵĹ���
		Map<ObjectId, ObjectId> workIds = new HashMap<ObjectId, ObjectId>();
		List<Document> workDocs = new ArrayList<Document>();
		c("work").find(new Document("project_id", project_id)).sort(new Document("parent_id", 1))
				.forEach((Document doc) -> {
					ObjectId work_id = doc.getObjectId("_id");
					ObjectId newWork_id = new ObjectId();
					workIds.put(work_id, newWork_id);
					doc.append("old_id", work_id);
					doc.append("_id", newWork_id);

					ObjectId parent_id = doc.getObjectId("parent_id");
					if (parent_id != null) {
						ObjectId newParent_id = workIds.get(parent_id);
						doc.append("parent_id", newParent_id);
					}

					doc.append("baseline_id", newBaseline_id);

					doc.remove("checkoutBy");
					doc.remove("space_id");

					workDocs.add(doc);
				});

		// ��ȡҪ�洢�����ߵĹ���������ϵ
		List<Document> worklinkDocs = new ArrayList<Document>();
		c("worklinks").find(new Document("project_id", project_id)).forEach((Document doc) -> {
			ObjectId target = doc.getObjectId("target");
			doc.append("target", workIds.get(target));
			ObjectId source = doc.getObjectId("source");
			doc.append("source", workIds.get(source));

			doc.append("baseline_id", newBaseline_id);

			doc.remove("_id");

			worklinkDocs.add(doc);
		});

		// ������Ŀ�͹�������
		c(Baseline.class).updateMany(new Document("_id", baseline.get_id()), new Document("$set", projectDoc));

		if (workDocs.size() > 0)
			c("baselineWork").insertMany(workDocs);

		if (worklinkDocs.size() > 0)
			c("baselineWorkLinks").insertMany(worklinkDocs);

		return get(newBaseline_id, Baseline.class);
	}

	@Override
	public long deleteBaseline(ObjectId _id) {
		c("baselineWork").deleteMany(new Document("baseline_id", _id));

		c("baselineWorkLinks").deleteMany(new Document("baseline_id", _id));

		return delete(_id, Baseline.class);
	}

	@Override
	public long updateBaseline(BasicDBObject filterAndUpdate) {
		return update(filterAndUpdate, Baseline.class);
	}

	@Override
	public List<BaselineComparable> getBaselineComparable(List<ObjectId> projectIds) {
		List<Bson> pipeline = new ArrayList<Bson>();
		pipeline.add(Aggregates.match(new Document("project_id", new Document("$in", projectIds))));
		pipeline.add(Aggregates.sort(new Document("project_id", 1).append("index", 1)));
		pipeline.add(Aggregates.lookup("project", "project_id", "_id", "project"));
		pipeline.add(Aggregates.unwind("$project"));
		pipeline.add(Aggregates.addFields(Arrays.asList(new Field<String>("projectName", "$project.name"),
				new Field<String>("projectNumber", "$project.id"))));
		pipeline.add(Aggregates.project(new BasicDBObject("project", false)));
		appendUserInfo(pipeline, "chargerId", "chargerInfo");
		appendUserInfo(pipeline, "assignerId", "assignerInfo");
		List<Work> works = c("work", Work.class).aggregate(pipeline).into(new ArrayList<Work>());

		pipeline = new ArrayList<Bson>();
		pipeline.add(Aggregates.match(new Document("baseline_id", new Document("$in", projectIds))));
		pipeline.add(Aggregates.sort(new Document("project_id", 1).append("index", 1)));
		pipeline.add(Aggregates.lookup("project", "project_id", "_id", "project"));
		pipeline.add(Aggregates.unwind("$project"));
		pipeline.add(Aggregates.addFields(Arrays.asList(new Field<String>("projectName", "$project.name"),
				new Field<String>("projectNumber", "$project.id"))));
		pipeline.add(Aggregates.project(new BasicDBObject("project", false)));
		appendUserInfo(pipeline, "chargerId", "chargerInfo");
		appendUserInfo(pipeline, "assignerId", "assignerInfo");
		List<Work> baselineWorks = c("baselineWork", Work.class).aggregate(pipeline).into(new ArrayList<Work>());

		List<BaselineComparable> result = new ArrayList<BaselineComparable>();
		ObjectId p = null;
		if (works.size() > 0) {
			for (Work work : works) {
				if (p == null) {
					p = work.getProject_id();
				}
				result.add(new BaselineComparable().setWork1(work));
			}
		}
		for (Work work : baselineWorks) {
			if (p == null) {
				p = work.getBaseline_id();
			}
			if (p.equals(work.getBaseline_id())) {
				result.add(new BaselineComparable().setWork1(work));
			} else {
				boolean add = true;
				for (BaselineComparable bc : result) {
					Work work1 = bc.getWork1();
					ObjectId work1Old_id = work1.getOld_id();
					if (work1Old_id != null) {
						if (work1Old_id.equals(work.getOld_id())) {
							bc.setWork2(work);
							add = false;
							break;
						}
					} else {
						if (work1.get_id().equals(work.getOld_id())) {
							bc.setWork2(work);
							add = false;
							break;
						}
					}
				}
				if (add) {
					result.add(new BaselineComparable().setWork2(work));
				}
			}
		}

		return result;
	}

	@Override
	public List<ProjectChange> listProjectChange(BasicDBObject condition, ObjectId _id) {
		Integer skip = (Integer) condition.get("skip");
		Integer limit = (Integer) condition.get("limit");
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter == null) {
			filter = new BasicDBObject();
		}
		filter.append("project_id", _id);

		ArrayList<Bson> pipeline = new ArrayList<Bson>();

		pipeline.add(Aggregates.match(filter));

		pipeline.add(Aggregates.sort(new BasicDBObject("_id", -1)));

		if (skip != null)
			pipeline.add(Aggregates.skip(skip));

		if (limit != null)
			pipeline.add(Aggregates.limit(limit));

		appendProject(pipeline);
		appendUserInfo(pipeline, "applicant", "applicantInfo");
		appendOrgFullName(pipeline, "applicantUnitId", "applicantUnit");

		ArrayList<ProjectChange> into = c(ProjectChange.class).aggregate(pipeline).into(new ArrayList<ProjectChange>());
		return into;
	}

	@Override
	public long countProjectChange(BasicDBObject filter, ObjectId _id) {
		if (filter == null) {
			filter = new BasicDBObject();
		}
		filter.append("project_id", _id);

		return count(filter, ProjectChange.class);
	}

	@Override
	public ProjectChange createProjectChange(ProjectChange pc) {
		List<ProjectChangeTask> reviewer = pc.getReviewer();
		List<OBSItem> obsItems = new OBSServiceImpl().getScopeOBS(pc.getProject_id());
		List<ChangeProcess> changeProcesss = new CommonServiceImpl().createChangeProcessDataSet();
		for (ChangeProcess changeProcess : changeProcesss) {
			if (changeProcess.getProjectOBSId() != null) {
				for (OBSItem obsItem : obsItems) {
					if (obsItem.getRoleId() != null && obsItem.getRoleId().equals(changeProcess.getProjectOBSId())) {
						ProjectChangeTask pct = new ProjectChangeTask();
						pct.user = obsItem.getManagerId();
						pct.name = changeProcess.getTaskName();
						reviewer.add(pct);
					}
				}
			} else {
				ProjectChangeTask pct = new ProjectChangeTask();
				pct.name = changeProcess.getTaskName();
				reviewer.add(pct);
			}
		}

		ProjectChange newPC = insert(pc, ProjectChange.class);
		return listProjectChangeInfo(newPC.get_id()).get(0);
	}

	@Override
	public long deleteProjectChange(ObjectId _id) {
		return delete(_id, ProjectChange.class);
	}

	@Override
	public long updateProjectChange(BasicDBObject filterAndUpdate) {
		return update(filterAndUpdate, ProjectChange.class);
	}

	@Override
	public List<ProjectChange> listProjectChangeInfo(ObjectId _id) {
		List<Bson> pipeline = new ArrayList<Bson>();
		pipeline.add(Aggregates.match(new Document("_id", _id)));

		pipeline.add(Aggregates.lookup("project", "project_id", "_id", "project"));
		pipeline.add(Aggregates.unwind("$project"));
		pipeline.add(Aggregates.addFields(Arrays.asList(new Field<String>("projectName", "$project.name"),
				new Field<String>("projectNumber", "$project.id"), new Field<String>("projectPMId", "$project.pmId"))));

		appendUserInfo(pipeline, "applicant", "applicantInfo");
		appendOrgFullName(pipeline, "applicantUnitId", "applicantUnit");
		pipeline.add(Aggregates.lookup("organization", "applicantUnitId", "_id", "organization"));
		pipeline.add(Aggregates.unwind("$organization", new UnwindOptions().preserveNullAndEmptyArrays(true)));
		pipeline.add(Aggregates.addFields(Arrays.asList(new Field<String>("applicantUnit", "$organization.fullName"),
				new Field<String>("managerId", "$applicantUnitId.managerId"))));
		pipeline.add(Aggregates.project(new Document("organization", false)));

		return c(ProjectChange.class).aggregate(pipeline).into(new ArrayList<ProjectChange>());
	}

	@Override
	public List<Result> submitProjectChange(List<ObjectId> projectChangeIds) {
		List<Result> result = submitProjectChangeCheck(projectChangeIds);
		if (!result.isEmpty()) {
			return result;
		}

		UpdateResult ur = c(ProjectChange.class).updateMany(new Document("_id", new Document("$in", projectChangeIds)),
				new Document("$set",
						new Document("submitDate", new Date()).append("status", ProjectChange.STATUS_SUBMIT)));
		if (ur.getModifiedCount() == 0) {
			result.add(Result.updateFailure("û�������ύ�����ı�����롣"));
			return result;
		}

		// ����֪ͨ
		ArrayList<Bson> pipeline = new ArrayList<Bson>();

		pipeline.add(Aggregates.match(new Document("_id", new Document("$in", projectChangeIds))));
		appendProject(pipeline);
		appendUserInfo(pipeline, "applicant", "applicantInfo");
		appendOrgFullName(pipeline, "applicantUnitId", "applicantUnit");

		c(ProjectChange.class).aggregate(pipeline).forEach((ProjectChange projectChange) -> {
			List<String> receivers = new ArrayList<String>();
			projectChange.getReviewer().forEach((ProjectChangeTask receiver) -> {
				receivers.add(receiver.user);
			});
			sendMessage("��Ŀ�������",
					"" + projectChange.getApplicantInfo() + "��������Ŀ" + projectChange.getProjectName() + "�ı�����룬����������ˡ�",
					projectChange.getApplicantId(), receivers, null);

			String pmId = c("project")
					.distinct("pmId", new Document("_id", projectChange.getProject_id()), String.class).first();
			if (!receivers.contains(pmId))
				sendMessage("��Ŀ�������",
						"" + projectChange.getApplicantInfo() + "��������Ŀ" + projectChange.getProjectName() + "�ı�����룬",
						projectChange.getApplicantId(), receivers, null);

		});

		return result;
	}

	private List<Result> submitProjectChangeCheck(List<ObjectId> projectChangeIds) {
		List<Result> result = new ArrayList<Result>();
		long count = c(ProjectChange.class).countDocuments(
				new Document("_id", new Document("$in", projectChangeIds)).append("reviewer.user", null));
		if (count > 0) {
			result.add(Result.submitProjectChangeError("ȱ�������Ա"));
		}
		return result;
	}

	@Override
	public List<Result> passProjectChange(ProjectChangeTask projectChangeTask) {
		List<Result> result = passProjectChangeCheck(projectChangeTask);
		if (!result.isEmpty()) {
			return result;
		}

		ProjectChange pc = get(projectChangeTask.projectChange_id, ProjectChange.class);
		List<ProjectChangeTask> reviewers = pc.getReviewer();
		List<BasicDBObject> reviewer = new ArrayList<BasicDBObject>();
		String status = ProjectChange.STATUS_PASS;
		for (ProjectChangeTask re : reviewers) {
			if (re.choice == null && !re.name.equals(projectChangeTask.name)) {
				status = ProjectChange.STATUS_SUBMIT;
			} else if (re.name.equals(projectChangeTask.name)) {
				re.user = projectChangeTask.user;
				re.choice = projectChangeTask.choice;
				re.date = projectChangeTask.date;
				re.comment = projectChangeTask.comment;
			}
			reviewer.add(getBson(re));
		}

		UpdateResult ur = c(ProjectChange.class).updateOne(new BasicDBObject("_id", projectChangeTask.projectChange_id),
				new BasicDBObject("$set", new BasicDBObject("reviewer", reviewer).append("status", status)));
		if (ur.getModifiedCount() == 0) {
			result.add(Result.updateFailure("û������ȷ�������ı�����롣"));
			return result;
		}

		// ���ͱ����׼֪ͨ
		sendMessage("��Ŀ�����������׼", "" + projectChangeTask.getUser() + "��׼����Ŀ" + pc.getProjectName() + "�ı�����룬",
				projectChangeTask.user, pc.getApplicantId(), null);
		if (ProjectChange.STATUS_PASS.equals(status)) {
			String pmId = c("project").distinct("pmId", new Document("_id", pc.getProject_id()), String.class).first();
			sendMessage("��Ŀ���������ͨ��", "��Ŀ" + pc.getProjectName() + "�ı�����������ͨ����", pc.getApplicantId(), pmId, null);
		}

		return result;
	}

	private List<Result> passProjectChangeCheck(ProjectChangeTask projectChangeTask) {
		return new ArrayList<Result>();
	}

	@Override
	public List<Result> confirmProjectChange(List<ObjectId> projectChangeIds, String userId) {
		List<Result> result = confirmProjectChangeCheck(projectChangeIds);
		if (!result.isEmpty()) {
			return result;
		}

		UpdateResult ur = c(ProjectChange.class).updateMany(new Document("_id", new Document("$in", projectChangeIds)),
				new Document("$set", new Document("verifyDate", new Date()).append("verify", userId).append("status",
						ProjectChange.STATUS_CONFIRM)));
		if (ur.getModifiedCount() == 0) {
			result.add(Result.updateFailure("û������ȷ�������ı�����롣"));
			return result;
		}
		// ����֪ͨ
		ArrayList<Bson> pipeline = new ArrayList<Bson>();

		pipeline.add(Aggregates.match(new Document("_id", new Document("$in", projectChangeIds))));
		appendProject(pipeline);
		appendUserInfo(pipeline, "applicant", "applicantInfo");
		appendOrgFullName(pipeline, "applicantUnitId", "applicantUnit");

		c(ProjectChange.class).aggregate(pipeline).forEach((ProjectChange pc) -> {
			sendMessage("��Ŀ��������ѹر�", "��Ŀ" + pc.getProjectName() + "�ı���ѹرգ�", userId, pc.getApplicantId(), null);
		});
		return result;
	}

	private List<Result> confirmProjectChangeCheck(List<ObjectId> projectChangeIds) {
		return new ArrayList<Result>();
	}

	public List<Result> cancelProjectChange(ProjectChangeTask projectChangeTask) {
		List<Result> result = cancelProjectChangeCheck(projectChangeTask);
		if (!result.isEmpty()) {
			return result;
		}
		ProjectChange pc = get(projectChangeTask.projectChange_id, ProjectChange.class);
		List<BasicDBObject> reviewer = new ArrayList<BasicDBObject>();
		List<ProjectChangeTask> reviewers = pc.getReviewer();
		for (ProjectChangeTask re : reviewers) {
			if (re.name.equals(projectChangeTask.name)) {
				re.user = projectChangeTask.user;
				re.choice = projectChangeTask.choice;
				re.date = projectChangeTask.date;
				re.comment = projectChangeTask.comment;
			}
			reviewer.add(getBson(re));
		}

		UpdateResult ur = c(ProjectChange.class).updateOne(new BasicDBObject("_id", projectChangeTask.projectChange_id),
				new BasicDBObject("$set",
						new BasicDBObject("reviewer", reviewer).append("status", ProjectChange.STATUS_CANCEL)));
		if (ur.getModifiedCount() == 0) {
			result.add(Result.updateFailure("û������ȡ�������ı�����롣"));
			return result;
		}
		String pmId = c("project").distinct("pmId", new Document("_id", pc.getProject_id()), String.class).first();
		sendMessage("��Ŀ��������ѷ��", "" + projectChangeTask.getUser() + "�������Ŀ" + pc.getProjectName() + "�ı�����룬",
				projectChangeTask.user, Arrays.asList(pmId, pc.getApplicantId()), null);

		return result;
	}

	private List<Result> cancelProjectChangeCheck(ProjectChangeTask projectChangeTask) {
		return new ArrayList<Result>();
	}

	@Override
	public long updateProjectChange(ProjectChangeTask projectChangeTask, ObjectId projectChange_id) {
		ProjectChange pc = get(projectChange_id, ProjectChange.class);
		List<ProjectChangeTask> reviewers = pc.getReviewer();
		List<BasicDBObject> reviewer = new ArrayList<BasicDBObject>();
		for (ProjectChangeTask re : reviewers) {
			if (re.name.equals(projectChangeTask.name)) {
				re.user = projectChangeTask.user;
			}
			reviewer.add(getBson(re));
		}

		UpdateResult ur = c(ProjectChange.class).updateOne(new BasicDBObject("_id", projectChange_id),
				new BasicDBObject("$set", new BasicDBObject("reviewer", reviewer)));

		return ur.getModifiedCount();
	}

	@Override
	public long checkCreateProjectChange(ObjectId _id) {
		return c(ProjectChange.class).countDocuments(new Document("project_id", _id).append("status",
				new Document("$nin", Arrays.asList(ProjectChange.STATUS_CANCEL, ProjectChange.STATUS_CONFIRM))));
	}

	@Override
	public List<ProjectChange> listReviewerProjectChange(BasicDBObject condition, String userId) {
		Integer skip = (Integer) condition.get("skip");
		Integer limit = (Integer) condition.get("limit");
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter == null) {
			filter = new BasicDBObject();
		}

		List<Bson> pipeline = (List<Bson>) new JQ("����������Ŀ���").set("userId", userId)
				.set("status", ProjectChange.STATUS_SUBMIT).array();

		pipeline.add(Aggregates.match(filter));

		pipeline.add(Aggregates.sort(new BasicDBObject("_id", -1)));

		if (skip != null)
			pipeline.add(Aggregates.skip(skip));

		if (limit != null)
			pipeline.add(Aggregates.limit(limit));

		appendProject(pipeline);
		appendUserInfo(pipeline, "applicant", "applicantInfo");
		appendOrgFullName(pipeline, "applicantUnitId", "applicantUnit");

		ArrayList<ProjectChange> into = c(ProjectChange.class).aggregate(pipeline).into(new ArrayList<ProjectChange>());
		return into;
	}

	@Override
	public List<Project> listAdministratedProjects(BasicDBObject condition, String managerId) {
		List<ObjectId> projectIds = getAdministratedProjects(managerId);
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter == null) {
			filter = new BasicDBObject();
			condition.put("filter", filter);
		}
		filter.append("_id", new BasicDBObject("$in", projectIds));
		return createDataSet(condition);
	}

	@Override
	public long countAdministratedProjects(BasicDBObject filter, String managerId) {
		List<ObjectId> projectIds = getAdministratedProjects(managerId);
		if(filter == null) {
			filter = new BasicDBObject();
		}
		filter.append("_id", new BasicDBObject("$in", projectIds));
		return count(filter);
	}

}
