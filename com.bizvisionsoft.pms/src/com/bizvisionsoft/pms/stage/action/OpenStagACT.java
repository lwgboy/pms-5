package com.bizvisionsoft.pms.stage.action;

import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.pms.work.action.OpenWorkPackage;
import com.bizvisionsoft.service.model.ProjectStatus;
import com.bizvisionsoft.service.model.Work;

public class OpenStagACT extends OpenWorkPackage{

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(Execute.PARAM_EVENT) Event event) {
		context.selected(em -> {
			Work work = (Work) em;
			if (work.isStage()) {
				if (ProjectStatus.Created.equals(work.getStatus())) {
					bruiService.switchPage("½×¶ÎÊ×Ò³£¨Æô¶¯£©", work.get_id().toHexString());
				} else if (ProjectStatus.Processing.equals(work.getStatus())) {
					bruiService.switchPage("½×¶ÎÊ×Ò³£¨Ö´ÐÐ£©", work.get_id().toHexString());
				} else if (ProjectStatus.Closing.equals(work.getStatus())) {
					bruiService.switchPage("½×¶ÎÊ×Ò³£¨ÊÕÎ²£©", work.get_id().toHexString());
				} else if (ProjectStatus.Closed.equals(work.getStatus())) {
					bruiService.switchPage("½×¶ÎÊ×Ò³£¨¹Ø±Õ£©", work.get_id().toHexString());
				}
			}

		});
	}

}
