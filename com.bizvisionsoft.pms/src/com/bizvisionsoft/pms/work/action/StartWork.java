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

public class StartWork {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(Execute.PARAM_EVENT) Event event) {
		context.selected(elem -> {

			if (brui.confirm("启动工作", "请确认启动工作" + elem + "。\n系统将记录现在时刻为工作的实际开始时间。")) {
				if (Services.get(WorkService.class).startWork(brui.command(((Work) elem).get_id(), new Date()))
						.isEmpty()) {
					Layer.message("工作已启动");
					Work t = Services.get(WorkService.class).getWork(((Work) elem).get_id());
					GridPart grid = (GridPart) context.getContent();
					grid.replaceItem(elem, t);
				}
			}
		});
	}

}
