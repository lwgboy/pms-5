package com.bizvisionsoft.pms.project.action;

import org.eclipse.swt.widgets.Event;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Selector;
import com.bizvisionsoft.service.ProjectTemplateService;
import com.bizvisionsoft.service.model.OBSModule;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.serviceconsumer.Services;

public class UseOBSModule {
	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context,
			@MethodParam(Execute.EVENT) Event event) {
		Project project = (Project) context.getRootInput();
		if (brui.confirm("������֯ģ��", "������֯ģ�彫<span class='layui-badge'>�滻</span>��Ŀ�Ŷӣ���ȷ��������֯ģ�塣")) {
			Selector.open("��֯ģ��ѡ����", context, project, l -> {
				Services.get(ProjectTemplateService.class).useOBSModule(((OBSModule) l.get(0)).get_id(),
						project.get_id());
				Layer.message("��֯ģ�������õ�����Ŀ��");
			});

		}
	}
}
