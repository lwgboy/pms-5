package com.bizvisionsoft.pms.projecttemplate;

import java.util.Optional;

import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.IStructuredDataPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.ProjectTemplateService;
import com.bizvisionsoft.service.model.OBSInTemplate;
import com.bizvisionsoft.serviceconsumer.Services;

public class CreateOBSRoleItemACT  {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(Execute.PARAM_EVENT) Event event) {
		context.selected(em -> {

			String message = Optional.ofNullable(AUtil.readLabel(em)).orElse("");
			message = "添加角色到" + message;
			String editor = "OBS模板节点编辑器（角色）";
			open(context, em, message, editor,true);

		});
	}

	
	protected void open(IBruiContext context, Object em, String message, String editor,boolean isRole) {
		OBSInTemplate input = new OBSInTemplate().setParent_id(((OBSInTemplate) em).get_id())
				.setScope_id(((OBSInTemplate) em).getScope_id()).setIsRole(isRole).generateSeq();

		Editor.create(editor, context, input, true).setTitle(message).ok((r, t) -> {
			OBSInTemplate item = Services.get(ProjectTemplateService.class).insertOBSItem(t);
			Object part = context.getContent();
			if (part instanceof IStructuredDataPart) {
				((IStructuredDataPart) part).add(em, item);
			}
		});
	}
}
