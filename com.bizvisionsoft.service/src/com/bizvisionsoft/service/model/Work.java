package com.bizvisionsoft.service.model;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;

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
import com.bizvisionsoft.service.OBSService;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.UserService;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.tools.MetaInfoWarpper;
import com.bizvisionsoft.service.tools.Util;
import com.mongodb.BasicDBObject;

/**
 * 
 * @author hua
 *
 */
@PersistenceCollection("work")
@Strict
public class Work implements ICBSScope, IOBSScope, IWBSScope, IWorkPackageMaster {

	/**
	 * 控制项目计划是否可以下达，根据项目状态判断
	 * 
	 * @return
	 */
	@Behavior("下达计划")
	private boolean enableDistribute() {
		return ProjectStatus.Processing.equals(status);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// id, 在gantt图中 使用String 类型传递，因此 ReadValue和WriteValue需要用方法重写
	@Persistence
	private ObjectId _id;

	@ReadValue("id")
	public String getId() {
		return _id == null ? null : _id.toHexString();
	}

	@WriteValue("id")
	public Work setId(String id) {
		this._id = id == null ? null : new ObjectId(id);
		return this;
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// parent_id, 在gantt图中 使用的字段为parent, String 类型传递，因此 ReadValue和WriteValue需要用方法重写
	// 写入parent时请注意，返回值表示了该parent值是否被更改。如果没有变化，返回false。告知调用者
	@Persistence
	private ObjectId parent_id;

	@ReadValue("parent")
	public String getParent() {
		return parent_id == null ? null : parent_id.toHexString();
	}

	@WriteValue("parent")
	public boolean setParent(Object parent) {
		ObjectId newParent_id;
		if (parent instanceof String) {
			newParent_id = new ObjectId((String) parent);
		} else {
			newParent_id = null;
		}
		if (!Util.equals(newParent_id, this.parent_id)) {
			this.parent_id = newParent_id;
			return true;
		} else {
			return false;
		}
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// project_id, 在gantt图中 使用的字段为project, String 类型传递，
	// 因此 ReadValue和WriteValue需要用方法重写
	// 甘特图组件（是指GanttPart, 并非Gantt）要求任务和关联关系必须带有project属性。
	// 如果不带有该属性，表示这些对象可能是客户端创建的
	// 写入时请注意，返回值表示了该parent值是否被更改。如果没有变化，返回false。告知调用者
	@Persistence
	private ObjectId project_id;

	@Override
	public ObjectId getProject_id() {
		return project_id;
	}

	@ReadValue("project")
	public String getProjectId() {
		return project_id == null ? null : project_id.toHexString();
	}

	@WriteValue("project")
	public boolean setProjectId(String project_id) {
		ObjectId newId;
		if (project_id instanceof String) {
			newId = new ObjectId((String) project_id);
		} else {
			newId = null;
		}
		if (!Util.equals(newId, this.project_id)) {
			this.project_id = newId;
			return true;
		} else {
			return false;
		}
	}

	@SetValue
	@ReadValue({ "projectName", "我的工作（日历牌）/details", "部门工作日程表/details" })
	private String projectName;

	@SetValue
	@ReadValue
	private String projectNumber;

	@Override
	public String getProjectName() {
		return projectName;
	}

	@Override
	public String getProjectNumber() {
		return projectNumber;
	}

	@ReadValue("projectText")
	public String getProjectText() {
		return projectName + " [" + projectNumber + "]";
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// index, 在gantt图中用于排序
	@WriteValue
	@ReadValue
	@Persistence
	private Integer index;

	public Work setIndex(int index) {
		this.index = index;
		return this;
	}

	public int getIndex() {
		return index;
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// WBS代码
	@WriteValue
	@ReadValue
	@Persistence
	private String wbsCode;

	public String getWBSCode() {
		return wbsCode;
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// WBS代码
	@Persistence
	private String code;

	@ReadValue("code")
	public String getCode() {
		return code;
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// text, 在gantt图text字段，数据库中为name字段
	@ReadValue({ "进度计划（查看）/name", "进度计划/name", "text" })
	@WriteValue
	@Persistence("name")
	@Label(Label.NAME_LABEL)
	private String text;

	@ReadValue({ "进度计划和监控（查看）/name", "进度计划和监控/name" })
	private String readWorkNameHTML() {
		if (stage) {
			String html = "<div style='display:inline-flex;justify-content:space-between;width:100%;padding-right:8px;'><div style='font-weight:bold;'>"
					+ text + "</div>";
			if (ProjectStatus.Created.equals(status))
				html += "<a class='layui-btn layui-btn-xs layui-btn-primary' style='display:block; width:50px;cursor: pointer;' href='"
						+ "start/" + "' target='_rwt'>" + "启动" + "</a>";
			else if (ProjectStatus.Processing.equals(status))
				html += "<a class='layui-btn layui-btn-xs layui-btn-primary' style='display:block; width:50px;cursor: pointer;' href='"
						+ "finish/" + "' target='_rwt'>" + "收尾" + "</a>";
			else if (ProjectStatus.Closing.equals(status))
				html += "<a class='layui-btn layui-btn-xs layui-btn-primary' style='display:block; width:50px;cursor: pointer;' href='"
						+ "close/" + "' target='_rwt'>" + "关闭" + "</a>";
			html += "</div>";
			return html;
		} else {
			return "<div>" + text + "</div>";
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// fullName, 在gantt图，编辑器中，数据库中均使用
	@WriteValue
	@SetValue
	private String fullName;

	@ReadValue("fullName")
	@GetValue("fullName")
	public String getFullName() {
		if (fullName == null || fullName.trim().isEmpty()) {
			fullName = text;
		}
		return fullName;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * 计划开始日期, 编辑器保存时需要校验
	 */
	@ReadValue("planStart")
	@Persistence("planStart")
	private Date planStart;

	@ReadValue("actualStart")
	@Persistence("actualStart")
	private Date actualStart;

	@WriteValue("项目甘特图/start_date")
	public boolean setStart_date(String start_date) {
		Date newDate = Util.str_date(start_date);
		if (actualStart != null) {
			if (!Util.equals(newDate, this.actualStart)) {
				actualStart = newDate;
				return true;
			}
		} else {
			if (!Util.equals(newDate, this.planStart)) {
				planStart = newDate;
				return true;
			}
		}
		return false;
	}

	@ReadValue({ "项目甘特图（无表格查看）/start_date", "项目甘特图（查看）/start_date", "项目甘特图（资源实际分配）/start_date", "项目进展甘特图/start_date",
			"项目基线甘特图/start_date" })
	public Date getStart_date() {
		if (actualStart != null) {
			return actualStart;
		}
		return planStart;
	}

	@ReadValue({ "项目甘特图/start_date", "项目甘特图（资源计划分配）/start_date", "我的工作（日历牌）/start_date", "部门工作日程表/start_date" })
	public Date getPlanStartDate() {
		return planStart;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////

	@ReadValue("terminateOn")
	@Persistence("terminateOn")
	private Date terminateOn;

	@ReadValue()
	@Persistence()
	private String terminateBy;

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// 计划完成日期, 编辑器保存时需要校验
	@ReadValue("planFinish")
	@Persistence("planFinish")
	private Date planFinish;

	@ReadValue("actualFinish")
	@Persistence("actualFinish")
	private Date actualFinish;

	/**
	 * <b>！！！Gantt图控件反写的方法 这个方法比较特殊！！！</b>
	 * <p>
	 * 注意返回true和false的目的是告知是否更改了该字段的值
	 * 
	 * @param start_date
	 *            <p>
	 *            接收的是JS传来的日期字符串。格式为yyyy-MM-dd'T'HH:mm:ss.SSS UTC
	 *            <p>
	 *            使用Util.str_date()方法可以转换
	 * @return
	 */
	@WriteValue("项目甘特图/end_date")
	public boolean setEnd_date(String end_date) {
		Date newDate = Util.str_date(end_date);
		if (actualFinish != null) {
			if (!Util.equals(newDate, this.actualFinish)) {
				actualFinish = newDate;
				return true;
			}
		} else {
			if (!Util.equals(newDate, this.planFinish)) {
				planFinish = newDate;
				return true;
			}
		}
		return false;
	}

	@ReadValue({ "项目甘特图（无表格查看）/end_date", "项目甘特图（查看）/end_date", "项目甘特图（资源实际分配）/end_date", "项目进展甘特图/end_date",
			"项目基线甘特图/end_date", "部门工作日程表/end_date" })
	public Date getEnd_date() {
		if (actualFinish != null) {
			return actualFinish;
		} else if (actualStart != null) {
			return new Date(planFinish.getTime() - planStart.getTime() + actualStart.getTime());
		}
		return planFinish;
	}

	@ReadValue({ "项目甘特图/end_date", "项目甘特图（资源计划分配）/end_date", "我的工作（日历牌）/end_date" })
	public Date getPlanEndDate() {
		return planFinish;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// 工期, 需要保存，但无需传递到gantt和编辑器
	@SetValue
	@ReadValue
	private int planDuration;

	@GetValue("planDuration")
	public int getPlanDuration() {
		if (planFinish != null && planStart != null) {
			return (int) ((planFinish.getTime() - planStart.getTime()) / (1000 * 3600 * 24));
		} else {
			return 0;
		}
	}

	@GetValue("actualDuration")
	@ReadValue("actualDuration")
	public int getActualDuration() {
		if (actualFinish != null && actualStart != null) {
			return (int) ((actualFinish.getTime() - actualStart.getTime()) / (1000 * 3600 * 24));
		} else if (actualFinish == null && actualStart != null) {
			return (int) (((new Date()).getTime() - actualStart.getTime()) / (1000 * 3600 * 24));
		} else {
			return 0;
		}
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// 工时, 需要保存
	@SetValue
	private double planWorks;

	@SetValue
	private double summaryPlanWorks;

	@ReadValue("planWorks")
	@GetValue("planWorks")
	public double getPlanWorks() {
		if (summary) {
			return summaryPlanWorks;
		}
		return planWorks;
	}

	@SetValue
	private double actualWorks;

	@SetValue
	private double summaryActualWorks;

	@ReadValue("actualWorks")
	@GetValue("actualWorks")
	public double getActualWorks() {
		if (summary) {
			return summaryActualWorks;
		}
		return actualWorks;
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// 工期完成率 百分比
	@ReadValue("dar")
	public Double getDAR() {
		if (planDuration != 0 && !milestone) {
			return 1d * getActualDuration() / planDuration;
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// 工作量完成率 百分比
	@Persistence
	private Date estimatedFinish;

	@SetValue("summaryPlanDuration")
	private double summaryPlanDuration;

	@SetValue("summaryActualDuration")
	private double summaryActualDuration;

	@ReadValue("war")
	public Double getWAR() {
		if (milestone)
			return null;

		if (getActualStart() == null)
			return 0d;

		if (getActualFinish() != null)
			return 1d;

		Double d = null;

		if (summary) {
			if (summaryPlanDuration != 0) {
				d = 1d * summaryActualDuration / summaryPlanDuration;
			}
		} else {
			if (estimatedFinish != null && planStart != null) {
				d = 1d * getActualDuration()
						/ ((int) ((estimatedFinish.getTime() - planStart.getTime()) / (1000 * 3600 * 24)));
			} else {
				if (getPlanDuration() != 0) {
					d = 1d * getActualDuration() / getPlanDuration();
				}
			}
		}
		d = d == null ? 0d : ((d != null && d > 1d) ? 1d : d);
		return d;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// 完成百分比
	@ReadValue
	@WriteValue
	@Persistence
	private Double progress;

	////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// 进度完成率指标 百分比
	@ReadValue("sar")
	public Double getSAR() {
		if (stage && actualStart != null && actualFinish != null && !milestone) {
			double d = 1d * (planFinish.getTime() - planStart.getTime())
					/ (actualFinish.getTime() - actualStart.getTime());
			return d > 1d ? 1d : d;
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// 如果是里程碑，gantt的type为milestone，否则为task。
	// 如果在gantt中更新了task,使得他有子工作，gantt将type改为project
	@Persistence
	private boolean milestone;

	@ImageURL("milestoneIcon")
	private String getMilestoneIcon() {
		if (milestone)
			return "/img/milestone_c.svg";
		return null;
	}

	@Persistence
	private boolean summary;

	@Persistence
	@Behavior("进入阶段页面")
	private boolean stage;

	@ReadValue
	@SetValue
	private String stageName;

	@Persistence
	private boolean distributed;

	@ReadValue("distributeIcon")
	private String getDistributedIcon() {
		if (!distributed) {
			return "<span class='layui-badge layui-bg-orange'>未下达</span>";
		} else {
			return "";
		}
	}

	@Persistence
	@ReadValue
	@WriteValue
	private String status;

	@ReadValue("type")
	public String getType() {
		if (milestone)
			return "milestone";
		else if (summary)
			return "project";
		else
			return "task";
	}

	@WriteValue("type")
	public boolean setType(String type) {
		boolean milestone = "milestone".equals(type);
		boolean summary = "project".equals(type);
		if (this.milestone != milestone || this.summary != summary) {
			this.milestone = milestone;
			this.summary = summary;
			return true;
		}
		return false;
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// 存储在数据库中的是管理级别。表现在Gantt中的是barstyle,样式
	@ReadValue
	@WriteValue
	private String barstyle;

	@ReadValue("manageLevel")
	@GetValue("manageLevel")
	public String getManageLevel() {
		if ("level1_task".equals(barstyle)) {
			return "1";
		} else if ("level2_task".equals(barstyle)) {
			return "2";
		} else if ("level3_task".equals(barstyle)) {
			return "3";
		} else {
			return null;
		}

	}

	@ReadValue("manageLevelHtml")
	private String getManageLevelHtml() {
		if ("level1_task".equals(barstyle)) {
			return "<span class='layui-badge layui-bg-cyan'>1</span>";
		} else if ("level2_task".equals(barstyle)) {
			return "<span class='layui-badge layui-bg-blue'>2</span>";
		} else if ("level3_task".equals(barstyle)) {
			return "<span class='layui-badge layui-bg-green'>3</span>";
		} else {
			return "";
		}
	}

	@SetValue("manageLevel")
	public Work setManageLevel(String level) {
		if ("1".equals(level)) {
			barstyle = "level1_task";
		} else if ("2".equals(level)) {
			barstyle = "level2_task";
		} else if ("3".equals(level)) {
			barstyle = "level3_task";
		}
		return this;
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// 以下是控制gantt的客户端的属性
	@ReadValue("editable")
	public Boolean getEditable() {
		return true;
	}

	@ReadValue("open")
	public Boolean getOpen() {
		return true;
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// 工作的标签文本
	@Label
	public String toString() {
		return text;
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * 工作角色
	 */
	@ReadValue
	@WriteValue
	@Persistence
	private String chargerId;

	@SetValue
	@ReadValue
	private String chargerInfo;

	@SetValue
	private UserMeta chargerInfo_meta;

	@WriteValue("charger")
	private void setCharger(User charger) {
		this.chargerId = Optional.ofNullable((User) charger).map(o -> o.getUserId()).orElse(null);
	}

	@ReadValue("charger")
	private User getCharger() {
		return Optional.ofNullable(chargerId).map(id -> ServicesLoader.get(UserService.class).get(id)).orElse(null);
	}

	@ReadValue({ "进度计划和监控/chargerInfoWithDistributeIcon", "进度计划和监控（查看）/chargerInfoWithDistributeIcon",
			"进度计划/chargerInfoWithDistributeIcon", "进度计划（查看）/chargerInfoWithDistributeIcon" })
	private String getChargerInfoWithIcon() {
		if (chargerId == null) {
			return "";
		}
		return "<div style='cursor:pointer;display:inline-flex;width: 100%;justify-content: space-between;'>"
				+ MetaInfoWarpper.userInfo(chargerInfo_meta, chargerInfo) + getDistributedIcon() + "</div>";
	}

	@ReadValue("chargerInfoHtml")
	public String getChargerInfoHtml() {
		if (chargerInfo == null) {
			return "";
		}
		return "<div style='cursor:pointer;display:inline-flex;width: 100%;justify-content: space-between;'>"
				+ MetaInfoWarpper.userInfo(chargerInfo_meta, chargerInfo) + "</div>";
	}

	public String getAssignerInfo() {
		if (assignerId == null) {
			return "";
		}
		return assignerInfo;
	}

	@ReadValue("部门工作日程表/section_id")
	public String getChargerId() {
		return chargerId;
	}

	@ReadValue
	@WriteValue
	@Persistence
	private String assignerId;

	@SetValue
	@ReadValue
	private String assignerInfo;

	@WriteValue("assigner")
	private void setAssigner(User assigner) {
		this.assignerId = Optional.ofNullable(assigner).map(o -> o.getUserId()).orElse(null);
	}

	@ReadValue("assigner")
	private User getAssigner() {
		return Optional.ofNullable(assignerId).map(id -> ServicesLoader.get(UserService.class).get(id)).orElse(null);
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@ReadValue
	@WriteValue
	@Persistence
	private List<TrackView> workPackageSetting;

	public List<TrackView> getWorkPackageSetting() {
		return workPackageSetting;
	}

	public void setWorkPackageSetting(List<TrackView> workPackageSetting) {
		this.workPackageSetting = workPackageSetting;
	}

	@Persistence
	@ReadValue
	@WriteValue
	private List<String> certificates;
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@Persistence
	private ObjectId cbs_id;

	@Persistence
	private ObjectId obs_id;
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Structure({ "项目WBS/list", "进度计划和监控/list", "进度计划和监控（查看）/list", "进度计划/list", "进度计划（查看）/list" })
	private List<Work> listChildren() {
		return ServicesLoader.get(WorkService.class).listChildren(_id);
	}

	@Structure({ "项目WBS/count", "进度计划和监控/count", "进度计划和监控（查看）/count", "进度计划/count", "进度计划（查看）/count" })
	private long countChildren() {
		return ServicesLoader.get(WorkService.class).countChildren(_id);
	}

	@Persistence
	private List<String> viewId;

	public List<String> getViewId() {
		return viewId;
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

	public Date getStartOn() {
		return readStartOn();
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

	public Date getFinishOn() {
		return readFinshOn();
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

	public Work set_id(ObjectId _id) {
		this._id = _id;
		return this;
	}

	public ObjectId get_id() {
		return _id;
	}

	public Work setProject_id(ObjectId project_id) {
		this.project_id = project_id;
		return this;
	}

	public Work setParent_id(ObjectId parent_id) {
		this.parent_id = parent_id;
		return this;
	}

	public ObjectId getParent_id() {
		return parent_id;
	}

	public int index() {
		return index;
	}

	public boolean isSummary() {
		return summary;
	}

	public boolean isMilestone() {
		return milestone;
	}

	public boolean isStage() {
		return stage;
	}

	public Work setStage(boolean stage) {
		this.stage = stage;
		return this;
	}

	public String getText() {
		return text;
	}

	public Work setText(String text) {
		this.text = text;
		return this;
	}

	public Project getProject() {
		return Optional.ofNullable(project_id).map(_id -> ServicesLoader.get(ProjectService.class).get(_id))
				.orElse(null);
	}

	@Override
	public ObjectId getScope_id() {
		return _id;
	}

	@Override
	public ObjectId getCBS_id() {
		return cbs_id;
	}

	@Override
	public String getScopeName() {
		return text;
	}

	@Override
	public Date[] getCBSRange() {
		return ServicesLoader.get(ProjectService.class).getPlanDateRange(project_id).toArray(new Date[0]);
	}

	@Override
	public ObjectId getOBS_id() {
		return obs_id;
	}

	@Override
	public OBSItem newOBSScopeRoot() {
		ObjectId obsParent_id = Optional.ofNullable(getProject()).map(ps -> ps.getOBS_id()).orElse(null);

		OBSItem obsRoot = new OBSItem()// 创建本项目的OBS根节点
				.set_id(new ObjectId())// 设置_id与项目关联
				.setScope_id(_id)// 设置scope_id表明该组织节点是该项目的组织
				.setParent_id(obsParent_id)// 设置上级的id
				.setName(text + "团队")// 设置该组织节点的默认名称
				.setRoleId(OBSItem.ID_CHARGER)// 设置该组织节点的角色id
				.setRoleName(OBSItem.NAME_CHARGER)// 设置该组织节点的名称
				.setManagerId(chargerId) // 设置该组织节点的角色对应的人
				.setScopeRoot(true);// 区分这个节点是范围内的根节点

		return obsRoot;
	}

	@Override
	public void updateOBSRootId(ObjectId obs_id) {
		ServicesLoader.get(WorkService.class).updateWork(new FilterAndUpdate().filter(new BasicDBObject("_id", _id))
				.set(new BasicDBObject("obs_id", obs_id)).bson());
		this.obs_id = obs_id;
	}

	@Override
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Persistence
	private String checkoutBy;

	@Persistence
	private ObjectId space_id;

	public void setSpaceId(ObjectId space_id) {
		this.space_id = space_id;
	}

	@Override
	public Workspace getWorkspace() {
		return ServicesLoader.get(WorkService.class).getWorkspace(_id);
	}

	public Work setChargerId(String chargerId) {
		this.chargerId = chargerId;
		return this;
	}

	@Override
	public List<WorkLink> createGanttLinkDataSet() {
		return ServicesLoader.get(WorkService.class).createWorkLinkDataSet(_id);
	}

	@Override
	public List<Work> createGanttTaskDataSet() {
		return ServicesLoader.get(WorkService.class).createWorkTaskDataSet(_id);
	}

	@ReadValue("warningIcon")
	public String getWarningIcon() {
		String overdue = getOverdue();
		if ("超期".equals(overdue))
			return "<span class='layui-badge'>超期</span>";
		else if ("预警".equals(overdue))
			return "<span class='layui-badge layui-bg-orange'>预警</span>";
		else
			return null;
	}

	@ReadValue
	@SetValue
	private String overdue;

	@SetValue
	private Integer warningDay;

	public Date getPlanFinish() {
		return planFinish;
	}

	public Date getPlanStart() {
		return planStart;
	}

	@Behavior("指派")
	private boolean behaviourAssigner(@MethodParam(MethodParam.CURRENT_USER_ID) String userid) {
		return userid.equals(assignerId);
	}

	@Behavior("开始工作")
	private boolean behaviourStart() {
		return actualStart == null;
	}

	@Behavior("完成工作")
	private boolean behaviourFinish() {
		return actualStart != null;
	}

	@Behavior({ "打开工作包" })
	private boolean behaviourOpenWorkPackage() {
		return !summary && !stage && !milestone;
	}

	@Behavior({ "设置工作包" })
	private boolean behaviourSetWorkPackage() {
		return !summary && !stage && !milestone && actualStart == null;
	}

	@Behavior("创建工作包")
	private boolean behaviourCreateWorkPackage() {
		return actualFinish == null;
	}

	@Structure({ "我的待处理工作（工作抽屉）/list" })
	private List<WorkBoardInfo> getWorkBoardInfo() {
		return Arrays.asList(new WorkBoardInfo().setWork(this));
	}

	@Structure({ "我的待处理工作（工作抽屉）/count" })
	private long countWorkBoardInfo() {
		return 1;
	}

	public Date getActualFinish() {
		return actualFinish;
	}

	public Date getActualStart() {
		return actualStart;
	}

	public String getAssignerId() {
		return assignerId;
	}

	public String getOverdue() {
		if (!summary && overdue == null) {
			Date actualFinish = getActualFinish();
			Date planFinish = getPlanFinish();
			Calendar cal = Calendar.getInstance();
			Date now = cal.getTime();
			if (actualFinish != null) {
				if (actualFinish.after(planFinish)) {
					overdue = "超期";
				} else {
					overdue = "";
				}
			} else {
				cal.setTime(planFinish);
				cal.add(Calendar.DAY_OF_MONTH, (warningDay == null ? 0 : (-1 * warningDay)));
				Date warning = cal.getTime();
				if (now.after(planFinish)) {
					overdue = "超期";
				} else if (now.after(warning)) {
					overdue = "预警";
				} else {
					overdue = "";
				}
			}
		}
		return overdue;
	}

	@SetValue
	private Estimate scheduleEst;

	@ReadValue("estOverdual")
	private Integer getEstimateOverdue() {
		return scheduleEst == null ? null : scheduleEst.overdue;
	}

	@ReadValue("estFinish")
	private Date getEstimateFinish() {
		return scheduleEst == null ? null : scheduleEst.finish;
	}

	@ReadValue("estDate")
	private Date getEstimateDate() {
		return scheduleEst == null ? null : scheduleEst.date;
	}

	@ReadValue("estDuration")
	private Integer getEstimateDuration() {
		return scheduleEst == null ? null : scheduleEst.duration;
	}

	@ReadValue("TF")
	@SetValue
	private Double getTF() {
		return summary || scheduleEst == null ? null : scheduleEst.tf;
	}

	@ReadValue("FF")
	@SetValue
	private Double getFF() {
		return summary || scheduleEst == null ? null : scheduleEst.ff;
	}

	@SetValue
	@ReadValue
	private String packageName;

	@ReadValue({ "我的工作/workpackageHtml", "进度计划/workpackageHtml", "进度计划（查看）/workpackageHtml" })
	public String getWorkPackageActionHtml() {
		if (summary || milestone) {
			return "";
		}

		StringBuffer sb = new StringBuffer();
		List<TrackView> wps = getWorkPackageSetting();
		sb.append("<div style='display: inline-flex;" + "    justify-content: space-between;" + "    width: 100%;'>");
		if (Util.isEmptyOrNull(wps)) {
			sb.append("<a class='layui-btn layui-btn-xs layui-btn-primary' style='flex:auto;' href='"
					+ "openWorkPackage/default" + "' target='_rwt'>" + "工作包" + "</a>");
		} else if (wps.size() == 1) {
			sb.append("<a class='layui-btn layui-btn-xs layui-btn-primary' style='flex:auto;' href='"
					+ "openWorkPackage/0" + "' target='_rwt'>" + wps.get(0).getName() + "</a>");

		} else {
			for (int i = 0; i < wps.size(); i++) {
				sb.append("<a class='layui-btn layui-btn-xs layui-btn-primary' style='flex:auto;' href='"
						+ "openWorkPackage/" + i + "' target='_rwt'>" + wps.get(i).getName() + "</a>");
			}
		}
		sb.append("</div>");
		return sb.toString();
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 获得给定用户在阶段中的角色
	@RoleBased
	private List<String> getStageRole(@MethodParam(MethodParam.CURRENT_USER_ID) String userId) {
		return ServicesLoader.get(OBSService.class).getScopeRoleofUser(_id, userId);
	}

	@SetValue
	private Double aci;

	@ReadValue("进度计划和监控/ACI")
	public Double getACI() {
		if (summary || milestone) {
			return null;
		}
		return aci;
	}

	@SetValue
	private Double acp;

	@ReadValue("进度计划和监控/ACP")
	public Double getACP() {
		if (summary || milestone) {
			return null;
		}
		return acp;
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 基线记录原始工作编号
	@SetValue
	private ObjectId old_id;

	@SetValue
	private ObjectId baseline_id;

	public ObjectId getOld_id() {
		return old_id;
	}

	public ObjectId getBaseline_id() {
		return baseline_id;
	}

	@ReadValue("statusHtml")
	public String getStatusHtml() {
		if (ProjectStatus.Created.equals(status)) {
			return "<span class='layui-badge layui-bg-blue layui-btn-fluid'>" + status + "</span>";
		} else if (ProjectStatus.Processing.equals(status)) {
			return "<span class='layui-badge layui-bg-blue layui-btn-fluid'>" + status + "</span>";
		} else if (ProjectStatus.Closing.equals(status)) {
			return "<span class='layui-badge layui-bg-green layui-btn-fluid'>" + status + "</span>";
		} else if (ProjectStatus.Closed.equals(status)) {
			return "<span class='layui-badge layui-bg-green layui-btn-fluid'>" + status + "</span>";
		} else {
			return "";
		}
	}

	public String getImageURL() {
		if (getActualFinish() != null) {
			return "/img/task_finished.svg";
		} else if (getActualStart() != null) {
			return "/img/task_wip.svg";
		} else {
			return "/img/task.svg";
		}
	}

}
