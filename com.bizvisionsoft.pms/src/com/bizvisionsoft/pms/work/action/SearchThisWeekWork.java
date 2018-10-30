package com.bizvisionsoft.pms.work.action;

import java.util.Calendar;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.mongodb.BasicDBObject;

public class SearchThisWeekWork {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_CONTENT) GridPart gridPart) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		int i = 8-cal.get(Calendar.DAY_OF_WEEK);
		cal.add(Calendar.DAY_OF_MONTH, i);
		cal.add(Calendar.MILLISECOND, -1);
		gridPart.doQuery(new BasicDBObject("planFinish",new BasicDBObject("$lt",cal.getTime())));
	}

}
