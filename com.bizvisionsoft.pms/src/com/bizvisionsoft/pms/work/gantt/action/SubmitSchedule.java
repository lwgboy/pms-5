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
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.WorkSpaceService;
import com.bizvisionsoft.service.model.IWBSScope;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.Work;
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
				Layer.message("��ǰ����Ŀ�ƻ���δ����", Layer.ICON_CANCEL);
			} else if (MessageDialog.openConfirm(brui.getCurrentShell(), "�ύ�ƻ�", "��ȷ���ύ��ǰ�ƻ���")) {
				submit(rootInput);
			}
		}
	}

	private void submit(IWBSScope rootInput) {
		Workspace workspace = rootInput.getWorkspace();
		if (workspace != null) {

			Boolean checkManageItem = true;
			Project project = null;
			if (rootInput instanceof Project) {
				project = Services.get(ProjectService.class).get(((Project) rootInput).get_id());
			} else if (rootInput instanceof Work) {
				project = ((Work) rootInput).getProject();
			}
			
			
			if (project != null && project.getChangeStatus() != null && "�����".equals(project.getChangeStatus()))
				checkManageItem = false;
			
			
			Result result = Services.get(WorkSpaceService.class).schedulePlanCheck(workspace, checkManageItem);

			if (Result.CODE_WORK_SUCCESS == result.code) {
				result = Services.get(WorkSpaceService.class).checkin(workspace);

				if (Result.CODE_WORK_SUCCESS == result.code) {
					Layer.message(result.message);
					brui.switchContent("��Ŀ����ͼ", null);
				}
			} else if (Result.CODE_UPDATEMANAGEITEM == result.code) {
				MessageDialog.openError(brui.getCurrentShell(), "�����",
						"����ڵ� <b style='color:red;'>" + result.data.getString("name") + "</b> ���ʱ�䳬���޶���");
			} else if (Result.CODE_UPDATESTAGE == result.code) {
				MessageDialog.openError(brui.getCurrentShell(), "�����",
						"���� <b style='color:red;'>" + result.data.getString("name") + "</b> �����ʱ�䳬���׶��޶���");
			} else if (Result.CODE_UPDATEPROJECT == result.code) {
				MessageDialog.openError(brui.getCurrentShell(), "�����",
						"���� <b style='color:red;'>" + result.data.getString("name") + "</b> �����ʱ�䳬���Ŀ�޶���");
			}
		}
	}
}
