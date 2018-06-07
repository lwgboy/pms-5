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
import com.bizvisionsoft.service.model.CBSSubjectCost;
import com.mongodb.BasicDBObject;

public class SetSearchProjectAnalysis {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		// 打开查询成本期间编辑器
		Editor<Object> editor = Editor.create("项目预算成本对比分析—查询", context, new Document(), false);
		if (Window.OK == editor.open()) {
			// 获取查询的成本期间
			BasicDBObject dbo = (BasicDBObject) editor.getResult();
			String startPeriod = getPeriod(dbo.getDate("date1"));

			GridPart content = (GridPart) context.getContent();
			GridTreeViewer viewer = content.getViewer();
			GridColumnGroup[] columnGroups = viewer.getGrid().getColumnGroups();
			for (GridColumnGroup columnGroup : columnGroups) {
				if ("period".equals(columnGroup.getData("name"))) {
					columnGroup.setText(startPeriod + "年");
				}
			}

			GridColumn[] columns = viewer.getGrid().getColumns();
			GridViewerColumn vcol;
			for (GridColumn column : columns) {
				// 修改当期成本列的列名和Label显示
				if ("cost".equals(column.getData("name"))) {
					vcol = new GridViewerColumn(viewer, column);
					vcol.setLabelProvider(getLabelProvider(startPeriod + "01", startPeriod + "12", "cost"));
				} else if ("budget".equals(column.getData("name"))) {
					vcol = new GridViewerColumn(viewer, column);
					vcol.setLabelProvider(getLabelProvider(startPeriod + "01", startPeriod + "12", "budget"));
				} else if ("01".equals(column.getData("name")) || "02".equals(column.getData("name"))
						|| "03".equals(column.getData("name")) || "04".equals(column.getData("name"))
						|| "05".equals(column.getData("name")) || "06".equals(column.getData("name"))
						|| "07".equals(column.getData("name")) || "08".equals(column.getData("name"))
						|| "09".equals(column.getData("name")) || "10".equals(column.getData("name"))
						|| "11".equals(column.getData("name")) || "12".equals(column.getData("name"))) {
					vcol = new GridViewerColumn(viewer, column);
					vcol.setLabelProvider(getLabelProvider(startPeriod + column.getData("name"), null, "cost"));
				}

			}
			viewer.refresh(true);
			viewer.expandAll();
		}

	}

	private ColumnLabelProvider getLabelProvider(String startPeriod, String endPeriod, String type) {
		return new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof CBSItem) {
					if ("cost".equals(type))
						if (endPeriod != null)
							return "" + ((CBSItem) element).getCost(startPeriod, endPeriod);
						else
							return "" + ((CBSItem) element).getCost(startPeriod);
					else if ("budget".equals(type))
						if (endPeriod != null)
							return "" + ((CBSItem) element).getBudget(startPeriod, endPeriod);
						else
							return "" + ((CBSItem) element).getBudget(startPeriod);
				} else if (element instanceof CBSSubjectCost) {

					if ("cost".equals(type))
						if (endPeriod != null)
							return "" + ((CBSSubjectCost) element).getCost(startPeriod, endPeriod);
						else
							return "" + ((CBSSubjectCost) element).getCost(startPeriod);
					else if ("budget".equals(type))
						if (endPeriod != null)
							return "" + ((CBSSubjectCost) element).getBudget(startPeriod, endPeriod);
						else
							return "" + ((CBSSubjectCost) element).getBudget(startPeriod);
				}
				return "";
			}
		};
	}

	private String getPeriod(Date date) {
		Calendar period = Calendar.getInstance();
		period.setTime(date);
		String result = "" + period.get(Calendar.YEAR);
		return result;
	}
}
