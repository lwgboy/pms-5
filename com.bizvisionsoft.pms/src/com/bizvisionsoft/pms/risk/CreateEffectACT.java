package com.bizvisionsoft.pms.risk;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.RiskService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.RBSItem;
import com.bizvisionsoft.service.model.RiskEffect;
import com.bizvisionsoft.serviceconsumer.Services;

public class CreateEffectACT {

	@Inject
	private IBruiService br;

	@Execute
	private void execute(@MethodParam(Execute.CONTEXT) IBruiContext context,
			@MethodParam(Execute.ACTION) Action action) {
		RiskEffect re = new RiskEffect().setProject_id(((Project) context.getRootInput()).get_id())
				.setCreationInfo(br.operationInfo());
		context.selected(c -> {
			re.setRBSItem_id(((RBSItem) c).get_id());
			boolean positive = false;
			String title = "";
			if ("添加正面影响的工作".equals(action.getName())) {
				positive = true;
				title = "正面影响";
			} else if ("添加负面影响的工作".equals(action.getName())) {
				positive = false;
				title = "负面影响";
			}
			re.setPositive(positive);
			Editor.create("风险影响编辑器.editorassy", context, re, false).setTitle(title).ok((r, o) -> {
				o = Services.get(RiskService.class).addRiskEffect(o, br.getDomain());
				((GridPart) context.getContent()).add(c, o);
			});
		});
	}

}
