package com.bizvisionsoft.pms.obs.action;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Selector;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.OBSItem;
import com.bizvisionsoft.service.model.OBSItemWarpper;
import com.bizvisionsoft.service.model.User;
import com.bizvisionsoft.serviceconsumer.Services;

public class TransferOBSMember {
	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Object em,
			@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) Object rootInput, @MethodParam(Execute.CONTEXT_INPUT_OBJECT) Object input,
			@MethodParam(Execute.CONTEXT) IBruiContext context) {
		if (br.confirm("移交", "请确认将 " + em + " 的工作进行移交。")) {
			// 弹出项目团队成员选择器，选择被移交人员
			new Selector(br.getAssembly("项目团队选择器.selectorassy"), context).setInput(rootInput).setTitle("选择工作移交人").open(l -> {
				Services.get(WorkService.class).transferWorkUser(((OBSItem) input).getScope_id(), ((User) em).getUserId(),
						((OBSItemWarpper) l.get(0)).getUserId(), br.getCurrentUserId(), br.getDomain());
			});
		}
	}
}
