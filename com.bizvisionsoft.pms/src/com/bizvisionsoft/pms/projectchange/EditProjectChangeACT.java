package com.bizvisionsoft.pms.projectchange;

import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.InfopadPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.bruiengine.util.EngUtil;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.ProjectChange;
import com.mongodb.BasicDBObject;

public class EditProjectChangeACT {

	@Inject
	private IBruiService brui;

	@Execute
	private void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context) {
		ProjectChange input = (ProjectChange) context.getInput();
		Editor.open("项目变更编辑器", context, input, (r, o) -> {
			ServicesLoader.get(ProjectService.class).updateProjectChange(new FilterAndUpdate()
					.filter(new BasicDBObject("_id", o.get_id())).set(EngUtil.getBson((ProjectChange) o, "_id")).bson());
			AUtil.simpleCopy(o, input);
			((InfopadPart) context.getContent()).reload();
		});

	}
}
