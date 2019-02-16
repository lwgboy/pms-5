package com.bizvisionsoft.serviceimpl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Generator;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.model.Baseline;
import com.bizvisionsoft.service.model.BaselineComparable;
import com.bizvisionsoft.service.model.CBSItem;
import com.bizvisionsoft.service.model.ChangeProcess;
import com.bizvisionsoft.service.model.Command;
import com.bizvisionsoft.service.model.ICommand;
import com.bizvisionsoft.service.model.Message;
import com.bizvisionsoft.service.model.News;
import com.bizvisionsoft.service.model.OBSItem;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.ProjectChange;
import com.bizvisionsoft.service.model.ProjectChangeTask;
import com.bizvisionsoft.service.model.ProjectScheduleInfo;
import com.bizvisionsoft.service.model.ProjectStatus;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.Role;
import com.bizvisionsoft.service.model.SalesItem;
import com.bizvisionsoft.service.model.Stockholder;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.model.WorkReport;
import com.bizvisionsoft.service.model.Workspace;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.service.tools.Formatter;
import com.bizvisionsoft.serviceimpl.exception.ServiceException;
import com.bizvisionsoft.serviceimpl.query.JQ;
import com.bizvisionsoft.serviceimpl.renderer.ProjectChangeRenderer;
import com.bizvisionsoft.serviceimpl.renderer.ProjectRenderer;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoBulkWriteException;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Field;
import com.mongodb.client.result.UpdateResult;

/**
 * @author hua
 *
 */
public class ProjectServiceImpl extends BasicServiceImpl implements ProjectService {

	@Override
	public Project insert(Project input) {
		// TODO ��¼������
		Project project = null;
		if (input.getProjectTemplate_id() == null) {
			/////////////////////////////////////////////////////////////////////////////
			// ����׼��
			ObjectId obsRoot_id = new ObjectId();// ��֯��
			ObjectId cbsRoot_id = new ObjectId();// Ԥ���

			ObjectId program_id = input.getProgram_id();
			ObjectId obsParent_id = null;// ��֯�ϼ�
			ObjectId cbsParent_id = null;// �ɱ��ϼ�
			if (program_id != null) {
				// ����ϼ�obs_id
				Document doc = c("program").find(new BasicDBObject("_id", program_id))
						.projection(new BasicDBObject("obs_id", true).append("cbs_id", true)).first();
				obsParent_id = Optional.ofNullable(doc).map(d -> d.getObjectId("obs_id")).orElse(null);
				cbsParent_id = Optional.ofNullable(doc).map(d -> d.getObjectId("cbs_id")).orElse(null);

			}
			/////////////////////////////////////////////////////////////////////////////
			// 0. ������Ŀ
			try {
				project = insert(input.setOBS_id(obsRoot_id).setCBS_id(cbsRoot_id), Project.class);
			} catch (Exception e) {
				if (e instanceof MongoBulkWriteException) {
					throw new ServiceException(e.getMessage());
				}
			}
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
			cbsRoot.setName(project.getName());
			insert(cbsRoot, CBSItem.class);

		} else {
			try {
				project = insert(input, Project.class);
			} catch (Exception e) {
				if (e instanceof MongoBulkWriteException) {
					throw new ServiceException(e.getMessage());
				}
			}
		}

		/////////////////////////////////////////////////////////////////////////////
		// ��Ŀ����
		// ���Ĭ��ȫ����Ŀ����
		ObjectId scope_id = project.getScope_id();
		List<Document> list = PROJECT_SETTING_NAMES.stream().map(name -> {
			Document setting = getSystemSetting(name);
			setting.put("name", name + "@" + scope_id.toString());
			setting.remove("_id");
			return setting;
		}).collect(Collectors.toList());
		c("setting").insertMany(list);

		return get(project.get_id());
	}

