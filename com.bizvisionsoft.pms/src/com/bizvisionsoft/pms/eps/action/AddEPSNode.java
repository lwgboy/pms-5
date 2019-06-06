package com.bizvisionsoft.pms.eps.action;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.EPSService;
import com.bizvisionsoft.service.model.EPS;
import com.bizvisionsoft.serviceconsumer.Services;

public class AddEPSNode {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		context.selected(elem -> {
			if (elem instanceof EPS) {

				new Editor<EPS>(br.getAssembly("EPS�༭��.editorassy"), context)

						.setInput(br.newInstance(EPS.class).setParent_id(((EPS) elem).get_id()))

						.ok((r, t) -> {
							EPS item = Services.get(EPSService.class).insert(t, br.getDomain());
							GridPart grid = (GridPart) context.getContent();
							grid.add(elem, item);
						});

			}
		});
	}

}
