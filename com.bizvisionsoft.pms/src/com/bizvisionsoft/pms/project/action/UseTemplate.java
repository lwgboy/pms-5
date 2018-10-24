package com.bizvisionsoft.pms.project.action;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Selector;
import com.bizvisionsoft.service.ProjectTemplateService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.ProjectTemplate;
import com.bizvisionsoft.serviceconsumer.Services;

public class UseTemplate {
	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		Project project = (Project) context.getRootInput();
		if (brui.confirm("套用项目模板",
				"套用项目模板将<span class='layui-badge'>替换</span>项目的进度计划、项目团队，请确认套用项目模板。")) {
			Selector.open("项目模板选择器", context, null, l -> {
				Services.get(ProjectTemplateService.class).useTemplate(((ProjectTemplate) l.get(0)).get_id(),
						project.get_id(), brui.getCurrentUserId());
				Layer.message("模板已套用到本项目，<br/>请编辑项目进度计划和团队");
			});

		}
	}
}
