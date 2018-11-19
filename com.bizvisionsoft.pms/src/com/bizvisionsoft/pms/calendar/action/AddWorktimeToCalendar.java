package com.bizvisionsoft.pms.calendar.action;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.CommonService;
import com.bizvisionsoft.service.model.Calendar;
import com.bizvisionsoft.service.model.WorkTime;
import com.bizvisionsoft.serviceconsumer.Services;
/**
 * 
 * @author gdiyang
 * @date 2018/10/27
 *
 */
public class AddWorktimeToCalendar {

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		context.selected(cal -> {
			Editor.create("工作时间编辑器", context, new WorkTime().set_id(new ObjectId()), false).ok((r, o) -> {
				GridPart grid = (GridPart) context.getContent();
				Services.get(CommonService.class).addCalendarWorktime(r, ((Calendar) cal).get_id());
				((Calendar) cal).addWorkTime(o);
				grid.refresh(cal);
			});

		});
	}

}
