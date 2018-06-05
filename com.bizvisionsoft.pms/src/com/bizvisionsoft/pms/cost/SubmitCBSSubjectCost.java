package com.bizvisionsoft.pms.cost;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Event;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.CBSService;
import com.bizvisionsoft.service.model.CBSItem;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.serviceconsumer.Services;

public class SubmitCBSSubjectCost {
	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		//TODO 判断当前时间与期间相同无法提交
		
		
		CBSItem cbsItem = (CBSItem) context.getInput();
		Date settlementDate = cbsItem.getSettlementDate();
		Calendar cal = Calendar.getInstance();
		cal.setTime(settlementDate);
		String id = "" + cal.get(Calendar.YEAR);
		id += String.format("%02d", cal.get(java.util.Calendar.MONTH) + 1);
		if (MessageDialog.openConfirm(brui.getCurrentShell(), "提交期间成本", "请确认提交当前期间（" + cal.get(Calendar.YEAR) + "年"
				+ String.format("%02d", cal.get(java.util.Calendar.MONTH) + 1) + "月" + "）的成本。\n提交后将无法修改该期间的成本数据。")) {
			List<Result> result = Services.get(CBSService.class).submitCBSSubjectCost(cbsItem.getScope_id(), id);
			if (result.isEmpty()) {
				Layer.message("已完成当前期间（" + id + "）的成本提交。");
				GridPart grid = (GridPart) context.getContent();
				grid.refreshAll();
				grid.getViewer().expandAll();
			}
		}
	}
}
