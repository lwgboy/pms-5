package com.bizvisionsoft.pms.projecttemplate;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;

import com.bizvisionsoft.annotations.md.service.ImageURL;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.ActionPanelPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.ProjectTemplateService;
import com.bizvisionsoft.service.model.ProjectTemplate;
import com.bizvisionsoft.serviceconsumer.Services;

public class ProjectTemplateSwitchACT {

	@Inject
	private IBruiService br;

	@Inject
	private IBruiContext context;

	private ProjectTemplate projectTemplate;

	@Init
	public void init() {
		projectTemplate = context.getRootInput(ProjectTemplate.class, false);
	}

	@Execute
	public void execute(@MethodParam(Execute.EVENT) Event event) {
		boolean enabled = !projectTemplate.isEnabled();
		Services.get(ProjectTemplateService.class).setEnabled(projectTemplate.get_id(), enabled, br.getDomain());
		projectTemplate.setEnabled(enabled);
		ActionPanelPart ap = (ActionPanelPart) context.getContent();
		Label btn = (Label) event.widget;
		ap.redrawButton(btn);
	}

	@ImageURL
	public String getImageURL() {
		return projectTemplate.isEnabled() ? "/img/power_on.svg" : "/img/power_off.svg";
	}

	@com.bizvisionsoft.annotations.md.service.Label
	public String getLabel() {
		return projectTemplate.isEnabled() ? "已启用" : "已停用";
	}

}
