package com.bizvisionsoft.pms.workpackage.action;

import java.util.ArrayList;
import java.util.List;

import com.bizvisionsoft.bruicommons.factory.action.ActionFactory;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.ActionMenu;
import com.bizvisionsoft.service.model.IWorkPackageMaster;
import com.bizvisionsoft.service.model.TrackView;
import com.bizvisionsoft.service.tools.Check;

public class SwitchWorkPackagePage {

	public static void openWorkPackage(IBruiService brui, IWorkPackageMaster work) {
		List<TrackView> wps = work.getWorkPackageSetting();
		if (Check.isNotAssigned(wps)) {
			brui.openContent(brui.getAssembly("工作包计划.assy"), new Object[] { work, null });
		} else if (wps.size() == 1) {
			brui.openContent(brui.getAssembly("工作包计划.assy"), new Object[] { work, wps.get(0) });
		} else {
			ArrayList<Action> actions = new ArrayList<Action>();
			ActionMenu menu = new ActionMenu(brui);
			wps.forEach(view -> {
				actions.add(new ActionFactory().name(view.get_id().toHexString())
						.text("<div>" + view.getCatagory()
								+ "</div><div style='width:120px;text-overflow:ellipsis;overflow: hidden;'>"
								+ view.getName() + "</div>")
						.tooltips(view.getName()).normalStyle()
						.exec((e, c) -> brui.openContent(brui.getAssembly("工作包计划.assy"), new Object[] { work, view }))
						.get());
			});
			menu.setActions(actions);
			menu.open();
		}
	}
}
