package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.ProjectStatus;
import com.bizvisionsoft.service.model.ResourcePlan;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.model.WorkLink;
import com.bizvisionsoft.service.model.WorkPackage;
import com.bizvisionsoft.service.model.Workspace;
import com.bizvisionsoft.serviceimpl.exception.ServiceException;
import com.mongodb.BasicDBObject;
import com.mongodb.Block;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Field;
import com.mongodb.client.result.UpdateResult;

public class WorkServiceImpl extends BasicServiceImpl implements WorkService {

	@Override
	public List<Work> createTaskDataSet(BasicDBObject condition) {
		return queryWork(null, null, condition, null, new BasicDBObject("index", 1)).into(new ArrayList<Work>());
	}

	private AggregateIterable<Work> queryWork(Integer skip, Integer limit, BasicDBObject basicCondition,
			BasicDBObject filter, BasicDBObject sort) {
		List<Bson> pipeline = new ArrayList<Bson>();

		if (basicCondition != null)
			pipeline.add(Aggregates.match(basicCondition));

		appendProject(pipeline);

		appendOverdue(pipeline);

		if (filter != null)
			pipeline.add(Aggregates.match(filter));

		if (sort != null)
			pipeline.add(Aggregates.sort(sort));

		if (skip != null)
			pipeline.add(Aggregates.skip(skip));

		if (limit != null)
			pipeline.add(Aggregates.limit(limit));

		AggregateIterable<Work> iterable = c(Work.class).aggregate(pipeline);
		return iterable;
	}

	private void appendProject(List<Bson> pipeline) {
		pipeline.add(Aggregates.lookup("project", "project_id", "_id", "project"));
		pipeline.add(Aggregates.unwind("$project"));
		List<Field<?>> fields = new ArrayList<Field<?>>();
		fields.add(new Field<String>("projectName", "$project.name"));
		fields.add(new Field<String>("projectNumber", "$project.id"));
		pipeline.add(Aggregates.addFields(fields));
		pipeline.add(Aggregates.project(new BasicDBObject("project", false)));
	}

	private void appendWork(List<Bson> pipeline) {
		pipeline.add(Aggregates.lookup("work", "work_id", "_id", "work"));
		pipeline.add(Aggregates.unwind("$work"));
	}

	private void appendOverdue(List<Bson> pipeline) {
		int warningDay = (int) getSystemSetting(WARNING_DAY);
		warningDay = warningDay * 24 * 60 * 60 * 1000;
		List<Field<?>> fields = new ArrayList<Field<?>>();
		fields.add(new Field<Bson>("overdue", new BasicDBObject("$cond", new BasicDBObject("if", new BasicDBObject(
				"$ne", new Object[] { new BasicDBObject("$ifNull", new Object[] { "$actualFinish", true }), true }))
						.append("else",
								new BasicDBObject("$cond", new BasicDBObject("if",
										new BasicDBObject("$lt", new Object[] { "$planFinish", new Date() }))
												.append("then", "�ѳ���")
												.append("else", new BasicDBObject("$cond",
														new BasicDBObject("if", new BasicDBObject("$lt",
																new Object[] { "$planFinish", new BasicDBObject("$add",
																		new Object[] { new Date(), warningDay }) }))
																				.append("then", "Ԥ��")
																				.append("else", "")))))
						.append("then", new BasicDBObject("$cond",
								new BasicDBObject("if",
										new BasicDBObject("$lt", new Object[] { "$planFinish", "$actualFinish" }))
												.append("then", "��").append("else", "��")))

		)));

		pipeline.add(Aggregates.addFields(fields));
	}

	@Override
	public List<WorkLink> createLinkDataSet(BasicDBObject condition) {
		return c(WorkLink.class).find(condition).into(new ArrayList<WorkLink>());
	}

	@Override
	public List<Work> listProjectRootTask(ObjectId project_id) {
		return createTaskDataSet(new BasicDBObject("project_id", project_id).append("parent_id", null));
	}

	@Override
	public long countProjectRootTask(ObjectId project_id) {
		return count(new BasicDBObject("project_id", project_id).append("parent_id", null), "work");
	}

	@Override
	public List<Work> listChildren(ObjectId parent_id) {
		return createTaskDataSet(new BasicDBObject("parent_id", parent_id));
	}

