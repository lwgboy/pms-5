package com.bizvisionsoft.pms.projecttemplate;

import java.util.List;

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
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context,
			@MethodParam(Execute.EVENT) GanttEvent event) {
		WorkInTemplate workinfo = (WorkInTemplate) event.task;
		String editor = "¹¤×÷ÊôÐÔ±à¼­Æ÷";
		Editor.create(editor, context, workinfo, false).setTitle(workinfo.toString()).ok((r, wi) -> {
			Services.get(ProjectTemplateService.class).updateWork(
					new FilterAndUpdate().filter(new BasicDBObject("_id", workinfo.get_id())).set(r).bson(), br.getDomain());
			List<TrackView> wps = wi.getWorkPackageSetting();
			workinfo.setWorkPackageSetting(wps);
		});

	}

}
