package com.bizvisionsoft.pms.cost;

import java.util.Calendar;
import java.util.Date;

import org.bson.Document;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.nebula.jface.gridviewer.GridViewerColumn;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridColumnGroup;
import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.model.CBSItem;
import com.mongodb.BasicDBObject;

public class SetSearchCBSPeriodByAnalysisACT {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		// 打开查询成本期间编辑器
		Editor<Object> editor = Editor.create("成本管理—查询", context, new Document(), false);
		if (Window.OK == editor.open()) {
			// 获取查询的成本期间
			BasicDBObject dbo = (BasicDBObject) editor.getResult();
			String startPeriod = getPeriod(dbo.getDate("date1"));
			String endPeriod = getPeriod(dbo.getDate("date2"));

			GridPart content = (GridPart) context.getContent();
			GridTreeViewer viewer = content.getViewer();
			GridColumnGroup[] columnGroups = viewer.getGrid().getColumnGroups();
			for (GridColumnGroup columnGroup : columnGroups) {
				if ("period".equals(columnGroup.getData("name"))) {
					if (startPeriod.equals(endPeriod)) {
						columnGroup.setText(startPeriod.substring(0, 4) + "/" + startPeriod.substring(4, 6));
					} else {
						columnGroup.setText(startPeriod.substring(0, 4) + "/" + startPeriod.substring(4, 6) + "-"
								+ endPeriod.substring(0, 4) + "/" + endPeriod.substring(4, 6));
					}
				}
			}

			GridColumn[] columns = viewer.getGrid().getColumns();
			GridViewerColumn vcol;
			for (GridColumn column : columns) {
				// 修改当期成本列的列名和Label显示
				if ("cost".equals(column.getData("name"))) {
					vcol = new GridViewerColumn(viewer, column);
					vcol.setLabelProvider(getLabelProvider(startPeriod, endPeriod, "cost"));
				} else if ("budget".equals(column.getData("name"))) {
					vcol = new GridViewerColumn(viewer, column);
					vcol.setLabelProvider(getLabelProvider(startPeriod, endPeriod, "budget"));
				} else if ("car".equals(column.getData("name"))) {
					vcol = new GridViewerColumn(viewer, column);
					vcol.setLabelProvider(getLabelProvider(startPeriod, endPeriod, "car"));
				} else if ("bdr".equals(column.getData("name"))) {
					vcol = new GridViewerColumn(viewer, column);
					vcol.setLabelProvider(getLabelProvider(startPeriod, endPeriod, "bv"));
				}

			}
			viewer.refresh(true);
		}

	}

	private ColumnLabelProvider getLabelProvider(String startPeriod, String endPeriod, String type) {
		return new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof CBSItem) {
					if ("cost".equals(type))
						return "" + ((CBSItem) element).getCost(startPeriod, endPeriod);
					else if ("budget".equals(type))
						return "" + ((CBSItem) element).getBudget(startPeriod, endPeriod);
					else if ("".equals(type))
						return "car" + ((CBSItem) element).getCAR(startPeriod, endPeriod);
					else if ("".equals(type))
						return "bv" + ((CBSItem) element).getBDR(startPeriod, endPeriod);
				}
				return "";
			}
		};
	}

	private String getPeriod(Date date) {
		Calendar period = Calendar.getInstance();
		period.setTime(date);
		String result = "" + period.get(Calendar.YEAR);
		result += String.format("%02d", period.get(java.util.Calendar.MONTH) + 1);
		return result;
	}
}