	@Override
	public long countChildren(ObjectId parent_id) {
		return count(new BasicDBObject("parent_id", parent_id), "work");
	}

	@Override
	public Work insertWork(Work work) {
		return insert(work, Work.class);
	}

	@Override
	public WorkLink insertLink(WorkLink link) {
		return insert(link, WorkLink.class);
	}

	@Override
	public long updateWork(BasicDBObject filterAndUpdate) {
		return update(filterAndUpdate, Work.class);
	}

	@Override
	public long updateLink(BasicDBObject filterAndUpdate) {
		return update(filterAndUpdate, WorkLink.class);
	}

	@Override
	public long deleteWork(ObjectId _id) {
		return delete(_id, Work.class);
	}

	@Override
	public long deleteLink(ObjectId _id) {
		return delete(_id, WorkLink.class);
	}

	@Override
	public Work getWork(ObjectId _id) {
		return queryWork(null, null, new BasicDBObject("_id", _id), null, null).first();
	}

	@Override
	public WorkLink getLink(ObjectId _id) {
		return get(_id, WorkLink.class);
	}

	@Override
	public List<Result> startStage(ObjectId _id, String executeBy) {
		List<Result> result = startStageCheck(_id, executeBy);
		if (!result.isEmpty()) {
			return result;
		}

		// �޸�״̬
		UpdateResult ur = c(Work.class).updateOne(new BasicDBObject("_id", _id),
				new BasicDBObject("$set", new BasicDBObject("status", ProjectStatus.Processing)
						.append("startOn", new Date()).append("startBy", executeBy)));

		// ����ur��������Ľ��
		if (ur.getModifiedCount() == 0) {
			throw new ServiceException("û���������������Ĺ�����");
		}

		ObjectId project_id = c("work").distinct("project_id", new BasicDBObject("_id", _id), ObjectId.class).first();
		ur = c(Project.class).updateOne(new BasicDBObject("_id", project_id),
				new BasicDBObject("$set", new BasicDBObject("stage_id", _id)));
		// ����ur��������Ľ��
		if (ur.getModifiedCount() == 0) {
			throw new ServiceException("�޷�������Ŀ��ǰ״̬��");
		}

		// TODO ֪ͨ�Ŷӳ�Ա�������Ѿ�����

		return result;
	}

