package com.bizvisionsoft.pms.resource;

import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.ResourcePlan;
import com.bizvisionsoft.serviceconsumer.Services;

public class DeleteResourcePlanACT {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		context.selected(em -> {
			ResourcePlan rp = (ResourcePlan) em;
			String usedEquipResId = rp.getUsedEquipResId();
			if (usedEquipResId != null)
				Services.get(WorkService.class).deleteEquipmentResourcePlan(rp.getWork_id(), usedEquipResId);

			String usedHumanResId = rp.getUsedHumanResId();
			if (usedHumanResId != null)
				Services.get(WorkService.class).deleteHumanResourcePlan(rp.getWork_id(), usedHumanResId);

			String usedTypedResId = rp.getUsedTypedResId();
			if (usedTypedResId != null)
				Services.get(WorkService.class).deleteTypedResourcePlan(rp.getWork_id(), usedTypedResId);

			GridPart content = (GridPart) context.getContent();
			content.remove(em);
		});
	}
}
