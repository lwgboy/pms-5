package com.bizvisionsoft.service.model;

import java.util.Date;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.mongocodex.SetValue;
import com.bizvisionsoft.annotations.md.service.Behavior;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.UserService;

@PersistenceCollection("projectChange")
public class ProjectChange {

	@Exclude
	public static final String STATUS_CREATE = "����";

	@Exclude
	public static final String STATUS_SUBMIT = "�ύ";

	@Exclude
	public static final String STATUS_CONFIRM = "ȷ��";

	@Exclude
	public static final String STATUS_CANCEL = "ȡ��";

	@Exclude
	public static final String CHOICE_CONFIRM = "ͨ��";

	@Exclude
	public static final String CHOICE_CANCEL = "ȡ��";

	private ObjectId _id;

	public ObjectId get_id() {
		return _id;
	}

	private ObjectId project_id;

	public ObjectId getProject_id() {
		return project_id;
	}

	private Project project;

	private Project getProject() {
		if (project == null) {
			project = ServicesLoader.get(ProjectService.class).get(project_id);
		}
		return project;
	}

	@ReadValue
	@SetValue
	private String projectName;

	@ReadValue
	@SetValue
	private String projectNumber;

	public ProjectChange setProject_id(ObjectId project_id) {
		this.project_id = project_id;
		return this;
	}

	@ReadValue
	private String status;

	public ProjectChange setStatus(String status) {
		this.status = status;
		return this;
	}

	/**
	 * ������
	 */
	private String applicant;

	public String getApplicantId() {
		return applicant;
	}

	public User getApplicant() {
		return ServicesLoader.get(UserService.class).get(applicant);
	}

	public ProjectChange setApplicant(String applicant) {
		this.applicant = applicant;
		return this;
	}

	public ProjectChange setApplicant(User user) {
		this.applicant = user.getUserId();
		this.applicantUnitId = user.getOrganizationId();
		return this;
	}

	@ReadValue
	@SetValue
	private String applicantInfo;

	/**
	 * ���뵥λ
	 */
	private ObjectId applicantUnitId;

	@ReadValue
	@SetValue
	private String applicantUnit;

	/**
	 * ����ʱ��
	 */
	@WriteValue
	@ReadValue
	private Date applicantDate;

	public ProjectChange setApplicantDate(Date applicantDate) {
		this.applicantDate = applicantDate;
		return this;
	}

	/**
	 * ��Ŀ��״
	 */
	@WriteValue
	@ReadValue
	private String projectSituation;

	/**
	 * �������ԭ��
	 */
	@WriteValue
	@ReadValue
	private String reason;

	/**
	 * �������ݼ��������š�����Ҫ��
	 */
	@WriteValue
	@ReadValue
	private String requirement;

	/**
	 * �ɹ���ʽ������
	 */
	@WriteValue
	@ReadValue
	private String achievements;

	/**
	 * �������������Ĵ���
	 */
	@WriteValue
	@ReadValue
	private String questionProcessing;

	@WriteValue
	@ReadValue
	private Date submitDate;

	/**
	 * ��Ŀ�������
	 */
	private String pmId;

	@SetValue
	private String projectPMId;

	@ReadValue
	@SetValue
	private String pmInfo;

	@WriteValue
	@ReadValue
	private Date pmDate;

	@WriteValue
	@ReadValue
	private String pmChoice;

	@WriteValue
	@ReadValue
	private String pmComment;

	/**
	 * �����쵼���
	 */
	private String deptId;

	@SetValue
	private String managerId;

	@ReadValue
	@SetValue
	private String deptInfo;

	@WriteValue
	@ReadValue
	private Date deptDate;

	@WriteValue
	@ReadValue
	private String deptChoice;

	@WriteValue
	@ReadValue
	private String deptComment;

	/**
	 * ��Ŀ�������
	 */
	private String psId;

	@ReadValue
	@SetValue
	private String psInfo;

	@WriteValue
	@ReadValue
	private Date psDate;

	@WriteValue
	@ReadValue
	private String psChocie;

	@WriteValue
	@ReadValue
	private String psComment;

	/**
	 * �����쵼���
	 */
	private String psManagerId;

	@ReadValue
	@SetValue
	private String psManagerInfo;

	@WriteValue
	@ReadValue
	private Date psManagerDate;

	@WriteValue
	@ReadValue
	private String psManagerChoice;

	@WriteValue
	@ReadValue
	private String psManagerComment;

	/**
	 * �ܹ���ʦ���
	 */
	private String chiefEngineerId;

	@ReadValue
	@SetValue
	private String chiefEngineerInfo;

	@WriteValue
	@ReadValue
	private Date chiefEngineerDate;

	@WriteValue
	@ReadValue
	private String chiefEngineerChoice;

	@WriteValue
	@ReadValue
	private String chiefEngineerComment;

	/**
	 * �û��������
	 */
	private String consumerId;

	@ReadValue
	@SetValue
	private String consumerInfo;

	@WriteValue
	@ReadValue
	private Date consumerDate;

	@WriteValue
	@ReadValue
	private String consumerChoice;

	@WriteValue
	@ReadValue
	private String consumerComment;

	@Behavior({ "ɾ�����", "�༭���", "�ύ���" })
	public boolean behaviourDelete() {
		return STATUS_CREATE.equals(status);
	}

	@Behavior({ "ȷ�ϱ��", "ȡ�����" })
	public boolean behaviourSubmit(@MethodParam(MethodParam.CURRENT_USER_ID) String userid) {
		boolean submit = false;
		if (STATUS_SUBMIT.equals(status)) {
			// TODO �����û������ж�
			if (isPM(userid)) {
				submit = true;
			} else if (isDept(userid)) {
				submit = true;
			} else if (isPS(userid)) {
				submit = true;
			} else if (isPSManager(userid)) {
				submit = true;
			} else if (isChiefEngineer(userid)) {
				submit = true;
			} else if (isConsumer(userid)) {
				submit = true;
			}
		}

		return submit;
	}

	private boolean isConsumer(String userid) {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean isChiefEngineer(String userid) {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean isPSManager(String userid) {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean isPS(String userid) {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean isDept(String userid) {
		if (deptId == null) {
			if (userid.equals(managerId)) {
				return true;
			}
		}
		return false;
	}

	private boolean isPM(String userid) {
		if (pmId == null) {
			if (userid.equals(projectPMId)) {
				return true;
			}
		}
		return false;
	}

	public String getConfimName(String userid) {
		if (isPM(userid)) {
			return "pm";
		} else if (isDept(userid)) {
			return "dept";
		} else if (isPS(userid)) {
			return "ps";
		} else if (isPSManager(userid)) {
			return "psManager";
		} else if (isChiefEngineer(userid)) {
			return "chiefEngineer";
		} else if (isConsumer(userid)) {
			return "consumer";
		}
		return "";
	}

}
