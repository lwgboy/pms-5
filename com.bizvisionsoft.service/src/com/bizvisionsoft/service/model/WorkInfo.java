package com.bizvisionsoft.service.model;

import java.util.Date;
import java.util.List;
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
import com.bizvisionsoft.service.sn.WorkGenerator;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.service.tools.Formatter;

//implements IWBSScope 
@PersistenceCollection("workspace")
@Strict
public class WorkInfo {

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// id, ��ganttͼ�� ʹ��String ���ʹ��ݣ���� ReadValue��WriteValue��Ҫ�÷�����д
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
		if (!Check.equals(newParent_id, this.parent_id)) {
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
		if (!Check.equals(newId, this.project_id)) {
			this.project_id = newId;
			return true;
		} else {
			return false;
		}
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	@WriteValue
	@ReadValue
	@Persistence
	private Integer index;

	public void setIndex(int index) {
		this.index = index;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// WBS���� TODO �Զ����ɵķ���
	@WriteValue
	@Persistence
	private String wbsCode;
	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// WBS���� TODO �Զ����ɵķ���
	@WriteValue
	@Persistence
	@Generator(name = Generator.DEFAULT_NAME, key = "work", generator = WorkGenerator.class, callback = Generator.NONE_CALLBACK)
	private String code;

	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// text, ��ganttͼtext�ֶΣ����ݿ���Ϊname�ֶ�
	@ReadValue
	@WriteValue
	@Persistence("name")
	@Label(Label.NAME_LABEL)
	private String text;
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

	public void setFullName(String fullName) {
		this.fullName = fullName;
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

	@WriteValue({ "����ͼ�ܳɹ����༭��/start_date", "����ͼ�����༭��/start_date", "����ͼ�׶ι����༭��/start_date", "����ͼ��̱������༭��/start_date" })
	public WorkInfo setStart_date(Date start_date) {
		checkDate(start_date, this.getEnd_date());
		if (actualStart != null) {
			actualStart = start_date;
		} else {
			planStart = start_date;
		}
		return this;
	}

	@WriteValue("��Ŀ����ͼ���༭��/start_date")
	public boolean setStart_date(String start_date) {
		Date newDate = Formatter.getDatefromJS(start_date);
		if (!Check.equals(newDate, this.planStart)) {
			planStart = newDate;
			return true;
		}
		return false;
	}

	@ReadValue({ "start_date" })
	public Date getStart_date() {
		return planStart;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// �ƻ��������, �༭������ʱ��ҪУ��
	@ReadValue("planFinish")
	@Persistence("planFinish")
	private Date planFinish;

	@ReadValue("actualFinish")
	@Persistence("actualFinish")
	private Date actualFinish;

	@WriteValue({ "����ͼ�ܳɹ����༭��/end_date", "����ͼ�����༭��/end_date", "����ͼ�׶ι����༭��/end_date" })
	public WorkInfo setEnd_date(Date end_date) {
		checkDate(getStart_date(), end_date);
		planFinish = end_date;
		return this;
	}

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
	@WriteValue("��Ŀ����ͼ���༭��/end_date")
	public boolean setEnd_date(String end_date) {
		Date newDate = Formatter.getDatefromJS(end_date);
		if (!Check.equals(newDate, this.planFinish)) {
			planFinish = newDate;
			return true;
		}
		return false;
	}

	@ReadValue("end_date")
	public Date getEnd_date() {
		return planFinish;
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// ����, ��Ҫ���棬�����贫�ݵ�gantt�ͱ༭��
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
	// �洢�����ݿ��е��ǹ����𡣱�����Gantt�е���barstyle,��ʽ
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
	private String chargerInfo;

	@ReadValue("chargerInfo")
	public String getChargerInfo() {
		return chargerInfo != null ? chargerInfo : "";
	}

	@WriteValue("charger")
	public void setCharger(OBSItemWarpper charger) {
		if (charger != null) {
			this.chargerId = charger.getUserId();
			this.chargerInfo = charger.getUserName();
		} else {
			this.chargerId = null;
			this.chargerInfo = null;
		}
	}

	@ReadValue("charger")
	private OBSItemWarpper getCharger() {
		return Optional.ofNullable(chargerId)
				.map(id -> new OBSItemWarpper().setUser(ServicesLoader.get(UserService.class).get(id))).orElse(null);
	}

	@ReadValue
	@WriteValue
	@Persistence
	private String assignerId;

	@SetValue
	@ReadValue
	private String assignerInfo;

	@WriteValue("assigner")
	private void setAssigner(OBSItemWarpper assigner) throws Exception {
		if (actualFinish == null)
			if (assigner != null) {
				this.assignerId = assigner.getUserId();
				this.assignerInfo = assigner.getUserName();
			} else {
				this.assignerId = null;
				this.assignerInfo = null;
			}
		else
			throw new Exception("��������ɣ�������༭ָ����");
	}

	@ReadValue("assigner")
	private OBSItemWarpper getAssigner() {
		return Optional.ofNullable(assignerId)
				.map(id -> new OBSItemWarpper().setUser(ServicesLoader.get(UserService.class).get(id))).orElse(null);
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
	 * ���ڱȽϵļƻ����ʱ��
	 */
	@SetValue
	private Date planFinish1;

	/**
	 * ���ڱȽϵ�ʵ�����ʱ��
	 */
	@SetValue
	private Date actualFinish1;

	@ReadValue("end_date1")
	public Date getEnd_date1() {
		return planFinish1;
	}

	/**
	 * ���ڱȽϵļƻ���ʼʱ��
	 */
	@SetValue
	private Date planStart1;

	/**
	 * ���ڱȽϵ�ʵ�ʿ�ʼʱ��
	 */
	@SetValue
	private Date actualStart1;

	@ReadValue("start_date1")
	public Date getStart_date1() {
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
				.setProjectName(project.getProjectName()).setProjectNumber(project.getProjectNumber());
	}

	public static WorkInfo newInstance(Work work) {
		return new WorkInfo().set_id(new ObjectId()).setProject_id(work.getProject_id()).setParent_id(work.get_id())
				.setProjectName(work.getProjectName()).setProjectNumber(work.getProjectNumber());
	}

	public static WorkInfo newInstance(WorkInfo workinfo) {
		return new WorkInfo().set_id(new ObjectId()).setProject_id(workinfo.getProject_id())
				.setParent_id(workinfo.get_id()).setProjectName(workinfo.projectName)
				.setProjectNumber(workinfo.projectNumber);
	}

	private void checkDate(Date start_date, Date end_date) {
		if (start_date != null && end_date != null && start_date.after(end_date)) {
			throw new RuntimeException("��ʼ���ڲ��������������");
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

	public WorkInfo setSpaceId(ObjectId space_id) {
		this.space_id = space_id;
		return this;
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
	// �������̱���gantt��typeΪmilestone������Ϊtask��
	// �����gantt�и�����task,ʹ�������ӹ�����gantt��type��Ϊproject
	@Persistence
	@ReadValue
	@WriteValue
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
	// ��ɰٷֱ�
	@ReadValue
	@WriteValue
	@Persistence
	private Double progress;
	////////////////////////////////////////////////////////////////////////////////////////////////////

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

	@Behavior({ "����������", "������̱�", "���WBSģ��" })
	private boolean behaviourAddTask() {
		if (stage && ProjectStatus.Closed.equals(status)) {
			// �׶ιرղ������
			return false;
		} else if (milestone) {
			// ��̲������
			return false;
		} else if (!stage && !milestone && actualFinish != null) {
			// ������ɲ������
			return false;
		}
		return true;
	}

	@Behavior("���ù�����")
	private boolean behaviourEditWPS() {
		return !summary && !stage;
	}

	@Behavior("ָ��������")
	private boolean behaviourSetCharger() {
		return !milestone && actualFinish == null;
	}

	@Behavior("ɾ������")
	private boolean behaviourDeleteTask() {
		return (stage && ProjectStatus.Created.equals(status)) || (!stage && actualStart == null);
	}

	public WorkInfo setPlanStart(Date planStart) {
		this.planStart = planStart;
		return this;
	}

	public WorkInfo setPlanFinish(Date planFinish) {
		this.planFinish = planFinish;
		return this;
	}

	public Date getPlanFinish() {
		return planFinish;
	}

	public Date getPlanStart() {
		return planStart;
	}

	public WorkInfo setChargerId(String chargerId) {
		this.chargerId = chargerId;
		return this;
	}

	public WorkInfo setMilestone(boolean milestone) {
		this.milestone = milestone;
		return this;
	}

	@ReadValue
	@WriteValue
	@Persistence
	private String chargerRoleId;

	@ReadValue
	@WriteValue
	@Persistence
	private String assignerRoleId;

	@ReadValue
	@WriteValue
	@Persistence
	private boolean distributed;

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
		WorkInfo other = (WorkInfo) obj;
		if (_id == null) {
			if (other._id != null)
				return false;
		} else if (!_id.equals(other._id))
			return false;
		return true;
	}

}
