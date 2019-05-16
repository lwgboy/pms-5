package com.bizvisionsoft.pms.calendar.action;

import java.util.Optional;

import org.eclipse.jface.dialogs.MessageDialog;

import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.action.DeleteSelected;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
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
public class DeleteElement {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		Object elem = context.getFirstElement();
		if (elem instanceof Calendar) {
			DeleteSelected.deleteElementInGrid(br, context, elem);
		} else if (elem instanceof WorkTime) {
			String message = Optional.ofNullable(AUtil.readTypeAndLabel(elem)).map(m -> "请确认将要删除 " + m)
					.orElse("请确认将要删除选择的记录。");
			if (MessageDialog.openConfirm(br.getCurrentShell(), "删除", message)) {
				Services.get(CommonService.class).deleteCalendarWorkTime(((WorkTime) elem).get_id(), br.getDomain());
				GridPart grid = ((GridPart) context.getContent());
				grid.remove(grid.getParentElement(elem), elem);
			}
		}
	}

}
