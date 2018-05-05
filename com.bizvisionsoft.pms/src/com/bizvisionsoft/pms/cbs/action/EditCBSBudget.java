package com.bizvisionsoft.pms.cbs.action;

import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.bruiengine.util.Util;
import com.bizvisionsoft.pms.cbs.assembly.BudgetCBS;
import com.bizvisionsoft.service.model.CBSItem;
import com.bizvisionsoft.service.model.CBSPeriod;
import com.bizvisionsoft.service.model.ICBSScope;

public class EditCBSBudget {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		context.selected(parent -> {
			CBSPeriod period = new CBSPeriod()//
					.setCBSItem_id(((CBSItem) parent).get_id());
			Util.ifInstanceThen(context.getRootInput(), ICBSScope.class, r -> period.setRange(r.getCBSRange()));

			Editor.create("ÆÚ¼äÔ¤Ëã±à¼­Æ÷", context, period, true).setTitle("±à¼­ÆÚ¼äÔ¤Ëã").ok((r, o) -> {
				BudgetCBS grid = (BudgetCBS) context.getContent();
				grid.updateCBSPeriodBudget(((CBSItem) parent),o);
			});

		});
	}

}
