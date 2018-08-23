package com.bizvisionsoft.jz.project;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.WorkSpaceService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.ProjectStatus;
import com.bizvisionsoft.serviceconsumer.Services;

public class CreateExternalProject {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(Execute.PARAM_EVENT) Event event) {
		new Editor<Project>(bruiService.getAssembly("������Э��Ŀ�༭��"), context).setInput(new Project()
				.setStatus(ProjectStatus.Created).setStageEnable(true).setProjectType(Project.PROJECTTYPE_EXTERNAL)
				.setStartApproved(true).setCreationInfo(bruiService.operationInfo())).ok((r, proj) -> {
					Project pj = Services.get(ProjectService.class).insert(proj);
					if (pj != null) {
						Services.get(WorkSpaceService.class).checkout(pj.getWorkspace(), pj.getPmId(), true);
						if (MessageDialog.openQuestion(bruiService.getCurrentShell(), "������Ŀ", "��Ŀ�����ɹ����Ƿ������Ŀ��ҳ��")) {
							bruiService.switchPage("��Ŀ��ҳ��������", pj.get_id().toHexString());
						}
					}
				});

	}

}
