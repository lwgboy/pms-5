package com.bizvisionsoft.pms.problem.action;

import org.eclipse.jface.dialogs.MessageDialog;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.ProblemService;
import com.bizvisionsoft.service.model.Problem;
import com.bizvisionsoft.serviceconsumer.Services;

public class CreateProblem {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		new Editor<Problem>(br.getAssembly("����༭����������"), context)
				.setInput(new Problem().setCreationInfo(br.operationInfo())).ok((r, t) -> {
					t = Services.get(ProblemService.class).insertProblem(t);
					if (t != null) {
						if (MessageDialog.openQuestion(br.getCurrentShell(), "��������", "���ⴴ���ɹ����Ƿ��������ҳ�棿")) {
							// TODO
						}
					}
				});

	}

}
