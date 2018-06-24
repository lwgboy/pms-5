package com.bizvisionsoft.serviceimpl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Generator;
import com.bizvisionsoft.math.scheduling.Graphic;
import com.bizvisionsoft.math.scheduling.Relation;
import com.bizvisionsoft.math.scheduling.Route;
import com.bizvisionsoft.math.scheduling.Task;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.model.CBSItem;
import com.bizvisionsoft.service.model.Command;
import com.bizvisionsoft.service.model.Message;
import com.bizvisionsoft.service.model.News;
import com.bizvisionsoft.service.model.OBSItem;
import com.bizvisionsoft.service.model.Project;
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
	 *  ͨ���ų̹������
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
		sendMessage("��Ŀ����", "��Ŀ" + name + "����" + new SimpleDateFormat(Util.DATE_FORMAT_DATE).format(com.date) + "������",
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
			messages.add(Message.newInstance("���´�Ĺ����ƻ�",
					"��Ŀ " + pjName + "������ " + w.getString("fullName") + "���ƻ� "
							+ new SimpleDateFormat(Util.DATE_FORMAT_DATE).format(w.getDate("planStart")) + " - "
							+ new SimpleDateFormat(Util.DATE_FORMAT_DATE).format(w.getDate("planFinish")),
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
		return c("work").count(new BasicDBObject("project_id", _id).append("stage", true));
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
		Date startWorkFinish = new Date();
		return listParticipatedProjects(condition, userId, startWorkFinish);
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
			pipeline.add(Aggregates.match(filter));

		if (skip != null)
			pipeline.add(Aggregates.skip(skip));

		if (limit != null)
			pipeline.add(Aggregates.limit(limit));

		appendOrgFullName(pipeline, "impUnit_id", "impUnitOrgFullName");

		appendUserInfo(pipeline, "pmId", "pmInfo");

		return c("project").aggregate(pipeline, Project.class).into(new ArrayList<Project>());
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
		sendMessage("��Ŀ�깤", "��Ŀ" + name + "����" + new SimpleDateFormat(Util.DATE_FORMAT_DATE).format(com.date) + "�깤��",
				com.userId, memberIds, null);

		return result;
	}

	private List<Result> finishProjectCheck(ObjectId _id, String executeBy) {
		//////////////////////////////////////////////////////////////////////
		// �������Ϣ
		// 1. �����������Ŀ�Ĺ����Ƿ�ȫ���깤����û�У�����
		ArrayList<Result> result = new ArrayList<Result>();
		long count = c("work").count(new BasicDBObject("project_id", _id).append("actualFinish", null));
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

		// TODO ֪ͨ��Ŀ�Ŷӳ�Ա����Ŀ�Ѿ��ر�

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
		ArrayList<Document> works = c("work").find(new Document("project_id", _id)).into(new ArrayList<>());
		ArrayList<Document> links = c("worklinks").find(new Document("project_id", _id)).into(new ArrayList<>());
		Document pj = c("project").find(new Document("_id", _id)).first();
		Date start = pj.getDate("planStart");
		Date end = pj.getDate("planFinish");
		Graphic gh = createGraphic(works, links);
		setupStartDate(gh, works, start);
		gh.schedule();

		// �����Ŀ�Ƿ���
		int warningLevel = 999;
		Document message = null;
		// 0��Ԥ�����
		float overTime = gh.getT() - ((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24));
		if (overTime > 0) {
			warningLevel = 0;// 0��Ԥ������Ŀ���ܳ��ڡ�
			message = new Document("date", new Date()).append("overdue", (int) overTime)
					.append("finish", gh.getFinishDate()).append("msg", "��ĿԤ�Ƴ���").append("duration", (int)gh.getT());

		}
		for (int i = 0; i < works.size(); i++) {
			Document doc = works.get(i);
			if ("1".equals(doc.getString("manageLevel"))) {
				if (checkOverdue(gh, doc)) {
					warningLevel = warningLevel > 1 ? 1 : warningLevel;
					message = message == null ? new Document("date", new Date()).append("msg", "һ������Ԥ�Ƴ���") : message;
				}
			}
			if ("2".equals(doc.getString("manageLevel"))) {
				if (checkOverdue(gh, doc)) {
					warningLevel = warningLevel > 2 ? 2 : warningLevel;
					message = message == null ? new Document("date", new Date()).append("msg", "��������Ԥ�Ƴ���") : message;
				}
			}
		}

		c("project").updateOne(new Document("_id", _id),
				new Document("$set", new Document("overdueIndex", warningLevel).append("scheduleEst", message)));
		return warningLevel;
	}

	private boolean checkOverdue(Graphic gh, Document doc) {
		Date planFinish = doc.getDate("planFinish");
		Date estFinish = gh.getTaskEFDate((doc.getObjectId("_id").toHexString()));
		long overTime = ((estFinish.getTime() - planFinish.getTime()) / (1000 * 60 * 60 * 24));
		if (overTime > 0) {
			c("work").updateOne(new Document("_id", doc.get("_id")),
					new Document("$set", new Document("scheduleEst", new Document("date", new Date())
							.append("overdue", (int) overTime).append("finish", estFinish).append("msg", "����Ԥ�Ƴ���"))));
			return true;
		} else {
			return false;
		}
	}

	private void setupStartDate(Graphic gh, ArrayList<Document> works, Date pjStart) {
		gh.setStartDate(pjStart);
		gh.getStartRoute().forEach(r -> {
			String id = r.end2.getId();
			ObjectId _id = new ObjectId(id);
			Document doc = works.stream().filter(d -> _id.equals(d.getObjectId("_id"))).findFirst().get();
			Date aStart = doc.getDate("actualStart");
			Date pStart = doc.getDate("planStart");
			gh.setStartDate(id, aStart == null ? pStart : aStart);
		});
	}

	/**
	 * ����work �� link ����ͼ
	 * 
	 * @param works
	 * @param links
	 * @return
	 */
	private Graphic createGraphic(ArrayList<Document> works, ArrayList<Document> links) {
		final ArrayList<Task> tasks = new ArrayList<Task>();
		works.forEach(doc -> createTask(tasks, doc));
		works.forEach(doc -> setParent(tasks, doc));
		final ArrayList<Route> routes = new ArrayList<Route>();
		links.forEach(doc -> createRoute(routes, tasks, doc));
		return new Graphic(tasks, routes);
	}

	private Route createRoute(ArrayList<Route> routes, ArrayList<Task> tasks, Document doc) {
		String end1Id = doc.getObjectId("source").toHexString();
		String end2Id = doc.getObjectId("target").toHexString();
		Task end1 = tasks.stream().filter(t -> end1Id.equals(t.getId())).findFirst().orElse(null);
		Task end2 = tasks.stream().filter(t -> end2Id.equals(t.getId())).findFirst().orElse(null);
		if (end1 != null && end2 != null) {
			String _type = doc.getString("type");
			int type = Relation.FTS;
			if ("FF".equals(_type)) {
				type = Relation.FTF;
			} else if ("SF".equals(_type)) {
				type = Relation.STF;
			} else if ("SS".equals(_type)) {
				type = Relation.STS;
			}
			Number lag = (Number) doc.get("lag");
			float interval = Optional.ofNullable(lag).map(l -> l.floatValue()).orElse(0f);
			Relation rel = new Relation(type, interval);
			Route route = new Route(end1, end2, rel);
			routes.add(route);
			return route;
		}
		return null;
	}

	private void setParent(final ArrayList<Task> tasks, final Document doc) {
		Optional.ofNullable(doc.getObjectId("parent_id")).map(_id -> _id.toHexString()).ifPresent(parentId -> {
			String id = doc.getObjectId("_id").toHexString();
			Task subTask = tasks.stream().filter(t -> id.equals(t.getId())).findFirst().orElse(null);
			Task parentTaskTask = tasks.stream().filter(t -> parentId.equals(t.getId())).findFirst().orElse(null);
			if (subTask != null && parentTaskTask != null) {
				parentTaskTask.addSubTask(subTask);
			}
		});
	}

	private Task createTask(ArrayList<Task> tasks, Document doc) {
		String id = doc.getObjectId("_id").toHexString();
		Date aStart = doc.getDate("actualStart");
		Date aFinish = doc.getDate("actualFinish");
		Date pStart = doc.getDate("planStart");
		Date pFinish = doc.getDate("planFinish");

		Date now = new Date();
		long duration;
		if (aFinish != null) {// ��������Ѿ���ɣ�����Ϊʵ�����-ʵ�ʿ�ʼ
			duration = aFinish.getTime() - aStart.getTime();
		} else if (aStart != null) {// ��������Ѿ���ʼ������û�����, ����Ҫ�ڼƻ������ϼ���ƫ����
			duration = aStart.getTime() - pStart.getTime() + pFinish.getTime() - pStart.getTime();
		} else if (now.after(pStart)) {// ���������δ��ʼ����ǰ�������Ѿ������ƻ���ʼ���ڣ�����Ҫ�ڼƻ������ϼ���ƫ����
			duration = now.getTime() - pStart.getTime() + pFinish.getTime() - pStart.getTime();
		} else {
			duration = pFinish.getTime() - pStart.getTime();
		}

		Task task = new Task(id, duration / (1000 * 60 * 60 * 24));
		task.setName(doc.getString("name"));
		tasks.add(task);
		return task;
	}

}
