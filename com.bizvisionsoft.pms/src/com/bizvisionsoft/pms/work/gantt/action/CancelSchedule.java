package com.bizvisionsoft.pms.work.gantt.action;

import org.eclipse.jface.dialogs.MessageDialog;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.WorkSpaceService;
import com.bizvisionsoft.service.model.IWBSScope;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.service.model.Workspace;
import com.bizvisionsoft.serviceconsumer.Services;

public class CancelSchedule {
	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) IWBSScope rootInput) {
		if (MessageDialog.openConfirm(br.getCurrentShell(), "�����༭", "��ȷ�ϳ�����ǰ�༭��")) {
			Workspace workspace = rootInput.getWorkspace();
			Result result = Services.get(WorkSpaceService.class).cancelCheckout(workspace, br.getDomain());
			if (Result.CODE_WORK_SUCCESS == result.code) {
				Layer.message("��ǰ�ƻ����޸��ѳ�����");
				br.switchContent("��Ŀ����ͼ", null);
			}
		}
	}
}
