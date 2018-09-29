package com.bizvisionsoft.service.model;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.GetValue;
import com.bizvisionsoft.annotations.md.mongocodex.Persistence;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.mongocodex.SetValue;
import com.bizvisionsoft.annotations.md.mongocodex.Strict;
import com.bizvisionsoft.annotations.md.service.Behavior;
import com.bizvisionsoft.annotations.md.service.ImageURL;
import com.bizvisionsoft.annotations.md.service.Label;
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
import com.bizvisionsoft.service.tools.MetaInfoWarpper;
import com.bizvisionsoft.service.tools.Util;
import com.mongodb.BasicDBObject;

/**
 * 项目基本模型，用于创建和编辑
 * 
 * @author hua
 *
 */
@Strict
@PersistenceCollection("project")
public class Project implements IOBSScope, ICBSScope, IWBSScope {

	/**
	 * 控制项目计划是否可以下达，根据项目状态判断
	 * 
	 * @return
	 */
	@Behavior("下达计划")
	private boolean enableDistribute() {
		return ProjectStatus.Processing.equals(status);
	}

	// @Behavior("我的项目/编辑项目") // 控制action
	// private boolean enableEdit() {
	// return true;// TODO 考虑权限
	// }
	//
	// @Behavior("我的项目/删除项目") // 控制action
	// private boolean enableDelete() {
	// return true;// TODO 考虑权限
	// }

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 标识属性
	/**
	 * _id
	 */
	@SetValue
	@GetValue
	private ObjectId _id;

	/**
	 * 编号
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
	 * 工作令号
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private String workOrder;

	public String getWorkOrder() {
		return workOrder;
	}

	/**
	 * 项目集Id
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private ObjectId projectSet_id;

	@SetValue
	@ReadValue
	private ProjectSet projectSet;

	/**
	 * 父项目Id
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
	 * WBS上级Id
	 */
	@Persistence
	private ObjectId wbsParent_id;

	/**
	 * EPS节点Id
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
	// 描述属性
	/**
	 * 名称
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
	 * 描述
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private String description;

	/**
	 * 类别：预研、科研(下级：新研、改性、适应性改造)、CBB
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private String catalog;

	public String getCatalog() {
		return catalog;
	}

	/**
	 * 项目等级 A, B, C
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private String classfication;

	/**
	 * 密级
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
			return "<span class='layui-badge-rim layui-btn-fluid'>" + status + "</span>";
		} else if (ProjectStatus.Processing.equals(status)) {
			if (stage != null) {
				return "<span class='layui-badge layui-bg-blue layui-btn-fluid'>" + stage + "</span>";
			} else {
				return "<span class='layui-badge layui-bg-blue layui-btn-fluid'>" + status + "</span>";
			}
		} else if (ProjectStatus.Closing.equals(status)) {
			return "<span class='layui-badge layui-bg-green layui-btn-fluid'>" + status + "</span>";
		} else if (ProjectStatus.Closed.equals(status)) {
			return "<span class='layui-badge layui-bg-gray layui-btn-fluid'>" + status + "</span>";
		} else {
			return "";
		}
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 计划属性
	/**
	 * 计划开始
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private Date planStart;
	/**
	 * 计划完成
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private Date planFinish;

	/**
	 * 计划工期
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
	 * 计划工时
	 */
	@SetValue
	private double summaryPlanWorks;

	@ReadValue("planWorks")
	public double getPlanWorks() {
		return summaryPlanWorks;
	}

	/**
	 * 实际开始
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private Date actualStart;

	/**
	 * 实际完成
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private Date actualFinish;

	/**
	 * 计划工期 ///TODO 根据计划开始和完成自动计算
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
	 * 计划工时 //TODO 计划工时的计算
	 */

	@SetValue
	private double summaryActualWorks;

	@ReadValue("actualWorks")
	public double getActualWorks() {
		return summaryActualWorks;
	}

	/**
	 * 启用阶段管理
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private boolean stageEnable;

	/**
	 * 阶段
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
	 * 项目经理
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private String pmId;

	@SetValue
	@ReadValue
	private String pmInfo;

	@SetValue
	private UserMeta pmInfo_meta;

	@ReadValue("pmInfoHtml")
	private String readPMInfoHtml() {
		if (pmId == null) {
			return "";
		}
		return "<div style='cursor:pointer;display:inline-flex;width: 100%;justify-content: space-between;'>"
				+ MetaInfoWarpper.userInfo(pmInfo_meta, pmInfo) + "</div>";
	}

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
	 * 承担单位
	 */
	@Persistence // 数据库存取
	private ObjectId impUnit_id;

