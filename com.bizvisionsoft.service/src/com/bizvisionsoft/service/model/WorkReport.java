package com.bizvisionsoft.service.model;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Persistence;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.ReadValue;

@PersistenceCollection("workReport")
public class WorkReport {

	@Persistence
	private ObjectId _id;

	public ObjectId get_id() {
		return _id;
	}

	public void set_id(ObjectId _id) {
		this._id = _id;
	}

	@Persistence
	private Date cycle;

	public Date getCycle() {
		return cycle;
	}

	@ReadValue("cycle")
	public String getCycleText() {
		if (TYPE_DAILY.equals(type)) {
			return new SimpleDateFormat("yyyy-MM-dd").format(cycle);
		} else if (TYPE_WEEKLY.equals(type)) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(cycle);
			int weekOfMonth = cal.get(Calendar.WEEK_OF_MONTH);
			return new SimpleDateFormat("yyyy-MM").format(cycle) + " " + weekOfMonth + "周";
		} else if (TYPE_MONTHLY.equals(type)) {
			return new SimpleDateFormat("yyyy-MM").format(cycle);
		}
		return "";
	}

	public static String TYPE_DAILY = "日报";
	public static String TYPE_WEEKLY = "周报";
	public static String TYPE_MONTHLY = "月报";

	@Persistence
	private String type;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Persistence
	private ObjectId project_id;

	@Persistence
	private String reporter;

	public void setReporter(String reporter) {
		this.reporter = reporter;
	}

	@Persistence
	private Date reportDate;

	@Persistence
	private Date submitDate;

	@Persistence
	private String verifier;

	@Persistence
	private String verifyDate;

}
