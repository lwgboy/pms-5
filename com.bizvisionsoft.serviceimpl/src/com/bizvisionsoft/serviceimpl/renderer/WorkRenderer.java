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
		return new WorkRenderer(work).renderingUnAssignmentWorkCard(true, false);
	}

	public static Document renderingFinishedWorkCard(Work work) {
		return new WorkRenderer(work).setTheme(new CardTheme("deepGrey")).renderingFinishedWorkCard(true);
	}

	public static Document renderingExecutingWorkCard(Work work) {
		return new WorkRenderer(work).renderingExecutingWorkCard(true, false);
	}

	public static Document renderingPlannedWorkCard(Work work) {
		return new WorkRenderer(work).renderingPlannedWorkCard(true, false);
	}

	private Work work;

	private CardTheme theme;

	private Document doc;

	public WorkRenderer(Work work) {
		this.work = work;
		theme = new CardTheme(work);
		doc = new Document("_id", work.get_id());
	}

	public WorkRenderer setTheme(CardTheme theme) {
		this.theme = theme;
		return this;
	}

	public Document renderingFinishedWorkCard(boolean showProject) {
		int rowHeight = 172;
		StringBuffer sb = new StringBuffer();
		// ��ʾҳǩ
		int margin = 8;
		sb.append("<div class='brui_card' style='height:" + (rowHeight - 2 * margin) + "px;margin:" + margin + "px;'>");

		sb.append(renderTitle(work.getActualFinish()));

		// ��ʾ��һ����Ϣ
		if (showProject)
			sb.append(renderProjectLine());
		else
			Check.isAssigned(work.getStageName(),
					s -> sb.append(RenderTools.getIconTextLine(s, RenderTools.IMG_URL_TASK, theme.emphasizeText)));

		// ��ʾ�ƻ���ʼ�ͼƻ����
		String text = "�ƻ���" + RenderTools.shortDate(work.getPlanStart()) + "~" + RenderTools.shortDate(work.getPlanFinish()) + "�������"
				+ RenderTools.shortDate(work.getActualFinish());
		sb.append(RenderTools.getIconTextLine(text, RenderTools.IMG_URL_CALENDAR, theme.emphasizeText));

		// ����������
		sb.append(renderCharger());

		sb.append("</div>");

		return doc.append("height", rowHeight).append("html", sb.toString());
	}

	public Document renderingUnAssignmentWorkCard(boolean showProject, boolean showActionButton) {
		int rowHeight = 220;

		StringBuffer sb = new StringBuffer();
		// ��ʾҳǩ
		int margin = 8;
		sb.append("<div class='brui_card' style='height:" + (rowHeight - 2 * margin) + "px;margin:" + margin + "px;'>");
		sb.append(renderTitle(work.getPlanFinish()));

		// ��ʾ��һ����Ϣ
		if (showProject)
			sb.append(renderProjectLine());
		else
			Check.isAssigned(work.getStageName(),
					s -> sb.append(RenderTools.getIconTextLine(s, RenderTools.IMG_URL_TASK, theme.emphasizeText)));

		// ��ʾ�ƻ���ʼ�ͼƻ����
		String text = "�ƻ���" + RenderTools.shortDate(work.getPlanStart()) + "~" + RenderTools.shortDate(work.getPlanFinish());
		sb.append(RenderTools.getIconTextLine(text, RenderTools.IMG_URL_CALENDAR, theme.emphasizeText));

		// ��ʾ��������ָ�ɹ���
		sb.append(renderButtons(showActionButton, "ָ��", "assignWork/" + work.get_id()));

		// ��ǩ
		sb.append(renderNoticeBudgets());
		sb.append("</div>");

		return doc.append("height", rowHeight).append("html", sb.toString());
	}

	/**
	 * ��Ⱦ�����еĹ���
	 * 
	 * @param c
	 * @param b
	 * 
	 * @param work
	 * @return
	 */
	public Document renderingExecutingWorkCard(boolean showProject, boolean showActionButton) {
		int rowHeight = 374;

		StringBuffer sb = new StringBuffer();
		// ��ʾҳǩ
		int margin = 8;
		sb.append("<div class='brui_card' style='height:" + (rowHeight - 2 * margin) + "px;margin:" + margin + "px;'>");

		sb.append(renderTitle(work.getPlanFinish()));

		// ��ʾ��һ����Ϣ
		sb.append(renderProjectLine());

		// ��ʾ�ƻ���ʼ�ͼƻ����
		String text = "�ƻ���" + RenderTools.shortDate(work.getPlanStart()) + "~" + RenderTools.shortDate(work.getPlanFinish()) + "����ʼ��"
				+ RenderTools.shortDate(work.getActualStart());
		sb.append(RenderTools.getIconTextLine(text, RenderTools.IMG_URL_CALENDAR, theme.emphasizeText));

		// ����������
		sb.append(renderCharger());

		// ��ʾ����ָ��
		sb.append(renderIndicators("����", work.getWAR(), "����", work.getDAR()));

		// ��ʾ����������ɹ���
		sb.append(renderButtons("���", "finishWork/" + work.get_id()));

		// ��ǩ
		sb.append(renderNoticeBudgets());

		sb.append("</div>");

		return doc.append("height", rowHeight).append("html", sb.toString());
	}

	/**
	 * ��Ⱦ�Ѽƻ��Ĺ���
	 * 
	 * @param c
	 * @param b
	 * 
	 * @param work
	 * @return
	 */
	public Document renderingPlannedWorkCard(boolean showProject, boolean showActionButton) {
		int rowHeight = 247;

		StringBuffer sb = new StringBuffer();
		// ��ʾҳǩ
		int margin = 8;
		sb.append("<div class='brui_card' style='height:" + (rowHeight - 2 * margin) + "px;margin:" + margin + "px;'>");

		sb.append(renderTitle(work.getPlanStart()));

		// ��ʾ��һ����Ϣ
		sb.append(renderProjectLine());

		// ��ʾ�ƻ���ʼ�ͼƻ����
		String text = "�ƻ���" + RenderTools.shortDate(work.getPlanStart()) + "~" + RenderTools.shortDate(work.getPlanFinish());
		sb.append(RenderTools.getIconTextLine(text, RenderTools.IMG_URL_CALENDAR, theme.emphasizeText));

		// ����������
		sb.append(renderCharger());

		// ��ʾ�������Ϳ�ʼ����
		sb.append(renderButtons("��ʼ", "startWork/" + work.get_id()));

		// ��ǩ
		sb.append(renderNoticeBudgets());

		sb.append("</div>");

		return doc.append("height", rowHeight).append("html", sb.toString());
	}

	private String renderTitle(Date date) {
		String name = work.getFullName();
		String _date = Formatter.getString(date, "M/d");
		return "<div class='label_title brui_card_head' style='background:#" + theme.headBgColor + ";color:#" + theme.headFgColor
				+ ";padding:8px'>" + "<div class='brui_card_text'>" + name + "</div><div class='label_display1'>" + _date + "</div></div>";
	}

	private String renderProjectLine() {
		return "<div style='padding-left:8px;padding-top:8px;display:flex;align-items:center;'><img src='" + RenderTools.IMG_URL_PROJECT
				+ "' width='20' height='20'><a href='openProject/' target='_rwt' class='label_caption brui_text_line' style='color:#"
				+ theme.lightText + ";margin-left:8px;width:100%'>��Ŀ��" + work.getProjectName() + "</a></div>";
	}

	private String renderCharger() {
		return renderUser("����", work.warpperChargerInfo(), theme.emphasizeText);
	}

	private String renderUser(String title, String text, String color) {
		return "<div style='padding-left:8px;padding-top:8px;display:flex;align-items:center;'><img src='" + RenderTools.IMG_URL_USER
				+ "' width='20' height='20'><div class='label_caption brui_text_line' style='color:#" + color
				+ ";margin-left:8px;width:100%;display:flex;'>" + title + "��<span style='cursor:pointer;'>" + text + "</span></div></div>";
	}

	private String renderNoticeBudgets() {
		StringBuffer sb = new StringBuffer();
		sb.append("<div style='margin-top:8px;padding:4px;display:flex;width:100%;justify-content:flex-end;align-items:center;'>");
		Double value = work.getTF();
		if (value != null) {
			String label = "<div class='layui-badge-rim' style='margin-right:4px;'>TF " + (int) Math.ceil(value) + "</div>";
			sb.append(MetaInfoWarpper.warpper(label,
					"��ʱ�TF����<br>�ڲ�Ӱ���ܹ��ڵ�ǰ���£��������������õĻ���ʱ�䣬����������ٿ�ʼʱ�������翪ʼʱ��֮��������ʱ���ӳ������ĳ���ʱ����Ƴ��俪��ʱ�䣬����Ӱ��ƻ����ܹ��ڡ�", 3000));
		}

		value = work.getFF();
		if (value != null) {
			String label = "<div class='layui-badge-rim' style='margin-right:4px;'>FF " + (int) Math.ceil(value) + "</div>";
			sb.append(
					MetaInfoWarpper.warpper(label, "����ʱ�FF����<br>�ڲ�Ӱ������������翪ʼʱ��������£��������������õĻ���ʱ�䡣���ù��������н����������翪ʼʱ�䣬��ȥ�ù������������ʱ�䡣", 3000));
		}

		value = work.getTF();
		if (value != null && value.doubleValue() == 0) {
			String label = "<div class='layui-badge-rim' style='margin-right:4px;'>CP</div>";
			sb.append(MetaInfoWarpper.warpper(label, "������������Ŀ�ؼ�·��", 3000));
		}

		String manageLevel = work.getManageLevel();
		if ("1".equals(manageLevel)) {
			String label = "<div class='layui-badge-rim' style='margin-right:4px;'>&#8544;</div>";
			sb.append(MetaInfoWarpper.warpper(label, "����һ��1��������Ĺ�����", 3000));
		}

		if ("2".equals(manageLevel)) {
			String label = "<div class='layui-badge-rim' style='margin-right:4px;'>&#8545;</div>";
			sb.append(MetaInfoWarpper.warpper(label, "����һ��2��������Ĺ�����", 3000));
		}
		// ����
		Check.isAssigned(work.getWarningIcon(), sb::append);

		sb.append("</div>");
		return sb.toString();
	}

	private String renderButtons(String label, String href) {
		return renderButtons(true, label, href);
	}

	private String renderButtons(boolean showActionButton, String label, String href) {
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

		StringBuffer sb = new StringBuffer();
		sb.append("<div style='margin-top:16px;padding:4px;display:flex;width:100%;justify-content:space-around;align-items:center;'>");
		btns.forEach(e -> {
			sb.append("<a class='label_card' href='" + e[0] + "' target='_rwt'>" + e[1] + "</a>");
		});
		if (showActionButton)
			sb.append(
					"<a class='label_card' style='color:#" + theme.headBgColor + ";' href='" + href + "' target='_rwt'>" + label + "</a>");
		sb.append("</div>");
		return sb.toString();
	}

	private String renderIndicators(String label1, double ind1, String label2, double ind2) {
		StringBuffer sb = new StringBuffer();
		sb.append("<div style='padding:4px;display:flex;width:100%;justify-content:space-evenly;align-items:center;'>");

		sb.append("<div><img src='/bvs/svg?type=progress&percent=" + ind1 + "&bgColor=" + theme.contrastBgColor + "&fgColor="
				+ theme.contrastFgColor + "' width=100 height=100/>");
		sb.append("<div class='label_body1' style='text-align:center;color:#9e9e9e'>" + label1 + "</div></div>");

		sb.append("<div style='background:#d0d0d0;width:1px;height:80px'></div>");
		sb.append("<div><img src='/bvs/svg?type=progress&percent=" + ind2 + "&bgColor=" + theme.contrastBgColor + "&fgColor="
				+ theme.contrastFgColor + "' width=100 height=100/>");
		sb.append("<div class='label_body1' style='text-align:center;color:#9e9e9e'>" + label2 + "</div></div>");

		sb.append("</div>");
		return sb.toString();
	}

}
