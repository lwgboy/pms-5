package com.bizvisionsoft.demo.rsclient;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.bizivisionsoft.widgets.gantt.Config;
import com.bizivisionsoft.widgets.gantt.Gantt;
import com.bizvisionsoft.annotations.ui.common.CreateUI;
import com.bizvisionsoft.annotations.ui.common.GetContainer;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.IBruiService;

public class GanttDemo {

	@Inject
	private IBruiService bruiService;

	@GetContainer
	private Composite content;

	@CreateUI
	private void createUI(Composite parent) {
		parent.setLayout(new FillLayout());
		Gantt gantt = new Gantt(parent, Config.defaultConfig(true));
		Calendar cal = Calendar.getInstance();
		Date from = cal.getTime();
		cal.add(Calendar.MONTH, 6);
		Date to = cal.getTime();
		gantt.setInitDateRange(from, to);

		try {
			InputStream s = new FileInputStream("d:/demodata.json");
			JsonObject inputData = JsonObject.readFrom(new InputStreamReader(s));
			gantt.setInputData(inputData);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
