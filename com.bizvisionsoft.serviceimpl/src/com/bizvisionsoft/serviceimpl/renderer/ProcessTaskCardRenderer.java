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

		RenderTools.appendLabelAndTextLine(sb, "���̣�", processMeta.getString("name"));
		// Date date = processInstance.getDate("startDate");
		// String userName = creationInfo.getString("userName");
		// RenderTools.appendLabelAndTextLine(sb, "��ʼ��", Formatter.getString(date) + " "
		// + userName);

		Optional.ofNullable(data.getString("subject"))
				.ifPresent(s -> RenderTools.appendLabelAndMultiLine(sb, "˵����", "deep_orange", s, CardTheme.TEXT_LINE));
		String status = taskData.getString("status");
		renderStatusAndActions(sb, status);

		RenderTools.appendCardBg(sb);
		return new Document("_id", data.get("_id")).append("html", sb.toString());
	}

	private void renderStatusAndActions(StringBuffer sb, String status) {

		String text;
		List<String[]> action = new ArrayList<>();
		if ("Created".equals(status)) {
			text = "���� " + Formatter.getString(taskData.getDate("createdOn"));
			action.add(new String[] { "nominate", "ָ��" });
			if(Boolean.TRUE.equals(taskData.getBoolean("skipable"))) 
				action.add(new String[] { "skip", "����" });//
			action.add(new String[] { "exit", "�˳�" });//
		} else if ("Ready".equals(status)) {
			text = "׼��";
			action.add(new String[] { "claim", "����" });
			action.add(new String[] { "delegate", "ί��" });
			action.add(new String[] { "start", "��ʼ" });
			action.add(new String[] { "forward", "�˻�" });
//			action.add(new String[] { "suspend", "��ͣ" });
			if(Boolean.TRUE.equals(taskData.getBoolean("skipable"))) 
				action.add(new String[] { "skip", "����" });//
			action.add(new String[] { "exit", "�˳�" });//
		} else if ("Reserved".equals(status)) {
			text = "Ԥ��";
			action.add(new String[] { "start", "��ʼ" });
			action.add(new String[] { "forward", "�˻�" });
			action.add(new String[] { "delegate", "ί��" });
//			action.add(new String[] { "suspend", "��ͣ" });
			if(Boolean.TRUE.equals(taskData.getBoolean("skipable"))) 
				action.add(new String[] { "skip", "����" });//
			action.add(new String[] { "exit", "�˳�" });//
		} else if ("InProgress".equals(status)) {
			text = "����";
			action.add(new String[] { "complete", "���" });
			action.add(new String[] { "stop", "ֹͣ" });
			action.add(new String[] { "delegate", "ί��" });
//			action.add(new String[] { "suspend", "��ͣ" });
			if(Boolean.TRUE.equals(taskData.getBoolean("skipable"))) 
				action.add(new String[] { "skip", "����" });//
			action.add(new String[] { "exit", "�˳�" });//
		} else if ("Suspended".equals(status)) {
			text = "��ͣ";
			action.add(new String[] { "resume", "����" });
		} else if ("Completed".equals(status)) {
			text = "���";
		} else if ("Failed".equals(status)) {
			text = "ʧ��";
		} else if ("Error".equals(status)) {
			text = "����";
		} else if ("Exited".equals(status)) {
			text = "�˳�";
		} else if ("Obsolete".equals(status)) {
			text = "����";
		} else {
			return;
		}

		RenderTools.appendLabelAndTextLine(sb, "״̬��", text);
		if (action.isEmpty())
			return;

		sb.append("<div class='brui_line_padding' style='display:flex;width:100%;justify-content:space-around;align-items:center;'>");
		action.forEach(e -> {
			sb.append("<a class='' href='" + e[0] + "' target='_rwt'>" + e[1] + "</a>");
		});
		sb.append("</div>");

	}

}
