package com.bizvisionsoft.pms.projecttemplate;

import org.bson.types.ObjectId;
import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.ProjectTemplateService;
import com.bizvisionsoft.service.model.ProjectTemplate;
import com.bizvisionsoft.serviceconsumer.Services;

public class OpenOBSDiagramACT {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context,
			@MethodParam(Execute.EVENT) Event event) {

		ProjectTemplate projectTemplate = context.getRootInput(ProjectTemplate.class, false);
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
