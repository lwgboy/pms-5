package com.bizvisionsoft.demo.rsclient;

import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.bizivisionsoft.widgets.chart.ECharts;
import com.bizvisionsoft.annotations.ui.common.CreateUI;
import com.bizvisionsoft.annotations.ui.common.GetContainer;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.IBruiService;

public class EChartsDemo {

	@Inject
	private IBruiService bruiService;

	@GetContainer
	private Composite content;

	@CreateUI
	private void createUI(Composite parent) {
		parent.setLayout(new FillLayout());
		createChart(parent);
	}

	private void createChart(Composite parent) {
		ECharts c = new ECharts(parent, SWT.NONE);

		JsonObject option = new JsonObject()
				.add("title", new JsonObject().add("text", "例子"))
				.add("legend",new JsonObject().add("data", new JsonArray().add("销量")))
				.add("xAxis", new JsonObject().add("data", new JsonArray().add("衬衫").add("羊毛衫").add("雪纺衫").add("裤子").add("高跟鞋").add("袜子")))
				.add("yAxis", new JsonObject())
				.add("series", new JsonArray().add(new JsonObject().add("name","销量").add("type", "bar").add("data", new JsonArray().add(5).add(20).add(36).add(10).add(10).add(20))))
				;
				
		c.setOption(option);
	}


}
