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
		
		Page page = UserSession.current().getSite().getPageByName("������-TOPS����");
		String name = null;
		String cId;
		if(element.getString("roleName").equals("ERA")) {
			name = "D0������Ӧ�ж����.gridassy";
		}else if(element.getString("roleName").equals("ICA")) {
			name = "D3��ʱ�����ж����.gridassy";
		}else if(element.getString("roleName").equals("PCA")) {
			name = "D6ִ�к�ȷ�����þ�����ʩ���.gridassy";
		}else if(element.getString("roleName").equals("SPA")) {
			name = "D7ϵͳ��Ԥ����ʩ���.gridassy";
		}else if(element.getString("roleName").equals("LRA")) {
			name = "D8��ʧ��ش�ʩ���.gridassy";
		}
		try {
			cId = URLEncoder.encode("_/" + name, "utf-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			throw new RuntimeException("ҳ����������޷���ȷ���롣" );
		}
		UserSession.current().getEntryPoint().switchPage(page, element.getObjectId("_id").toHexString(), cId, true);
	}

}
