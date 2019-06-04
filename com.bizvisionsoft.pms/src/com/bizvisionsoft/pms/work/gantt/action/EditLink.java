package com.bizvisionsoft.pms.work.gantt.action;

import com.bizivisionsoft.widgets.gantt.GanttEvent;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GanttPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;

public class EditLink {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context,
			@MethodParam(Execute.EVENT) GanttEvent event) {
		Editor.open("������ӹ�ϵ�༭����1��1��.editorassy", context, event.link, (r, wi) -> {
			GanttPart content = (GanttPart) context.getContent();
			content.updateLink(wi);
		});
	}

}
