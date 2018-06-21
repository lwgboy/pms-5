package com.bizvisionsoft.pms.resource;

import org.bson.types.ObjectId;
import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.ResourcePlan;
import com.bizvisionsoft.service.model.ResourceTransfer;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.serviceconsumer.Services;

public class EditResourcePlanACT {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		context.selected(em -> {
			ResourcePlan rp = (ResourcePlan) em;
			ObjectId work_id = rp.getWork_id();
			Work work = Services.get(WorkService.class).getWork(work_id);
			ResourceTransfer rt = new ResourceTransfer();
			rt.addWorkIds(work_id);
			rt.setType(ResourceTransfer.TYPE_PLAN);
			rt.setShowType(ResourceTransfer.SHOWTYPE_ONEWORK_MULTIRESOURCE);
			rt.setFrom(work.getPlanStart());
			rt.setTo(work.getPlanFinish());
			rt.setCheckTime(false);
			rt.setCanAdd(true);
			rt.setCanClose(true);
			rt.setShowResActual(false);
			rt.setShowResPlan(false);
			rt.setShowResTypeInfo(false);
			rt.setShowDelete(false);
			
			brui.openContent(brui.getAssembly("编辑资源情况"), rt);
		});
	}
}
