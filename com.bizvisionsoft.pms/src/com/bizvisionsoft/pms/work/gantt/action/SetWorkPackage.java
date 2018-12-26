package com.bizvisionsoft.pms.work.gantt.action;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Event;

import com.bizivisionsoft.widgets.gantt.GanttEvent;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.IStructuredDataPart;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.WorkSpaceService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.TrackView;
import com.bizvisionsoft.service.model.UpdateWorkPackages;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.model.WorkInfo;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class SetWorkPackage {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context, @MethodParam(Execute.EVENT) Event event) {
		String editor = "�������Ա༭��";

		if (event instanceof GanttEvent) {
			WorkInfo workinfo = (WorkInfo) ((GanttEvent) event).task;
			Editor.create(editor, context, workinfo, false).setTitle(workinfo.toString()).ok((r, wi) -> {
				Services.get(WorkSpaceService.class)
						.updateWork(new FilterAndUpdate().filter(new BasicDBObject("_id", workinfo.get_id())).set(r).bson());
				List<TrackView> wps = wi.getWorkPackageSetting();
				workinfo.setWorkPackageSetting(wps);
			});
		} else {
			context.selected(t -> {
				if (t instanceof Work) {
					Work work = Services.get(WorkService.class).getWork(((Work) t).get_id());
					List<TrackView> oldSetting = new ArrayList<TrackView>();
					if (work.getWorkPackageSetting() != null)
						oldSetting.addAll(work.getWorkPackageSetting());
					Editor.create(editor, context, work, true).setTitle(t.toString()).ok((r, wi) -> {
						if (check(oldSetting, wi.getWorkPackageSetting(), work.get_id())) {
							Services.get(WorkService.class)
									.updateWork(new FilterAndUpdate().filter(new BasicDBObject("_id", ((Work) t).get_id())).set(r).bson());

							// List<TrackView> wps = wi.getWorkPackageSetting();
							// ((Work) t).setWorkPackageSetting(wps);
							// Object content = context.getContent();
							Check.instanceThen(context.getContent(), IStructuredDataPart.class, c -> c.doModify(t, work, r));
						}
					});
				}
			});

		}

	}

	private boolean check(List<TrackView> oldSetting, List<TrackView> newSetting, ObjectId work_id) {
		String message = "";
		WorkService service = Services.get(WorkService.class);
		List<UpdateWorkPackages> uwps = new ArrayList<UpdateWorkPackages>();
		// ��鹤����
		if (Check.isAssigned(oldSetting)) {
			// ѭ�����ԭ�������趨�Ƿ����µ�������
			for (TrackView t1 : oldSetting) {
				boolean b = false;
				if (Check.isAssigned(newSetting))
					for (TrackView t2 : newSetting) {
						if (t1.toString().equals(t2.toString())) {
							b = true;
							break;
						}
					}
				// ���ԭ�������趨�����µ�������
				if (!b) {
					// ���ù������Ƿ���ڹ������ƻ������ڹ������ƻ�ʱ������ʾ.
					if (service.countWorkPackage(
							new BasicDBObject("work_id", work_id).append("catagory", t1.getCatagory()).append("name", t1.getName())) > 0) {
						uwps.add(new UpdateWorkPackages().setWork_id(work_id).setCatagory(t1.getCatagory()).setName(t1.getName()));
						message = "<span class='layui-badge layui-bg-orange'>����</span> �޸Ĺ������趨��ɾ���������ƻ�<br>";
					}
				}
			}
		}
		if (!message.isEmpty()) {
			if (MessageDialog.openQuestion(bruiService.getCurrentShell(), "�޸Ĺ������趨", message + "<br>�Ƿ������")) {
				service.removeWorkPackage(uwps);
			} else
				return false;
		}

		return true;
	}

}
