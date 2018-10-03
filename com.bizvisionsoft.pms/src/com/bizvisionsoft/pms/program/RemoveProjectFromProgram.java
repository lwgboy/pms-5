package com.bizvisionsoft.pms.program;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.ProgramService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.tools.Checker;
import com.bizvisionsoft.serviceconsumer.Services;

public class RemoveProjectFromProgram {
	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context) {
		context.selected(em -> {
			if ((em instanceof Project) && br.confirm("移出项目集", "请确定将项目" + em + "移出项目集。")) {
				Services.get(ProgramService.class).unsetProgram(((Project) em).get_id());
				Checker.ifInstance(context.getContent(), GridPart.class, c -> c.remove(em));
				Layer.message("已移除项目" + em );
			}
		});
	}
}
