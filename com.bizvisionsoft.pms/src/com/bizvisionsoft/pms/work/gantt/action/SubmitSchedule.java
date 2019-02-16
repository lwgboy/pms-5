package com.bizvisionsoft.pms.work.gantt.action;

import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.MessageDialog;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GanttPart;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.WorkSpaceService;
import com.bizvisionsoft.service.model.IWBSScope;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.Workspace;
import com.bizvisionsoft.serviceconsumer.Services;

public class SubmitSchedule {
	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) IWBSScope rootInput,
			@MethodParam(Execute.CONTEXT_CONTENT) GanttPart ganttPart) {
		if (rootInput != null) {
			if (ganttPart.isDirty()) {
				ganttPart.save((t, l) -> submit(rootInput));
			} else {
				submit(rootInput);
			}
		}
	}

	private void submit(IWBSScope rootInput) {
		Workspace workspace = rootInput.getWorkspace();
		Assert.isNotNull(workspace, "当前的工作区不能为空");
		if (!MessageDialog.openConfirm(brui.getCurrentShell(), "提交项目计划", "请确认提交项目进度计划。")) {
			return;
		}
		List<Result> results = Services.get(WorkSpaceService.class).schedulePlanCheck(workspace);
		if (!results.isEmpty()) {
			StringBuffer sb = new StringBuffer();
			results.stream().filter(r -> r.type == Result.TYPE_ERROR).map(r -> {
				return "<span class='layui-badge'>错误</span> " + r.message + "<br>";
			}).forEach(sb::append);
			results.stream().filter(r -> r.type == Result.TYPE_WARNING).map(r -> {
				return "<span class='layui-badge layui-bg-orange'>警告</span> " + r.message + "<br>";
			}).forEach(sb::append);
			results.stream().filter(r -> r.type == Result.TYPE_QUESTION).map(r -> {
				return "<span class='layui-badge layui-bg-blue'>信息</span> " + r.message + "<br>";
			}).forEach(sb::append);
			MessageDialog.openInformation(brui.getCurrentShell(), "提交项目计划", sb.toString());
		} else {
			Result result = Services.get(WorkSpaceService.class).checkin(workspace);
			if (Result.CODE_WORK_SUCCESS == result.code) {
				Layer.message(result.message);
				brui.switchContent("项目甘特图", null);
			}

		}
	}
}
