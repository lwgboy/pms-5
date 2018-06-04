package com.bizvisionsoft.pms.cost;

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

public class SetSearchCBSPeriod {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		Editor<Object> editor = Editor.create("成本管理—查询", context, new Document(), false);
		if (Window.OK == editor.open()) {
			BasicDBObject dbo = (BasicDBObject) editor.getResult();
			String startPeriod = getPeriod(dbo.getDate("date1"));
			String endPeriod = getPeriod(dbo.getDate("date2"));

			IBruiContext bruiContext = context.getChildContextByAssemblyName("成本管理");
			GridPart content = (GridPart) bruiContext.getContent();
			GridTreeViewer viewer = content.getViewer();
			GridColumn column = viewer.getGrid().getColumn(viewer.getGrid().getColumnCount() - 1);
			if (startPeriod.equals(endPeriod)) {
				column.setText(startPeriod);
			} else {
				column.setText(startPeriod + "-" + endPeriod);
			}

			GridViewerColumn vcol = new GridViewerColumn(viewer, column);
			vcol.setLabelProvider(new ColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					return "" + ((CBSItem) element).getCost(startPeriod, endPeriod);
				}
			});
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
