package com.bizvisionsoft.pms.calendar.action;

import java.util.Date;

import org.bson.types.ObjectId;

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
import com.bizvisionsoft.service.tools.Formatter;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

/**
 * 
 * @author gdiyang
 * @date 2018/10/27
 *
 */
public class AddWorktimeToCalendar {

	@Inject
	private String editor;
	
	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		context.selected(cal -> {
			Editor.create(editor, context, new WorkTime(), false).ok((r, wt) -> {
				if (wt.dates != null) {
					addWorkTimeItem((Calendar) cal, wt.dates.get(0), wt.dates.get(1), r, (GridPart) context.getContent());
				} else {
					addWorkTimeItem((Calendar) cal, wt.date, null, r, (GridPart) context.getContent());
				}
			});

		});
	}

	private void addWorkTimeItem(Calendar cal, Date from, Date to, BasicDBObject r, GridPart grid) {
		String name = r.getString("name");
		if (to == null || from == null) {
			addWorkTimeItem(cal, from, r, name);
		} else {
			java.util.Calendar start = java.util.Calendar.getInstance();
			start.setTime(Formatter.getStartOfDay(from));

			java.util.Calendar end = java.util.Calendar.getInstance();
			end.setTime(Formatter.getStartOfDay(to));

			while (!start.after(end)) {
				addWorkTimeItem(cal, start.getTime(), r, name + " (" + Formatter.getString(start.getTime()) + ")");
				start.add(java.util.Calendar.DATE, 1);
			}
		}

		grid.refresh(cal);
	}

	private void addWorkTimeItem(Calendar cal, Date date, BasicDBObject r, String name) {
		if (date != null) {
			r.append("date", date);
		}
		r.append("_id", new ObjectId()).append("name", name);
		Services.get(CommonService.class).addCalendarWorktime(r, ((Calendar) cal).get_id(), br.getDomain());
		cal.addWorkTime((WorkTime) new WorkTime().decodeBson(r));
	}

}
