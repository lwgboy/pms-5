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
import com.bizvisionsoft.service.model.EPSInfo;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.service.model.ProjectStatus;

public class OpenEPSOrProject {

	@Inject
	private IBruiService bruiService;

	@Listener({ "�ҹ������Ŀ�嵥/Selection" })
	public void openSelected(Event event) {
		Optional.ofNullable(event.item).map(itm->itm.getData()).ifPresent(em->open(em));
	}

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context,
			@MethodParam(Execute.EVENT) Event event) {
		context.selected(em -> open(em));
	}

	private void open(Object em) {
		if (em instanceof Project) {
			SwitchProjectPage.loadAndOpenProject(bruiService, (Project) em);
		} else if (em instanceof EPSInfo) {
			if (((EPSInfo) em).getType().equals(EPSInfo.TYPE_PROJECT)) {
				if (ProjectStatus.Created.equals(((EPSInfo) em).getStatus())) {
					bruiService.switchPage("��Ŀ��ҳ��������", ((EPSInfo) em).get_id().toHexString());
				} else if (ProjectStatus.Processing.equals(((EPSInfo) em).getStatus())) {
					bruiService.switchPage("��Ŀ��ҳ��ִ�У�", ((EPSInfo) em).get_id().toHexString());
				} else if (ProjectStatus.Closing.equals(((EPSInfo) em).getStatus())) {
					bruiService.switchPage("��Ŀ��ҳ����β��", ((EPSInfo) em).get_id().toHexString());
				} else if (ProjectStatus.Closed.equals(((EPSInfo) em).getStatus())) {
					bruiService.switchPage("��Ŀ��ҳ���رգ�", ((EPSInfo) em).get_id().toHexString());
				}
			}
		}
	}

}
