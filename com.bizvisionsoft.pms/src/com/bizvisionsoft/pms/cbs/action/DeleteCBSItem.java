package com.bizvisionsoft.pms.cbs.action;

import java.util.Optional;

import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.pms.cbs.assembly.BudgetCBS;
import com.bizvisionsoft.service.model.CBSItem;

public class DeleteCBSItem {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		context.selected(elem -> {
			deleteElementInGrid(br, context, elem);
		});
	}

	public static void deleteElementInGrid(IBruiService br, IBruiContext context, Object elem) {
		String message = Optional.ofNullable(AUtil.readTypeAndLabel(elem)).map(m -> "请确认将要删除 " + m)
				.orElse("请确认将要删除选择的记录。");

		if (br.confirm("删除", message)) {
			BudgetCBS cbsGrid = (BudgetCBS) context.getContent();
			cbsGrid.deleteCBSItem((CBSItem) elem);
		}
	}
}
