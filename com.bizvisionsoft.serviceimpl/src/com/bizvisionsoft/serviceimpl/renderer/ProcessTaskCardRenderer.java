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

	public ProcessTaskCardRenderer(Document data, String lang) {
		this.data = data;
		this.lang = lang;
		processInstance = (Document) data.get("processInstance");
		processMeta = (Document) processInstance.get("meta");
		taskData = (Document) data.get("taskData");
	}

	public static Document renderTasksAssignedAsPotentialOwner(Document data, String lang) {
		return new ProcessTaskCardRenderer(data, lang).renderTasksAssignedAsPotentialOwner();
	}

	private Document renderTasksAssignedAsPotentialOwner() {

		StringBuffer sb = new StringBuffer();
		String title = data.getString("name");
		String processName = processMeta.getString("name");
		String status = taskData.getString("status");
		String statusText = Formatter.getStatusText(status, lang);
		RenderTools.appendHeadof2LineWithStatus(sb, title, processName, statusText, indigo);

		Optional.ofNullable(data.getString("subject")).ifPresent(s -> RenderTools.appendLabelAndTextLine(sb, "说明：", s));
		RenderTools.appendLabelAndTextLine(sb, "流程：", RenderTools.shortDateTime(processInstance.getDate("startDate")));
		RenderTools.appendLabelAndTextLine(sb, "签发：", RenderTools.shortDateTime(taskData.getDate("createdOn")));

		renderActions(sb, status);

		RenderTools.appendCardBg(sb);
		String content = sb.toString();
		return getData(content);
	}

	private Document getData(String content) {
		return new Document("_id", data.get("_id")).append("html", content).append("task", data.getString("name")).append("process",
				processMeta.getString("name"));
	}

	private void renderActions(StringBuffer sb, String status) {

		List<String[]> action = new ArrayList<>();
		if ("Created".equals(status)) {//任务创建，但没有指定owner的情况
			action.add(new String[] { "nominate", "指派" });
			if (Boolean.TRUE.equals(taskData.getBoolean("skipable")))
				action.add(new String[] { "skip", "跳过" });//
//			action.add(new String[] { "exit", "退出" });//
		} else if ("Ready".equals(status)) {//当一个任务存在多potential owner的时候
//			action.add(new String[] { "claim", "签收" });//start==claim
			action.add(new String[] { "start", "签收" });//在多人的情况下，当前用户可以主动签收
			action.add(new String[] { "delegate", "委派" });//多人的情况可交由其他人处理，但是谁具有这个权限?TODO
//			action.add(new String[] { "forward", "转发" });//退回仍然还是Ready状态，这个没有意义
			// action.add(new String[] { "suspend", "暂停" });
			if (Boolean.TRUE.equals(taskData.getBoolean("skipable")))
				action.add(new String[] { "skip", "跳过" });//
//			action.add(new String[] { "exit", "退出" });//
		} else if ("Reserved".equals(status)) {
			action.add(new String[] { "start", "签收" });
			action.add(new String[] { "delegate", "委派" });//多人的情况可交由其他人处理，但是谁具有这个权限?TODO
//			action.add(new String[] { "forward", "转发" });//退回到Ready状态没有意义
			// action.add(new String[] { "suspend", "暂停" });
			if (Boolean.TRUE.equals(taskData.getBoolean("skipable")))
				action.add(new String[] { "skip", "跳过" });//
			// action.add(new String[] { "exit", "退出" });//
		} else if ("InProgress".equals(status)) {
			action.add(new String[] { "complete", "提交" });
//			 action.add(new String[] { "stop", "停办" });//停办只能将InProgress的任务转为Reserved，没有实际意义。
			action.add(new String[] { "delegate", "委派" });//多人的情况可交由其他人处理，但是谁具有这个权限?TODO
			// action.add(new String[] { "suspend", "暂停" });
			if (Boolean.TRUE.equals(taskData.getBoolean("skipable")))
				action.add(new String[] { "skip", "跳过" });//
//			action.add(new String[] { "exit", "退出" });//
		} else if ("Suspended".equals(status)) {
//			action.add(new String[] { "resume", "继续" });
		} else if ("Completed".equals(status)) {
		} else if ("Failed".equals(status)) {
		} else if ("Error".equals(status)) {
		} else if ("Exited".equals(status)) {
		} else if ("Obsolete".equals(status)) {
		} else {
			return;
		}

		if (action.isEmpty())
			return;

		sb.append("<div class='brui_line_padding' style='display:flex;width:100%;justify-content:space-around;align-items:center;'>");
		action.forEach(e -> {
			sb.append("<a class='' href='" + e[0] + "' target='_rwt'>" + e[1] + "</a>");
		});
		sb.append("</div>");

	}

}
