package com.bizvisionsoft.pms.resource;

import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.ResourceActual;
import com.bizvisionsoft.serviceconsumer.Services;

public class DeleteResourceActualACT {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(Execute.PARAM_EVENT) Event event) {
		context.selected(em -> {
			ResourceActual ra = (ResourceActual) em;
			String usedEquipResId = ra.getUsedEquipResId();
			if (usedEquipResId != null)
				Services.get(WorkService.class).deleteEquipmentResourceActual(ra.getWork_id(), usedEquipResId);

			String usedHumanResId = ra.getUsedHumanResId();
			if (usedHumanResId != null)
				Services.get(WorkService.class).deleteHumanResourceActual(ra.getWork_id(), usedHumanResId);

			String usedTypedResId = ra.getUsedTypedResId();
			if (usedTypedResId != null)
				Services.get(WorkService.class).deleteTypedResourceActual(ra.getWork_id(), usedTypedResId);

			GridPart content = (GridPart) context.getContent();
			content.remove(em);
		});
	}
}
