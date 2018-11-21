package com.bizvisionsoft.pms.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;

import com.bizivisionsoft.widgets.chart.ECharts;
import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.CreateUI;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruicommons.model.Assembly;
import com.bizvisionsoft.bruiengine.BruiAssemblyEngine;
import com.bizvisionsoft.bruiengine.assembly.EditorPart;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.assembly.StickerTitlebar;
import com.bizvisionsoft.bruiengine.assembly.ToolItemDescriptor;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.BruiEditorContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.service.IServiceWithId;
import com.bizvisionsoft.bruiengine.service.UserSession;
import com.bizvisionsoft.bruiengine.ui.AssemblyContainer;
import com.bizvisionsoft.bruiengine.util.BruiColors.BruiColor;
import com.bizvisionsoft.bruiengine.util.BruiToolkit;
import com.bizvisionsoft.bruiengine.util.Controls;
import com.bizvisionsoft.service.CatalogService;
import com.bizvisionsoft.service.model.Catalog;
import com.bizvisionsoft.service.model.Program;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.serviceconsumer.Services;

public class ResourceChartASM {

	@Inject
	private IBruiService brui;

	@Inject
	private BruiAssemblyContext context;

	private Object input;

	private Document option;

	private GridPart part;

	private ECharts chart;

	@Init
	private void init() {
		input = context.getRootInput();
		// option = new Document("");dateRange dateType seriesType
		option = createDefaultOption();
		if (input instanceof Project) {
			// 分析项目中的资源
		} else if (input instanceof Program) {
			// 分析项目集中的资源
			// }else if(input instanceof Portfolio) {//分析项目组合的资源
		} else {// 分析当前用户所在组织的资源

		}
	}

	private Document createDefaultOption() {
		// 今年
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		cal.set(year, 0, 1, 0, 0);
		Date start = cal.getTime();
		cal.add(Calendar.YEAR, 1);
		cal.add(Calendar.MINUTE, -1);
		Date end = cal.getTime();
		return new Document("dateRange", Arrays.asList(start, end)).append("dateType", "月").append("seriesType", "汇总")
				.append("dataType", new ArrayList<String>(Arrays.asList("计划", "实际"))).append("isAggregate", false);
	}

	@CreateUI
	public void createUI(Composite parent) {
		parent.setLayout(new FormLayout());

		// 创建顶栏
		Composite content = Controls.handle(createBar(parent)).loc(SWT.LEFT | SWT.TOP | SWT.RIGHT, 48)
				// 在顶栏下方增加面板
				.add(() -> Controls.contentPanel(parent).mLoc()).formLayout().bg(BruiColor.white).get();

		// 在面板中创建容器（左）
		Controls.comp(content).loc(SWT.TOP | SWT.BOTTOM | SWT.LEFT, 0.25f).formLayout().put(this::leftPane)
				// 在容器右边画一根分割线
				.addRight(() -> Controls.label(content, SWT.SEPARATOR | SWT.VERTICAL).loc(SWT.TOP | SWT.BOTTOM, 1))
				// 在线的右边做图表容器（右）
				.addRight(() -> Controls.comp(content).loc(SWT.TOP | SWT.BOTTOM | SWT.RIGHT).formLayout().put(this::rightPane));

	}

	private void leftPane(Composite parent) {

		Composite title = Controls.comp(parent).html(
				"<div class='label_title' style='height: 48px;width:100%;line-height:48px;padding-left:16px;border-bottom:solid 1px rgb(230,230,230);background-color: #fafafa;'>"
						+ "选择组织或资源</div>")
				.loc(SWT.TOP | SWT.LEFT | SWT.RIGHT, 48).get();

		Composite resSelector = createResourceSelector(parent);

		Composite optionPane = createOptionPane(parent);

		Controls.handle(optionPane).bottom().left().right();

		Controls.handle(resSelector).bottom(optionPane).top(title).left().right();

	}

