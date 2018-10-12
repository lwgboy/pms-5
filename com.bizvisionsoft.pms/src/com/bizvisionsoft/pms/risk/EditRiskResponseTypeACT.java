package com.bizvisionsoft.pms.risk;

import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.RiskService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.RiskResponse;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class EditRiskResponseTypeACT {

	@Inject
	private IBruiService brui;

	@Execute
	private void execute(@MethodParam(Execute.CONTEXT) IBruiContext context,
			@MethodParam(Execute.ACTION) Action action) {
		context.selected(c -> {
			if (c instanceof RiskResponse) {
				String type = ((RiskResponse) c).getType();
				Editor.create("项目风险应对计划编辑器-" + type, context, ((RiskResponse) c), false).setTitle("创建 " + type)
						.ok((r, o) -> {
							Services.get(RiskService.class).updateRiskResponse(
									new FilterAndUpdate().filter(new BasicDBObject("_id", o.get_id())).set(r).bson());
							GridPart viewer = (GridPart) context.getContent();
							AUtil.simpleCopy(o, c);
							viewer.update(c);
						});
			}
		});
	}
}
