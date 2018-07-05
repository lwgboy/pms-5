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
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.FolderInTemplate;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class EditFolderInTemplateACT {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(Execute.PARAM_EVENT) Event event) {
		context.selected(sd -> {
			FolderInTemplate folder = (FolderInTemplate) sd;
			InputDialog id = new InputDialog(brui.getCurrentShell(), "创建文件夹", "文件夹名称", folder.getName(), t -> {
				return t.trim().isEmpty() ? "请输入名称" : null;
			});
			if (InputDialog.OK == id.open()) {
				String folderName = id.getValue();
				folder.setName(folderName);
				Services.get(ProjectTemplateService.class)
						.updateFolderInTemplate(new FilterAndUpdate().filter(new BasicDBObject("_id", folder.get_id()))
								.set(new BasicDBObject("name", folderName)).bson());
				GridPart view = (GridPart) context.getContent();
				view.update(folder);
			}
		});

	}
}
