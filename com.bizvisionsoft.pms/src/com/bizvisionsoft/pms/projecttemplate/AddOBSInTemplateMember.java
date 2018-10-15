package com.bizvisionsoft.pms.projecttemplate;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Selector;
import com.bizvisionsoft.service.ProjectTemplateService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.OBSInTemplate;
import com.bizvisionsoft.service.model.User;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class AddOBSInTemplateMember {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context, @MethodParam(Execute.EVENT) Event event) {
		new Selector(bruiService.getAssembly("用户选择器"), context).setTitle("选择用户添加为团队成员").open(r -> {
			final List<String> ids = new ArrayList<String>();
			GridPart grid = (GridPart) context.getContent();
			List<?> input = (List<?>) grid.getViewerInput();
			r.forEach(a -> {
				if (!input.contains(a)) {
					grid.insert(a, 0);
					ids.add(((User) a).getUserId());
				}
			});
			if (!ids.isEmpty()) {
				ObjectId obsId = ((OBSInTemplate) context.getInput()).get_id();
				BasicDBObject fu = new FilterAndUpdate().filter(new BasicDBObject("_id", obsId)).update(
						new BasicDBObject("$addToSet", new BasicDBObject("member", new BasicDBObject("$each", ids))))
						.bson();
				Services.get(ProjectTemplateService.class).updateOBSModule(fu);
			}
		});
	}

}
