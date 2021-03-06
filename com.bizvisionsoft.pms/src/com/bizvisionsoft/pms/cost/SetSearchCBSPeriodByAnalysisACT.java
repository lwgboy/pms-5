package com.bizvisionsoft.pms.cost;

import java.util.Calendar;
import java.util.Date;

import org.bson.Document;
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
import com.bizvisionsoft.service.CBSService;
import com.bizvisionsoft.service.model.CBSItem;
import com.bizvisionsoft.service.model.CBSSubjectCost;
import com.bizvisionsoft.service.tools.Formatter;
import com.bizvisionsoft.serviceconsumer.Services;

public class SetSearchCBSPeriodByAnalysisACT {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		// 打开查询成本期间编辑器
		DateTimeInputDialog dtid = new DateTimeInputDialog(br.getCurrentShell(), "查询期间", "请选择查询预算成本对比期间",
				(a, b) -> (a == null || b == null) ? "必须选择时间" : null).setDateSetting(DateTimeSetting.month().setRange(true));
		if (dtid.open() == DateTimeInputDialog.OK) {
			Date[] range = dtid.getValues();
			// 获取查询的成本期间
			String startPeriod = getPeriod(range[0]);
			String endPeriod = getPeriod(range[1]);

			GridPart content = (GridPart) context.getContent();
			GridTreeViewer viewer = content.getViewer();
			GridColumnGroup[] columnGroups = viewer.getGrid().getColumnGroups();
			for (GridColumnGroup columnGroup : columnGroups) {
				if ("period".equals(columnGroup.getData("name"))) {
					if (startPeriod.equals(endPeriod)) {
						columnGroup.setText(
								"期间：" + startPeriod.substring(0, 4) + "/" + Integer.parseInt(startPeriod.substring(4, 6)) + "（万元）");
					} else {
						columnGroup.setText("期间：" + startPeriod.substring(0, 4) + "/" + Integer.parseInt(startPeriod.substring(4, 6)) + "-"
								+ endPeriod.substring(0, 4) + "/" + Integer.parseInt(endPeriod.substring(4, 6)) + "（万元）");
					}
				}
			}

			Document doc = Services.get(CBSService.class).getCBSSummary(startPeriod, endPeriod, br.getCurrentUserId(), br.getDomain());

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
					double totalCost = doc.get("totalCost") != null ? ((Number) doc.get("totalCost")).doubleValue() : 0d;
					double totalBudget = doc.get("totalBudget") != null ? ((Number) doc.get("totalBudget")).doubleValue() : 0d;
					value = totalCost - totalBudget;
				}

				if (value != null && value instanceof Number && ((Number) value).doubleValue() != 0)
					column.setFooterText(Formatter.getString(value));

			}
			viewer.refresh(true);
		}

	}

	private ColumnLabelProvider getLabelProvider(String startPeriod, String endPeriod, String type) {
		return new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof CBSItem) {
					CBSItem itm = (CBSItem) element;
					if ("cost".equals(type)) {
						double cost = itm.getDurationCost(startPeriod, endPeriod);
						if (cost != 0)
							return Formatter.getString(cost);
					} else if ("budget".equals(type)) {
						double budget = itm.getDurationBudget(startPeriod, endPeriod);
						if (budget != 0)
							return Formatter.getString(budget);
					} else if ("car".equals(type)) {
						//////////////////////////////////////////////////////////////////////
						//// BUG: 去指标方法更新为可以返回null, Double类型
						//////////////////////////////////////////////////////////////////////
						// Object car = ((CBSItem) element).getCAR(startPeriod, endPeriod);
						// if (car instanceof Number)
						// if (((Number) car).doubleValue() != 0)
						// return new DecimalFormat("#.0%").format(car);
						// else
						// return car.toString();
						Double car = itm.getDurationCAR(startPeriod, endPeriod);
						if (car != null && car != 0)
							return Formatter.getPercentageFormatString(car);
					} else if ("bv".equals(type)) {
						//////////////////////////////////////////////////////////////////////
						//// BUG: 去指标方法更新为可以返回null, Double类型
						//////////////////////////////////////////////////////////////////////
						// Object bdr = itm.getBDR(startPeriod, endPeriod);
						// if (bdr instanceof Number)
						// if (((Number) bdr).doubleValue() != 0)
						// return new DecimalFormat("#.0%").format(bdr);
						// else
						// return bdr.toString();
						Double bdr = itm.getDurationBDR(startPeriod, endPeriod);
						if (bdr != null && bdr != 0)
							return Formatter.getPercentageFormatString(bdr);
					} else if ("overspend".equals(type)) {
						double overspend = itm.getDurationOverspend(startPeriod, endPeriod);
						if (overspend != 0)
							return Formatter.getString(overspend);
					}
				} else if (element instanceof CBSSubjectCost) {
					CBSSubjectCost itm = (CBSSubjectCost) element;
					if ("cost".equals(type)) {
						double cost = itm.getDurationCost(startPeriod, endPeriod);
						if (cost != 0)
							return Formatter.getString(cost);
					} else if ("budget".equals(type)) {
						double budget = itm.getDurationBudget(startPeriod, endPeriod);
						if (budget != 0)
							return Formatter.getString(budget);
						//////////////////////////////////////////////////////////////////////
						//// BUG: 去指标方法更新为可以返回null, Double类型
						//////////////////////////////////////////////////////////////////////
						// } else if ("car".equals(type)) {
						// Object car = sbj.getCAR(startPeriod, endPeriod);
						// if (car instanceof Number)
						// if (((Number) car).doubleValue() != 0)
						// return new DecimalFormat("#.0%").format(car);
						// else
						// return car.toString();
						// } else if ("bv".equals(type)) {
						// Object bdr = sbj.getBDR(startPeriod, endPeriod);
						// if (bdr instanceof Number)
						// if (((Number) bdr).doubleValue() != 0)
						// return new DecimalFormat("#.0%").format(bdr);
						// else
						// return bdr.toString();
					} else if ("car".equals(type)) {
						Double car = itm.getDurationCAR(startPeriod, endPeriod);
						if (car != null && car != 0)
							return Formatter.getPercentageFormatString(car);
					} else if ("bv".equals(type)) {
						Double bdr = itm.getDurationBDR(startPeriod, endPeriod);
						if (bdr != null && bdr != 0)
							return Formatter.getPercentageFormatString(bdr);
					} else if ("overspend".equals(type)) {
						double overspend = itm.getDurationOverspend(startPeriod, endPeriod);
						if (overspend != 0)
							return Formatter.getString(overspend);
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
