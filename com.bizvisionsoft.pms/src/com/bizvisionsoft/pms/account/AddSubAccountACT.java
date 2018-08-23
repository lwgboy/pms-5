package com.bizvisionsoft.pms.account;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.model.AccountIncome;
import com.bizvisionsoft.service.model.AccountItem;

public class AddSubAccountACT {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context) {
		context.selected(parent -> {
			Object input;
			if (parent instanceof AccountIncome) {
				input = new AccountIncome().setParentId(((AccountIncome) parent).getId());
			} else if (parent instanceof AccountItem) {
				input = new AccountItem().setParent_id(((AccountItem) parent).get_id());
			} else {
				return;
			}
			Editor.create("财务科目编辑器", context, input, true).ok((r, o) -> {
				GridPart grid = (GridPart) context.getContent();
				grid.doCreateSubItem(parent, o);
			});

		});
	}

}
