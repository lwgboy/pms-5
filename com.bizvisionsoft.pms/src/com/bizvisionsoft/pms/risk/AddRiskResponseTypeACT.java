package com.bizvisionsoft.pms.risk;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.RiskService;
import com.bizvisionsoft.service.model.RiskResponse;
import com.bizvisionsoft.service.model.RiskResponseType;
import com.bizvisionsoft.serviceconsumer.Services;

public class AddRiskResponseTypeACT {

	@Inject
	private IBruiService brui;

	@Execute
	private void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		context.selected(c -> {
			String type = ((RiskResponseType) c).getType();
			Editor.create("项目风险应对计划编辑器-" + type, context,
					new RiskResponse().setType(type).setRBSItem_id(((RiskResponseType) c).get_id()), false)
					.setTitle("创建 " + type).ok((r, o) -> {
						Services.get(RiskService.class).insertRiskResponse(o);
						GridPart viewer = (GridPart) context.getContent();
						viewer.insert(r);
					});
		});
	}
}
