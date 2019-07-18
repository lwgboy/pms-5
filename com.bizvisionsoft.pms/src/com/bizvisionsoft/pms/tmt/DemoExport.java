package com.bizvisionsoft.pms.tmt;

import org.eclipse.rap.json.JsonObject;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.service.UserSession;

public class DemoExport {
	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) Object input) {
		UserSession.bruiToolkit().transportServerFile("bvs/fs", "产品材料零件清单.doc", new JsonObject().add("id", "5d2badfb28c2761958118b35")
				.add("namespace", "docuFiles").add("name", "产品材料零件清单.doc").add("domain", "bvs_1DD02230I"));
	}
}
