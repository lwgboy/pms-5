package com.bizvisionsoft.pms.projecttemplate;

import java.util.List;

import org.bson.types.ObjectId;

import com.bizivisionsoft.widgets.gantt.GanttEvent;
import com.bizvisionsoft.annotations.md.service.DataSet;
import com.bizvisionsoft.annotations.md.service.Listener;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.util.Util;
import com.bizvisionsoft.service.ProjectTemplateService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.ProjectTemplate;
import com.bizvisionsoft.service.model.WorkInTemplate;
import com.bizvisionsoft.service.model.WorkLinkInTemplate;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class GanttDS {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService brui;

	private ProjectTemplateService service;

	private ObjectId template_id;

	@Init
	private void init() {
		service = Services.get(ProjectTemplateService.class);
		template_id = context.getInput(ProjectTemplate.class,false).get_id();
	}

	@DataSet("data")
	public List<WorkInTemplate> listWorks() {
		return service.listWorks(template_id);
	}

	@DataSet("links")
	public List<WorkLinkInTemplate> listLinks() {
		return service.listLinks(template_id);
	}

	@Listener("onAfterTaskAdd")
	public void onAfterTaskAdd(GanttEvent e) {
		WorkInTemplate work = (WorkInTemplate) e.task;
		work.setTemplate_id(template_id);
		service.insertWork(work);
	}

	@Listener({ "onAfterTaskUpdate", "onAfterTaskMove", "onAfterTaskResize", "onAfterTaskProgress" })
	public void onAfterTaskUpdate(GanttEvent e) {
		service.updateWork(new FilterAndUpdate().filter(new BasicDBObject("_id", new ObjectId(e.id)))
				.set(Util.getBson((WorkInTemplate) e.task, "_id")).bson());
	}

	@Listener("onAfterTaskDelete")
	public void onAfterTaskDelete(GanttEvent e) {
		service.deleteWork(new ObjectId(e.id));
	}

	@Listener("onAfterLinkAdd")
	public void onAfterLinkAddInSpace(GanttEvent e) {
		WorkLinkInTemplate link = (WorkLinkInTemplate) e.link;
		link.setTemplate_id(template_id);
		service.insertLink(link);
	}

	@Listener("onAfterLinkUpdate")
	public void onAfterLinkUpdateInSpace(GanttEvent e) {
		service.updateLink(new FilterAndUpdate().filter(new BasicDBObject("_id", new ObjectId(e.id)))
				.set(Util.getBson((WorkLinkInTemplate) e.link, "_id")).bson());
	}

	@Listener("onAfterLinkDelete")
	public void onAfterLinkDeleteInSpace(GanttEvent e) {
		service.deleteLink(new ObjectId(e.id));
	}

}
