package com.bizvisionsoft.pms.problem.action;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.bson.Document;
import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruicommons.model.Page;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.service.UserSession;

public class ToDoAction {
	
	@Inject
	private IBruiService br;


	@Execute
	public void execute(@MethodParam(Execute.EVENT) Event e, @MethodParam(Execute.CONTEXT) IBruiContext context,
			@MethodParam(Execute.CONTEXT_SELECTION_1ST) Document element) {
		
		Page page = UserSession.current().getSite().getPageByName("问题解决-TOPS过程");
		String name = null;
		String cId;
		if(element.getString("roleName").equals("ERA")) {
			name = "D0紧急反应行动表格.gridassy";
		}else if(element.getString("roleName").equals("ICA")) {
			name = "D3临时控制行动表格.gridassy";
		}else if(element.getString("roleName").equals("PCA")) {
			name = "D6执行和确认永久纠正措施表格.gridassy";
		}else if(element.getString("roleName").equals("SPA")) {
			name = "D7系统性预防措施表格.gridassy";
		}else if(element.getString("roleName").equals("LRA")) {
			name = "D8损失挽回措施表格.gridassy";
		}
		try {
			cId = URLEncoder.encode("_/" + name, "utf-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			throw new RuntimeException("页面内容组件无法正确编码。" );
		}
		UserSession.current().getEntryPoint().switchPage(page, element.getObjectId("_id").toHexString(), cId, true);
	}

}
