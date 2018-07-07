package com.bizvisionsoft.pms.cost;

import java.util.Calendar;
import java.util.Date;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.CBSService;
import com.bizvisionsoft.service.model.CBSSubject;
import com.bizvisionsoft.service.model.CBSSubjectCost;
import com.bizvisionsoft.serviceconsumer.Services;

public class EditCBSSubjectCostACT {
	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context) {
		context.selected(em -> {
			if (em instanceof CBSSubjectCost) {
				CBSSubjectCost cbsSubjectCost = (CBSSubjectCost) em;
				// 获取编辑成本的期间
				Date settlementDate = ((CBSSubjectCost) em).getCbsItem().getNextSettlementDate();
				Calendar cal = Calendar.getInstance();
				cal.setTime(settlementDate);
				String id = "" + cal.get(Calendar.YEAR);
				id += String.format("%02d", cal.get(java.util.Calendar.MONTH) + 1);
				// 从CBSSubjectCost对象中获取该期间所对应的CBSSubject，为空时创建新的CBSSubject
				CBSSubject cbsSubject = cbsSubjectCost.getCBSSubjects(id);
				if (cbsSubject == null) {
					cbsSubject = new CBSSubject().setCBSItem_id(cbsSubjectCost.getCbsItem().get_id())
							.setSubjectNumber(cbsSubjectCost.getId()).setId(id);
				}
				Editor.create("期间成本编辑器", context, cbsSubject, true).setTitle("科目:" + cbsSubjectCost.getName() + " 期间 :"
						+ cal.get(Calendar.YEAR) + "年" + (cal.get(java.util.Calendar.MONTH) + 1) + "月").ok((r, o) -> {
							CBSSubject newSubjectCost = Services.get(CBSService.class).upsertCBSSubjectCost(o);
							// 更新CBSSubjectCost存储的CBSSubject
							cbsSubjectCost.updateCBSSubjects(newSubjectCost);
							GridPart grid = (GridPart) context.getContent();
							grid.refreshAll();
							grid.getViewer().expandAll();
						});
			}
		});
	}
}
