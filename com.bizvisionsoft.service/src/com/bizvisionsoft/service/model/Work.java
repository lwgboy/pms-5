package com.bizvisionsoft.service.model;

import java.util.Arrays;
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
import com.bizvisionsoft.annotations.md.service.ServiceParam;
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
public class Work implements ICBSScope, IOBSScope, IWBSScope {

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
	@ReadValue
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
		return projectName + "[" + projectNumber + "]";
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
	@ReadValue
	@WriteValue
	@Persistence("name")
	@Label(Label.NAME_LABEL)
	private String text;

	@ReadValue("name")
	private String getWorkNameHTML() {
		if (stage) {
			String html = "<div style='display:inline-flex;justify-content:space-between;width:100%;padding-right:8px;'><div style='font-weight:bold;'>"
					+ text + "</div>";
			html += "<a href='openStage/' target='_rwt'><img src='rwt-resources/extres/img/open_c.svg' style='cursor:pointer;' width='20px' height='20px'/></a></div>";
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

	@SetValue
	@ReadValue
	private int actualDuration;

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
	// 工时, 需要保存
	@SetValue
	@ReadValue
	private double planWorks;

	@GetValue("planWorks")
	public double getPlanWorks() {
		return planWorks;
	}

	@SetValue
	@ReadValue
	private double actualWorks;

	@GetValue("actualWorks")
	public double getActualWorks() {
		return actualWorks;
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// 完成百分比
	@ReadValue
	@WriteValue
	@Persistence
	private double progress;

	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// 工期完成率 百分比
	@ReadValue("dar")
	public Double getDAR() {
		if (planDuration != 0) {
			return 1d * actualDuration / planDuration;
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// 工作量完成率 百分比
	@ReadValue("war")
	public Double getWAR() {
		if (planWorks != 0) {
			return 1d * actualWorks / planWorks;
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// 工时完成率 百分比
	@ReadValue("sar")
	public Double getSAR() {
		if (planDuration != 0) {
			return 1d * actualDuration / planDuration;
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

	@ReadValue("manageLevelHtml")
	private String getManageLevelHtml() {
		if ("level1_task".equals(barstyle)) {
			return "<span class='layui-badge level1_task'>1</span>";
		} else if ("level2_task".equals(barstyle)) {
			return "<span class='layui-badge level2_task'>2</span>";
		} else if ("level3_task".equals(barstyle)) {
			return "<span class='layui-badge level3_task'>3</span>";
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

	@WriteValue("charger")
	private void setCharger(User charger) {
		this.chargerId = Optional.ofNullable(charger).map(o -> o.getUserId()).orElse(null);
	}

	@ReadValue("charger")
	private User getCharger() {
		return Optional.ofNullable(chargerId).map(id -> ServicesLoader.get(UserService.class).get(id)).orElse(null);
	}

	@ReadValue("chargerInfoWithDistributeIcon")
	private String getChargerInfoWithIcon() {
		return "<div style='display:inline-flex;width: 100%;justify-content: space-between;'>" + chargerInfo
				+ getDistributedIcon() + "</div>";
	}

	public String getChargerInfo() {
		return chargerInfo;
	}

	@ReadValue("部门工作日程表/section_id")
	private String getSectionId() {
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

	@Structure("进度计划和监控/list")
	private List<Work> listChildren() {
		return ServicesLoader.get(WorkService.class).listChildren(_id);
	}

	@Structure("进度计划和监控/count")
	private long countChildren() {
		return ServicesLoader.get(WorkService.class).countChildren(_id);
	}

	@Persistence
	private List<String> viewId;

	public List<String> getViewId() {
		return viewId;
	}

	@Persistence
	private Date startOn;

	@Persistence
	private String startBy;

	@Persistence
	private Date finishOn;

	@Persistence
	private String finishBy;

	public Date getStartOn() {
		return startOn;
	}

	public Date getFinishOn() {
		return finishOn;
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

	public Work setIndex(int index) {
		this.index = index;
		return this;
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
	private String getWarningIcon() {
		if ("已超期".equals(overdue))
			return "<span class='layui-badge'>超期</span>";
		else if ("预警".equals(overdue))
			return "<span class='layui-badge layui-bg-orange'>预警</span>";
		else
			return null;
	}

	@ReadValue
	@SetValue
	private String overdue;

	public Date getPlanFinish() {
		return planFinish;
	}

	public Date getPlanStart() {
		return planStart;
	}

	@Behavior("指派")
	private boolean behaviourAssigner(@ServiceParam(ServiceParam.CURRENT_USER_ID) String userid) {
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

	@Behavior("打开工作包")
	private boolean behaviourOpenWorkPackage() {
		return !summary && !stage;
	}

	@ReadValue
	@SetValue
	public TrackView scheduleMonitoring;

	@Structure("我的待处理工作（首页小组件）/list")
	private List<WorkBoardInfo> getWorkBoardInfo() {
		return Arrays.asList(new WorkBoardInfo().setWork(this));
	}

	@Structure("我的待处理工作（首页小组件）/count")
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

}
