package com.bizvisionsoft.demo.rsclient;

import java.util.Date;

import org.eclipse.swt.widgets.Event;

import com.bizivisionsoft.widgets.datetime.DateTimeSetting;
import com.bizvisionsoft.annotations.ui.common.Execute;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.annotations.ui.common.MethodParam;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.DateTimeInputDialog;

public class DateTimeSelectorDemoACT {

	@Inject
	private IBruiService br;

	@Execute
	public void execute(@MethodParam(Execute.PARAM_EVENT) Event event) {
		DateTimeInputDialog dtid = new DateTimeInputDialog(br.getCurrentShell(), "演示选择时间范围", "请选择时间范围", null)
				.setDateSetting(DateTimeSetting.date().setRange(true));
		if (dtid.open() == DateTimeInputDialog.OK) {
			Date[] range = dtid.getValues();
			System.out.println(range[0]);
			System.out.println(range[1]);
		}
	}

}
