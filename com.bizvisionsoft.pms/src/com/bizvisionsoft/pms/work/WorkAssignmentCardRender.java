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

public class WorkAssignmentCardRender extends AbstractWorkCardRender{

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService brui;

	@Override
	protected BruiAssemblyContext getContext() {
		return context;
	}

	@Override
	protected IBruiService getBruiService() {
		return brui;
	}
	
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
		Work work = (Work) cell.getItem().getData();
		GridItem gridItem = (GridItem) cell.getViewerRow().getItem();
		int rowHeight = 244;
		gridItem.setHeight(rowHeight);
		CardTheme theme = new CardTheme(work);

		StringBuffer sb = new StringBuffer();
		// ��ʾҳǩ
		int margin = 8;
		sb.append("<div class='brui_card' style='height:" + (rowHeight - 2 * margin) + "px;margin:" + margin + "px;'>");

		renderTitle(theme, sb, work);

		// ��ǩ
//		renderNoticeBudgets(work, sb);
		
		// ��ʾ��Ŀͼ�������
		renderProjectLine(theme,sb, work);

		// ��ʾ�ƻ���ʼ�ͼƻ����
		String text = "�ƻ���" + Formatter.getString(work.getPlanStart()) + "~" + Formatter.getString(work.getPlanFinish());
		renderIconTextLine(sb, text, "img/calendar_c.svg", theme.emphasizeText);

		// ����������
		renderIconTextLine(sb, "����" + work.getChargerInfoHtml(), "img/user_c.svg", theme.emphasizeText);

		// ��ʾ����������ɹ���
		renderButtons(theme, sb, work, "ָ��", "assignWork/" + work.get_id());


		sb.append("</div>");

		cell.setText(sb.toString());
	}


}
