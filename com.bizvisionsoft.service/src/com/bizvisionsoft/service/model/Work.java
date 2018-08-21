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
	 * ������Ŀ�ƻ��Ƿ�����´������Ŀ״̬�ж�
	 * 
	 * @return
	 */
	@Behavior("�´�ƻ�")
	private boolean enableDistribute() {
		return ProjectStatus.Processing.equals(status);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// id, ��ganttͼ�� ʹ��String ���ʹ��ݣ���� ReadValue��WriteValue��Ҫ�÷�����д
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
	// parent_id, ��ganttͼ�� ʹ�õ��ֶ�Ϊparent, String ���ʹ��ݣ���� ReadValue��WriteValue��Ҫ�÷�����д
	// д��parentʱ��ע�⣬����ֵ��ʾ�˸�parentֵ�Ƿ񱻸��ġ����û�б仯������false����֪������
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
	// project_id, ��ganttͼ�� ʹ�õ��ֶ�Ϊproject, String ���ʹ��ݣ�
	// ��� ReadValue��WriteValue��Ҫ�÷�����д
	// ����ͼ�������ָGanttPart, ����Gantt��Ҫ������͹�����ϵ�������project���ԡ�
	// ��������и����ԣ���ʾ��Щ��������ǿͻ��˴�����
	// д��ʱ��ע�⣬����ֵ��ʾ�˸�parentֵ�Ƿ񱻸��ġ����û�б仯������false����֪������
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
	@ReadValue({ "projectName", "�ҵĹ����������ƣ�/details", "���Ź����ճ̱�/details" })
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
	// index, ��ganttͼ����������
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
	// WBS����
	@WriteValue
	@ReadValue
	@Persistence
	private String wbsCode;

	public String getWBSCode() {
		return wbsCode;
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// WBS����
	@Persistence
	private String code;

	@ReadValue("code")
	public String getCode() {
		return code;
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// text, ��ganttͼtext�ֶΣ����ݿ���Ϊname�ֶ�
	@ReadValue({ "���ȼƻ����鿴��/name", "���ȼƻ�/name", "text" })
	@WriteValue
	@Persistence("name")
	@Label(Label.NAME_LABEL)
	private String text;

	@ReadValue({ "���ȼƻ��ͼ�أ��鿴��/name", "���ȼƻ��ͼ��/name" })
	private String readWorkNameHTML() {
		if (stage) {
			String html = "<div style='display:inline-flex;justify-content:space-between;width:100%;padding-right:8px;'><div style='font-weight:bold;'>"
					+ text + "</div>";
			if (ProjectStatus.Created.equals(status))
				html += "<a class='layui-btn layui-btn-xs layui-btn-primary' style='display:block; width:50px;cursor: pointer;' href='"
						+ "start/" + "' target='_rwt'>" + "����" + "</a>";
			else if (ProjectStatus.Processing.equals(status))
				html += "<a class='layui-btn layui-btn-xs layui-btn-primary' style='display:block; width:50px;cursor: pointer;' href='"
						+ "finish/" + "' target='_rwt'>" + "��β" + "</a>";
			else if (ProjectStatus.Closing.equals(status))
				html += "<a class='layui-btn layui-btn-xs layui-btn-primary' style='display:block; width:50px;cursor: pointer;' href='"
						+ "close/" + "' target='_rwt'>" + "�ر�" + "</a>";
			html += "</div>";
			return html;
		} else {
			return "<div>" + text + "</div>";
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// fullName, ��ganttͼ���༭���У����ݿ��о�ʹ��
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
	 * �ƻ���ʼ����, �༭������ʱ��ҪУ��
	 */
	@ReadValue("planStart")
	@Persistence("planStart")
	private Date planStart;

	@ReadValue("actualStart")
	@Persistence("actualStart")
	private Date actualStart;

	@WriteValue("��Ŀ����ͼ/start_date")
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

	@ReadValue({ "��Ŀ����ͼ���ޱ��鿴��/start_date", "��Ŀ����ͼ���鿴��/start_date", "��Ŀ����ͼ����Դʵ�ʷ��䣩/start_date", "��Ŀ��չ����ͼ/start_date",
			"��Ŀ���߸���ͼ/start_date" })
	public Date getStart_date() {
		if (actualStart != null) {
			return actualStart;
		}
		return planStart;
	}

	@ReadValue({ "��Ŀ����ͼ/start_date", "��Ŀ����ͼ����Դ�ƻ����䣩/start_date", "�ҵĹ����������ƣ�/start_date", "���Ź����ճ̱�/start_date" })
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
	// �ƻ��������, �༭������ʱ��ҪУ��
	@ReadValue("planFinish")
	@Persistence("planFinish")
	private Date planFinish;

	@ReadValue("actualFinish")
	@Persistence("actualFinish")
	private Date actualFinish;

	/**
	 * <b>������Ganttͼ�ؼ���д�ķ��� ��������Ƚ����⣡����</b>
	 * <p>
	 * ע�ⷵ��true��false��Ŀ���Ǹ�֪�Ƿ�����˸��ֶε�ֵ
	 * 
	 * @param start_date
	 *            <p>
	 *            ���յ���JS�����������ַ�������ʽΪyyyy-MM-dd'T'HH:mm:ss.SSS UTC
	 *            <p>
	 *            ʹ��Util.str_date()��������ת��
	 * @return
	 */
	@WriteValue("��Ŀ����ͼ/end_date")
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

	@ReadValue({ "��Ŀ����ͼ���ޱ��鿴��/end_date", "��Ŀ����ͼ���鿴��/end_date", "��Ŀ����ͼ����Դʵ�ʷ��䣩/end_date", "��Ŀ��չ����ͼ/end_date",
			"��Ŀ���߸���ͼ/end_date", "���Ź����ճ̱�/end_date" })
	public Date getEnd_date() {
		if (actualFinish != null) {
			return actualFinish;
		} else if (actualStart != null) {
			return new Date(planFinish.getTime() - planStart.getTime() + actualStart.getTime());
		}
		return planFinish;
	}

	@ReadValue({ "��Ŀ����ͼ/end_date", "��Ŀ����ͼ����Դ�ƻ����䣩/end_date", "�ҵĹ����������ƣ�/end_date" })
	public Date getPlanEndDate() {
		return planFinish;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// ����, ��Ҫ���棬�����贫�ݵ�gantt�ͱ༭��
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
	// ��ʱ, ��Ҫ����
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
	// ��������� �ٷֱ�
	@ReadValue("dar")
	public Double getDAR() {
		if (planDuration != 0 && !milestone) {
			return 1d * getActualDuration() / planDuration;
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// ����������� �ٷֱ�
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
	// ��ɰٷֱ�
	@ReadValue
	@WriteValue
	@Persistence
	private Double progress;

	////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// ���������ָ�� �ٷֱ�
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
	// �������̱���gantt��typeΪmilestone������Ϊtask��
	// �����gantt�и�����task,ʹ�������ӹ�����gantt��type��Ϊproject
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
	@Behavior("����׶�ҳ��")
	private boolean stage;

	@ReadValue
	@SetValue
	private String stageName;

	@Persistence
	private boolean distributed;

	@ReadValue("distributeIcon")
	private String getDistributedIcon() {
		if (!distributed) {
			return "<span class='layui-badge layui-bg-orange'>δ�´�</span>";
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
	// �洢�����ݿ��е��ǹ����𡣱�����Gantt�е���barstyle,��ʽ
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
	// �����ǿ���gantt�Ŀͻ��˵�����
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
	// �����ı�ǩ�ı�
	@Label
	public String toString() {
		return text;
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * ������ɫ
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

	@ReadValue({ "���ȼƻ��ͼ��/chargerInfoWithDistributeIcon", "���ȼƻ��ͼ�أ��鿴��/chargerInfoWithDistributeIcon",
			"���ȼƻ�/chargerInfoWithDistributeIcon", "���ȼƻ����鿴��/chargerInfoWithDistributeIcon" })
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

	@ReadValue("���Ź����ճ̱�/section_id")
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

	@Structure({ "��ĿWBS/list", "���ȼƻ��ͼ��/list", "���ȼƻ��ͼ�أ��鿴��/list", "���ȼƻ�/list", "���ȼƻ����鿴��/list" })
	private List<Work> listChildren() {
		return ServicesLoader.get(WorkService.class).listChildren(_id);
	}

	@Structure({ "��ĿWBS/count", "���ȼƻ��ͼ��/count", "���ȼƻ��ͼ�أ��鿴��/count", "���ȼƻ�/count", "���ȼƻ����鿴��/count" })
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

		OBSItem obsRoot = new OBSItem()// ��������Ŀ��OBS���ڵ�
				.set_id(new ObjectId())// ����_id����Ŀ����
				.setScope_id(_id)// ����scope_id��������֯�ڵ��Ǹ���Ŀ����֯
				.setParent_id(obsParent_id)// �����ϼ���id
				.setName(text + "�Ŷ�")// ���ø���֯�ڵ��Ĭ������
				.setRoleId(OBSItem.ID_CHARGER)// ���ø���֯�ڵ�Ľ�ɫid
				.setRoleName(OBSItem.NAME_CHARGER)// ���ø���֯�ڵ������
				.setManagerId(chargerId) // ���ø���֯�ڵ�Ľ�ɫ��Ӧ����
				.setScopeRoot(true);// ��������ڵ��Ƿ�Χ�ڵĸ��ڵ�

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
		if ("����".equals(overdue))
			return "<span class='layui-badge'>����</span>";
		else if ("Ԥ��".equals(overdue))
			return "<span class='layui-badge layui-bg-orange'>Ԥ��</span>";
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

	@Behavior("ָ��")
	private boolean behaviourAssigner(@MethodParam(MethodParam.CURRENT_USER_ID) String userid) {
		return userid.equals(assignerId);
	}

	@Behavior("��ʼ����")
	private boolean behaviourStart() {
		return actualStart == null;
	}

	@Behavior("��ɹ���")
	private boolean behaviourFinish() {
		return actualStart != null;
	}

	@Behavior({ "�򿪹�����" })
	private boolean behaviourOpenWorkPackage() {
		return !summary && !stage && !milestone;
	}

	@Behavior({ "���ù�����" })
	private boolean behaviourSetWorkPackage() {
		return !summary && !stage && !milestone && actualStart == null;
	}

	@Behavior("����������")
	private boolean behaviourCreateWorkPackage() {
		return actualFinish == null;
	}

	@Structure({ "�ҵĴ����������������룩/list" })
	private List<WorkBoardInfo> getWorkBoardInfo() {
		return Arrays.asList(new WorkBoardInfo().setWork(this));
	}

	@Structure({ "�ҵĴ����������������룩/count" })
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
					overdue = "����";
				} else {
					overdue = "";
				}
			} else {
				cal.setTime(planFinish);
				cal.add(Calendar.DAY_OF_MONTH, (warningDay == null ? 0 : (-1 * warningDay)));
				Date warning = cal.getTime();
				if (now.after(planFinish)) {
					overdue = "����";
				} else if (now.after(warning)) {
					overdue = "Ԥ��";
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

	@ReadValue({ "�ҵĹ���/workpackageHtml", "���ȼƻ�/workpackageHtml", "���ȼƻ����鿴��/workpackageHtml" })
	public String getWorkPackageActionHtml() {
		if (summary || milestone) {
			return "";
		}

		StringBuffer sb = new StringBuffer();
		List<TrackView> wps = getWorkPackageSetting();
		sb.append("<div style='display: inline-flex;" + "    justify-content: space-between;" + "    width: 100%;'>");
		if (Util.isEmptyOrNull(wps)) {
			sb.append("<a class='layui-btn layui-btn-xs layui-btn-primary' style='flex:auto;' href='"
					+ "openWorkPackage/default" + "' target='_rwt'>" + "������" + "</a>");
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
	// ��ø����û��ڽ׶��еĽ�ɫ
	@RoleBased
	private List<String> getStageRole(@MethodParam(MethodParam.CURRENT_USER_ID) String userId) {
		return ServicesLoader.get(OBSService.class).getScopeRoleofUser(_id, userId);
	}

	@SetValue
	private Double aci;

	@ReadValue("���ȼƻ��ͼ��/ACI")
	public Double getACI() {
		if (summary || milestone) {
			return null;
		}
		return aci;
	}

	@SetValue
	private Double acp;

	@ReadValue("���ȼƻ��ͼ��/ACP")
	public Double getACP() {
		if (summary || milestone) {
			return null;
		}
		return acp;
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	// ���߼�¼ԭʼ�������
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
