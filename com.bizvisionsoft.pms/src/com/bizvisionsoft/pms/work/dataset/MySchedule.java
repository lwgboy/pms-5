package com.bizvisionsoft.pms.work.dataset;

import java.util.List;

import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.UserService;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.User;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.serviceconsumer.Services;

public class MySchedule {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService brui;

	@DataSet({ "部门工作日程表/list" })
	public List<Work> createDeptUserWork() {
		String userid = brui.getCurrentUserId();
		return Services.get(WorkService.class).createDeptUserWorkDataSet(userid);

	}

	@DataSet({ "部门工作日程表/section" })
	public List<User> createDeptUser() {
		String userid = brui.getCurrentUserId();
		return Services.get(UserService.class).createDeptUserDataSet(userid);
	}

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		System.out.println(context);
	}

}
