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
import com.bizvisionsoft.service.model.OBSItem;

public class EditOBSItem {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(Execute.PARAM_EVENT) Event event) {
		context.selected(em -> {
			Assembly assembly;
			if(((OBSItem)em).isRole()) {
				assembly = bruiService.getAssembly("OBS�ڵ�༭������ɫ��");
			}else {
				assembly = bruiService.getAssembly("OBS�ڵ�༭�����Ŷӣ�");
			}
			String message = "�༭ " + Optional.ofNullable(AUtil.readLabel(em)).orElse("");

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

}