	private List<Result> startStageCheck(ObjectId _id, String executeBy) {
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
	public Workspace getWorkspace(ObjectId _id) {
		BasicDBObject dbo = c("work").find(new BasicDBObject("_id", _id), BasicDBObject.class)
				.projection(new BasicDBObject("space_id", Boolean.TRUE).append("checkoutBy", Boolean.TRUE)
						.append("project_id", Boolean.TRUE))
				.first();
		return Workspace.newInstance(dbo.getObjectId("project_id"), _id, dbo.getObjectId("space_id"),
				dbo.getString("checkoutBy"));
	}

	@Override
	public List<WorkLink> createWorkLinkDataSet(ObjectId parent_id) {
		List<ObjectId> inputIds = new ArrayList<ObjectId>();
		inputIds.add(parent_id);
		inputIds = getDesentItems(inputIds, "work", "parent_id");
		return c(WorkLink.class).find(new BasicDBObject("source", new BasicDBObject("$in", inputIds)).append("target",
				new BasicDBObject("$in", inputIds))).into(new ArrayList<WorkLink>());
	}

	@Override
	public List<Work> createWorkTaskDataSet(ObjectId parent_id) {
		List<Work> result = new ArrayList<Work>();
		queryWork(null, null, new BasicDBObject("parent_id", parent_id), null, new BasicDBObject("index", 1))
				.forEach(new Block<Work>() {
					@Override
					public void apply(final Work work) {
						result.add(work);
						result.addAll(createWorkTaskDataSet(work.get_id()));
					}
				});
		return result;
	}

	@Override
	public List<WorkLink> createProjectLinkDataSet(ObjectId project_id) {
		return c(WorkLink.class).find(new BasicDBObject("project_id", project_id)).sort(new BasicDBObject("index", 1))
				.into(new ArrayList<WorkLink>());
	}

	@Override
	public List<Work> createProjectTaskDataSet(ObjectId project_id) {
		return queryWork(null, null, new BasicDBObject("project_id", project_id), null, new BasicDBObject("index", 1))
				.into(new ArrayList<Work>());
	}

	@Override
	public List<Work> createProcessingWorkDataSet(BasicDBObject condition, String userid) {
		Integer skip = Optional.ofNullable(condition).map(c -> (Integer) c.get("skip")).orElse(null);
		Integer limit = Optional.ofNullable(condition).map(c -> (Integer) c.get("limit")).orElse(null);
		BasicDBObject filter = Optional.ofNullable(condition).map(c -> (BasicDBObject) c.get("filter")).orElse(null);

		return queryWork(skip, limit, new BasicDBObject("$or",
				new BasicDBObject[] { new BasicDBObject("chargerId", userid), new BasicDBObject("assignerId", userid) })
						.append("summary", false).append("actualFinish", null).append("distributed", true),
				filter, new BasicDBObject("planFinish", 1)).into(new ArrayList<Work>());
	}

	@Override
	public long countProcessingWorkDataSet(BasicDBObject filter, String userid) {
		if (filter == null)
			filter = new BasicDBObject();

		filter.put("summary", false);
		filter.put("actualFinish", null);
		filter.put("distributed", true);
		filter.put("$or", new BasicDBObject[] { new BasicDBObject("chargerId", userid),
				new BasicDBObject("assignerId", userid) });
		return count(filter, Work.class);
	}

	@Override
	public List<Work> createFinishedWorkDataSet(BasicDBObject condition, String userid) {
		Integer skip = Optional.ofNullable(condition).map(c -> (Integer) c.get("skip")).orElse(null);
		Integer limit = Optional.ofNullable(condition).map(c -> (Integer) c.get("limit")).orElse(null);
		BasicDBObject filter = Optional.ofNullable(condition).map(c -> (BasicDBObject) c.get("filter")).orElse(null);

		return queryWork(skip, limit, new BasicDBObject("$or",
				new BasicDBObject[] { new BasicDBObject("chargerId", userid), new BasicDBObject("assignerId", userid) })
						.append("summary", false).append("actualFinish", new BasicDBObject("$ne", null)),
				filter, new BasicDBObject("actualFinish", -1)).into(new ArrayList<Work>());
	}

	@Override
	public long countFinishedWorkDataSet(BasicDBObject filter, String userid) {
		if (filter == null)
			filter = new BasicDBObject();

		if (filter.get("actualFinish") == null)
			filter.put("actualFinish", new BasicDBObject("$ne", null));

		filter.put("summary", false);
		filter.put("distributed", true);
		filter.put("$or", new BasicDBObject[] { new BasicDBObject("chargerId", userid),
				new BasicDBObject("assignerId", userid) });

		return count(filter, Work.class);
	}

	@Override
	public List<Work> createDeptUserWorkDataSet(String userid) {
		ObjectId organization_id = c("user").distinct("org_id", new BasicDBObject("userId", userid), ObjectId.class)
				.first();
		if (organization_id != null) {
			List<String> users = c("user")
					.distinct("userId", new BasicDBObject().append("org_id", organization_id), String.class)
					.into(new ArrayList<String>());

			return queryWork(null, null,
					new BasicDBObject("$or",
							new BasicDBObject[] { new BasicDBObject("chargerId", new BasicDBObject("$in", users)),
									new BasicDBObject("assignerId", new BasicDBObject("$in", users)) })
											.append("summary", false).append("actualFinish", null)
											.append("distributed", true),
					null, new BasicDBObject("chargerId", 1).append("assignerId", 1).append("planFinish", 1))
							.into(new ArrayList<Work>());
		} else
			return new ArrayList<Work>();
	}

	@Override
	public List<WorkPackage> listWorkPackage(BasicDBObject condition) {
		List<Bson> pipeline = new ArrayList<Bson>();

		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter != null)
			pipeline.add(Aggregates.match(filter));

		appendWork(pipeline);

		BasicDBObject sort = (BasicDBObject) condition.get("sort");
		if (sort != null)
			pipeline.add(Aggregates.sort(sort));

		Integer skip = (Integer) condition.get("skip");
		if (skip != null)
			pipeline.add(Aggregates.skip(skip));

		Integer limit = (Integer) condition.get("limit");
		if (limit != null)
			pipeline.add(Aggregates.limit(limit));

		appendUserInfo(pipeline, "chargerId", "chargerInfo");

		AggregateIterable<WorkPackage> iterable = c(WorkPackage.class).aggregate(pipeline);
		return iterable.into(new ArrayList<WorkPackage>());
	}

