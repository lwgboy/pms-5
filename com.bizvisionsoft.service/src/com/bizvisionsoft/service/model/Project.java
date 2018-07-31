package com.bizvisionsoft.service.model;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.Generator;
import com.bizvisionsoft.annotations.md.mongocodex.GetValue;
import com.bizvisionsoft.annotations.md.mongocodex.Persistence;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.mongocodex.SetValue;
import com.bizvisionsoft.annotations.md.mongocodex.Strict;
import com.bizvisionsoft.annotations.md.service.Behavior;
import com.bizvisionsoft.annotations.md.service.ImageURL;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadOptions;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.RoleBased;
import com.bizvisionsoft.annotations.md.service.Structure;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.service.CBSService;
import com.bizvisionsoft.service.EPSService;
import com.bizvisionsoft.service.OBSService;
import com.bizvisionsoft.service.OrganizationService;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.ProjectSetService;
import com.bizvisionsoft.service.RiskService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.UserService;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.sn.WorkOrderGenerator;
import com.mongodb.BasicDBObject;

/**
 * ��Ŀ����ģ�ͣ����ڴ����ͱ༭
 * 
 * @author hua
 *
 */
@Strict
@PersistenceCollection("project")
public class Project implements IOBSScope, ICBSScope, IWBSScope {

	/**
	 * ������Ŀ�ƻ��Ƿ�����´������Ŀ״̬�ж�
	 * 
	 * @return
	 */
	@Behavior("�´�ƻ�")
	private boolean enableDistribute() {
		return ProjectStatus.Processing.equals(status);
	}

	// @Behavior("�ҵ���Ŀ/�༭��Ŀ") // ����action
	// private boolean enableEdit() {
	// return true;// TODO ����Ȩ��
	// }
	//
	// @Behavior("�ҵ���Ŀ/ɾ����Ŀ") // ����action
	// private boolean enableDelete() {
	// return true;// TODO ����Ȩ��
	// }

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// ��ʶ����
	/**
	 * _id
	 */
	@SetValue
	@GetValue
	private ObjectId _id;

	/**
	 * ���
	 */
	@ReadValue
	@Label(Label.ID_LABEL)
	@WriteValue
	@Persistence
	// @Generator(name = Generator.DEFAULT_NAME, key = Generator.DEFAULT_KEY,
	// generator = ProjectGenerator.class, callback = Generator.NONE_CALLBACK)
	private String id;

	@Override
	public String getProjectNumber() {
		return id;
	}

	/**
	 * �������
	 */
	@ReadValue
	@WriteValue
	@Persistence
	@Generator(name = Generator.DEFAULT_NAME, key = "project", generator = WorkOrderGenerator.class, callback = Generator.NONE_CALLBACK)
	private String workOrder;

	public String getWorkOrder() {
		return workOrder;
	}

	public Project generateWorkOrder() {
		this.workOrder = ServicesLoader.get(ProjectService.class).generateWorkOrder(get_id());
		return this;
	}

	/**
	 * ��Ŀ��Id
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private ObjectId projectSet_id;

	@SetValue
	@ReadValue
	private ProjectSet projectSet;

	/**
	 * ����ĿId
	 */
	@Persistence
	private ObjectId parentProject_id;

	@SetValue
	@ReadValue
	private Project parentProject;

	public ObjectId getParentProject_id() {
		return parentProject_id;
	}

	/**
	 * WBS�ϼ�Id
	 */
	@Persistence
	private ObjectId wbsParent_id;

	/**
	 * EPS�ڵ�Id
	 */
	@Persistence
	private ObjectId eps_id;

	@SetValue
	@ReadValue
	private EPS eps;

