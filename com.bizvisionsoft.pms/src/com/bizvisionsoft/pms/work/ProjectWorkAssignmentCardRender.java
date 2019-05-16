package com.bizvisionsoft.pms.work;

import org.eclipse.jface.viewers.ViewerCell;

import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.annotations.ui.grid.GridRenderUICreated;
import com.bizvisionsoft.annotations.ui.grid.GridRenderUpdateCell;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.tools.Check;

public class ProjectWorkAssignmentCardRender extends WorkAssignmentCardRender {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService br;

	private String userId;

	@Override
	public BruiAssemblyContext getContext() {
		return context;
	}

	@Override
	public IBruiService getBruiService() {
		return br;
	}

	@Init
	protected void init() {
		super.init();
		userId = getBruiService().getCurrentUserId();
	}

	@GridRenderUICreated
	protected void uiCreated() {
		super.uiCreated();
	}

	@GridRenderUpdateCell
	protected void renderCell(@MethodParam(GridRenderUpdateCell.PARAM_CELL) ViewerCell cell) {
		super.renderCell(cell);
	}

	@Override
	protected void showFirstRow(Work work, CardTheme theme, StringBuffer sb) {
		// 显示阶段图标和名称
		Check.isAssigned(work.getStageName(), s -> renderIconTextLine(sb, s, "img/task_c.svg", theme.emphasizeText));
	}

	@Override
	protected void showButtons(Work work, CardTheme theme, StringBuffer sb, String label, String href) {
		// 根据当前用户判断是否显示操作按钮
		renderButtons(theme, sb, work, Check.equals(userId, work.getAssignerId()), label, href);
	}

	@Override
	protected void showAssigner(CardTheme theme, StringBuffer sb, Work work) {
		renderUser(sb, work, "指派", work.warpperAssignerInfo(), theme.emphasizeText);
	}

	@Override
	protected int getRowHeight() {
		return 247;
	}
}
