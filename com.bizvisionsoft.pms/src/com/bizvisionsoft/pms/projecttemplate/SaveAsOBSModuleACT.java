package com.bizvisionsoft.pms.projecttemplate;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.ProjectTemplateService;
import com.bizvisionsoft.service.model.OBSModule;
import com.bizvisionsoft.service.model.ProjectTemplate;
import com.bizvisionsoft.serviceconsumer.Services;

public class SaveAsOBSModuleACT {
	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		// 弹出组织模板编辑器，输入组织模板信息（模板编号、模板名称和说明）
		Editor.open("组织模板编辑器", context, new OBSModule(), (o, c) -> {
			// 调用服务，创建组织模板，并将当前项目模板的组织结构复制到组织模板中
			Services.get(ProjectTemplateService.class).templateOBSSaveAsOBSModule(c,
					context.getRootInput(ProjectTemplate.class, false).get_id());
			Layer.message("当前项目模板的组织结构已另存为组织模板。");
		});
	}
}
