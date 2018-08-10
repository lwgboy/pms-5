package com.bizvisionsoft.pms.eps.action;

import java.util.Optional;

import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.md.service.Listener;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.model.EPSInfo;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.ProjectSet;
import com.bizvisionsoft.service.model.ProjectStatus;

public class OpenProjectOrProjectSet {

	@Inject
	private IBruiService bruiService;

	@Listener({ "我管理的项目清单/Selection" })
	public void openSelected(Event event) {
		Optional.ofNullable(event.item).map(itm->itm.getData()).ifPresent(em->open(em));
	}

	@Execute
	public void execute(@MethodParam(Execute.PARAM_CONTEXT) IBruiContext context,
			@MethodParam(Execute.PARAM_EVENT) Event event) {
		context.selected(em -> open(em));
	}

	private void open(Object em) {
		if (em instanceof ProjectSet) {

		} else if (em instanceof Project) {
			if (ProjectStatus.Created.equals(((Project) em).getStatus())) {
				bruiService.switchPage("项目首页（启动）", ((Project) em).get_id().toHexString());
			} else if (ProjectStatus.Processing.equals(((Project) em).getStatus())) {
				bruiService.switchPage("项目首页（执行）", ((Project) em).get_id().toHexString());
			} else if (ProjectStatus.Closing.equals(((Project) em).getStatus())) {
				bruiService.switchPage("项目首页（收尾）", ((Project) em).get_id().toHexString());
			} else if (ProjectStatus.Closed.equals(((Project) em).getStatus())) {
				bruiService.switchPage("项目首页（关闭）", ((Project) em).get_id().toHexString());
			}
		} else if (em instanceof EPSInfo) {
			if (((EPSInfo) em).getType().equals(EPSInfo.TYPE_PROJECT)) {
				if (ProjectStatus.Created.equals(((EPSInfo) em).getStatus())) {
					bruiService.switchPage("项目首页（启动）", ((EPSInfo) em).get_id().toHexString());
				} else if (ProjectStatus.Processing.equals(((EPSInfo) em).getStatus())) {
					bruiService.switchPage("项目首页（执行）", ((EPSInfo) em).get_id().toHexString());
				} else if (ProjectStatus.Closing.equals(((EPSInfo) em).getStatus())) {
					bruiService.switchPage("项目首页（收尾）", ((EPSInfo) em).get_id().toHexString());
				} else if (ProjectStatus.Closed.equals(((EPSInfo) em).getStatus())) {
					bruiService.switchPage("项目首页（关闭）", ((EPSInfo) em).get_id().toHexString());
				}
			}
		}
	}

}
