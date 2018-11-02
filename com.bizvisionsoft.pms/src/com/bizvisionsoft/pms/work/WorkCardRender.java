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
import com.bizvisionsoft.service.tools.Formatter;

public class WorkCardRender extends AbstractWorkCardRender {

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

		// ��ʾ��һ����Ϣ
		showFirstRow(work, theme, sb);

		// ��ʾ�ƻ���ʼ�ͼƻ����
		String text = "�ƻ���" + Formatter.getString(work.getPlanStart()) + "~" + Formatter.getString(work.getPlanFinish()) + "��ʵ����ɣ�"
				+ Formatter.getString(work.getActualFinish());
		renderIconTextLine(sb, text, "img/calendar_c.svg", theme.emphasizeText);

		// ����������
		renderCharger(theme, sb, work);

		sb.append("</div>");

		cell.setText(sb.toString());

	}

	private void renderToStartCard(ViewerCell cell, Work work) {
		GridItem gridItem = (GridItem) cell.getViewerRow().getItem();
		int rowHeight = 232;
		gridItem.setHeight(rowHeight);
		CardTheme theme = new CardTheme(work);

		StringBuffer sb = new StringBuffer();
		// ��ʾҳǩ
		int margin = 8;
		sb.append("<div class='brui_card' style='height:" + (rowHeight - 2 * margin) + "px;margin:" + margin + "px;'>");

		renderTitle(theme, sb, work);
		// ��ǩ
		// renderNoticeBudgets(work, sb);

		// ��ʾ��һ����Ϣ
		showFirstRow(work, theme, sb);

		// ��ʾ�ƻ���ʼ�ͼƻ����
		String text = "�ƻ���" + Formatter.getString(work.getPlanStart()) + "~" + Formatter.getString(work.getPlanFinish());
		renderIconTextLine(sb, text, "img/calendar_c.svg", theme.emphasizeText);

		// ����������
		renderCharger(theme, sb, work);

		showButtons(work, theme, sb, "��ʼ", "startWork/" + work.get_id());

		sb.append("</div>");

		cell.setText(sb.toString());

	}

	protected void showButtons(Work work, CardTheme theme, StringBuffer sb, String label, String href) {
		// ��ʾ�������͹�������
		renderButtons(theme, sb, work, label, href);
	}

	protected void showFirstRow(Work work, CardTheme theme, StringBuffer sb) {
		// Ĭ�� ��ʾ��Ŀͼ�������
		renderProjectLine(theme, sb, work);
	}

	/**
	 * ���ƴ���������Ƭ
	 * 
	 * @param cell
	 * @param work
	 */
	private void renderToFinishCard(ViewerCell cell, Work work) {
		GridItem gridItem = (GridItem) cell.getViewerRow().getItem();
		int rowHeight = 360;
		gridItem.setHeight(rowHeight);
		CardTheme theme = new CardTheme(work);

		StringBuffer sb = new StringBuffer();
		// ��ʾҳǩ
		int margin = 8;
		sb.append("<div class='brui_card' style='height:" + (rowHeight - 2 * margin) + "px;margin:" + margin + "px;'>");

		renderTitle(theme, sb, work);

		// ��ǩ
		// renderNoticeBudgets(work, sb);

		// ��ʾ��һ����Ϣ
		showFirstRow(work, theme, sb);

		// ��ʾ�ƻ���ʼ�ͼƻ����
		String text = "�ƻ���" + Formatter.getString(work.getPlanStart()) + "~" + Formatter.getString(work.getPlanFinish()) + "��ʵ�ʿ�ʼ��"
				+ Formatter.getString(work.getActualStart());
		renderIconTextLine(sb, text, "img/calendar_c.svg", theme.emphasizeText);

		// ����������
		renderCharger(theme, sb, work);

		// ��ʾ����ָ��
		renderIndicators(theme, sb, "����", work.getWAR(), "����", work.getDAR());

		// ��ʾ����������ɹ���
		showButtons(work, theme, sb, "���", "finishWork/" + work.get_id());

		sb.append("</div>");

		cell.setText(sb.toString());
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
