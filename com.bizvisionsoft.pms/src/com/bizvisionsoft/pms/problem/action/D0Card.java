package com.bizvisionsoft.pms.problem.action;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;

public class D0Card extends ActionCard{
	
	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Document element,
			@MethodParam(Execute.CONTEXT) BruiAssemblyContext context, @MethodParam(Execute.EVENT) Event e,
			@MethodParam(Execute.ACTION) Action a) {
		run(element, context, e, a);
	}

	@Override
	protected IBruiService getBruiService() {
		return br;
	}

	@Override
	protected Document doUpdate(Document append, String lang, String render) {
		return service.updateD0ERA(append, lang, render);
	}

	@Override
	protected void doDelete(ObjectId _id) {
		service.deleteD0ERA(_id);
	}

	@Override
	protected String getVerfiyEditorName() {
		return "D0-ERA验证-编辑器";
	}

	@Override
	protected String getItemTypeName() {
		return "紧急反应行动";
	}

	@Override
	protected String getEditorName() {
		return "D0-ERA-编辑器";
	}

	@Override
	protected Document getAction(ObjectId _id) {
		return service.getD0ERA(_id);
	}
}
