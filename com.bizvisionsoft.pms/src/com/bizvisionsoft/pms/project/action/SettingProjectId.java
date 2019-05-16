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
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.ProjectScheduleInfo;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.serviceconsumer.Services;

public class SettingProjectId {
	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context, @MethodParam(Execute.EVENT) Event event) {
		context.selected(se -> {
			Project project;
			if (se instanceof ProjectScheduleInfo)
				project = ((ProjectScheduleInfo) se).getProject();
			else
				project = (Project) se;
			InputDialog id = new InputDialog(br.getCurrentShell(), "������Ŀ���", "��������Ŀ���", "",
					txt -> txt.trim().isEmpty() ? "��Ŀ��Ų���Ϊ��" : null);
			if (id.open() == Window.OK) {
				Services.get(ProjectService.class).updateProjectId(project.get_id(), id.getValue(), br.getDomain());
				project.setId(id.getValue());
				Check.instanceThen(context.getContent(), GridPart.class, grid -> grid.update(se));

			}
		});
	}

}
