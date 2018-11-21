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
			// ������Ŀ�е���Դ
		} else if (input instanceof Program) {
			// ������Ŀ���е���Դ
			// }else if(input instanceof Portfolio) {//������Ŀ��ϵ���Դ
		} else {// ������ǰ�û�������֯����Դ

		}
	}

	private Document createDefaultOption() {
		// ����
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		cal.set(year, 0, 1, 0, 0);
		Date start = cal.getTime();
		cal.add(Calendar.YEAR, 1);
		cal.add(Calendar.MINUTE, -1);
		Date end = cal.getTime();
		return new Document("dateRange", Arrays.asList(start, end)).append("dateType", "��").append("seriesType", "����")
				.append("dataType", new ArrayList<String>(Arrays.asList("�ƻ�", "ʵ��"))).append("isAggregate", false);
	}

	@CreateUI
	public void createUI(Composite parent) {
		parent.setLayout(new FormLayout());

		// ��������
		Composite content = Controls.handle(createBar(parent)).loc(SWT.LEFT | SWT.TOP | SWT.RIGHT, 48)
				// �ڶ����·��������
				.add(() -> Controls.contentPanel(parent).mLoc()).formLayout().bg(BruiColor.white).get();

		// ������д�����������
		Controls.comp(content).loc(SWT.TOP | SWT.BOTTOM | SWT.LEFT, 0.25f).formLayout().put(this::leftPane)
				// �������ұ߻�һ���ָ���
				.addRight(() -> Controls.label(content, SWT.SEPARATOR | SWT.VERTICAL).loc(SWT.TOP | SWT.BOTTOM, 1))
				// ���ߵ��ұ���ͼ���������ң�
				.addRight(() -> Controls.comp(content).loc(SWT.TOP | SWT.BOTTOM | SWT.RIGHT).formLayout().put(this::rightPane));

	}

	private void leftPane(Composite parent) {

		Composite title = Controls.comp(parent).html(
				"<div class='label_title' style='height: 48px;width:100%;line-height:48px;padding-left:16px;border-bottom:solid 1px rgb(230,230,230);background-color: #fafafa;'>"
						+ "ѡ����֯����Դ</div>")
				.loc(SWT.TOP | SWT.LEFT | SWT.RIGHT, 48).get();

		Composite resSelector = createResourceSelector(parent);

		Composite optionPane = createOptionPane(parent);

		Controls.handle(optionPane).bottom().left().right();

		Controls.handle(resSelector).bottom(optionPane).top(title).left().right();

	}

	private Composite createResourceSelector(Composite parent) {
		AssemblyContainer ac = new AssemblyContainer(parent, context).setAssembly(brui.getAssembly("��ԴĿ¼")).setServices(brui).create();
		part = (GridPart) ac.getContext().getContent();
		// ��������ѡ��
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

		Assembly assembly = brui.getAssembly("��Դͼ��ѡ��");
		BruiAssemblyEngine engine = BruiAssemblyEngine.newInstance(assembly);
		BruiEditorContext optionContext = UserSession.newEditorContext();
		context.add(optionContext);
		optionContext.setParent(context);
		optionContext.setEmbeded(true);
		optionContext.setEngine(engine);
		optionContext.setInput(option);
		engine.init(new IServiceWithId[] { brui, optionContext });
		final EditorPart editor = (EditorPart) engine.getTarget();
		editor.addToolItem(new ToolItemDescriptor("��ѯ", BruiToolkit.CSS_INFO, this::query));
		engine.createUI(optionPane);
		return pane;
	}

	private void rightPane(Composite parent) {
		chart = Controls.handle(new ECharts(parent, SWT.NONE)).loc().get();
		chart.setOption(new JsonObject());// ���ÿն������EChart�޷���ȫ�ͷ�
	}

	private void query(Event e) {
		List<Document> input = part.getCheckedItems(i -> ((Catalog) i).getDocument());
		if (input.isEmpty()) {
			Layer.error("��ѡ��Ҫ��ѯ������");
			return;
		}
		// TODO ��ѯ���ڽ�ֹΪ��
		List<?> dateRange = (List<?>) option.get("dateRange");
		if (dateRange.get(0) == null || dateRange.get(1) == null) {
			Layer.error("��ѡ��Ҫ��ѯ��ʱ�䷶Χ");
			return;
		}
		Document chartData = Services.get(CatalogService.class)
				.createResourcePlanAndUserageChart(new Document("input", input).append("option", option));
		JsonObject chartOption = JsonObject.readFrom(((Document) chartData).toJson());
		chart.setOption(chartOption);
	}

	private StickerTitlebar createBar(Composite parent) {
		// TODO ��ѯ����
		Action a = new Action();
		a.setName("������Ŀ���ļ���");
		a.setImage("/img/add_16_w.svg");
		a.setTooltips("������Ŀ���ļ���");
		a.setStyle("normal");

		Action b = new Action();
		b.setName("��ѯ");
		b.setImage("/img/search_w.svg");
		b.setTooltips("��ѯ��Ŀ�ĵ�");
		b.setStyle("info");

		StickerTitlebar bar = new StickerTitlebar(parent, null, Arrays.asList(a, b)).setActions(context.getAssembly().getActions())
				.setText(context.getAssembly().getTitle());
		bar.addListener(SWT.Selection, l -> {
			// if ("������Ŀ���ļ���".equals(((Action) l.data).getName())) {
			// if (createFolder(null)) {
			// folderPane.setViewerInput(getInput());
			// }
			// } else if ("��ѯ".equals(((Action) l.data).getName())) {
			// filePane.openQueryEditor();
			// }
		});
		return bar;
	}

}
