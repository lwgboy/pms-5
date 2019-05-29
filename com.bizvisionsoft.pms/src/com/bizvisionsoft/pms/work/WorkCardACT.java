package com.bizvisionsoft.pms.work;

import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.pms.project.SwitchProjectPage;
import com.bizvisionsoft.pms.work.action.IWorkAction;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.TrackView;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class WorkCardACT implements IWorkAction {

	@Inject
	private IBruiService br;
	private WorkService service;

	public WorkCardACT() {
		service = Services.get(WorkService.class);
	}

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Document element,
			@MethodParam(Execute.CONTEXT) BruiAssemblyContext context, @MethodParam(Execute.EVENT) Event e) {
		if (e.text == null)
			return;
		ObjectId _id = element.getObjectId("_id");
		Work work = service.getWork(_id, br.getDomain());
		GridTreeViewer viewer = (GridTreeViewer) context.getContent("viewer");
		if (e.text.startsWith("startWork/")) {
			startWork(work, element, viewer, context);
		} else if (e.text.startsWith("finishWork/")) {
			finishWork(work, element, viewer, context);
		} else if (e.text.startsWith("checkWork/")) {
			checkWork(work, element, viewer, context);
		} else {
			if (e.text.startsWith("openWorkPackage/")) {
				String idx = e.text.split("/")[1];
				openWorkPackage(work, idx, viewer, context);
			} else if (e.text.startsWith("assignWork/")) {
				assignWork(work, element, viewer, context);
			} else if (e.text.startsWith("openProject/")) {
				openProject(work, viewer, context);
			}
		}
	}

	private void checkWork(final Work work, Document doc, GridTreeViewer viewer, BruiAssemblyContext context) {
		checkWork(work, context, w -> {
			List<Document> list = service.listMyExecutingWorkCard(new BasicDBObject("_id", work.get_id()), br.getCurrentUserId(),
					RWT.getLocale().getLanguage(), br.getDomain());
			if (list.size() > 0) {
				doc.put("html", list.get(0).get("html"));
				viewer.update(doc, null);
			}
		});
	}

	private void startWork(Work work, Document doc, GridTreeViewer viewer, BruiAssemblyContext context) {
		startWork(work, w -> {
			((List<?>) viewer.getInput()).remove(doc);
			context.refresh(false);
		});
	}

	private void assignWork(Work work, Document doc, GridTreeViewer viewer, BruiAssemblyContext context) {
		assignWork(work, context, w -> {
			AUtil.simpleCopy(w, work);
			((List<?>) viewer.getInput()).remove(doc);
			context.refresh(false);
			br.updateSidebarActionBudget("ָ�ɹ���");
		});
	}

	private void finishWork(Work work, Document doc, GridTreeViewer viewer, BruiAssemblyContext context) {
		finishWork(work, w -> {
			((List<?>) viewer.getInput()).remove(doc);
			context.refresh(false);
			br.updateSidebarActionBudget("������");
		});
	}

	private void openProject(Work work, GridTreeViewer viewer, BruiAssemblyContext context) {
		SwitchProjectPage.openProject(br, work.getProject_id());
	}

	private void openWorkPackage(Work work, String idx, GridTreeViewer viewer, BruiAssemblyContext context) {
		if ("default".equals(idx)) {
			br.openContent(br.getAssembly("�������ƻ�"), new Object[] { work, null });
		} else {
			List<TrackView> wps = work.getWorkPackageSetting();
			br.openContent(br.getAssembly("�������ƻ�"), new Object[] { work, wps.get(Integer.parseInt(idx)) });
		}
	}

	@Override
	public IBruiService getBruiService() {
		return br;
	}

}
