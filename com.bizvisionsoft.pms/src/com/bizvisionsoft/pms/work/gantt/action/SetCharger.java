package com.bizvisionsoft.pms.work.gantt.action;

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
			@MethodParam(Execute.EVENT) GanttEvent event) {
		WorkInfo wi = (WorkInfo) event.task;
		new Selector(br.getAssembly("��Ŀ�Ŷ�ѡ����"), context,true).setTitle("ָ��������").open(r -> {
			wi.setCharger((OBSItemWarpper) r.get(0));
			GanttPart content = (GanttPart) context.getContent();
			content.updateTask(wi);
		});
	}
}
