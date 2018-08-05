package com.bizvisionsoft.pms.project.action;

import org.bson.types.ObjectId;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.serviceconsumer.Services;

public class EstimateSchedule {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context) {
		Object rootInput = context.getRootInput();
		ObjectId project_id = null;
		if (rootInput instanceof Project) {
			project_id = ((Project) rootInput).get_id();
		} else if (rootInput instanceof Work) {
			project_id = ((Work) rootInput).getProject_id();
		}

		if (project_id != null) {
			Integer result = Services.get(ProjectService.class).schedule(project_id);
			// TODO 刷新
			// TODO 避免客户端狂点估算按钮，应考虑每天只运行一次
			// TODO 后台自动刷新
			if (result == 0) {
				Layer.message("<span style='color:red'>I级预警</span><br/>项目进度预计超期", Layer.ICON_CANCEL);
			} else if (result == 1) {
				Layer.message("<span style='color:red'>II级预警</span>", Layer.ICON_CANCEL);
			} else if (result == 2) {
				Layer.message("<span style='color:red'>III级预警</span>", Layer.ICON_CANCEL);
			} else {
				Layer.message("进度估算完成<br/>没有预警信息");
			}
			((GridPart) context.getContent()).refreshAll();
		}
	}

}
