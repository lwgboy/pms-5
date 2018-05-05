package com.bizvisionsoft.service.model;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.GetValue;
import com.bizvisionsoft.annotations.md.mongocodex.Persistence;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.mongocodex.SetValue;
import com.bizvisionsoft.annotations.md.mongocodex.Strict;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.Structure;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.UserService;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.tools.Util;
import com.mongodb.BasicDBObject;

/**
 * <div class="doc" id="doc_content">
 * 
 * <a name="specifyingdataproperties">
 * <h2>Specifying Data Properties</h2></a>
 * 
 * <p>
 * A data source for the Gantt chart is an object that stores 2 types of
 * information:
 * </p>
 * 
 * <ul>
 * <li><strong>tasks</strong> - the items of tasks.</li>
 * <li><strong>links</strong> - the items of dependency links.</li>
 * </ul>
 * 
 * <h3 id="task_properties">Properties of a task object</h3>
 * 
 * <ul>
 * <li><b><i>Mandatory properties</i></b> - these properties will always be
 * defined on the client, they must be provided by the datasource in order for
 * gantt to operate correctly.</li>
 * <ul>
 * <li><b>text</b> - (<i> string </i>) the task text.</li>
 * <li><b>start_date</b> - (<i> Date|string </i>) the date when a task is
 * scheduled to begin. Must match
 * <a href="api__gantt_xml_date_config.html">xml_date</a> format if provided as
 * a string.</li>
 * <li><b>duration</b> - (<i> number </i>) the task duration.
 * <a href="desktop__loading.html#loadingtaskdates">Can be replaced with the
 * 'end_date' property</a>.</li>
 * <li><b>id</b> - (<i> string|number </i>) the task id.</li>
 * </ul>
 * <li><b><i>Optional properties</i></b> - these properties may or may not be
 * defined. The default logic and templates of gantt will use these properties
 * if they are defined.</li>
 * <ul>
 * <li><b>type</b> - (<i>string</i>) the task type. The available values are
 * stored in the <a href="api__gantt_types_config.html">types</a> object:</li>
 * <ul>
 * <li><a href="desktop__task_types.html#regulartasks">"task"</a> - a regular
 * task (<i>default value</i>).</li>
 * <li><a href="desktop__task_types.html#projecttasks">"project"</a> - a task
 * that starts, when its earliest child task starts, and ends, when its latest
 * child ends. <i>The <b>start_date</b>, <b>end_date</b>, <b>duration</b>
 * properties are ignored for such tasks.</i></li>
 * <li><a href="desktop__task_types.html#milestones">"milestone"</a> - a
 * zero-duration task that is used to mark out important dates of the project.
 * <i>The <b>duration</b>, <b>progress</b>, <b>end_date</b> properties are
 * ignored for such tasks. </i></li>
 * </ul>
 * <li><b>parent</b> - (<i> string|number </i>) the id of the parent task. The
 * id of the root task is specified by the
 * <a href="api__gantt_root_id_config.html">root_id</a> config.</li>
 * <li><b>progress</b> - (<i> number from 0 to 1 </i>) the task progress.</li>
 * <li><b>open</b> - (<i> boolean </i>) specifies whether the task branch will
 * be opened initially (to show child tasks).</li>
 * <li><b>end_date</b> - (<i> Date|string </i>) the date when a task is
 * scheduled to be completed. Used as an alternative to the <i>duration</i>
 * property for setting the duration of a task. Must match
 * <a href="api__gantt_xml_date_config.html">xml_date</a> format if provided as
 * a string.</li>
 * <li><b>readonly</b>-(<i>boolean</i>) optional, can mark task as <a href=
 * "desktop__readonly_mode.html#readonlymodeforspecifictaskslinks">readonly</a>.
 * </li>
 * <li><b>editable</b>-(<i>boolean</i>) optional, can mark task as <a href=
 * "desktop__readonly_mode.html#readonlymodeforspecifictaskslinks">editable</a>.
 * </li>
 * </ul>
 * <li><b><i>Dynamic properties</i></b> - are created on the client and
 * represent the current state of a task or a link. They shouldn't be saved to
 * the database, gantt will ignore these properties if they are specified in
 * your JSON/XML.</li>
 * <ul>
 * <li><b>$source</b> - (<i> array </i>) ids of links that come out of the
 * task.</li>
 * <li><b>$target</b> - (<i> array </i>) ids of links that come into task.</li>
 * <li><b>$level</b> - (<i> number </i>) the task's level in the tasks hierarchy
 * (zero-based numbering).</li>
 * <li><b>$open</b> - (<i> boolean </i>) specifies whether the task is currently
 * opened.</li>
 * <li><b>$index</b> - (<i> number </i>) the number of the task row in the
 * gantt.</li>
 * </ul>
 * </ul>
 * 
 * <p>
 * The default date format for JSON and XML data is <strong>"%d-%m-%Y
 * %H:%i"</strong> (see the <a href="desktop__date_format.html"> date format
 * specification</a>).<br>
 * To change it, use the <a href="api__gantt_xml_date_config.html">xml_date</a>
 * configuration option.
 * </p>
 * 
 * <pre>
 * <code><pre class="js">gantt.<span class="me1">config</span>.<span class=
 * "me1">xml_date</span><span class="sy0">=</span><span class=
 * "st0">"%Y-%m-%d"</span><span class="sy0">;</span>
gantt.<span class="me1">init</span><span class="br0">(</span><span class=
"st0">"gantt_here"</span><span class="br0">)</span><span class=
"sy0">;</span></pre></code>
 * </pre>
 * 
 * <p>
 * Once loaded into Gantt, the <strong>start_date</strong> and
 * <strong>end_date</strong> properties will be parsed into the Date type.
 * </p>
 * 
 * <p>
 * Date formats that are not supported by the
 * <a href="api__gantt_xml_date_config.html">xml_date</a> config can be parsed
 * manually via the <a href="api__gantt_xml_date_template.html">xml_date</a>
 * template.
 * </p>
 * 
 * @author hua
 *
 */
