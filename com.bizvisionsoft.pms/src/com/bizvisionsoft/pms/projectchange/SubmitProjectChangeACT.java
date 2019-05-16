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

public class SubmitProjectChangeACT {

	@Inject
	private IBruiService br;

	@Execute
	private void execute(@MethodParam(Execute.CONTEXT_INPUT_OBJECT) ProjectChange input) {
		if (br.confirm("�ύ�������", "��ȷ���ύ������롣<br>ϵͳ����¼����ʱ��Ϊ��������ύʱ�䣬�ύ��ñ�����뽫�޷������޸ġ�")) {
			List<Result> result = Services.get(ProjectService.class).submitProjectChange(Arrays.asList(input.get_id()), br.getDomain());
			if (result.isEmpty()) {
				Layer.message("����������ύ");
				br.closeCurrentContent();
			} else {
				if (result.get(0).code == Result.CODE_PROJECTCHANGE_NOTASKUSER)
					Layer.message("��Ϊ������˻���ָ�������Ա���ٽ����ύ", Layer.ICON_ERROR);
			}
		}
	}
}