	@Override
	public Project get(ObjectId _id) {
		List<Project> ds = list(new BasicDBObject("filter", new BasicDBObject("_id", _id)));
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
	public List<Project> list(BasicDBObject condition) {
		Integer skip = (Integer) condition.get("skip");
		Integer limit = (Integer) condition.get("limit");
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		BasicDBObject sort = (BasicDBObject) condition.get("sort");
		return query(skip, limit, filter, sort);
	}

	private List<Project> query(Integer skip, Integer limit, BasicDBObject filter, BasicDBObject sort) {
		List<Bson> pipeline = appendQueryPipeline(skip, limit, filter, sort, new ArrayList<>());
		return c(Project.class).aggregate(pipeline).into(new ArrayList<Project>());
	}

	private List<Bson> appendQueryPipeline(Integer skip, Integer limit, BasicDBObject filter, BasicDBObject sort, List<Bson> pipeline) {
		// 1. �е���֯
		appendOrgFullName(pipeline, "impUnit_id", "impUnitOrgFullName");

		appendUserInfo(pipeline, "pmId", "pmInfo");

		appendStage(pipeline, "stage_id", "stage");

		appendLookupAndUnwind(pipeline, "program", "program_id", "program");

		appendLookupAndUnwind(pipeline, "eps", "eps_id", "eps");

		appendLookupAndUnwind(pipeline, "project", "parentProject_id", "parentProject");

		if (filter != null)
			pipeline.add(Aggregates.match(filter));

		if (sort != null)
			pipeline.add(Aggregates.sort(sort));
		else
			// yangjun 2018/10/31
			pipeline.add(Aggregates.sort(new BasicDBObject("planFinish", 1).append("_id", -1)));

		if (skip != null)
			pipeline.add(Aggregates.skip(skip));

		if (limit != null)
			pipeline.add(Aggregates.limit(limit));

		// yangjun 2018/11/1
		pipeline.addAll(new JQ("׷��-��Ŀ-�����빤ʱ").array());

		pipeline.addAll(new JQ("׷��-��Ŀ-SAR").array());

		return pipeline;
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
		try {
			return update(fu, Project.class);
		} catch (Exception e) {
			handleMongoException(e, "��Ŀ" + fu);
		}
		return 0;
	}

	/**
	 * ʹ��update���޸���Ŀ
	 */
	@Override
	@Deprecated
	public void updateProjectId(ObjectId _id, String id) {
		Document cond = new Document("_id", _id);
		Project project = c(Project.class).find(cond).first();

		if (!ProjectStatus.Created.equals(project.getStatus())) {
			throw new ServiceException("ֻ������Ŀ����ǰ���ñ��");
		}

		try {
			String workOrder = generateWorkOrder(_id);
			c("project").updateOne(cond, new Document("$set", new Document("id", id).append("workOrder", workOrder)));
		} catch (Exception e) {
			throw handleMongoException(e, "��Ŀ���" + id);
		}
	}

	@Override
	public void approveProject(Command com) {
		Document cond = new Document("_id", com._id);
		Project project = c(Project.class).find(cond).first();

		if (!ProjectStatus.Created.equals(project.getStatus())) {
			throw new ServiceException("����������Ŀ�����ٴ���׼");
		}

		if (Boolean.TRUE.equals(project.getStartApproved())) {
			throw new ServiceException("����׼����Ŀ�����ٴ���׼");
		}

		c("project").updateOne(cond, new Document("$set", new Document("startApproved", true).append("approveInfo", com.info())));
	}

	@Override
	public List<Result> startProject(Command com) {
		/////////////////////////////////////////////////////////////////////////////
		// �����Ŀ����
		List<Result> result = startProjectCheck(com._id, com.userId);
		if (result.stream().anyMatch(r -> Result.TYPE_ERROR == r.type// ����ı��뷵��
				|| (Result.TYPE_WARNING == r.type && ICommand.Start_Project.equals(com.name)))) {// �����Ծ���ı��뷵��
			return result;
		}
		/////////////////////////////////////////////////////////////////////////////
		// �޸���Ŀ״̬
		c("project").updateOne(new Document("_id", com._id),
				new Document("$set", new Document("status", ProjectStatus.Processing).append("startInfo", com.info())));

		List<ObjectId> ids;

		boolean stageEnabled = getValue("project", "stageEnable", com._id, Boolean.class);

		if (stageEnabled) {
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// �´�׶μƻ�
			ids = c("work").distinct("_id", new Document("project_id", com._id)// ����Ŀ�е����н׶�
					.append("stage", true), ObjectId.class).into(new ArrayList<ObjectId>());
		} else {
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// �´�ǽ׶ι������Ŀ�Ĺ����ƻ�
			ids = c("work").distinct("_id", new Document("project_id", com._id)// ����Ŀ�е����й���
					, ObjectId.class).into(new ArrayList<ObjectId>());
		}
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// �����´�ƻ��Ĺ�������Ŀ����¼�´���Ϣ
		if (!ids.isEmpty()) {
			Document distributeInfo = com.info();
			c("work").updateMany(new Document("_id", new Document("$in", ids)), //
					new Document("$set", new Document("distributed", true).append("distributeInfo", distributeInfo)));
			c("project").updateOne(new Document("_id", com._id), //
					new Document("$set", new Document("distributed", true).append("distributeInfo", distributeInfo)));
		}

		/////////////////////////////////////////////////////////////////////////////
		// ֪ͨ��Ŀ�Ŷӳ�Ա����Ŀ�Ѿ�����
		List<String> memberIds = getProjectMembers(com._id);
		sendMessage("��Ŀ����֪ͨ", "��Ŀ��" + getName("project", com._id) + " ���� " + Message.format(com.date) + " ������", com.userId, memberIds,
				null);
		return new ArrayList<>();
	}

	private List<String> getProjectMembers(ObjectId _id) {
		List<ObjectId> parentIds = c("obs").distinct("_id", new BasicDBObject("scope_id", _id), ObjectId.class).into(new ArrayList<>());
		List<ObjectId> ids = getDesentItems(parentIds, "obs", "parent_id");
		ArrayList<String> memberIds = c("obs").distinct("managerId",
				new BasicDBObject("_id", new BasicDBObject("$in", ids)).append("managerId", new BasicDBObject("$ne", null)), String.class)
				.into(new ArrayList<>());
		return memberIds;
	}

	/**
	 * ��ĿԤ��
	 */
	private static String START_SETTING_FIELD_BUDGET = "budget";
	/**
	 * ��Ŀ��Դ
	 */
	private static String START_SETTING_FIELD_RESOURCE = "resource";
	/**
	 * ��Ŀ�Ŷ�
	 */
	private static String START_SETTING_FIELD_OBS = "obs";
	/**
	 * ��Ŀ����
	 */
	private static String START_SETTING_FIELD_RBS = "rbs";
	/**
	 * ��Ŀ�ƻ�
	 */
	private static String START_SETTING_FIELD_PLAN = "plan";
	/**
	 * һ������ڵ�
	 */
	private static String START_SETTING_FIELD_CHARGER_L1 = "asgnL1";
	/**
	 * ��������ڵ�
	 */
	private static String START_SETTING_FIELD_CHARGER_L2 = "asgnL2";
	/**
	 * ��������ڵ�
	 */
	private static String START_SETTING_FIELD_CHARGER_L3 = "asgnL3";
	/**
	 * ���й���
	 */
	private static String START_SETTING_FIELD_CHARGER_ALL = "asgnAll";
	/**
	 * ������׼
	 */
	private static String START_SETTING_FIELD_APPROVEONSTART = "appvOnStart";

	/**
	 * Ҫ��
	 */
	private static String START_SETTING_VALUE_REQUIREMENT = "1";
	/**
	 * ���棨Ĭ�ϣ�
	 */
	private static String START_SETTING_VALUE_WARNING = "2";
	/**
	 * ����
	 */
	private static String START_SETTING_VALUE_IGNORE = "3";

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
		ArrayList<Result> results = new ArrayList<Result>();

		Project project = get(_id);
		// ��ȡ��Ŀ��������
		Document systemSetting = Optional.ofNullable(getSystemSetting(START_SETTING_NAME + "@" + _id.toString())).orElse(new Document());
		// ����Ŀ���������л�ȡ��ĿԤ������
		
		Object setting;
		setting = getSettingValue(systemSetting, START_SETTING_FIELD_BUDGET, START_SETTING_VALUE_WARNING);
		// ����Ϊ����ʱ�������м�飬
		if (!START_SETTING_VALUE_IGNORE.equals(setting)) {
			// ������Ŀ��cbs_id��ȡ��ǰ��Ŀ������CBS�ڵ�Id��
			List<ObjectId> cbsIds = new ArrayList<>();
			lookupDesentItems(Arrays.asList(project.getCBS_id()), "cbs", "parent_id", true).forEach((Document d) -> {
				cbsIds.add(d.getObjectId("_id"));
			});
			// ���û�б���cbsItem��Ԥ��Ϳ�ĿԤ�㣬�������ʾ
			long l = c("cbsPeriod").countDocuments(new Document("cbsItem_id", new Document("$in", cbsIds)));
			if (l == 0) {
				l = c("cbsSubject").countDocuments(new Document("cbsItem_id", new Document("$in", cbsIds)));
				if (l == 0)
					// �������Ϊ���棬�򷵻ؾ��棻�������ΪҪ���򷵻ش���
					if (START_SETTING_VALUE_WARNING.equals(setting))
						results.add(Result.warning("��Ŀ��δ����Ԥ�㡣"));
					else if (START_SETTING_VALUE_REQUIREMENT.equals(setting))
						results.add(Result.error("��Ŀ��δ����Ԥ�㡣"));

			}
		}

		// ��ȡ��Ŀ�Ŷ�����
		setting = getSettingValue(systemSetting, START_SETTING_FIELD_OBS, START_SETTING_VALUE_WARNING);

		// ����Ϊ����ʱ�������м�飬
		if (!START_SETTING_VALUE_IGNORE.equals(setting)) {
			// ��ȡ��Ŀ�Ŷ��зǸ��ڵ㣬���Ҹýڵ�û�е����ߺͳ�Ա�ļ�¼������¼��Ϊ0ʱ������ӷ�����Ϣ��
			long l = c("obs").countDocuments(
					new Document("scope_id", _id).append("scopeRoot", false).append("managerId", null).append("member", null));
			if (l > 0)
				// �������Ϊ���棬�򷵻ؾ��棻�������ΪҪ���򷵻ش���
				if (START_SETTING_VALUE_WARNING.equals(setting))
					results.add(Result.warning("��Ŀ��δ�齨��Ŀ�Ŷӣ����ߴ���û�е����ߺͳ�Ա�Ľ�ɫ���Ŷӡ�"));
				else if (START_SETTING_VALUE_REQUIREMENT.equals(setting))
					results.add(Result.error("��Ŀ��δ�齨��Ŀ�Ŷӣ����ߴ���û�е����ߺͳ�Ա�Ľ�ɫ���Ŷӡ�"));
		}

		// ��ȡ��Ŀ��������
		setting = getSettingValue(systemSetting, START_SETTING_FIELD_RBS, START_SETTING_VALUE_WARNING);
		// ����Ϊ����ʱ�������м�飬
		if (!START_SETTING_VALUE_IGNORE.equals(setting)) {
			// ��ȡ��Ŀ�������еļ�¼������¼��Ϊ0ʱ������ӷ�����Ϣ��
			long l = c("rbsItem").countDocuments(new Document("project_id", _id).append("parent_id", null));
			if (l == 0)
				// �������Ϊ���棬�򷵻ؾ��棻�������ΪҪ���򷵻ش���
				if (START_SETTING_VALUE_WARNING.equals(setting))
					results.add(Result.warning("��Ŀ��δ���������"));
				else if (START_SETTING_VALUE_REQUIREMENT.equals(setting))
					results.add(Result.error("��Ŀ��δ���������"));
		}
		// ��ȡ��Ŀ��Դ����
		setting = getSettingValue(systemSetting, START_SETTING_FIELD_RESOURCE, START_SETTING_VALUE_WARNING);

		// ����Ϊ����ʱ�������м�飬
		if (!START_SETTING_VALUE_IGNORE.equals(setting)) {
			// ������з���̱���Ҷ�ӽڵ��id
			List<ObjectId> workIds = c("work")
					.distinct("_id", new Document("project_id", _id).append("milestone", false).append("summary", false), ObjectId.class)
					.into(new ArrayList<>());
			// ���û�н��ȼƻ�����������Դ���
			if (workIds.size() > 0) {
				// ��ȡ��д����Դ�ƻ��Ĺ���id
				List<ObjectId> resourceWorkIds = c("resourcePlan")
						.distinct("work_id", new Document("work_id", new Document("$in", workIds)), ObjectId.class).into(new ArrayList<>());
				String message = "";
				if (resourceWorkIds.size() == 0) {
					message = "��Ŀ��δ������Դ�ƻ�";
				} else {
					workIds.removeAll(resourceWorkIds);
					if (workIds.size() > 0) {
						message = Formatter
								.getString(c("work").distinct("fullName", new Document("_id", new Document("$in", workIds)), String.class)
										.into(new ArrayList<>()));
						message = "������" + message + " δ������Դ�ƻ�";
					}
				}
				if (!message.isEmpty() && START_SETTING_VALUE_WARNING.equals(setting))
					results.add(Result.warning(message));
				else if (!message.isEmpty() && START_SETTING_VALUE_REQUIREMENT.equals(setting))
					results.add(Result.error(message));
			}

		}
		// ��ȡ��Ŀ�ƻ�����
		setting = getSettingValue(systemSetting, START_SETTING_FIELD_PLAN, START_SETTING_VALUE_WARNING);
		// ����Ϊ����ʱ�������м�飬
		if (!START_SETTING_VALUE_IGNORE.equals(setting)) {
			// ��ȡ��Ŀ���ȼƻ��е������¼������¼��Ϊ0ʱ������ӷ�����Ϣ��
			long l = c("work").countDocuments(new Document("project_id", _id).append("parent_id", null));
			if (l == 0)
				// �������Ϊ���棬�򷵻ؾ��棻�������ΪҪ���򷵻ش���
				if (START_SETTING_VALUE_WARNING.equals(setting))
					results.add(Result.warning("��Ŀ��δ�������ȼƻ���"));
				else if (START_SETTING_VALUE_REQUIREMENT.equals(setting))
					results.add(Result.error("��Ŀ��δ�������ȼƻ���"));

			// ��ȡ�Ǽ�ؽڵ�����
			setting = getSettingValue(systemSetting, START_SETTING_FIELD_CHARGER_ALL, START_SETTING_VALUE_WARNING);

			// ��ȡδ���ø����˺Ͳ����ߵĽڵ�����
			List<Document> work = c("work")
					.find(new Document("project_id", _id).append("milestone", false).append("assignerId", null).append("chargerId", null))
					.into(new ArrayList<>());
			if (START_SETTING_VALUE_WARNING.equals(setting) && work.size() > 0) {
				results.add(Result
						.warning("������" + Formatter.getString(work.stream().map(d -> d.getString("fullName")).collect(Collectors.toList()))
								+ " û��ָ�������˺�ָ����."));
			} else if (START_SETTING_VALUE_REQUIREMENT.equals(setting) && work.size() > 0) {
				results.add(Result
						.error("������" + Formatter.getString(work.stream().map(d -> d.getString("fullName")).collect(Collectors.toList()))
								+ " û��ָ�������˺�ָ����."));
			} else if (START_SETTING_VALUE_IGNORE.equals(setting) && work.size() > 0) {
				Map<String, String> manageLevels = new HashMap<String, String>();
				List<String> warning = new ArrayList<String>();
				List<String> error = new ArrayList<String>();

				// ��ȡһ����ؽڵ�����
				setting = getSettingValue(systemSetting, START_SETTING_FIELD_CHARGER_L1, START_SETTING_VALUE_WARNING);

				if (!START_SETTING_VALUE_IGNORE.equals(setting))
					manageLevels.put("1", setting.toString());

				// ��ȡ������ؽڵ�����
				setting = getSettingValue(systemSetting, START_SETTING_FIELD_CHARGER_L2, START_SETTING_VALUE_WARNING);

				if (!START_SETTING_VALUE_IGNORE.equals(setting))
					manageLevels.put("2", setting.toString());

				// ��ȡ������ؽڵ�����
				setting = getSettingValue(systemSetting, START_SETTING_FIELD_CHARGER_L3, START_SETTING_VALUE_WARNING);

				if (!START_SETTING_VALUE_IGNORE.equals(setting))
					manageLevels.put("3", setting.toString());

				work.forEach((Document d) -> {
					// �������ͻ�ȡ�ڵ����ã���������ӵ���Ӧ�ļ����С�
					if (START_SETTING_VALUE_WARNING.equals(manageLevels.get(d.getString("manageLevel"))))
						warning.add(d.getString("fullName"));
					else if (START_SETTING_VALUE_REQUIREMENT.equals(manageLevels.get(d.getString("manageLevel"))))
						error.add(d.getString("fullName"));

				});
				// ��Ӿ�����ʾ
				if (warning.size() > 0)
					results.add(Result.warning("������" + Formatter.getString(warning) + " û��ָ�������˺�ָ����."));
				// ��Ӵ�����ʾ
				if (error.size() > 0)
					results.add(Result.error("������" + Formatter.getString(error) + " û��ָ�������˺�ָ����."));
			}

		}

		// ��ȡ������׼����
		setting = getSettingValue(systemSetting, START_SETTING_FIELD_APPROVEONSTART, false);

		// �����Ҫ��׼������������Ŀû����׼�������򷵻ش���
		if (Boolean.TRUE.equals(setting) && !Boolean.TRUE.equals(project.getStartApproved())) {
			results.add(Result.error("��Ŀ��δ���������׼��"));
		}
		return results;
	}

