package com.bizvisionsoft.serviceimpl.renderer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.bson.Document;

import com.bizvisionsoft.service.tools.CardTheme;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.service.tools.Formatter;
import com.bizvisionsoft.serviceimpl.BasicServiceImpl;

public class ProcessTaskCardRenderer extends BasicServiceImpl {

	private static final CardTheme theme = new CardTheme(CardTheme.INDIGO);

	private Document data;

	private String lang;

	private Document processInstance;

	private Document processMeta;

	private Document taskData;

	private String userId;

	private String domain;

	public ProcessTaskCardRenderer(Document data, String userId, String lang,String domain) {
		this.data = data;
		this.userId = userId;
		this.lang = lang;
		this.domain = domain;
		processInstance = (Document) data.get("processInstance");
		processMeta = (Document) processInstance.get("meta");
		taskData = (Document) data.get("taskData");
	}

	public static Document renderTasksAssignedAsPotentialOwner(Document data, String userId, String lang,String domain) {
		return new ProcessTaskCardRenderer(data, userId, lang,domain).renderTasksAssignedAsPotentialOwner();
	}

	private Document renderTasksAssignedAsPotentialOwner() {

		StringBuffer sb = new StringBuffer();
		String title = data.getString("name");

		String processSubject = processMeta.getString("proc_subject");
		String processName = processMeta.getString("name");
		String processDesc = processMeta.getString("proc_desc");
		String processTitle = Arrays.asList(processSubject, processName, processDesc).stream().filter(Check::isAssigned).findFirst()
				.orElse("");

		String status = taskData.getString("status");
		String statusText = Formatter.getStatusText(status, lang);
		RenderTools.appendHeadof2LineWithStatus(sb, title, processTitle, statusText, theme);

		Optional.ofNullable(data.getString("description")).ifPresent(s -> RenderTools.appendLabelAndTextLine(sb, "描述：", s));

		RenderTools.appendLabelAndTextLine(sb, "流程：", RenderTools.shortDateTime(processInstance.getDate("startDate")) + " S/N："
				+ String.format("%08d", taskData.getLong("processInstanceId")));
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
		if ("Created".equals(status)) {// 任务创建，但没有指定owner的情况
			appendNominateAction(action);
			appendSkipAction(action);
		} else if ("Ready".equals(status)) {// 当一个任务存在多potential owner的时候
			appendStartAction(action);
			appendDelegateAction(action);
			appendSkipAction(action);
		} else if ("Reserved".equals(status)) {
			appendStartAction(action);
			appendDelegateAction(action);
			appendSkipAction(action);
		} else if ("InProgress".equals(status)) {
			appendCompleteAction(action);
			// action.add(new String[] { "stop", "停办"
			// });//停办只能将InProgress的任务转为Reserved，没有实际意义。
			// 如果有多个潜在执行者
			appendDelegateAction(action);
			// action.add(new String[] { "suspend", "暂停" });
			appendSkipAction(action);
			// action.add(new String[] { "exit", "退出" });//
		} else if ("Suspended".equals(status)) {
			// action.add(new String[] { "resume", "继续" });
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

	private void appendNominateAction(List<String[]> action) {
		action.add(new String[] { "nominate", "指派" });
	}

	private void appendCompleteAction(List<String[]> action) {
		action.add(new String[] { "complete", "提交" });
	}

	private void appendStartAction(List<String[]> action) {
		action.add(new String[] { "start", "签收" });
	}

	private void appendSkipAction(List<String[]> action) {
		if (Boolean.TRUE.equals(taskData.getBoolean("skipable")))
			action.add(new String[] { "skip", "跳过" });//
	}

	private void appendDelegateAction(List<String[]> action) {
		// 多人的情况可交由其他人处理，但是谁具有这个权限:
		// 1. 考虑部门经理可以委派该部门的成员
		// 2. 考虑部门的管理者可以委派至该部门的成员
		// 3. 考虑一个委派者权限
		boolean isManager = c("organization",domain).countDocuments(new Document("managerId", userId)) == 1;
		if (!isManager)
			isManager = c("funcPermission",domain).countDocuments(
					new Document("id", userId).append("role", new Document("$in", Arrays.asList("部门经理", "委派者"))).append("type", "用户")) == 1;
		if (isManager)
			action.add(new String[] { "delegate", "委派" });
	}

}
