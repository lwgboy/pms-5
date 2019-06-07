package com.bizvisionsoft.pms.obs.action;

import java.util.Optional;

import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.IStructuredDataPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.model.OBSItem;

public class EditOBSItem extends AbstractChangeOBSItemMember {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Object em, @MethodParam(Execute.CONTEXT) IBruiContext context) {
		String editorId;
		if (((OBSItem) em).isRole()) {
			editorId = "OBS�ڵ�༭������ɫ��.editorassy";
		} else if (((OBSItem) em).isScopeRoot()) {
			editorId = "OBS�ڵ�༭��������.editorassy";
		} else {
			editorId = "OBS�ڵ�༭�����Ŷӣ�.editorassy";
		}
		String message = "�༭ " + Optional.ofNullable(AUtil.readLabel(em)).orElse("");
		Editor.create(editorId, context, em, false).setTitle(message).ok((r, o) -> {
			OBSItem obsItem = (OBSItem) em;
			if (!obsItem.getManagerId().equals(((OBSItem) o).getManagerId())
					&& checkChange(br, obsItem.get_id(), obsItem.getScope_id(), obsItem.getManagerId(), "editobsitem", message)) {
				Object part = context.getContent();
				if (part instanceof IStructuredDataPart) {
					((IStructuredDataPart) part).doModify(em, o, r);
				}
			}
		});
	}
}
