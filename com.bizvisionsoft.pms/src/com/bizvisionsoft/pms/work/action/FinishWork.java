package com.bizvisionsoft.pms.work.action;

import java.util.Date;

import org.eclipse.swt.widgets.Event;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.serviceconsumer.Services;

public class FinishWork {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		context.selected(e -> {
			if (brui.confirm("��ɹ���", "��ȷ����ɹ���" + (Work) e + "��\nϵͳ����¼����ʱ��Ϊ������ʵ�����ʱ�䡣")) {
				if (Services.get(WorkService.class).finishWork(brui.command(((Work) e).get_id(), new Date()))
						.isEmpty()) {
					Layer.message("��������ɡ�");
					GridPart grid = (GridPart) context.getContent();
					grid.remove(e);
				}
			}
		});
	}

}
