package com.bizvisionsoft.serviceimpl.renderer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.bson.Document;

import com.bizvisionsoft.service.model.TrackView;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.tools.CardTheme;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.service.tools.Formatter;
import com.bizvisionsoft.service.tools.MetaInfoWarpper;
import com.bizvisionsoft.service.tools.NLS;

public class WorkRenderer {

	private String lang;

	public static Document render(Work work, String lang) {
		return new WorkRenderer(work).setLanguage(lang).setCanAction(true).setShowProject(true).render();
	}

	public static Document render(Work work, boolean canAction, boolean showProject, String lang) {
		return new WorkRenderer(work).setLanguage(lang).setCanAction(canAction).setShowProject(showProject).render();
	}

	private Document render() {
		StringBuffer sb = new StringBuffer();
		if (work.getActualStart() == null && work.getChargerId() != null) {
			renderingPlannedWorkCard(sb);
		} else if (work.getActualStart() == null && work.getChargerId() == null && work.getAssignerId() != null) {
			renderingUnAssignmentWorkCard(sb);
		} else if (work.getActualStart() != null && work.getActualFinish() == null) {
			renderingExecutingWorkCard(sb);
		} else if (work.getActualStart() != null && work.getActualFinish() != null) {
			renderingFinishedWorkCard(sb);
		}

		RenderTools.appendCardBg(sb);
		
		return new Document("_id", work.get_id()).append("html", sb.toString());
	}

	private Work work;

	private CardTheme theme;

	private boolean canAction;

	private boolean showProject;

	private WorkRenderer(Work work) {
		this.work = work;
		theme = new CardTheme(CardTheme.INDIGO);
		canAction = true;
		showProject = true;
	}

	private WorkRenderer setCanAction(boolean canAction) {
		this.canAction = canAction;
		return this;
	}

	private WorkRenderer setShowProject(boolean showProject) {
		this.showProject = showProject;
		return this;
	}
	
	private WorkRenderer setLanguage(String lang) {
		this.lang = lang;
		return this;
	}

	private void renderingFinishedWorkCard(StringBuffer sb) {
		sb.append(renderTitle(work.getActualFinish()));

		// ��ʾ��һ����Ϣ
		if (showProject)
			renderProjectLine(sb);

		if (Check.isAssigned(work.getStageName()))
			renderStageName(sb);

		// ��ʾ�ƻ���ʼ�ͼƻ����
		renderPlanSchedule(sb);

		// ����������
		renderCharger(sb);

	}

