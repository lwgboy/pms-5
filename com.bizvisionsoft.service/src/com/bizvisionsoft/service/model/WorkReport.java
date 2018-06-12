package com.bizvisionsoft.service.model;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Persistence;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.mongocodex.SetValue;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

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

	@WriteValue
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
			return new SimpleDateFormat("yyyy-MM-dd").format(period);
		} else if (TYPE_WEEKLY.equals(type)) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(period);
			int weekOfMonth = cal.get(Calendar.WEEK_OF_MONTH);
			return new SimpleDateFormat("yyyy-MM").format(period) + " " + weekOfMonth + "周";
		} else if (TYPE_MONTHLY.equals(type)) {
			return new SimpleDateFormat("yyyy-MM").format(period);
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
	// System.out.println(date);
	// }

	public static String TYPE_DAILY = "日报";
	
	public static String TYPE_WEEKLY = "周报";
	
	public static String TYPE_MONTHLY = "月报";
	@WriteValue
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

	public WorkReport setProject_id(ObjectId project_id) {
		this.project_id = project_id;
		return this;
	}

	@ReadValue
	@SetValue
	private String projectInfo;

	@ReadValue
	@Persistence
	private ObjectId stage_id;

	public WorkReport setStage_id(ObjectId stage_id) {
		this.stage_id = stage_id;
		return this;
	}

	@ReadValue
	@SetValue
	private String stageInfo;

	@WriteValue
	@Persistence
	private String reporter;

	@ReadValue
	@SetValue
	private String reporterInfo;

	public WorkReport setReporter(String reporter) {
		this.reporter = reporter;
		return this;
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
	@Persistence
	private String verifyDate;

	@ReadValue
	@WriteValue
	@Persistence
	private String workRemark;

	@ReadValue
	@WriteValue
	@Persistence
	private String otherRemark;

	@Label
	private String getLabel() {
		if (TYPE_DAILY.equals(type)) {
			return projectInfo + "/" + new SimpleDateFormat("yyyy-MM-dd").format(period);
		} else if (TYPE_WEEKLY.equals(type)) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(period);
			int weekOfMonth = cal.get(Calendar.WEEK_OF_MONTH);
			return  projectInfo + "/" + new SimpleDateFormat("yyyy-MM").format(period) + " "
					+ weekOfMonth + "周";
		} else if (TYPE_MONTHLY.equals(type)) {
			return  projectInfo + "/" + new SimpleDateFormat("yyyy-MM").format(period);
		}
		return "";
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
}
