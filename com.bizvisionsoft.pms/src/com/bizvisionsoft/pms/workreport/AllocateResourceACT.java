package com.bizvisionsoft.pms.workreport;

import java.util.Arrays;

import org.eclipse.swt.widgets.Event;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.ActionMenu;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.service.model.ResourceAssignment;
import com.bizvisionsoft.service.model.WorkReportAssignment;

public class AllocateResourceACT {
	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(value = Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(value = Execute.PARAM_EVENT) Event event) {
		WorkReportAssignment workReportAssignment = (WorkReportAssignment) context.getInput();
		if (workReportAssignment.getWork() == null) {
			Layer.message("����ѡ��Ҫ�����Դ�����Ĺ�����");
			return;
		}

		// ��ʾ��Դѡ���
		Action hrRes = new Action();
		hrRes.setName("hr");
		hrRes.setText("������Դ");
		hrRes.setImage("/img/team_w.svg");
		hrRes.setStyle("normal");

		Action eqRes = new Action();
		eqRes.setName("eq");
		eqRes.setText("�豸��Դ");
		eqRes.setImage("/img/equipment_w.svg");
		eqRes.setStyle("normal");

		Action typedRes = new Action();
		typedRes.setName("tr");
		typedRes.setText("��Դ����");
		typedRes.setImage("/img/resource_w.svg");
		typedRes.setStyle("info");

		// ����menu
		new ActionMenu(brui).setActions(Arrays.asList(hrRes, eqRes, typedRes)).handleActionExecute("hr", a -> {
			addResource("��������-���������Դ�༭��", context, workReportAssignment);
			return false;
		}).handleActionExecute("eq", a -> {
			addResource("��������-����豸��Դ�༭��", context, workReportAssignment);
			return false;
		}).handleActionExecute("tr", a -> {
			addResource("��������-�����Դ���ͱ༭��", context, workReportAssignment);
			return false;
		}).open();
	}

	private void addResource(String editorId, IBruiContext context, WorkReportAssignment workReportAssignment) {
		// TODO
		ResourceAssignment resourceAssignment = new ResourceAssignment()
				.setWork_id(workReportAssignment.getWork().get_id());
		Editor.open(editorId, context, resourceAssignment, (t, r) -> {
			// Services.get(WorkService.class).addResourceActual(r);
			// GridPart content = (GridPart) context.getContent();
			// content.insert(Services.get(WorkService.class).listResourceActual(work.get_id()));
		});

	}
}
