package com.bizvisionsoft.demo.rsclient;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;

import com.bizivisionsoft.widgets.datetime.DateTimeSelector;
import com.bizivisionsoft.widgets.datetime.DateTimeSetting;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiService;

public class DateTimeSelectorDemoACT {
	
	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_EVENT) Event event) {
		//演示直接选择日期
		final DateTimeSelector dts = new DateTimeSelector(event.item);
		dts.addListener(SWT.Selection, l->{
			System.out.println(dts.getDate());
			System.out.println(dts.getEndDate());
		});
		dts.show(DateTimeSetting.date());
	}

}
