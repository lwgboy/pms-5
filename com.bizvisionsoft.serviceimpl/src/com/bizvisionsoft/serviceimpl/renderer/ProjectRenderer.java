package com.bizvisionsoft.serviceimpl.renderer;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.bson.Document;

import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.model.News;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.ProjectStatus;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.service.tools.Formatter;
import com.bizvisionsoft.service.tools.MetaInfoWarpper;

public class ProjectRenderer {

	public static Document render(Project pj) {
		return new ProjectRenderer(pj).render();
	}

	private static final int margin = 8;

	private CardTheme theme;

	private Project pj;

	private int rowHeight;

	public ProjectRenderer(Project pj) {
		this.pj = pj;
		theme = new CardTheme(pj);

		if (ProjectStatus.Created.equals(pj.getStatus())) {
			rowHeight = 174;
		} else if (ProjectStatus.Processing.equals(pj.getStatus())) {
			rowHeight = 174;
		} else if (ProjectStatus.Closing.equals(pj.getStatus())) {
			rowHeight = 142;
		} else {
			rowHeight = 142;
		}

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

		sb.insert(0, "<div class='brui_card' style='cursor:pointer;height:" + (rowHeight - 2 * margin) + "px;margin:" + margin + "px;'>");
		sb.append("</div>");
		return new Document("_id", pj.get_id()).append("html", sb.toString()).append("height", rowHeight);
	}

	private void renderClosedProject(StringBuffer sb) {
		//TODO
		sb.append(renderTitle());
		sb.append(renderPM());
	}

	private void renderClosingProject(StringBuffer sb) {
		//TODO �����β��Ŀ��Ӧ����ʾ��Щ
		sb.append(renderTitle());
		sb.append(renderPM());
		String text = "�ƻ���" + Formatter.getString(pj.getPlanStart()) + "~" + RenderTools.shortDate(pj.getPlanFinish())
				+ Optional.ofNullable(pj.getActualStart()).map(d -> " ��ʼ��" + RenderTools.shortDate(d)).orElse(" ��δ��ʼ");
		sb.append(RenderTools.getIconTextLine(text, RenderTools.IMG_URL_CALENDAR, theme.lightText));
		sb.append(renderOrg());

		if (pj.isStageEnable()) {
			rowHeight += 40;
			sb.append(renderProjectStage());
		}

		sb.append(renderTimeline());

		sb.append(renderIndicators());

		sb.append(renderWarningNotice());
	}

	private void renderProcessingProject(StringBuffer sb) {
		sb.append(renderTitle());
		sb.append(renderPM());
		String text = "�ƻ���" + Formatter.getString(pj.getPlanStart()) + "~" + RenderTools.shortDate(pj.getPlanFinish())
				+ Optional.ofNullable(pj.getActualStart()).map(d -> " ��ʼ��" + RenderTools.shortDate(d)).orElse(" ��δ��ʼ");
		sb.append(RenderTools.getIconTextLine(text, RenderTools.IMG_URL_CALENDAR, theme.lightText));
		sb.append(renderOrg());

		if (pj.isStageEnable()) {
			rowHeight += 40;
			sb.append(renderProjectStage());
		}

		sb.append(renderTimeline());

		sb.append(renderIndicators());

		sb.append(renderWarningNotice());

	}

	private void renderCreateProject(StringBuffer sb) {
		sb.append(renderTitle());
		sb.append(renderPM());

		String text = "�ƻ���" + RenderTools.shortDate(pj.getPlanStart()) + "~" + RenderTools.shortDate(pj.getPlanFinish())
				+ "&nbsp;&nbsp;&nbsp;���ڣ�" + pj.getPlanDuration() + "��";
		sb.append(RenderTools.getIconTextLine(text, RenderTools.IMG_URL_CALENDAR, theme.lightText));

		sb.append(renderOrg());
	}

	private String renderOrg() {
		return RenderTools.getTextLine("�е���λ��" + pj.getImpUnitOrgFullName(), theme.emphasizeText);
	}

