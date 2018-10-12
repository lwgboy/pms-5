package com.bizvisionsoft.pms.work.gantt.action;

import org.eclipse.swt.widgets.Event;

import com.bizivisionsoft.widgets.gantt.GanttEvent;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GanttPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Selector;
import com.bizvisionsoft.service.model.OBSItemWarpper;
import com.bizvisionsoft.service.model.WorkInfo;

public class SetCharger {
	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context,
			@MethodParam(Execute.EVENT) Event event) {
		WorkInfo wi = (WorkInfo) ((GanttEvent) event).task;
		new Selector(br.getAssembly("项目团队选择器"), context,true).setTitle("指定负责人").open(r -> {
			wi.setCharger((OBSItemWarpper) r.get(0));
			GanttPart content = (GanttPart) context.getContent();
			content.updateTask(wi);
		});
	}
}
