package com.bizvisionsoft.service.model;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

public class WorkReportSummary {

	// private Date reportDate;

	// private String type;

	private ObjectId _id;

	private String reportBy;

	private String projectName;

	private String projectId;

	private String stageName;

	private String workName;

	private String statement;

	private String problems;

	private String pmRemark;

	private Date estimatedFinish;

	private Date workPlanFinish;

	@ReadValue
	@WriteValue
	private List<String> tags;

	@ReadValue("plan")
	private String readPlanHtml() {
		StringBuffer sb = new StringBuffer();
		sb.append("<div class='label_subhead'>" + projectName + " [" + projectId + "]</div>");
		sb.append("<div>" + stageName + " / " + workName + "</div>");
		sb.append("<div> ������:" + reportBy );

		if (estimatedFinish != null) {
			int days = (int) ((estimatedFinish.getTime() - workPlanFinish.getTime()) / (1000 * 24 * 60 * 60));
			sb.append("Ԥ�ƣ�"
					+ (days > 0 ? ("<span class='layui-badge'>����" + Math.abs(days) + "��</span>")
							: ("<span class='layui-badge layui-bg-green'>��ǰ" + Math.abs(days) + "��</span>"))
					+ "");
		}
		sb.append("</div>");
		sb.append("<div style='margin-top:16px;display:flex;justify-content:flex-start;align-items:center;'>");
		sb.append("<a href='tag' target='_rwt'><i class='layui-icon layui-icon-note' style='color:#3f51b5;'></i></a>");
		if (tags != null && tags.size() > 0) {
			tags.forEach(t -> {
				sb.append("<span class='layui-badge layui-bg-orange' style='margin-left:8px;'>" + t + "</span>");
			});
		}
		sb.append("</div>");
		return sb.toString();
	}

	@ReadValue("statement")
	private String readStatement() {
		return getHtml(statement, "statement");
	}

	private String getHtml(String text, String key) {
		StringBuffer sb = new StringBuffer();
		// ����и�64����ȥ��������8px
		sb.append("<div style='height:60px;display:block;margin-top:20px'>");

		sb.append("<div style='height:60px;" // 3�����ָ߶�
				+ "white-space:normal; word-break:break-all;" //
				+ "text-overflow: ellipsis;"//
				+ "text-overflow:-o-ellipsis-lastline;"//
				+ "overflow: hidden;"//
				+ "display: -webkit-box;"//
				+ "-webkit-box-orient:vertical;"//
				+ "-webkit-line-clamp:3;"// �ȸ�������ʾʡ�Ժ�
				+ "'>"
				+ Optional.ofNullable(text).map(t -> "<a href='" + key + "' target='_rwt'>" + t + "</a>").orElse("")
				+ "</div>");
		return sb.toString();
	}

	@ReadValue("problems")
	private String readProblems() {
		return getHtml(problems, "problems");
	}

	@ReadValue("pmRemark")
	private String readRemark() {
		return getHtml(pmRemark, "pmRemark");
	}

	public String getPmRemark() {
		return pmRemark;
	}

	public String getProblems() {
		return problems;
	}

	public String getStatement() {
		return statement;
	}

	public List<String> getTags() {
		return tags;
	}

	public ObjectId get_id() {
		return _id;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

}
