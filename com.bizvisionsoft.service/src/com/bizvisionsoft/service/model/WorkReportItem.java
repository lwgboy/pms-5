package com.bizvisionsoft.service.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Persistence;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.mongocodex.SetValue;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

@PersistenceCollection("workReportItem")
public class WorkReportItem {

	@WriteValue
	@ReadValue
	private ObjectId _id;

	@WriteValue
	@ReadValue
	private ObjectId work_id;

	@WriteValue
	@ReadValue
	private ObjectId report_id;

	@SetValue
	private Work work;

	@WriteValue
	@ReadValue
	private Date estimatedFinish;

	@WriteValue
	@ReadValue
	private String statement;

	@WriteValue
	@ReadValue
	private String problems;

	@WriteValue
	@ReadValue
	private String pmRemark;

	@Persistence
	private String reporter;

	@ReadValue("reportWorkPlan")
	private String getReportWorkPlanHtml() {
		StringBuffer sb = new StringBuffer();
		sb.append("<div class='label_title'>" + work.getFullName() + "</div>");
		sb.append("<div>计划: " + formatText(work.getPlanStart()) + " ~ " + formatText(work.getPlanFinish()) + "</div>");
		sb.append("<div>实际: " + formatText(work.getActualStart()) + " ~ " + formatText(work.getActualFinish())
				+ "</div>");

		return sb.toString();
	}

	private String formatText(Date date) {
		return Optional.ofNullable(date).map(d -> new SimpleDateFormat("yyyy-MM-dd").format(d)).orElse("");
	}

	@ReadValue("reportWorkStatement")
	private String getReportWorkStatementHtml() {
		if (statement == null) {
			return "";
		}
		return "<div>" + statement + "</div>";
	}

	@ReadValue("reportProblems")
	private String getReportProblemsHtml() {
		if (problems == null) {
			return "";
		}
		return "<div>" + problems + "</div>";
	}

	@ReadValue("reportPMRemark")
	private String getReportPMRemarkHtml() {
		if (pmRemark == null) {
			return "";
		}
		return "<div>" + pmRemark + "</div>";
	}

	@Label
	public String toString() {
		return work.getFullName();
	}
}
