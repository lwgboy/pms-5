package com.bizvisionsoft.pms.projecttemplate;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GanttPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Selector;
import com.bizvisionsoft.service.ProjectTemplateService;
import com.bizvisionsoft.service.model.ProjectTemplate;
import com.bizvisionsoft.service.model.WBSModule;
import com.bizvisionsoft.service.model.WorkInTemplate;
import com.bizvisionsoft.service.model.WorkLinkInTemplate;
import com.bizvisionsoft.service.tools.Checker;
import com.bizvisionsoft.serviceconsumer.Services;

public class AddWBSModuleACT {
	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(Execute.PARAM_EVENT) Event event) {
		StructuredSelection selection = context.getSelection();
		WorkInTemplate parent = (WorkInTemplate) selection.getFirstElement();
		Selector.open("WBS模块选择器", context, null, r -> {
			GanttPart content = (GanttPart) context.getContent();
			Object input = context.getInput();
			if (input == null) {
				input = context.getRootInput();
			}
			append(content, input, (WBSModule) r.get(0), parent);
		});

	}

	private void append(GanttPart gantt, Object input, WBSModule module, WorkInTemplate parent) {
		ObjectId templateId ;
		if(input instanceof ProjectTemplate) {
			templateId = ((ProjectTemplate) input).get_id();
		}else if(input instanceof WBSModule){
			templateId = ((WBSModule) input).get_id();
		}else {
			templateId = null;
		}
		
		String var = module.getVar();
		Map<String, String> varMap = new HashMap<String, String>();
		if (!Checker.isNotAssigned(var)) {
			String[] v = var.trim().split(";");
			for (int i = 0; i < v.length; i++) {
				InputDialog id = new InputDialog(br.getCurrentShell(), "WBS模块参数", v[i], "", null);
				if (id.open() == InputDialog.OK) {
					varMap.put(v[i], id.getValue());
				}
			}
		}
		List<WorkInTemplate> works = Services.get(ProjectTemplateService.class).listWorks(module.get_id());
		Map<ObjectId, WorkInTemplate> idMap = new HashMap<ObjectId, WorkInTemplate>();
		for (int i = 0; i < works.size(); i++) {
			WorkInTemplate work = works.get(i);

			// 更改id
			ObjectId newId = new ObjectId();
			idMap.put(work.get_id(), work);
			work.set_id(newId);

			String text = updateName(work.getText(), varMap);
			work.setText(text);
			text = updateName(work.getFullName(), varMap);
			work.setFullName(text);

			ObjectId parent_id = work.getParent_id();
			if (parent_id != null) {
				work.setParent_id(idMap.get(parent_id).get_id());
			} else {
				if (parent != null) {
					work.setParent_id(parent.get_id());
				}
			}

			if(templateId!=null) {
				work.setTemplate_id(templateId);
			}
			
			gantt.addTask(work);
		}
		
		List<WorkLinkInTemplate> links = Services.get(ProjectTemplateService.class).listLinks(module.get_id());
		for (int i = 0; i < links.size(); i++) {
			WorkLinkInTemplate link = links.get(i);
			link.set_id(new ObjectId());
			
			ObjectId src = link.getSourceId();
			link.setSource(idMap.get(src));
			
			ObjectId tgt = link.getTargetId();
			link.setTarget(idMap.get(tgt));
			
			if(templateId!=null) {
				link.setTemplate_id(templateId);
			}
			
			gantt.addLink(link);
		}
	}

	private String updateName(String name, Map<String, String> varMap) {
		Iterator<String> iter = varMap.keySet().iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			String value = varMap.get(key);
			name = name.replaceAll("\\[" + key + "\\]", value);
		}
		return name;
	}

}
