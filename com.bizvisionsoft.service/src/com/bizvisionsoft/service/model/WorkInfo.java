package com.bizvisionsoft.service.model;

import java.util.Date;
import java.util.Optional;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.GetValue;
import com.bizvisionsoft.annotations.md.mongocodex.Persistence;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.mongocodex.SetValue;
import com.bizvisionsoft.annotations.md.mongocodex.Strict;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.tools.Util;
import com.mongodb.BasicDBObject;

@PersistenceCollection("workspace")
@Strict
public class WorkInfo implements IWBSScope {

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
	@ReadValue({ "start_date", "planStart" })
	@Persistence("planStart")
	private Date start_date;

	@WriteValue({ "甘特图总成工作编辑器/start_date", "甘特图工作编辑器/start_date", "甘特图阶段工作编辑器/start_date" })
	public WorkInfo setStart_date(Date start_date) {
		checkDate(start_date, this.end_date, this.deadline);
		this.start_date = start_date;
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
	@WriteValue({ "项目甘特图/start_date", "项目甘特图(编辑)/start_date" })
	public boolean setStart_date(String start_date) {
		Date newDate = Util.str_date(start_date);
		if (!Util.equals(newDate, this.start_date)) {
			this.start_date = newDate;
			return true;
		} else {
			return false;
		}
	}

	public Date getStart_date() {
		return start_date;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// 计划完成日期, 编辑器保存时需要校验
	@ReadValue({ "end_date", "planFinish" })
	@Persistence("planFinish")
	private Date end_date;

	@WriteValue({ "甘特图总成工作编辑器/end_date", "甘特图工作编辑器/end_date", "甘特图阶段工作编辑器/end_date" })
	public WorkInfo setEnd_date(Date end_date) {
		checkDate(this.start_date, end_date, this.deadline);
		this.end_date = end_date;
		return this;
	}

	@WriteValue({ "项目甘特图/end_date", "项目甘特图(编辑)/end_date" })
	public boolean setEnd_date(String end_date) {
		Date newDate = Util.str_date(end_date);
		if (!Util.equals(newDate, this.end_date)) {
			this.end_date = newDate;
			return true;
		} else {
			return false;
		}
	}

	public Date getEnd_date() {
		return end_date;
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// 期限, 编辑器保存时需要校验
	@ReadValue
	@Persistence
	private Date deadline;

	@WriteValue("deadline")
	public void setDeadline(Date deadline) {
		checkDate(this.start_date, this.end_date, deadline);
		this.deadline = deadline;
	}

	@WriteValue({ "项目甘特图/deadline", "项目甘特图(编辑)/deadline" })
	public boolean setDeadline(String deadline) {
		Date newDate = Util.str_date(deadline);
		if (!Util.equals(newDate, this.deadline)) {
			this.deadline = newDate;
			return true;
		} else {
			return false;
		}
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// 工期, 需要保存，但无需传递到gantt和编辑器
	@GetValue("planDuration")
	public int getDuration() {
		if (end_date != null && start_date != null) {
			return (int) ((end_date.getTime() - start_date.getTime()) / (1000 * 3600 * 24));
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
	@Persistence
	private ObjectId cbs_id;

	@Persistence
	private ObjectId obs_id;
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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

	public int index() {
		return index;
	}

	public static WorkInfo newInstance(ObjectId project_id) {
		return newInstance(project_id, null);
	}

	/**
	 * 生成本层的顺序号
	 * 
	 * @param projectId
	 * @param parentId
	 * @return
	 */
	private WorkInfo generateIndex() {
		index = ServicesLoader.get(WorkService.class)
				.nextWBSIndex(new BasicDBObject("project_id", project_id).append("parent_id", parent_id));
		return this;
	}

	public static WorkInfo newInstance(ObjectId project_id, ObjectId parent_id) {
		return new WorkInfo().set_id(new ObjectId()).setProject_id(project_id).setParent_id(parent_id).generateIndex();
	}

	private void checkDate(Date start_date, Date end_date, Date deadline) {
		if (start_date != null && end_date != null && start_date.after(end_date)) {
			throw new RuntimeException("开始日期不得晚于完成日期");
		}

		if (start_date != null && deadline != null && start_date.after(deadline)) {
			throw new RuntimeException("如果设定了期限，开始日期不得晚于期限日期");
		}

		if (end_date != null && deadline != null && end_date.after(deadline)) {
			throw new RuntimeException("如果设定了期限，完成日期不得晚于期限日期");
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

	@Override
	public ObjectId getScope_id() {
		return _id;
	}

	@Override
	public ObjectId getWBS_id() {
		return _id;
	}

	@Persistence
	private String checkOutBy;

	@Override
	public String getCheckOutUserId() {
		return checkOutBy;
	}

	@Persistence
	private ObjectId space_id;

	@Override
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
	

	@Persistence
	private boolean summary;

	public boolean isSummary() {
		return summary;
	}

}
