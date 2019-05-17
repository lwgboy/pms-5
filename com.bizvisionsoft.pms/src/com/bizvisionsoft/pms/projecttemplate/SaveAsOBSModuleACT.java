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
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		// ������֯ģ��༭����������֯ģ����Ϣ��ģ���š�ģ�����ƺ�˵����
		Editor.open("��֯ģ��༭��", context, br.newInstance(OBSModule.class), (o, c) -> {
			// ���÷��񣬴�����֯ģ�壬������ǰ��Ŀģ�����֯�ṹ���Ƶ���֯ģ����
			Services.get(ProjectTemplateService.class).templateOBSSaveAsOBSModule(c,
					context.getRootInput(ProjectTemplate.class, false).get_id(), br.getDomain());
			Layer.message("��ǰ��Ŀģ�����֯�ṹ�����Ϊ��֯ģ�塣");
		});
	}
}
