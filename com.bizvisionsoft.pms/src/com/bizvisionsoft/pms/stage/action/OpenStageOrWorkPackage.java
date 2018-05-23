package com.bizvisionsoft.pms.stage.action;

import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.model.ProjectStatus;
import com.bizvisionsoft.service.model.Work;

public class OpenStageOrWorkPackage {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		context.selected(em -> {
			Work work = (Work) em;
			if (work.isStage()) {
				if (ProjectStatus.Created.equals(work.getStatus())) {
					bruiService.switchPage("�׶���ҳ��������", work.get_id().toHexString());
				} else if (ProjectStatus.Processing.equals(work.getStatus())) {
					bruiService.switchPage("�׶���ҳ��ִ�У�", work.get_id().toHexString());
				} else if (ProjectStatus.Closing.equals(work.getStatus())) {
					bruiService.switchPage("�׶���ҳ����β��", work.get_id().toHexString());
				} else if (ProjectStatus.Closed.equals(work.getStatus())) {
					bruiService.switchPage("�׶���ҳ���رգ�", work.get_id().toHexString());
				}
			} else {
				bruiService.openContent(bruiService.getAssembly("����������"), work);
			}

		});
	}

}
