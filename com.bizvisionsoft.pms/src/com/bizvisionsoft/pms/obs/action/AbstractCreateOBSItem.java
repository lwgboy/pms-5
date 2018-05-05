package com.bizvisionsoft.pms.obs.action;

import com.bizvisionsoft.bruiengine.assembly.IStructuredDataPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.OBSService;
import com.bizvisionsoft.service.model.OBSItem;
import com.bizvisionsoft.serviceconsumer.Services;

public abstract class AbstractCreateOBSItem {

	protected void open(IBruiContext context, Object em, String message, String editor) {
		OBSItem input = new OBSItem().setParent_id(((OBSItem) em).get_id())
				.setScope_id(((OBSItem) em).getScope_id());

		Editor.create(editor, context, input, true).setTitle(message).ok((r, t) -> {
			OBSItem item = Services.get(OBSService.class).insert(t);
			Object part = context.getContent();
			if (part instanceof IStructuredDataPart) {
				((IStructuredDataPart) part).add(em, item);
			}
		});
	}

}
