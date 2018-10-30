package com.bizvisionsoft.pms.assembly;

import java.util.Optional;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

import com.bizvisionsoft.annotations.ui.common.CreateUI;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.BruiService;
import com.bizvisionsoft.bruiengine.util.Controls;

public class SidebarHeader {

	@Inject
	private BruiService br;

	private static final int size = 48;

	@CreateUI
	public void createUI(Composite parent) {
		Controls.handle(parent).layout(new FormLayout());

		Controls.label(parent).loc(SWT.LEFT | SWT.TOP, size, size).html(getImageHtml())
				.addRight(() -> Controls.label(parent).height(size).loc(SWT.TOP | SWT.RIGHT).html(getNameHtml()));

	}

	private String getImageHtml() {
		String url = Optional.ofNullable(br.getCurrentUserInfo().getHeadpicURL()).orElse("resource/image/icon_p_br_bl.svg");
		return "<img alt='headpic' src='" + url + "' width=" + size + "px height=" + size + "px/>";
	}

	private String getNameHtml() {
		String name = br.getCurrentUserInfo().getName();
		String cid = br.getCurrentConsignerId();
		String uid = br.getCurrentUserId();
		if (!uid.equals(cid)) {
			name += " (" + br.getCurrentConsignerInfo().getName() + " ´ú¹Ü)";
		}

		return "<div style='margin-top:4px;width:144px'><img src='resource/image/logo_w.svg' height=22px><div style='margin-left:2px;margin-top:2px;color:white;font-size:13px;'>"
				+ name + "</div></div>";
	}
}
