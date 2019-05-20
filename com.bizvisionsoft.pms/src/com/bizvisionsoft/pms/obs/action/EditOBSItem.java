package com.bizvisionsoft.pms.obs.action;

import java.util.Optional;

import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruicommons.model.Assembly;
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
		Assembly assembly;
		if (((OBSItem) em).isRole()) {
			assembly = br.getAssembly("OBS�ڵ�༭������ɫ��");
		} else if (((OBSItem) em).isScopeRoot()) {
			assembly = br.getAssembly("OBS�ڵ�༭��������");
		} else {
			assembly = br.getAssembly("OBS�ڵ�༭�����Ŷӣ�");
		}
		String message = "�༭ " + Optional.ofNullable(AUtil.readLabel(em)).orElse("");

		Editor<Object> editor = new Editor<Object>(assembly, context).setEditable(true).setTitle(message).setInput(false, em);

		editor.ok((r, o) -> {
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
