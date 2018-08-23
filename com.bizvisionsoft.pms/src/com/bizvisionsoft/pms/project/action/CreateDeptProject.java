package com.bizvisionsoft.pms.project.action;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.md.mongocodex.Generator;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.CommonService;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.WorkSpaceService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.ProjectStatus;
import com.bizvisionsoft.serviceconsumer.Services;

public class CreateDeptProject {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(Execute.PARAM_EVENT) Event event) {
		new Editor<Project>(bruiService.getAssembly("创建部门项目编辑器"), context).setInput(new Project()
				.setStatus(ProjectStatus.Created).setStageEnable(true).setProjectType(Project.PROJECTTYPE_DEPT)
				.setStartApproved(true).setCreationInfo(bruiService.operationInfo())).ok((r, proj) -> {
					int index = ServicesLoader.get(CommonService.class).generateCode(Generator.DEFAULT_NAME,
							"deptproject");
					proj.setId(String.format("%05d", index));
					Project pj = Services.get(ProjectService.class).insert(proj);
					if (pj != null) {
						Services.get(WorkSpaceService.class).checkout(pj.getWorkspace(), pj.getPmId(), true);
						if (MessageDialog.openQuestion(bruiService.getCurrentShell(), "创建部门项目", "部门项目创建成功，是否进入项目主页？")) {
							bruiService.switchPage("项目首页（启动）", pj.get_id().toHexString());
						}
					}
				});

	}

}
