package com.bizvisionsoft.pms.obs.action;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.eclipse.jface.dialogs.MessageDialog;

import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruicommons.model.Assembly;
import com.bizvisionsoft.bruiengine.assembly.IStructuredDataPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.OBSService;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.OBSItem;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.serviceconsumer.Services;

public class EditOBSItem {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Object em, @MethodParam(Execute.CONTEXT) IBruiContext context) {
		Assembly assembly;
		if (((OBSItem) em).isRole()) {
			assembly = br.getAssembly("OBS�ڵ�༭������ɫ��");
		} else if (((OBSItem) em).isScopeRoot()) {
			assembly = br.getAssembly("OBS�ڵ�༭��������");
		} else {
			assembly = br.getAssembly("OBS�ڵ�༭�����Ŷӣ�");
		}
		String message = "�༭ " + Optional.ofNullable(AUtil.readLabel(em)).orElse("");

		Editor<Object> editor = new Editor<Object>(assembly, context).setEditable(true).setTitle(message).setInput(false, em);

		editor.ok((r, o) -> {
			if (check((OBSItem) em, (OBSItem) o, message)) {
				Object part = context.getContent();
				if (part instanceof IStructuredDataPart) {
					((IStructuredDataPart) part).doModify(em, o, r);
				}
			}
		});

	}

	private boolean check(OBSItem em, OBSItem o, String title) {
		if (em.getManagerId() != null && !em.getManagerId().equals(o.getManagerId())) {
			List<Result> result = Services.get(OBSService.class).deleteProjectMemberCheck(em.get_id(), "editobsitem", br.getDomain());
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
					MessageDialog.openError(br.getCurrentShell(), title, message);
					return false;
				} else if (hasWarning) {
					if (!MessageDialog.openQuestion(br.getCurrentShell(), title, message + "<br>�Ƿ������"))
						return false;
					else
						Services.get(WorkService.class).removeUnStartWorkUser(Arrays.asList(em.getManagerId()), em.getScope_id(),
								br.getCurrentUserId(), br.getDomain());

				}
			}
		}

		return true;
	}

}
