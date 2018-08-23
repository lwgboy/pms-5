package com.bizvisionsoft.jz.project;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Selector;
import com.bizvisionsoft.jz.PLMObject;
import com.bizvisionsoft.service.model.IWorkPackageMaster;
import com.bizvisionsoft.service.model.TrackView;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.service.model.WorkPackage;

public class AddPLMObject {

	@Inject
	private IBruiService bruiService;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context) {
		Object[] input = (Object[]) context.getInput();
		IWorkPackageMaster workPackageMaster = (IWorkPackageMaster) input[0];
		TrackView tv = (TrackView) input[1];
		if (!workPackageMaster.isTemplate()) {
			// TODO ѡ����ȱ�ٶ������͵Ŀ���
			Selector.open("ѡȡPLM����ѡ����", context, (Work) workPackageMaster, (r) -> {
				r.forEach(plmObject -> {
					if (plmObject instanceof PLMObject) {
						WorkPackage wp = WorkPackage.newInstance(workPackageMaster, tv);
						wp.id = (String) ((PLMObject) plmObject).getValue("id");
						wp.description = (String) ((PLMObject) plmObject).getValue("name");
						wp.verNo = (String) ((PLMObject) plmObject).getValue("majorVerNo");
						wp.planStatus = true;
						wp.documentType = (String) ((PLMObject) plmObject).getValue("type");
						wp.completeStatus = ((PLMObject) plmObject).getValue("status") != null;
						// ((PLMObject) plmObject).getValue("security");
						// ((PLMObject) plmObject).getValue("stage");
						// ((PLMObject) plmObject).getValue("createdBy");
						// ((PLMObject) plmObject).getValue("createDate");
						WorkPackagePlan wpp = (WorkPackagePlan) context.getContent();
						wpp.insert(wp);
					}
				});

			});
		}
	}
}
