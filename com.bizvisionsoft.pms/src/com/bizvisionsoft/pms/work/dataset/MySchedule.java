package com.bizvisionsoft.pms.work.dataset;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.model.Work;

public class MySchedule {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService brui;

	@DataSet({ "我的工作（日历牌）/list" })
	public List<Work> data() {
		ArrayList<Work> result = new ArrayList<Work>();

		Calendar cal = Calendar.getInstance();
		for (int i = 0; i < 10; i++) {
			cal.add(Calendar.DATE, -1);
			Date start = cal.getTime();
			cal.add(Calendar.DATE, 3);
			Date end = cal.getTime();
			result.add(new Work().set_id(new ObjectId()).setStart_date(start).setEnd_date(end).setText("工作" + i));
		}
		
		return result;
	}
	
	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		System.out.println(context);
	}

}
