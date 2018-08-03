package com.bizvisionsoft.pms.project.action;

import org.eclipse.swt.widgets.Event;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.Project;
import com.mongodb.BasicDBObject;

public class EditProjectCodeACT {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(Execute.PARAM_EVENT) Event event) {
		context.selected(se -> {
			Editor.open("�༭��Ŀ�༭��", context, (Project) se, (b, o) -> {
				Object code = b.get("id");
				Object workOrder = b.get("workOrder");
				if (code != null && (workOrder == null || "".equals(workOrder))) {
					workOrder = ServicesLoader.get(ProjectService.class).generateWorkOrder(o.get_id());
					b.put("workOrder", workOrder);
				}
				try {
					ServicesLoader.get(ProjectService.class)
							.update(new FilterAndUpdate().filter(new BasicDBObject("_id", o.get_id())).set(b).bson());
					GridPart gp = (GridPart) context.getContent();
					gp.refreshAll();
				} catch (Exception e) {
					String message = e.getMessage();
					if (message.indexOf("index") >= 0) {
						Layer.message("����¼����ͬ����Ŀ���", Layer.ICON_CANCEL);
					}
				}
			});
		});
	}

}
