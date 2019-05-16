package com.bizvisionsoft.pms.project.action;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.ProjectTemplateService;
import com.bizvisionsoft.service.model.OBSModule;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.serviceconsumer.Services;

public class SaveAsOBSModule {
	

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		Project project = context.getRootInput(Project.class, false);
		// 弹出组织模板编辑器，输入组织模板信息（模板编号、模板名称和说明，默认适用范围为当前项目所在EPS节点，可进行修改。）
		Editor.open("组织模板编辑器", context, new OBSModule().addEPS_id(project.getEps_id()), (o, c) -> {
			// 调用服务，创建组织模板，并将当前项目的组织结构复制到组织模板中
			Services.get(ProjectTemplateService.class).projectOBSSaveAsOBSModule(c, project.get_id(), br.getDomain());
			Layer.message("当前项目的组织结构已另存为组织模板。");
		});
	}
}
