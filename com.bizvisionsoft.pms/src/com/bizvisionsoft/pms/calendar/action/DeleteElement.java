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

public class DeleteElement {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		Object elem = context.getFirstElement();
		if (elem instanceof Calendar) {
			DeleteSelected.deleteElementInGrid(bruiService, context, elem);
		} else if (elem instanceof WorkTime) {
			String message = Optional.ofNullable(AUtil.readTypeAndLabel(elem)).map(m -> "��ȷ�Ͻ�Ҫɾ�� " + m)
					.orElse("��ȷ�Ͻ�Ҫɾ��ѡ��ļ�¼��");
			if (MessageDialog.openConfirm(bruiService.getCurrentShell(), "ɾ��", message)) {
				Services.get(CommonService.class).deleteCalendarWorkTime(((WorkTime) elem).get_id());
				GridPart grid = ((GridPart) context.getContent());
				grid.remove(grid.getParentElement(elem), elem);
			}
		}
	}

}
