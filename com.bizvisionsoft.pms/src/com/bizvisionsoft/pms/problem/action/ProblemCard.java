package com.bizvisionsoft.pms.problem.action;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.swt.widgets.Event;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruiengine.assembly.IQueryEnable;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.ProblemService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.Problem;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class ProblemCard {
	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Document element,
			@MethodParam(Execute.CONTEXT) BruiAssemblyContext context, @MethodParam(Execute.EVENT) Event e,
			@MethodParam(Execute.ACTION) Action a) {
		ObjectId _id = element.getObjectId("_id");
		if ("open8D".equals(e.text)) {
			br.switchPage("������-TOPS����", _id.toHexString());
		} else if ("kickoff".equals(e.text)) {
			if (br.confirm("����������", "��ȷ����������������")) {
				BasicDBObject fu = new FilterAndUpdate().filter(new BasicDBObject("_id", _id)).set(new BasicDBObject("status", "�����"))
						.bson();
				if (Services.get(ProblemService.class).updateProblems(fu, br.getDomain()) > 0) {
					Layer.message("����������������");
					br.switchPage("������-TOPS����", _id.toHexString());
				}
			}
		} else if ("create".equals(e.text)) {
			Editor.create("����༭����������", context, new Problem().setCreationInfo(br.operationInfo()), true).ok((d,t)->{
				Services.get(ProblemService.class).insertProblem(t, br.getDomain());
				((IQueryEnable)context.getContent()).doRefresh();
			});
		}
	}

}
