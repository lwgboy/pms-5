package com.bizvisionsoft.pms.account;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.CommonService;
import com.bizvisionsoft.service.model.AccountIncome;
import com.bizvisionsoft.serviceconsumer.Services;

public class AddSubAccountIncomeACT {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context) {
		context.selected(parent -> {
			Editor.create("财务科目编辑器", context, new AccountIncome().setParent_id(((AccountIncome) parent).get_id()), true)
					.ok((r, o) -> {
						AccountIncome item = Services.get(CommonService.class).insertAccountIncome(o);
						GridPart grid = (GridPart) context.getContent();
						((AccountIncome) parent).addChild(item);
						grid.refresh(parent);
					});

		});
	}

}
