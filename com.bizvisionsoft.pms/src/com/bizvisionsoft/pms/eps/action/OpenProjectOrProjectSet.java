package com.bizvisionsoft.pms.eps.action;

import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.ProjectSet;
import com.bizvisionsoft.service.model.ProjectStatus;

public class OpenProjectOrProjectSet {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		context.selected(em -> {
			if (em instanceof ProjectSet) {

			} else if (em instanceof Project) {
				if (ProjectStatus.Created.equals(((Project) em).getStatus())) {
					bruiService.switchPage("项目首页（启动）", ((Project) em).get_id().toHexString());
				} else if (ProjectStatus.Processing.equals(((Project) em).getStatus())) {
					bruiService.switchPage("项目首页（执行）", ((Project) em).get_id().toHexString());
				}
			}
		});

	}

}
