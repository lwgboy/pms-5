package com.bizvisionsoft.pms.work.gantt.action;

import java.util.List;

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
		if (ganttPart.isDirty()) {
			ganttPart.save((t, l) -> submit(rootInput));
		} else {
			submit(rootInput);
		}
	}

	private void submit(IWBSScope rootInput) {
		Workspace workspace = rootInput.getWorkspace();
		if (!brui.confirm("提交项目计划", "请确认提交项目进度计划。")) {
			return;
		}
		List<Result> results = Services.get(WorkSpaceService.class).schedulePlanCheck(workspace, false);
		// 如果有错误，提示并返回
		String msg = results.stream().filter(r -> r.type == Result.TYPE_ERROR)
				.map(r -> "<span class='layui-badge'>错误</span> " + r.message + "<br>").reduce(String::concat).orElse(null);

		if (msg != null) {
			Layer.alert("项目计划存在一些问题", msg, 600, 400, false);
			return;
		}

		msg = results.stream().filter(r -> r.type == Result.TYPE_WARNING || r.type == Result.TYPE_INFO).map(r -> {
			if (r.type == Result.TYPE_WARNING)
				return "<span class='layui-badge layui-bg-orange'>警告</span> " + r.message + "<br>";
			return "<span class='layui-badge layui-bg-blue'>信息</span> " + r.message + "<br>";
		}).reduce(String::concat).orElse(null);
		if (msg == null || brui.confirm("提交项目计划", msg + "<br>请确认提交项目计划")) {
			Result result = Services.get(WorkSpaceService.class).checkin(workspace);
			if (Result.CODE_WORK_SUCCESS == result.code) {
				Layer.message(result.message);
				brui.switchContent("项目甘特图", null);
			}
		}
	}
}
