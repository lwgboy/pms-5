package com.bizvisionsoft.pms.work.gantt.action;

import org.eclipse.jface.dialogs.MessageDialog;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.UserService;
import com.bizvisionsoft.service.WorkSpaceService;
import com.bizvisionsoft.service.model.IWBSScope;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.User;
import com.bizvisionsoft.service.model.Workspace;
import com.bizvisionsoft.serviceconsumer.Services;

public class EditGantt {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) IWBSScope rootInput) {
		Workspace workspace = rootInput.getWorkspace();
		if (workspace != null) {
			// 显示编辑器
			String checkoutUserId = workspace.getCheckoutBy();
			if (checkoutUserId == null || "".equals(checkoutUserId) || br.getCurrentUserId().equals(checkoutUserId)) {
				// 开发检出服务，名称：checkoutSchedulePlan，参数要考虑如下：
				Result result = Services.get(WorkSpaceService.class).checkout(workspace, br.getCurrentUserId(), false, br.getDomain());
				if (Result.CODE_WORK_SUCCESS == result.code) {
					br.switchContent("项目甘特图（编辑）.ganttassy", workspace);
				} else if (Result.CODE_HASCHECKOUTSUB == result.code) {
					if (MessageDialog.openConfirm(br.getCurrentShell(), "提示",
							"本计划中的  <b style='color: red;'>" + result.data.get("name") + "</b>  工作正在由   <b style='color: red;'>"
									+ result.data.get("username") + "</b>  进行计划编辑。" + "继续需编辑本计划，将撤销该用户未提交的计划。")) {
						result = Services.get(WorkSpaceService.class).checkout(workspace, br.getCurrentUserId(), true, br.getDomain());
						if (Result.CODE_WORK_SUCCESS == result.code) {
							br.switchContent("项目甘特图（编辑）.ganttassy", workspace);
						}
					}
				}
			} else {
				User checkoutUser = Services.get(UserService.class).get(checkoutUserId, br.getDomain());
				br.error("提示", checkoutUser + "已经检出计划，在该计划检入或取消检出以前，不能进行编辑。");
			}
		}
	}

}
