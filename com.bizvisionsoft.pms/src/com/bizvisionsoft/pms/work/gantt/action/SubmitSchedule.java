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
		if (!brui.confirm("�ύ��Ŀ�ƻ�", "��ȷ���ύ��Ŀ���ȼƻ���")) {
			return;
		}
		List<Result> results = Services.get(WorkSpaceService.class).schedulePlanCheck(workspace, false);
		// ����д�����ʾ������
		String msg = results.stream().filter(r -> r.type == Result.TYPE_ERROR)
				.map(r -> "<span class='layui-badge'>����</span> " + r.message + "<br>").reduce(String::concat).orElse(null);

		if (msg != null) {
			Layer.alert("��Ŀ�ƻ�����һЩ����", msg, 600, 400, false);
			return;
		}

		msg = results.stream().filter(r -> r.type == Result.TYPE_WARNING || r.type == Result.TYPE_INFO).map(r -> {
			if (r.type == Result.TYPE_WARNING)
				return "<span class='layui-badge layui-bg-orange'>����</span> " + r.message + "<br>";
			return "<span class='layui-badge layui-bg-blue'>��Ϣ</span> " + r.message + "<br>";
		}).reduce(String::concat).orElse(null);
		if (msg == null || brui.confirm("�ύ��Ŀ�ƻ�", msg + "<br>��ȷ���ύ��Ŀ�ƻ�")) {
			Result result = Services.get(WorkSpaceService.class).checkin(workspace);
			if (Result.CODE_WORK_SUCCESS == result.code) {
				Layer.message(result.message);
				brui.switchContent("��Ŀ����ͼ", null);
			}
		}
	}
}
