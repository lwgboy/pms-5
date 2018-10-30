package com.bizvisionsoft.pms.work.gantt.action;

import com.bizivisionsoft.widgets.gantt.GanttEvent;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GanttPart;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.model.WorkLinkInfo;

public class DeleteLink {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_CONTENT) GanttPart part,
			@MethodParam(Execute.EVENT) GanttEvent event) {
		if (br.confirm("删除", "请确认将要删除选择的工作搭接关系。")) {
			WorkLinkInfo link = (WorkLinkInfo) event.link;
			part.deleteLink(link.getId());
		}
	}

}
