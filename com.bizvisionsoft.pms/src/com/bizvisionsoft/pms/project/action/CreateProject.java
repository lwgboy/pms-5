package com.bizvisionsoft.pms.project.action;

import org.eclipse.jface.dialogs.MessageDialog;

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

public class CreateProject {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		new Editor<Project>(br.getAssembly("������Ŀ�༭��"), context)
				.setInput(br.newInstance(Project.class).setStatus(ProjectStatus.Created).setStageEnable(true).setCreationInfo(br.operationInfo()))
				.ok((r, proj) -> {
					Project pj = Services.get(ProjectService.class).insert(proj, br.getDomain());
					if (pj != null) {
						Services.get(WorkSpaceService.class).checkout(pj.getWorkspace(), pj.getPmId(), true, br.getDomain());
						if (MessageDialog.openQuestion(br.getCurrentShell(), "������Ŀ", "��Ŀ�����ɹ����Ƿ������Ŀ��ҳ��")) {
							br.switchPage("��Ŀ��ҳ��������", pj.get_id().toHexString());
						}
					}
				});

	}

}
