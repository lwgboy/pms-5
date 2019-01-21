package com.bizvisionsoft.pms.problem.action;

import org.eclipse.jface.dialogs.MessageDialog;

import com.bizivisionsoft.widgets.util.Layer;
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
	private String render;

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		if ("create".equals(actionType)) {
			create(context);
		} else {
			Problem problem = (Problem) context.getRootInput();
			edit(context, problem);
		}
	}

	private void edit(IBruiContext context, Problem problem) {
		new Editor<Problem>(br.getAssembly("����༭�����༭��"), context).setTitle("�����ʼ��¼").setEditable(false).setInput(problem).ok((r, t) -> {
			r.remove("_id");
			BasicDBObject fu = new FilterAndUpdate().filter(new BasicDBObject("_id", problem.get_id())).set(r).bson();
			long l = Services.get(ProblemService.class).updateProblems(fu);
			if (l > 0) {
				AUtil.simpleCopy(t, problem);
			}
		});
	}

	private void create(IBruiContext context) {
		new Editor<Problem>(br.getAssembly("����༭����������"), context).setInput(new Problem().setCreationInfo(br.operationInfo())).ok((r, t) -> {
			ProblemService service = Services.get(ProblemService.class);
			t = service.insertProblem(t);
			if (t != null) {
				if (MessageDialog.openQuestion(br.getCurrentShell(), "���������ʼ��¼", "�����Ѿ������ɹ����Ƿ�������ʼ������⣿")) {
					BasicDBObject fu = new FilterAndUpdate().filter(new BasicDBObject("_id", t.get_id()))
							.set(new BasicDBObject("status", "�����")).bson();
					if (service.updateProblems(fu) > 0) {
						Layer.message("����������������");
						br.switchPage("������-TOPS����", t.get_id().toHexString());
					}
				}
			}
		});
	}

}
