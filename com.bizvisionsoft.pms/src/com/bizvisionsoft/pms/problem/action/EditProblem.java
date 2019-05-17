package com.bizvisionsoft.pms.problem.action;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Event;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.IQueryEnable;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.ProblemService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.Problem;
import com.bizvisionsoft.service.tools.Check;
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
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context, @MethodParam(Execute.EVENT) Event e) {
		if ("create".equals(actionType)) {
			create(context);
		} else if ("edit".equals(actionType) || "edit".equals(e.text)) {
			Problem problem = (Problem) context.getRootInput();
			edit(context, problem, true);
		} else if ("read".equals(actionType) || "read".equals(e.text)) {
			Problem problem = (Problem) context.getRootInput();
			edit(context, problem, false);
		}
	}

	private void edit(IBruiContext context, Problem problem, boolean editable) {
		new Editor<Problem>(br.getAssembly("����༭�����༭��"), context).setTitle("�����ʼ��¼").setEditable(editable).setInput(problem).ok((r, t) -> {
			r.remove("_id");
			BasicDBObject fu = new FilterAndUpdate().filter(new BasicDBObject("_id", problem.get_id())).set(r).bson();
			long l = Services.get(ProblemService.class).updateProblems(fu, br.getDomain());
			if (l > 0) {
				AUtil.simpleCopy(t, problem);// ��дproblem
				Check.instanceThen(context.getContent(), IQueryEnable.class, q -> q.doRefresh());
			}
		});
	}

	private void create(IBruiContext context) {
		new Editor<Problem>(br.getAssembly("����༭����������"), context).setInput(br.newInstance(Problem.class).setCreationInfo(br.operationInfo())).ok((r, t) -> {
			ProblemService service = Services.get(ProblemService.class);
			t = service.insertProblem(t, br.getDomain());
			if (t != null) {
				if (MessageDialog.openQuestion(br.getCurrentShell(), "���������ʼ��¼", "�����Ѿ������ɹ����Ƿ�������ʼ������⣿")) {
					BasicDBObject fu = new FilterAndUpdate().filter(new BasicDBObject("_id", t.get_id()))
							.set(new BasicDBObject("status", "�����")).bson();
					if (service.updateProblems(fu, br.getDomain()) > 0) {
						Layer.message("����������������");
						br.switchPage("������-TOPS����", t.get_id().toHexString());
					}
				}
			}
		});
	}

}
