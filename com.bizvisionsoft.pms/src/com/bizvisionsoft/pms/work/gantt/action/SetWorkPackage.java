package com.bizvisionsoft.pms.work.gantt.action;

import java.util.List;

import org.eclipse.swt.widgets.Event;

import com.bizivisionsoft.widgets.gantt.GanttEvent;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.IStructuredDataPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.WorkSpaceService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.TrackView;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.model.WorkInfo;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class SetWorkPackage {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context, @MethodParam(Execute.EVENT) Event event) {
		String editor = "¹¤×÷ÊôÐÔ±à¼­Æ÷";

		if (event instanceof GanttEvent) {
			WorkInfo workinfo = (WorkInfo) ((GanttEvent) event).task;
			Editor.create(editor, context, workinfo, false).setTitle(workinfo.toString()).ok((r, wi) -> {
				Services.get(WorkSpaceService.class)
						.updateWork(new FilterAndUpdate().filter(new BasicDBObject("_id", workinfo.get_id())).set(r).bson());
				List<TrackView> wps = wi.getWorkPackageSetting();
				workinfo.setWorkPackageSetting(wps);
			});
		} else {
			context.selected(t -> {
				if (t instanceof Work) {
					Work work = Services.get(WorkService.class).getWork(((Work) t).get_id());
					Editor.create(editor, context, work, true).setTitle(t.toString()).ok((r, wi) -> {
						Services.get(WorkService.class)
								.updateWork(new FilterAndUpdate().filter(new BasicDBObject("_id", ((Work) t).get_id())).set(r).bson());

						// List<TrackView> wps = wi.getWorkPackageSetting();
						// ((Work) t).setWorkPackageSetting(wps);
						// Object content = context.getContent();
						Check.instanceThen(context.getContent(), IStructuredDataPart.class, c -> c.doModify(t, work, r));

					});
				}
			});

		}

	}

}
