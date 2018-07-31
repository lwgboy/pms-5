package com.bizvisionsoft.service.model;

import java.util.Date;
import java.util.List;

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
import com.bizvisionsoft.annotations.md.service.Structure;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.service.ProjectTemplateService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.tools.Util;

@PersistenceCollection("workInTemplate")
@Strict
public class WorkInTemplate implements IWorkPackageMaster {

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// id, 在gantt图中 使用String 类型传递，因此 ReadValue和WriteValue需要用方法重写
	@Persistence
	private ObjectId _id;

	@ReadValue("id")
	public String getId() {
		return _id == null ? null : _id.toHexString();
	}

	@WriteValue("id")
	public WorkInTemplate setId(String id) {
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

	@Persistence
	private ObjectId template_id;

	@ReadValue("project")
	public String getTemplateId() {
		return template_id == null ? null : template_id.toHexString();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// index, 在gantt图中用于排序
	@WriteValue
	@ReadValue
	@Persistence
	private Integer index;
	
	public void setIndex(int index) {
		this.index = index;
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// WBS代码
	@WriteValue
	@ReadValue({"项目模板WBS（分配角色）/wbsCode","项目模板WBS/wbsCode"})
	@Persistence
	private String wbsCode;

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
	@ReadValue
	@WriteValue
	@Persistence("name")
	@Label(Label.NAME_LABEL)
	private String text;
	
	public String getText() {
		return text;
	}
	
	public WorkInTemplate setText(String text) {
		this.text = text;
		return this;
	}
	
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

	public WorkInTemplate setFullName(String fullName) {
		this.fullName = fullName;
		return this;
	}

	
	////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * 计划开始日期, 编辑器保存时需要校验
	 */
	@ReadValue("start_date")
	@Persistence
	@WriteValue({ "项目模板工作编辑器/start_date", "项目模板里程碑工作编辑器/start_date" })
	private Date planStart;

	@WriteValue("项目模板甘特图/start_date")
	public boolean setStart_date(String start_date) {
		Date newDate = Util.str_date(start_date);
		if (!Util.equals(newDate, this.planStart)) {
			planStart = newDate;
			return true;
		}
		return false;
	}

	public Date getPlanStart() {
		return planStart;
	}

	public void setPlanStart(Date planStart) {
		this.planStart = planStart;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// 计划完成日期, 编辑器保存时需要校验
	@ReadValue("end_date")
	@Persistence
	@WriteValue("项目模板工作编辑器/end_date")
	private Date planFinish;

	@WriteValue("项目模板甘特图/end_date")
	public boolean setEnd_date(String end_date) {
		Date newDate = Util.str_date(end_date);
		if (!Util.equals(newDate, this.planFinish)) {
			planFinish = newDate;
			return true;
		}
		return false;
	}

	@Override
	public Date getPlanFinish() {
		return planFinish;
	}

	public void setPlanFinish(Date planFinish) {
		this.planFinish = planFinish;
	}

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

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// 工时, 需要保存
	@SetValue
	@ReadValue
	private double planWorks;

	@GetValue("planWorks")
	public double getPlanWorks() {
		return planWorks;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// 如果是里程碑，gantt的type为milestone，否则为task。
	// 如果在gantt中更新了task,使得他有子工作，gantt将type改为project
	@ReadValue
	@WriteValue
	@Persistence
	private boolean milestone;

	@Persistence
	private boolean summary;

	@Persistence
	private boolean stage;

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
	
	public boolean isMilestone() {
		return milestone;
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// 存储在数据库中的是管理级别。表现在Gantt中的是barstyle,样式
	@ReadValue
	@WriteValue
	private String barstyle;

	@ReadValue("manageLevel")
	@GetValue("manageLevel")
	private String getManageLevel() {
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

	@SetValue("manageLevel")
	public WorkInTemplate setManageLevel(String level) {
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

	@ReadValue("open")
	private boolean ganttOpen = true;
	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// 工作的标签文本
	@Label
	public String toString() {
		return text;
	}

	@ReadValue
	@WriteValue
	@Persistence
	private List<TrackView> workPackageSetting;

	public List<TrackView> getWorkPackageSetting() {
		return workPackageSetting;
	}

	@Persistence
	@ReadValue
	@WriteValue
	private List<String> certificates;

	@ReadValue({ "项目模板WBS/wpsText", "项目模板WBS（分配角色）/wpsText" })
	private String getWorkPackageSettingText() {
		if (Util.isEmptyOrNull(workPackageSetting)) {
			return "";
		} else {
			StringBuffer sb = new StringBuffer();
			workPackageSetting.forEach(tv -> sb.append(tv.getCatagory() + ":" + tv.getName() + " "));
			return sb.toString().trim();
		}
	}

	@ReadValue({ "项目模板WBS/manageLevelHtml", "项目模板WBS（分配角色）/manageLevelHtml" })
	private String getManageLevelHtml() {
		if ("level1_task".equals(barstyle)) {
			return "<span class='layui-badge level1_task'>1</span>";
		} else if ("level2_task".equals(barstyle)) {
			return "<span class='layui-badge level2_task'>2</span>";
		} else if ("level3_task".equals(barstyle)) {
			return "<span class='layui-badge layui-bg-green'>3</span>";
		} else {
			return "";
		}
	}

	@ImageURL({ "项目模板WBS/milestoneIcon", "项目模板WBS（分配角色）/milestoneIcon" })
	private String getMilestoneIcon() {
		if (milestone)
			return "/img/flag_blue.svg";
		return null;
	}

	@Structure({ "项目模板WBS/list", "项目模板WBS（分配角色）/list" })
	private List<WorkInTemplate> listChildren() {
		return ServicesLoader.get(ProjectTemplateService.class).listWBSChildren(_id);
	}

	@Structure({ "项目模板WBS/count", "项目模板WBS（分配角色）/count" })
	private long countChildren() {
		return ServicesLoader.get(ProjectTemplateService.class).countWBSChildren(_id);
	}

	@Behavior("设定工作包类型")
	private boolean behaviourEditWPS() {
		return !summary && !stage && !milestone;
	}
	
	@Behavior("角色")
	private boolean behaviourEditRole() {
		return !milestone;
	}

	@Behavior("工作包")
	private boolean behaviourOpenWorkpackagePlan() {
		return !Util.isEmptyOrNull(workPackageSetting);
	}

	@ReadValue
	@SetValue
	private OBSInTemplate chargerRole;

	@ReadValue
	@WriteValue
	@Persistence
	private String chargerRoleId;

	@WriteValue("chargerRole")
	public void setChargerRole(OBSInTemplate obsItem) {
		chargerRole = obsItem;
		if (obsItem == null) {
			chargerRoleId = null;
		} else {
			chargerRoleId = obsItem.getId();
		}
	}

	@ReadValue
	@SetValue
	private OBSInTemplate assignerRole;

	@ReadValue
	@WriteValue
	@Persistence
	private String assignerRoleId;

	@WriteValue("assignerRole")
	public void setAssignerRole(OBSInTemplate obsItem) {
		assignerRole = obsItem;
		if (obsItem == null) {
			assignerRoleId = null;
		} else {
			assignerRoleId = obsItem.getId();
		}
	}

	public ObjectId get_id() {
		return _id;
	}

	public WorkInTemplate setTemplate_id(ObjectId template_id) {
		this.template_id = template_id;
		return this;
	}

	public ObjectId getParent_id() {
		return parent_id;
	}

	public WorkInTemplate set_id(ObjectId _id) {
		this._id = _id;
		return this;
	}

	public WorkInTemplate setParent_id(ObjectId parent_id) {
		this.parent_id = parent_id;
		return this;
	}

	public static WorkInTemplate newInstance(ProjectTemplate template) {
		return new WorkInTemplate().set_id(new ObjectId()).setTemplate_id(template.get_id());
	}

	public static WorkInTemplate newInstance(WBSModule template) {
		return new WorkInTemplate().set_id(new ObjectId()).setTemplate_id(template.get_id());
	}
	
	public static WorkInTemplate newInstance(WorkInTemplate parentTask) {
		return new WorkInTemplate().set_id(new ObjectId()).setTemplate_id(parentTask.template_id)
				.setParent_id(parentTask._id);
	}

	public void setWorkPackageSetting(List<TrackView> workPackageSetting) {
		this.workPackageSetting = workPackageSetting;
	}

	public boolean isSummary() {
		return summary;
	}

	@Override
	public boolean isTemplate() {
		return true;
	}

	public WorkInTemplate setMilestone(boolean milestone) {
		this.milestone = milestone;
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_id == null) ? 0 : _id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WorkInTemplate other = (WorkInTemplate) obj;
		if (_id == null) {
			if (other._id != null)
				return false;
		} else if (!_id.equals(other._id))
			return false;
		return true;
	}

}
