package com.bizvisionsoft.pms.work.gantt.action;

import java.util.Arrays;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GanttPart;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.util.CommandHandler;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.WorkSpaceService;
import com.bizvisionsoft.service.model.IWBSScope;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.Work;
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
		if (workspace != null) {

			Boolean checkManageItem;
			Project project = null;
			if (rootInput instanceof Project) {
				project = Services.get(ProjectService.class).get(((Project) rootInput).get_id());
			} else if (rootInput instanceof Work) {
				project = ((Work) rootInput).getProject();
			}
			if (project != null && project.getChangeStatus() != null && "�����".equals(project.getChangeStatus()))
				checkManageItem = false;
			else
				checkManageItem = true;

			CommandHandler.run("�ύ��Ŀ�ƻ�", "��ȷ���ύ��Ŀ" + project + " �Ľ��ȼƻ���", "��Ŀ���ȼƻ��ύ�ɹ���", "��Ŀ���ȼƻ��ύʧ�ܡ�",
					() -> Services.get(WorkSpaceService.class).schedulePlanCheck(workspace, checkManageItem),
					() -> Arrays.asList(Services.get(WorkSpaceService.class).checkin(workspace)),
					code -> brui.switchContent("��Ŀ����ͼ", null));
		}
	}
}
