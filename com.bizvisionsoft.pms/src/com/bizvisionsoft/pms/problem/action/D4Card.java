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
			parameter = "{\"type\":\"因果分析-制造\",\"title\":\"问题产生因果分析\"}";
		}else {
			parameter = "{\"type\":\"因果分析-流出\",\"title\":\"问题流出因果分析\"}";
		}
		br.switchContent("D4关系图", null, parameter);
	}

}
