package com.bizvisionsoft.pms.baseline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.bson.types.ObjectId;
import org.eclipse.jface.viewers.StructuredSelection;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruicommons.model.Assembly;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.model.Baseline;
import com.bizvisionsoft.service.model.Project;

public class ComparisonBaselineACT {

	@Inject
	private IBruiService brui;

	@SuppressWarnings("unchecked")
	@Execute
	private void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context) {
		StructuredSelection selection = context.getSelection();
		List<Baseline> list = selection.toList();
		if (list.size() > 2) {
			Layer.message("ֻ������������߽��жԱȡ�", Layer.ICON_CANCEL);
			return;
		} else if (list.size() == 0) {
			Layer.message("����ѡ��һ�������뵱ǰ���Ƚ��жԱȡ�", Layer.ICON_CANCEL);
			return;
		}

		List<ObjectId> projectIds = new ArrayList<ObjectId>();
		String title = "";
		if (list.size() < 2) {
			projectIds.add(((Project) context.getRootInput()).get_id());
			title += "�� : " + ((Project) context.getRootInput()).getName();
		}
		Collections.sort(list, new Comparator<Baseline>() {
			@Override
			public int compare(Baseline o1, Baseline o2) {
				return o1.get_id().compareTo(o2.get_id());
			}

		});
		for (Baseline l : list) {
			if ("".equals(title))
				title += "�� : " + l.getName();
			else
				title += " �� : " + l.getName();

			projectIds.add(l.get_id());
		}
		// brui.openContent(brui.getAssembly("��Ŀ���ȸ��ٸ���ͼ"), null);
		Assembly assembly = brui.getAssembly("��Ŀ���߱Ƚϸ���ͼ");
		assembly.setTitle(title);
		assembly.setStickerTitle(title);
		brui.openContent(assembly, projectIds.toArray(new ObjectId[0]));
	}
}
