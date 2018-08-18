package com.bizvisionsoft.pms.projectset;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.util.Util;
import com.bizvisionsoft.service.ProjectSetService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.serviceconsumer.Services;

public class RemoveProjectFromProjectSet {
	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context) {
		context.selected(em -> {
			if ((em instanceof Project) && br.confirm("移出项目集", "请确定将项目" + em + "移出项目集。")) {
				Services.get(ProjectSetService.class).unsetProjectSet(((Project) em).get_id());
				Util.ifInstanceThen(context.getContent(), GridPart.class, c -> c.remove(em));
				Layer.message("已移除项目" + em );
			}
		});
	}
}
