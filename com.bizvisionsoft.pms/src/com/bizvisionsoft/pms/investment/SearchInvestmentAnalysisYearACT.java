package com.bizvisionsoft.pms.investment;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.nebula.jface.gridviewer.GridViewerColumn;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridColumnGroup;
import org.eclipse.swt.widgets.Event;

import com.bizivisionsoft.widgets.datetime.DateTimeSetting;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.DateTimeInputDialog;
import com.bizvisionsoft.service.model.EPSInfo;
import com.bizvisionsoft.service.tools.Util;

public class SearchInvestmentAnalysisYearACT {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(Execute.PARAM_EVENT) Event event) {
		// 打开查询成本期间编辑器
		DateTimeInputDialog dt = new DateTimeInputDialog(bruiService.getCurrentShell(), "设置期间", "请设置投资回报分析期间", null,
				d -> d == null ? "必须选择时间" : null).setDateSetting(DateTimeSetting.year());
		if (dt.open() == DateTimeInputDialog.OK) {
			// 获取查询的成本期间
			String startPeriod = getPeriod(dt.getValue());

			GridPart content = (GridPart) context.getContent();
			GridTreeViewer viewer = content.getViewer();
			GridColumnGroup[] columnGroups = viewer.getGrid().getColumnGroups();
			for (GridColumnGroup columnGroup : columnGroups) {
				if ("period".equals(columnGroup.getData("name"))) {
					columnGroup.setText(startPeriod + " 年销售利润（万元）");
				}
			}

			GridColumn[] columns = viewer.getGrid().getColumns();
			GridViewerColumn vcol;
			for (GridColumn column : columns) {
				// 修改当期成本列的列名和Label显示
				Object name = column.getData("name");
				if ("cost".equals(name)) {
					vcol = new GridViewerColumn(viewer, column);
					vcol.setLabelProvider(getLabelProvider(startPeriod + "01", startPeriod + "12", "cost"));
				}else if ("profit".equals(name)) {
					vcol = new GridViewerColumn(viewer, column);
					vcol.setLabelProvider(getLabelProvider(startPeriod + "01", startPeriod + "12", "profit"));
				} else if ("roi".equals(name)) {
					vcol = new GridViewerColumn(viewer, column);
					vcol.setLabelProvider(getLabelProvider(startPeriod + "01", startPeriod + "12", "roi"));
				} else if ("01".equals(name) || "02".equals(name) || "03".equals(name) || "04".equals(name)
						|| "05".equals(name) || "06".equals(name) || "07".equals(name) || "08".equals(name)
						|| "09".equals(name) || "10".equals(name) || "11".equals(name) || "12".equals(name)) {
					vcol = new GridViewerColumn(viewer, column);
					vcol.setLabelProvider(getLabelProvider(startPeriod + name, null, "profit"));
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
				if (element instanceof EPSInfo) {
					if ("cost".equals(type)) {
						double cost = ((EPSInfo) element).getCost(startPeriod, endPeriod);
						if (cost != 0)
							return Util.getFormatNumber(cost);
					} else if ("roi".equals(type)) {
						double roi = ((EPSInfo) element).getROI();
						if (roi != 0)
							return Util.getFormatPercentage(roi);
					} else if ("profit".equals(type)) {
						double profit;
						if (endPeriod != null) {
							profit = ((EPSInfo) element).getProfit(startPeriod, endPeriod);
						} else {
							profit = ((EPSInfo) element).getProfit(startPeriod);
						}
						if (profit != 0)
							return Util.getFormatNumber(profit);
					}
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
