package com.bizvisionsoft.pms.cost;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.nebula.jface.gridviewer.GridViewerColumn;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridColumnGroup;

import com.bizivisionsoft.widgets.datetime.DateTimeSetting;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.DateTimeInputDialog;
import com.bizvisionsoft.service.model.ICBSAmount;
import com.bizvisionsoft.service.tools.Formatter;

public class SetSearchProjectAnalysisACT {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context) {
		// 打开查询成本期间编辑器
		DateTimeInputDialog dt = new DateTimeInputDialog(bruiService.getCurrentShell(), "设置期间", "请设置项目预算成本对比分析期间", null,
				d -> d == null ? "必须选择时间" : null).setDateSetting(DateTimeSetting.year());
		if (dt.open() == DateTimeInputDialog.OK) {
			// 获取查询的成本期间
			String startPeriod = getPeriod(dt.getValue());
			GridPart content = (GridPart) context.getContent();
			GridTreeViewer viewer = content.getViewer();
			GridColumnGroup[] columnGroups = viewer.getGrid().getColumnGroups();
			for (GridColumnGroup columnGroup : columnGroups) {
				if ("period".equals(columnGroup.getData("name"))) {
					columnGroup.setText(startPeriod + " 年（万元）");
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
				Double value = null;
				if (element instanceof ICBSAmount) {
					if ("cost".equals(type))
						if (endPeriod != null) {
							value = ((ICBSAmount) element).getCost(startPeriod, endPeriod);
						} else {
							value = ((ICBSAmount) element).getCost(startPeriod);
						}
					else if ("budget".equals(type))
						if (endPeriod != null) {
							value = ((ICBSAmount) element).getBudget(startPeriod, endPeriod);
						} else {
							value = ((ICBSAmount) element).getBudget(startPeriod);
						}
				}

				if (value == null)
					return "";
				if (value.doubleValue() == 0d)
					return "";
				return Formatter.getString(value);
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
