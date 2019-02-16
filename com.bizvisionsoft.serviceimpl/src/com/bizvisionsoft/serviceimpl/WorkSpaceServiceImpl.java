package com.bizvisionsoft.serviceimpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import com.bizvisionsoft.service.model.Result;
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

	/**
	 * һ������ڵ㸺����
	 */
	private static String CHECKIN_SETTING_FIELD_CHARGER_L1 = "asgnL1";
	/**
	 * ��������ڵ㸺����
	 */
	private static String CHECKIN_SETTING_FIELD_CHARGER_L2 = "asgnL2";
	/**
	 * ��������ڵ㸺����
	 */
	private static String CHECKIN_SETTING_FIELD_CHARGER_L3 = "asgnL3";
	/**
	 * ���й���
	 */
	private static String CHECKIN_SETTING_FIELD_CHARGER_ALL = "asgnAll";

	/**
	 * һ������ڵ�ƻ����ʱ��
	 */
	private static String CHECKIN_SETTING_FIELD_SCHEDULE_L1 = "scheduleL1";
	/**
	 * ��������ڵ�ƻ����ʱ��
	 */
	private static String CHECKIN_SETTING_FIELD_SCHEDULE_L2 = "scheduleL2";
	/**
	 * ��������ڵ�ƻ����ʱ��
	 */
	private static String CHECKIN_SETTING_FIELD_SCHEDULE_L3 = "scheduleL3";
	/**
	 * ��̱��ƻ����ʱ��
	 */
	private static String CHECKIN_SETTING_FIELD_MILESTONE = "milestone";
	/**
	 * ���й����ƻ����ʱ��
	 */
	private static String CHECKIN_SETTING_FIELD_SCHEDULE_ALL = "scheduleAll";
	/**
	 * ��Ŀ�ƻ���ʼʱ��
	 */
	private static String CHECKIN_SETTING_FIELD_PROJECT_START = "projectStart";

	/**
	 * ��Ŀ�ƻ����ʱ��
	 */
	private static String CHECKIN_SETTING_FIELD_PROJECT_FINISH = "projectFinish";

	/**
	 * Ҫ�󡢽�ֹ
	 */
	private static String CHECKIN_SETTING_VALUE_REQUIREMENT = "1";
	/**
	 * ���桢ѯ��
	 */
	private static String CHECKIN_SETTING_VALUE_WARNING = "2";
	/**
	 * ���ԡ�����
	 */
	private static String CHECKIN_SETTING_VALUE_ALLOW = "3";
	/**
	 * �Զ��޸�
	 */
	private static String CHECKIN_SETTING_VALUE_AUTO = "4";

	@Override
	public List<Result> schedulePlanCheck(Workspace workspace) {
		ArrayList<Result> results = new ArrayList<Result>();

		ObjectId space_id = workspace.getSpace_id();
		ObjectId project_id = workspace.getProject_id();

		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// ��1�����ƻ���ǿ����Ҫ��
		// ��ֹ�޼ƻ����ύ
		if (c("workspace").countDocuments(new Document("space_id", space_id)) == 0) {
			results.add(Result.error("û�н��ȼƻ���"));
			return results;
		}
		// Ҫ����̱��ڵ�����й�����ϵ
		// TODO �����ų̹����Ĺ�������û���ų��Ϲ�����ϵ�Ĺ����� ���Ǹ���������ʾ��ѯ��
		// TODO �Ż����´��� ���ϳ�Ϊһ����ѯ
		List<ObjectId> milestones = c("workspace")
				.distinct("_id", new Document("space_id", space_id).append("milestone", true), ObjectId.class)
				.into(new ArrayList<ObjectId>());
		if (milestones.size() > 0) {
			List<ObjectId> sources = c("worklinksspace").distinct("source",
					new Document("space_id", space_id).append("source", new Document("$in", milestones)), ObjectId.class)
					.into(new ArrayList<ObjectId>());

			List<ObjectId> targets = c("worklinksspace").distinct("target",
					new Document("space_id", space_id).append("target", new Document("$in", milestones)), ObjectId.class)
					.into(new ArrayList<ObjectId>());

			milestones.removeAll(sources);
			milestones.removeAll(targets);
			if (milestones.size() > 0) {
				ArrayList<String> name = c("workspace").distinct("fullName",
						new Document("space_id", space_id).append("_id", new Document("$in", milestones)), String.class)
						.into(new ArrayList<String>());
				results.add(Result.error("��̱�:" + Formatter.getString(name) + " ������ڹ���������ϵ."));
			}
		}
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// ��2�������ý��м��
		Document setting = getScopeSetting(project_id, CHECKIN_SETTING_NAME);
		String settingValue;
		Map<String, Object> mLvlSetting;

		// ��2.1�������Աָ��
		// ��ȡ���й������ã�Ĭ��Ϊ������
		settingValue = getSettingValue(setting, CHECKIN_SETTING_FIELD_CHARGER_ALL, CHECKIN_SETTING_VALUE_WARNING);
		mLvlSetting = new HashMap<String, Object>();
		// ��ȡ������ؽڵ�����
		mLvlSetting.put("1", getSettingValue(setting, CHECKIN_SETTING_FIELD_CHARGER_L1, CHECKIN_SETTING_VALUE_WARNING));
		mLvlSetting.put("2", getSettingValue(setting, CHECKIN_SETTING_FIELD_CHARGER_L2, CHECKIN_SETTING_VALUE_WARNING));
		mLvlSetting.put("3", getSettingValue(setting, CHECKIN_SETTING_FIELD_CHARGER_L3, CHECKIN_SETTING_VALUE_WARNING));
		checkAssignment(settingValue, mLvlSetting, space_id, results);

		// ��2.2����鹤���ƻ�
		settingValue = getSettingValue(setting, CHECKIN_SETTING_FIELD_SCHEDULE_ALL, CHECKIN_SETTING_VALUE_ALLOW);
		mLvlSetting = new HashMap<String, Object>();
		// ��ȡ������ؽڵ�����
		mLvlSetting.put("1", getSettingValue(setting, CHECKIN_SETTING_FIELD_SCHEDULE_L1, CHECKIN_SETTING_VALUE_ALLOW));
		mLvlSetting.put("2", getSettingValue(setting, CHECKIN_SETTING_FIELD_SCHEDULE_L2, CHECKIN_SETTING_VALUE_ALLOW));
		mLvlSetting.put("3", getSettingValue(setting, CHECKIN_SETTING_FIELD_SCHEDULE_L3, CHECKIN_SETTING_VALUE_ALLOW));
		String milestoneSetting = getSettingValue(setting, CHECKIN_SETTING_FIELD_MILESTONE, CHECKIN_SETTING_VALUE_ALLOW);
		checkWorkSchedule(settingValue, mLvlSetting, milestoneSetting, space_id, results);

		// ��2.3���������Ŀ�ƻ���ʼ�ͼƻ����ʱ��Ĳ�һ��
		// ��ȡ��������ƻ���ʼʱ�������ƻ����ʱ��
		Document doc = c("workspace").aggregate(Arrays.asList(//
				new Document("$match", new Document("space_id", space_id)), //
				new Document("$group", new Document("_id", null)//
						.append("finish", new Document("$max", "$planFinish"))//
						.append("start", new Document("$min", "$planStart")))))
				.first();
		if (doc != null) {
			Document prj = getDocument(project_id, "project");
			Date earliestStart = doc.getDate("start");
			if (prj.getDate("planStart").after(earliestStart)) {
				// ��ȡ������Ŀ�ƻ���ʼ�����ã�Ĭ��Ϊ��ֹ
				settingValue = getSettingValue(setting, CHECKIN_SETTING_FIELD_PROJECT_START, CHECKIN_SETTING_VALUE_REQUIREMENT);
				if (CHECKIN_SETTING_VALUE_REQUIREMENT.equals(settingValue)) {
					results.add(Result.error("��������ƻ���ʼʱ��������Ŀ�ƻ���ʼʱ�䡣"));
				} else if (CHECKIN_SETTING_VALUE_WARNING.equals(settingValue)) {
					results.add(Result.question("��������ƻ���ʼʱ��������Ŀ�ƻ���ʼʱ�䡣"));
				}
			}
			Date latestFinish = doc.getDate("finish");
			if (prj.getDate("planFinish").before(latestFinish)) {
				// ��ȡ������Ŀ�ƻ���ɵ����ã�Ĭ��Ϊ��ֹ
				settingValue = getSettingValue(setting, CHECKIN_SETTING_FIELD_PROJECT_FINISH, CHECKIN_SETTING_VALUE_REQUIREMENT);
				if (CHECKIN_SETTING_VALUE_REQUIREMENT.equals(settingValue)) {
					results.add(Result.error("��������ƻ����ʱ��������Ŀ�ƻ����ʱ�䡣"));
				} else if (CHECKIN_SETTING_VALUE_WARNING.equals(settingValue)) {
					results.add(Result.question("��������ƻ����ʱ��������Ŀ�ƻ����ʱ�䡣"));
				}
			}
		}
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		return results;
	}

	private void checkWorkSchedule(String defaultSetting, Map<String, Object> mLvlSetting, String milestoneSetting, ObjectId space_id,
			ArrayList<Result> results) {
		List<Bson> pipeline = new ArrayList<Bson>();
		pipeline.add(Aggregates.match(new Document("space_id", space_id)));
		pipeline.add(Aggregates.lookup("work", "_id", "_id", "work"));
		pipeline.add(Aggregates.unwind("$work"));
		pipeline.add(Aggregates.project(new Document("fullName", true).append("manageLevel", true).append("wpf",
				new Document("$gt", Arrays.asList("$planFinish", "$work.planFinish")))));
		pipeline.add(Aggregates.match(new BasicDBObject("wpf", true)));
		c("workspace").aggregate(pipeline).forEach((Document d) -> {
			// �������ֵ
			Object setting = null;
			if (d.getBoolean("milestone", false)) {
				setting = Optional.ofNullable(milestoneSetting).orElse(defaultSetting);
			} else {
				setting = Optional.ofNullable(d.getString("manageLevel")).map(mLvlSetting::get).orElse(defaultSetting);
			}
			if (CHECKIN_SETTING_VALUE_REQUIREMENT.equals(setting))// ��Ӵ�����ʾ
				results.add(Result.error("������" + d.getString("fullName") + "�����ʱ�䳬���޶��ļƻ�������ڡ�"));
			else if (CHECKIN_SETTING_VALUE_WARNING.equals(setting)) // ѯ��ʱ�����ݸ�����ؽڵ����ý����ж�
				results.add(Result.warning("������" + d.getString("fullName") + "�����ʱ�䳬���޶��ļƻ�������ڡ�"));
		});
	}

	/**
	 * 
	 * @param results
	 *            ���صĽ��
	 * @param defaultSetting
	 *            Ĭ�ϵ�����
	 * @param mLvlSetting
	 *            �����������
	 * @param works
	 *            ���Ĺ���
	 */
	private void checkAssignment(String defaultSetting, Map<String, Object> mLvlSetting, ObjectId space_id, ArrayList<Result> results) {
		// ��ȡû��ָ�������˺�ָ���ߵĹ���
		c("workspace").find(new Document("space_id", space_id)//
				.append("milestone", false)//
				.append("assignerId", null)//
				.append("chargerId", null)).forEach((Document d) -> {
					// ����ֵ
					Object setting = Optional.ofNullable(d.getString("manageLevel")).map(mLvlSetting::get).orElse(defaultSetting);
					if (CHECKIN_SETTING_VALUE_WARNING.equals(setting))// ��Ӿ�����ʾ
						results.add(Result.warning("������" + d.getString("fullName") + "��û��ָ�������˺�ָ���ߡ�"));
					else if (CHECKIN_SETTING_VALUE_REQUIREMENT.equals(setting))// ��Ӵ�����ʾ
						results.add(Result.error("������" + d.getString("fullName") + "��û��ָ�������˺�ָ���ߡ�"));
				});

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
			c("riskEffect").deleteMany(new BasicDBObject("work_id", new BasicDBObject("$in", deleteIds)));
			// ����ɾ������ɾ��Work
			c(Work.class).deleteMany(new BasicDBObject("_id", new BasicDBObject("$in", deleteIds)));
			// ����ɾ������ɾ����Դ�ƻ�
			c("resourcePlan").deleteMany(new BasicDBObject("work_id", new BasicDBObject("$in", deleteIds)));
			// ����ɾ������ɾ���������ƻ���ִ��
			ArrayList<ObjectId> packageIds = c("workPackage")
					.distinct("_id", new BasicDBObject("work_id", new BasicDBObject("$in", deleteIds)), ObjectId.class)
					.into(new ArrayList<>());
			c("workPackage").deleteMany(new BasicDBObject("work_id", new BasicDBObject("$in", deleteIds)));
			c("workPackageProgress").deleteMany(new BasicDBObject("package_id", new BasicDBObject("$in", packageIds)));
		}

		// ���ݲ��뼯�ϲ���Work
		if (!insertIds.isEmpty()) {
			ArrayList<Document> insertDoc = new ArrayList<>();
			ArrayList<ObjectId> parentIds = new ArrayList<>();
			c("workspace").find(new BasicDBObject("_id", new BasicDBObject("$in", insertIds))).forEach((Document d) -> {
				insertDoc.add(d);
				parentIds.add(d.getObjectId("parent_id"));
			});
			c("work").insertMany(insertDoc);
			// ɾ����������work��parent�ķ���
			c("riskEffect").deleteMany(new BasicDBObject("work_id", new BasicDBObject("$in", parentIds)));
			// ɾ����������work��parent����Դ�ƻ�
			c("resourcePlan").deleteMany(new BasicDBObject("work_id", new BasicDBObject("$in", parentIds)));
			// ��������work��parent����Դ����
			c("resourceActual").deleteMany(new BasicDBObject("work_id", new BasicDBObject("$in", parentIds)));
			// ɾ����������work��parent�Ĺ������趨�ͼ����
			c("work").updateMany(new BasicDBObject("work_id", new BasicDBObject("$in", parentIds)),
					new BasicDBObject("$set", new BasicDBObject("workPackageSetting", null).append("checklist", null)));
			// ɾ����������work��parent�Ĺ������ƻ���ִ��
			ArrayList<ObjectId> packageIds = c("workPackage")
					.distinct("_id", new BasicDBObject("work_id", new BasicDBObject("$in", parentIds)), ObjectId.class)
					.into(new ArrayList<>());
			c("workPackage").deleteMany(new BasicDBObject("work_id", new BasicDBObject("$in", parentIds)));
			c("workPackageProgress").deleteMany(new BasicDBObject("package_id", new BasicDBObject("$in", packageIds)));
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
						messages.add(Message.newInstance(msgSubject, content + "����ָ�������ι��������ˡ�", checkoutBy, (String) newChargerId, null));
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

		// ɾ���ƻ�ʱ�������Դ�ƻ�
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

			///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// ���������á���������˰�����������Ŀ�ļƻ���ʼ�����ʱ��
			Document setting = getScopeSetting(project_id, CHECKIN_SETTING_NAME);
			String startSetting = getSettingValue(setting, CHECKIN_SETTING_FIELD_PROJECT_START, CHECKIN_SETTING_VALUE_REQUIREMENT);
			String finishSetting = getSettingValue(setting, CHECKIN_SETTING_FIELD_PROJECT_FINISH, CHECKIN_SETTING_VALUE_REQUIREMENT);
			if (CHECKIN_SETTING_VALUE_AUTO.equals(startSetting) || CHECKIN_SETTING_VALUE_AUTO.equals(finishSetting)) {
				Document doc = c("work").aggregate(Arrays.asList(//
						new Document("$match", new Document("project_id", project_id)), //
						new Document("$group", new Document("_id", null)//
								.append("finish", new Document("$max", "$planFinish"))//
								.append("start", new Document("$min", "$planStart")))))
						.first();
				if (doc != null) {
					Document set = new Document();
					if (CHECKIN_SETTING_VALUE_AUTO.equals(startSetting))
						set.append("planStart", doc.get("start"));
					if (CHECKIN_SETTING_VALUE_AUTO.equals(finishSetting))
						set.append("planFinish", doc.get("finish"));
					c("project").updateOne(new Document("_id", project_id), new Document("$set", set));
				}
			}
			///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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
