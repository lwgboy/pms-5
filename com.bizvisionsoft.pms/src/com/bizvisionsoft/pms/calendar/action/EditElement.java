package com.bizvisionsoft.pms.calendar.action;

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
/**
 * 
 * @author gdiyang
 * @date 2018/10/27
 *
 */
public class EditElement {

	@Inject
	private IBruiService br;
	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Object em,
			@MethodParam(Execute.CONTEXT_CONTENT) GridPart grid, @MethodParam(Execute.CONTEXT) IBruiContext context) {
		if (em instanceof Calendar) {
			Editor.open("���������༭��", context, em, (r, o) -> {
				grid.doModify(em, o, r);
			});
		} else if (em instanceof WorkTime) {
			Editor.open("����ʱ��༭��", context, em, (r, o) -> {
				Services.get(CommonService.class).updateCalendarWorkTime(r, br.getDomain());
				grid.replaceItem(em, o);
			});
		}
	}

}
