package com.bizvisionsoft.pms.projecttemplate;

import com.bizivisionsoft.widgets.gantt.GanttEvent;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GanttPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.model.WorkInTemplate;

public class AddMilestoneACT {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context, @MethodParam(Execute.EVENT) GanttEvent event) {
		// IWBSScope wbsScope = (IWBSScope) context.getRootInput();
		// 显示编辑器
		Editor.create("项目模板里程碑工作编辑器.editorassy", context,
				WorkInTemplate.newInstance((WorkInTemplate) event.task).setMilestone(true).setManageLevel("1"), false).ok((r, wi) -> {
					wi.setPlanFinish(wi.getPlanStart());
					GanttPart content = (GanttPart) context.getContent();
					content.addTask(wi);
				});
	}
}
