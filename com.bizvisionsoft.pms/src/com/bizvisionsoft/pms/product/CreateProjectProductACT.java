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
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context) {
		Product product = new Product().setProject_id(context.getRootInput(Project.class, false).get_id());
		Editor<?> editor = new Editor<Object>(brui.getAssembly("��Ʒ�༭��"), context).setInput(product);
		editor.setTitle("������ĿĿ���Ʒ");
		editor.ok((r, o) -> {
			GridPart grid = (GridPart) context.getContent();
			grid.doCreate(null, o);
		});

	}

}
