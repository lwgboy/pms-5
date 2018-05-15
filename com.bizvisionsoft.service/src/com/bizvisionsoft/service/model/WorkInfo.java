package com.bizvisionsoft.service.model;

import java.util.Date;
import java.util.Optional;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Generator;
import com.bizvisionsoft.annotations.md.mongocodex.GetValue;
import com.bizvisionsoft.annotations.md.mongocodex.Persistence;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.mongocodex.SetValue;
import com.bizvisionsoft.annotations.md.mongocodex.Strict;
import com.bizvisionsoft.annotations.md.service.Behavior;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.UserService;
import com.bizvisionsoft.service.WorkSpaceService;
import com.bizvisionsoft.service.sn.WorkGenerator;
import com.bizvisionsoft.service.tools.Util;
import com.mongodb.BasicDBObject;

//implements IWBSScope 
@PersistenceCollection("workspace")
@Strict
public class WorkInfo {

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// id, 在gantt图中 使用String 类型传递，因此 ReadValue和WriteValue需要用方法重写
	@Persistence
	private ObjectId _id;

	@ReadValue("id")
	public String getId() {
		return _id == null ? null : _id.toHexString();
	}

	@WriteValue("id")
	public WorkInfo setId(String id) {
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

	@SetValue
	private String projectName;

	public WorkInfo setProjectName(String projectName) {
		this.projectName = projectName;
		return this;
	}

	@SetValue
	private String projectNumber;

	public WorkInfo setProjectNumber(String projectNumber) {
		this.projectNumber = projectNumber;
		return this;
	}

	public String getProjectNumber() {
		return projectNumber;
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
	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// index, 在gantt图中用于排序
	@ReadValue
	@WriteValue
	@Persistence
	private int index;

	public int index() {
		return index;
	}

	/**
	 * 生成本层的顺序号
	 * 
	 * @param projectId
	 * @param parentId
	 * @return
	 */
	private WorkInfo generateIndex() {
		index = ServicesLoader.get(WorkSpaceService.class)
				.nextWBSIndex(new BasicDBObject("project_id", project_id).append("parent_id", parent_id));
		return this;
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// WBS代码 TODO 自动生成的方法
	@WriteValue
	@ReadValue
	@Persistence
	private String wbsCode;

	private WorkInfo setWBSCode(String parentWBSCode) {
		if (parentWBSCode != null) {
			this.wbsCode = parentWBSCode + "." + index;
		} else {
			this.wbsCode = "" + index;
		}

		return this;
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// WBS代码 TODO 自动生成的方法
	@WriteValue
	@Persistence
	@Generator(name = Generator.DEFAULT_NAME, key = "work", generator = WorkGenerator.class, callback = Generator.NONE_CALLBACK)
	private String code;

	@ReadValue
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
	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// fullName, 在gantt图，编辑器中，数据库中均使用
	@WriteValue
	@SetValue
	private String fullName;

	@ReadValue("fullName")
	@GetValue("fullName")
	private String getFullName() {
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

	@WriteValue({ "甘特图总成工作编辑器/start_date", "甘特图工作编辑器/start_date", "甘特图阶段工作编辑器/start_date" })
	public WorkInfo setStart_date(Date start_date) {
		checkDate(start_date, this.getEnd_date());
		if (actualStart != null) {
			actualStart = start_date;
		} else {
			planStart = start_date;
		}
		return this;
	}

	@WriteValue("项目甘特图（编辑）/start_date")
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

	@ReadValue({ "start_date" })
	public Date getStart_date() {
		if (actualStart != null) {
			return actualStart;
		}
		return planStart;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// 计划完成日期, 编辑器保存时需要校验
	@ReadValue("planFinish")
	@Persistence("planFinish")
	private Date planFinish;

	@ReadValue("actualFinish")
	@Persistence("actualFinish")
	private Date actualFinish;

	@WriteValue({ "甘特图总成工作编辑器/end_date", "甘特图工作编辑器/end_date", "甘特图阶段工作编辑器/end_date" })
	public WorkInfo setEnd_date(Date end_date) {
		checkDate(getStart_date(), end_date);
		if (actualFinish != null) {
			actualFinish = end_date;
		} else {
			planFinish = end_date;
		}
		return this;
	}

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
	@WriteValue("项目甘特图（编辑）/end_date")
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

	@ReadValue("end_date")
	public Date getEnd_date() {
		if (actualFinish != null) {
			return actualFinish;
		}
		return planFinish;
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// 工期, 需要保存，但无需传递到gantt和编辑器
	@GetValue("planDuration")
	public int getPlanDuration() {
		if (planFinish != null && planStart != null) {
			return (int) ((planFinish.getTime() - planStart.getTime()) / (1000 * 3600 * 24));
		} else {
			return 0;
		}
	}

	@GetValue("actualDuration")
	public int getActualDuration() {
		if (actualFinish != null && actualStart != null) {
			return (int) ((actualFinish.getTime() - actualStart.getTime()) / (1000 * 3600 * 24));
		} else {
			return 0;
		}
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// 存储在数据库中的是管理级别。表现在Gantt中的是barstyle,样式
	@ReadValue
	@WriteValue
	private String barstyle;

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
	public WorkInfo setManageLevel(String level) {
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

	@WriteValue("charger")
	private void setCharger(User charger) {
		this.chargerId = Optional.ofNullable(charger).map(o -> o.getUserId()).orElse(null);
	}

	@ReadValue("charger")
	private User getCharger() {
		return Optional.ofNullable(chargerId).map(id -> ServicesLoader.get(UserService.class).get(id)).orElse(null);
	}
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@Persistence
	private ObjectId cbs_id;

	@Persistence
	private ObjectId obs_id;
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * 用于比较的计划完成时间
	 */
	@SetValue
	private Date planFinish1;

	/**
	 * 用于比较的实际完成时间
	 */
	@SetValue
	private Date actualFinish1;

	@ReadValue("end_date1")
	public Date getEnd_date1() {
		if (actualFinish1 != null) {
			return actualFinish1;
		}
		return planFinish1;
	}

	/**
	 * 用于比较的计划开始时间
	 */
	@SetValue
	private Date planStart1;

	/**
	 * 用于比较的实际开始时间
	 */
	@SetValue
	private Date actualStart1;

	@ReadValue("start_date1")
	public Date getStart_date1() {
		if (actualStart1 != null) {
			return actualStart1;
		}
		return planStart1;
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////

	public WorkInfo set_id(ObjectId _id) {
		this._id = _id;
		return this;
	}

	public ObjectId get_id() {
		return _id;
	}

	public WorkInfo setProject_id(ObjectId project_id) {
		this.project_id = project_id;
		return this;
	}

	public ObjectId getProject_id() {
		return project_id;
	}

	public WorkInfo setParent_id(ObjectId parent_id) {
		this.parent_id = parent_id;
		return this;
	}

	public ObjectId getParent_id() {
		return parent_id;
	}

	public static WorkInfo newInstance(Project project) {
		return new WorkInfo().set_id(new ObjectId()).setProject_id(project.get_id())
				.setProjectName(project.getProjectName()).setProjectNumber(project.getProjectNumber()).generateIndex()
				.setWBSCode(null);
	}

	public static WorkInfo newInstance(Work work) {
		return new WorkInfo().set_id(new ObjectId()).setProject_id(work.getProject_id()).setParent_id(work.get_id())
				.setProjectName(work.getProjectName()).setProjectNumber(work.getProjectNumber()).generateIndex()
				.setWBSCode(work.getWBSCode());
	}

	public static WorkInfo newInstance(WorkInfo workinfo) {
		return new WorkInfo().set_id(new ObjectId()).setProject_id(workinfo.getProject_id())
				.setParent_id(workinfo.get_id()).setProjectName(workinfo.projectName)
				.setProjectNumber(workinfo.projectNumber).generateIndex().setWBSCode(workinfo.wbsCode);
	}

	private void checkDate(Date start_date, Date end_date) {
		if (start_date != null && end_date != null && start_date.after(end_date)) {
			throw new RuntimeException("开始日期不得晚于完成日期");
		}
	}

	public String getText() {
		return text;
	}

	public WorkInfo setText(String text) {
		this.text = text;
		return this;
	}

	public Project getProject() {
		return Optional.ofNullable(project_id).map(_id -> ServicesLoader.get(ProjectService.class).get(_id))
				.orElse(null);
	}

	@Persistence
	private ObjectId space_id;

	public ObjectId getSpaceId() {
		return space_id;
	}

	public void setSpaceId(ObjectId space_id) {
		this.space_id = space_id;
	}

	@Persistence
	private boolean stage;

	public WorkInfo setStage(boolean stage) {
		this.stage = stage;
		return this;
	}

	public boolean isStage() {
		return stage;
	}

	@Persistence
	@ReadValue
	@WriteValue
	private String status;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// 如果是里程碑，gantt的type为milestone，否则为task。
	// 如果在gantt中更新了task,使得他有子工作，gantt将type改为project
	@Persistence
	private boolean milestone;

	@Persistence
	private boolean summary;

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

	public boolean isSummary() {
		return summary;
	}

	public boolean isMilestone() {
		return milestone;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// 完成百分比
	@ReadValue
	@WriteValue
	@Persistence
	private Float progress;
	////////////////////////////////////////////////////////////////////////////////////////////////////

	@Behavior({ "项目甘特图（编辑）/创建子任务" })
	private boolean behaviourAddTask() {
		return actualFinish == null;
	}

	@Behavior({ "项目甘特图（编辑）/删除任务" })
	private boolean behaviourDeleteTask() {
		return actualStart == null;
	}

}
