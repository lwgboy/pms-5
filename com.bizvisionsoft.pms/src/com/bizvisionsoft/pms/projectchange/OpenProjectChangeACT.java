package com.bizvisionsoft.pms.projectchange;

import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.model.ProjectChange;

public class OpenProjectChangeACT {
	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		context.selected(s -> {
			brui.openContent(brui.getAssembly("±ä¸üÏêÇé"), s, e -> {
				if (s instanceof ProjectChange) {
					ProjectChange o = ServicesLoader.get(ProjectService.class)
							.getProjectChange(((ProjectChange) s).get_id());
					GridPart viewer = (GridPart) context.getContent();
					AUtil.simpleCopy(o, s);
					viewer.update(s);
				}
			});
		});
	}

}
