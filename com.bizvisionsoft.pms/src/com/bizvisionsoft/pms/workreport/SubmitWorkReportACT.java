package com.bizvisionsoft.pms.workreport;

import java.util.Arrays;
import java.util.List;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.InfopadPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.WorkReportService;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.WorkReport;
import com.bizvisionsoft.serviceconsumer.Services;

public class SubmitWorkReportACT {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {

		WorkReport input = (WorkReport) context.getInput();
		boolean ok = brui.confirm("�ύ" + input.getType(),
				"��ȷ���ύ���棺" + input.getLabel() + "��\nϵͳ����¼����ʱ��Ϊ�����ύʱ�䣬�ύ��ñ��潫�޷������޸ġ�");
		if (!ok) {
			return;
		}
		List<Result> result = Services.get(WorkReportService.class).submitWorkReport(Arrays.asList(input.get_id()));
		if (result.isEmpty()) {
			Layer.message("�������ύ");
			InfopadPart ip = (InfopadPart) context.getChildContextByAssemblyName("�������������Ϣ���").getContent();
			ip.reload();
			brui.closeCurrentContent();
		} else {
			if (result.get(0).code == Result.CODE_WORKREPORT_HASNOSTATEMENTWORK)
				Layer.message("��Ϊ���й�����д�����r���ٽ����ύ��", Layer.ICON_CANCEL);
		}
	}
}
