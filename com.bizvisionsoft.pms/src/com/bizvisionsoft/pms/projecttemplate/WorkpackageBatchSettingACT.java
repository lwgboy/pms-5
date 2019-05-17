package com.bizvisionsoft.pms.projecttemplate;

import java.util.ArrayList;
import java.util.Iterator;

import org.bson.types.ObjectId;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.ProjectTemplateService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.WorkInTemplate;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class WorkpackageBatchSettingACT {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		GridPart part = (GridPart) context.getContent();
		Iterator<?> iter = part.getViewer().getStructuredSelection().iterator();
		final ArrayList<ObjectId> ids = new ArrayList<ObjectId>();
		while (iter.hasNext()) {
			WorkInTemplate workinfo = (WorkInTemplate) iter.next();
			if (!workinfo.isSummary() && !workinfo.isMilestone()) {
				ids.add(workinfo.get_id());
			}
		}

		if (ids.isEmpty()) {
			Layer.message("请选择需设置工作包的工作");
			return;
		}
		WorkInTemplate workinfo = br.newInstance(WorkInTemplate.class);
		String editor = "工作属性编辑器";
		Editor.create(editor, context, workinfo, false).setTitle("设置工作包").ok((r, wi) -> {
		BasicDBObject set = new BasicDBObject("workPackageSetting",r.get("workPackageSetting"));
			Services.get(ProjectTemplateService.class).updateWork(new FilterAndUpdate()
					.filter(new BasicDBObject("_id", new BasicDBObject("$in", ids))).set(set).bson(), br.getDomain());
			part.refreshAll();
		});

	}

}