	@SetValue // 查询服务设置
	@ReadValue // 表格用
	private String impUnitOrgFullName;

	public ObjectId getImpUnit_id() {
		return impUnit_id;
	}

	@WriteValue("impUnit") // 编辑器用
	public void setOrganization(Organization org) {
		this.impUnit_id = Optional.ofNullable(org).map(o -> o.get_id()).orElse(null);
	}

	@ReadValue("impUnit") // 编辑器用
	public Organization getOrganization() {
		return Optional.ofNullable(impUnit_id).map(_id -> ServicesLoader.get(OrganizationService.class).get(_id))
				.orElse(null);
	}
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@ReadValue
	@SetValue
	private Date settlementDate;

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 客户化基本属性
	/**
	 * 分为：纵向、横向、争取、自主
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private String type1;

	/**
	 * 分为：独立、联合、部分委托
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private String type2;

	/**
	 * 分为：其它
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
	 * 军兵种
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private List<String> arms;

	/**
	 * 战区
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private List<String> area;

	/**
	 * 主管机关
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private String customer;

	/**
	 * 主机厂所
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private String customer2;

	/**
	 * 目标值
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private String target;

	/**
	 * 成果形式
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private String achievement;

	/**
	 * 重大专项类别
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private String majorCategory;

	/**
	 * 所属平台/配套系统
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private String platform;

	/**
	 * 用户代表
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private String customerRepresentative;

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
	private String typeName = "项目";

	@Structure("我的项目（首页小组件）/list")
	private List<ProjectBoardInfo> getProjectBoardInfo() {
		return Arrays.asList(new ProjectBoardInfo().setProject(this));
	}

	@Structure("我的项目（首页小组件）/count")
	private long countProjectBoardInfo() {
		return 1;
	}

	@Persistence
	private OperationInfo creationInfo;

	@ReadValue("createOn")
	private Date readCreateOn() {
		return Optional.ofNullable(creationInfo).map(c -> c.date).orElse(null);
	}

	@ReadValue("createByInfo")
	private String readCreateBy() {
		return Optional.ofNullable(creationInfo).map(c -> c.userName).orElse(null);
	}

	@ReadValue("createByConsignerInfo")
	private String readCreateByConsigner() {
		return Optional.ofNullable(creationInfo).map(c -> c.consignerName).orElse(null);
	}

	@Persistence
	private OperationInfo approveInfo;

	@ReadValue("approveOn")
	private Date readApproveOn() {
		return Optional.ofNullable(approveInfo).map(c -> c.date).orElse(null);
	}

	@ReadValue("approveByInfo")
	private String readApproveBy() {
		return Optional.ofNullable(approveInfo).map(c -> c.userName).orElse(null);
	}

	@ReadValue("approveByConsignerInfo")
	private String readApproveByConsigner() {
		return Optional.ofNullable(approveInfo).map(c -> c.consignerName).orElse(null);
	}

	@Persistence
	private OperationInfo startInfo;

	@ReadValue({ "startOn", "start" })
	private Date readStartOn() {
		return Optional.ofNullable(startInfo).map(c -> c.date).orElse(null);
	}

	@ReadValue("startByInfo")
	private String readStartBy() {
		return Optional.ofNullable(startInfo).map(c -> c.userName).orElse(null);
	}

	@ReadValue("startByConsignerInfo")
	private String readStartByConsigner() {
		return Optional.ofNullable(startInfo).map(c -> c.consignerName).orElse(null);
	}

	@Persistence
	private OperationInfo finishInfo;

	@ReadValue({ "finishOn", "finish" })
	private Date readFinshOn() {
		return Optional.ofNullable(finishInfo).map(c -> c.date).orElse(null);
	}

	@ReadValue("finishByInfo")
	private String readFinishBy() {
		return Optional.ofNullable(finishInfo).map(c -> c.userName).orElse(null);
	}

	@ReadValue("finishByConsignerInfo")
	private String readFinishByConsigner() {
		return Optional.ofNullable(finishInfo).map(c -> c.consignerName).orElse(null);
	}

	@Persistence
	private OperationInfo closeInfo;

	@ReadValue({ "closeOn" })
	private Date readCloseOn() {
		return Optional.ofNullable(closeInfo).map(c -> c.date).orElse(null);
	}

	@ReadValue("closeByInfo")
	private String readCloseBy() {
		return Optional.ofNullable(closeInfo).map(c -> c.userName).orElse(null);
	}

	@ReadValue("closeByConsignerInfo")
	private String readCloseByConsigner() {
		return Optional.ofNullable(closeInfo).map(c -> c.consignerName).orElse(null);
	}

	@Persistence
	private OperationInfo distributeInfo;

	@ReadValue({ "distributeOn" })
	private Date readDistributeOn() {
		return Optional.ofNullable(distributeInfo).map(c -> c.date).orElse(null);
	}

	@ReadValue("distributeByInfo")
	private String readDistributeBy() {
		return Optional.ofNullable(distributeInfo).map(c -> c.userName).orElse(null);
	}

	@ReadValue("distributeByConsignerInfo")
	private String readDistributeByConsigner() {
		return Optional.ofNullable(distributeInfo).map(c -> c.consignerName).orElse(null);
	}

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

	public Project setCreationInfo(OperationInfo creationInfo) {
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

		OBSItem obsRoot = new OBSItem()// 创建本项目的OBS根节点
				.set_id(new ObjectId())// 设置_id与项目关联
				.setScope_id(_id)// 设置scope_id表明该组织节点是该项目的组织
				.setParent_id(obsParent_id)// 设置上级的id
				.setName(getName() + "项目组")// 设置该组织节点的默认名称
				.setRoleId(OBSItem.ID_PM)// 设置该组织节点的角色id
				.setRoleName(OBSItem.NAME_PM)// 设置该组织节点的名称
				.setManagerId(getPmId()) // 设置该组织节点的角色对应的人
				.setScopeRoot(true);// 区分这个节点是范围内的根节点

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
	// 工期完成率 百分比
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
	// 工作量完成率 百分比

	@SetValue("summaryPlanDuration")
	private double summaryPlanDuration;

	@SetValue("summaryActualDuration")
	private double summaryActualDuration;

	/**
	 * 项目所有工作累计的实际工期与计划工期的比值，反映项目工作量的完成情况。
	 * 
	 * @return
	 */
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
	// 进度完成率 百分比
	@SetValue
	private List<ObjectId> stage_ids;