	public List<Result> distributeProjectPlan(Command com) {
		final List<Message> msg = new ArrayList<>();
		final Set<ObjectId> ids = new HashSet<>();

		Project project = get(com._id, Project.class);
		final String projectName = project.getName();
		boolean stageEnabled = project.isStageEnable();

		if (stageEnabled) {
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// �´�׶μƻ�(�׶� �Ѵ���)
			c("work").find(//
					new Document("project_id", com._id)// ����Ŀ��
							.append("chargerId", new Document("$ne", null))// �����˲�Ϊ��
							// .append("distributed", new Document("$ne", true))// û���´��
							.append("status", ProjectStatus.Created) // �Ѵ����Ľ׶�
							.append("stage", true))// �׶�
					.forEach((Document w) -> {
						ids.add(w.getObjectId("_id"));
						msg.add(Message.distributeStageMsg(projectName, w, com.userId, w.getString("chargerId")));
					});
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// �´﹤���ƻ����׶��ǽ����еģ������ܳ��͹����ģ�û���´�ƻ��Ĺ���
			c("work").aggregate(new JQ("��ѯ-����-�׶����´�Ĺ����ƻ�").set("project_id", com._id).set("match", new Document()).array())
					.forEach((Document w) -> {
						ids.add(w.getObjectId("_id"));
						Check.isAssigned(w.getString("chargerId"),
								c -> msg.add(Message.distributeWorkMsg("�����ƻ��´�֪ͨ", projectName, w, true, com.userId, c)));
						Check.isAssigned(w.getString("assignerId"),
								c -> msg.add(Message.distributeWorkMsg("�����ƻ��´�֪ͨ", projectName, w, false, com.userId, c)));
					});
		} else {
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// �´�ǽ׶ι������Ŀ�Ĺ����ƻ�
			c("work").find(//
					new Document("project_id", com._id)// ����Ŀ��
							.append("chargerId", new Document("$ne", null))// �����˲�Ϊ��
			// .append("distributed", new Document("$ne", true))// û���´��
			).forEach((Document w) -> {
				ids.add(w.getObjectId("_id"));
				msg.add(Message.distributeStageMsg(projectName, w, com.userId, w.getString("chargerId")));
			});
		}
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// ���û�п��´�ļƻ�����ʾ
		if (ids.isEmpty()) {
			return Arrays.asList(Result.info("û����Ҫ�´�ļƻ���"));
		}
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// �����´�ƻ��Ĺ�������Ŀ����¼�´���Ϣ
		Document distributeInfo = com.info();
		c("work").updateMany(new Document("_id", new Document("$in", new ArrayList<>(ids))), //
				new Document("$set", new Document("distributed", true).append("distributeInfo", distributeInfo)));
		c("project").updateOne(new Document("_id", com._id), //
				new Document("$set", new Document("distributed", true).append("distributeInfo", distributeInfo)));
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// ������Ϣ֪ͨ
		sendMessages(msg);

		return new ArrayList<>();
	}

	@Override
	public List<Work> listStage(ObjectId _id) {
		return new WorkServiceImpl().query(null, null, new BasicDBObject("project_id", _id).append("stage", true),
				new BasicDBObject("wbsCode", 1), Work.class);
	}

	@Override
	public long countStage(ObjectId _id) {
		return c("work").countDocuments(new BasicDBObject("project_id", _id).append("stage", true));
	}

	@Override
	public List<Work> listMyStage(String userId) {
		return new WorkServiceImpl().createTaskDataSet(
				new BasicDBObject("$or", Arrays.asList(new BasicDBObject("chargerId", userId), new BasicDBObject("assignerId", userId)))
						.append("stage", true).append("status", new BasicDBObject("$in",
								Arrays.asList(ProjectStatus.Created, ProjectStatus.Processing, ProjectStatus.Closing))));
	}

	@Override
	public long countMyStage(String userId) {
		return count(
				new BasicDBObject("$or", Arrays.asList(new BasicDBObject("chargerId", userId), new BasicDBObject("assignerId", userId)))
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
	public long updateStockholder(BasicDBObject fu) {
		return update(fu, Stockholder.class);
	}

	@Override
	public List<Project> listManagedProjects(BasicDBObject condition, String userid) {
		Integer skip = (Integer) condition.get("skip");
		Integer limit = (Integer) condition.get("limit");
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		// � 2018/10/30
		if (filter == null) {
			filter = new BasicDBObject();
			// condition.put("filter", filter);
		}
		BasicDBObject sort = (BasicDBObject) condition.get("sort");
		// if (sort == null) {
		// sort = new BasicDBObject();
		// condition.put("sort", sort);
		// }

		filter.put("pmId", userid);
		return query(skip, limit, filter, sort);
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
		return iterateParticipatedProject(condition, userId).into(new ArrayList<>());
	}

	@Override
	public List<Document> listParticipatedProjectsCard(BasicDBObject condition, String userId) {
		return iterateParticipatedProject(condition, userId).map(ProjectRenderer::render).into(new ArrayList<>());
	}

	private AggregateIterable<Project> iterateParticipatedProject(BasicDBObject condition, String userId) {
		Integer skip = (Integer) condition.get("skip");
		Integer limit = (Integer) condition.get("limit");
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		BasicDBObject sort = (BasicDBObject) condition.get("sort");

		List<Bson> pipeline = new ArrayList<Bson>();
		// � 2018/11/1
		pipeline.addAll(new JQ("��ѯ-��Ŀ-������").set("userId", userId).array());

		appendQueryPipeline(skip, limit, filter, sort, pipeline);

		AggregateIterable<Project> iterable = c("obs").aggregate(pipeline, Project.class);
		return iterable;
	}

	@Override
	public long countParticipatedProjects(BasicDBObject filter, String userId) {
		List<Bson> pipeline = new ArrayList<Bson>();
		// � 2018/11/1
		pipeline.addAll(new JQ("��ѯ-��Ŀ-������").set("userId", userId).array());
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
								.append("$and", Arrays.asList(
										new Document("$or",
												Arrays.asList(new Document("actualFinish", null),
														new Document("actualFinish", new Document("$gte", startWorkFinish)))),
										new Document("$or",
												Arrays.asList(new Document("chargerId", userId), new Document("assignerId", userId))))),
						ObjectId.class)
				.into(new ArrayList<ObjectId>());

		List<Bson> pipeline = new ArrayList<Bson>();
		pipeline.add(Aggregates.match(new Document("_id", new Document("$in", project_ids)).append("status",
				new Document("$in", Arrays.asList(ProjectStatus.Processing, ProjectStatus.Closing)))));

		if (filter != null)
			pipeline.add(Aggregates.match(filter));

		if (skip != null)
			pipeline.add(Aggregates.skip(skip));

		if (limit != null)
			pipeline.add(Aggregates.limit(limit));

		appendOrgFullName(pipeline, "impUnit_id", "impUnitOrgFullName");

		appendUserInfo(pipeline, "pmId", "pmInfo");

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
								.append("$and", Arrays.asList(
										new Document("$or",
												Arrays.asList(new Document("actualFinish", null),
														new Document("actualFinish", new Document("$gte", startWorkFinish)))),
										new Document("$or",
												Arrays.asList(new Document("chargerId", userId), new Document("assignerId", userId))))),
						ObjectId.class)
				.into(new ArrayList<ObjectId>());

		pipeline.add(Aggregates.match(new Document("_id", new Document("$in", project_ids))));
		if (filter != null)
			pipeline.add(new Document("$match", filter));

		return c("project").aggregate(pipeline).into(new ArrayList<>()).size();
	}

