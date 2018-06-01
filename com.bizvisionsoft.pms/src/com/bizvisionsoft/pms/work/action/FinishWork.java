package com.bizvisionsoft.pms.work.action;

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
		context.selected(elem -> {
			if (brui.confirm("完成工作", "请确认完成工作" + (Work) elem + "。\n系统将记录现在时刻为工作的实际完成时间。")) {
				if (Services.get(WorkService.class).finishWork(((Work) elem).get_id()).isEmpty()) {
					Layer.message("工作已完成。");
					GridPart grid = (GridPart) context.getContent();
					grid.remove(elem);
				}
			}
		});
	}

}
