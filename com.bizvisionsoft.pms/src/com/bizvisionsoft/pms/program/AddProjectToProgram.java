package com.bizvisionsoft.pms.program;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Selector;
import com.bizvisionsoft.service.ProgramService;
import com.bizvisionsoft.service.model.Program;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.tools.Checker;
import com.bizvisionsoft.serviceconsumer.Services;

public class AddProjectToProgram {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context) {
		context.selected(em -> {
			if (em instanceof Program) {
				Selector.open("ÏîÄ¿Ñ¡ÔñÆ÷", context, null, s -> {
					List<ObjectId> pjIds = new ArrayList<ObjectId>();
					s.forEach(pj -> pjIds.add(((Project) pj).get_id()));
					Services.get(ProgramService.class).addProjects(pjIds, ((Program) em).get_id());
					Checker.ifInstance(context.getContent(), GridPart.class, c -> c.refreshAndExpand(em));
				});
			}
		});
	}

}
