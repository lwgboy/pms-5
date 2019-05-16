package com.bizvisionsoft.pms.product;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.model.Product;
import com.bizvisionsoft.service.model.Project;

public class CreateProjectProductACT {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		Product product = new Product().setProject_id(context.getRootInput(Project.class, false).get_id());
		Editor<?> editor = new Editor<Object>(br.getAssembly("产品编辑器"), context).setInput(product);
		editor.setTitle("创建项目目标产品");
		editor.ok((r, o) -> {
			GridPart grid = (GridPart) context.getContent();
			grid.doCreate(null, o);
		});

	}

}
