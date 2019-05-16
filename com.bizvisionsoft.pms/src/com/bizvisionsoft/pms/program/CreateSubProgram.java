package com.bizvisionsoft.pms.program;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.ProgramService;
import com.bizvisionsoft.service.model.Program;
import com.bizvisionsoft.serviceconsumer.Services;

public class CreateSubProgram {
	
	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		context.selected(em -> {
			if (em instanceof Program) {
				Program pjSet = new Program();
				pjSet.setParent_id(((Program) em).get_id());
				pjSet.setCreationInfo(br.operationInfo());
				new Editor<Program>(br.getAssembly("ÏîÄ¿¼¯±à¼­Æ÷"), context)
						.setInput(pjSet)

						.ok((r, t) -> {
							Program result = Services.get(ProgramService.class).insert(t, br.getDomain());
							GridPart grid = (GridPart) context.getContent();
							grid.add(em, result);
						});
			}
		});
	}

}
