package com.bizvisionsoft.pms.work;

import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.nebula.widgets.grid.GridItem;

import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.annotations.ui.grid.GridRenderUICreated;
import com.bizvisionsoft.annotations.ui.grid.GridRenderUpdateCell;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.service.tools.Formatter;
import com.bizvisionsoft.service.tools.MetaInfoWarpper;

public class WorkCardRender extends AbstractWorkCardRender{

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService brui;
	
	@Init
	protected void init() {
		super.init();
	}

	@GridRenderUICreated
	protected void uiCreated() {
		super.uiCreated();
	}

	@GridRenderUpdateCell
	protected void renderCell(@MethodParam(GridRenderUpdateCell.PARAM_CELL) ViewerCell cell) {
		Work work = (Work) cell.getElement();
		// ִ����
		if (work.getActualStart() != null && work.getActualFinish() == null) {
			renderToFinishCard(cell, work);
		} else if (work.getActualStart() == null) {// �Ѽƻ���δ��ʼ
			renderToStartCard(cell, work);
		} else if (work.getActualFinish() != null) {// �����
			renderFinishedCard(cell, work);
		}
	}

	private void renderFinishedCard(ViewerCell cell, Work work) {
		GridItem gridItem = (GridItem) cell.getViewerRow().getItem();
		int rowHeight = 180;
		gridItem.setHeight(rowHeight);
		CardTheme theme = new CardTheme("deepGrey");

		StringBuffer sb = new StringBuffer();
		// ��ʾҳǩ
		int margin = 8;
		sb.append("<div class='brui_card' style='height:" + (rowHeight - 2 * margin) + "px;margin:" + margin + "px;'>");

		renderTitle(theme, sb, work);
		// ��ʾ��Ŀͼ�������
		renderIconTextLine(sb, "��Ŀ��" + work.getProjectName(), "img/project_c.svg", theme.lightText);

		// ��ʾ�ƻ���ʼ�ͼƻ����
		String text = "�ƻ���" + Formatter.getString(work.getPlanStart()) + "~" + Formatter.getString(work.getPlanFinish());
		renderIconTextLine(sb, text, "img/calendar_c.svg", theme.emphasizeText);

		// ����������
		renderIconTextLine(sb, "����" + work.getChargerInfoHtml(), "img/user_c.svg", theme.emphasizeText);

		sb.append("</div>");

		cell.setText(sb.toString());

	}

	private void renderToStartCard(ViewerCell cell, Work work) {
		GridItem gridItem = (GridItem) cell.getViewerRow().getItem();
		int rowHeight = 244;
		gridItem.setHeight(rowHeight);
		CardTheme theme = new CardTheme(work);

		StringBuffer sb = new StringBuffer();
		// ��ʾҳǩ
		int margin = 8;
		sb.append("<div class='brui_card' style='height:" + (rowHeight - 2 * margin) + "px;margin:" + margin + "px;'>");

		renderTitle(theme, sb, work);
		// ��ʾ��Ŀͼ�������
		renderIconTextLine(sb, "��Ŀ��" + work.getProjectName(), "img/project_c.svg", theme.lightText);

		// ��ʾ�ƻ���ʼ�ͼƻ����
		String text = "�ƻ���" + Formatter.getString(work.getPlanStart()) + "~" + Formatter.getString(work.getPlanFinish());
		renderIconTextLine(sb, text, "img/calendar_c.svg", theme.emphasizeText);

		// ����������
		renderIconTextLine(sb, "����" + work.getChargerInfoHtml(), "img/user_c.svg", theme.emphasizeText);

		// ��ʾ����������ɹ���
		renderButtons(theme, sb, work, "��ʼ", "startWork/" + work.get_id());

		// ��ǩ
		renderNoticeBudgets(work, sb);

		sb.append("</div>");

		cell.setText(sb.toString());

	}

	/**
	 * ���ƴ���������Ƭ
	 * 
	 * @param cell
	 * @param work
	 */
	private void renderToFinishCard(ViewerCell cell, Work work) {
		GridItem gridItem = (GridItem) cell.getViewerRow().getItem();
		int rowHeight = 372;
		gridItem.setHeight(rowHeight);
		CardTheme theme = new CardTheme(work);

		StringBuffer sb = new StringBuffer();
		// ��ʾҳǩ
		int margin = 8;
		sb.append("<div class='brui_card' style='height:" + (rowHeight - 2 * margin) + "px;margin:" + margin + "px;'>");

		renderTitle(theme, sb, work);
		// ��ʾ��Ŀͼ�������
		renderIconTextLine(sb, "��Ŀ��" + work.getProjectName(), "img/project_c.svg", theme.lightText);

		// ��ʾ�ƻ���ʼ�ͼƻ����
		String text = "�ƻ���" + Formatter.getString(work.getPlanStart()) + "~" + Formatter.getString(work.getPlanFinish()) + "��ʵ�ʿ�ʼ��"
				+ Formatter.getString(work.getActualStart());
		renderIconTextLine(sb, text, "img/calendar_c.svg", theme.emphasizeText);

		// ����������
		renderIconTextLine(sb, "����" + work.getChargerInfoHtml(), "img/user_c.svg", theme.emphasizeText);

		// ��ʾ����ָ��
		renderIndicators(theme, sb, "����", work.getWAR(), "����", work.getDAR());

		// ��ʾ����������ɹ���
		renderButtons(theme, sb, work, "���", "finishWork/" + work.get_id());

		// ��ǩ
		renderNoticeBudgets(work, sb);

		sb.append("</div>");

		cell.setText(sb.toString());
	}

	protected void renderNoticeBudgets(Work work, StringBuffer sb) {
		sb.append("<div style='padding:8px;display:flex;width:100%;justify-content:flex-end;align-items:center;'>");
		Check.isAssigned(work.getManageLevel(), l -> {
			if ("1".equals(l)) {
				String label = "<div class='layui-badge layui-bg-blue' style='width:36px;margin-right:4px;'>1��</div>";
				sb.append(MetaInfoWarpper.warpper(label, "����һ��1��������Ĺ�����", 3000));
			}

			if ("2".equals(l)) {
				String label = "<div class='layui-badge layui-bg-cyan' style='width:36px;margin-right:4px;'>2��</div>";
				sb.append(MetaInfoWarpper.warpper(label, "����һ��2��������Ĺ�����", 3000));
			}
		});
		// ����
		Check.isAssigned(work.getWarningIcon(), sb::append);
	}

	@Override
	protected BruiAssemblyContext getContext() {
		return context;
	}

	@Override
	protected IBruiService getBruiService() {
		return brui;
	}

}
