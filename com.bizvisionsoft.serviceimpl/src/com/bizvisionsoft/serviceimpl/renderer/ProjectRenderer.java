package com.bizvisionsoft.serviceimpl.renderer;

import java.util.Date;
import java.util.List;

import org.bson.Document;

import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.model.News;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.ProjectStatus;
import com.bizvisionsoft.service.tools.CardTheme;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.service.tools.MetaInfoWarpper;

public class ProjectRenderer {

	public static Document render(Project pj) {
		return new ProjectRenderer(pj).render();
	}

	private CardTheme theme;

	private Project pj;

	public ProjectRenderer(Project pj) {
		this.pj = pj;
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
		RenderTools.appendLabelAndTextLine(sb, "�е���λ��", pj.getImpUnitOrgFullName(), 24);
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

	private void renderProjectStage(StringBuffer text) {
		text.append("<div class='brui_ly_hline layui-btn-group brui_line_padding' style='display:inline-flex;'>");
		ServicesLoader.get(ProjectService.class).listStage(pj.get_id()).forEach(work -> {
			String style;
			if (work.getStartOn() != null && work.getFinishOn() != null) {
				style = "layui-btn layui-btn-sm";
			} else if (work.getStartOn() != null && work.getFinishOn() == null) {
				style = "layui-btn layui-btn-normal layui-btn-sm";
			} else {
				style = "layui-btn layui-btn-primary layui-btn-sm";
			}
			text.append("<div class='" + style + "' style='width: 100%;'>" + work.getText() + "</div>");
		});
		text.append("</div>");
	}

	private void renderTimeline(StringBuffer sb) {
		List<News> news = ServicesLoader.get(ProjectService.class).getRecentNews(pj.get_id(), 5);
		if (news.size() > 0) {
			sb.append("<div style='padding:8px 16px 0px 16px;'>");
			for (int i = 0; i < news.size(); i++) {
				sb.append("<div class='label_caption' style='height:20px;display:flex;color:#" + theme.lightText
						+ ";White-space:nowrap;overflow:hidden;text-overflow:ellipsis;width:100%;'>"
						+ "<i class='layui-icon layui-icon-circle' style='margin-right:4px;'></i>" + news.get(i).getSummary() + "</div>");

			}
			sb.append("</div>");
		}
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
			appendIndicator(sb, ind, "������", label, CardTheme.CONTRAST_TEAL);
		}

		ind = pj.getDAR();
		if (ind != null) {
			label = "��Ŀʵ�ʹ�����ƻ����ڵı�ֵ����ӳ��Ŀ����Ŀǰ��ʱ״����";
			appendIndicator(sb, ind, "�������", label, CardTheme.CONTRAST_CYAN);
		}

		ind = pj.getCAR();
		if (ind != null) {
			label = "��Ŀ�ۼƵ�ʵ�ʳɱ���ƻ��ɱ���Ԥ�㣩�ı�ֵ����ӳ��Ŀ����Ŀǰ���ĵ��ʽ�״����";
			appendIndicator(sb, ind, "Ԥ������", label, CardTheme.CONTRAST_INDIGO);
		}

		ind = pj.getDurationProbability();
		if (ind != null) {
			label = "���ݷ���ģ��󣬼��㰴�ƻ������Ŀ�ĸ��ʡ�";
			appendIndicator(sb, ind, "���ڸ���", label,
					ind > 0.75 ? CardTheme.CONTRAST_TEAL : (ind > 0.5 ? CardTheme.CONTRAST_BLUE : CardTheme.CONTRAST_ORANGE));
		}

		sb.append("</div>");

	}

	private void appendIndicator(StringBuffer sb, Double ind, String label, String text, String[] indColor) {
		sb.append("<div><div class='label_caption' style='text-align:center;color:#9e9e9e'>" + MetaInfoWarpper.warpper(label, text)
				+ "</div><img src='/bvs/svg?type=progress&percent=" + ind + "&bgColor=" + theme.contrastBgColor + "&fgColor="
				+ theme.contrastFgColor + "' width=72 height=72/></div>");
	}

}
