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
		// ������֯ģ��༭����������֯ģ����Ϣ��ģ���š�ģ�����ƺ�˵����Ĭ�����÷�ΧΪ��ǰ��Ŀ����EPS�ڵ㣬�ɽ����޸ġ���
		Editor.open("��֯ģ��༭��", context, new OBSModule().addEPS_id(project.getEps_id()), (o, c) -> {
			// ���÷��񣬴�����֯ģ�壬������ǰ��Ŀ����֯�ṹ���Ƶ���֯ģ����
			Services.get(ProjectTemplateService.class).projectOBSSaveAsOBSModule(c, project.get_id(), br.getDomain());
			Layer.message("��ǰ��Ŀ����֯�ṹ�����Ϊ��֯ģ�塣");
		});
	}
}
