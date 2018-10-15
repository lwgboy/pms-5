package com.bizvisionsoft.pms.obs.action;

import org.bson.types.ObjectId;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.Work;
import com.bizvisionsoft.serviceconsumer.Services;

public class AssignToWork {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.ROOT_CONTEXT_INPUT_OBJECT) Object input) {
		if (input instanceof Project) {
			ObjectId project_id = ((Project) input).get_id();
			// ����Ƿ������Ҫ����ԭ�й�������Ĺ���
			if (Services.get(WorkService.class).checkCoverWork(project_id)) {
				// ����ʱ������ʾѯ���Ƿ񸲸�ԭ�еĹ�������
				IDialogConstants constants = IDialogConstants.get();
				MessageDialog d = new MessageDialog(br.getCurrentShell(), "���乤��", null,
						"��ȷ���Ƿ񽫽�ɫ�ĵ����߷��䵽�����ĸ����˺�ָ���ߣ�������ԭ�еĹ�������", MessageDialog.QUESTION_WITH_CANCEL,
						new String[] { constants.CANCEL_LABEL, constants.NO_LABEL, constants.YES_LABEL }, 2);
				d.buttonStyle = MessageDialog.getButtonStyle(MessageDialog.QUESTION_WITH_CANCEL);

				// ��ȡ�û�������ť����
				int open = d.open();
				// ѡ��ȡ��ʱ,ֱ�ӽ�������
				if (open == 0) {
					return;
				}
				// ѡ��"��"ʱ,������Ҫ�����Ѿ�����Ĺ���.
				boolean cover = false;
				if (open == 2) {
					cover = true;
				}

				// ���÷���,Ϊ����������Ա
				Services.get(WorkService.class).assignRoleToProject(project_id, cover);
				Layer.message("����ɽ�ɫ�Ĺ�������");
			} else if (br.confirm("���乤��", "��ȷ�Ͻ���ɫ�ĵ����߷��䵽�����ĸ����˺�ָ���ߡ�")) {
				// ������ʾѯ���Ƿ���Ҫ���为���˺�ָ����
				Services.get(WorkService.class).assignRoleToProject(project_id, false);
				Layer.message("����ɽ�ɫ�Ĺ�������");
			}
		} else if (input instanceof Work) {
			// TODO ԭ�׶��Ŷӷ��乤�����ܣ��粻�����ӽ׶��Ŷӹ�����ȥ����
			if (br.confirm("���乤��", "��ȷ�Ͻ���ɫ�ĵ����߷��䵽�����ĸ����˺�ָ���ߡ�")) {
				Services.get(WorkService.class).assignRoleToStage(((Work) input).get_id());
				Layer.message("����ɽ�ɫ�Ĺ�������");
			}
		}
	}
}
