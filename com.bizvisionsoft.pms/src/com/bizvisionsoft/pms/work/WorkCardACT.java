package com.bizvisionsoft.pms.work;

import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.swt.widgets.Event;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Selector;
import com.bizvisionsoft.pms.project.SwitchProjectPage;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.ICommand;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.TrackView;
import com.bizvisionsoft.service.model.User;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class WorkCardACT {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Document element,
			@MethodParam(Execute.CONTEXT) BruiAssemblyContext context, @MethodParam(Execute.EVENT) Event e) {
		if (e.text == null)
			return;
		ObjectId _id = element.getObjectId("_id");
		Work work = Services.get(WorkService.class).getWork(_id);
		GridTreeViewer viewer = (GridTreeViewer) context.getContent("viewer");
		if (e.text.startsWith("startWork/")) {
			startWork(work, viewer, context);
		} else if (e.text.startsWith("finishWork/")) {
			finishWork(work, viewer, context);
		} else {
			if (e.text.startsWith("openWorkPackage/")) {
				String idx = e.text.split("/")[1];
				openWorkPackage(work, idx, viewer, context);
			} else if (e.text.startsWith("assignWork/")) {
				assignWork(work, viewer, context);
			} else if (e.text.startsWith("openProject/")) {
				openProject(work, viewer, context);
			}
		}
	}

	private void startWork(Work work, GridTreeViewer viewer, BruiAssemblyContext context) {
		if (br.confirm("启动工作", "请确认启动工作" + work + "。<br>系统将记录现在时刻为工作的实际开始时间。")) {
			List<Result> result = Services.get(WorkService.class).startWork(br.command(work.get_id(), new Date(), ICommand.Start_Work));
			if (result.isEmpty()) {
				Layer.message("工作已启动");
				viewer.remove(work);
			}
		}
	}

	private void assignWork(Work work, GridTreeViewer viewer, BruiAssemblyContext context) {
		Selector.open("指派用户选择器", context, work, l -> {
			Services.get(WorkService.class).updateWork(new FilterAndUpdate().filter(new BasicDBObject("_id", work.get_id()))
					.set(new BasicDBObject("chargerId", ((User) l.get(0)).getUserId())).bson());

			work.setChargerId(((User) l.get(0)).getUserId());
			viewer.remove(work);
			br.updateSidebarActionBudget("指派工作");
		});
	}

	private void finishWork(Work work, GridTreeViewer viewer, BruiAssemblyContext context) {
		if (br.confirm("完成工作", "请确认完成工作：" + work + "</span>。<br>系统将记录现在时刻为工作的实际完成时间。")) {
			List<Result> result = Services.get(WorkService.class).finishWork(br.command(work.get_id(), new Date(), ICommand.Finish_Work));
			if (result.isEmpty()) {
				Layer.message("工作已完成");
				viewer.remove(work);
				br.updateSidebarActionBudget("处理工作");
			}
		}
	}

	private void openProject(Work work, GridTreeViewer viewer, BruiAssemblyContext context) {
		SwitchProjectPage.openProject(br, work.getProject_id());
	}

	private void openWorkPackage(Work work, String idx, GridTreeViewer viewer, BruiAssemblyContext context) {
		if ("default".equals(idx)) {
			br.openContent(br.getAssembly("工作包计划"), new Object[] { work, null });
		} else {
			List<TrackView> wps = work.getWorkPackageSetting();
			br.openContent(br.getAssembly("工作包计划"), new Object[] { work, wps.get(Integer.parseInt(idx)) });
		}
	}

}
