package com.bizvisionsoft.pms.obs.action;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Selector;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.OBSItem;
import com.bizvisionsoft.service.model.OBSItemWarpper;
import com.bizvisionsoft.serviceconsumer.Services;

public class TransferOBSItem {
	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Object em,
			@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) Object rootInput, @MethodParam(Execute.CONTEXT) IBruiContext context) {
		OBSItem obsItem = (OBSItem) em;
		// 如果所选OBSItem没有担任者，则不需要进行移交
		if (obsItem.getManagerId() == null) {
			Layer.error("无人员需要移交工作.");
			return;
		}
		if (br.confirm("移交", "请确认将 " + em + " 的工作进行移交。")) {
			// 弹出项目团队成员选择器，选择被移交人员
			new Selector(br.getAssembly("项目团队选择器"), context).setTitle("选择工作移交人").setInput(rootInput).open(l -> {
				Services.get(WorkService.class).transferWorkUser(obsItem.getScope_id(), obsItem.getManagerId(),
						((OBSItemWarpper) l.get(0)).getUserId(), br.getCurrentUserId());
			});
		}
	}
}
