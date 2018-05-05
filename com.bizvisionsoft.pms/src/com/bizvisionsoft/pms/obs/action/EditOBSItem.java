package com.bizvisionsoft.pms.obs.action;

import java.util.Optional;

import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruicommons.model.Assembly;
import com.bizvisionsoft.bruiengine.assembly.IStructuredDataPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.model.IOBSScope;
import com.bizvisionsoft.service.model.OBSItem;

public class EditOBSItem {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		context.selected(em -> {
			String editName = isRootOBSItem(em, context.getRootInput()) ? "OBS¸ù±à¼­Æ÷" : "OBS½Úµã±à¼­Æ÷£¨ÐÞ¸ÄÓÃ£©";
			Assembly assembly = bruiService.getAssembly(editName);
			String message = "±à¼­ " + Optional.ofNullable(AUtil.readTypeAndLabel(em)).orElse("");

			Editor<Object> editor = new Editor<Object>(assembly, context).setEditable(true).setTitle(message)
					.setInput(false, em);

			editor.ok((r, o) -> {
				Object part = context.getContent();
				if (part instanceof IStructuredDataPart) {
					((IStructuredDataPart) part).doModify(em, o, r);
				}
			});

		});

	}

	private boolean isRootOBSItem(Object em, Object input) {
		return em instanceof OBSItem && input instanceof IOBSScope
				&& ((OBSItem) em).get_id().equals(((IOBSScope) input).getOBS_id());
	}

}
