package com.bizvisionsoft.pms.cbs.action;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.pms.cbs.assembly.BudgetCBS;
import com.bizvisionsoft.service.model.CBSItem;
import com.bizvisionsoft.service.model.CBSPeriod;
import com.bizvisionsoft.service.model.ICBSScope;
import com.bizvisionsoft.service.tools.Check;

public class EditCBSBudget {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		context.selected(item -> {
			CBSPeriod period = new CBSPeriod()//
					.setCBSItem_id(((CBSItem) item).get_id());
			Check.instanceThen(context.getRootInput(), ICBSScope.class, r -> period.setRange(r.getCBSRange()));

			Editor.create("ÆÚ¼äÔ¤Ëã±à¼­Æ÷", context, period, true).setTitle("±à¼­ÆÚ¼äÔ¤Ëã").ok((r, o) -> {
				try {
					Date periodDate = new SimpleDateFormat("yyyyMM").parse(o.getId());
					o.checkRange(periodDate);
					
					BudgetCBS grid = (BudgetCBS) context.getContent();
					grid.updateCBSPeriodBudget(((CBSItem) item), o);

				} catch (ParseException e) {
					Layer.message(e.getMessage(), Layer.ICON_CANCEL);
				}
			});

		});
	}

}
