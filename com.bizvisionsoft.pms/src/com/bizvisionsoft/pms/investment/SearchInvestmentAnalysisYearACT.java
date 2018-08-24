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
		// �򿪲�ѯ�ɱ��ڼ�༭��
		DateTimeInputDialog dt = new DateTimeInputDialog(bruiService.getCurrentShell(), "�����ڼ�", "������Ͷ�ʻر������ڼ�", null,
				d -> d == null ? "����ѡ��ʱ��" : null).setDateSetting(DateTimeSetting.year());
		if (dt.open() == DateTimeInputDialog.OK) {
			// ��ȡ��ѯ�ĳɱ��ڼ�
			String startPeriod = getPeriod(dt.getValue());

			GridPart content = (GridPart) context.getContent();
			GridTreeViewer viewer = content.getViewer();
			GridColumnGroup[] columnGroups = viewer.getGrid().getColumnGroups();
			for (GridColumnGroup columnGroup : columnGroups) {
				if ("period".equals(columnGroup.getData("name"))) {
					columnGroup.setText(startPeriod + " ������������Ԫ��");
				}
			}

			GridColumn[] columns = viewer.getGrid().getColumns();
			GridViewerColumn vcol;
			for (GridColumn column : columns) {
				// �޸ĵ��ڳɱ��е�������Label��ʾ
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
