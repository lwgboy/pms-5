package com.bizvisionsoft.pms.obs.action;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.serviceconsumer.Services;

public class AssignToWork {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) Object input) {
		if (br.confirm("���乤��", "��ȷ�Ͻ���ɫ�ĵ����߷��䵽�����ĸ����˺�ָ���ߡ�")) {
			if (input instanceof Project) {
				Services.get(WorkService.class).assignRoleToProject(((Project) input).get_id());
			} else if (input instanceof Work) {
				Services.get(WorkService.class).assignRoleToStage(((Work) input).get_id());
			}
			Layer.message("����ɽ�ɫ�Ĺ������䡣");
		}
	}
}
