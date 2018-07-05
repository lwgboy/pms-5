package com.bizvisionsoft.pms.projecttemplate;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.ProjectTemplateService;
import com.bizvisionsoft.service.model.FolderInTemplate;
import com.bizvisionsoft.service.model.ProjectTemplate;
import com.bizvisionsoft.serviceconsumer.Services;

public class CreateFolderInTemplateACT {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(Execute.PARAM_EVENT) Event event) {
		ProjectTemplate rootInput = (ProjectTemplate) context.getRootInput();
		InputDialog id = new InputDialog(brui.getCurrentShell(), "创建文件夹", "文件夹名称", null, t -> {
			return t.trim().isEmpty() ? "请输入名称" : null;
		});
		if (InputDialog.OK == id.open()) {
			String folderName = id.getValue();
			FolderInTemplate folder = new FolderInTemplate();
			folder.setName(folderName);
			folder.setTempalte_id(rootInput.get_id());
			folder = Services.get(ProjectTemplateService.class).insertFolderInTemplate(folder);
			GridPart view = (GridPart) context.getContent();
			view.insert(folder);
		}

	}
}
