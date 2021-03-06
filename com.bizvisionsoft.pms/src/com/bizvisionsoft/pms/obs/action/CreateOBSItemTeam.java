package com.bizvisionsoft.pms.obs.action;

import java.util.Optional;

import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;

public class CreateOBSItemTeam extends AbstractCreateOBSItem {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Object em,@MethodParam(Execute.CONTEXT) IBruiContext context) {

			String message = Optional.ofNullable(AUtil.readLabel(em)).orElse("");
			message = "创建" + message + "下级团队";
			open(context, em, message, "OBS节点编辑器（团队）.editorassy",false, br.getDomain());

	}

}