	@Override
	public long delete(ObjectId _id) {
		Project project = get(_id);
		if (!ProjectStatus.Created.equals(project.getStatus())) {
			throw new ServiceException("��ǰ��Ŀ������ɾ����ֻ��ɾ���Ѵ���״̬����Ŀ��");
		}
		// ������е�work
		List<ObjectId> workIds = c("work").distinct("_id", new Document("project_id", _id), ObjectId.class).into(new ArrayList<>());
		// ���������resourcePlan
		c("resourcePlan").deleteMany(new Document("work_id", new Document("$in", workIds)));
		// ���������resourceActual
		c("resourceActual").deleteMany(new Document("work_id", new Document("$in", workIds)));

		// ���������workPackage
		c("workPackage").deleteMany(new Document("work_id", new Document("$in", workIds)));
		// ���worklinks
		c("worklinks").deleteMany(new Document("project_id", _id));
		// ���work
		c("work").deleteMany(new Document("project_id", _id));
		// ���workspace
		c("workspace").deleteMany(new Document("project_id", _id));
		// ���worklinksspace
		c("worklinksspace").deleteMany(new Document("project_id", _id));

		// ���obs
		c("obs").deleteMany(
				new Document("$or", Arrays.asList(new Document("scope_id", new Document("$in", workIds)), new Document("scope_id", _id))));

		// ���cbs
		List<ObjectId> cbsIds = c("cbs")
				.distinct("_id", new Document("$or", Arrays.asList(new Document("scope_id", new Document("$in", workIds)),
						new Document("scopeRoot", false).append("scope_id", _id))), ObjectId.class)
				.into(new ArrayList<>());
		c("cbs").deleteMany(new Document("_id", new Document("$in", cbsIds)));
		c("cbsPeriod").deleteMany(new Document("cbsItem_id", new Document("$in", cbsIds)));
		c("cbsSubject").deleteMany(new Document("cbsItem_id", new Document("$in", cbsIds)));

		// ���folder
		c("folder").deleteMany(new Document("project_id", _id));

		// ���baseline
		c("baseline").deleteMany(new Document("project_id", _id));
		c("baselineWork").deleteMany(new Document("project_id", _id));
		c("baselineWorkLinks").deleteMany(new Document("project_id", _id));

		// ���projectChange
		c("projectChange").deleteMany(new Document("project_id", _id));

		// ���rbs
		List<ObjectId> rbsIds = c("rbsItem").distinct("_id", new Document("project_id", _id), ObjectId.class).into(new ArrayList<>());
		c("rbsItem").deleteMany(new Document("project_id", _id));
		c("riskEffect").deleteMany(new Document("project_id", _id));
		c("riskResponse").deleteMany(new Document("rbsItem_id", new Document("$in", rbsIds)));

		// ���salesItem
		c("salesItem").deleteMany(new Document("project_id", _id));

		// ���stockholder
		c("stockholder").deleteMany(new Document("project_id", _id));

		// ���workReport
		c("workReport").deleteMany(new Document("project_id", _id));
		c("workReportItem").deleteMany(new Document("work_id", new Document("$in", workIds)));
		c("workReportResourceActual").deleteMany(new Document("work_id", new Document("$in", workIds)));

		// �����Ŀ����
		c("setting").deleteMany(new Document("name", Pattern.compile("@" + _id.toString(), Pattern.CASE_INSENSITIVE)));

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
		//////////////////////////////////////////////////////////////////////////////////////////////////
		// ״̬���
		Document project = c("project").find(new Document("_id", com._id)).first();
		if (!ProjectStatus.Processing.equals(project.getString("status"))) {// �����ǽ����в�����β
			return Arrays.asList(Result.error("��Ŀ��ǰ��״̬������ִ����β����"));
		}
		//////////////////////////////////////////////////////////////////////////////////////////////////
		// �������δ��ɵĹ���������
		if (ICommand.Finish_Project.equals(com.name)) {
			long count = c("work").countDocuments(new BasicDBObject("project_id", com._id).append("actualFinish", null));
			if (count > 0) {
				return Arrays.asList(Result.warning("��Ŀ����һЩ��δ��ɵĹ�����"));
			}
		}

		//////////////////////////////////////////////////////////////////////////////////////////////////
		// ���ʱ��
		// yangjun 2018/10/31
		Document latest = c("work").find(new Document("parent_id", com._id)).projection(new Document("actualFinish", true))
				.sort(new Document("actualFinish", -1).append("_id", -1)).first();
		Date actualFinish = Optional.ofNullable(latest).map(l -> l.getDate("actualFinish")).orElse(new Date());

		//////////////////////////////////////////////////////////////////////////////////////////////////
		// �޸���Ŀ״̬
		c("project").updateOne(new Document("_id", com._id), new Document("$set", new Document("status", ProjectStatus.Closing)
				.append("progress", 1d).append("finishInfo", com.info()).append("actualFinish", actualFinish)));

		// ֪ͨ��Ŀ�Ŷӳ�Ա����Ŀ��β
		List<String> memberIds = getProjectMembers(com._id);
		sendMessage("��Ŀ��β֪ͨ", "��Ŀ��" + project.getString("name") + " ���� " + Formatter.getString(com.date) + " ������β��", com.userId, memberIds,
				null);

		return new ArrayList<>();
	}

