package com.bizvisionsoft.pms.project;

import org.bson.Document;
import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiService;

public class ProjectCardACT {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Document element, @MethodParam(Execute.EVENT) Event e) {
		if (e != null && "openItem/".equals(e.text))
			SwitchProjectPage.openProject(br, element.getObjectId("_id"));
	}

}
