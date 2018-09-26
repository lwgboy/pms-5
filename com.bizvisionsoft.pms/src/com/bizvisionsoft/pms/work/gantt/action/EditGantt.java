package com.bizvisionsoft.pms.work.gantt.action;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.WorkSpaceService;
import com.bizvisionsoft.service.model.IWBSScope;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.Workspace;
import com.bizvisionsoft.serviceconsumer.Services;

public class EditGantt {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(Execute.PARAM_EVENT) Event event) {
		IWBSScope rootInput = (IWBSScope) context.getRootInput();
		if (rootInput != null) {
			Workspace workspace = rootInput.getWorkspace();
			if (workspace != null) {
				// 显示编辑器
				String checkoutUserId = workspace.getCheckoutBy();
				if (checkoutUserId == null || "".equals(checkoutUserId)
						|| brui.getCurrentUserId().equals(checkoutUserId)) {
					// 开发检出服务，名称：checkoutSchedulePlan，参数要考虑如下：
					Result result = Services.get(WorkSpaceService.class).checkout(workspace, brui.getCurrentUserId(),
							false);
					if (Result.CODE_WORK_SUCCESS == result.code) {
						brui.switchContent("项目甘特图（编辑）", workspace);
					} else if (Result.CODE_HASCHECKOUTSUB == result.code) {
						if (MessageDialog.openConfirm(brui.getCurrentShell(), "提示",
								"本计划中的  <b style='color: red;'>" + result.data.get("name")
										+ "</b>  工作正在由   <b style='color: red;'>" + result.data.get("username")
										+ "</b>  进行计划编辑。" + "继续需编辑本计划，将撤销该用户未提交的计划。")) {
							result = Services.get(WorkSpaceService.class).checkout(workspace, brui.getCurrentUserId(),
									true);
							if (Result.CODE_WORK_SUCCESS == result.code) {
								brui.switchContent("项目甘特图（编辑）", workspace);
							}
						}
					}
				} else {
					MessageDialog.openError(brui.getCurrentShell(), "提示", "计划已经由其他用户检出：" + checkoutUserId);
				}
			}
		}
	}

}
