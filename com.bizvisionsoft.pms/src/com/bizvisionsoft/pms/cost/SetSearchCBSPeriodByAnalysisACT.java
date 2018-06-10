package com.bizvisionsoft.pms.cost;

import java.text.DecimalFormat;
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
import com.bizvisionsoft.service.CBSService;
import com.bizvisionsoft.service.model.CBSItem;
import com.bizvisionsoft.service.model.CBSSubjectCost;
import com.bizvisionsoft.serviceconsumer.Services;
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
						columnGroup.setText("期间：" + startPeriod.substring(0, 4) + "/"
								+ Integer.parseInt(startPeriod.substring(4, 6)) + "（万元）");
					} else {
						columnGroup.setText("期间：" + startPeriod.substring(0, 4) + "/"
								+ Integer.parseInt(startPeriod.substring(4, 6)) + "-" + endPeriod.substring(0, 4) + "/"
								+ Integer.parseInt(endPeriod.substring(4, 6)) + "（万元）");
					}
				}
			}

			Document doc = Services.get(CBSService.class).getCBSSummary(startPeriod, endPeriod);

			GridColumn[] columns = viewer.getGrid().getColumns();
			GridViewerColumn vcol;
			for (GridColumn column : columns) {
				// 修改当期成本列的列名和Label显示
				Object value = null;
				Object name = column.getData("name");
				if ("cost".equals(name)) {
					vcol = new GridViewerColumn(viewer, column);
					vcol.setLabelProvider(getLabelProvider(startPeriod, endPeriod, "cost"));
					value = doc.get("cost");
				} else if ("budget".equals(name)) {
					vcol = new GridViewerColumn(viewer, column);
					vcol.setLabelProvider(getLabelProvider(startPeriod, endPeriod, "budget"));
					value = doc.get("budget");
				} else if ("overspend".equals(name)) {
					vcol = new GridViewerColumn(viewer, column);
					vcol.setLabelProvider(getLabelProvider(startPeriod, endPeriod, "overspend"));
					double cost = doc.get("cost") != null ? ((Number) doc.get("cost")).doubleValue() : 0d;
					double budget = doc.get("budget") != null ? ((Number) doc.get("budget")).doubleValue() : 0d;
					value = cost - budget;
				} else if ("car".equals(name)) {
					vcol = new GridViewerColumn(viewer, column);
					vcol.setLabelProvider(getLabelProvider(startPeriod, endPeriod, "car"));
				} else if ("bdr".equals(name)) {
					vcol = new GridViewerColumn(viewer, column);
					vcol.setLabelProvider(getLabelProvider(startPeriod, endPeriod, "bv"));
				} else if ("totalCost".equals(name)) {
					value = doc.get("totalCost");
				} else if ("totalBudget".equals(name)) {
					value = doc.get("totalBudget");
				} else if ("totalOverspend".equals(name)) {
					double totalCost = doc.get("totalCost") != null ? ((Number) doc.get("totalCost")).doubleValue()
							: 0d;
					double totalBudget = doc.get("totalBudget") != null
							? ((Number) doc.get("totalBudget")).doubleValue()
							: 0d;
					value = totalCost - totalBudget;
				}

				if (value != null && value instanceof Number && ((Number) value).doubleValue() != 0)
					column.setFooterText(new DecimalFormat("#.0").format(value));

			}
			viewer.refresh(true);
		}

	}

	private ColumnLabelProvider getLabelProvider(String startPeriod, String endPeriod, String type) {
		return new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof CBSItem) {
					if ("cost".equals(type)) {
						double cost = ((CBSItem) element).getCost(startPeriod, endPeriod);
						if (cost != 0)
							return new DecimalFormat("#.0").format(cost);
					} else if ("budget".equals(type)) {
						double budget = ((CBSItem) element).getBudget(startPeriod, endPeriod);
						if (budget != 0)
							return new DecimalFormat("#.0").format(budget);
					} else if ("car".equals(type)) {
						Object car = ((CBSItem) element).getCAR(startPeriod, endPeriod);
						if (car instanceof Number)
							if (((Number) car).doubleValue() != 0)
								return new DecimalFormat("#.0%").format(car);
							else
								return car.toString();
					} else if ("bv".equals(type)) {
						Object bdr = ((CBSItem) element).getBDR(startPeriod, endPeriod);
						if (bdr instanceof Number)
							if (((Number) bdr).doubleValue() != 0)
								return new DecimalFormat("#.0%").format(bdr);
							else
								return bdr.toString();
					} else if ("overspend".equals(type)) {
						double overspend = ((CBSItem) element).getOverspend(startPeriod, endPeriod);
						if (overspend != 0)
							return new DecimalFormat("#.0").format(overspend);
					}
				} else if (element instanceof CBSSubjectCost) {
					if ("cost".equals(type)) {
						double cost = ((CBSSubjectCost) element).getCost(startPeriod, endPeriod);
						if (cost != 0)
							return new DecimalFormat("#.0").format(cost);
					} else if ("budget".equals(type)) {
						double budget = ((CBSSubjectCost) element).getBudget(startPeriod, endPeriod);
						if (budget != 0)
							return new DecimalFormat("#.0").format(budget);
					} else if ("car".equals(type)) {
						Object car = ((CBSSubjectCost) element).getCAR(startPeriod, endPeriod);
						if (car instanceof Number)
							if (((Number) car).doubleValue() != 0)
								return new DecimalFormat("#.0%").format(car);
							else
								return car.toString();
					} else if ("bv".equals(type)) {
						Object bdr = ((CBSSubjectCost) element).getBDR(startPeriod, endPeriod);
						if (bdr instanceof Number)
							if (((Number) bdr).doubleValue() != 0)
								return new DecimalFormat("#.0%").format(bdr);
							else
								return bdr.toString();
					} else if ("overspend".equals(type)) {
						double overspend = ((CBSSubjectCost) element).getOverspend(startPeriod, endPeriod);
						if (overspend != 0)
							return new DecimalFormat("#.0").format(overspend);
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
		result += String.format("%02d", period.get(java.util.Calendar.MONTH) + 1);
		return result;
	}
}