	@Override
	public long countWorkPackage(BasicDBObject filter) {
		return count(filter, WorkPackage.class);
	}

	@Override
	public WorkPackage insertWorkPackage(WorkPackage wp) {
		return insert(wp, WorkPackage.class);
	}

	@Override
	public long deleteWorkPackage(ObjectId _id) {
		return delete(_id, WorkPackage.class);
	}

	public List<Result> distributeWorkPlan(ObjectId _id, String distributeBy) {
		List<Result> result = distributeWorkPlanCheck(_id, distributeBy);
		if (!result.isEmpty()) {
			return result;
		}

		List<ObjectId> inputIds = Arrays.asList(_id);
		inputIds = getDesentItems(inputIds, "work", "parent_id");

		UpdateResult ur = c("work").updateMany(
				new BasicDBObject("_id", new BasicDBObject("$in", inputIds))
						.append("$or",
								new BasicDBObject[] { new BasicDBObject("chargerId", new Document("$ne", null)),
										new BasicDBObject("assignerId", new Document("$ne", null)) })
						.append("distributed", new BasicDBObject("$ne", true)),
				new BasicDBObject("$set", new BasicDBObject("distributed", true).append("distributeBy", distributeBy)
						.append("distributeOn", new Date())));

		if (ur.getModifiedCount() == 0) {
			result.add(Result.updateFailure("û����Ҫ�´�ļƻ���"));
			return result;
		}

		return new ArrayList<Result>();
	}

	private List<Result> distributeWorkPlanCheck(ObjectId _id, String distributeBy) {
		// TODO ����Ƿ�����´�
		return new ArrayList<Result>();
	}

	@Override
	public List<Result> startWork(ObjectId _id) {
		List<Result> result = startWorkCheck(_id);

		// ����û�п�ʼʱ���ʼʱ����ڵ�ǰʱ��ʱ�����µ�ǰ�����Ŀ�ʼʱ��Ϊ��ǰʱ�䲢��ȡ���µĹ���
		Date startDate = new Date();
		Document doc = c("work").findOneAndUpdate(
				new BasicDBObject("_id", _id).append("$or",
						new BasicDBObject[] { new BasicDBObject("actualStart", null),
								new BasicDBObject("actualStart", new BasicDBObject("$gt", startDate)) }),
				new BasicDBObject("$set", new BasicDBObject("actualStart", startDate)));

		// ���û�л�ù�����ù����Ѿ�����
		if (doc == null) {
			result.add(Result.updateFailure("�����Ѿ�������"));
			return result;
		}

		// �����ϼ�����������ʱ��
		ObjectId parent_id = doc.getObjectId("parent_id");
		if (parent_id != null)
			startParentWork(parent_id, startDate);

		return new ArrayList<Result>();
	}

	private void startParentWork(ObjectId _id, Date startDate) {
		// ����û�п�ʼʱ���ʼʱ���������ʱ��ʱ�����¹����Ŀ�ʼʱ��Ϊ��ǰʱ�䣬����ȡ���µĹ���
		Document doc = c("work").findOneAndUpdate(
				new BasicDBObject("_id", _id).append("$or",
						new BasicDBObject[] { new BasicDBObject("actualStart", null),
								new BasicDBObject("actualStart", new BasicDBObject("$gt", startDate)) }),
				new BasicDBObject("$set", new BasicDBObject("actualStart", startDate)));
		// ���û�л�ù�����ù����Ѿ�������������Ҫ����ѭ�������ϼ�����
		if (doc == null)
			return;
		// �����ϼ�����������ʱ�䣬���û���ϼ��������������Ŀ����ʱ��
		ObjectId parent_id = doc.getObjectId("parent_id");
		if (parent_id != null)
			startParentWork(parent_id, startDate);
		else
			startProject(doc.getObjectId("project_id"), startDate);

	}

