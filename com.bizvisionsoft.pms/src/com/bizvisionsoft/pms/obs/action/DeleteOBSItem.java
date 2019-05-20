package com.bizvisionsoft.pms.obs.action;

import org.eclipse.jface.dialogs.MessageDialog;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.TreePart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.model.OBSItem;

public class DeleteOBSItem extends AbstractChangeOBSItemMember {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Object em, @MethodParam(Execute.CONTEXT) IBruiContext context) {
		if (MessageDialog.openConfirm(br.getCurrentShell(), "删除", "请确认将要删除 " + em)) {
			if (checkDelete(br, (OBSItem) em)) {
				TreePart part = (TreePart) context.getContent();
				part.doDelete(em);
			}
		}
	}

}
