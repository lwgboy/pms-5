package com.bizvisionsoft.pms.workpackage.action;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.DocumentService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.Docu;
import com.bizvisionsoft.service.model.WorkPackage;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class RemoveDocuOfWorkPackage {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_SELECTION_1ST) Docu docu, @MethodParam(Execute.CONTEXT_INPUT_OBJECT) WorkPackage wp,
			@MethodParam(Execute.CONTEXT_CONTENT) GridPart grid) {
		IDialogConstants constants = IDialogConstants.get();
		MessageDialog d = new MessageDialog(br.getCurrentShell(), "��ѡ�����", null,
				"��ѡ���ĵ�<span class='layui-badge  layui-bg-blue'>" + docu + "</span>�Ĳ�����"
						+ "��<br><span class='layui-badge  layui-bg-green' style='width:48px;'>��</span> ɾ���ĵ��������Ͽ��г���ɾ���ĵ���ɾ�����޷��ָ���"
						+ "<br><span class='layui-badge  layui-bg-orange'  style='width:48px;'>��</span> �������������ĵ��빤�������Ĺ�����ϵ��",
				MessageDialog.QUESTION_WITH_CANCEL, new String[] { constants.CANCEL_LABEL, constants.NO_LABEL, constants.YES_LABEL }, 2);
		d.buttonStyle = MessageDialog.getButtonStyle(MessageDialog.QUESTION_WITH_CANCEL);

		int open = d.open();
		if (1 == open) {
			BasicDBObject fu = new FilterAndUpdate().filter(new BasicDBObject("_id", docu.get_id()))
					.update(new BasicDBObject("$pull", new BasicDBObject("workPackage_id", wp.get_id()))).bson();
			Services.get(DocumentService.class).updateDocument(fu, br.getDomain());
			grid.remove(docu);
		} else if (2 == open) {
			Services.get(DocumentService.class).deleteDocument(docu.get_id(), br.getDomain());
			grid.remove(docu);
		}
	}

}
