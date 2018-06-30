package com.bizvisionsoft.service.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.Persistence;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.mongocodex.SetValue;
import com.bizvisionsoft.annotations.md.service.Behavior;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.UserService;

@PersistenceCollection("projectChange")
public class ProjectChange {

	@Exclude
	public static final String STATUS_CREATE = "����";

	@Exclude
	public static final String STATUS_SUBMIT = "�ύ";

	@Exclude
	public static final String STATUS_PASS = "ͨ��";

	@Exclude
	public static final String STATUS_CONFIRM = "�ر�";

	@Exclude
	public static final String STATUS_CANCEL = "ȡ��";

	@Exclude
	public static final String CHOICE_CONFIRM = "��׼";

	@Exclude
	public static final String CHOICE_CANCEL = "���";

	private ObjectId _id;

	public ObjectId get_id() {
		return _id;
	}

	private ObjectId project_id;

	public ObjectId getProject_id() {
		return project_id;
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

	public String getStatus() {
		return status;
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
	@Persistence
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

	private List<ProjectChangeTask> reviewer = new ArrayList<ProjectChangeTask>();

	public List<ProjectChangeTask> getReviewer() {
		return reviewer;
	}

	@Behavior({ "ɾ�����", "�༭���", "�ύ���" })
	public boolean behaviourDelete() {
		return STATUS_CREATE.equals(status);
	}

	@Behavior({ "������", "��׼���" })
	public boolean behaviourSubmit(@MethodParam(MethodParam.CURRENT_USER_ID) String userid) {
		if (STATUS_SUBMIT.equals(status)) {
			for (ProjectChangeTask re : reviewer) {
				if (userid.equals(re.user) && re.choice == null) {
					return true;
				}
			}
		}

		return false;
	}

	@Behavior({ "ȷ�ϱ��" })
	public boolean behaviourConfirm() {
		return STATUS_PASS.equals(status);
	}

	public String getConfimName(String userid) {
		for (ProjectChangeTask re : reviewer) {
			if (userid.equals(re.user) && re.choice == null) {
				return re.name;
			}
		}
		return "";
	}

}
