package com.bizvisionsoft.pms.work.dataset;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.model.WorkInfo;

public class MySchedule {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService brui;

	@DataSet({ "我的工作（日历牌）/list" })
	public List<WorkInfo> data() {
		ArrayList<WorkInfo> result = new ArrayList<WorkInfo>();

		Calendar cal = Calendar.getInstance();
		for (int i = 0; i < 10; i++) {
			cal.add(Calendar.DATE, -1);
			Date start = cal.getTime();
			cal.add(Calendar.DATE, 3);
			Date end = cal.getTime();
			result.add(new WorkInfo().set_id(new ObjectId()).setStart_date(start).setEnd_date(end).setText("工作" + i));
		}
		
		return result;
	}

}