	@ReadValue("epsName")
	public String getEPSName() {
		if (eps != null)
			return eps.getName();

		return "";
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// ��������
	/**
	 * ����
	 */
	@ReadValue
	@WriteValue
	@Persistence
	@Label(Label.NAME_LABEL)
	private String name;

	@Override
	public String getProjectName() {
		return name;
	}

	/**
	 * ����
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private String description;

	/**
	 * ���Ԥ�С�����(�¼������С����ԡ���Ӧ�Ը���)��CBB
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private String catalog;

	public String getCatalog() {
		return catalog;
	}

	/**
	 * ��Ŀ�ȼ� A, B, C
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private String classfication;

	/**
	 * �ܼ�
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private String securityLevel;

	@ReadValue
	@WriteValue
	@Persistence
	private String status;

	@ReadValue("statusHtml")
	public String getStatusHtml() {
		if (ProjectStatus.Created.equals(status)) {
			return "<span class='layui-badge layui-bg-blue layui-btn-fluid'>" + status + "</span>";
		} else if (ProjectStatus.Processing.equals(status)) {
			if (stage != null) {
				return "<span class='layui-badge layui-bg-blue layui-btn-fluid'>" + stage + "</span>";
			} else {
				return "<span class='layui-badge layui-bg-blue layui-btn-fluid'>" + status + "</span>";
			}
		} else if (ProjectStatus.Closing.equals(status)) {
			return "<span class='layui-badge layui-bg-green layui-btn-fluid'>" + status + "</span>";
		} else if (ProjectStatus.Closed.equals(status)) {
			return "<span class='layui-badge layui-bg-green layui-btn-fluid'>" + status + "</span>";
		} else if (ProjectStatus.Suspended.equals(status)) {
			return "<span class='layui-badge layui-bg-gray layui-btn-fluid'>" + status + "</span>";
		} else if (ProjectStatus.Terminated.equals(status)) {
			return "<span class='layui-badge layui-bg-black layui-btn-fluid'>" + status + "</span>";
		} else {
			return "";
		}
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// �ƻ�����
	/**
	 * �ƻ���ʼ
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private Date planStart;
	/**
	 * �ƻ����
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private Date planFinish;

	/**
	 * �ƻ�����
	 **/
	@ReadValue("planDuration")
	@GetValue("planDuration")
	public int getPlanDuration() {
		if (planFinish != null && planStart != null) {
			return (int) ((planFinish.getTime() - planStart.getTime()) / (1000 * 3600 * 24));
		} else {
			return 0;
		}
	}

	/**
	 * �ƻ���ʱ
	 */
	@SetValue
	private double summaryPlanWorks;

	@ReadValue("planWorks")
	public double getPlanWorks() {
		return summaryPlanWorks;
	}

	/**
	 * ʵ�ʿ�ʼ
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private Date actualStart;

	/**
	 * ʵ�����
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private Date actualFinish;

	/**
	 * ����ʱ��
	 */
	@ReadValue
	@SetValue
	private Date startOn;

	/**
	 * �깤ʱ��
	 */
	@ReadValue
	@SetValue
	private Date finishOn;

	@ReadValue("start")
	public Date getStart_date() {
		return startOn;
	}

	@ReadValue("finish")
	public Date getEnd_date() {
		return finishOn;
	}

	/**
	 * �ƻ����� ///TODO ���ݼƻ���ʼ������Զ�����
	 */
	@ReadValue
	@GetValue("actualDuration")
	public int getActualDuration() {
		if (actualFinish != null && actualStart != null) {
			return (int) ((actualFinish.getTime() - actualStart.getTime()) / (1000 * 3600 * 24));
		} else if (actualFinish == null && actualStart != null) {
			return (int) (((new Date()).getTime() - actualStart.getTime()) / (1000 * 3600 * 24));
		} else {
			return 0;
		}
	}

	/**
	 * �ƻ���ʱ //TODO �ƻ���ʱ�ļ���
	 */

	@SetValue
	private double summaryActualWorks;

	@ReadValue("actualWorks")
	public double getActualWorks() {
		return summaryActualWorks;
	}

	/**
	 * ���ý׶ι���
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private boolean stageEnable;

	/**
	 * �׶�
	 */
	@ReadValue
	@SetValue
	private Work stage;

	@Persistence
	private ObjectId stage_id;

	public ObjectId getStage_id() {
		return stage_id;
	}

	@Persistence
	private ObjectId projectTemplate_Id;

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * ��Ŀ����
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private String pmId;

	@SetValue
	@ReadValue
	private String pmInfo;

	@WriteValue("pm")
	private void setPM(User pm) {
		this.pmId = Optional.ofNullable(pm).map(o -> o.getUserId()).orElse(null);
	}

	@ReadValue("pm")
	private User getPM() {
		return Optional.ofNullable(pmId).map(id -> ServicesLoader.get(UserService.class).get(id)).orElse(null);
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * �е���λ
	 */
	@Persistence // ���ݿ��ȡ
	private ObjectId impUnit_id;

	@SetValue // ��ѯ��������
	@ReadValue // �����
	private String impUnitOrgFullName;

	public ObjectId getImpUnit_id() {
		return impUnit_id;
	}

	@WriteValue("impUnit") // �༭����
	public void setOrganization(Organization org) {
		this.impUnit_id = Optional.ofNullable(org).map(o -> o.get_id()).orElse(null);
	}

	@ReadValue("impUnit") // �༭����
	public Organization getOrganization() {
		return Optional.ofNullable(impUnit_id).map(_id -> ServicesLoader.get(OrganizationService.class).get(_id))
				.orElse(null);
	}
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@ReadValue
	@SetValue
	private Date settlementDate;

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// �ͻ�����������
	/**
	 * ��Ϊ�����򡢺�����ȡ������
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private String type1;

	/**
	 * ��Ϊ�����������ϡ�����ί��
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private String type2;

	/**
	 * ��Ϊ������
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private String type3;

	@ReadValue("type2_3")
	private String getType2_3() {
		String str = "";
		if (type2 != null)
			str += type2 + " ";
		if (type3 != null)
			str += type3;
		return str.trim();
	}

	/**
	 * ������
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private List<String> arms;

	/**
	 * ս��
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private List<String> area;

	/**
	 * ���ܻ���
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private String customer;

	/**
	 * ��������
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private String customer2;

	/**
	 * Ŀ��ֵ
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private String target;

	/**
	 * �ɹ���ʽ
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private String achievement;

	/**
	 * �ش�ר�����
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private String majorCategory;

	/**
	 * ����ƽ̨/����ϵͳ
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private String platform;

	/**
	 * �û�����
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private String customerRepresentative;

	/**
	 * ������
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private String code;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@WriteValue("eps_or_projectset_id")
	public void setEPSorProjectSet(Object element) {
		if (element instanceof EPS)
			this.eps_id = ((EPS) element).get_id();
		if (element instanceof ProjectSet)
			this.projectSet_id = ((ProjectSet) element).get_id();
	}

	@ReadValue("eps_or_projectset_id")
	public Object getEPSOrProjectSet() {
		if (eps_id != null)
			return ServicesLoader.get(EPSService.class).get(eps_id);
		if (projectSet_id != null)
			return ServicesLoader.get(ProjectSetService.class).get(projectSet_id);
		return null;
	}

	@ReadOptions("catalog")
	public Map<String, Object> getCatalogOptions() {
		LinkedHashMap<String, Object> options = new LinkedHashMap<String, Object>();
		options.put("Ԥ��", "Ԥ��");
		options.put("����-����", "����-����");
		options.put("����-����", "����-����");
		options.put("����-��Ӧ�Ը���", "����-��Ӧ�Ը���");
		options.put("CBB", "CBB");
		return options;
	}

	@Override
	@Label
	public String toString() {
		if (id != null)
			return name + " [" + id + "]";
		return name;
	}

	@ImageURL("name")
	private String icon = "/img/project_c.svg";

	@ReadValue(ReadValue.TYPE)
	@Exclude
	private String typeName = "��Ŀ";

	@Structure("�ҵ���Ŀ����ҳС�����/list")
	private List<ProjectBoardInfo> getProjectBoardInfo() {
		return Arrays.asList(new ProjectBoardInfo().setProject(this));
	}

	@Structure("�ҵ���Ŀ����ҳС�����/count")
	private long countProjectBoardInfo() {
		return 1;
	}

	@Persistence
	private CreationInfo creationInfo;

	@Persistence
	private ObjectId obs_id;

	@Persistence
	private ObjectId cbs_id;

	public ObjectId get_id() {
		return _id;
	}

	public Project set_id(ObjectId _id) {
		this._id = _id;
		return this;
	}

	public Date getPlanStart() {
		return planStart;
	}

	public Date getPlanFinish() {
		return planFinish;
	}

	public Date getActualStart() {
		return actualStart;
	}

	public Date getActualFinish() {
		return actualFinish;
	}

	public Project setStageEnable(boolean stageEnable) {
		this.stageEnable = stageEnable;
		return this;
	}

	public Project setCreationInfo(CreationInfo creationInfo) {
		this.creationInfo = creationInfo;
		return this;
	}

	public ObjectId getProjectTemplate_id() {
		return projectTemplate_Id;
	}

	public String getPmId() {
		return pmId;
	}

	public Project setOBS_id(ObjectId obs_id) {
		this.obs_id = obs_id;
		return this;
	}

	public Project setCBS_id(ObjectId cbs_id) {
		this.cbs_id = cbs_id;
		return this;
	}

	public ObjectId getOBS_id() {
		return obs_id;
	}

	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}

	public ObjectId getProjectSet_id() {
		return projectSet_id;
	}

	@Override
	public ObjectId getScope_id() {
		return _id;
	}

	@Override
	public String getScopeName() {
		return name;
	}

	@Override
	public ObjectId getCBS_id() {
		return cbs_id;
	}

	@Override
	public Date[] getCBSRange() {
		return new Date[] { planStart, planFinish };
	}

	public boolean isStageEnable() {
		return stageEnable;
	}

	public String getStatus() {
		return status;
	}

	public Project setStatus(String status) {
		this.status = status;
		return this;
	}

	@Override
	public OBSItem newOBSScopeRoot() {

		ObjectId obsParent_id = Optional.ofNullable(projectSet_id)
				.map(pjset_id -> ServicesLoader.get(ProjectSetService.class).get(pjset_id)).map(ps -> ps.getOBS_id())
				.orElse(null);

		OBSItem obsRoot = new OBSItem()// ��������Ŀ��OBS���ڵ�
				.set_id(new ObjectId())// ����_id����Ŀ����
				.setScope_id(_id)// ����scope_id��������֯�ڵ��Ǹ���Ŀ����֯
				.setParent_id(obsParent_id)// �����ϼ���id
				.setName(getName() + "��Ŀ��")// ���ø���֯�ڵ��Ĭ������
				.setRoleId(OBSItem.ID_PM)// ���ø���֯�ڵ�Ľ�ɫid
				.setRoleName(OBSItem.NAME_PM)// ���ø���֯�ڵ������
				.setManagerId(getPmId()) // ���ø���֯�ڵ�Ľ�ɫ��Ӧ����
				.setScopeRoot(true);// ��������ڵ��Ƿ�Χ�ڵĸ��ڵ�

		return obsRoot;

	}

	@Override
	public void updateOBSRootId(ObjectId obs_id) {
		ServicesLoader.get(ProjectService.class).update(new FilterAndUpdate().filter(new BasicDBObject("_id", _id))
				.set(new BasicDBObject("obs_id", obs_id)).bson());
		this.obs_id = obs_id;
	}

	@Persistence
	private String checkoutBy;

	@Persistence
	private ObjectId space_id;

	@Override
	public Workspace getWorkspace() {
		return ServicesLoader.get(ProjectService.class).getWorkspace(_id);
	}

	@Override
	public ObjectId getParent_id() {
		return null;
	}

	@Override
	public ObjectId getProject_id() {
		return _id;
	}

	@Override
	public List<WorkLink> createGanttLinkDataSet() {
		return ServicesLoader.get(WorkService.class).createProjectLinkDataSet(_id);
	}

	@Override
	public List<Work> createGanttTaskDataSet() {
		return ServicesLoader.get(WorkService.class).createProjectTaskDataSet(_id);
	}

	public String getPmInfo() {
		return pmInfo;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// ��������� �ٷֱ�
	@ReadValue("dar")
	public Double getDAR() {
		int planDuration = getPlanDuration();
		if (planDuration != 0) {
			return 1d * getActualDuration() / planDuration;
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// ����������� �ٷֱ�

	@SetValue("summaryPlanDuration")
	private double summaryPlanDuration;

	@SetValue("summaryActualDuration")
	private double summaryActualDuration;

	@ReadValue("war")
	public Double getWAR() {
		if (getActualStart() == null) {
			return 0d;
		}
		if (getActualFinish() != null) {
			return 1d;
		}
		if (summaryPlanDuration != 0) {
			double d = 1d * summaryActualDuration / summaryPlanDuration;
			return d > 1d ? 1d : d;
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// ��������� �ٷֱ�
	@SetValue
	private List<ObjectId> stage_ids;

	@SetValue("sar")
	private Double sar;

	@ReadValue("sar")
	public Double getSAR() {
		return sar != null ? (sar * Math.pow(0.8, getChangeNo())) : null;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////

	private CBSItem cbsItem;

	@ReadValue("cost")
	public double getCost() {
		if (cbsItem == null) {
			cbsItem = ServicesLoader.get(CBSService.class).getCBSItemCost(cbs_id);
		}
		return Optional.ofNullable(cbsItem.cbsSubjectCost).orElse(0d);
	}

	@ReadValue("budget")
	public double getBudget() {
		if (cbsItem == null) {
			cbsItem = ServicesLoader.get(CBSService.class).getCBSItemCost(cbs_id);
		}
		return Optional.ofNullable(cbsItem.cbsSubjectBudget).orElse(0d);
	}

	@ReadValue("car")
	public Double getCAR() {
		if (getActualFinish() != null) {
			return 1d;
		}

		Double budget = getBudget();
		if (budget != null && budget != 0) {
			return getCost() / budget;
		}
		return null;
	}

	@ReadValue("bdr")
	public Double getBDR() {
		Double budget = getBudget();
		if (budget != null && budget != 0) {
			return (getCost() - budget) / budget;
		}
		return null;
	}

	// @SetValue
	// private String overdueWarning;

	@SetValue
	private Integer overdueIndex;

	@SetValue
	private Estimate scheduleEst;

	@ReadValue("estFinish")
	public Date getEstimateFinish() {
		return Optional.ofNullable(scheduleEst).map(s -> s.finish).orElse(null);
	}

	@ReadValue("estDuration")
	public Integer getEstimateDuration() {
		return Optional.ofNullable(scheduleEst).map(s -> s.duration).orElse(null);
	}

	@ReadValue("estOverdue")
	public Integer getEstimateOverdue() {
		return Optional.ofNullable(scheduleEst).map(s -> s.overdue).orElse(null);
	}

	@ReadValue("warningOverdue")
	public String getOverdueHtml() {
		Date _actual = getActualFinish();
		Date _plan = getPlanFinish();
		if (_actual == null) {
			_actual = new Date();
		}
		// �����ǰʱ������ʱ���Ѿ������˼ƻ���ɣ���ʾΪ����
		if (_actual.after(_plan)) {
			return "<span class='layui-badge layui-bg-red' style='width:60px;'>����</span>";
		}

		if (overdueIndex != null) {
			switch (overdueIndex) {
			case 0:
				return "<span class='layui-badge layui-bg-red' style='width:60px;'>��Ԥ��</span>";
			case 1:
				return "<span class='layui-badge layui-bg-orange' style='width:60px;'>��Ԥ��</span>";
			case 2:
				return "<span class='layui-badge layui-bg-blue' style='width:60px;'>��Ԥ��</span>";
			}
		}

		return "";

		//////////////////////////////////////
		// �Ѿ�ͨ��������ų̹������
		// if ("Ԥ��".equals(overdueWarning)) {
		// return "<span class='layui-badge layui-bg-orange'>" + overdueWarning +
		// "</span>";
		// } else {
		// return "";
		// }
	}

	public Integer getOverdueIndex() {
		return overdueIndex;
	}

	@ReadValue("warningOvercost")
	private String getOvercostHtml() {
		if (getOvercost() > 0) {
			return "<span class='layui-badge'>��֧</span>";
		} else {
			return "";
		}
	}

	@ReadValue("overcost")
	public double getOvercost() {
		return getCost() - getBudget();
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	// ��ø����û�����Ŀ�еĽ�ɫ
	@RoleBased
	private List<String> getProjectRole(@MethodParam(MethodParam.CURRENT_USER_ID) String userId) {
		return ServicesLoader.get(OBSService.class).getScopeRoleofUser(_id, userId);
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////////

	public Project setPmId(String pmId) {
		this.pmId = pmId;
		return this;
	}

	public Project setEps_id(ObjectId eps_id) {
		this.eps_id = eps_id;
		return this;
	}

	public Project setPlanStart(Date planStart) {
		this.planStart = planStart;
		return this;
	}

	public Project setPlanFinish(Date planFinish) {
		this.planFinish = planFinish;
		return this;
	}

	public Project setId(String id) {
		this.id = id;
		return this;
	}

	public Project setName(String name) {
		this.name = name;
		return this;
	}

	@ReadValue("pcp")
	public Double getDurationProbability() {
		return ServicesLoader.get(RiskService.class).getDurationProbability(_id);
	}

	public List<List<Double>> getDurationForcast() {
		return ServicesLoader.get(RiskService.class).getDurationForcast(_id);
	}

	@ReadValue
	@SetValue("changeNo")
	private Double changeNo;

	public double getChangeNo() {
		return changeNo != null ? changeNo.doubleValue() : 0;
	}

	@ReadValue
	@SetValue
	private String changeStatus;

	public String getChangeStatus() {
		return changeStatus;
	}

	public String getPPMId() {
		return null;
	}

	@Behavior({ "�༭������" })
	private boolean behaviourEdit() {
		return !ProjectStatus.Closing.equals(status) && !ProjectStatus.Terminated.equals(status);
	}

	@Behavior({ "ɾ��" })
	private boolean behaviourDelete() {
		return ProjectStatus.Created.equals(status);
	}

	@Behavior({ "��ֹ" })
	private boolean behaviourTerminate() {
		return !ProjectStatus.Created.equals(status) && !ProjectStatus.Closed.equals(status)
				&& !ProjectStatus.Terminated.equals(status);
	}

	@Behavior({ "��ͣ" })
	private boolean behaviourSuspend() {
		return ProjectStatus.Processing.equals(status);
	}

	@Behavior({ "���¿�ʼ" })
	private boolean behaviourReStart() {
		return ProjectStatus.Suspended.equals(status);
	}

}