	@Override
	public List<Result> closeProject(Command com) {

		/////////////////////////////////////////////////////////////////////////////
		// �����Ŀ�깤
		List<Result> result = closeProjectCheck(com._id, com.date, com.userId);
		if (result.stream().anyMatch(r -> Result.TYPE_ERROR == r.type// ����ı��뷵��
				|| (Result.TYPE_QUESTION == r.type && ICommand.Close_Project.equals(com.name))
				|| (Result.TYPE_WARNING == r.type && ICommand.Close_Project.equals(com.name)))) {// �����Ծ���ı��뷵��
			return result;
		}

		//////////////////////////////////////////////////////////////////////////////////////////////////
		// ״̬���
		Document project = c("project").find(new Document("_id", com._id)).first();
		if (ProjectStatus.Closed.equals(project.getString("status"))) {
			return Arrays.asList(Result.error("��Ŀ��ǰ��״̬������ִ�йرղ���"));
		}
		//////////////////////////////////////////////////////////////////////////////////////////////////
		// �������δ��ɵĹ���������
		if (ICommand.Finish_Project.equals(com.name)) {
			long count = c("work").countDocuments(new BasicDBObject("project_id", com._id).append("actualFinish", null));
			if (count > 0) {
				return Arrays.asList(Result.warning("��Ŀ����һЩ��δ��ɵĹ�������Щ����Ҳ��ͬʱ�رա�"));
			}
		}
		//////////////////////////////////////////////////////////////////////////////////////////////////
		// �޸���Ŀ״̬
		c("project").updateOne(new Document("_id", com._id),
				new Document("$set", new Document("status", ProjectStatus.Closed).append("closeInfo", com.info())));
		//////////////////////////////////////////////////////////////////////////////////////////////////
		// �޸Ĺ���״̬
		// ɾ��δ��ʼ�Ĺ�����skipStart
		// ��ȡҪɾ���Ĺ���
		List<ObjectId> delete = new ArrayList<ObjectId>();
		c("work").find(new Document("project_id", com._id).append("actualStart", null).append("milestone", false))
				.sort(new Document("wbsCode", 1)).forEach((Document d) -> {
					// �������������Ҫɾ���Ĺ����У���ȡ�估���¼�������id��ӵ�ɾ��������
					if (!delete.contains(d.getObjectId("_id"))) {
						List<ObjectId> workIds = new ArrayList<>();
						lookupDesentItems(Arrays.asList(d.getObjectId("_id")), "work", "parent_id", true).forEach((Document doc) -> {
							workIds.add(doc.getObjectId("_id"));
						});
						delete.addAll(workIds);
					}
				});
		if (delete.size() > 0)
			c("work").deleteMany(new Document("_id", new Document("$in", delete)));
		// �깤Ϊ�깤�Ĺ���
		List<ObjectId> close = new ArrayList<ObjectId>();
		c("work").find(new Document("project_id", com._id).append("actualFinish", null).append("actualStart", new Document("$ne", null)))
				.sort(new Document("wbsCode", 1)).forEach((Document d) -> {
					// �������������ɹ����У���ӵ��رռ�����
					if (!close.contains(d.getObjectId("_id"))) {
						List<ObjectId> workIds = new ArrayList<>();
						lookupDesentItems(Arrays.asList(d.getObjectId("_id")), "work", "parent_id", true).forEach((Document doc) -> {
							workIds.add(doc.getObjectId("_id"));
						});
						close.addAll(workIds);
					}
				});
		if (close.size() > 0) {
			// ������̱�
			c("work").updateMany(new Document("_id", new Document("$in", close)).append("milestone", true), new Document("$set",
					new Document("skipFinish", true).append("actualFinish", com.date).append("actualStart", com.date)));
			// ���½׶�
			c("work").updateMany(new Document("_id", new Document("$in", close)).append("stage", true), new Document("$set",
					new Document("skipFinish", true).append("actualFinish", com.date).append("status", ProjectStatus.Closed)));
			// ������������
			c("work").updateMany(new Document("_id", new Document("$in", close)).append("milestone", false).append("stage", false),
					new Document("$set", new Document("skipFinish", true).append("actualFinish", com.date)));
		}
		// �ر���Ŀ���
		c("projectChange").updateMany(
				new Document("project_id", com._id).append("status", new Document("$ne", ProjectChange.STATUS_CONFIRM)),
				new Document("$set", new Document("skipFinish", true).append("verifyDate", com.date).append("verify", com.userId)
						.append("status", ProjectChange.STATUS_CONFIRM)));

		// ȷ����Ŀ����
		c("workReport").updateMany(new Document("project_id", com._id).append("status", new Document("$ne", ProjectChange.STATUS_CONFIRM)),
				new Document("$set", new Document("skipFinish", true).append("verifyDate", com.date)
						.append("status", WorkReport.STATUS_CONFIRM).append("verifier", com.userId)));

		// ֪ͨ��Ŀ�Ŷӳ�Ա����Ŀ�Ѿ��ر�
		List<String> memberIds = getProjectMembers(com._id);
		sendMessage("��Ŀ�ر�֪ͨ",
				"��Ŀ��" + project.getString("name") + " ���� " + new SimpleDateFormat(Formatter.DATE_FORMAT_DATE).format(com.date) + " �رա�",
				com.userId, memberIds, null);
		return new ArrayList<>();
	}

	/**
	 * ��Ŀ�ɱ�
	 */
	private static String CLOSE_SETTING_FIELD_COST = "cost";
	/**
	 * ��Դ����
	 */
	private static String CLOSE_SETTING_FIELD_RESOURCE = "resource";
	/**
	 * ��Ŀ���
	 */
	private static String CLOSE_SETTING_FIELD_CHANGE = "change";
	/**
	 * ��Ŀ����
	 */
	private static String CLOSE_SETTING_FIELD_REPORT = "report";
	/**
	 * ��Ʒ����
	 */
	private static String CLOSE_SETTING_FIELD_MATERIAL = "material";
	/**
	 * ���й�����δ������
	 */
	private static String CLOSE_SETTING_FIELD_START_ALL = "startAll";
	/**
	 * һ������ڵ㣨δ������
	 */
	private static String CLOSE_SETTING_FIELD_START_L1 = "startL1";
	/**
	 * ��������ڵ㣨δ������
	 */
	private static String CLOSE_SETTING_FIELD_START_L2 = "startL2";
	/**
	 * ��������ڵ㣨δ������
	 */
	private static String CLOSE_SETTING_FIELD_START_L3 = "startL3";
	/**
	 * ���й�����δ�깤��
	 */
	private static String CLOSE_SETTING_FIELD_FINISH_ALL = "finishAll";
	/**
	 * һ������ڵ㣨δ�깤��
	 */
	private static String CLOSE_SETTING_FIELD_FINISH_L1 = "finishL1";
	/**
	 * ��������ڵ㣨δ�깤��
	 */
	private static String CLOSE_SETTING_FIELD_FINISH_L2 = "finishL2";
	/**
	 * ��������ڵ㣨δ�깤��
	 */
	private static String CLOSE_SETTING_FIELD_FINISH_L3 = "finishL3";
	/**
	 * ��̱���δ�깤��
	 */
	private static String CLOSE_SETTING_FIELD_FINISH_MILESTONE = "finishMilestone";

	/**
	 * Ҫ�󡢽�ֹ
	 */
	private static String CLOSE_SETTING_VALUE_REQUIREMENT = "1";
	/**
	 * ѯ��
	 */
	private static String CLOSE_SETTING_VALUE_QUESTION = "2";
	/**
	 * ���ԡ�ɾ������ɡ�ȷ�ϡ��ر�
	 */
	private static String CLOSE_SETTING_VALUE_IGNORE = "3";
	
