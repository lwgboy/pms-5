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
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_CONTENT) GanttPart ganttPart,
			@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) IWBSScope root) {
		if (ganttPart.isDirty()) {
			Layer.message("��ǰ����Ŀ�ƻ���δ����", Layer.ICON_CANCEL);
		} else {
			Workspace ws = root.getWorkspace();
			List<WorkInfo> workSet = Services.get(WorkSpaceService.class).createComparableWorkDataSet(ws.getSpace_id());
			List<WorkLinkInfo> linkSet = Services.get(WorkSpaceService.class)
					.createLinkDataSet(new BasicDBObject("space_id", ws.getSpace_id()));
			brui.openContent(brui.getAssembly("�Ƚϸ���ͼ"), new Object[] { workSet, linkSet });
		}
	}
}
