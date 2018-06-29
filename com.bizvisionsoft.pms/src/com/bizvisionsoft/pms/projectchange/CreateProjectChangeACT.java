package com.bizvisionsoft.pms.projectchange;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruicommons.ModelLoader;
import com.bizvisionsoft.bruicommons.model.Assembly;
import com.bizvisionsoft.bruicommons.model.FormField;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.CommonService;
import com.bizvisionsoft.service.OBSService;
import com.bizvisionsoft.service.ProjectService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.model.ChangeProcess;
import com.bizvisionsoft.service.model.OBSItem;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.ProjectChange;
import com.bizvisionsoft.service.model.ProjectChangeTask;
import com.bizvisionsoft.serviceconsumer.Services;

public class CreateProjectChangeACT {

	@Inject
	private IBruiService brui;

	@Execute
	private void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context) {
		Project project = (Project) context.getRootInput();

		List<ChangeProcess> changeProcesss = ServicesLoader.get(CommonService.class).createChangeProcessDataSet();
		List<String> cpName = new ArrayList<String>();

		Assembly editorConfig = ModelLoader.site.getAssemblyByName("œÓƒø±‰∏¸±‡º≠∆˜");
		editorConfig.getFields().forEach(formField -> {
			if ("…Û∫À".equals(formField.getName())) {
				List<FormField> newFormFields = new ArrayList<FormField>();
				List<FormField> formFields = formField.getFormFields();
				changeProcesss.forEach(cp -> {
					if (cp.getProjectOBSId() == null) {
						cpName.add(cp.getTaskName());
					}
				});
				for (int i = 0; i < cpName.size(); i++) {
					FormField newFormField = formFields.get(i);
					newFormField.setText(cpName.get(i).replace("…Û∫À", ""));
					newFormFields.add(newFormField);

				}
				formField.setFormFields(newFormFields);
			}
		});

		new Editor<ProjectChange>(editorConfig, context).setInput(false,
				new ProjectChange().setProject_id(project.get_id()).setApplicant(brui.getCurrentUserInfo())
						.setStatus(ProjectChange.STATUS_CREATE).setApplicantDate(new Date()))
				.ok((r, o) -> {
					List<ProjectChangeTask> reviewer = o.getReviewer();
					for (int i = 0; i < cpName.size(); i++) {
						String name = cpName.get(i);
						for (ProjectChangeTask re : reviewer) {
							String key = re.name;
							if (("reviewer" + (i + 1)).equals(key)) {
								re.name = name;
							}
						}
					}

					List<OBSItem> obsItems = ServicesLoader.get(OBSService.class).getScopeOBS(project.getScope_id());

					for (ChangeProcess changeProcess : changeProcesss) {
						if (changeProcess.getProjectOBSId() != null) {
							for (OBSItem obsItem : obsItems) {
								if (obsItem.getRoleId() != null
										&& obsItem.getRoleId().equals(changeProcess.getProjectOBSId())) {
									ProjectChangeTask pct = new ProjectChangeTask();
									pct.reviewer = obsItem.getManagerId();
									pct.name = changeProcess.getTaskName();
									reviewer.add(pct);
								}
							}
						}
					}

					ProjectChange pc = Services.get(ProjectService.class).createProjectChange(o);
					GridPart grid = (GridPart) context.getContent();
					grid.insert(pc);
				});

	}
}
