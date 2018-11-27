package com.bizvisionsoft.pms.eps.action;

import java.util.Optional;

import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.md.service.Listener;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.pms.project.SwitchProjectPage;
import com.bizvisionsoft.pms.workpackage.action.SwitchWorkPackagePage;
import com.bizvisionsoft.service.model.EPSInfo;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.ProjectScheduleInfo;
import com.bizvisionsoft.service.model.ProjectStatus;
import com.bizvisionsoft.service.model.Work;

public class OpenEPSOrProject {

	@Inject
	private IBruiService brui;

	@Listener({ "我管理的项目清单/Selection" })
	public void openSelected(Event event) {
		Optional.ofNullable(event.item).map(itm -> itm.getData()).ifPresent(em -> open(em));
	}

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context, @MethodParam(Execute.EVENT) Event event) {
		context.selected(em -> open(em));
	}

	private void open(Object em) {
		if (em instanceof Project) {
			SwitchProjectPage.loadAndOpenProject(brui, (Project) em);
		} else if (em instanceof EPSInfo) {
			if (((EPSInfo) em).getType().equals(EPSInfo.TYPE_PROJECT)) {
				if (ProjectStatus.Created.equals(((EPSInfo) em).getStatus())) {
					brui.switchPage("项目首页（启动）", ((EPSInfo) em).get_id().toHexString());
				} else if (ProjectStatus.Processing.equals(((EPSInfo) em).getStatus())) {
					brui.switchPage("项目首页（执行）", ((EPSInfo) em).get_id().toHexString());
				} else if (ProjectStatus.Closing.equals(((EPSInfo) em).getStatus())) {
					brui.switchPage("项目首页（收尾）", ((EPSInfo) em).get_id().toHexString());
				} else if (ProjectStatus.Closed.equals(((EPSInfo) em).getStatus())) {
					brui.switchPage("项目首页（关闭）", ((EPSInfo) em).get_id().toHexString());
				}
			}
		} else if (em instanceof ProjectScheduleInfo) {
			if (((ProjectScheduleInfo) em).typeEquals(Project.class)) {
				SwitchProjectPage.loadAndOpenProject(brui, ((ProjectScheduleInfo) em).getProject());
			} else if (((ProjectScheduleInfo) em).typeEquals(Work.class)) {
				SwitchWorkPackagePage.openWorkPackage(brui, ((ProjectScheduleInfo) em).getWork());
			}
		}
	}

}
