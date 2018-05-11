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
import com.bizvisionsoft.service.model.User;
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

	@DataSet({ "部门工作日程表/list" })
	public List<Work> data1() {
		ArrayList<Work> result = new ArrayList<Work>();

		Calendar cal = Calendar.getInstance();
		for (int i = 0; i < 10; i++) {
			cal.add(Calendar.DATE, -1);
			Date start = cal.getTime();
			cal.add(Calendar.DATE, 3);
			Date end = cal.getTime();
			result.add(new Work().setChargerId("zh").set_id(new ObjectId()).setStart_date(start).setEnd_date(end)
					.setText("工作甲" + i));
		}

		cal = Calendar.getInstance();
		for (int i = 0; i < 10; i++) {
			cal.add(Calendar.DATE, -2);
			Date start = cal.getTime();
			cal.add(Calendar.DATE, 2);
			Date end = cal.getTime();
			result.add(new Work().setChargerId("1").set_id(new ObjectId()).setStart_date(start).setEnd_date(end)
					.setText("工作乙" + i));
		}

		cal = Calendar.getInstance();
		for (int i = 0; i < 10; i++) {
			cal.add(Calendar.DATE, -3);
			Date start = cal.getTime();
			cal.add(Calendar.DATE, 1);
			Date end = cal.getTime();
			result.add(new Work().setChargerId("2").set_id(new ObjectId()).setStart_date(start).setEnd_date(end)
					.setText("工作丙" + i));
		}
		
		cal = Calendar.getInstance();
		for (int i = 0; i < 10; i++) {
			cal.add(Calendar.DATE, -3);
			Date start = cal.getTime();
			cal.add(Calendar.DATE, 1);
			Date end = cal.getTime();
			result.add(new Work().setChargerId("3").set_id(new ObjectId()).setStart_date(start).setEnd_date(end)
					.setText("工作丙" + i));
		}
		
		cal = Calendar.getInstance();
		for (int i = 0; i < 10; i++) {
			cal.add(Calendar.DATE, -3);
			Date start = cal.getTime();
			cal.add(Calendar.DATE, 1);
			Date end = cal.getTime();
			result.add(new Work().setChargerId("4").set_id(new ObjectId()).setStart_date(start).setEnd_date(end)
					.setText("工作丙" + i));
		}
		
		cal = Calendar.getInstance();
		for (int i = 0; i < 10; i++) {
			cal.add(Calendar.DATE, -3);
			Date start = cal.getTime();
			cal.add(Calendar.DATE, 1);
			Date end = cal.getTime();
			result.add(new Work().setChargerId("5").set_id(new ObjectId()).setStart_date(start).setEnd_date(end)
					.setText("工作丙" + i));
		}
		
		cal = Calendar.getInstance();
		for (int i = 0; i < 10; i++) {
			cal.add(Calendar.DATE, -3);
			Date start = cal.getTime();
			cal.add(Calendar.DATE, 1);
			Date end = cal.getTime();
			result.add(new Work().setChargerId("6").set_id(new ObjectId()).setStart_date(start).setEnd_date(end)
					.setText("工作丙" + i));
		}
		
		cal = Calendar.getInstance();
		for (int i = 0; i < 10; i++) {
			cal.add(Calendar.DATE, -3);
			Date start = cal.getTime();
			cal.add(Calendar.DATE, 1);
			Date end = cal.getTime();
			result.add(new Work().setChargerId("7").set_id(new ObjectId()).setStart_date(start).setEnd_date(end)
					.setText("工作丙" + i));
		}
		
		cal = Calendar.getInstance();
		for (int i = 0; i < 10; i++) {
			cal.add(Calendar.DATE, -3);
			Date start = cal.getTime();
			cal.add(Calendar.DATE, 1);
			Date end = cal.getTime();
			result.add(new Work().setChargerId("8").set_id(new ObjectId()).setStart_date(start).setEnd_date(end)
					.setText("工作丙" + i));
		}
		
		cal = Calendar.getInstance();
		for (int i = 0; i < 10; i++) {
			cal.add(Calendar.DATE, -3);
			Date start = cal.getTime();
			cal.add(Calendar.DATE, 1);
			Date end = cal.getTime();
			result.add(new Work().setChargerId("9").set_id(new ObjectId()).setStart_date(start).setEnd_date(end)
					.setText("工作丙" + i));
		}

		return result;
	}

	@DataSet({ "部门工作日程表/section" })
	public List<User> data2() {
		ArrayList<User> result = new ArrayList<User>();
		User user = new User().setName("1").setUserId("zh");
		result.add(user);
		user = new User().setName("2").setUserId("1");
		result.add(user);
		result.add(new User().setName("阿斯达2").setUserId("2"));
		result.add(new User().setName("123安德森").setUserId("3"));
		result.add(new User().setName("阿斯达").setUserId("4"));
		result.add(new User().setName("21213").setUserId("5"));
		result.add(new User().setName("阿斯达2 1啊啊").setUserId("6"));
		result.add(new User().setName("啊撒地方").setUserId("7"));
		result.add(new User().setName("法国和").setUserId("8"));
		result.add(new User().setName("总成行").setUserId("9"));
		return result;
	}

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		System.out.println(context);
	}

}