	private List<Result> closeProjectCheck(ObjectId _id, Date date, String userId) {
		List<Result> results = new ArrayList<Result>();

		Project project = get(_id);

		Document systemSetting = Optional.ofNullable(getSystemSetting(CLOSE_SETTING_NAME + "@" + _id.toString())).orElse(new Document());
		String setting;

		// δ��ʼ�����жϼ�����
		// ��ȡ���й��������ã�Ĭ��Ϊɾ��
		setting = getSettingValue(systemSetting, CLOSE_SETTING_FIELD_START_ALL, CLOSE_SETTING_VALUE_IGNORE);
		// ��ȡδ��ʼ���������ų���̱�

		List<Document> start = c("work").find(new Document("project_id", _id).append("actualStart", null).append("milestone", false))
				.sort(new Document("wbsCode", 1)).into(new ArrayList<>());
		if (CLOSE_SETTING_VALUE_REQUIREMENT.equals(setting) && start.size() > 0) {// ���óɲ�����ʱ����Ӵ�����ʾ
			results.add(Result.error("����:"
					+ Formatter.getString(start.stream().map(d -> d.getString("fullName")).collect(Collectors.toList())) + " δ��ʼ���������깤��Ŀ"));
		} else if (start.size() > 0) {// ���й������ó�ѯ��ʱ�����ݸ������������ý��в���
			Map<String, String> manageLevels = new HashMap<String, String>();
			manageLevels.put("1", getSettingValue(systemSetting, CLOSE_SETTING_FIELD_START_L1, CLOSE_SETTING_VALUE_IGNORE));
			manageLevels.put("2", getSettingValue(systemSetting, CLOSE_SETTING_FIELD_START_L2, CLOSE_SETTING_VALUE_IGNORE));
			manageLevels.put("3", getSettingValue(systemSetting, CLOSE_SETTING_FIELD_START_L3, CLOSE_SETTING_VALUE_IGNORE));

			List<ObjectId> delete = new ArrayList<ObjectId>();
			List<String> notallow = new ArrayList<String>();
			List<String> question = new ArrayList<String>();
			// ��ȡ����δ��ʼ�Ľڵ㣬�����ж�
			start.forEach((Document d) -> {
				// �������������Ҫɾ���Ĺ����У��������ü��
				if (!delete.contains(d.getObjectId("_id")))
					if (CLOSE_SETTING_VALUE_REQUIREMENT.equals(manageLevels.get(d.getString("manageLevel")))) // ���óɲ�����ʱ����Ӵ�����ʾ
						notallow.add(d.getString("fullName"));
					else if (CLOSE_SETTING_VALUE_QUESTION.equals(manageLevels.get(d.getString("manageLevel")))) // ���ó�ѯ��ʱ�����ѯ����ʾ
						question.add(d.getString("fullName"));

			});
			if (notallow.size() > 0)
				results.add(Result.error("����:" + Formatter.getString(notallow) + " δ��ʼ���������깤��Ŀ"));
			if (question.size() > 0)
				results.add(Result.question("����:" + Formatter.getString(question) + " δ��ʼ��"));

		}

		// δ�깤�����жϼ�������Ĭ��Ϊ���
		setting = getSettingValue(systemSetting, CLOSE_SETTING_FIELD_FINISH_ALL, CLOSE_SETTING_VALUE_IGNORE);

		// ��ȡδ�깤������
		List<Document> finish = c("work")
				.find(new Document("project_id", _id).append("actualFinish", null).append("actualStart", new Document("$ne", null)))
				.sort(new Document("wbsCode", 1)).into(new ArrayList<>());
		if (CLOSE_SETTING_VALUE_REQUIREMENT.equals(setting) && finish.size() > 0) {// ���óɲ�����ʱ����Ӵ�����ʾ
			results.add(Result.error("����:"
					+ Formatter
							.getString(Formatter.getString(finish.stream().map(d -> d.getString("fullName")).collect(Collectors.toList())))
					+ " δ�깤���������깤��Ŀ"));
		} else if (finish.size() > 0) {// ���й������ó�ѯ��ʱ�����ݸ������������ý��в���
			Map<String, Object> manageLevels = new HashMap<String, Object>();
			manageLevels.put("1", getSettingValue(systemSetting, CLOSE_SETTING_FIELD_FINISH_L1, CLOSE_SETTING_VALUE_IGNORE));
			manageLevels.put("2", getSettingValue(systemSetting, CLOSE_SETTING_FIELD_FINISH_L2, CLOSE_SETTING_VALUE_IGNORE));
			manageLevels.put("3", getSettingValue(systemSetting, CLOSE_SETTING_FIELD_FINISH_L3, CLOSE_SETTING_VALUE_IGNORE));
			manageLevels.put("milestone", getSettingValue(systemSetting, CLOSE_SETTING_FIELD_FINISH_MILESTONE, CLOSE_SETTING_VALUE_IGNORE));

			List<ObjectId> close = new ArrayList<ObjectId>();
			List<String> notallow = new ArrayList<String>();
			List<String> question = new ArrayList<String>();

			finish.forEach((Document d) -> {
				// �������������ɹ����У����������ж�
				if (!close.contains(d.getObjectId("_id"))) {
					Object set;
					// �����������̱���������̱������ж�
					if (d.getBoolean("milestone", false)) {
						set = manageLevels.get("milestone");
					} else {
						set = manageLevels.get(d.getString("manageLevel"));
					}
					if (CLOSE_SETTING_VALUE_REQUIREMENT.equals(set)) // ���óɲ�����ʱ����Ӵ�����ʾ
						notallow.add(d.getString("fullName"));
					else if (CLOSE_SETTING_VALUE_QUESTION.equals(set)) // ���ó�ѯ��ʱ�����ѯ����ʾ
						question.add(d.getString("fullName"));
				}
			});

			if (notallow.size() > 0)
				results.add(Result.error("����:" + Formatter.getString(notallow) + " δ�깤���������깤��Ŀ"));
			if (question.size() > 0)
				results.add(Result.question("����:" + Formatter.getString(question) + " δ�깤��"));
		}

		// ��Ŀ�����Ĭ��Ϊȷ��
		setting = getSettingValue(systemSetting, CLOSE_SETTING_FIELD_CHANGE, CLOSE_SETTING_VALUE_IGNORE);

		// ��ȡδ�ر���Ŀ�����
		long l = c("projectChange")
				.countDocuments(new Document("project_id", _id).append("status", new Document("$ne", ProjectChange.STATUS_CONFIRM)));
		if (CLOSE_SETTING_VALUE_REQUIREMENT.equals(setting) && l > 0) {// ���ó�Ҫ��ʱ����Ӵ�����ʾ
			results.add(Result.error("����δ�رյ���Ŀ������������깤��Ŀ"));
		} else if (CLOSE_SETTING_VALUE_QUESTION.equals(setting) && l > 0) {// ���ó�ѯ��ʱ�����ѯ����ʾ
			results.add(Result.question("����δ�رյ���Ŀ������Ƿ��Զ��ر���Ŀ������깤��Ŀ"));
		}

		// ��Ŀ���棬Ĭ��Ϊ�ر�
		setting = getSettingValue(systemSetting, CLOSE_SETTING_FIELD_REPORT, CLOSE_SETTING_VALUE_IGNORE);
		// ��ȡδȷ�ϵ���Ŀ������
		l = c("workReport")
				.countDocuments(new Document("project_id", _id).append("status", new Document("$ne", WorkReport.STATUS_CONFIRM)));
		if (CLOSE_SETTING_VALUE_REQUIREMENT.equals(setting) && l > 0) {// ���ó�Ҫ��ʱ����Ӵ�����ʾ
			results.add(Result.error("����δȷ�ϵ���Ŀ���棬�������깤��Ŀ"));
		} else if (CLOSE_SETTING_VALUE_QUESTION.equals(setting) && l > 0) {// ���ó�ѯ��ʱ�����ѯ����ʾ
			results.add(Result.question("����δȷ�ϵ���Ŀ��������Ƿ��Զ�ȷ�Ϻ��깤��Ŀ"));
		}

		// ��Ŀ�ɱ���Ĭ��Ϊ����
		setting = getSettingValue(systemSetting, CLOSE_SETTING_FIELD_COST, CLOSE_SETTING_VALUE_IGNORE);
		if (!CLOSE_SETTING_VALUE_IGNORE.equals(setting)) {
			// ��ȡ�ɱ���id
			List<ObjectId> cbsIds = new ArrayList<>();
			lookupDesentItems(Arrays.asList(project.getCBS_id()), "cbs", "parent_id", true).forEach((Document d) -> {
				cbsIds.add(d.getObjectId("_id"));
			});
			// ���û��cbsSubjectû�гɱ����������ʾ
			l = c("cbsSubject")
					.countDocuments(new Document("cbsItem_id", new Document("$in", cbsIds)).append("cost", new Document("$ne", null)));
			if (l == 0)
				// �������Ϊ���棬�򷵻ؾ��棻�������ΪҪ���򷵻ش���
				if (CLOSE_SETTING_VALUE_QUESTION.equals(setting))
					results.add(Result.question("��Ŀ��δά���ɱ���"));
				else if (CLOSE_SETTING_VALUE_REQUIREMENT.equals(setting))
					results.add(Result.error("��Ŀ��δά���ɱ���"));

		}

		// ��Դ������Ĭ��Ϊ����
		setting = getSettingValue(systemSetting, CLOSE_SETTING_FIELD_RESOURCE, CLOSE_SETTING_VALUE_IGNORE);
		if (!CLOSE_SETTING_VALUE_IGNORE.equals(setting)) {
			// ������з���̱���Ҷ�ӽڵ��id
			List<ObjectId> workIds = c("work")
					.distinct("_id", new Document("project_id", _id).append("milestone", false).append("summary", false), ObjectId.class)
					.into(new ArrayList<>());
			// ���û�н��ȼƻ�����������Դ���
			if (workIds.size() > 0) {
				// ��ȡ��д����Դ�ƻ��Ĺ���id
				List<ObjectId> resourceWorkIds = c("resourceActual")
						.distinct("work_id", new Document("work_id", new Document("$in", workIds)), ObjectId.class).into(new ArrayList<>());
				String message = "";
				if (resourceWorkIds.size() == 0) {
					message = "��Ŀ��δ������Դ����";
				} else {
					workIds.removeAll(resourceWorkIds);
					if (workIds.size() > 0) {
						message = Formatter
								.getString(c("work").distinct("fullName", new Document("_id", new Document("$in", workIds)), String.class)
										.into(new ArrayList<>()));
						message = "������" + message + " δ������Դ����";
					}
				}
				if (!message.isEmpty() && CLOSE_SETTING_VALUE_QUESTION.equals(setting))
					results.add(Result.question(message));
				else if (!message.isEmpty() && CLOSE_SETTING_VALUE_REQUIREMENT.equals(setting))
					results.add(Result.error(message));
			}
		}

		// TODO ��Ʒ���ϣ�Ĭ��Ϊ����
		setting = getSettingValue(systemSetting, CLOSE_SETTING_FIELD_MATERIAL, CLOSE_SETTING_VALUE_IGNORE);
		if (!CLOSE_SETTING_VALUE_IGNORE.equals(setting)) {

		}

		return results;
	}

