package com.bizvisionsoft.pms.cost;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.CBSService;
import com.bizvisionsoft.service.model.CBSItem;
import com.bizvisionsoft.service.model.CBSSubject;
import com.bizvisionsoft.service.model.CBSSubjectCost;
import com.bizvisionsoft.serviceconsumer.Services;

public class EditCBSSubjectCost {
	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		context.selected(em -> {
			if (em instanceof CBSSubjectCost) {
				CBSSubjectCost cbsSubjectCost = (CBSSubjectCost) em;
				CBSItem cbsItem = (CBSItem) context.getInput();
				Date settlementDate = cbsItem.getNextSettlementDate();
				Calendar cal = Calendar.getInstance();
				cal.setTime(settlementDate);
				String id = "" + cal.get(Calendar.YEAR);
				id += String.format("%02d", cal.get(java.util.Calendar.MONTH) + 1);
				CBSSubject cbsSubject = cbsSubjectCost.getCBSSubjects(id);
				if (cbsSubject == null) {
					cbsSubject = new CBSSubject().setCBSItem_id(cbsSubjectCost.getCbsItem().get_id())
							.setSubjectNumber(cbsSubjectCost.getId()).setId(id);
				}
				Editor.create("期间成本编辑器", context, cbsSubject, true).setTitle("科目期间成本").ok((r, o) -> {
					CBSSubject newSubjectCost = Services.get(CBSService.class).upsertCBSSubjectCost(o);
					cbsSubjectCost.updateCBSSubjects(newSubjectCost);
					GridPart grid = (GridPart) context.getContent();
					grid.refreshAll();
					grid.getViewer().expandAll();
				});
			}
		});
	}
}
