package com.bizvisionsoft.pms.problem.action;

import org.eclipse.swt.widgets.Event;

import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiService;

public class D4Card {
	
	@Inject
	private IBruiService br;


	@Execute
	public void execute(@MethodParam(Execute.EVENT) Event e) {
		String parameter;
		if("open/make".equals(e.text)) {
			parameter = "{\"type\":\"�������-����\",\"title\":\"��������������\"}";
		}else {
			parameter = "{\"type\":\"�������-����\",\"title\":\"���������������\"}";
		}
		br.switchContent("D4��ϵͼ", null, parameter);
	}

}
