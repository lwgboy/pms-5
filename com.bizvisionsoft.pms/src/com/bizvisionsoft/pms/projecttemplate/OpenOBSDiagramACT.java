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
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) ProjectTemplate projectTemplate) {

		ObjectId pjtId = projectTemplate.get_id();

		if (!Services.get(ProjectTemplateService.class).hasOBS(pjtId)) {
			if (!brui.confirm("��֯�ṹģ��", "��δ������֯�ṹģ�壬�Ƿ�����")) {
				return;
			}
			Services.get(ProjectTemplateService.class).createRootOBS(pjtId);
		}
		brui.switchContent(brui.getAssembly("��Ŀģ����֯�ṹͼ"), projectTemplate);
//		brui.openContent(brui.getAssembly("��Ŀģ����֯�ṹͼ"), projectTemplate);
	}

}
