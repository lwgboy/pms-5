package com.bizvisionsoft.pms.problem.action;

import org.bson.Document;
import org.eclipse.rap.rwt.RWT;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.md.service.Behavior;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.IQueryEnable;
import com.bizvisionsoft.bruiengine.assembly.InfopadPart;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.ProblemService;
import com.bizvisionsoft.service.model.Problem;
import com.bizvisionsoft.serviceconsumer.Services;

public class EditD2ProblemDesc {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) BruiAssemblyContext context,
			@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) Problem problem) {
		String editor="D2-5W2H��������-�༭��.editorassy";
		if(Services.get(ProblemService.class).selectProblemsCard(problem.get_id()
				,br.getCurrentUserId(),problem.domain)&&problem.getStatus().equals("�����")) {
			Layer.error("��û�з���Ȩ��!");
		}else {
			ProblemService service = Services.get(ProblemService.class);
			Document d = service.getD2ProblemDesc(problem.get_id(), br.getDomain());
			Editor.create(editor, context, d, true).ok((r, t) -> {
				service.updateD2ProblemDesc(t, RWT.getLocale().getLanguage(), br.getDomain());
				if (context.getContent().getClass().isInstance(new InfopadPart(null))) {
					InfopadPart content = (InfopadPart) context.getContent();
					content.reload();
				} else {
					IQueryEnable content = (IQueryEnable) context.getContent();
					content.doRefresh();
				}
			});
		}
		
	}

	@Behavior({ "�༭���������ı�" ,ProblemService.ACTION_EDIT})
	private boolean enableEdit(@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) Problem problem,
			@MethodParam(Execute.CONTEXT_SELECTION_1ST) Document element) {
		return "�����".equals(problem.getStatus());
	}

}
