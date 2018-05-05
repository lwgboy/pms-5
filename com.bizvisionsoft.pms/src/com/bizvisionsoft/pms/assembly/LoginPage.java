package com.bizvisionsoft.pms.assembly;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.bizvisionsoft.annotations.ui.common.CreateUI;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.session.UserSession;
import com.bizvisionsoft.bruiengine.ui.BruiToolkit;
import com.bizvisionsoft.service.UserService;
import com.bizvisionsoft.service.model.User;
import com.bizvisionsoft.serviceconsumer.Services;

public class LoginPage {

	@Inject
	private IBruiService bruiService;

	@CreateUI
	private void createUI(Composite parent) {
		int u = 8;
		parent.setLayout(new FormLayout());

		final Text tName = new Text(parent, SWT.BORDER);
		tName.setMessage("请输入您的账号");
		FormData fd = new FormData();
		tName.setLayoutData(fd);
		fd.top = new FormAttachment(0, 4 * u);
		fd.left = new FormAttachment(0, 4 * u);
		fd.right = new FormAttachment(100, -4 * u);
		fd.width = 240;

		final Text tPassword = new Text(parent, SWT.BORDER | SWT.PASSWORD);
		tPassword.setMessage("登录密码");
		fd = new FormData();
		tPassword.setLayoutData(fd);
		fd.top = new FormAttachment(tName, 2 * u);
		fd.left = new FormAttachment(0, 4 * u);
		fd.right = new FormAttachment(100, -4 * u);
		fd.width = 240;

		Button bLogin = UserSession.bruiToolkit().newStyledControl(Button.class, parent, SWT.PUSH,
				BruiToolkit.CSS_INFO);
		bLogin.setText("登录");
		fd = new FormData();
		bLogin.setLayoutData(fd);
		fd.top = new FormAttachment(tPassword, 4 * u);
		fd.left = new FormAttachment(0, 4 * u);
		fd.right = new FormAttachment(100, -4 * u);
		fd.bottom = new FormAttachment(100, -4 * u);
		fd.height = 40;

		bLogin.addListener(SWT.Selection, e -> {
			try {
				login(tName.getText().trim(), tPassword.getText().trim());
			} catch (Exception e1) {
				MessageDialog.openError(bruiService.getCurrentShell(), "账户验证", e1.getMessage());
			}
		});
	}

	private void login(String userName, String password) throws Exception {
		if (userName.isEmpty()) {
			throw new Exception("请输入正确的用户名。");
		}

		User user = null;
		try {
			user = Services.get(UserService.class).check(userName, password);
		} catch (Exception e) {
		}

		if (user == null) {
			throw new Exception("无法通过账户验证，请输入正确的用户名和密码。");
		}

		bruiService.setCurrentUserInfo(user);
		if (bruiService != null) {
			bruiService.closeCurrentPart();
		}

	}

}
