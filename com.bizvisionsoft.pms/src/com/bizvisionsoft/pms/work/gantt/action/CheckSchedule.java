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

public class CheckSchedule {
	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) IWBSScope rootInput,
			@MethodParam(Execute.CONTEXT_CONTENT) GanttPart ganttPart) {
		if (ganttPart.isDirty()) {
			ganttPart.save((t, l) -> check(rootInput));
		} else {
			check(rootInput);
		}
	}

	private void check(IWBSScope rootInput) {
		Workspace workspace = rootInput.getWorkspace();
		List<Result> result = Services.get(WorkSpaceService.class).schedulePlanCheck(workspace, true, br.getDomain());
		String content = result.stream().map(r -> {
			switch (r.type) {
			case Result.TYPE_ERROR:
				return "<span class='layui-badge'>错误</span> " + r.message + "<br>";
			case Result.TYPE_WARNING:
				return "<span class='layui-badge layui-bg-orange'>警告</span> " + r.message + "<br>";
			default:
				return "<span class='layui-badge layui-bg-blue'>信息</span> " + r.message + "<br>";
			}
		}).reduce(String::concat).orElse(null);
		if (content != null) {
			Layer.alert("项目计划检查", content, 600, 400, false);
		} else {
			Layer.message("已通过检查。");
		}
	}
}
