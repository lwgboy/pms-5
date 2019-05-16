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
import com.bizvisionsoft.service.datatools.Query;
import com.bizvisionsoft.service.model.User;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.serviceconsumer.Services;

public class MySchedule {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService br;

	@DataSet({ "部门工作日程表/list" })
	public List<Work> createDeptUserWork() {
		String userid = br.getCurrentUserId();
		return Services.get(WorkService.class).createDeptUserWorkDataSet(userid, br.getDomain());

	}

	@DataSet({ "部门工作日程表/section" })
	public List<User> createDeptUser() {
		String userid = br.getCurrentUserId();
		return Services.get(UserService.class).createDeptUserDataSet(new Query().bson(), userid, br.getDomain());
	}

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context,
			@MethodParam(Execute.EVENT) Event event) {
		//TODO 
	}

}
