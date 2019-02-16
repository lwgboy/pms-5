package com.bizvisionsoft.pms.work.gantt.action;

import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.WorkSpaceService;
import com.bizvisionsoft.service.model.IWBSScope;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.Workspace;
import com.bizvisionsoft.serviceconsumer.Services;

public class CheckSchedule {
	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) IWBSScope rootInput) {
		// TODO 使用CommandHandler进行处理
		if (rootInput != null) {
			Workspace workspace = rootInput.getWorkspace();
			if (workspace != null) {
				List<Result> result = Services.get(WorkSpaceService.class).schedulePlanCheck(workspace);
				if (!result.isEmpty()) {
					StringBuffer sb = new StringBuffer();
					result.stream().filter(r -> r.type == Result.TYPE_ERROR).map(r -> {
						return "<span class='layui-badge'>错误</span> " + r.message + "<br>";
					}).forEach(sb::append);
					result.stream().filter(r -> r.type == Result.TYPE_WARNING).map(r -> {
						return "<span class='layui-badge layui-bg-orange'>警告</span> " + r.message + "<br>";
					}).forEach(sb::append);
					result.stream().filter(r -> r.type == Result.TYPE_QUESTION).map(r -> {
						return "<span class='layui-badge layui-bg-blue'>信息</span> " + r.message + "<br>";
					}).forEach(sb::append);
					MessageDialog.openInformation(brui.getCurrentShell(), "项目计划检查", sb.toString());
					//TODO 显示多行的问题
				} else {
					Layer.message("已通过检查。");
				}
			}
		}
	}
}
