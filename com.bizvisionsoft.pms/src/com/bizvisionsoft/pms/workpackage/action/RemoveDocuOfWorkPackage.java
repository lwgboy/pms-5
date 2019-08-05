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
		MessageDialog d = new MessageDialog(br.getCurrentShell(), "请选择操作", null,
				"请选择文档<span class='layui-badge  layui-bg-blue'>" + docu + "</span>的操作："
						+ "。<br><span class='layui-badge  layui-bg-green' style='width:48px;'>是</span> 删除文档，从资料库中彻底删除文档，删除后将无法恢复。"
						+ "<br><span class='layui-badge  layui-bg-orange'  style='width:48px;'>否</span> 解除关联，解除文档与工作交付的关联关系。",
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