@PersistenceCollection("work")
@Strict
public class WorkInfo implements ICBSScope, IOBSScope {

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
	// WBS代码 TODO 自动生成的方法
	@WriteValue
	@ReadValue
	@Persistence
	private String wbsCode;
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

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// 计划开始日期, 编辑器保存时需要校验
	@ReadValue({"start_date","planStart"})
	@Persistence("planStart")
	private Date start_date;

	@WriteValue({ "甘特图总成工作编辑器/start_date", "甘特图工作编辑器/start_date", "甘特图阶段工作编辑器/end_date" })
	public void setStart_date(Date start_date) {
		checkDate(start_date, this.end_date, this.deadline);
		this.start_date = start_date;
	}

	@WriteValue("项目甘特图/start_date")
	public boolean setStart_date(String start_date) {
		Date newDate = Util.str_date(start_date);
		if (!Util.equals(newDate, this.start_date)) {
			this.start_date = newDate;
			return true;
		} else {
			return false;
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// 计划完成日期, 编辑器保存时需要校验
	@ReadValue({"end_date","planFinish"})
	@Persistence("planFinish")
	private Date end_date;

	@WriteValue({ "甘特图总成工作编辑器/end_date", "甘特图工作编辑器/end_date", "甘特图阶段工作编辑器/end_date" })
	public void setEnd_date(Date end_date) {
		checkDate(this.start_date, end_date, this.deadline);
		this.end_date = end_date;
	}

	@WriteValue("项目甘特图/end_date")
	public boolean setEnd_date(String end_date) {
		Date newDate = Util.str_date(end_date);
		if (!Util.equals(newDate, this.end_date)) {
			this.end_date = newDate;
			return true;
		} else {
			return false;
		}
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

	@WriteValue("项目甘特图/deadline")
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
			return (int) (end_date.getTime() - start_date.getTime()) / (1000 * 3600 * 24);
		} else {
			return 0;
		}
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// 完成百分比
	@ReadValue
	@WriteValue
	@Persistence
	private Float progress;
	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// 如果是里程碑，gantt的type为milestone，否则为task。
	// 如果在gantt中更新了task,使得他有子工作，gantt将type改为project
	@Persistence
	private boolean milestone;

	@Persistence
	private boolean summary;

	@Persistence
	private boolean stage;
	
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

	@Structure("项目进度计划表/list")
	private List<WorkInfo> listChildren(){
		return ServicesLoader.get(WorkService.class).listChildren(_id);
	}

	@Structure("项目进度计划表/count")
	private long countChildren(){
		return ServicesLoader.get(WorkService.class).countChildren(_id);
	}

	
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

	public boolean isSummary() {
		return summary;
	}

	public boolean isMilestone() {
		return milestone;
	}

	public boolean isStage() {
		return stage;
	}

	public WorkInfo setStage(boolean stage) {
		this.stage = stage;
		return this;
	}

	public String getText() {
		return text;
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

	
	public String getStatus() {
		return status;
	}
}
