package com.bizvisionsoft.service.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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
	public static final String STATUS_CREATE = "创建";

	@Exclude
	public static final String STATUS_SUBMIT = "提交";

	@Exclude
	public static final String STATUS_CONFIRM = "确认";

	@Exclude
	public static final String STATUS_CANCEL = "取消";

	@Exclude
	public static final String CHOICE_CONFIRM = "通过";

	@Exclude
	public static final String CHOICE_CANCEL = "取消";

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

	/**
	 * 申请人
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
	 * 申请单位
	 */
	@Persistence
	private ObjectId applicantUnitId;

	@ReadValue
	@SetValue
	private String applicantUnit;

	/**
	 * 申请时间
	 */
	@WriteValue
	@ReadValue
	private Date applicantDate;

	public ProjectChange setApplicantDate(Date applicantDate) {
		this.applicantDate = applicantDate;
		return this;
	}

	/**
	 * 项目现状
	 */
	@WriteValue
	@ReadValue
	private String projectSituation;

	/**
	 * 申请调整原因
	 */
	@WriteValue
	@ReadValue
	private String reason;

	/**
	 * 任务内容及工作安排、进度要求
	 */
	@WriteValue
	@ReadValue
	private String requirement;

	/**
	 * 成果形式及数量
	 */
	@WriteValue
	@ReadValue
	private String achievements;

	/**
	 * 调整后相关问题的处理
	 */
	@WriteValue
	@ReadValue
	private String questionProcessing;

	@WriteValue
	@ReadValue
	private Date submitDate;

	private List<ProjectChangeTask> reviewer = new ArrayList<ProjectChangeTask>();

	@WriteValue("reviewer1")
	private void setReviewer1(User reviewer) {
		boolean b = true;
		for (ProjectChangeTask pct : this.reviewer) {
			if ("reviewer1".equals(pct.name)) {
				pct.reviewer = Optional.ofNullable(reviewer).map(o -> o.getUserId()).orElse(null);
				b = false;
			}
		}
		if (b) {
			ProjectChangeTask pct = new ProjectChangeTask();
			pct.reviewer = Optional.ofNullable(reviewer).map(o -> o.getUserId()).orElse(null);
			pct.name = "reviewer1";
			this.reviewer.add(pct);
		}
	}

	@WriteValue("reviewer2")
	private void setReviewer2(User reviewer) {
		boolean b = true;
		for (ProjectChangeTask pct : this.reviewer) {
			if ("reviewer2".equals(pct.name)) {
				pct.reviewer = Optional.ofNullable(reviewer).map(o -> o.getUserId()).orElse(null);
				b = false;
			}
		}
		if (b) {
			ProjectChangeTask pct = new ProjectChangeTask();
			pct.reviewer = Optional.ofNullable(reviewer).map(o -> o.getUserId()).orElse(null);
			pct.name = "reviewer2";
			this.reviewer.add(pct);
		}
	}

	@WriteValue("reviewer3")
	private void setReviewer3(User reviewer) {
		boolean b = true;
		for (ProjectChangeTask pct : this.reviewer) {
			if ("reviewer3".equals(pct.name)) {
				pct.reviewer = Optional.ofNullable(reviewer).map(o -> o.getUserId()).orElse(null);
				b = false;
			}
		}
		if (b) {
			ProjectChangeTask pct = new ProjectChangeTask();
			pct.reviewer = Optional.ofNullable(reviewer).map(o -> o.getUserId()).orElse(null);
			pct.name = "reviewer3";
			this.reviewer.add(pct);
		}
	}

	@WriteValue("reviewer4")
	private void setReviewer4(User reviewer) {
		boolean b = true;
		for (ProjectChangeTask pct : this.reviewer) {
			if ("reviewer4".equals(pct.name)) {
				pct.reviewer = Optional.ofNullable(reviewer).map(o -> o.getUserId()).orElse(null);
				b = false;
			}
		}
		if (b) {
			ProjectChangeTask pct = new ProjectChangeTask();
			pct.reviewer = Optional.ofNullable(reviewer).map(o -> o.getUserId()).orElse(null);
			pct.name = "reviewer4";
			this.reviewer.add(pct);
		}
	}

	@WriteValue("reviewer5")
	private void setReviewer5(User reviewer) {
		boolean b = true;
		for (ProjectChangeTask pct : this.reviewer) {
			if ("reviewer5".equals(pct.name)) {
				pct.reviewer = Optional.ofNullable(reviewer).map(o -> o.getUserId()).orElse(null);
				b = false;
			}
		}
		if (b) {
			ProjectChangeTask pct = new ProjectChangeTask();
			pct.reviewer = Optional.ofNullable(reviewer).map(o -> o.getUserId()).orElse(null);
			pct.name = "reviewer5";
			this.reviewer.add(pct);
		}
	}

	@WriteValue("reviewer6")
	private void setReviewer6(User reviewer) {
		boolean b = true;
		for (ProjectChangeTask pct : this.reviewer) {
			if ("reviewer6".equals(pct.name)) {
				pct.reviewer = Optional.ofNullable(reviewer).map(o -> o.getUserId()).orElse(null);
				b = false;
			}
		}
		if (b) {
			ProjectChangeTask pct = new ProjectChangeTask();
			pct.reviewer = Optional.ofNullable(reviewer).map(o -> o.getUserId()).orElse(null);
			pct.name = "reviewer6";
			this.reviewer.add(pct);
		}
	}

	@WriteValue("reviewer7")
	private void setReviewer7(User reviewer) {
		boolean b = true;
		for (ProjectChangeTask pct : this.reviewer) {
			if ("reviewer7".equals(pct.name)) {
				pct.reviewer = Optional.ofNullable(reviewer).map(o -> o.getUserId()).orElse(null);
				b = false;
			}
		}
		if (b) {
			ProjectChangeTask pct = new ProjectChangeTask();
			pct.reviewer = Optional.ofNullable(reviewer).map(o -> o.getUserId()).orElse(null);
			pct.name = "reviewer7";
			this.reviewer.add(pct);
		}
	}

	@WriteValue("reviewer8")
	private void setReviewer8(User reviewer) {
		boolean b = true;
		for (ProjectChangeTask pct : this.reviewer) {
			if ("reviewer8".equals(pct.name)) {
				pct.reviewer = Optional.ofNullable(reviewer).map(o -> o.getUserId()).orElse(null);
				b = false;
			}
		}
		if (b) {
			ProjectChangeTask pct = new ProjectChangeTask();
			pct.reviewer = Optional.ofNullable(reviewer).map(o -> o.getUserId()).orElse(null);
			pct.name = "reviewer8";
			this.reviewer.add(pct);
		}
	}

	@WriteValue("reviewer9")
	private void setReviewer9(User reviewer) {
		boolean b = true;
		for (ProjectChangeTask pct : this.reviewer) {
			if ("reviewer9".equals(pct.name)) {
				pct.reviewer = Optional.ofNullable(reviewer).map(o -> o.getUserId()).orElse(null);
				b = false;
			}
		}
		if (b) {
			ProjectChangeTask pct = new ProjectChangeTask();
			pct.reviewer = Optional.ofNullable(reviewer).map(o -> o.getUserId()).orElse(null);
			pct.name = "reviewer9";
			this.reviewer.add(pct);
		}
	}

	public List<ProjectChangeTask> getReviewer() {
		return reviewer;
	}

	@Behavior({ "删除变更", "编辑变更", "提交变更" })
	public boolean behaviourDelete() {
		return STATUS_CREATE.equals(status);
	}

	@Behavior({ "确认变更", "取消变更" })
	public boolean behaviourSubmit(@MethodParam(MethodParam.CURRENT_USER_ID) String userid) {
		if (STATUS_SUBMIT.equals(status)) {
			for (ProjectChangeTask re : reviewer) {
				if (userid.equals(re.reviewer) && re.choice == null) {
					return true;
				}
			}
		}

		return false;
	}

	public String getConfimName(String userid) {
		for (ProjectChangeTask re : reviewer) {
			if (userid.equals(re.reviewer) && re.choice == null) {
				return re.name;
			}
		}
		return "";
	}

}
