package com.bizvisionsoft.pms.projectchange;

import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.swt.SWT;

import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.annotations.ui.grid.GridRenderColumnHeader;
import com.bizvisionsoft.annotations.ui.grid.GridRenderUICreated;
import com.bizvisionsoft.annotations.ui.grid.GridRenderUpdateCell;
import com.bizvisionsoft.bruicommons.model.Column;
import com.bizvisionsoft.bruiengine.assembly.GridPartDefaultRender;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Selector;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.model.ProjectChange;
import com.bizvisionsoft.service.model.ProjectChangeTask;
import com.bizvisionsoft.service.model.User;

public class ProjectChangeRender extends GridPartDefaultRender {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService br;

	private GridTreeViewer viewer;

	@GridRenderUICreated
	private void uiCreated() {
		viewer = (GridTreeViewer) context.getContent("viewer");
		viewer.getGrid().addListener(SWT.Selection, e -> {
			if (e.text != null) {
				ProjectChangeTask item = (ProjectChangeTask) e.item.getData();
				if (e.text.startsWith("userInfo/")) {
					Selector.open("用户选择器—单选.selectorassy", context, null, l -> {
						item.user = ((User) l.get(0)).getUserId();
						ServicesLoader.get(ProjectService.class).updateProjectChange(item, ((ProjectChange) context.getInput()).get_id(),
								br.getDomain());
						viewer.refresh();
					});
				} else if (e.text.split("/").length > 1) {
				}
			}
		});
	}

	@Override
	@GridRenderUpdateCell
	public void renderCell(@MethodParam(GridRenderUpdateCell.PARAM_CELL) ViewerCell cell,
			@MethodParam(GridRenderUpdateCell.PARAM_COLUMN) Column column, @MethodParam(GridRenderUpdateCell.PARAM_VALUE) Object value,
			@MethodParam(GridRenderUpdateCell.PARAM_IMAGE) Object image) {
		ProjectChangeTask task = (ProjectChangeTask) cell.getElement();
		if ("userInfo".equals(column.getName())) {
			StringBuffer sb = new StringBuffer();
			sb.append("<div style='display:block;height:25px;'>");
			ProjectChange pc = (ProjectChange) context.getInput();
			String user = task.getUser();
			if (user != null && !"".equals(user)) {
				sb.append(user);
			}

			if (ProjectChange.STATUS_CREATE.equals(pc.getStatus())) {
				sb.append(
						"<a href='userInfo/' target='_rwt'><button class='layui-btn layui-btn-xs layui-btn-primary' style='position:absolute;right:0px;'>"
								+ "<i class='layui-icon  layui-icon-edit'></i>" + "</button></a>");
			}

			sb.append("</div>");
			cell.setText(sb.toString());
		} else {
			super.renderCell(cell, column, value, image);
		}
	}

	@Override
	@GridRenderColumnHeader
	public void renderColumnHeader(@MethodParam(GridRenderColumnHeader.PARAM_COLUMN_WIDGET) GridColumn col,
			@MethodParam(GridRenderColumnHeader.PARAM_COLUMN) Column column) {
		super.renderColumnHeader(col, column);
	}

}