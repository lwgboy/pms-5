package com.bizvisionsoft.pms.eps.action;

import org.eclipse.jface.dialogs.MessageDialog;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.ProjectSetService;
import com.bizvisionsoft.service.model.EPS;
import com.bizvisionsoft.service.model.ProjectSet;
import com.bizvisionsoft.serviceconsumer.Services;

public class CreateProjectSet {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context) {
		context.selected(em -> {
			ProjectSet input = new ProjectSet();
			if (em instanceof EPS) {
				input.setEps_id(((EPS) em).get_id());
			} else if (em instanceof ProjectSet) {
				input.setParent_id(((ProjectSet) em).get_id());
			} else {
				MessageDialog.openError(bruiService.getCurrentShell(), "������Ŀ��",
						"ֻ����EPS�ڵ����Ŀ���´����µ���Ŀ����\n��ѡ��EPS�ڵ����Ŀ���ڵ㡣");
				return;
			}
			new Editor<ProjectSet>(bruiService.getAssembly("��Ŀ���༭��"), context).setInput(input).ok((r, pjset) -> {
				pjset = Services.get(ProjectSetService.class).insert(pjset);
				if (pjset != null) {
					GridPart grid = (GridPart) context.getContent();
					grid.add(em, pjset);
				}
			});

		});
	}

}
