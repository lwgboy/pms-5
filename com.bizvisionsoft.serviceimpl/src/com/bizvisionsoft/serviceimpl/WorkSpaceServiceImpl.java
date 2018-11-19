package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.service.WorkSpaceService;
import com.bizvisionsoft.service.model.Baseline;
import com.bizvisionsoft.service.model.Message;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.ProjectStatus;
import com.bizvisionsoft.service.model.ResourcePlan;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.RiskEffect;
import com.bizvisionsoft.service.model.User;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.model.WorkInfo;
import com.bizvisionsoft.service.model.WorkLinkInfo;
import com.bizvisionsoft.service.model.Workspace;
import com.bizvisionsoft.service.model.WorkspaceGanttData;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.service.tools.Formatter;
import com.bizvisionsoft.serviceimpl.query.JQ;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Field;

public class WorkSpaceServiceImpl extends BasicServiceImpl implements WorkSpaceService {

	@Override
	public int nextWBSIndex(BasicDBObject condition) {
		// yangjun 2018/10/31
		Document doc = c("workspace").find(condition).sort(new BasicDBObject("index", -1).append("_id", -1))
				.projection(new BasicDBObject("index", 1)).first();
		return Optional.ofNullable(doc).map(d -> d.getInteger("index", 0)).orElse(0) + 1;
	}

	public WorkInfo getWorkInfo(ObjectId _id) {
		// yangjun 2018/10/31
		List<WorkInfo> list = createTaskDataSet(new BasicDBObject("_id", _id));
		if (list.size() > 0)
			return list.get(0);

		return null;
	}

	@Override
	public List<WorkInfo> createTaskDataSet(BasicDBObject condition) {
		List<Bson> pipeline = new ArrayList<Bson>();
		pipeline.add(Aggregates.match(condition));
		pipeline.add(Aggregates.lookup("project", "project_id", "_id", "project"));
		pipeline.add(Aggregates.unwind("$project"));

		appendUserInfo(pipeline, "chargerId", "chargerInfo");

		List<Field<?>> fields = new ArrayList<Field<?>>();
		fields.add(new Field<String>("projectName", "$project.name"));
		fields.add(new Field<String>("projectNumber", "$project.id"));
		pipeline.add(Aggregates.addFields(fields));
		pipeline.add(Aggregates.project(new BasicDBObject("project", false)));
		// yangjun 2018/10/31
		pipeline.add(Aggregates.sort(new BasicDBObject("index", 1).append("_id", -1)));
		return c(WorkInfo.class).aggregate(pipeline).into(new ArrayList<WorkInfo>());
	}

	@Override
	public List<WorkLinkInfo> createLinkDataSet(BasicDBObject condition) {
		return c(WorkLinkInfo.class).find(condition).into(new ArrayList<WorkLinkInfo>());
	}

	@Override
	public WorkInfo insertWork(WorkInfo work) {
		return insert(work, WorkInfo.class);
	}

	@Override
	public WorkLinkInfo insertLink(WorkLinkInfo link) {
		return insert(link, WorkLinkInfo.class);
	}

	@Override
	public long updateWork(BasicDBObject filterAndUpdate) {
		return update(filterAndUpdate, WorkInfo.class);
	}

	@Override
	public long updateLink(BasicDBObject filterAndUpdate) {
		return update(filterAndUpdate, WorkLinkInfo.class);
	}

	@Override
	public long deleteWork(ObjectId _id) {
		return delete(_id, WorkInfo.class);
	}

	@Override
	public long deleteLink(ObjectId _id) {
		return delete(_id, WorkLinkInfo.class);
	}