	private String renderPM() {
		return "<div style='padding-left:8px;padding-top:8px;display:flex;align-items:center;'><img src='" + RenderTools.IMG_URL_USER
				+ "' width='20' height='20'><div class='label_caption brui_text_line' style='color:#" + theme.emphasizeText
				+ ";margin-left:8px;width:100%;display:flex;'>��Ŀ����" + pj.warpperPMInfo() + "</div></div>";
	}

	private String renderTitle() {
		return "<div class='brui_card_head' style='background:#" + theme.headBgColor + ";color:#" + theme.headFgColor + ";padding:8px'>" //
				+ "<div>"//
				+ "<div class='label_title'>" + pj.getName() + "</div>"//
				+ "<div>" + Check.isAssignedThen(pj.getProjectNumber(), n -> "S/N: " + n).orElse("S/N: ����") + "</div>"//
				+ "</div>" //
				+ "<div class='label_title'>" + pj.getStatus() + "</div>"//
				+ "</div>";

	}

	private String renderProjectStage() {
		StringBuffer text = new StringBuffer();
		text.append("<div class='layui-btn-group' "
				+ "style='padding:8px 8px 0px 8px;width: 100%;display:inline-flex;justify-content:space-between;'>");
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
		return text.toString();
	}

	private String renderTimeline() {
		StringBuffer sb = new StringBuffer();
		List<News> news = ServicesLoader.get(ProjectService.class).getRecentNews(pj.get_id(), 5);
		if (news.size() > 0) {
			rowHeight += 8;
			sb.append("<div style='padding:8px 16px 0px 16px;'>");
			for (int i = 0; i < news.size(); i++) {
				sb.append("<div class='label_caption' style='height:20px;display:flex;color:#" + theme.lightText
						+ ";white-space:nowrap;overflow:hidden;text-overflow:ellipsis;width:100%;'>"
						+ "<i class='layui-icon layui-icon-circle' style='margin-right:4px;'></i>" + news.get(i).getSummary() + "</div>");

				rowHeight += 20;
			}
			sb.append("</div>");
		}
		return sb.toString();
	}

	private String renderWarningNotice() {
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
			rowHeight += 24;
			return "<div style='margin-top:8px;padding:4px;display:flex;width:100%;justify-content:flex-end;align-items:center;'>" + html
					+ "</div>";

		}
		return "";
	}

	private String renderIndicators() {
		StringBuffer sb = new StringBuffer();

		Double ind = pj.getWAR();
		String label = "�����������<br>��Ŀ���й����ۼƵ�ʵ�ʹ�����ƻ����ڵı�ֵ����ӳ��Ŀ����������������";
		if (ind != null) {
			appendIndicator(sb, ind, label, CardTheme.CONTRAST_TEAL);
			rowHeight += 80;
		}

		ind = pj.getDAR();
		label = "���������<br>��Ŀʵ�ʹ�����ƻ����ڵı�ֵ����ӳ��Ŀ����Ŀǰ��ʱ״����";
		if (ind != null) {
			appendIndicator(sb, ind, label, CardTheme.CONTRAST_TEAL);
			rowHeight += 80;
		}

		ind = pj.getCAR();
		label = "Ԥ��ʹ����<br>��Ŀ�ۼƵ�ʵ�ʳɱ���ƻ��ɱ���Ԥ�㣩�ı�ֵ����ӳ��Ŀ����Ŀǰ���ĵ��ʽ�״����";
		if (ind != null) {
			appendIndicator(sb, ind, label, CardTheme.CONTRAST_TEAL);
			rowHeight += 80;
		}

		return sb.toString();
	}

	private void appendIndicator(StringBuffer sb, Double ind, String label, String[] indColor) {
		sb.append("<div style='padding:8px 16px 0px 16px;display:flex;width:100%;align-items:center;'>");
		sb.append("<img style='flex-shrink:0;' src='/bvs/svg?type=progress&percent=" + ind + "&bgColor=" + indColor[0] + "&fgColor="
				+ indColor[1] + "' width=72px height=72px/>");
		sb.append("<div class='brui_card_text3 label_caption' style='color:#" + theme.lightText
				+ ";margin-left:8px;padding-left:8px;border-left:solid 2px #" + theme.lightText + ";'>" + label + "</div>");
		sb.append("</div>");
	}

}
