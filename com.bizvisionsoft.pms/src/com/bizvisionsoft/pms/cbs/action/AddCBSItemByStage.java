package com.bizvisionsoft.pms.cbs.action;

import java.util.List;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.pms.cbs.assembly.BudgetCBS;
import com.bizvisionsoft.service.CBSService;
import com.bizvisionsoft.service.model.CBSItem;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.serviceconsumer.Services;
/**
 * 取消了CBS可分解功能，该操作无效
 * @author gdiyang
 * @date 2018/10/27
 *
 */
@Deprecated
public class AddCBSItemByStage {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		Object rootInput = context.getRootInput();
		if (rootInput instanceof Project) {
			Project project = (Project) rootInput;
			try {
				List<CBSItem> cbsItems = Services.get(CBSService.class).addCBSItemByStage(project.getCBS_id(),
						project.get_id());
				BudgetCBS budgetCBS = (BudgetCBS) context.getChildContextByName("cbs").getContent();
				CBSItem cbsRoot = (CBSItem) budgetCBS.getViewerInput().get(0);
				cbsRoot.addChild(cbsItems);
				budgetCBS.refresh(cbsRoot);
				budgetCBS.expandToLevel(cbsRoot, -1);
			} catch (Exception e) {
				String message = e.getMessage();
				if (message.indexOf("index") >= 0) {
					Layer.message("请勿在同一范围内重复添加相同编号的成本项", Layer.ICON_CANCEL);
				}
			}
		}
	}
}
