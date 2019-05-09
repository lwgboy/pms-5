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

		Optional.ofNullable(data.getString("subject")).ifPresent(s -> RenderTools.appendLabelAndTextLine(sb, "˵����", s));
		RenderTools.appendLabelAndTextLine(sb, "���̣�", RenderTools.shortDateTime(processInstance.getDate("startDate")));
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
		if ("Created".equals(status)) {//���񴴽�����û��ָ��owner�����
			action.add(new String[] { "nominate", "ָ��" });
			if (Boolean.TRUE.equals(taskData.getBoolean("skipable")))
				action.add(new String[] { "skip", "����" });//
//			action.add(new String[] { "exit", "�˳�" });//
		} else if ("Ready".equals(status)) {//��һ��������ڶ�potential owner��ʱ��
//			action.add(new String[] { "claim", "ǩ��" });//start==claim
			action.add(new String[] { "start", "ǩ��" });//�ڶ��˵�����£���ǰ�û���������ǩ��
			action.add(new String[] { "delegate", "ί��" });//���˵�����ɽ��������˴�������˭�������Ȩ��?TODO
//			action.add(new String[] { "forward", "ת��" });//�˻���Ȼ����Ready״̬�����û������
			// action.add(new String[] { "suspend", "��ͣ" });
			if (Boolean.TRUE.equals(taskData.getBoolean("skipable")))
				action.add(new String[] { "skip", "����" });//
//			action.add(new String[] { "exit", "�˳�" });//
		} else if ("Reserved".equals(status)) {
			action.add(new String[] { "start", "ǩ��" });
			action.add(new String[] { "delegate", "ί��" });//���˵�����ɽ��������˴�������˭�������Ȩ��?TODO
//			action.add(new String[] { "forward", "ת��" });//�˻ص�Ready״̬û������
			// action.add(new String[] { "suspend", "��ͣ" });
			if (Boolean.TRUE.equals(taskData.getBoolean("skipable")))
				action.add(new String[] { "skip", "����" });//
			// action.add(new String[] { "exit", "�˳�" });//
		} else if ("InProgress".equals(status)) {
			action.add(new String[] { "complete", "�ύ" });
//			 action.add(new String[] { "stop", "ͣ��" });//ͣ��ֻ�ܽ�InProgress������תΪReserved��û��ʵ�����塣
			action.add(new String[] { "delegate", "ί��" });//���˵�����ɽ��������˴�������˭�������Ȩ��?TODO
			// action.add(new String[] { "suspend", "��ͣ" });
			if (Boolean.TRUE.equals(taskData.getBoolean("skipable")))
				action.add(new String[] { "skip", "����" });//
//			action.add(new String[] { "exit", "�˳�" });//
		} else if ("Suspended".equals(status)) {
//			action.add(new String[] { "resume", "����" });
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
