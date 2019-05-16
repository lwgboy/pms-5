package com.bizvisionsoft.serviceimpl.renderer;

import java.util.Date;
import java.util.List;

import org.bson.Document;

import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.model.News;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.ProjectStatus;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.tools.CardTheme;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.service.tools.MetaInfoWarpper;

public class ProjectRenderer {

	public static Document render(Project pj,String domain) {
		return new ProjectRenderer(pj, domain).render();
	}

	private CardTheme theme;

	private Project pj;

	private String domain;

	public ProjectRenderer(Project pj,String domain) {
		this.pj = pj;
		this.domain = domain;
		theme = new CardTheme(pj);
	}

	private Document render() {
		StringBuffer sb = new StringBuffer();
		if (ProjectStatus.Created.equals(pj.getStatus())) {
			renderCreateProject(sb);
		} else if (ProjectStatus.Processing.equals(pj.getStatus())) {
			renderProcessingProject(sb);
		} else if (ProjectStatus.Closing.equals(pj.getStatus())) {
			renderClosingProject(sb);
		} else {
			renderClosedProject(sb);
		}

		RenderTools.appendCardBg(sb);

		return new Document("_id", pj.get_id()).append("html", sb.toString());
	}

	private void renderClosedProject(StringBuffer sb) {
		sb.append(renderTitle());

		renderPM(sb);

		renderOrg(sb);

		renderPlanSchedule(sb);

		renderActualSchedule(sb);

		if (pj.isStageEnable()) {
			renderProjectStage(sb);
		}

	}

	private void renderClosingProject(StringBuffer sb) {
		sb.append(renderTitle());

		renderPM(sb);

		renderOrg(sb);

		renderPlanSchedule(sb);

		if (pj.getActualStart() != null) {
			renderActualSchedule(sb);
		}

		if (pj.isStageEnable()) {
			renderProjectStage(sb);
		}

		renderTimeline(sb);

		renderIndicators(sb);

		renderWarningNotice(sb);
	}

	private void renderProcessingProject(StringBuffer sb) {
		sb.append(renderTitle());

		renderPM(sb);

		renderOrg(sb);

		renderPlanSchedule(sb);

		if (pj.getActualStart() != null) {
			renderActualSchedule(sb);
		}

		if (pj.isStageEnable()) {
			renderProjectStage(sb);
		}

		renderTimeline(sb);

		renderIndicators(sb);

		renderWarningNotice(sb);

	}

	private void renderCreateProject(StringBuffer sb) {
		sb.append(renderTitle());

		renderPM(sb);

		renderOrg(sb);

		renderPlanSchedule(sb);

	}

	private void renderPlanSchedule(StringBuffer sb) {
		String text = RenderTools.shortDate(pj.getPlanStart()) + " ~ " + RenderTools.shortDate(pj.getPlanFinish()) + " "
				+ pj.getPlanDuration() + "��";
		RenderTools.appendIconLabelAndTextLine(sb, RenderTools.IMG_URL_CALENDAR, "�ƻ����ȣ�", text);
	}

	private void renderActualSchedule(StringBuffer sb) {
		Date actualStart = pj.getActualStart();
		Date actualFinish = pj.getActualFinish();
		String text = RenderTools.shortDate(actualStart) + " ~ " + RenderTools.shortDate(actualFinish) + "  " + pj.getActualDuration()
				+ "��";
		RenderTools.appendIconLabelAndTextLine(sb, RenderTools.IMG_URL_CALENDAR, "ʵ�ʽ��ȣ�", text);
	}

	private void renderOrg(StringBuffer sb) {
		RenderTools.appendLabelAndTextLine(sb, "���ε�λ��", pj.getImpUnitOrgFullName(), 24);
	}

	private void renderPM(StringBuffer sb) {
		RenderTools.appendIconLabelAndTextLine(sb, RenderTools.IMG_URL_USER, "��Ŀ����", pj.warpperPMInfo());
	}

	private String renderTitle() {
		String content = "<div class='brui_card_head' style='height:48px;background:#" + theme.headBgColor + ";color:#" + theme.headFgColor
				+ ";padding:8px'>" //
				+ "<div>"//
				+ "<a class='label_subhead' href='openItem/' target='_rwt' style='color:#" + theme.headFgColor + "';>" + pj.getName()
				+ "</a>"//
				+ "<div class='label_caption'>" + Check.isAssignedThen(pj.getProjectNumber(), n -> "S/N: " + n).orElse("S/N: ����") + "</div>"//
				+ "</div>" //
				+ "<div class='label_title'>" + pj.getStatus() + "</div>"//
				+ "</div>";

		return content;
	}

