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
			Layer.message("只允许对两个基线进行对比。", Layer.ICON_CANCEL);
			return;
		} else if (list.size() == 0) {
			Layer.message("最少选择一个基线与当前进度进行对比。", Layer.ICON_CANCEL);
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
		// brui.openContent(brui.getAssembly("项目进度跟踪甘特图"), null);
		brui.openContent(brui.getAssembly("项目基线比较甘特图"), projectIds.toArray(new ObjectId[0]));
	}
}
