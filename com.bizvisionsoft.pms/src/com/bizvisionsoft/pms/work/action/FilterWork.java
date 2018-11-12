package com.bizvisionsoft.pms.work.action;

import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;

import com.bizivisionsoft.widgets.datetime.DateTimeSelector;
import com.bizivisionsoft.widgets.datetime.DateTimeSetting;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.service.tools.Formatter;
import com.mongodb.BasicDBObject;

public class FilterWork {

	@Inject
	private String filterType;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_CONTENT) GridPart grid, @MethodParam(Execute.EVENT) Event e) {

		new DateTimeSelector(DateTimeSetting.date()).bind(e.widget, SWT.LEFT).open(date -> {
			BasicDBObject condition = new BasicDBObject();
			if (date != null) {
				Date end = Formatter.getEndOfDay(date);
				if ("计划开始".equals(filterType)) {
					condition.append("planStart", new BasicDBObject("$lte", end));
				} else if ("计划完成".equals(filterType)) {
					condition.append("planFinish", new BasicDBObject("$lte", end));
				} else if ("实际完成".equals(filterType)) {
					Date start = Formatter.getStartOfDay(date);
					condition.append("actualFinish", new BasicDBObject("$lte", end).append("$gte", start));
				} else {
					throw new RuntimeException("缺少参数");
				}
			}
			grid.doQuery(condition);
			return true;
		});
	}

}
