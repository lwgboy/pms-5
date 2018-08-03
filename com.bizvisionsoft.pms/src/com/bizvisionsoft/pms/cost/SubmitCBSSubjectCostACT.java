package com.bizvisionsoft.pms.cost;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.CBSService;
import com.bizvisionsoft.service.model.CBSItem;
import com.bizvisionsoft.service.model.ICBSScope;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.serviceconsumer.Services;

public class SubmitCBSSubjectCostACT {
	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context) {
		// 获取当前显示的CBSItem根，主页打开时从contextInput中获取，项目、阶段打开时从contextRootInput中获取
		Object input = context.getInput();
		if (input == null) {
			input = context.getRootInput();
			if (input instanceof ICBSScope) {
				ICBSScope icbsScope = (ICBSScope) input;
				input = Services.get(CBSService.class).get(icbsScope.getCBS_id());
			}
		}

		if (input instanceof CBSItem) {
			CBSItem cbsItem = (CBSItem) input;
			// 获取编辑的成本期间
			Date settlementDate = cbsItem.getNextSettlementDate();
			Calendar cal = Calendar.getInstance();
			int newYear = cal.get(Calendar.YEAR);
			int newMonth = cal.get(Calendar.MONTH);
			cal.setTime(settlementDate);
			// 编辑的成本期间为当前年月时，禁止提交。
			if (cal.get(Calendar.YEAR) == newYear && cal.get(Calendar.MONTH) == newMonth) {
				cal.add(Calendar.MONTH, -1);
				Layer.message(
						"禁止重复提交期间（" + cal.get(Calendar.YEAR) + "年"
								+ String.format("%02d", cal.get(java.util.Calendar.MONTH) + 1) + "月" + "）成本。",
						Layer.ICON_CANCEL);
				return;
			}
			// 提交当前期间成本
			// cal.set(Calendar.DATE, 1);
			// cal.set(Calendar.HOUR, 0);
			// cal.set(Calendar.MINUTE, 0);
			// cal.set(Calendar.SECOND, 0);
			// cal.set(Calendar.MILLISECOND, 0);
			// cal.add(Calendar.MILLISECOND, -1);

			if (MessageDialog.openConfirm(brui.getCurrentShell(), "确认期间成本",
					"请确认提交当前期间（" + cal.get(Calendar.YEAR) + "年"
							+ String.format("%02d", cal.get(java.util.Calendar.MONTH) + 1) + "月"
							+ "）的成本。\n提交后将无法修改该期间的成本数据。")) {
				List<Result> result = Services.get(CBSService.class).submitCBSSubjectCost(cal.getTime(),
						cbsItem.getScope_id());
				if (result.isEmpty()) {
					Layer.message("已完成当前期间（" + cal.get(Calendar.YEAR) + "年"
							+ String.format("%02d", cal.get(java.util.Calendar.MONTH) + 1) + "月" + "）的成本提交");
					GridPart grid = (GridPart) context.getContent();
					grid.refreshAll();
					grid.getViewer().expandAll();
				} else {
					for (Result r : result)
						if (Result.CODE_CBS_REPEATSUBMIT == r.code) {
							Layer.message("禁止重复提交期间（" + cal.get(Calendar.YEAR) + "年"
									+ String.format("%02d", cal.get(java.util.Calendar.MONTH) + 1) + "月" + "）成本。",
									Layer.ICON_CANCEL);
							return;
						}
				}
			}
		}
	}

}
