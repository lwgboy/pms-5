package com.bizvisionsoft.pms.work.gantt.action;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.WorkSpaceService;
import com.bizvisionsoft.service.model.IWBSScope;
import com.bizvisionsoft.service.model.Result;
import com.bizvisionsoft.serviceconsumer.Services;

public class EditGantt {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		IWBSScope wbsScope = (IWBSScope) context.getRootInput();

		// ��ʾ�༭��
		String checkOutUserId = wbsScope.getCheckOutUserId();
		if (checkOutUserId == null || "".equals(checkOutUserId) || brui.getCurrentUserId().equals(checkOutUserId)) {
			// ��������������ƣ�checkOutSchedulePlan������Ҫ�������£�
			Result result = Services.get(WorkSpaceService.class).checkOutSchedulePlan(wbsScope.getCheckOutKey(),
					brui.getCurrentUserId(), false);
			if (Result.CODE_SUCCESS == result.code) {
				brui.switchContent("��Ŀ����ͼ(�༭)", wbsScope);
			} else if (Result.CODE_HASCHECKOUTSUB == result.code) {
				if (MessageDialog.openConfirm(brui.getCurrentShell(), "��ʾ", result.message + "��ȷ���Ƿ�ȡ���¼��ļ����")) {
					result = Services.get(WorkSpaceService.class).checkOutSchedulePlan(wbsScope.getCheckOutKey(),
							brui.getCurrentUserId(), true);
					if (Result.CODE_SUCCESS == result.code) {
						brui.switchContent("��Ŀ����ͼ(�༭)", wbsScope);
					}
				}
			}
		} else {
			MessageDialog.openError(brui.getCurrentShell(), "��ʾ", "��û�б༭�ƻ���Ȩ�ޡ�");
		}
		// ����Ҫ����ǰ�û�id���ж���������Լ���Ȩ����ı���
		// ����Ҫ�� �Ƿ�ȡ���¼���� ��cancelCheckOutSubSchedule����
		// �÷�����÷�ʽ���£�
		// checkOutSchedulePlan( WBS�ڵ��»���Id, ������������ȡ���¼����)
		// 1.��������Լ���Ȩ����ģ����ؽ��
		// ����com.bizvisionsoft.service.model .Result����errorCode���Լ���һ�� ��ΪResult�ĳ�����
		// 2.��ʱ������¼�����Ļ�������cancelCheckOutSubScheduleΪfalse�����ؽ��
		// ����com.bizvisionsoft.service.model .Result����errorCode���Լ���һ�� ��ΪResult�ĳ�����
		// �ͻ�����ʾ������ʾ�������������ǡ����¼�������Ĵ���ʱ����ʾ��ѯ���Ƿ�ȡ���¼��ļ��������ǣ��������£�
		// checkOutSchedulePlan( WBS�ڵ��»���Id, ����������ȡ���¼����)
		// 3.������¼�����Ļ�������cancelCheckOutSubScheduleΪtrue��ȡ���¼�������޸ļ��״̬��������������¼�������ݡ�����֪ͨ����ʱ����������ɫ���ֿ���Ϊ��һ����cancelCheckOutSchedulePlan��work_id,
		// userId����
		// 4.���WBS�ڵ��»���Id���Լ��������¼��������������ݣ�����ˣ�ʱ�䣬copy
		// ���سɹ�Result��

		// Editor.create("��Ŀ����ͼ(�༭)", context.setInput(project),project,
		// false).setTitle("���ȼƻ�����ͼ").ok((r, o) -> {
		// });
	}

}
