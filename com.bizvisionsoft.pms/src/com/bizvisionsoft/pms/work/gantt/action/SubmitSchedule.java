package com.bizvisionsoft.pms.work.gantt.action;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Event;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GanttPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.WorkSpaceService;
import com.bizvisionsoft.service.model.IWBSScope;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.Workspace;
import com.bizvisionsoft.serviceconsumer.Services;

public class SubmitSchedule {
	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(Execute.PARAM_EVENT) Event event) {
		IWBSScope rootInput = (IWBSScope) context.getRootInput();
		if (rootInput != null) {
			GanttPart ganttPart = (GanttPart) context.getContent();
			if (ganttPart.isDirty()) {
				Layer.message("当前的项目计划还未保存。", Layer.ICON_CANCEL);
			} else if (MessageDialog.openConfirm(brui.getCurrentShell(), "提交计划", "请确认提交当前计划。")) {
				submit(rootInput);
			}
		}
	}

	private void submit(IWBSScope rootInput) {
		Workspace workspace = rootInput.getWorkspace();
		if (workspace != null) {
			Result result = null;
			if (!(rootInput instanceof Project) || !"变更中".equals(((Project) rootInput).getChangeStatus()))
				result = Services.get(WorkSpaceService.class).schedulePlanCheck(workspace, true);
			else
				result = Services.get(WorkSpaceService.class).schedulePlanCheck(workspace, false);

			if (Result.CODE_WORK_SUCCESS == result.code) {
				result = Services.get(WorkSpaceService.class).checkin(workspace);

				if (Result.CODE_WORK_SUCCESS == result.code) {
					Layer.message(result.message);
					brui.switchContent("项目甘特图", null);
				}
			}  else if (Result.CODE_UPDATEMANAGEITEM == result.code) {
				MessageDialog.openError(brui.getCurrentShell(), "检查结果",
						"管理节点 <b style='color:red;'>" + result.data.getString("name") + "</b> 完成时间超过限定。");
			} else if (Result.CODE_UPDATESTAGE == result.code) {
				MessageDialog.openError(brui.getCurrentShell(), "检查结果",
						"工作计划中最晚完成时间超过阶段  <b style='color:red;'>" + result.data.getString("name") + "</b>限定。");
			} else if (Result.CODE_UPDATEPROJECT == result.code) {
				MessageDialog.openError(brui.getCurrentShell(), "检查结果",
						"工作计划中最晚完成时间超过项目 <b style='color:red;'>" + result.data.getString("name") + "</b>限定。");
			}
		}
	}
}
