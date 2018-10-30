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
		if (brui.confirm("������Ŀģ��",
				"������Ŀģ�彫<span class='layui-badge'>�滻</span>��Ŀ�Ľ��ȼƻ�����Ŀ�Ŷӣ���ȷ��������Ŀģ�塣")) {
			Selector.open("��Ŀģ��ѡ����", context, null, l -> {
				Services.get(ProjectTemplateService.class).useTemplate(((ProjectTemplate) l.get(0)).get_id(),
						project.get_id(), brui.getCurrentUserId());
				Layer.message("ģ�������õ�����Ŀ��<br/>��༭��Ŀ���ȼƻ����Ŷ�");
			});

		}
	}
}
