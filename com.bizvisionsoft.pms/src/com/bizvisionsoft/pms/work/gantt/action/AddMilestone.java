package com.bizvisionsoft.pms.work.gantt.action;

import com.bizivisionsoft.widgets.gantt.GanttEvent;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GanttPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.model.WorkInfo;

public class AddMilestone {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context,
			@MethodParam(Execute.EVENT) GanttEvent event) {
		// IWBSScope wbsScope = (IWBSScope) context.getRootInput();
		// ÏÔÊ¾±à¼­Æ÷

		WorkInfo workInfo = WorkInfo.newInstance((WorkInfo) event.task).setMilestone(true)
				.setManageLevel("1");
		new Editor<WorkInfo>(br.getAssembly("¸ÊÌØÍ¼Àï³Ì±®¹¤×÷±à¼­Æ÷.editorassy"), context).setInput(workInfo).ok((r, wi) -> {
			wi.setPlanFinish(wi.getPlanStart());
			GanttPart content = (GanttPart) context.getContent();
			content.addTask(wi);
		});
	}

}