	@SetValue("sar")
	private Double sar;

	@ReadValue("sar")
	public Double getSAR() {
		if (actualFinish == null) {// 这个指标不适用于未完成项目
			return null;
		}
		return sar != null ? (sar * Math.pow(0.8, getChangeCount())) : null;
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
		double budget = getBudget();
		if (budget != 0) {
			return getCost() / budget;
		}
		return null;
	}

	@ReadValue("bdr")
	public Double getBDR() {
		if (getActualFinish() == null) {// 这个指标不适用于未完成项目
			return null;
		}

		double budget = getBudget();
		if (budget != 0) {
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
		// 如果当前时间或完成时间已经超过了计划完成，提示为超期
		if (_actual.after(_plan)) {
			return "<span class='layui-badge layui-bg-red' style='width:60px;'>超期</span>";
		}

		if (overdueIndex != null) {
			switch (overdueIndex) {
			case 0:
				return "<span class='layui-badge layui-bg-red' style='width:60px;'>Ⅰ级预警</span>";
			case 1:
				return "<span class='layui-badge layui-bg-orange' style='width:60px;'>Ⅱ级预警</span>";
			case 2:
				return "<span class='layui-badge layui-bg-blue' style='width:60px;'>Ⅲ级预警</span>";
			}
		}

		return "";

		//////////////////////////////////////
		// 已经通过上面的排程估算替代
		// if ("预警".equals(overdueWarning)) {
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
			return "<span class='layui-badge'>超支</span>";
		} else {
			return "";
		}
	}

	@ReadValue("overcost")
	public double getOvercost() {
		return getCost() - getBudget();
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 获得给定用户在项目中的角色
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
	@SetValue
	private Double changeCount;

	public double getChangeCount() {
		return changeCount.doubleValue();
	}

	@ReadValue
	@SetValue
	private String changeStatus;

	@Persistence
	@ReadValue
	@WriteValue
	private Boolean startApproved;

	public String getChangeStatus() {
		return changeStatus;
	}

	@Behavior({ "删除", "编辑" })
	private boolean behaviourEditProjectInfo() {
		return ProjectStatus.Created.equals(status);
	}

	@Behavior("设置编号")
	private boolean behaviourEditProjectId() {
		return ProjectStatus.Created.equals(status) && Util.isEmptyOrNull(id);
	}

	@Behavior("批准启动")
	private boolean behaviourApproveProjectStart() {
		return ProjectStatus.Created.equals(status) && !Boolean.TRUE.equals(startApproved);
	}

	public Boolean getStartApproved() {
		return startApproved;
	}

	public Project setStartApproved(boolean startApproved) {
		this.startApproved = startApproved;
		return this;
	}

}