	private void startProject(ObjectId _id, Date startDate) {
		c("project").updateOne(
				new BasicDBObject("_id", _id).append("$or",
						new BasicDBObject[] { new BasicDBObject("actualStart", null),
								new BasicDBObject("actualStart", new BasicDBObject("$gt", startDate)) }),
				new BasicDBObject("$set", new BasicDBObject("actualStart", startDate)));
	}

	private List<Result> startWorkCheck(ObjectId _id) {
		// TODO ����Ƿ������������
		return new ArrayList<Result>();
	}

	@Override
	public List<Result> finishWork(ObjectId _id) {
		List<Result> result = finishWorkCheck(_id);
		// ����û�����ʱ������ʱ��С�ڵ�ǰʱ��ʱ�����¹��������ʱ��Ϊ��ǰʱ�䣬����ȡ���µĹ���
		Date finishDate = new Date();
		Document doc = c("work").findOneAndUpdate(
				new BasicDBObject("_id", _id).append("$or",
						new BasicDBObject[] { new BasicDBObject("actualFinish", null),
								new BasicDBObject("actualFinish", new BasicDBObject("$lt", finishDate)) }),
				new BasicDBObject("$set", new BasicDBObject("actualFinish", finishDate)));
		// ���û�л�ù�����ù����Ѿ��깤��������Ҫ����ѭ���깤�ϼ�����
		if (doc == null) {
			result.add(Result.updateFailure("�����Ѿ���ɡ�"));
			return result;
		}

		// �����ϼ��������깤ʱ��
		ObjectId parent_id = doc.getObjectId("parent_id");
		if (parent_id != null)
			finishParentWork(parent_id, finishDate);

		return result;
	}

	private void finishParentWork(ObjectId _id, Date finishDate) {
		// �жϸù����Ƿ����δ��ɵ��ӹ����������򲻸��¸ù���ʵ�����ʱ��
		long count = c("work").count(new BasicDBObject("parent_id", _id).append("actualFinish", null));
		if (count == 0) {
			// ����û�����ʱ������ʱ��С�ڵ�ǰʱ����Ҳ�Ϊ�׶�ʱ�����¹��������ʱ��Ϊ��ǰʱ�䣬����ȡ���µĹ���
			Document doc = c("work")
					.findOneAndUpdate(
							new BasicDBObject("_id", _id)
									.append("$or",
											new BasicDBObject[] { new BasicDBObject("actualFinish", null),
													new BasicDBObject("actualFinish",
															new BasicDBObject("$lt", finishDate)) })
									.append("stage", false),
							new BasicDBObject("$set", new BasicDBObject("actualFinish", finishDate)));
			// ���û�л�ù�����ù����Ѿ��깤��������Ҫ����ѭ���깤�ϼ�����
			if (doc == null)
				return;

			// TODO ����ժҪ����ʵ�ʹ���

			// �����ϼ��������깤ʱ��
			ObjectId parent_id = doc.getObjectId("parent_id");
			if (parent_id != null)
				finishParentWork(parent_id, finishDate);
		}
	}

	private List<Result> finishWorkCheck(ObjectId _id) {
		// TODO ����Ƿ������ɹ���
		return new ArrayList<Result>();
	}

	@Override
	public List<Result> finishStage(ObjectId _id, String executeBy) {
		List<Result> result = finishStageCheck(_id, executeBy);
		if (!result.isEmpty()) {
			return result;
		}

		Document doc = c("work").find(new BasicDBObject("parent_id", _id))
				.projection(new BasicDBObject("actualFinish", true)).sort(new BasicDBObject("actualFinish", -1))
				.first();

		// �޸���Ŀ״̬
		UpdateResult ur = c("work").updateOne(new BasicDBObject("_id", _id),
				new BasicDBObject("$set",
						new BasicDBObject("status", ProjectStatus.Closing).append("finishOn", new Date())
								.append("finishBy", executeBy).append("actualFinish", doc.get("actualFinish"))));

		// ����ur��������Ľ��
		if (ur.getModifiedCount() == 0) {
			result.add(Result.updateFailure("û�������깤�����Ľ׶Ρ�"));
			return result;
		}

		// TODO ����׶�ʵ�ʹ���

		return result;
	}