	@Override
	public Result checkout(Workspace workspace, String userId, Boolean cancelCheckoutSubSchedule) {
		// ��ȡ���������Ĺ���ID inputIdsΪ��Ҫ���Ƶ�Workspace�еĹ�����inputIdHasWorksΪ��Ҫ����У��Ĺ���
		List<ObjectId> inputIds = new ArrayList<ObjectId>();
		List<ObjectId> inputIdHasWorks = new ArrayList<ObjectId>();
		// �ж��Ƿ�Ϊ��Ŀ
		if (workspace.getWork_id() == null) {
			// ��ȡ��Ŀ�����й���
			inputIds = c(Work.class).distinct("_id", new BasicDBObject("project_id", workspace.getProject_id()), ObjectId.class)
					.into(new ArrayList<ObjectId>());
			inputIdHasWorks.addAll(inputIds);
		} else {
			// ��ȡ������������¼�����
			inputIdHasWorks.add(workspace.getWork_id());
			inputIdHasWorks = getDesentItems(inputIdHasWorks, "work", "parent_id");
			// ��ȡ����������¼�����
			inputIds.addAll(inputIdHasWorks);
			inputIds.remove(workspace.getWork_id());
		}
		// �ж��Ƿ���Ҫ��鱾�ƻ���������Ա���
		if (Boolean.TRUE.equals(cancelCheckoutSubSchedule)) {
			// ��ȡҪ����������ù����¼������Ĺ�����id�����������
			List<ObjectId> spaceIds = c("work")
					.distinct("space_id", new BasicDBObject("_id", new BasicDBObject("$in", inputIdHasWorks)), ObjectId.class)
					.into(new ArrayList<ObjectId>());
			spaceIds.add(workspace.getSpace_id());
			cleanWorkspace(spaceIds);
		} else {
			// ��ȡ��������Ա����ļƻ�
			List<Bson> pipeline = new ArrayList<Bson>();
			pipeline.add(Aggregates.match(new BasicDBObject("_id", new BasicDBObject().append("$in", inputIdHasWorks)).append("checkoutBy",
					new BasicDBObject("$ne", null))));
			pipeline.add(Aggregates.lookup("user", "checkoutBy", "userId", "user"));
			pipeline.add(Aggregates.unwind("$user"));
			pipeline.add(Aggregates.project(new BasicDBObject("name", Boolean.TRUE).append("username", "$user.name")));

			BasicDBObject checkout = c("work").aggregate(pipeline, BasicDBObject.class).first();
			if (checkout != null) {
				// �类������Ա���,����ʾ����ǰ�û�
				Result result = Result.checkoutError("�ƻ����ڽ��мƻ��༭��", Result.CODE_HASCHECKOUTSUB);
				result.setResultDate(checkout);
				return result;
			}
		}

		// ���ɹ�������ʶ
		ObjectId space_id = new ObjectId();

		// �����Ŀʱ������Ŀ��Ǽ���˺͹��������
		if (workspace.getWork_id() == null) {
			c("project").updateOne(new BasicDBObject("_id", workspace.getProject_id()),
					new BasicDBObject("$set", new BasicDBObject("checkoutBy", userId).append("space_id", space_id)));
		}
		// ��work�����м���Ĺ������Ӽ���˺͹��������
		c("work").updateMany(new BasicDBObject("_id", new BasicDBObject("$in", inputIdHasWorks)),
				new BasicDBObject("$set", new BasicDBObject("checkoutBy", userId).append("space_id", space_id)));

		// ��ȡ��������������Work��List��,��Ϊ��ȡ�Ĺ�����ӹ��������
		List<Bson> pipeline = new ArrayList<Bson>();
		pipeline.add(Aggregates.match(new BasicDBObject("_id", new BasicDBObject().append("$in", inputIds))));
		pipeline.add(Aggregates.addFields(new Field<ObjectId>("space_id", space_id)));
		pipeline.add(Aggregates.project(new BasicDBObject("checkoutBy", false)));
		List<Document> works = c("work").aggregate(pipeline).into(new ArrayList<Document>());
		if (works.size() > 0) {

			// ������Ĺ�������workspace������
			c("workspace").insertMany(works);

			// ��ȡ����Ĺ�����ӹ�ϵ��������worklinksspace������
			pipeline = new ArrayList<Bson>();
			pipeline.add(Aggregates.match(
					new BasicDBObject("source", new BasicDBObject("$in", inputIds)).append("target", new BasicDBObject("$in", inputIds))));
			pipeline.add(Aggregates.addFields(new Field<ObjectId>("space_id", space_id)));

			List<Document> workLinkInfos = c("worklinks").aggregate(pipeline).into(new ArrayList<Document>());

			if (workLinkInfos.size() > 0) {
				c("worklinksspace").insertMany(workLinkInfos);
			}
		}
		return Result.checkoutSuccess("����ɹ���");
	}

