package com.bizvisionsoft.pms.calendar.action;

import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.CommonService;
import com.bizvisionsoft.service.model.Calendar;
import com.bizvisionsoft.service.model.WorkTime;
import com.bizvisionsoft.serviceconsumer.Services;

public class EditElement {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		Object em = context.getFristElement();
		GridPart grid = (GridPart) context.getContent();
		if (em instanceof Calendar) {
			Editor.open("工作日历编辑器", context, em, (r, o) -> {
				grid.doModify(em, o, r);
			});
		} else if (em instanceof WorkTime) {
			Editor.open("工作时间编辑器", context, em, (r, o) -> {
				Services.get(CommonService.class).updateCalendarWorkTime(r);
				grid.replaceItem(em, o);
			});
		}
	}

}