	/**
	 * TODO Ǩ�Ƶ�ǰ̨����
	 * 
	 * @param _id
	 * @return
	 */
	@Deprecated
	private String generateWorkOrder(ObjectId _id) {
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
		Project project = get(_id);
		String catalog = project.getCatalog();
		ObjectId parentproject_id = project.getParentProject_id();
		ObjectId program_id = project.getProgram_id();
		ObjectId impunit_id = project.getImpUnit_id();

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
			String parentWorkOrder = c("project").distinct("workOrder", new Document("_id", parentproject_id), String.class).first();
			String[] workorders = parentWorkOrder.split("-");
			workOrder += "-" + workorders[1];
			int index = generateCode(Generator.DEFAULT_NAME, "projectno" + parentWorkOrder);
			workOrder += "-" + String.format("%02d", index);

		} else if (program_id != null) {
			String programWorkOrder = c("program").distinct("workOrder", new Document("_id", program_id), String.class).first();
			String[] workorders = programWorkOrder.split("-");
			workOrder += "-" + workorders[1];
			int index = generateCode(Generator.DEFAULT_NAME, "projectno" + programWorkOrder);
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
		List<Bson> pipeline = new JQ("��ѯ-ʱ����")
				.set("match", new Document("manageLevel", new Document("$in", Arrays.asList("1", "2"))).append("project_id", _id))
				.set("limit", count).array();
		c("work").aggregate(pipeline).forEach((Document doc) -> {
			Object user = Optional.ofNullable(doc.get("userInfo")).orElse("");
			if (doc.get("date").equals(doc.get("actualFinish"))) {
				result.add(new News().setDate(doc.getDate("date")).setContent(user + " " + doc.get("name") + " ��� "));
			} else if (doc.get("date").equals(doc.get("actualStart"))) {
				result.add(new News().setDate(doc.getDate("date")).setContent(user + " " + doc.get("name") + " ����"));
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
		c("work").find(new Document("project_id", project_id)).sort(new Document("parent_id", 1)).forEach((Document doc) -> {
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
		// TODO JQ
		List<Bson> pipeline = new ArrayList<Bson>();
		pipeline.add(Aggregates.match(new Document("project_id", new Document("$in", projectIds))));
		pipeline.add(Aggregates.sort(new Document("project_id", 1).append("index", 1)));
		pipeline.add(Aggregates.lookup("project", "project_id", "_id", "project"));
		pipeline.add(Aggregates.unwind("$project"));
		pipeline.add(Aggregates.addFields(
				Arrays.asList(new Field<String>("projectName", "$project.name"), new Field<String>("projectNumber", "$project.id"))));
		pipeline.add(Aggregates.project(new BasicDBObject("project", false)));
		appendUserInfo(pipeline, "chargerId", "chargerInfo");
		appendUserInfo(pipeline, "assignerId", "assignerInfo");
		List<Work> works = c("work", Work.class).aggregate(pipeline).into(new ArrayList<Work>());

		pipeline = new ArrayList<Bson>();
		pipeline.add(Aggregates.match(new Document("baseline_id", new Document("$in", projectIds))));
		pipeline.add(Aggregates.sort(new Document("baseline_id", 1).append("index", 1)));
		pipeline.add(Aggregates.lookup("project", "project_id", "_id", "project"));
		pipeline.add(Aggregates.unwind("$project"));
		pipeline.add(Aggregates.addFields(
				Arrays.asList(new Field<String>("projectName", "$project.name"), new Field<String>("projectNumber", "$project.id"))));
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
					if (work1 != null) {
						ObjectId work1Old_id = work1.getOld_id();
						if (work1Old_id != null && work1Old_id.equals(work.getOld_id())) {
							bc.setWork2(work);
							add = false;
							break;
						} else if (work1.get_id().equals(work.getOld_id())) {
							bc.setWork2(work);
							add = false;
							break;
						} else if (work1.getFullName().equals(work.getFullName())) {
							bc.setWork2(work);
							add = false;
							break;
						}
					} else {
						Work work2 = bc.getWork2();
						if (work2 != null) {
							ObjectId work2Old_id = work2.getOld_id();
							if (work2Old_id != null && work2Old_id.equals(work.getOld_id())) {
								bc.setWork1(work);
								add = false;
								break;
							} else if (work2.get_id().equals(work.getOld_id())) {
								bc.setWork1(work);
								add = false;
								break;
							} else if (work2.getFullName().equals(work.getFullName())) {
								bc.setWork1(work);
								add = false;
								break;
							}
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
		// yangjun 2018/10/31
		return c(ProjectChange.class).aggregate(pipeline).into(new ArrayList<ProjectChange>());
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
					// yangjun 2018/10/31
					if (Check.equals(obsItem.getRoleId(), changeProcess.getProjectOBSId())) {
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
	public ProjectChange getProjectChange(ObjectId _id) {
		ArrayList<Bson> pipeline = new ArrayList<Bson>();

		pipeline.add(Aggregates.match(new BasicDBObject("_id", _id)));

		pipeline.add(Aggregates.sort(new BasicDBObject("_id", -1)));

		appendProject(pipeline);
		appendUserInfo(pipeline, "applicant", "applicantInfo");
		appendOrgFullName(pipeline, "applicantUnitId", "applicantUnit");

		return c(ProjectChange.class).aggregate(pipeline).first();
	}

	@Override
	public List<ProjectChange> listProjectChangeInfo(ObjectId _id) {
		List<Bson> pipeline = new ArrayList<Bson>();

		pipeline.addAll(new JQ("��ѯ-��Ŀ���").set("match", new Document("_id", _id)).array());

		appendUserInfo(pipeline, "applicant", "applicantInfo");
		appendOrgFullName(pipeline, "applicantUnitId", "applicantUnit");

		return c(ProjectChange.class).aggregate(pipeline).into(new ArrayList<ProjectChange>());
	}

	@Override
	public List<Result> submitProjectChange(List<ObjectId> projectChangeIds) {
		List<Result> result = submitProjectChangeCheck(projectChangeIds);
		if (!result.isEmpty()) {
			return result;
		}

		UpdateResult ur = c(ProjectChange.class).updateMany(new Document("_id", new Document("$in", projectChangeIds)),
				new Document("$set", new Document("submitDate", new Date()).append("status", ProjectChange.STATUS_SUBMIT)));
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
			sendMessage("��Ŀ�������", "" + projectChange.getApplicantInfo() + " ��������Ŀ��" + projectChange.getProjectName() + " �ı�����룬����������ˡ�",
					projectChange.getApplicantId(), receivers, null);

			String pmId = c("project").distinct("pmId", new Document("_id", projectChange.getProject_id()), String.class).first();
			if (!receivers.contains(pmId))
				sendMessage("��Ŀ�������", "" + projectChange.getApplicantInfo() + " ��������Ŀ��" + projectChange.getProjectName() + " �ı�����룬",
						projectChange.getApplicantId(), receivers, null);

		});

		return result;
	}

	private List<Result> submitProjectChangeCheck(List<ObjectId> projectChangeIds) {
		List<Result> result = new ArrayList<Result>();
		long count = c(ProjectChange.class)
				.countDocuments(new Document("_id", new Document("$in", projectChangeIds)).append("reviewer.user", null));
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
		// yangjun 2018/10/31
		ProjectChange pc = getProjectChange(projectChangeTask.projectChange_id);
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
		sendMessage("��Ŀ�����������׼", "" + projectChangeTask.getUser() + " ��׼����Ŀ��" + pc.getProjectName() + " �ı�����룬", projectChangeTask.user,
				pc.getApplicantId(), null);
		if (ProjectChange.STATUS_PASS.equals(status)) {
			String pmId = c("project").distinct("pmId", new Document("_id", pc.getProject_id()), String.class).first();
			sendMessage("��Ŀ���������ͨ��", "��Ŀ��" + pc.getProjectName() + " �ı�����������ͨ����", pc.getApplicantId(), pmId, null);
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

		UpdateResult ur = c(ProjectChange.class).updateMany(new Document("_id", new Document("$in", projectChangeIds)), new Document("$set",
				new Document("verifyDate", new Date()).append("verify", userId).append("status", ProjectChange.STATUS_CONFIRM)));
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
			sendMessage("��Ŀ��������ѹر�", "��Ŀ��" + pc.getProjectName() + " �ı���ѹرգ�", userId, pc.getApplicantId(), null);
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
				new BasicDBObject("$set", new BasicDBObject("reviewer", reviewer).append("status", ProjectChange.STATUS_CANCEL)));
		if (ur.getModifiedCount() == 0) {
			result.add(Result.updateFailure("û������ȡ�������ı�����롣"));
			return result;
		}
		String pmId = c("project").distinct("pmId", new Document("_id", pc.getProject_id()), String.class).first();
		sendMessage("��Ŀ��������ѷ��", "" + projectChangeTask.getUser() + " �������Ŀ��" + pc.getProjectName() + " �ı�����룬", projectChangeTask.user,
				Arrays.asList(pmId, pc.getApplicantId()), null);

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
		return c("projectChange").countDocuments(new Document("project_id", _id).append("status",
				new Document("$nin", Arrays.asList(ProjectChange.STATUS_CANCEL, ProjectChange.STATUS_CONFIRM))));
	}

	@Override
	public long countReviewerProjectChange(BasicDBObject filter, String userId) {
		List<Bson> pipeline = (List<Bson>) new JQ("��ѯ-��Ŀ���-������").set("userId", userId).set("status", ProjectChange.STATUS_SUBMIT).array();

		if (filter != null)
			pipeline.add(Aggregates.match(filter));

		return c("projectChange").aggregate(pipeline).into(new ArrayList<>()).size();
	}

	@Override
	public List<ProjectChange> listReviewerProjectChange(BasicDBObject condition, String userId) {
		return listReviewerPC(condition, userId).into(new ArrayList<>());
	}

	@Override
	public List<Document> listReviewerProjectChangeCard(BasicDBObject condition, String userId) {
		return listReviewerPC(condition, userId).map(ProjectChangeRenderer::render).into(new ArrayList<>());
	}

	private AggregateIterable<ProjectChange> listReviewerPC(BasicDBObject condition, String userId) {
		Integer skip = (Integer) condition.get("skip");
		Integer limit = (Integer) condition.get("limit");
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter == null) {
			filter = new BasicDBObject();
		}

		List<Bson> pipeline = (List<Bson>) new JQ("��ѯ-��Ŀ���-������").set("userId", userId).set("status", ProjectChange.STATUS_SUBMIT).array();

		appendProject(pipeline);
		appendUserInfo(pipeline, "applicant", "applicantInfo");
		appendOrgFullName(pipeline, "applicantUnitId", "applicantUnit");

		pipeline.add(Aggregates.match(filter));

		pipeline.add(Aggregates.sort(new BasicDBObject("_id", -1)));

		if (skip != null)
			pipeline.add(Aggregates.skip(skip));

		if (limit != null)
			pipeline.add(Aggregates.limit(limit));

		return c(ProjectChange.class).aggregate(pipeline);
	}

	@Override
	@Deprecated
	public List<Project> listAdministratedProjects(BasicDBObject condition, String managerId) {
		List<ObjectId> projectIds = getAdministratedProjects(managerId);
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter == null) {
			filter = new BasicDBObject();
			condition.put("filter", filter);
		}
		filter.append("_id", new BasicDBObject("$in", projectIds));
		return list(condition);
	}

	@Override
	public List<Document> listAdministratedProjectsCard(BasicDBObject condition, String managerId) {
		List<ObjectId> projectIds = getAdministratedProjects(managerId);
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		if (filter == null) {
			filter = new BasicDBObject();
			condition.put("filter", filter);
		}
		filter.append("_id", new BasicDBObject("$in", projectIds));

		Integer skip = (Integer) condition.get("skip");
		Integer limit = (Integer) condition.get("limit");
		BasicDBObject sort = (BasicDBObject) condition.get("sort");

		List<Bson> pipeline = appendQueryPipeline(skip, limit, filter, sort, new ArrayList<>());
		return c(Project.class).aggregate(pipeline).map(ProjectRenderer::render).into(new ArrayList<>());
	}

	@Override
	public long countAdministratedProjects(BasicDBObject filter, String managerId) {
		List<ObjectId> projectIds = getAdministratedProjects(managerId);
		if (filter == null) {
			// filter = new BasicDBObject();
			return projectIds.size();
		}
		filter.append("_id", new BasicDBObject("$in", projectIds));
		return count(filter);
	}

	/**
	 * ��ȡ������Ŀ�����û�������Ŀ�ܼࡢ��Ӧ���������������������ɫʱ����ʾȫ����Ŀ��Ϣ������ֻ��ʾ��ǰ�û���Ϊ��ĿPMO�Ŷӳ�Ա����Ŀ
	 */
	@Override
	public List<Project> listAllProjects(BasicDBObject condition, String userid) {
		Integer skip = (Integer) condition.get("skip");
		Integer limit = (Integer) condition.get("limit");
		BasicDBObject filter = (BasicDBObject) condition.get("filter");
		BasicDBObject sort = (BasicDBObject) condition.get("sort");
		if (sort == null) {
			// yangjun 2018/10/31
			sort = new BasicDBObject("creationInfo.date", -1).append("_id", -1);
		}

		List<Bson> pipeline = new ArrayList<Bson>();
		// ��ǰ�û�������Ŀ�ܼ�Ȩ��ʱ��ʾȫ��������ʾȫ��ʱ������PMO�ŶӲ�ѯ
		if (!checkUserRoles(userid, Role.SYS_ROLE_PD_ID)) {
			appendQueryUserInProjectPMO(pipeline, userid, "$_id");
		}

		appendQueryPipeline(skip, limit, filter, sort, pipeline);

		return c(Project.class).aggregate(pipeline).into(new ArrayList<>());
	}

	/**
	 * ��ȡ������Ŀ�������û�������Ŀ�ܼࡢ��Ӧ���������������������ɫʱ����ʾȫ����Ŀ��������ֻ��ʾ��ǰ�û���Ϊ��ĿPMO�Ŷӳ�Ա����Ŀ��
	 */
	@Override
	public long countAllProjects(BasicDBObject filter, String userid) {
		// �����ǰ�û�������Ŀ�ܼ��Ȩ��ʱ����ȫ����Ŀ����
		if (checkUserRoles(userid, Role.SYS_ROLE_PD_ID)) {
			return count(filter);
		}
		// ����ʾȫ��ʱ��ֻ�����û�����ĿPMO�Ŷ��е���Ŀ��
		List<Bson> pipeline = new ArrayList<Bson>();
		appendQueryUserInProjectPMO(pipeline, userid, "$_id");
		return c(Project.class).aggregate(pipeline).into(new ArrayList<>()).size();
	}

	@Override
	public Integer schedule(ObjectId _id) {
		return super.schedule(_id);
	}

	@Override
	public List<ProjectScheduleInfo> listManagedProjectSchedules(BasicDBObject condition, String userid) {
		return listAllProjects(condition, userid).stream().map(project -> new ProjectScheduleInfo().setProject(project))
				.collect(Collectors.toList());
	}

	@Override
	public long countManagedProjectSchedules(BasicDBObject filter, String userid) {
		return countAllProjects(filter, userid);
	}

	@Override
	public List<ProjectScheduleInfo> listSubManagedProjectSchedules(ProjectScheduleInfo parent) {
		WorkServiceImpl workService = new WorkServiceImpl();
		if (parent.typeEquals(Project.class)) {
			return workService.listProjectRootTask(parent.get_id()).stream().map(work -> new ProjectScheduleInfo().setWork(work))
					.collect(Collectors.toList());
		} else if (parent.typeEquals(Work.class)) {
			return workService.listChildren(parent.get_id()).stream().map(work -> new ProjectScheduleInfo().setWork(work))
					.collect(Collectors.toList());
		} else {
			// TODO ��������
			return new ArrayList<ProjectScheduleInfo>();
		}
	}

	@Override
	public long countSubManagedProjectSchedules(ProjectScheduleInfo parent) {
		WorkServiceImpl workService = new WorkServiceImpl();
		if (parent.typeEquals(Project.class)) {
			return workService.countProjectRootTask(parent.get_id());
		} else if (parent.typeEquals(Work.class)) {
			return workService.countChildren(parent.get_id());
		} else {
			// TODO ��������
			return 0l;
		}
	}

	@Override
	public Document getOrganizationSAR(String year, String userId) {
		return getOrganizationSAR(year, userId, "stageSar", year + " �� ��Ŀ�ƻ������");
	}

	@Override
	public Document getOrganizationSAR1(String year, String userId) {
		return getOrganizationSAR(year, userId, "sar1", year + " �� һ���ƻ������");
	}

	@Override
	public Document getOrganizationSAR2(String year, String userId) {
		return getOrganizationSAR(year, userId, "sar2", year + " �� �����ƻ������");
	}

	@SuppressWarnings("unchecked")
	private Document getOrganizationSAR(String year, String userId, String sarName, String title) {
		List<String> xAxisData = new ArrayList<String>();
		for (int i = 1; i <= 12; i++) {
			String key = year + "-" + String.format("%02d", i);
			xAxisData.add(key);
		}
		List<String> legendData = new ArrayList<String>();
		List<Document> series = new ArrayList<Document>();

		c("organization")
				.aggregate(
						new JQ("��ѯ-�е����żƻ������")
								.set("workmatch",
										new Document("$expr",
												new Document("$eq",
														Arrays.asList(year,
																new Document("$dateToString",
																		new Document("format", "%Y").append("date", "$actualFinish"))))))
								.set("orgmatch", new Document()).set("projectmatch", new Document())// "_id", new Document("$in",
																									// getAdministratedProjects(userId))))
								.array())
				.forEach((Document d) -> {
					List<Document> project = (List<Document>) d.get("project");
					if (Check.isAssigned(project)) {
						String name = d.getString("name");
						double[] sard = new double[12];
						for (Document p : project) {
							int i = Integer.parseInt(p.getString("month").split("-")[1]) - 1;
							sard[i] = new BigDecimal(p.getDouble(sarName)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
						}
						series.add(new Document("name", name).append("type", "bar").append("data", Formatter.toList(sard)));
						legendData.add(name);
					}
				});
		Document doc = new JQ("ͼ��-�ƻ������").set("xAxisData", xAxisData).set("legendData", legendData).set("series", series)
				.set("title", title).doc();
		return doc;
	}

	@Override
	public Project appointmentProjectManger(ObjectId _id, String newPMId, String currentUserId) {
		String msgSubject = "��Ŀָ��֪ͨ";
		Project project = c(Project.class).find(new Document("_id", _id)).first();
		String userId = project.getPmId();
		new WorkServiceImpl().removeUnStartWorkUser(Arrays.asList(userId), _id, currentUserId);
		c("project").updateOne(new BasicDBObject("_id", _id), new BasicDBObject("$set", new BasicDBObject("pmId", newPMId)));
		c("obs").updateOne(new BasicDBObject("_id", project.getOBS_id()),
				new BasicDBObject("$set", new BasicDBObject("managerId", newPMId)));

		List<Message> messages = new ArrayList<>();
		String content = "��Ŀ��" + project.getName();
		messages.add(Message.newInstance(msgSubject, content + "�����Ѳ��ٵ�����Ŀ����", currentUserId, userId, null));
		messages.add(Message.newInstance(msgSubject, content + "����ָ����������Ŀ����", currentUserId, newPMId, null));
		// ��Ϣ
		String projectStatus = project.getStatus();
		if (messages.size() > 0 && !ProjectStatus.Created.equals(projectStatus))
			sendMessages(messages);

		return get(_id);
	}

}
