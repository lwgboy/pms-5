package com.bizvisionsoft.pms.projectchange;

import java.util.Arrays;
import java.util.List;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.model.ProjectChange;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.serviceconsumer.Services;

public class ConfirmProjectChangeACT {

	@Inject
	private IBruiService brui;

	@Execute
	private void execute(@MethodParam(Execute.CONTEXT_INPUT_OBJECT) ProjectChange input) {
		if (brui.confirm("�رձ������", "��ȷ�ϱ�������Ŀ���ȼƻ��ѱ�����ɲ��ύ��")) {
			List<Result> result = Services.get(ProjectService.class).confirmProjectChange(Arrays.asList(input.get_id()),
					brui.getCurrentUserId());
			if (result.isEmpty()) {
				Layer.message("����ѹر�");
				brui.closeCurrentContent();
			}
		}
	}
}
