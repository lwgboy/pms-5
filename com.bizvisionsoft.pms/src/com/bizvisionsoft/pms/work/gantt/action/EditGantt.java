package com.bizvisionsoft.pms.work.gantt.action;

import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.WorkInfo;

public class EditGantt {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		Project project = (Project) context.getRootInput();
		// ��ʾ�༭��

		// ��������������ƣ�checkOutSchedulePlan������Ҫ�������£�
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
		brui.switchContent("��Ŀ����ͼ(�༭)", null);
	}

}