	private List<Result> finishStageCheck(ObjectId _id, String executeBy) {
		//////////////////////////////////////////////////////////////////////
		// �������Ϣ
		// 1. ��������ý׶εĹ����Ƿ�ȫ���깤����û�У����󡣸��ݹ����깤���Զ����ϼ�����ʵ����ɵĹ���ֻ��Ҫ�жϽ׶���һ�������Ƿ�ȫ���깤��
		ArrayList<Result> result = new ArrayList<Result>();
		long count = c("work").count(new BasicDBObject("parent_id", _id).append("actualFinish", null));
		if (count > 0) {
			result.add(Result.finishError("�׶δ���û���깤�Ĺ�����"));
		}
		return result;
	}

	@Override
	public List<Result> closeStage(ObjectId _id, String executeBy) {
		List<Result> result = closeStageCheck(_id, executeBy);
		if (!result.isEmpty()) {
			return result;
		}

		// �޸�״̬
		UpdateResult ur = c(Work.class).updateOne(new BasicDBObject("_id", _id),
				new BasicDBObject("$set", new BasicDBObject("status", ProjectStatus.Closed)
						.append("closeOn", new Date()).append("closeBy", executeBy)));

		// ����ur��������Ľ��
		if (ur.getModifiedCount() == 0) {
			throw new ServiceException("û������ر������Ĺ�����");
		}

		// TODO ֪ͨ�Ŷӳ�Ա�������Ѿ��ر�

		return result;
	}

	private List<Result> closeStageCheck(ObjectId _id, String executeBy) {
		//////////////////////////////////////////////////////////////////////
		// �������Ϣ
		return new ArrayList<Result>();
	}

	@Override
	public ResourcePlan addResource(ResourcePlan res) {
		ResourcePlan r = insert(res, ResourcePlan.class);
		return queryResourceUsage(new Document("_id", r.get_id())).get(0);
	}

	@Override
	public List<ResourcePlan> listResource(ObjectId _id) {
		Document match = new Document("work_id", _id);
		return queryResourceUsage(match);
	}

	private List<ResourcePlan> queryResourceUsage(Document match) {
		List<? extends Bson> pipeline = Arrays.asList(new Document("$match", match),

				new Document("$lookup",
						new Document("from", "user").append("localField", "usedHumanResId")
								.append("foreignField", "userId").append("as", "user")),
				new Document("$unwind", new Document("path", "$user").append("preserveNullAndEmptyArrays", true)),

				new Document("$lookup",
						new Document("from", "equipment").append("localField", "usedEquipResId")
								.append("foreignField", "id").append("as", "equipment")),
				new Document("$unwind", new Document("path", "$equipment").append("preserveNullAndEmptyArrays", true)),

				new Document("$lookup",
						new Document("from", "resourceType").append("localField", "usedTypedResId")
								.append("foreignField", "id").append("as", "resourceType")),
				new Document("$unwind",
						new Document("path", "$resourceType").append("preserveNullAndEmptyArrays", true)),

				new Document("$addFields",
						new Document("type", new Document("$cond", Arrays.asList("$user", "������Դ",
								new Document("$cond", Arrays.asList("$equipment", "�豸��ʩ", "��Դ����")))))

										.append("resType_id", new Document("$cond", Arrays.asList(
												"$user.resourceType_id", "$user.resourceType_id",
												new Document("$cond",
														Arrays.asList("$equipment.resourceType_id",
																"$equipment.resourceType_id", "$resourceType._id")))))

										.append("name",
												new Document("$cond",
														Arrays.asList("$user.name", "$user.name",
																new Document("$cond", Arrays.asList("$equipment.name",
																		"$equipment.name", "$resourceType.name")))))

										.append("id",
												new Document("$cond",
														Arrays.asList("$usedHumanResId", "$usedHumanResId",
																new Document("$cond", Arrays.asList("$usedEquipResId",
																		"$usedEquipResId", "$usedTypedResId")))))),

				new Document("$lookup",
						new Document("from", "resourceType").append("localField", "resType_id")
								.append("foreignField", "_id").append("as", "resType")),
				new Document("$unwind", new Document("path", "$resType").append("preserveNullAndEmptyArrays", true)),

				new Document("$project",
						new Document("resourceType", false).append("resType_id", false).append("user", false)
								.append("equipment", false)),

				new Document("$sort", new Document("type", 1).append("id", 1)));

		return c(ResourcePlan.class).aggregate(pipeline).into(new ArrayList<ResourcePlan>());
	}
}
