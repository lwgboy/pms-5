package com.bizvisionsoft.pms.projectchange;

import java.util.Date;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.ProjectChange;
import com.bizvisionsoft.serviceconsumer.Services;

public class CreateProjectChangeACT {

	@Inject
	private IBruiService br;

	@Execute
	private void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		Project project = (Project) context.getRootInput();
		long check = Services.get(ProjectService.class).checkCreateProjectChange(project.get_id(), br.getDomain());
		if (check > 0) {
			Layer.message("����δ��ɵ���Ŀ������޷������±������", Layer.ICON_ERROR);
			return;
		}
		Editor.open("��Ŀ����༭��.editorassy", context,
				br.newInstance(ProjectChange.class).setProject_id(project.get_id()).setApplicant(br.getCurrentUserInfo())
						.setStatus(ProjectChange.STATUS_CREATE).setApplicantDate(new Date()).setApplicantUnitId(project.getImpUnit_id()),
				(r, o) -> {
					ProjectChange pc = Services.get(ProjectService.class).createProjectChange(o, br.getDomain());
					GridPart grid = (GridPart) context.getContent();
					grid.insert(pc, 0);
				});
	}
}
