package com.bizvisionsoft.pms.message;

import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;

import com.bizivisionsoft.widgets.datetime.DateTimeSelector;
import com.bizivisionsoft.widgets.datetime.DateTimeSetting;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.IQueryEnable;
import com.bizvisionsoft.service.tools.Formatter;
import com.mongodb.BasicDBObject;

public class FilterMsgCardACT {

	@Inject
	private String filterType;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_CONTENT) IQueryEnable grid, @MethodParam(Execute.EVENT) Event e) {

		new DateTimeSelector(DateTimeSetting.date()).bind(e.widget, SWT.LEFT).open(date -> {
			BasicDBObject condition = new BasicDBObject();
			if (date != null) {
				Date start = Formatter.getStartOfDay(date);
				Date end = Formatter.getEndOfDay(date);
				condition.append("sendDate", new BasicDBObject("$lte", end).append("$gte", start));
			}
			grid.doQuery(condition);
			return true;
		});
	}

}
