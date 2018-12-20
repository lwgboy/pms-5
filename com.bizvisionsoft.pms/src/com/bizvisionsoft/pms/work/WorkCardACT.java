package com.bizvisionsoft.pms.work;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruicommons.factory.assembly.EditorFactory;
import com.bizvisionsoft.bruicommons.factory.fields.CheckFieldFactory;
import com.bizvisionsoft.bruicommons.factory.fields.TextFieldFactory;
import com.bizvisionsoft.bruicommons.model.FormField;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.bruiengine.ui.Selector;
import com.bizvisionsoft.mongocodex.tools.BsonTools;
import com.bizvisionsoft.pms.project.SwitchProjectPage;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.CheckItem;
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
		Work work = service.getWork(_id);
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
		Document input = new Document();

		final Map<String, CheckItem> checklistMap = new LinkedHashMap<String, CheckItem>();
		EditorFactory ef = new EditorFactory().title("工作检查表").labelAlignment(SWT.LEFT).labelWidth(840);

		for (CheckItem ci : work.getChecklist()) {
			FormField checkField = new CheckFieldFactory().text(ci.getDescription()).get();
			String name = checkField.getName();
			FormField remarkField = new TextFieldFactory().message("如不能通过检查，请说明").labelStyle(TextFieldFactory.LABEL_HIDE)
					.name("remark-" + name).get();

			ef.appendField(checkField).appendField(remarkField);

			checklistMap.put(name, ci);
			input.put(name, ci.isChecked());
			input.put("remark-" + name, ci.getRemark());
		}

		new Editor<Document>(ef.get(), context).setEditable(true).setInput(input).ok((d, t) -> {
			BasicDBObject filter = new BasicDBObject("_id", work.get_id());

			Iterator<String> iter = t.keySet().iterator();
			boolean finished = true;
			while (iter.hasNext()) {
				String key = iter.next();
				if (key.startsWith("remark-")) {
					String key1 = key.substring(7);
					String remark = t.getString(key);
					checklistMap.get(key1).setRemark(remark);
				} else {
					boolean value = Boolean.TRUE.equals(t.get(key));
					checklistMap.get(key).setChecked(value);
					finished &= value;
				}
			}
			Object checkItems = BsonTools.encodeBsonValue(new ArrayList<>(checklistMap.values()));
			FilterAndUpdate fu = new FilterAndUpdate().filter(filter).set(new BasicDBObject("checklist", checkItems));
			service.updateWork(fu.bson());
			if (finished) {
				List<Document> list = service.listMyExecutingWorkCard(filter, br.getCurrentUserId());
				if (list.size() > 0) {
					doc.put("html", list.get(0).get("html"));
					viewer.update(doc, null);
				}
			}
		});
	}

	private void startWork(Work work, Document doc, GridTreeViewer viewer, BruiAssemblyContext context) {
		if (br.confirm("启动工作", "请确认启动工作" + work + "。<br>系统将记录现在时刻为工作的实际开始时间。")) {
			List<Result> result = service.startWork(br.command(work.get_id(), new Date(), ICommand.Start_Work));
			if (result.isEmpty()) {
				Layer.message("工作已启动");
				((List<?>) viewer.getInput()).remove(doc);
				viewer.remove(doc);
			}
		}
	}

	private void assignWork(Work work, Document doc, GridTreeViewer viewer, BruiAssemblyContext context) {
		Selector.open("指派用户选择器", context, work, l -> {
			service.updateWork(new FilterAndUpdate().filter(new BasicDBObject("_id", work.get_id()))
					.set(new BasicDBObject("chargerId", ((User) l.get(0)).getUserId())).bson());

			work.setChargerId(((User) l.get(0)).getUserId());
			((List<?>) viewer.getInput()).remove(doc);
			viewer.remove(doc);
			br.updateSidebarActionBudget("指派工作");
		});
	}

	private void finishWork(Work work, Document doc, GridTreeViewer viewer, BruiAssemblyContext context) {
		// TODO 改为commandHandler
		if (br.confirm("完成工作", "请确认完成工作：" + work)) {
			List<Result> result = service.finishWork(br.command(work.get_id(), new Date(), ICommand.Finish_Work));
			if (result.isEmpty()) {
				Layer.message("工作已完成");
				((List<?>) viewer.getInput()).remove(doc);
				viewer.remove(doc);
				br.updateSidebarActionBudget("处理工作");
			} else {
				String msg = result.stream().filter(r -> r.type == Result.TYPE_ERROR).map(r -> r.message + "<br>").reduce("",
						String::concat);
				br.error("完成工作", msg);
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
