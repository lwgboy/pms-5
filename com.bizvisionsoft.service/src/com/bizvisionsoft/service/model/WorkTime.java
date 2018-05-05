package com.bizvisionsoft.service.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.service.Behavior;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.service.tools.Util;

public class WorkTime {

	@Behavior({ "工作日历/删除", "工作日历/编辑" })
	private boolean behavior = true;
	
	@ReadValue(ReadValue.TYPE)
	@Exclude
	private String typeName = "工作时间";

	@ReadValue
	@WriteValue
	public ObjectId _id;

	@ReadValue
	@WriteValue
	@Label
	public String name;

	@ReadValue
	@WriteValue
	public List<String> day;

	@ReadValue
	@WriteValue
	public Date date;

	@ReadValue
	@WriteValue
	public boolean workingDay;

	@ReadValue
	@WriteValue
	public Date from1;

	@ReadValue
	@WriteValue
	public Date to1;

	@ReadValue
	@WriteValue
	public Date from2;

	@ReadValue
	@WriteValue
	public Date to2;

	@ReadValue
	@WriteValue
	public Date from3;

	@ReadValue
	@WriteValue
	public Date to3;

	@Override
	@ReadValue("工作日历/id")
	public String toString() {
		// 工作日设置
		if (date != null && !workingDay) {
			return "非工作日";
		}

		if (day != null && !day.isEmpty()) {
			if (from1 != null || to1 != null || from2 != null || to2 != null || from3 != null || to3 != null)
				return "工作时间";
		}
		// String text = name;
		// if (date != null) {
		// text += " " + new SimpleDateFormat(Util.DATE_FORMAT_DATE).format(date) +
		// (workingDay ? "工作日" : "非工作日");
		// }
		return "工作日设置";
	}

	@ReadValue("工作日历/description")
	public String getDescription() {
		String text = "";
		if (date != null)
			text += " " + new SimpleDateFormat(Util.DATE_FORMAT_DATE).format(date) + (workingDay ? "工作日" : "非工作日");

		if (day != null && !day.isEmpty())
			text += " 每周工作: " + day;

		if (from1 != null)
			text += " 上午上班: " + new SimpleDateFormat(Util.DATE_FORMAT_TIME).format(from1);
		if (to1 != null)
			text += " 上午下班: " + new SimpleDateFormat(Util.DATE_FORMAT_TIME).format(to1);
		if (from2 != null)
			text += " 下午上班: " + new SimpleDateFormat(Util.DATE_FORMAT_TIME).format(from2);
		if (to2 != null)
			text += " 下午下班: " + new SimpleDateFormat(Util.DATE_FORMAT_TIME).format(to2);
		if (from3 != null)
			text += " 晚班上班: " + new SimpleDateFormat(Util.DATE_FORMAT_TIME).format(from3);
		if (to3 != null)
			text += " 晚班下班" + new SimpleDateFormat(Util.DATE_FORMAT_TIME).format(to3);

		return text.trim();
	}

	public WorkTime set_id(ObjectId _id) {
		this._id = _id;
		return this;
	}

	public ObjectId get_id() {
		return _id;
	}
}
