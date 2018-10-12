package com.bizvisionsoft.pms.work.action;

import java.util.ArrayList;
import java.util.List;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.ActionMenu;
import com.bizvisionsoft.service.model.IWorkPackageMaster;
import com.bizvisionsoft.service.model.TrackView;
import com.bizvisionsoft.service.tools.Check;

public class OpenWorkPackage {

	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		context.selected(elem -> {
			openWorkPackageMenu((IWorkPackageMaster) elem);
		});
	}

	protected void openWorkPackageMenu(final IWorkPackageMaster work) {
		List<TrackView> wps = work.getWorkPackageSetting();
		if (Check.isNotAssigned(wps)) {
			brui.openContent(brui.getAssembly("工作包计划"), new Object[] { work, null });
		} else if (wps.size() == 1) {
			brui.openContent(brui.getAssembly("工作包计划"), new Object[] { work, wps.get(0) });
		} else {
			ArrayList<Action> actions = new ArrayList<Action>();
			ActionMenu menu = new ActionMenu(brui);
			wps.forEach(view -> {
				actions.add(createAction(view));
				menu.handleActionExecute(view.get_id().toHexString(), f -> {
					brui.openContent(brui.getAssembly("工作包计划"), new Object[] { work, view });
					return false;
				});
			});
			menu.setActions(actions);
			menu.open();
		}

	}

	private Action createAction(TrackView view) {
		// 显示资源选择框
		Action hrRes = new Action();
		hrRes.setName(view.get_id().toHexString());
		hrRes.setText("<div>" + view.getCatagory()
				+ "</div><div style='width:120px;text-overflow:ellipsis;overflow: hidden;'>" + view.getName()
				+ "</div>");
		hrRes.setTooltips(view.getName());
		hrRes.setStyle("normal");
		return hrRes;
	}

}
