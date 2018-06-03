package com.bizvisionsoft.pms.projecttemplate;

import java.util.List;

import org.eclipse.swt.widgets.Event;

import com.bizivisionsoft.widgets.gantt.GanttEvent;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.ProjectTemplateService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.TrackView;
import com.bizvisionsoft.service.model.WorkInTemplate;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class SetWorkPackageACT {
	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		WorkInTemplate workinfo = (WorkInTemplate) ((GanttEvent) event).task;
		String editor = "�������Ա༭��";
		Editor.create(editor, context, workinfo, false).setTitle(workinfo.toString()).ok((r, wi) -> {
			Services.get(ProjectTemplateService.class).updateWork(
					new FilterAndUpdate().filter(new BasicDBObject("_id", workinfo.get_id())).set(r).bson());
			List<TrackView> wps = wi.getWorkPackageSetting();
			workinfo.setWorkPackageSetting(wps);
		});

	}

}
