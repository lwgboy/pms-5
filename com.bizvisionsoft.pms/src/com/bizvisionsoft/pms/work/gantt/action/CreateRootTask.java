package com.bizvisionsoft.pms.work.gantt.action;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruicommons.model.Assembly;
import com.bizvisionsoft.bruiengine.assembly.GanttPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.model.IWBSScope;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.ProjectStatus;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.model.WorkInfo;

public class CreateRootTask {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		IWBSScope rootInput = (IWBSScope) context.getRootInput();
		String title;
		String editor;
		WorkInfo workInfo;
		// if (rootInput instanceof Project) {
		// workInfo = WorkInfo.newInstance((Project) rootInput);

		if (rootInput instanceof Project) {
			workInfo = WorkInfo.newInstance((Project) rootInput);
		} else {
			workInfo = WorkInfo.newInstance((Work) rootInput);
		}
		if ((rootInput instanceof Project) && ((Project) rootInput).isStageEnable()) {
			title = "创建阶段";
			editor = "甘特图阶段工作编辑器.editorassy";
			workInfo.setManageLevel("1").setStage(true).setStatus(ProjectStatus.Created);
		} else {
			title = "创建工作";
			editor = "甘特图工作编辑器.editorassy";
		}
		Editor.create(editor, context, workInfo, false).setTitle(title).ok((r, wi) -> {
			GanttPart content = (GanttPart) context.getContent();
			content.addTask(wi);
		});
		// }
	}

}
