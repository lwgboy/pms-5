package com.bizvisionsoft.service.model;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.Persistence;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.mongocodex.SetValue;
import com.bizvisionsoft.annotations.md.service.Behavior;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.service.tools.MetaInfoWarpper;

@PersistenceCollection("workReport")
public class WorkReport {

	@WriteValue
	@Persistence
	private ObjectId _id;

	public ObjectId get_id() {
		return _id;
	}

	public void set_id(ObjectId _id) {
		this._id = _id;
	}

	@Persistence
	private Date period;

	public Date getPeriod() {
		return period;
	}

	public WorkReport setPeriod(Date period) {
		this.period = period;
		return this;
	}

	@ReadValue("period")
	public String getCycleText() {
		if (TYPE_DAILY.equals(type)) {
			return new SimpleDateFormat("yyyy��M��dd��").format(period);
		} else if (TYPE_WEEKLY.equals(type)) {
			return new SimpleDateFormat("yyyy��M�� ��W").format(period) + "��";
		} else if (TYPE_MONTHLY.equals(type)) {
			return new SimpleDateFormat("yyyy��M��").format(period);
		}
		return "";
	}

	// public static void main(String[] args) {
	// Calendar cal = Calendar.getInstance();
	// cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
	// cal.set(Calendar.HOUR_OF_DAY, 0);
	// cal.set(Calendar.MINUTE, 0);
	// cal.set(Calendar.SECOND, 0);
	// Date date = cal.getTime();
	// }

	@Exclude
	public static final String TYPE_DAILY = "�ձ�";

	@Exclude
	public static final String TYPE_WEEKLY = "�ܱ�";

	@Exclude
	public static final String TYPE_MONTHLY = "�±�";

	@WriteValue
	@ReadValue
	@Persistence
	private String type;

	public String getType() {
		return type;
	}

	public WorkReport setType(String type) {
		this.type = type;
		return this;
	}

	@WriteValue
	@Persistence
	private ObjectId project_id;

	public ObjectId getProject_id() {
		return project_id;
	}

	public WorkReport setProject_id(ObjectId project_id) {
		this.project_id = project_id;
		return this;
	}

	@ReadValue
	@SetValue
	private String projectName;

	@ReadValue
	@SetValue
	private String projectNumber;

	@ReadValue
	@Persistence
	private ObjectId stage_id;

	public WorkReport setStage_id(ObjectId stage_id) {
		this.stage_id = stage_id;
		return this;
	}

	@ReadValue
	@SetValue
	private String stageName;

	@WriteValue
	@Persistence
	private String reporter;

	@ReadValue
	@SetValue
	private String reporterInfo;

	@ReadValue
	@SetValue
	private UserMeta reporterInfo_meta;

	public WorkReport setReporter(String reporter) {
		this.reporter = reporter;
		return this;
	}

	public String getReporter() {
		return reporter;
	}

	@ReadValue("reporterInfoHtml")
	public String getReporterInfoHtml() {
		if (reporterInfo == null) {
			return "";
		}
		return "<div class='brui_ly_hline'>>" + warpperReporterInfo() + "</div>";
	}

	public String warpperReporterInfo() {
		return MetaInfoWarpper.userInfo(reporterInfo_meta, reporterInfo);
	}

	@ReadValue
	@Persistence
	private Date reportDate;

	public WorkReport setReportDate(Date reportDate) {
		this.reportDate = reportDate;
		return this;
	}

	@ReadValue
	@Persistence
	private Date submitDate;

	@Persistence
	private String verifier;

	@ReadValue
	@SetValue
	private String verifierInfo;

	@ReadValue
	@SetValue
	private UserMeta verifierInfo_meta;

	@ReadValue("verifierInfoHtml")
	public String getVerifierInfoHtml() {
		if (verifierInfo == null) {
			return "";
		}
		return "<div class='brui_ly_hline'>>" + warpperVerifierInfo() + "</div>";
	}

	public String warpperVerifierInfo() {
		return MetaInfoWarpper.userInfo(verifierInfo_meta, verifierInfo);
	}

	@ReadValue
	@SetValue
	private Date verifyDate;

	@ReadValue
	@WriteValue
	@Persistence
	private String workRemark;

	@ReadValue
	@WriteValue
	@Persistence
	private String otherRemark;

	@SetValue
	private String pmId;

	@ReadValue
	@SetValue
	private String pmInfo;

	@ReadValue
	@SetValue
	private UserMeta pmInfo_meta;
	

	@ReadValue("pmInfoHtml")
	public String getPMInfoHtml() {
		if (pmInfo == null) {
			return "";
		}
		return "<div class='brui_ly_hline'>" + warpperPMInfo() + "</div>";
	}

	public String warpperPMInfo() {
		return MetaInfoWarpper.userInfo(pmInfo_meta, pmInfo);
	}

	@ReadValue
	@WriteValue
	@Persistence
	private String status;

	@Exclude
	public static final String STATUS_CREATE = "����";

	@Exclude
	public static final String STATUS_SUBMIT = "�ύ";

	@Exclude
	public static final String STATUS_CONFIRM = "ȷ��";

	@Label
	public String getLabel() {
		return projectName + "/" + getCycleText();
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
		WorkReport other = (WorkReport) obj;
		if (_id == null) {
			if (other._id != null)
				return false;
		} else if (!_id.equals(other._id))
			return false;
		return true;
	}

	public Date getPeriodForm() {
		return period;
	}

	public Date getPeriodTo() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(period);
		if (TYPE_WEEKLY.equals(type)) {
			cal.add(Calendar.DAY_OF_MONTH, 6);
		} else if (TYPE_MONTHLY.equals(type)) {
			cal.add(Calendar.MONTH, 1);
			cal.add(Calendar.DAY_OF_MONTH, -1);
		}
		return cal.getTime();
	}

	@Behavior({ "ɾ������", "�༭����", "�ύ����" })
	private boolean behaviourEdit() {
		return STATUS_CREATE.equals(status);
	}

	@Behavior("ȷ�ϱ���")
	private boolean behaviourConfirm(@MethodParam(MethodParam.CURRENT_USER_ID) String userid) {
		return STATUS_SUBMIT.equals(status) && userid.equals(pmId);
	}

	public WorkReport setStatus(String status) {
		this.status = status;
		return this;
	}

	public String getStatus() {
		return status;
	}

}
