package com.awesometech.leoco.action;


import java.util.List;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.UrlLauncher;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.service.model.TrackView;

public class OpenOA {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.CONTEXT) IBruiContext context) {
		TrackView view = (TrackView) context.getInput();
		@SuppressWarnings("unchecked")
		List<Object> list = (List<Object>)view.getParameter("WF_INSTS");
		String instID = (String)list.get(0);
		RWT.getClient().getService(UrlLauncher.class).openURL("http://oa.leocodg.com.cn/workflow/request/ViewRequest_new.jsp?requestid="+instID);
	}

}
