package com.bizvisionsoft.pms.projectset;

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
import com.bizvisionsoft.bruiengine.util.Util;
import com.bizvisionsoft.service.ProjectSetService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.ProjectSet;
import com.bizvisionsoft.serviceconsumer.Services;

public class AddProjectToProjectSet {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context) {
		context.selected(em -> {
			if (em instanceof ProjectSet) {
				Selector.open("ÏîÄ¿Ñ¡ÔñÆ÷", context, null, s -> {
					List<ObjectId> pjIds = new ArrayList<ObjectId>();
					s.forEach(pj -> pjIds.add(((Project) pj).get_id()));
					Services.get(ProjectSetService.class).addProjects(pjIds, ((ProjectSet) em).get_id());
					Util.ifInstanceThen(context.getContent(), GridPart.class, c -> c.refreshAndExpand(em));
				});
			}
		});
	}

}