	@Override
	public Result schedulePlanCheck(Workspace workspace, Boolean checkManageItem) {
		// ��ȡ����Ľڵ㡣
		if (checkManageItem) {
			List<Bson> pipeline = new ArrayList<Bson>();
			// TODO �����������ؽڵ�,Ӧ��ͨ��ϵͳ����ȷ����鼶��
			// yangjun 2018/10/31
			pipeline.add(Aggregates.match(new BasicDBObject("space_id", workspace.getSpace_id()).append("manageLevel",
					new BasicDBObject("$nin", Arrays.asList("1", "2")))));
			pipeline.add(Aggregates.lookup("work", "_id", "_id", "work"));
			pipeline.add(Aggregates.unwind("$work"));
			pipeline.add(Aggregates.project(new BasicDBObject("name", Boolean.TRUE).append("wpf",
					new BasicDBObject("$gt", new String[] { "$planFinish", "$work.planFinish" }))));
			pipeline.add(Aggregates.match(new BasicDBObject("wpf", Boolean.TRUE)));
			WorkInfo workInfo = c(WorkInfo.class).aggregate(pipeline).first();
			if (workInfo != null) {
				Date planFinish = workInfo.getPlanFinish();
				Result result = Result.checkoutError("����ڵ����ʱ�䳬���޶��ļƻ�������ڣ�Ҫ�󲻵�����" + Formatter.getString(planFinish),
						Result.CODE_UPDATEMANAGEITEM);
				result.data = new BasicDBObject("name", workInfo.getText());
				return result;
			}
		}
		Document doc = c("workspace").aggregate(Arrays.asList(new Document("$match", new Document("space_id", workspace.getSpace_id())),
				new Document("$group", new Document("_id", null).append("finish", new Document("$max", "$planFinish"))))).first();
		if (workspace.getWork_id() != null) {
			Work work = new WorkServiceImpl().getWork(workspace.getWork_id());
			Date planFinish = work.getPlanFinish();
			if (planFinish.before(doc.getDate("finish"))) {
				String name = c("workspace").distinct("name",
						new Document("space_id", workspace.getSpace_id()).append("planFinish", doc.getDate("finish")), String.class)
						.first();
				Result result = Result.checkoutError("���ʱ�䳬���׶μƻ��깤���ڣ�Ҫ�󲻵�����" + Formatter.getString(planFinish), Result.CODE_UPDATESTAGE);
				result.data = new BasicDBObject("name", name).append("planFinish", planFinish);
				return result;
			}
		} else {
			Project project = new ProjectServiceImpl().get(workspace.getProject_id());
			Date planFinish = project.getPlanFinish();
			if (planFinish.before(doc.getDate("finish"))) {
				String name = c("workspace").distinct("name",
						new Document("space_id", workspace.getSpace_id()).append("planFinish", doc.getDate("finish")), String.class)
						.first();
				Result result = Result.checkoutError("���ʱ�䳬����Ŀ�ƻ��깤���ڣ�Ҫ�󲻵�����" + Formatter.getString(planFinish), Result.CODE_UPDATEPROJECT);
				result.data = new BasicDBObject("name", name).append("planFinish", planFinish);
				return result;
			}
		}
		// ���ؼ����
		return Result.checkoutSuccess("��ͨ����顣");
	}

