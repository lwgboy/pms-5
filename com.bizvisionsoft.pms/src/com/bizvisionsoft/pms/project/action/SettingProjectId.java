package com.bizvisionsoft.pms.project.action;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.util.EngUtil;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.serviceconsumer.Services;

public class SettingProjectId {
	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(Execute.PARAM_EVENT) Event event) {
		context.selected(se -> {
			InputDialog id = new InputDialog(br.getCurrentShell(), "������Ŀ���", "��������Ŀ���", "",
					txt -> txt.trim().isEmpty() ? "��Ŀ��Ų���Ϊ��" : null);
			if (id.open() == Window.OK) {
				Services.get(ProjectService.class).updateProjectId(((Project) se).get_id(), id.getValue());
				EngUtil.ifInstanceThen(context.getContent(), GridPart.class,
						grid -> grid.update(((Project) se).setId(id.getValue())));

			}
		});
	}

}
