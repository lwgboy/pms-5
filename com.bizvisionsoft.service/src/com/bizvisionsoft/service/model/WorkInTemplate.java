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
	// id, ��ganttͼ�� ʹ��String ���ʹ��ݣ���� ReadValue��WriteValue��Ҫ�÷�����д
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

	@Persistence
	private ObjectId template_id;

	@ReadValue("project")
	public String getTemplateId() {
		return template_id == null ? null : template_id.toHexString();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// index, ��ganttͼ����������
	@WriteValue
	@ReadValue
	@Persistence
	private Integer index;
	
	public void setIndex(int index) {
		this.index = index;
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// WBS����
	@WriteValue
	@ReadValue({"��Ŀģ��WBS�������ɫ��/wbsCode","��Ŀģ��WBS/wbsCode"})
	@Persistence
	private String wbsCode;

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

	public WorkInTemplate setFullName(String fullName) {
		this.fullName = fullName;
		return this;
	}

	
	////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * �ƻ���ʼ����, �༭������ʱ��ҪУ��
	 */
	@ReadValue("start_date")
	@Persistence
	@WriteValue({ "��Ŀģ�幤���༭��/start_date", "��Ŀģ����̱������༭��/start_date" })
	private Date planStart;

	@WriteValue("��Ŀģ�����ͼ/start_date")
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
	// �ƻ��������, �༭������ʱ��ҪУ��
	@ReadValue("end_date")
	@Persistence
	@WriteValue("��Ŀģ�幤���༭��/end_date")
	private Date planFinish;

	@WriteValue("��Ŀģ�����ͼ/end_date")
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

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// ��ʱ, ��Ҫ����
	@SetValue
	@ReadValue
	private double planWorks;

	@GetValue("planWorks")
	public double getPlanWorks() {
		return planWorks;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// �������̱���gantt��typeΪmilestone������Ϊtask��
	// �����gantt�и�����task,ʹ�������ӹ�����gantt��type��Ϊproject
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
	// �洢�����ݿ��е��ǹ����𡣱�����Gantt�е���barstyle,��ʽ
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
	// �����ı�ǩ�ı�
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

	@ReadValue({ "��Ŀģ��WBS/wpsText", "��Ŀģ��WBS�������ɫ��/wpsText" })
	private String getWorkPackageSettingText() {
		if (Util.isEmptyOrNull(workPackageSetting)) {
			return "";
		} else {
			StringBuffer sb = new StringBuffer();
			workPackageSetting.forEach(tv -> sb.append(tv.getCatagory() + ":" + tv.getName() + " "));
			return sb.toString().trim();
		}
	}

	@ReadValue({ "��Ŀģ��WBS/manageLevelHtml", "��Ŀģ��WBS�������ɫ��/manageLevelHtml" })
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

	@ImageURL({ "��Ŀģ��WBS/milestoneIcon", "��Ŀģ��WBS�������ɫ��/milestoneIcon" })
	private String getMilestoneIcon() {
		if (milestone)
			return "/img/flag_blue.svg";
		return null;
	}

	@Structure({ "��Ŀģ��WBS/list", "��Ŀģ��WBS�������ɫ��/list" })
	private List<WorkInTemplate> listChildren() {
		return ServicesLoader.get(ProjectTemplateService.class).listWBSChildren(_id);
	}

	@Structure({ "��Ŀģ��WBS/count", "��Ŀģ��WBS�������ɫ��/count" })
	private long countChildren() {
		return ServicesLoader.get(ProjectTemplateService.class).countWBSChildren(_id);
	}

	@Behavior("�趨����������")
	private boolean behaviourEditWPS() {
		return !summary && !stage && !milestone;
	}
	
	@Behavior("��ɫ")
	private boolean behaviourEditRole() {
		return !milestone;
	}

	@Behavior("������")
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
