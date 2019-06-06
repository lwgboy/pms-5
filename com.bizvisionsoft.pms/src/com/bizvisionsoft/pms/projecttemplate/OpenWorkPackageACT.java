package com.bizvisionsoft.pms.projecttemplate;

import java.util.ArrayList;
import java.util.List;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruicommons.factory.action.ActionFactory;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.ActionMenu;
import com.bizvisionsoft.service.model.TrackView;
import com.bizvisionsoft.service.model.WorkInTemplate;
import com.bizvisionsoft.service.tools.Check;

//TODO 未进行测试，在site文件中未发现使用该方法的操作
@Deprecated
public class OpenWorkPackageACT {
	
	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT_SELECTION_1ST) WorkInTemplate work) {
		List<TrackView> wps = work.getWorkPackageSetting();
		if (Check.isNotAssigned(wps)) {//TODO ???
			br.openContent(br.getAssembly("模板工作包计划"), new Object[] { work, null });
		} else {
			ArrayList<Action> actions = new ArrayList<Action>();
			ActionMenu menu = new ActionMenu(br);
			wps.forEach(view -> {
				actions.add(new ActionFactory().name(view.get_id().toHexString())
						.text("<div>" + view.getCatagory()
								+ "</div><div style='width:120px;text-overflow:ellipsis;overflow: hidden;'>"
								+ view.getName() + "</div>")
						.tooltips(view.getName()).normalStyle()
						.exec((e, c) -> br.openContent(br.getAssembly("模板工作包计划"), new Object[] { work, view })).get());
			});
			menu.setActions(actions);
			menu.open();
		}

	}
}
