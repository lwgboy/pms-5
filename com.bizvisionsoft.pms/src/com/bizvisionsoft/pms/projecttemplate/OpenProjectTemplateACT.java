package com.bizvisionsoft.pms.projecttemplate;

import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.model.OBSModule;
import com.bizvisionsoft.service.model.ProjectTemplate;
import com.bizvisionsoft.service.model.WBSModule;

public class OpenProjectTemplateACT {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context,
			@MethodParam(Execute.EVENT) Event event) {
		context.selected(em->{
			if(em instanceof WBSModule) {
				brui.openContent(brui.getAssembly("项目模板甘特图"), em);
			} else if (em instanceof OBSModule) {
				// 当前选择为组织模板时，打开OBS模板组织结构图进行组织模板的编辑
				brui.openContent(brui.getAssembly("OBS模板组织结构图"), em);
			} else {
				brui.switchPage("项目模板", ((ProjectTemplate) em).get_id().toHexString());
			}
		});
	}

}
