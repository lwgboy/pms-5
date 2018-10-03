package com.bizvisionsoft.pms.projecttemplate;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.ActionMenu;
import com.bizvisionsoft.service.model.TrackView;
import com.bizvisionsoft.service.model.WorkInTemplate;
import com.bizvisionsoft.service.tools.Checker;

public class OpenWorkPackageACT {
	@Inject
	private IBruiService brui;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(Execute.PARAM_EVENT) Event event) {
		context.selected(elem -> {
			openWorkPackageMenu((WorkInTemplate) elem);
		});
	}

	protected void openWorkPackageMenu(final WorkInTemplate work) {
		List<TrackView> wps = work.getWorkPackageSetting();
		if (Checker.isNotAssigned(wps)) {
			brui.openContent(brui.getAssembly("模板工作包计划"), new Object[] { work, null });
		} else {
			ArrayList<Action> actions = new ArrayList<Action>();
			ActionMenu menu = new ActionMenu(brui);
			wps.forEach(view -> {
				actions.add(createAction(view));
				menu.handleActionExecute(view.get_id().toHexString(), f -> {
					brui.openContent(brui.getAssembly("模板工作包计划"), new Object[] { work, view });
					return false;
				});
			});
			menu.setActions(actions);
			menu.open();
		}

	}

	private Action createAction(TrackView view) {
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