	private Composite createResourceSelector(Composite parent) {
		AssemblyContainer ac = new AssemblyContainer(parent, context).setAssembly(brui.getAssembly("资源目录")).setServices(brui).create();
		part = (GridPart) ac.getContext().getContent();
		// 控制树的选择
		part.getViewer().getGrid().addListener(SWT.Selection, e -> {
			GridItem item = (GridItem) e.item;
			if (item.getChecked()) {
				GridItem parentItem = item.getParentItem();
				while(parentItem!=null) {
					parentItem.setChecked(false);
					parentItem = parentItem.getParentItem();
				}
				unselectChildren(item);
			}
		});

		Composite resSelector = ac.getContainer();
		return resSelector;
	}

	private void unselectChildren(GridItem item) {
		Arrays.asList(item.getItems()).stream().forEach(i -> {
			i.setChecked(false);
			unselectChildren(i);
		});
	}

	private Composite createOptionPane(Composite parent) {
		Composite pane = Controls.comp(parent).formLayout().get();

		Composite optionPane = Controls.comp(pane).html(
				"<div class='label_title' style='height: 1px;width:100%;line-height:1px;padding-left:16px;border-bottom:solid 1px rgb(230,230,230);'></div>")
				.loc(SWT.TOP | SWT.LEFT | SWT.RIGHT, 1).add(() -> Controls.comp(pane).loc()).get();//

		Assembly assembly = brui.getAssembly("资源图表选项");
		BruiAssemblyEngine engine = BruiAssemblyEngine.newInstance(assembly);
		BruiEditorContext optionContext = UserSession.newEditorContext();
		context.add(optionContext);
		optionContext.setParent(context);
		optionContext.setEmbeded(true);
		optionContext.setEngine(engine);
		optionContext.setInput(option);
		engine.init(new IServiceWithId[] { brui, optionContext });
		final EditorPart editor = (EditorPart) engine.getTarget();
		editor.addToolItem(new ToolItemDescriptor("查询", BruiToolkit.CSS_INFO, this::query));
		engine.createUI(optionPane);
		return pane;
	}

	private void rightPane(Composite parent) {
		chart = Controls.handle(new ECharts(parent, SWT.NONE)).loc().get();
		chart.setOption(new JsonObject());// 设置空对象避免EChart无法完全释放
	}

	private void query(Event e) {
		List<Document> input = part.getCheckedItems(i -> ((Catalog) i).getDocument());
		if (input.isEmpty()) {
			Layer.error("请选择要查询的数据");
			return;
		}
		// TODO 查询日期禁止为空
		List<?> dateRange = (List<?>) option.get("dateRange");
		if (dateRange.get(0) == null || dateRange.get(1) == null) {
			Layer.error("请选择要查询的时间范围");
			return;
		}
		Document chartData = Services.get(CatalogService.class)
				.createResourcePlanAndUserageChart(new Document("input", input).append("option", option));
		JsonObject chartOption = JsonObject.readFrom(((Document) chartData).toJson());
		chart.setOption(chartOption);
	}

	private StickerTitlebar createBar(Composite parent) {
		// TODO 查询错误
		Action a = new Action();
		a.setName("创建项目根文件夹");
		a.setImage("/img/add_16_w.svg");
		a.setTooltips("创建项目根文件夹");
		a.setStyle("normal");

		Action b = new Action();
		b.setName("查询");
		b.setImage("/img/search_w.svg");
		b.setTooltips("查询项目文档");
		b.setStyle("info");

		StickerTitlebar bar = new StickerTitlebar(parent, null, Arrays.asList(a, b)).setActions(context.getAssembly().getActions())
				.setText(context.getAssembly().getTitle());
		bar.addListener(SWT.Selection, l -> {
			// if ("创建项目根文件夹".equals(((Action) l.data).getName())) {
			// if (createFolder(null)) {
			// folderPane.setViewerInput(getInput());
			// }
			// } else if ("查询".equals(((Action) l.data).getName())) {
			// filePane.openQueryEditor();
			// }
		});
		return bar;
	}

}
