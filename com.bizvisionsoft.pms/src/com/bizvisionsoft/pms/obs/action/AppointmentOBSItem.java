package com.bizvisionsoft.pms.obs.action;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.IStructuredDataPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Selector;
import com.bizvisionsoft.service.OBSService;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.OBSItem;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.User;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class AppointmentOBSItem {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Object em, @MethodParam(Execute.CONTEXT) IBruiContext context) {
		OBSItem element = (OBSItem) em;
		if (element.getManagerId() != null) {
			List<Result> result = Services.get(OBSService.class).deleteProjectMemberCheck(element.get_id(), "appointmentobsitem", br.getDomain());
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
					MessageDialog.openError(br.getCurrentShell(), "ָ��������", message);
					return;
				} else if (hasWarning) {
					if (!MessageDialog.openQuestion(br.getCurrentShell(), "ָ��������", message + "<br>�Ƿ������"))
						return;

				}
			}
		}

		new Selector(br.getAssembly("�û�ѡ��������ѡ"), context).setTitle("ָ��������").open(r -> {
			Services.get(WorkService.class).removeUnStartWorkUser(Arrays.asList(element.getManagerId()), element.getScope_id(),
					br.getCurrentUserId(), br.getDomain());
			element.setManager((User) r.get(0));
			String userId = ((User) r.get(0)).getUserId();
			BasicDBObject data = new BasicDBObject("_id", element.get_id()).append("managerId", userId);
			Check.instanceThen(context.getContent(), IStructuredDataPart.class, part -> part.doModify(element, element, data));
		});
	}

}
