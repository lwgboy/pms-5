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
		// �򿪲�ѯ�ɱ��ڼ�༭��
		DateTimeInputDialog dt = new DateTimeInputDialog(bruiService.getCurrentShell(), "�����ڼ�", "��������ĿԤ��ɱ��Աȷ����ڼ�", null,
				d -> d == null ? "����ѡ��ʱ��" : null).setDateSetting(DateTimeSetting.year());
		if (dt.open() == DateTimeInputDialog.OK) {
			// ��ȡ��ѯ�ĳɱ��ڼ�
			String startPeriod = getPeriod(dt.getValue());
			GridPart content = (GridPart) context.getContent();
			GridTreeViewer viewer = content.getViewer();
			GridColumnGroup[] columnGroups = viewer.getGrid().getColumnGroups();
			for (GridColumnGroup columnGroup : columnGroups) {
				if ("period".equals(columnGroup.getData("name"))) {
					columnGroup.setText(startPeriod + " �꣨��Ԫ��");
				}
			}

			GridColumn[] columns = viewer.getGrid().getColumns();
			GridViewerColumn vcol;
			for (GridColumn column : columns) {
				// �޸ĵ��ڳɱ��е�������Label��ʾ
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
