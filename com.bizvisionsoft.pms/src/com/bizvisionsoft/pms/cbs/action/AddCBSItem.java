package com.bizvisionsoft.pms.cbs.action;

import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.pms.cbs.assembly.BudgetCBS;
import com.bizvisionsoft.service.model.CBSItem;

public class AddCBSItem {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		context.selected(parent -> {
			Editor.create("成本项编辑器", context, CBSItem.getInstance((CBSItem) parent), true).setTitle("添加子项").ok((r, o) -> {
				BudgetCBS cbsGrid = (BudgetCBS) context.getContent();
				cbsGrid.addCBSItem((CBSItem) parent, o);
			});

		});
	}
}
