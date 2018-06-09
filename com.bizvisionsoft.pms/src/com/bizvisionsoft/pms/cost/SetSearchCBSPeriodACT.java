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

public class SetSearchCBSPeriodACT {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		// �򿪲�ѯ�ɱ��ڼ�༭��
		Editor<Object> editor = Editor.create("�ɱ�������ѯ", context, new Document(), false);
		if (Window.OK == editor.open()) {
			// ��ȡ��ѯ�ĳɱ��ڼ�
			BasicDBObject dbo = (BasicDBObject) editor.getResult();
			String startPeriod = getPeriod(dbo.getDate("date1"));
			String endPeriod = getPeriod(dbo.getDate("date2"));

			GridPart content = (GridPart) context.getContent();
			GridTreeViewer viewer = content.getViewer();
			GridColumn[] columns = viewer.getGrid().getColumns();
			for (GridColumn column : columns) {
				// �޸ĵ��ڳɱ��е�������Label��ʾ
				if ("periodCost".equals(column.getData("name"))) {
					if (startPeriod.equals(endPeriod)) {
						column.setText("�ڼ䣺"+startPeriod.substring(0, 4) + "/" + Integer.parseInt(startPeriod.substring(4, 6)) + "����Ԫ��");
					} else {
						column.setText("�ڼ䣺"+startPeriod.substring(0, 4) + "/" + Integer.parseInt(startPeriod.substring(4, 6)) + "-"
								+ endPeriod.substring(0, 4) + "/" + Integer.parseInt(endPeriod.substring(4, 6)) + "����Ԫ��");
					}

					GridViewerColumn vcol = new GridViewerColumn(viewer, column);
					vcol.setLabelProvider(new ColumnLabelProvider() {
						@Override
						public String getText(Object element) {
							if (element instanceof CBSItem) {
								return new DecimalFormat("#.0").format(((CBSItem) element).getCost(startPeriod, endPeriod));
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
