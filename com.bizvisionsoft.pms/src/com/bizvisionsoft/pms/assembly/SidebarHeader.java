package com.bizvisionsoft.pms.assembly;

import java.util.Optional;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.bizvisionsoft.annotations.ui.common.CreateUI;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.service.UserSession;
import com.bizvisionsoft.bruiengine.ui.BruiToolkit;

public class SidebarHeader {

	@Inject
	private IBruiService bruiService;

	@CreateUI
	public void createUI(Composite parent) {
		BruiToolkit bruiToolkit = UserSession.bruiToolkit();
		int size = 48;
		parent.setLayout(new FormLayout());
		Label pic = new Label(parent, SWT.NONE);
		bruiToolkit.enableMarkup(pic);
		Label title = new Label(parent, SWT.NONE);
		bruiToolkit.enableMarkup(title);
		FormData fd = new FormData(size, size);
		pic.setLayoutData(fd);
		fd.left = new FormAttachment();
		fd.top = new FormAttachment();
		fd = new FormData();
		title.setLayoutData(fd);
		fd.top = new FormAttachment();
		fd.left = new FormAttachment(pic);
		fd.height = size;
		fd.right = new FormAttachment(100);

		// 获得当前进程用户信息中的头像
		String url = Optional.ofNullable(bruiService.getCurrentUserInfo().getHeadpicURL())
				.orElse(bruiService.getResourceURL("/img/user_g_60x60.png"));
		pic.setText("<img alt='headpic' src='" + url + "' width=" + size + "px height=" + size + "px/>");
		title.setText(
				"<div style='color:white;margin-left:8px;margin-top:4px'><div style='font-size:16px;'>项目管理系统</div><div style='font-size:14px;'>BizVision PMS</div></div>");

	}
}