	@Override
	public Result checkin(Workspace workspace) {
		if (workspace.getSpace_id() == null) {
			return Result.checkoutError("�ύʧ�ܡ�", Result.CODE_ERROR);
		}

		ProjectServiceImpl projectServiceImpl = new ProjectServiceImpl();
		ObjectId project_id = workspace.getProject_id();
		projectServiceImpl.createBaseline(new Baseline().setProject_id(project_id).setCreationDate(new Date()).setName("�޸Ľ��ȼƻ�"));

		List<ObjectId> workIds = c(Work.class).distinct("_id", new BasicDBObject("space_id", workspace.getSpace_id()), ObjectId.class)
				.into(new ArrayList<ObjectId>());
		workIds.remove(workspace.getWork_id());

		List<ObjectId> workspaceIds = c(WorkInfo.class)
				.distinct("_id", new BasicDBObject("space_id", workspace.getSpace_id()), ObjectId.class).into(new ArrayList<ObjectId>());

		// ��ȡ���뼯��
		List<ObjectId> insertIds = new ArrayList<ObjectId>();
		insertIds.addAll(workspaceIds);
		insertIds.removeAll(workIds);

		// ��ȡɾ������
		List<ObjectId> deleteIds = new ArrayList<ObjectId>();
		deleteIds.addAll(workIds);
		deleteIds.removeAll(workspaceIds);

		deleteIds = getDesentItems(deleteIds, "work", "parent_id");

		// ��ȡ�޸ļ���
		List<ObjectId> updateIds = new ArrayList<ObjectId>();
		updateIds.addAll(workspaceIds);
		updateIds.removeAll(insertIds);
		updateIds.removeAll(deleteIds);

		if (!deleteIds.isEmpty()) {

			List<Work> stages = c(Work.class).find(new Document("_id", new Document("$in", deleteIds)).append("stage", true))
					.into(new ArrayList<Work>());
			if (!stages.isEmpty()) {
				stages.forEach(stage -> {
					ObjectId obs_id = stage.getOBS_id();
					if (obs_id != null)
						new OBSServiceImpl().delete(obs_id);

					ObjectId cbs_id = stage.getCBS_id();
					if (cbs_id != null)
						new CBSServiceImpl().delete(cbs_id);
				});
			}
			// ɾ������
			c(RiskEffect.class).deleteMany(new BasicDBObject("work_id", new BasicDBObject("$in", deleteIds)));
			// ɾ����Դ�ƻ�
			c(ResourcePlan.class).deleteMany(new BasicDBObject("work_id", new BasicDBObject("$in", workIds)));
			// ����ɾ������ɾ��Work
			c(Work.class).deleteMany(new BasicDBObject("_id", new BasicDBObject("$in", deleteIds)));
			// ����ɾ������ɾ����Դ�ƻ�
			c(ResourcePlan.class).deleteMany(new BasicDBObject("work_id", new BasicDBObject("$in", deleteIds)));
		}

		// ���ݲ��뼯�ϲ���Work
		if (!insertIds.isEmpty()) {
			ArrayList<Document> insertDoc = c("workspace").find(new BasicDBObject("_id", new BasicDBObject("$in", insertIds)))
					.into(new ArrayList<Document>());
			c("work").insertMany(insertDoc);
		}

		Project project = c(Project.class).find(new Document("_id", project_id)).first();
		final List<Message> messages = new ArrayList<>();

		String msgSubject = "�����ƻ�����֪ͨ";

		c("workspace").find(new BasicDBObject("_id", new BasicDBObject("$in", updateIds))).forEach((Document d) -> {
			// ����Work
			Object _id = d.get("_id");
			// ����workspace��
			d.remove("_id");
			d.remove("space_id");
			// TODO �´�״̬�������޸�֪ͨ
			boolean distributed = d.getBoolean("distributed", false);
			if (distributed) {
				Document doc = c("work").find(new Document("_id", _id)).first();
				// yangjun 2018/10/31 �޸�
				String newAssignerId = d.getString("assignerId");
				String oldAssignerId = doc.getString("assignerId");
				String newChargerId = d.getString("chargerId");
				String oldChargerId = doc.getString("chargerId");
				Date oldPlanStart = doc.getDate("planStart");
				Date newPlanStart = d.getDate("planStart");
				Date oldPlanFinish = doc.getDate("planFinish");
				Date newPlanFinish = d.getDate("planFinish");
				String checkoutBy = workspace.getCheckoutBy();

				String content = "��Ŀ��" + project.getName() + " ��������" + doc.getString("fullName");
				if (!Check.equals(oldAssignerId, newAssignerId)) {
					if (oldAssignerId != null) {
						messages.add(Message.newInstance(msgSubject, content + "�����Ѳ��ٵ��ι���ָ���ߡ�", checkoutBy, (String) oldAssignerId, null));
					}
					if (newAssignerId != null) {
						messages.add(Message.newInstance(msgSubject, content + "����ָ�������ι���ָ���ߡ�", checkoutBy, (String) newAssignerId, null));
					}
				}

				if (!Check.equals(oldChargerId, newChargerId)) {
					if (oldChargerId != null) {
						messages.add(Message.newInstance(msgSubject, content + "�����Ѳ��ٵ��ι��������ˡ�", checkoutBy, (String) oldAssignerId, null));
					}
					if (newChargerId != null) {
						messages.add(Message.newInstance(msgSubject, content + "����ָ�������ι��������ˡ�", checkoutBy, (String) oldAssignerId, null));
					}
				}

				if (!oldPlanStart.equals(newPlanStart) || !oldPlanFinish.equals(newPlanFinish)) {
					// ʹ��ͨ�õ��´﹤���ƻ���֪ͨģ��
					String chargerId = doc.getString("chargerId");
					messages.add(Message.distributeWorkMsg(msgSubject, project.getName(), doc, true, checkoutBy, chargerId));
					String assignerId = doc.getString("assignerId");
					messages.add(Message.distributeWorkMsg(msgSubject, project.getName(), doc, true, checkoutBy, assignerId));
				}
			}

			// �������˺�ָ����Ϊ�յ���� yangjun 2018/11/1
			if (Check.isNotAssigned(d.getString("chargerId")))
				d.put("chargerId", null);
			if (Check.isNotAssigned(d.getString("assignerId")))
				d.put("assignerId", null);

			c("work").updateOne(new BasicDBObject("_id", _id), new BasicDBObject("$set", d));
		});
		String projectStatus = project.getStatus();
		if (messages.size() > 0 && !ProjectStatus.Created.equals(projectStatus))
			sendMessages(messages);

		List<ObjectId> deleteResourcePlanId = new ArrayList<ObjectId>();
		Set<ObjectId> updateWorksId = new HashSet<ObjectId>();

		List<? extends Bson> pipeline = Arrays.asList(new Document("$match", new Document("work_id", new Document("$in", updateIds))),
				new Document("$lookup",
						new Document("from", "workspace")
								.append("localField", "work_id").append("foreignField", "_id").append("as", "workspace")),
				new Document("$unwind", "$workspace"),
				new Document("$addFields",
						new Document("delete",
								new Document("$or",
										Arrays.asList(new Document("$lt", Arrays.asList("$id", "$workspace.planStart")),
												new Document("$gt", Arrays.asList("$id", "$workspace.planFinish")))))),
				new Document("$match", new Document("delete", true)),
				new Document("$project", new Document("_id", true).append("work_id", true)));
		c("resourcePlan").aggregate(pipeline).forEach((Document d) -> {
			deleteResourcePlanId.add(d.getObjectId("_id"));
			updateWorksId.add(d.getObjectId("work_id"));
		});

		if (!deleteResourcePlanId.isEmpty()) {
			c("resourcePlan").deleteMany(new BasicDBObject("_id", new BasicDBObject("$in", deleteResourcePlanId)));
		}

		updateWorksId.forEach(_id -> updateWorkPlanWorks(_id));

		// ��ȡworklinksspace�еļ�¼
		List<Document> worklinks = c("worklinksspace").find(new BasicDBObject("space_id", workspace.getSpace_id()))
				.into(new ArrayList<Document>());

		// ɾ��worklinks�еļ�¼
		if (!workIds.isEmpty()) {
			c("worklinks").deleteMany(
					new BasicDBObject("source", new BasicDBObject("$in", workIds)).append("target", new BasicDBObject("$in", workIds)));
		}

		// ����ȡ��worklinksspace�еļ�¼����worklinks
		if (!worklinks.isEmpty()) {
			c("worklinks").insertMany(worklinks);
		}

		// TODO ���ȼƻ��ύ����ʾ�����¡�
		if (Result.CODE_WORK_SUCCESS == cleanWorkspace(Arrays.asList(workspace.getSpace_id())).code) {

			if (Arrays.asList(ProjectStatus.Closing, ProjectStatus.Processing).contains(projectStatus)) {
				List<ObjectId> workids = new ArrayList<ObjectId>();
				if (project.isStageEnable()) {
					/////////////////////////////////////////////////////////////////////////////////////////////////////////////
					// �´�׶μƻ�
					workids.addAll(c("work").distinct("_id", new Document("project_id", project_id)// ����Ŀ�е����н׶�
							.append("stage", true).append("distributed", new Document("$ne", true)), ObjectId.class)
							.into(new ArrayList<ObjectId>()));

					c("work").aggregate(new JQ("��ѯ-����-�׶����´�Ĺ����ƻ�").set("project_id", project_id).set("match", new Document()).array())
							.forEach((Document w) -> workids.add(w.getObjectId("_id")));
				} else {
					workids.addAll(insertIds);
				}
				/////////////////////////////////////////////////////////////////////////////////////////////////////////////
				// ���û�п��´�ļƻ�����ʾ
				if (!workids.isEmpty()) {
					/////////////////////////////////////////////////////////////////////////////////////////////////////////////
					// �����´�ƻ��ĺ���Ŀ����¼�´���Ϣ
					User user = workspace.getCheckoutUser();
					Document distributeInfo = new Document("date", new Date()).append("userId", user.getUserId()).append("userName",
							user.getName());
					c("work").updateMany(new Document("_id", new Document("$in", workids)), //
							new Document("$set", new Document("distributed", true).append("distributeInfo", distributeInfo)));
				}
			}

			if (ProjectStatus.Created.equals(projectStatus)) {
				sendMessage("��Ŀ���ȼƻ��������", "��Ŀ��" + project.getName() + " ���ȼƻ��Ѹ��¡�", workspace.getCheckoutBy(), project.getPmId(), null);
			} else {
				List<ObjectId> parentIds = c("obs").distinct("_id", new BasicDBObject("scope_id", project_id), ObjectId.class)
						.into(new ArrayList<>());
				List<ObjectId> ids = getDesentItems(parentIds, "obs", "parent_id");
				ArrayList<String> memberIds = c("obs").distinct("managerId",
						new BasicDBObject("_id", new BasicDBObject("$in", ids)).append("managerId", new BasicDBObject("$ne", null)),
						String.class).into(new ArrayList<>());

				sendMessage("��Ŀ���ȼƻ��������", "��Ŀ��" + project.getName() + " ���ȼƻ��Ѹ��¡�", workspace.getCheckoutBy(), memberIds, null);
			}

			return Result.checkoutSuccess("��Ŀ���ȼƻ��ύ�ɹ�");
		} else {
			return Result.checkoutError("��Ŀ���ȼƻ��ύʧ��", Result.CODE_ERROR);
		}
	}

