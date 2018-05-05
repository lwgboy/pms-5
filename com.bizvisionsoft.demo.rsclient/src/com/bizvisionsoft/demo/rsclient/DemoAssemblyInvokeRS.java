package com.bizvisionsoft.demo.rsclient;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.bizvisionsoft.annotations.ui.common.CreateUI;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.session.UserSession;
import com.bizvisionsoft.bruiengine.ui.BruiToolkit;
import com.bizvisionsoft.bruiengine.util.Util;
import com.bizvisionsoft.service.UserService;
import com.bizvisionsoft.service.model.User;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public class DemoAssemblyInvokeRS {

	@Inject
	private IBruiService bruiService;

	@CreateUI
	private void createUI(Composite parent) {
		parent.setLayout(new RowLayout(SWT.VERTICAL));
		BruiToolkit bruiToolkit = UserSession.bruiToolkit();
		Button button ;

		button = bruiToolkit.newStyledControl(Button.class, parent, SWT.PUSH, BruiToolkit.CSS_NORMAL);
		button.setText("更新一个用户,输入条件");
		button.addListener(SWT.Selection, e -> {
			BasicDBObject filterAndUpdate = new BasicDBObject().append("filter", new BasicDBObject("userId", "zh"))
					.append("update", new BasicDBObject("$set", new BasicDBObject("activated", false)));
			long cnt = Services.get(UserService.class).update(filterAndUpdate);
			System.out.println(cnt);
		});

		button = bruiToolkit.newStyledControl(Button.class, parent, SWT.PUSH, BruiToolkit.CSS_NORMAL);
		button.setText("更新一个用户，根据用户Id");
		button.addListener(SWT.Selection, e -> {
			// BasicDBObject update = new BasicDBObject("$set", new BasicDBObject("name",
			// "ABCD"));
			// long cnt = ServicesLoader.get(UserService.class).updateByUserId("1", update);
			// System.out.println(cnt);
		});

//		button = bruiToolkit.newStyledControl(Button.class, parent, SWT.PUSH, BruiToolkit.CSS_NORMAL);
//		button.setText("创建根组织");
//		button.addListener(SWT.Selection, e -> {
//			Organization orgInfo = new Organization().setFullName("武汉优先商用软件有限公司").setName("优先商软").setNumber("BIZV")
//					.setType("公司");
//			orgInfo = Services.get(OrganizationService.class).insert(orgInfo);
//			System.out.println(orgInfo);
//		});

//		button = bruiToolkit.newStyledControl(Button.class, parent, SWT.PUSH, BruiToolkit.CSS_NORMAL);
//		button.setText("创建下级组织");
//		button.addListener(SWT.Selection, e -> {
//			Organization orgInfo = new Organization().setFullName("优先商软测试部").setName("测试部").setNumber("RTD")
//					.setType("部门").setParentId(new ObjectId("5a98b5de89db0071a0f77269"));
//			orgInfo = Services.get(OrganizationService.class).insert(orgInfo);
//			System.out.println(orgInfo);
//		});

		button = bruiToolkit.newStyledControl(Button.class, parent, SWT.PUSH, BruiToolkit.CSS_NORMAL);
		button.setText("创建一批测试用户");
		button.addListener(SWT.Selection, e -> {
			for (int i = 0; i < 300; i++) {
				String id = Util._10_to_N(i, 24);
				User user = new User();//
				Services.get(UserService.class).insert(user);
			}
			System.out.println("ok");
		});

//		button = bruiToolkit.newStyledControl(Button.class, parent, SWT.PUSH, BruiToolkit.CSS_NORMAL);
//		button.setText("测试查询用户");
//		button.addListener(SWT.Selection, e -> {
//			BasicDBObject condition = new BasicDBObject().append("skip", 0).append("limit", 1).append("filter",
//					new BasicDBObject("userId", "zh"));
//			List<UserInfo> result = Services.get(UserService.class).createDataSet(condition);
//			System.out.println("ok" + result);
//		});
	}
}
