package com.bizvisionsoft.serviceimpl.renderer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.Document;

import com.bizvisionsoft.service.model.TrackView;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.service.tools.Formatter;
import com.bizvisionsoft.service.tools.MetaInfoWarpper;

public class WorkRenderer {

	public static Document renderingUnAssignmentWorkCard(Work work) {
		Document doc = new Document();
		int rowHeight = 220;
		CardTheme theme = new CardTheme(work);

		StringBuffer sb = new StringBuffer();
		// ��ʾҳǩ
		int margin = 8;
		sb.append("<div class='brui_card' style='height:" + (rowHeight - 2 * margin) + "px;margin:" + margin + "px;'>");

		renderTitle(theme, sb, work, work.getPlanFinish());

		// ��ǩ
		// renderNoticeBudgets(work, sb);

		// ��ʾ��һ����Ϣ
		renderProjectLine(theme, sb, work);

		// ��ʾ�ƻ���ʼ�ͼƻ����
		String text = "�ƻ���" + Formatter.getString(work.getPlanStart()) + "~" + Formatter.getString(work.getPlanFinish());
		renderIconTextLine(sb, text, "img/calendar_c.svg", theme.emphasizeText);

		// ��ʾ��������ָ�ɹ���
		renderButtons(theme, sb, work, "ָ��", "assignWork/" + work.get_id());

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

		// ��ʾ��һ����Ϣ
		renderProjectLine(theme, sb, work);

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
	public static Document renderingExecutingWorkCard(Work work) {
		Document doc = new Document();
		int rowHeight = 374;
		CardTheme theme = new CardTheme(work);

		StringBuffer sb = new StringBuffer();
		// ��ʾҳǩ
		int margin = 8;
		sb.append("<div class='brui_card' style='height:" + (rowHeight - 2 * margin) + "px;margin:" + margin + "px;'>");

		renderTitle(theme, sb, work, work.getPlanFinish());

		// ��ʾ��һ����Ϣ
		renderProjectLine(theme, sb, work);

		// ��ʾ�ƻ���ʼ�ͼƻ����
		String text = "�ƻ���" + Formatter.getString(work.getPlanStart()) + "~" + Formatter.getString(work.getPlanFinish()) + "����ʼ��"
				+ Formatter.getString(work.getActualStart());
		renderIconTextLine(sb, text, "img/calendar_c.svg", theme.emphasizeText);

		// ����������
		renderCharger(theme, sb, work);

		// ��ʾ����ָ��
		renderIndicators(theme, sb, "����", work.getWAR(), "����", work.getDAR());

		// ��ʾ����������ɹ���
		renderButtons(theme, sb, work, "���", "finishWork/" + work.get_id());

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
	public static Document renderingPlannedWorkCard(Work work) {
		Document doc = new Document();
		int rowHeight = 247;

		CardTheme theme = new CardTheme(work);

		StringBuffer sb = new StringBuffer();
		// ��ʾҳǩ
		int margin = 8;
		sb.append("<div class='brui_card' style='height:" + (rowHeight - 2 * margin) + "px;margin:" + margin + "px;'>");

		renderTitle(theme, sb, work, work.getPlanStart());

		// ��ʾ��һ����Ϣ
		renderProjectLine(theme, sb, work);

		// ��ʾ�ƻ���ʼ�ͼƻ����
		String text = "�ƻ���" + Formatter.getString(work.getPlanStart()) + "~" + Formatter.getString(work.getPlanFinish());
		renderIconTextLine(sb, text, "img/calendar_c.svg", theme.emphasizeText);

		// ����������
		renderCharger(theme, sb, work);

		// ��ʾ�������Ϳ�ʼ����
		renderButtons(theme, sb, work, "��ʼ", "startWork/" + work.get_id());

		// ��ǩ
		renderNoticeBudgets(sb, work);

		sb.append("</div>");

		doc.put("height", rowHeight);
		doc.put("html", sb.toString());
		doc.put("_id", work.get_id());
		return doc;
	}

	private static void renderTitle(CardTheme theme, StringBuffer sb, Work work, Date date) {
		String name = work.getFullName();
		String _date = Formatter.getString(date, "M/d");
		sb.append("<div class='label_title brui_card_head' style='background:#" + theme.headBgColor + ";color:#" + theme.headFgColor
				+ ";padding:8px'>" + "<div style='word-break:break-word;white-space:pre-line;'>" + name
				+ "</div><div style='font-size:36px;'>" + _date + "</div></div>");
	}

	private static void renderIconTextLine(StringBuffer sb, String text, String icon, String color) {
		sb.append("<div style='padding:8px 8px 0px 8px;display:flex;align-items:center;'><img src='" + getResourceURL(icon)
				+ "' width='20' height='20'><div class='label_caption brui_text_line' style='color:#" + color
				+ ";margin-left:8px;width:100%'>" + text + "</div></div>");
	}

	private static void renderProjectLine(CardTheme theme, StringBuffer sb, Work work) {
		sb.append("<div style='padding-left:8px;padding-top:8px;display:flex;align-items:center;'><img src='"
				+ getResourceURL("img/project_c.svg")
				+ "' width='20' height='20'><a href='openProject/' target='_rwt' class='label_caption brui_text_line' style='color:#"
				+ theme.lightText + ";margin-left:8px;width:100%'>��Ŀ��" + work.getProjectName() + "</a></div>");
	}

	private static void renderCharger(CardTheme theme, StringBuffer sb, Work work) {
		renderUser(sb, work, "����", work.warpperChargerInfo(), theme.emphasizeText);
	}

	private static void renderUser(StringBuffer sb, Work work, String title, String text, String color) {
		sb.append("<div style='padding-left:8px;padding-top:8px;display:flex;align-items:center;'><img src='"
				+ getResourceURL("img/user_c.svg") + "' width='20' height='20'><div class='label_caption brui_text_line' style='color:#"
				+ color + ";margin-left:8px;width:100%;display:flex;'>" + title + "��<span style='cursor:pointer;'>" + text
				+ "</span></div></div>");
	}

	private static String getResourceURL(String url) {
		return "rwt-resources/extres/" + url;
	}

	private static void renderNoticeBudgets(StringBuffer sb, Work work) {
		sb.append("<div style='margin-top:8px;padding:4px;display:flex;width:100%;justify-content:flex-end;align-items:center;'>");
		Double value = work.getTF();
		if (value != null) {
			String label = "<div class='layui-badge-rim' style='margin-right:4px;cursor:pointer;'>TF " + (int) Math.ceil(value) + "</div>";
			sb.append(MetaInfoWarpper.warpper(label,
					"��ʱ�TF����<br>�ڲ�Ӱ���ܹ��ڵ�ǰ���£��������������õĻ���ʱ�䣬����������ٿ�ʼʱ�������翪ʼʱ��֮��������ʱ���ӳ������ĳ���ʱ����Ƴ��俪��ʱ�䣬����Ӱ��ƻ����ܹ��ڡ�", 3000));
		}

		value = work.getFF();
		if (value != null) {
			String label = "<div class='layui-badge-rim' style='margin-right:4px;cursor:pointer;'>FF " + (int) Math.ceil(value) + "</div>";
			sb.append(
					MetaInfoWarpper.warpper(label, "����ʱ�FF����<br>�ڲ�Ӱ������������翪ʼʱ��������£��������������õĻ���ʱ�䡣���ù��������н����������翪ʼʱ�䣬��ȥ�ù������������ʱ�䡣", 3000));
		}

		value = work.getTF();
		if (value != null && value.doubleValue() == 0) {
			String label = "<div class='layui-badge-rim' style='margin-right:4px;cursor:pointer;'>CP</div>";
			sb.append(MetaInfoWarpper.warpper(label, "������������Ŀ�ؼ�·��", 3000));
		}

		Check.isAssigned(work.getManageLevel(), l -> {
			if ("1".equals(l)) {
				String label = "<div class='layui-badge-rim' style='margin-right:4px;cursor:pointer;'>&#8544;</div>";
				sb.append(MetaInfoWarpper.warpper(label, "����һ��1��������Ĺ�����", 3000));
			}

			if ("2".equals(l)) {
				String label = "<div class='layui-badge-rim' style='margin-right:4px;cursor:pointer;'>&#8545;</div>";
				sb.append(MetaInfoWarpper.warpper(label, "����һ��2��������Ĺ�����", 3000));
			}
		});
		// ����
		Check.isAssigned(work.getWarningIcon(), sb::append);
		//

		sb.append("</div>");
	}

	private static void renderButtons(CardTheme theme, StringBuffer sb, Work work, String label, String href) {
		renderButtons(theme, sb, work, true, label, href);
	}

	private static void renderButtons(CardTheme theme, StringBuffer sb, Work work, boolean showActionButton, String label, String href) {
		List<TrackView> wps = work.getWorkPackageSetting();
		List<String[]> btns = new ArrayList<>();
		if (Check.isNotAssigned(wps)) {
			btns.add(new String[] { "openWorkPackage/default", "������" });
		} else if (wps.size() == 1) {
			btns.add(new String[] { "openWorkPackage/0", wps.get(0).getName() });
		} else {
			for (int i = 0; i < wps.size(); i++) {
				btns.add(new String[] { "openWorkPackage/" + i, wps.get(i).getName() });
			}
		}
		// sb.append("<div
		// style='margin-top:12px;width:100%;background:#d0d0d0;height:1px;'></div>");
		sb.append("<div style='margin-top:16px;padding:4px;display:flex;width:100%;justify-content:space-around;align-items:center;'>");
		btns.forEach(e -> {
			sb.append("<a class='label_card' href='" + e[0] + "' target='_rwt'>" + e[1] + "</a>");
		});
		if (showActionButton)
			sb.append(
					"<a class='label_card' style='color:#" + theme.headBgColor + ";' href='" + href + "' target='_rwt'>" + label + "</a>");
		sb.append("</div>");
	}

	private static void renderIndicators(CardTheme theme, StringBuffer sb, String label1, double ind1, String label2, double ind2) {
		sb.append("<div style='padding:4px;display:flex;width:100%;justify-content:space-evenly;align-items:center;'>");

		sb.append("<div><img src='/bvs/svg?type=progress&percent=" + ind1 + "&bgColor=" + theme.contrastBgColor + "&fgColor="
				+ theme.contrastFgColor + "' width=100 height=100/>");
		sb.append("<div class='label_body1' style='text-align:center;color:#9e9e9e'>" + label1 + "</div></div>");

		sb.append("<div style='background:#d0d0d0;width:1px;height:80px'></div>");
		sb.append("<div><img src='/bvs/svg?type=progress&percent=" + ind2 + "&bgColor=" + theme.contrastBgColor + "&fgColor="
				+ theme.contrastFgColor + "' width=100 height=100/>");
		sb.append("<div class='label_body1' style='text-align:center;color:#9e9e9e'>" + label2 + "</div></div>");

		sb.append("</div>");
	}

}
