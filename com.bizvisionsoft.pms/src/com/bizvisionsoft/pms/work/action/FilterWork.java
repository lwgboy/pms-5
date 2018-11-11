package com.bizvisionsoft.pms.work.action;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;

import com.bizivisionsoft.widgets.datetime.DateTimeSelector;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.mongodb.BasicDBObject;

public class FilterWork {
	
	@Inject
	private String field;
	
	@Inject
	private String operator;
	

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_CONTENT) GridPart grid,
			@MethodParam(Execute.EVENT) Event e) {
		new DateTimeSelector().bind(e.widget, SWT.LEFT).open(date -> {
			//TODO 处理一下日期
			grid.doQuery(new BasicDBObject(field, new BasicDBObject(operator, date)));
			return true;
		});
	}

}