	private void updateWorkPlanWorks(ObjectId work_id) {
		if (work_id != null) {
			// TODO �޸ļ��㷽ʽ
			List<? extends Bson> pipeline = Arrays.asList(new Document("$match", new Document("work_id", work_id)),
					new Document("$addFields",
							new Document("planQty", new Document("$sum", Arrays.asList("$planBasicQty", "$planOverTimeQty")))),
					new Document("$group", new Document("_id", "$work_id").append("planWorks", new Document("$sum", "$planQty"))));

			double planWorks = Optional.ofNullable(c("resourcePlan").aggregate(pipeline).first()).map(d -> (Double) d.get("planWorks"))
					.map(p -> p.doubleValue()).orElse(0d);
			c(Work.class).updateOne(new Document("_id", work_id), new Document("$set", new Document("planWorks", planWorks)));
		}
	}

	@Override
	public Result cancelCheckout(Workspace workspace) {
		if (workspace.getSpace_id() == null) {
			return Result.checkoutError("����ʧ�ܡ�", Result.CODE_ERROR);
		}

		if (Result.CODE_WORK_SUCCESS == cleanWorkspace(Arrays.asList(workspace.getSpace_id())).code) {
			return Result.checkoutSuccess("�ѳɹ�������");
		} else {
			return Result.checkoutError("����ʧ�ܡ�", Result.CODE_ERROR);
		}
	}

