package com.bizvisionsoft.pms.work.gantt.action;

import java.util.List;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GanttPart;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.WorkSpaceService;
import com.bizvisionsoft.service.model.IWBSScope;
import com.bizvisionsoft.service.model.WorkInfo;
import com.bizvisionsoft.service.model.WorkLinkInfo;
import com.bizvisionsoft.service.model.Workspace;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class CompareSchedule {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_CONTENT) GanttPart ganttPart,
			@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) IWBSScope root) {
		if (ganttPart.isDirty()) {
			Layer.message("当前的项目计划还未保存", Layer.ICON_ERROR);
		} else {
			Workspace ws = root.getWorkspace();
			List<WorkInfo> workSet = Services.get(WorkSpaceService.class).createComparableWorkDataSet(ws.getSpace_id(), br.getDomain());
			List<WorkLinkInfo> linkSet = Services.get(WorkSpaceService.class)
					.createLinkDataSet(new BasicDBObject("space_id", ws.getSpace_id()), br.getDomain());
			br.openContent(br.getAssembly("比较甘特图.ganttassy"), new Object[] { workSet, linkSet });
		}
	}
}
