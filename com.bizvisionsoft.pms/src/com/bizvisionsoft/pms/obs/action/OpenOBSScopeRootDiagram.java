package com.bizvisionsoft.pms.obs.action;

import org.bson.types.ObjectId;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.OBSService;
import com.bizvisionsoft.service.model.IOBSScope;
import com.bizvisionsoft.service.model.OBSItem;
import com.bizvisionsoft.serviceconsumer.Services;

public class OpenOBSScopeRootDiagram extends AbstractCreateOBSItem {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		Shell s = brui.getCurrentShell();
		Object scope = context.getRootInput();
		if (scope instanceof IOBSScope) {
			ObjectId obsRoot_id = ((IOBSScope) scope).getOBS_id();
			String label = AUtil.readLabel(scope);
			if (obsRoot_id == null) {// 还未建立根组织
				if (MessageDialog.openQuestion(s, "组织结构图", label + "尚未建立团队，是否建立？")) {
					OBSItem item = ((IOBSScope) scope).newOBSScopeRoot();
					item = Services.get(OBSService.class).insert(item);
					((IOBSScope) scope).updateOBSRootId(item.get_id());
					// TODO 错误处理
				}
			}
			brui.switchContent("组织结构图", null);
		}
	}

}
