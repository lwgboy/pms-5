package com.bizvisionsoft.serviceimpl.renderer;

import org.bson.Document;

import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.service.tools.Formatter;

public class ProjectWorkRenderer extends AbstractRenderer {

	public static Document renderingUnAssignmentWorkCard(Work work, String userid) {
		Document doc = new Document();
		int rowHeight = 220;
		CardTheme theme = new CardTheme(work);

		StringBuffer sb = new StringBuffer();
		// ��ʾҳǩ
		int margin = 8;
		sb.append("<div class='brui_card' style='height:" + (rowHeight - 2 * margin) + "px;margin:" + margin + "px;'>");

		renderTitle(theme, sb, work, work.getPlanFinish());

		// ��ʾ��һ����Ϣ
		Check.isAssigned(work.getStageName(), s -> renderIconTextLine(sb, s, "img/task_c.svg", theme.emphasizeText));

		// ��ʾ�ƻ���ʼ�ͼƻ����
		String text = "�ƻ���" + Formatter.getString(work.getPlanStart()) + "~" + Formatter.getString(work.getPlanFinish());
		renderIconTextLine(sb, text, "img/calendar_c.svg", theme.emphasizeText);

		// ��ʾ��������ָ�ɹ���
		renderButtons(theme, sb, work, Check.equals(userid, work.getChargerId()), "ָ��", "assignWork/" + work.get_id());

		// ��ǩ
		renderNoticeBudgets(sb, work);
		sb.append("</div>");

		doc.put("height", rowHeight);
		doc.put("html", sb.toString());
		doc.put("_id", work.get_id());
		return doc;
	}

	public static Document renderingFinishedWorkCard(Work work) {
		Document doc = new Document();
		int rowHeight = 172;
		CardTheme theme = new CardTheme("deepGrey");

		StringBuffer sb = new StringBuffer();
		// ��ʾҳǩ
		int margin = 8;
		sb.append("<div class='brui_card' style='height:" + (rowHeight - 2 * margin) + "px;margin:" + margin + "px;'>");

		renderTitle(theme, sb, work, work.getActualFinish());

		Check.isAssigned(work.getStageName(), s -> renderIconTextLine(sb, s, "img/task_c.svg", theme.emphasizeText));

		// ��ʾ�ƻ���ʼ�ͼƻ����
		String text = "�ƻ���" + Formatter.getString(work.getPlanStart()) + "~" + Formatter.getString(work.getPlanFinish()) + "�������"
				+ Formatter.getString(work.getActualFinish());
		renderIconTextLine(sb, text, "img/calendar_c.svg", theme.emphasizeText);

		// ����������
		renderCharger(theme, sb, work);

		sb.append("</div>");
		doc.put("height", rowHeight);
		doc.put("html", sb.toString());
		doc.put("_id", work.get_id());
		return doc;
	}

	/**
	 * ��Ⱦ�����еĹ���
	 * 
	 * @param work
	 * @return
	 */
	public static Document renderingExecutingWorkCard(Work work, String userid) {
		Document doc = new Document();
		int rowHeight = 374;
		CardTheme theme = new CardTheme(work);

		StringBuffer sb = new StringBuffer();
		// ��ʾҳǩ
		int margin = 8;
		sb.append("<div class='brui_card' style='height:" + (rowHeight - 2 * margin) + "px;margin:" + margin + "px;'>");

		renderTitle(theme, sb, work, work.getPlanFinish());

		Check.isAssigned(work.getStageName(), s -> renderIconTextLine(sb, s, "img/task_c.svg", theme.emphasizeText));

		// ��ʾ�ƻ���ʼ�ͼƻ����
		String text = "�ƻ���" + Formatter.getString(work.getPlanStart()) + "~" + Formatter.getString(work.getPlanFinish()) + "����ʼ��"
				+ Formatter.getString(work.getActualStart());
		renderIconTextLine(sb, text, "img/calendar_c.svg", theme.emphasizeText);

		// ����������
		renderCharger(theme, sb, work);

		// ��ʾ����ָ��
		renderIndicators(theme, sb, "����", work.getWAR(), "����", work.getDAR());

		// ��ʾ����������ɹ���
		renderButtons(theme, sb, work, Check.equals(userid, work.getChargerId()), "���", "finishWork/" + work.get_id());

		// ��ǩ
		renderNoticeBudgets(sb, work);

		sb.append("</div>");

		doc.put("height", rowHeight);
		doc.put("html", sb.toString());
		doc.put("_id", work.get_id());
		return doc;
	}

	/**
	 * ��Ⱦ�Ѽƻ��Ĺ���
	 * 
	 * @param work
	 * @return
	 */
	public static Document renderingPlannedWorkCard(Work work, String userid) {
		Document doc = new Document();
		int rowHeight = 247;

		CardTheme theme = new CardTheme(work);

		StringBuffer sb = new StringBuffer();
		// ��ʾҳǩ
		int margin = 8;
		sb.append("<div class='brui_card' style='height:" + (rowHeight - 2 * margin) + "px;margin:" + margin + "px;'>");

		renderTitle(theme, sb, work, work.getPlanStart());

		Check.isAssigned(work.getStageName(), s -> renderIconTextLine(sb, s, "img/task_c.svg", theme.emphasizeText));

		// ��ʾ�ƻ���ʼ�ͼƻ����
		String text = "�ƻ���" + Formatter.getString(work.getPlanStart()) + "~" + Formatter.getString(work.getPlanFinish());
		renderIconTextLine(sb, text, "img/calendar_c.svg", theme.emphasizeText);

		// ����������
		renderCharger(theme, sb, work);

		// ��ʾ�������Ϳ�ʼ����
		renderButtons(theme, sb, work, Check.equals(userid, work.getChargerId()), "��ʼ", "startWork/" + work.get_id());

		// ��ǩ
		renderNoticeBudgets(sb, work);

		sb.append("</div>");

		doc.put("height", rowHeight);
		doc.put("html", sb.toString());
		doc.put("_id", work.get_id());
		return doc;
	}
}
