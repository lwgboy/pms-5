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

		Optional.ofNullable(data.getString("description")).ifPresent(s -> RenderTools.appendLabelAndTextLine(sb, "������", s));

		RenderTools.appendLabelAndTextLine(sb, "���̣�", RenderTools.shortDateTime(processInstance.getDate("startDate")) + " S/N��"
				+ String.format("%08d", taskData.getLong("processInstanceId")));
		RenderTools.appendLabelAndTextLine(sb, "ǩ����", RenderTools.shortDateTime(taskData.getDate("createdOn")));

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
		if ("Created".equals(status)) {// ���񴴽�����û��ָ��owner�����
			appendNominateAction(action);
			appendSkipAction(action);
		} else if ("Ready".equals(status)) {// ��һ��������ڶ�potential owner��ʱ��
			appendStartAction(action);
			appendDelegateAction(action);
			appendSkipAction(action);
		} else if ("Reserved".equals(status)) {
			appendStartAction(action);
			appendDelegateAction(action);
			appendSkipAction(action);
		} else if ("InProgress".equals(status)) {
			appendCompleteAction(action);
			// action.add(new String[] { "stop", "ͣ��"
			// });//ͣ��ֻ�ܽ�InProgress������תΪReserved��û��ʵ�����塣
			// ����ж��Ǳ��ִ����
			appendDelegateAction(action);
			// action.add(new String[] { "suspend", "��ͣ" });
			appendSkipAction(action);
			// action.add(new String[] { "exit", "�˳�" });//
		} else if ("Suspended".equals(status)) {
			// action.add(new String[] { "resume", "����" });
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
		action.add(new String[] { "nominate", "ָ��" });
	}

	private void appendCompleteAction(List<String[]> action) {
		action.add(new String[] { "complete", "�ύ" });
	}

	private void appendStartAction(List<String[]> action) {
		action.add(new String[] { "start", "ǩ��" });
	}

	private void appendSkipAction(List<String[]> action) {
		if (Boolean.TRUE.equals(taskData.getBoolean("skipable")))
			action.add(new String[] { "skip", "����" });//
	}

	private void appendDelegateAction(List<String[]> action) {
		// ���˵�����ɽ��������˴�������˭�������Ȩ��:
		// 1. ���ǲ��ž������ί�ɸò��ŵĳ�Ա
		// 2. ���ǲ��ŵĹ����߿���ί�����ò��ŵĳ�Ա
		// 3. ����һ��ί����Ȩ��
		boolean isManager = c("organization",domain).countDocuments(new Document("managerId", userId)) == 1;
		if (!isManager)
			isManager = c("funcPermission",domain).countDocuments(
					new Document("id", userId).append("role", new Document("$in", Arrays.asList("���ž���", "ί����"))).append("type", "�û�")) == 1;
		if (isManager)
			action.add(new String[] { "delegate", "ί��" });
	}

}
