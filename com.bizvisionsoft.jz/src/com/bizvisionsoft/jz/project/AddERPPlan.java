package com.bizvisionsoft.jz.project;

import org.eclipse.jface.dialogs.InputDialog;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.IWorkPackageMaster;
import com.bizvisionsoft.service.model.TrackView;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class AddERPPlan {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context) {
		Object[] input = (Object[]) context.getInput();
		IWorkPackageMaster work = (IWorkPackageMaster) input[0];
		TrackView tv = (TrackView) input[1];
		String catagory = tv.getCatagory();
		if ("�ɹ�".equals(catagory)) {
			InputDialog id = new InputDialog(bruiService.getCurrentShell(), "�ɹ��ƻ�������", "����д�ɹ��ƻ��Ĺ�����", null, t -> {
				return t.trim().isEmpty() ? "����д�ɹ��ƻ��Ĺ�����" : null;
			});
			if (InputDialog.OK == id.open()) {
				Services.get(WorkService.class).updateWork(new FilterAndUpdate()
						.filter(new BasicDBObject("_id", work.get_id()).append("workPackageSetting._id", tv.get_id()))
						.set(new BasicDBObject("workPackageSetting.$.trackWorkOrder", id.getValue())).bson());
			}
		} else if ("����".equals(catagory)) {
			Editor.open("ѡȡ�����ƻ�", context, tv, false, (i, o) -> {
				BasicDBObject set = new BasicDBObject();
				set.put("workPackageSetting.$.trackWorkOrder", i.get("trackWorkOrder"));
				set.put("workPackageSetting.$.trackMaterielId", i.get("trackMaterielId"));

				Services.get(WorkService.class).updateWork(new FilterAndUpdate()
						.filter(new BasicDBObject("_id", work.get_id()).append("workPackageSetting._id", tv.get_id()))
						.set(set).bson());
			});
		}

		// ���¼ƻ���ִ�����
	}
}
