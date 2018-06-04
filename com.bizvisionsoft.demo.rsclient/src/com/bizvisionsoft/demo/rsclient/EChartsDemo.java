package com.bizvisionsoft.demo.rsclient;

import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.bizivisionsoft.widgets.chart.ECharts;
import com.bizvisionsoft.annotations.ui.common.CreateUI;
import com.bizvisionsoft.annotations.ui.common.GetContainer;
import com.bizvisionsoft.annotations.ui.common.GetContent;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;

public class EChartsDemo {

	@Inject
	private IBruiService bruiService;

	@GetContainer
	@GetContent("chart")
	private ECharts content;

	@Inject
	private BruiAssemblyContext context;

	@CreateUI
	private void createUI(Composite parent) {
		parent.setLayout(new FillLayout());
		createChart(parent);
		((BruiAssemblyContext)context.getChildContextByAssemblyName("Ä³Ä³Í¼±í")).getContent();
	}

	private void createChart(Composite parent) {
		content = new ECharts(parent, SWT.NONE);

		JsonObject option = new JsonObject().add("title", new JsonObject().add("text", "Àý×Ó"))
				.add("legend", new JsonObject().add("data", new JsonArray().add("ÏúÁ¿")))
				.add("xAxis",
						new JsonObject().add("data",
								new JsonArray().add("³ÄÉÀ").add("ÑòÃ«ÉÀ").add("Ñ©·ÄÉÀ").add("¿ã×Ó").add("¸ß¸úÐ¬").add("Íà×Ó")))
				.add("yAxis", new JsonObject())
				.add("series", new JsonArray().add(new JsonObject().add("name", "ÏúÁ¿").add("type", "bar").add("data",
						new JsonArray().add(5).add(20).add(36).add(10).add(10).add(20))));
		content.setOption(option);
	}

}
