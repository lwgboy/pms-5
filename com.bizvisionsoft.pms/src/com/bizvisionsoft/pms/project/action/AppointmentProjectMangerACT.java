package com.bizvisionsoft.pms.project.action;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Selector;
import com.bizvisionsoft.pms.obs.action.AbstractChangeOBSItemMember;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.User;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.serviceconsumer.Services;

public class AppointmentProjectMangerACT extends AbstractChangeOBSItemMember {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Object em,
			@MethodParam(Execute.CONTEXT) IBruiContext context) {
		Project project = (Project) em;
		new Selector(br.getAssembly("用户选择器—单选"), context).setTitle("指定项目经理").open(r -> {
			if (checkChange(br, project.getOBS_id(), project.get_id(), project.getPmId(), "appointmentobsitem",
					"指定项目经理")) {
				User user = (User) r.get(0);
				Project p = Services.get(ProjectService.class).appointmentProjectManger(project.get_id(),
						user.getUserId(), br.getCurrentUserId());
				AUtil.simpleCopy(p, em);
				Check.instanceThen(context.getContent(), GridPart.class, grid -> grid.update(em));
				Layer.message("已完成项目经理的指定");
			}
		});
	}
}
