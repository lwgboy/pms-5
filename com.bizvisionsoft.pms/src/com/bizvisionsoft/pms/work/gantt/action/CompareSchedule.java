package com.bizvisionsoft.pms.work.gantt.action;

import java.util.List;

import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
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
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		IWBSScope root = (IWBSScope) context.getRootInput();
		Workspace ws = root.getWorkspace();
		List<WorkInfo> workSet = Services.get(WorkSpaceService.class).createComparableWorkDataSet(root.getScope_id());
		List<WorkLinkInfo> linkSet = Services.get(WorkSpaceService.class)
				.createLinkDataSet(new BasicDBObject("space_id", ws.getSpace_id()));
		brui.openContent(brui.getAssembly("±»Ωœ∏ ÃÿÕº"), new Object[] { workSet, linkSet });
	}
}
