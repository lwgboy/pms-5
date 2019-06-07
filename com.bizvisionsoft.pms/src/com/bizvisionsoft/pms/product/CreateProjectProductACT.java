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
		Product product = br.newInstance(Product.class).setProject_id(context.getRootInput(Project.class, false).get_id());
		Editor.create("��Ʒ�༭��.editorassy", context, product, false).setTitle("������ĿĿ���Ʒ").ok((r, o) -> {
			GridPart grid = (GridPart) context.getContent();
			grid.doCreate(null, o);
		});
	}

}
