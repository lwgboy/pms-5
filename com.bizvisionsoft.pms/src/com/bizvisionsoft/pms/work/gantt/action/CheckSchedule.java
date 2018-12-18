package com.bizvisionsoft.pms.work.gantt.action;

import java.util.List;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.WorkSpaceService;
import com.bizvisionsoft.service.model.IWBSScope;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.model.Workspace;
import com.bizvisionsoft.serviceconsumer.Services;

public class CheckSchedule {
	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) IWBSScope rootInput) {
		//TODO ʹ��CommandHandler���д���
		if (rootInput != null) {
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
				
				List<Result> result = Services.get(WorkSpaceService.class).schedulePlanCheck(workspace, checkManageItem);

//				if (Result.CODE_WORK_SUCCESS == result.code) {
//					Layer.message(result.message);
//				} else if (Result.CODE_UPDATEMANAGEITEM == result.code) {
//					brui.error( "�����",
//							"����ڵ� <b style='color:red;'>" + result.data.getString("name") + "</b> �����ʱ�䳬���޶���");
//				} else if (Result.CODE_UPDATESTAGE == result.code) {
//					brui.error( "�����",
//							"���� <b style='color:red;'>" + result.data.getString("name") + "</b> �����ʱ�䳬���׶��޶���");
//				} else if (Result.CODE_UPDATEPROJECT == result.code) {
//					brui.error("�����",
//							"���� <b style='color:red;'>" + result.data.getString("name") + "</b> �����ʱ�䳬���Ŀ�޶���");
//				}
			}
		}
	}
}
