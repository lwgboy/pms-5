package com.bizvisionsoft.pms.baseline;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.eclipse.jface.viewers.StructuredSelection;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.model.Baseline;
import com.bizvisionsoft.service.model.Project;

public class ComparisonBaselineACT {

	@Inject
	private IBruiService brui;

	@Execute
	private void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context) {
		StructuredSelection selection = context.getSelection();
		List<?> list = selection.toList();
		if (list.size() > 2) {
			Layer.message("ֻ������������߽��жԱȡ�", Layer.ICON_CANCEL);
			return;
		} else if (list.size() == 0) {
			Layer.message("����ѡ��һ�������뵱ǰ���Ƚ��жԱȡ�", Layer.ICON_CANCEL);
			return;
		}

		List<ObjectId> projectIds = new ArrayList<ObjectId>();
		list.forEach(l -> {
			if (l instanceof Baseline) {
				projectIds.add(((Baseline) l).get_id());
			}
		});
		if (projectIds.size() < 2) {
			projectIds.add(((Project) context.getRootInput()).get_id());
		}
		// brui.openContent(brui.getAssembly("��Ŀ���ȸ��ٸ���ͼ"), null);
		brui.openContent(brui.getAssembly("��Ŀ���߱Ƚϸ���ͼ"), projectIds.toArray(new ObjectId[0]));
	}
}
