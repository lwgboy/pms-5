package com.bizvisionsoft.pms.project;

import org.bson.Document;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiService;

public class ProjectCardACT {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Document element) {
		SwitchProjectPage.openProject(br, element.getObjectId("_id"));
	}

}
