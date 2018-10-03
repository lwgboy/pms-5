package com.bizvisionsoft.pms.project.action;

import java.util.Date;

import org.eclipse.swt.widgets.Event;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.model.ICommand;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.tools.Checker;
import com.bizvisionsoft.serviceconsumer.Services;

public class ApproveProject {
	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(Execute.PARAM_EVENT) Event event) {
		context.selected(se -> {
			if (br.confirm("批准项目启动", "请确认是否批准项目启动。")) {
				Services.get(ProjectService.class)
						.approveProject(br.command(((Project) se).get_id(), new Date(), ICommand.Approve_Project));
				Checker.instanceThen(context.getContent(), GridPart.class,
						grid -> grid.update(((Project) se).setStartApproved(true)));
				Layer.message("已批准项目启动");
			}
		});
	}
}
