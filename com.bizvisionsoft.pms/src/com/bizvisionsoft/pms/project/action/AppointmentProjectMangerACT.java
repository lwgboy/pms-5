package com.bizvisionsoft.pms.project.action;

import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Selector;
import com.bizvisionsoft.service.OBSService;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.User;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.serviceconsumer.Services;

public class AppointmentProjectMangerACT {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Object em, @MethodParam(Execute.CONTEXT) IBruiContext context) {
		Project project = (Project) em;
		List<Result> result = Services.get(OBSService.class).deleteProjectMemberCheck(project.getOBS_id(), "appointmentobsitem", br.getDomain());
		boolean hasError = false;
		boolean hasWarning = false;
		String message = "";
		if (!result.isEmpty()) {
			for (Result r : result)
				if (Result.TYPE_ERROR == r.type) {
					hasError = true;
					message += "<span class='layui-badge'>����</span> " + r.message + "<br>";
				} else if (Result.TYPE_WARNING == r.type) {
					hasWarning = true;
					message += "<span class='layui-badge layui-bg-orange'>����</span> " + r.message + "<br>";
				} else {
					message += "<span class='layui-badge layui-bg-blue'>��Ϣ</span> " + r.message + "<br>";
				}
		}
		if (!message.isEmpty()) {
			if (hasError) {
				MessageDialog.openError(br.getCurrentShell(), "ָ����Ŀ����", message);
				return;
			} else if (hasWarning) {
				if (!MessageDialog.openQuestion(br.getCurrentShell(), "ָ����Ŀ����", message + "<br>�Ƿ������"))
					return;

			}
		}

		new Selector(br.getAssembly("�û�ѡ��������ѡ"), context).setTitle("ָ����Ŀ����").open(r -> {
			User user = (User) r.get(0);
			Project p = Services.get(ProjectService.class).appointmentProjectManger(project.get_id(), user.getUserId(),
					br.getCurrentUserId(), br.getDomain());
			AUtil.simpleCopy(p, em);
			Check.instanceThen(context.getContent(), GridPart.class, grid -> grid.update(em));
			Layer.message("�������Ŀ�����ָ��");
		});
	}
}
