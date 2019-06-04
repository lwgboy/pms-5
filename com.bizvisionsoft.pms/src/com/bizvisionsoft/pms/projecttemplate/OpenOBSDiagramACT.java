package com.bizvisionsoft.pms.projecttemplate;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.ProjectTemplateService;
import com.bizvisionsoft.service.model.ProjectTemplate;
import com.bizvisionsoft.serviceconsumer.Services;

public class OpenOBSDiagramACT {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) ProjectTemplate projectTemplate) {

		ObjectId pjtId = projectTemplate.get_id();

		if (!Services.get(ProjectTemplateService.class).hasOBS(pjtId, br.getDomain())) {
			if (!br.confirm("组织结构模板", "尚未建立组织结构模板，是否建立？")) {
				return;
			}
			Services.get(ProjectTemplateService.class).createRootOBS(pjtId, br.getDomain());
		}
		br.switchContent(br.getAssembly("项目模板组织结构图.treeassy"), projectTemplate);
//		brui.openContent(brui.getAssembly("项目模板组织结构图"), projectTemplate);
	}

}
