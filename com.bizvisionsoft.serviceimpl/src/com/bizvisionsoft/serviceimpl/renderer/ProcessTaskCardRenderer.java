package com.bizvisionsoft.serviceimpl.renderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bson.Document;

import com.bizvisionsoft.service.tools.CardTheme;
import com.bizvisionsoft.service.tools.Formatter;

public class ProcessTaskCardRenderer {

	private static final CardTheme indigo = new CardTheme(CardTheme.INDIGO);

	private Document data;

	private String lang;

	private Document processInstance;

	private Document processMeta;

	private Document taskData;

	private Document creationInfo;

	public ProcessTaskCardRenderer(Document data, String lang) {
		this.data = data;
		this.lang = lang;
		processInstance = (Document) data.get("processInstance");
		processMeta = (Document) processInstance.get("meta");
		taskData = (Document) data.get("taskData");
		creationInfo = (Document) processInstance.get("creationInfo");
	}

	public static Document renderTasksAssignedAsPotentialOwner(Document data, String lang) {
		return new ProcessTaskCardRenderer(data, lang).renderTasksAssignedAsPotentialOwner();
	}

	private Document renderTasksAssignedAsPotentialOwner() {

		StringBuffer sb = new StringBuffer();
		RenderTools.appendSingleLineHeader(sb, indigo, data.getString("name"), 36);

		RenderTools.appendLabelAndTextLine(sb, "流程：", processMeta.getString("name"));
		// Date date = processInstance.getDate("startDate");
		// String userName = creationInfo.getString("userName");
		// RenderTools.appendLabelAndTextLine(sb, "开始：", Formatter.getString(date) + " "
		// + userName);

		Optional.ofNullable(data.getString("subject"))
				.ifPresent(s -> RenderTools.appendLabelAndMultiLine(sb, "说明：", "deep_orange", s, CardTheme.TEXT_LINE));
		String status = taskData.getString("status");
		renderStatusAndActions(sb, status);

		RenderTools.appendCardBg(sb);
		return new Document("_id", data.get("_id")).append("html", sb.toString());
	}

	private void renderStatusAndActions(StringBuffer sb, String status) {

		String text;
		List<String[]> action = new ArrayList<>();
		if ("Created".equals(status)) {
			text = "创建 " + Formatter.getString(taskData.getDate("createdOn"));
			action.add(new String[] { "nominate", "指派" });
			if(Boolean.TRUE.equals(taskData.getBoolean("skipable"))) 
				action.add(new String[] { "skip", "跳过" });//
			action.add(new String[] { "exit", "退出" });//
		} else if ("Ready".equals(status)) {
			text = "准备";
			action.add(new String[] { "claim", "认领" });
			action.add(new String[] { "delegate", "委托" });
			action.add(new String[] { "start", "开始" });
			action.add(new String[] { "forward", "退回" });
//			action.add(new String[] { "suspend", "暂停" });
			if(Boolean.TRUE.equals(taskData.getBoolean("skipable"))) 
				action.add(new String[] { "skip", "跳过" });//
			action.add(new String[] { "exit", "退出" });//
		} else if ("Reserved".equals(status)) {
			text = "预留";
			action.add(new String[] { "start", "开始" });
			action.add(new String[] { "forward", "退回" });
			action.add(new String[] { "delegate", "委托" });
//			action.add(new String[] { "suspend", "暂停" });
			if(Boolean.TRUE.equals(taskData.getBoolean("skipable"))) 
				action.add(new String[] { "skip", "跳过" });//
			action.add(new String[] { "exit", "退出" });//
		} else if ("InProgress".equals(status)) {
			text = "进行";
			action.add(new String[] { "complete", "完成" });
			action.add(new String[] { "stop", "停止" });
			action.add(new String[] { "delegate", "委托" });
//			action.add(new String[] { "suspend", "暂停" });
			if(Boolean.TRUE.equals(taskData.getBoolean("skipable"))) 
				action.add(new String[] { "skip", "跳过" });//
			action.add(new String[] { "exit", "退出" });//
		} else if ("Suspended".equals(status)) {
			text = "暂停";
			action.add(new String[] { "resume", "继续" });
		} else if ("Completed".equals(status)) {
			text = "完成";
		} else if ("Failed".equals(status)) {
			text = "失败";
		} else if ("Error".equals(status)) {
			text = "错误";
		} else if ("Exited".equals(status)) {
			text = "退出";
		} else if ("Obsolete".equals(status)) {
			text = "废弃";
		} else {
			return;
		}

		RenderTools.appendLabelAndTextLine(sb, "状态：", text);
		if (action.isEmpty())
			return;

		sb.append("<div class='brui_line_padding' style='display:flex;width:100%;justify-content:space-around;align-items:center;'>");
		action.forEach(e -> {
			sb.append("<a class='' href='" + e[0] + "' target='_rwt'>" + e[1] + "</a>");
		});
		sb.append("</div>");

	}

}
