package com.bizvisionsoft.pms.cost;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.nebula.jface.gridviewer.GridViewerColumn;
import org.eclipse.nebula.widgets.grid.GridColumn;

import com.bizivisionsoft.widgets.datetime.DateTimeSetting;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.DateTimeInputDialog;
import com.bizvisionsoft.service.model.CBSItem;
import com.bizvisionsoft.service.tools.Formatter;

public class SetSearchCBSPeriodACT {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context) {
		// �򿪲�ѯ�ɱ��ڼ�༭��
		DateTimeInputDialog dtid = new DateTimeInputDialog(bruiService.getCurrentShell(), "��ѯ�ɱ��ڼ�", "��ѡ���ѯ�ɱ��ڼ�",
				(a, b) -> (a == null || b == null) ? "����ѡ��ʱ��" : null)
						.setDateSetting(DateTimeSetting.month().setRange(true));
		if (dtid.open() == DateTimeInputDialog.OK) {
			Date[] range = dtid.getValues();
			// ��ȡ��ѯ�ĳɱ��ڼ�
			String startPeriod = getPeriod(range[0]);
			String endPeriod = getPeriod(range[1]);

			GridPart content = (GridPart) context.getContent();
			GridTreeViewer viewer = content.getViewer();
			GridColumn[] columns = viewer.getGrid().getColumns();
			for (GridColumn column : columns) {
				// �޸ĵ��ڳɱ��е�������Label��ʾ
				if ("periodCost".equals(column.getData("name"))) {
					if (startPeriod.equals(endPeriod)) {
						column.setText("�ڼ䣺" + startPeriod.substring(0, 4) + "/"
								+ Integer.parseInt(startPeriod.substring(4, 6)) + "����Ԫ��");
					} else {
						column.setText("�ڼ䣺" + startPeriod.substring(0, 4) + "/"
								+ Integer.parseInt(startPeriod.substring(4, 6)) + "-" + endPeriod.substring(0, 4) + "/"
								+ Integer.parseInt(endPeriod.substring(4, 6)) + "����Ԫ��");
					}

					GridViewerColumn vcol = new GridViewerColumn(viewer, column);
					vcol.setLabelProvider(new ColumnLabelProvider() {
						@Override
						public String getText(Object element) {
							if (element instanceof CBSItem) {
								double cost = ((CBSItem) element).getCost(startPeriod, endPeriod);
								if (cost != 0)
									return Formatter.getString(cost);
							}
							return "";
						}
					});
				}

			}
			viewer.refresh(true);
		}
	}

	private String getPeriod(Date date) {
		Calendar period = Calendar.getInstance();
		period.setTime(date);
		String result = "" + period.get(Calendar.YEAR);
		result += String.format("%02d", period.get(java.util.Calendar.MONTH) + 1);
		return result;
	}
}
