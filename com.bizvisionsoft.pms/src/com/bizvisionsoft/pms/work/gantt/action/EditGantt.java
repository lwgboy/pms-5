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
import com.bizvisionsoft.serviceconsumer.Services;

public class EditGantt {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		IWBSScope wbsScope = (IWBSScope) context.getRootInput();

		// 显示编辑器
		String checkOutUserId = wbsScope.getCheckOutUserId();
		if (checkOutUserId == null || "".equals(checkOutUserId) || brui.getCurrentUserId().equals(checkOutUserId)) {
			// 开发检出服务，名称：checkOutSchedulePlan，参数要考虑如下：
			Result result = Services.get(WorkSpaceService.class).checkOutSchedulePlan(wbsScope.getCheckOutKey(),
					brui.getCurrentUserId(), false);
			if (Result.CODE_SUCCESS == result.code) {
				brui.switchContent("项目甘特图(编辑)", wbsScope);
			} else if (Result.CODE_HASCHECKOUTSUB == result.code) {
				if (MessageDialog.openConfirm(brui.getCurrentShell(), "提示", result.message + "请确认是否取消下级的检出。")) {
					result = Services.get(WorkSpaceService.class).checkOutSchedulePlan(wbsScope.getCheckOutKey(),
							brui.getCurrentUserId(), true);
					if (Result.CODE_SUCCESS == result.code) {
						brui.switchContent("项目甘特图(编辑)", wbsScope);
					}
				}
			}
		} else {
			MessageDialog.openError(brui.getCurrentShell(), "提示", "您没有编辑计划的权限。");
		}
		// 参数要传当前用户id。判断如果不是自己有权检出的报错。
		// 参数要传 是否取消下级检出 （cancelCheckOutSubSchedule）。
		// 该服务调用方式如下：
		// checkOutSchedulePlan( WBS节点下划线Id, “张三”，不取消下级检出)
		// 1.如果不是自己有权检出的，返回结果
		// 单个com.bizvisionsoft.service.model .Result对象，errorCode你自己定一个 作为Result的常量。
		// 2.这时如果有下级检出的话，并且cancelCheckOutSubSchedule为false，返回结果
		// 单个com.bizvisionsoft.service.model .Result对象，errorCode你自己定一个 作为Result的常量。
		// 客户端显示错误提示。如果错误代码是“有下级检出”的代码时。提示中询问是否取消下级的检出。如果是，调用以下：
		// checkOutSchedulePlan( WBS节点下划线Id, “张三”，取消下级检出)
		// 3.如果有下级检出的话，并且cancelCheckOutSubSchedule为true，取消下级检出，修改检出状态，清除工作区的下级检出内容。发出通知（暂时不做）。红色部分开发为另一服务（cancelCheckOutSchedulePlan（work_id,
		// userId））
		// 4.标记WBS节点下划线Id，以及其所有下级被检出。标记内容：检出人，时间，copy
		// 返回成功Result。

		// Editor.create("项目甘特图(编辑)", context.setInput(project),project,
		// false).setTitle("进度计划甘特图").ok((r, o) -> {
		// });
	}

}