	private Result cleanWorkspace(List<ObjectId> spaceIds) {
		c(WorkInfo.class).deleteMany(new BasicDBObject("space_id", new BasicDBObject("$in", spaceIds)));

		c(WorkLinkInfo.class).deleteMany(new BasicDBObject("space_id", new BasicDBObject("$in", spaceIds)));

		c("project").updateOne(new BasicDBObject("space_id", new BasicDBObject("$in", spaceIds)),
				new BasicDBObject("$unset", new BasicDBObject("checkoutBy", true).append("space_id", true)));

		c("work").updateMany(new BasicDBObject("space_id", new BasicDBObject("$in", spaceIds)),
				new BasicDBObject("$unset", new BasicDBObject("checkoutBy", true).append("space_id", true)));

		return Result.checkoutSuccess("����ɳ����ɹ���");
	}

	@Override
	public List<WorkInfo> createComparableWorkDataSet(ObjectId space_id) {
		List<? extends Bson> pipeline = Arrays.asList(new Document().append("$match", new Document().append("space_id", space_id)),
				new Document().append("$lookup",
						new Document().append("from", "work").append("localField", "_id").append("foreignField", "_id").append("as",
								"work")),
				new Document().append("$unwind", new Document().append("path", "$work").append("preserveNullAndEmptyArrays", true)),
				new Document().append("$addFields",
						new Document().append("planStart1", "$work.planStart").append("planFinish1", "$work.planFinish")
								.append("actualStart1", "$work.actualStart").append("actualFinish1", "$work.actualFinish")),
				new Document().append("$project", new Document().append("work", false)));

		return c(WorkInfo.class).aggregate(pipeline).into(new ArrayList<WorkInfo>());
	}

	@Override
	public Result updateGanttData(WorkspaceGanttData ganttData) {
		// TODO Auto-generated method stub
		ObjectId space_id = ganttData.getSpace_id();

		c(WorkInfo.class).deleteMany(new Document("space_id", space_id));

		c(WorkLinkInfo.class).deleteMany(new Document("space_id", space_id));

		List<WorkInfo> workInfos = ganttData.getTasks();
		for (int i = 0; i < workInfos.size(); i++) {
			workInfos.get(i).setIndex(i);
			if (workInfos.get(i).getParent_id() == null && ganttData.getWork_id() != null) {
				workInfos.get(i).setParent_id(ganttData.getWork_id());
			}
		}

		if (workInfos.size() > 0)
			c(WorkInfo.class).insertMany(workInfos);

		List<WorkLinkInfo> links = ganttData.getLinks();
		if (links.size() > 0)
			c(WorkLinkInfo.class).insertMany(links);
		return new Result();
	}
}
