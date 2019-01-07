package com.bizvisionsoft.pms.problem.action;

import org.eclipse.jface.dialogs.MessageDialog;

import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.ProblemService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.Problem;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class EditProblem {

	@Inject
	private String actionType;

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		if ("create".equals(actionType)) {
			new Editor<Problem>(br.getAssembly("����༭����������"), context).setInput(new Problem().setCreationInfo(br.operationInfo()))
					.ok((r, t) -> {
						t = Services.get(ProblemService.class).insertProblem(t);
						if (t != null) {
							if (MessageDialog.openQuestion(br.getCurrentShell(), "���������ʼ��¼", "���ⴴ���ɹ����Ƿ��������ҳ�棿")) {
								br.switchPage("������", t.get_id().toHexString());
							}
						}
					});
		} else {
			Problem problem = (Problem) context.getRootInput();
			new Editor<Problem>(br.getAssembly("����༭�����༭��"), context).setTitle("�����ʼ��¼").setEditable(false).setInput(problem)
					.ok((r, t) -> {
						r.remove("_id");
						BasicDBObject fu = new FilterAndUpdate().filter(new BasicDBObject("_id", problem.get_id())).set(r).bson();
						long l = Services.get(ProblemService.class).updateProblems(fu);
						if (l > 0) {
							AUtil.simpleCopy(t, problem);
						}
					});
		}

	}

}
