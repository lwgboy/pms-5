package com.bizvisionsoft.pms.projecttemplate;

import java.util.Optional;

import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.IStructuredDataPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.model.OBSInTemplate;

public class EditOBSItemACT {

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		context.selected(em -> {
			String editorId;
			if (((OBSInTemplate) em).isRole()) {
				editorId = "OBS模板节点编辑器（角色）.editorassy";
			} else if (((OBSInTemplate) em).isScopeRoot()) {
				editorId = "OBS模板节点编辑器（根）.editorassy";
			} else {
				editorId = "OBS模板节点编辑器（团队）.editorassy";
			}
			String message = "编辑 " + Optional.ofNullable(AUtil.readLabel(em)).orElse("");
			Editor.create(editorId, context, em, false).setTitle(message).ok((r, o) -> {
				Object part = context.getContent();
				if (part instanceof IStructuredDataPart) {
					((IStructuredDataPart) part).doModify(em, o, r);
				}
			});
		});
	}

}