	private void renderingUnAssignmentWorkCard(StringBuffer sb) {

		// ��ʾҳǩ
		sb.append(renderTitle(work.getPlanFinish()));

		// ��ʾ��һ����Ϣ
		if (showProject)
			renderProjectLine(sb);

		if (Check.isAssigned(work.getStageName()))
			renderStageName(sb);

		// ��ʾ�ƻ���ʼ�ͼƻ����
		renderPlanSchedule(sb);

		// ����ָ����
		renderAssigner(sb);

		// ��ʾ��������ָ�ɹ���
		sb.append(renderButtons("ָ��", null, "assignWork/" + work.get_id()));

		// ��ǩ
		sb.append(renderNoticeBudgets());

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
	private void renderingExecutingWorkCard(StringBuffer sb) {

		sb.append(renderTitle(work.getPlanFinish()));

		// ��ʾ��һ����Ϣ
		if (showProject)
			renderProjectLine(sb);

		if (Check.isAssigned(work.getStageName()))
			renderStageName(sb);

		renderPlanSchedule(sb);

		// ����������
		renderCharger(sb);

		// ��ʾ����ָ��
		sb.append(renderIndicators("����", work.getWAR(), "����", work.getDAR()));

		// ��ʾ����������ɹ���
		long count = Optional.ofNullable(work.getChecklist()).map(cl -> cl.stream().filter(c -> !"ͨ��".equals(c.getChoise())).count())
				.orElse(0l);
		if (count > 0) {
			long denyCount = Optional.ofNullable(work.getChecklist()).map(cl -> cl.stream().filter(c -> "���".equals(c.getChoise())).count())
					.orElse(0l);
			String badge = null;
			if (denyCount > 0) {
				badge = "<span class='layui-badge layui-bg-orange' style='margin-left:8px;'>" + denyCount + "</span>";
				badge = MetaInfoWarpper.warpper(badge, denyCount + "�������δ��ͨ����", 3000);
			}
			sb.append(renderButtons(NLS.get(lang, "���"), badge, "checkWork/" + work.get_id()));
		} else {
			sb.append(renderButtons("���", null, "finishWork/" + work.get_id()));
		}

		// ��ǩ
		sb.append(renderNoticeBudgets());

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
	private void renderingPlannedWorkCard(StringBuffer sb) {
		sb.append(renderTitle(work.getPlanStart()));

		// ��ʾ��һ����Ϣ
		if (showProject)
			renderProjectLine(sb);

		if (Check.isAssigned(work.getStageName()))
			renderStageName(sb);

		// ��ʾ�ƻ���ʼ�ͼƻ����
		renderPlanSchedule(sb);

		// ����������
		renderCharger(sb);

		// ��ʾ�������Ϳ�ʼ����
		sb.append(renderButtons("��ʼ", null, "startWork/" + work.get_id()));

		// ��ǩ
		sb.append(renderNoticeBudgets());

	}

	private void renderStageName(StringBuffer sb) {
		RenderTools.appendIconLabelAndTextLine(sb, RenderTools.IMG_URL_TASK,  "�׶Σ�",  work.getStageName());
	}

	private void renderPlanSchedule(StringBuffer sb) {
		Date planStart = work.getPlanStart();
		Date planFinish = work.getPlanFinish();
		Date actualStart = work.getActualStart();
		Date actualFinish = work.getActualFinish();

		RenderTools.appendSchedule(sb, planStart, planFinish, actualStart, actualFinish);
	}

	private String renderTitle(Date date) {
		String name = work.getFullName();
		String _date = Formatter.getString(date, "M/d");
		
		return "<div class='brui_card_head' style='height:48px;background:#" + theme.headBgColor + ";color:#" + theme.headFgColor
				+ ";padding:8px;'>" + "<div class='label_subhead brui_card_text'>" + name + "</div>"//
				+ "<div class='label_headline' style='text-align:center;margin-left:8px'>" + _date + "</div>"
				+ "</div>";
	}

	private void renderProjectLine(StringBuffer sb) {
		RenderTools.appendIconLabelAndTextLine(sb, RenderTools.IMG_URL_PROJECT,"��Ŀ��",  work.getProjectName());
	}

	private void renderCharger(StringBuffer sb) {
		RenderTools.appendIconLabelAndTextLine(sb, RenderTools.IMG_URL_USER,  "����",  work.warpperChargerInfo());
	}

	private void renderAssigner(StringBuffer sb) {
		RenderTools.appendIconLabelAndTextLine(sb, RenderTools.IMG_URL_USER,  "ָ�ɣ�",  work.warpperAssignerInfo());
	}

	private String renderNoticeBudgets() {
		StringBuffer sb = new StringBuffer();
		sb.append("<div class='brui_line_padding' style='margin-top:8px;display:flex;width:100%;justify-content:flex-end;align-items:center;'>");
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

	private String renderButtons(String label, String badge, String href) {

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
		sb.append("<div class='brui_line_padding' style='display:flex;width:100%;justify-content:space-around;align-items:center;'>");
		btns.forEach(e -> {
			sb.append("<a class='' href='" + e[0] + "' target='_rwt'>" + e[1] + "</a>");
		});
		if (canAction) {
			sb.append("<div style='display:inline-flex;align-items:center;'>");
			sb.append(
					"<a class='' style='color:#" + theme.headBgColor + ";' href='" + href + "' target='_rwt'>" + label + "</a>");
			if (Check.isAssigned(badge)) {
				sb.append(" " + badge);
			}
			sb.append("</div>");
		}
		sb.append("</div>");
		return sb.toString();
	}

	private String renderIndicators(String label1, double ind1, String label2, double ind2) {
		StringBuffer sb = new StringBuffer();
		sb.append("<div class='brui_line_padding' style='display:flex;width:100%;justify-content:space-evenly;align-items:center;'>");

		sb.append("<div><div class='label_caption' style='text-align:center;color:#9e9e9e'>" + label1 + "</div><img src='/bvs/svg?type=progress&percent=" + ind1 + "&bgColor=" + theme.contrastBgColor + "&fgColor="
				+ theme.contrastFgColor + "' width=72 height=72/>");
		sb.append("</div>");

		sb.append("<div style='background:#d0d0d0;width:1px;height:60px'></div>");
		
		sb.append("<div><div class='label_caption' style='text-align:center;color:#9e9e9e'>" + label2 + "</div><img src='/bvs/svg?type=progress&percent=" + ind2 + "&bgColor=" + theme.contrastBgColor + "&fgColor="
				+ theme.contrastFgColor + "' width=72 height=72/>");
		sb.append("</div>");

		sb.append("</div>");
		return sb.toString();
	}

}