	private void renderProjectStage(StringBuffer sb) {
		sb.append("<div class='brui_ly_hline layui-btn-group brui_line_padding' style='display:inline-flex;'>");
		List<Work> stages = ServicesLoader.get(ProjectService.class).listStage(pj.get_id(), domain);
		double pc = (double) Math.round(100d / stages.size() * 100) / 100;
		stages.forEach(work -> {
			String style;
			if (work.getStartOn() != null && work.getFinishOn() != null) {
				style = "layui-btn layui-btn-sm";
			} else if (work.getStartOn() != null && work.getFinishOn() == null) {
				style = "layui-btn layui-btn-normal layui-btn-sm";
			} else {
				style = "layui-btn layui-btn-primary layui-btn-sm";
			}
			String text = work.getText();
			sb.append("<div class='" + style + "' style='width:" + pc
					+ "%;text-overflow:ellipsis;overflow:hidden;padding:0 2px;' onclick='layer.tips(\"" + text
					+ "\", this, {tips: 1,time:2000})'>" + text + "</div>");
		});
		sb.append("</div>");
	}

	private void renderTimeline(StringBuffer sb) {
		List<News> list = ServicesLoader.get(ProjectService.class).getRecentNews(pj.get_id(), 3, domain);
		RenderTools.appendList(sb, list, theme.lightText, n -> n.getSummary());
	}

	private void renderWarningNotice(StringBuffer sb) {
		Date _actual = pj.getActualFinish();
		Date _plan = pj.getPlanFinish();
		if (_actual == null) {
			_actual = new Date();
		}
		String text = null;
		String msg = null;
		if (_actual.after(_plan)) {
			text = "<span class='layui-badge'>����</span>";
			msg = "��ǰʱ���Ѿ�������Ŀ�ƻ����ʱ��";
		} else {
			Integer idx = pj.getOverdueIndex();
			if (idx != null) {
				switch (idx) {
				case 0:
					text = "<span class='layui-badge'>&#8544;��Ԥ��</span>";
					msg = "���ݽ��ȹ��㣬����ǰ��չ�ٶȣ���Ŀ��Ҫ���ڣ�Ӧ���ǹؼ�·���Ĺ������иϹ���";
					break;
				case 1:
					text = "<span class='layui-badge layui-bg-orange'>&#8545;��Ԥ��</span>";
					msg = "���ݽ��ȹ��㣬��Ŀ��1������ڵ㽫���ڡ�";
					break;
				case 2:
					text = "<span class='layui-badge layui-bg-orange'>&#8546;��Ԥ��</span>";
					msg = "���ݽ��ȹ��㣬��Ŀ��2������ڵ㽫���ڡ�";
					break;
				}
			}
		}

		if (text != null) {
			String html = MetaInfoWarpper.warpper(text, msg);
			sb.append(
					"<div class='brui_line_padding' style='margin-top:8px;display:flex;width:100%;justify-content:flex-end;align-items:center;'>"
							+ html + "</div>");

		}
	}

	private void renderIndicators(StringBuffer sb) {
		sb.append("<div class='brui_line_padding' style='display:flex;width:100%;justify-content:space-evenly;align-items:center;'>");

		String label;
		Double ind = pj.getWAR();
		if (ind != null) {
			label = "��Ŀ���й����ۼƵ�ʵ�ʹ�����ƻ����ڵı�ֵ����ӳ��Ŀ����������������";
			RenderTools.appendIndicator(sb, ind, "������", label, CardTheme.CONTRAST_TEAL);
		}

		ind = pj.getDAR();
		if (ind != null) {
			label = "��Ŀʵ�ʹ�����ƻ����ڵı�ֵ����ӳ��Ŀ����Ŀǰ��ʱ״����";
			RenderTools.appendIndicator(sb, ind, "�������", label, CardTheme.CONTRAST_CYAN);
		}

		ind = pj.getCAR();
		if (ind != null) {
			label = "��Ŀ�ۼƵ�ʵ�ʳɱ���ƻ��ɱ���Ԥ�㣩�ı�ֵ����ӳ��Ŀ����Ŀǰ���ĵ��ʽ�״����";
			RenderTools.appendIndicator(sb, ind, "Ԥ������", label, CardTheme.CONTRAST_INDIGO);
		}

		ind = pj.getDurationProbability();
		if (ind != null) {
			label = "���ݷ���ģ��󣬼��㰴�ƻ������Ŀ�ĸ��ʡ�";
			RenderTools.appendIndicator(sb, ind, "���ڸ���", label,
					ind > 0.75 ? CardTheme.CONTRAST_TEAL : (ind > 0.5 ? CardTheme.CONTRAST_BLUE : CardTheme.CONTRAST_ORANGE));
		}

		sb.append("</div>");

	}

}
