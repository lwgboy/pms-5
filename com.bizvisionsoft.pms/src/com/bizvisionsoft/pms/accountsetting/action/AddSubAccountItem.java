package com.bizvisionsoft.pms.accountsetting.action;

import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.CommonService;
import com.bizvisionsoft.service.model.AccountItem;
import com.bizvisionsoft.serviceconsumer.Services;

public class AddSubAccountItem {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		context.selected(parent -> {
			Editor.create("财务科目编辑器", context, new AccountItem().setParent_id(((AccountItem) parent).get_id()), true)
					.ok((r, o) -> {
						AccountItem item = Services.get(CommonService.class).insertAccountItem(o);
						GridPart grid = (GridPart) context.getContent();
						((AccountItem) parent).addChild(item);
						grid.refresh(parent);
					});

		});
	}

}
