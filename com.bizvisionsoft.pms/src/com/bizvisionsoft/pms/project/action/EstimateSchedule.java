package com.bizvisionsoft.pms.project.action;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.serviceconsumer.Services;

public class EstimateSchedule {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context) {
		Integer result = Services.get(ProjectService.class)
				.schedule(context.getRootInput(Project.class, false).get_id());
		// TODO ˢ��
		// TODO ����ͻ��˿����㰴ť��Ӧ����ÿ��ֻ����һ��
		// TODO ��̨�Զ�ˢ��
		if (result == 0) {
			Layer.message("<span style='color:red'>I��Ԥ��</span><br/>��Ŀ����Ԥ�Ƴ��ڡ�", Layer.ICON_CANCEL);
		} else if (result == 1) {
			Layer.message("<span style='color:red'>II��Ԥ��</span>", Layer.ICON_CANCEL);
		} else if (result == 2) {
			Layer.message("<span style='color:red'>III��Ԥ��</span>", Layer.ICON_CANCEL);
		} else {
			Layer.message("���ȹ������<br/>û��Ԥ����Ϣ��");
		}
	}

}
