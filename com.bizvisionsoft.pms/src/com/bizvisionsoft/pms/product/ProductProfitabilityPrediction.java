package com.bizvisionsoft.pms.product;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.nebula.jface.gridviewer.GridViewerColumn;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumnGroup;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.CreateUI;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruicommons.model.Column;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.assembly.StickerTitlebar;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.service.UserSession;
import com.bizvisionsoft.bruiengine.util.BruiColors;
import com.bizvisionsoft.bruiengine.util.BruiColors.BruiColor;
import com.bizvisionsoft.bruiengine.util.Util;
import com.bizvisionsoft.service.CommonService;
import com.bizvisionsoft.service.ProductService;
import com.bizvisionsoft.service.datatools.FilterAndUpdate;
import com.bizvisionsoft.service.model.Product;
import com.bizvisionsoft.service.model.Project;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;
import com.mongodb.Function;

public class ProductProfitabilityPrediction extends GridPart {

	static class ExtendLabel extends ColumnLabelProvider {

		private Function<Document, String> func;
		private Function<Document, Color> colorFunc;

		public static ColumnLabelProvider newInstance(Function<Document, String> func) {
			ExtendLabel p = new ExtendLabel();
			p.func = func;
			return p;
		}

		public static ColumnLabelProvider newInstance(Function<Document, String> func,
				Function<Document, Color> colorFunc) {
			ExtendLabel p = new ExtendLabel();
			p.func = func;
			p.colorFunc = colorFunc;
			return p;
		}

		@Override
		public String getText(Object element) {
			return func.apply((Document) element);
		}

		@Override
		public Color getBackground(Object element) {
			if (colorFunc != null) {
				return colorFunc.apply((Document) element);
			}
			return null;
		}
	}

	private static final String type = "SKU盈利预测";

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService bruiService;

	private Project project;

	@Init
	public void init() {
		setContext(context);
		setConfig(context.getAssembly());
		setBruiService(bruiService);

		project = context.getRootInput(Project.class, false);

		super.init();
	}

	@Override
	protected GridTreeViewer createGridViewer(Composite parent) {
		viewer = new GridTreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setAutoExpandLevel(GridTreeViewer.ALL_LEVELS);
		viewer.setUseHashlookup(false);

		Grid grid = viewer.getGrid();
		grid.setHeaderVisible(true);
		grid.setFooterVisible(false);
		grid.setLinesVisible(true);

		return viewer;
	}

	@CreateUI
	public void createUI(Composite parent) {
		parent.setLayout(new FormLayout());

		StickerTitlebar bar = createBar(parent);

		FormData fd = new FormData();
		bar.setLayoutData(fd);
		fd.left = new FormAttachment(0);
		fd.top = new FormAttachment(0);
		fd.right = new FormAttachment(100);
		fd.height = 48;

		Composite content = UserSession.bruiToolkit().newContentPanel(parent);
		fd = new FormData();
		content.setLayoutData(fd);
		fd.left = new FormAttachment(0, 8);
		fd.top = new FormAttachment(bar, 8);
		fd.right = new FormAttachment(100, -8);
		fd.bottom = new FormAttachment(100, -8);
		content.setLayout(new FormLayout());

		Control grid = createGridControl(content);
		fd = new FormData();
		grid.setLayoutData(fd);
		fd.left = new FormAttachment(0);
		fd.top = new FormAttachment();
		fd.right = new FormAttachment(100);
		fd.bottom = new FormAttachment(100);

		setViewerInput();

	}

	@Override
	public void setViewerInput() {
		List<Document> result = Services.get(CommonService.class)
				.listStructuredData(new BasicDBObject("host_id", project.get_id()).append("type", type));
		if (result.isEmpty()) {
			result = initStructuredData();
		}
		setViewerInput(result);
	}

	private List<Document> initStructuredData() {
		List<Document> result = Arrays.asList(createRowData("是否OEM", "01"), //
				createRowData("批量", "02"), //
				createRowData("立项销售量", "03"), //
				createRowData("立项4折价", "04"), //
				createRowData("立项成本单价", "05"), //
				createRowData("总收入（不含税不含返利）", "06"), //
				createRowData("立项4折价净收入", "07"), //
				createRowData("总成本（不含税）", "08"), //
				createRowData("结算立项成本金额", "09"), //
				createRowData("    玩具毛利率（不含影视片 ）", "10"), //
				createRowData("立项版权成本", "11"), //
				createRowData("营业税金及附加", "12"), //
				createRowData("固定投入", "13"), //
				createRowData("立项模具费", "14"), //
				createRowData("手板费", "15"), //
				createRowData("减去固定投入的毛利", "16"), //
				createRowData("减去固定投入的毛利率%", "17")//
		);

		Services.get(CommonService.class).insertStructuredData(result);
		return result;
	}

	private StickerTitlebar createBar(Composite parent) {
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

		List<Action> rightActions;
		rightActions = Arrays.asList(a, b);

		StickerTitlebar bar = new StickerTitlebar(parent, null, rightActions)
				.setActions(context.getAssembly().getActions()).setText(context.getAssembly().getTitle());
		bar.addListener(SWT.Selection, l -> {
			if ("创建项目根文件夹".equals(((Action) l.data).getName())) {
			} else if ("查询".equals(((Action) l.data).getName())) {
			}
		});
		return bar;
	}

	@Override
	protected void createColumns(Grid grid) {
		createColumn(grid, "项目", 240, SWT.LEFT);
		createColumn(grid, "序号", 60, SWT.CENTER);
		createColumn(grid, "立项总体", 80, SWT.RIGHT);
		createColumn(grid, "项目比例", 80, SWT.RIGHT);
		// 创建产品列
		createProductColumns(grid);

		createColumn(grid, "SKU合计", 160, SWT.RIGHT);
		createColumn(grid, "项目进度", 160, SWT.RIGHT);

	}
	
	private void createColumn(Grid grid, final String name, int width, int style) {
		Column c = new Column();
		c.setName(name);
		c.setText(name);
		c.setWidth(width);
		c.setAlignment(style);
		c.setMoveable(false);
		c.setResizeable(true);
		createColumn(grid, c).setLabelProvider(
				ExtendLabel.newInstance(d -> getCellText(d, name), d -> getColor(d.getString("index"), name)));
	}

	private String getCellText(Document d, String col) {
		String index = d.getString("index");
		if ("序号".equals(col)) {
			return index;
		} else if ("项目".equals(col)) {
			return d.getString("name");
		}
		
		if(Arrays.asList("04","05","03","11","14","15").contains(index) && isProductCol(col)) {
			return Util.getFormatText(d.get(col), "0.0", null);
		}

		return "";
	}

	private Color getColor(String row, String col) {
		return isComputeCell(row, col) ? BruiColors.getColor(BruiColor.Grey_50) : null;
	}

	private boolean isComputeCell(String row, String col) {
		return Arrays.asList("06", "07", "08", "09", "10", "12", "13", "16", "17").contains(row)
				|| !isProductCol(col);
	}
	
	private boolean isProductCol(String col) {
		return !Arrays.asList("项目", "序号", "立项总体", "项目比例", "SKU合计", "项目进度").contains(col);
	}

	private void createProductColumns(Grid grid) {
		final GridColumnGroup grp = new GridColumnGroup(grid, SWT.LEFT);
		grp.setData("name", "产品SKU");
		grp.setText("产品SKU");
		grp.setExpanded(true);
		Services.get(ProductService.class).listProjectProduct(project.get_id())
				.forEach(p -> createProductColumn(grp, p));
	}

	private void createProductColumn(GridColumnGroup grp, Product p) {
		Column c = new Column();
		final String col = p.getId();
		c.setName(col);
		c.setText(col);
		c.setWidth(120);
		c.setAlignment(SWT.RIGHT);
		c.setMoveable(false);
		c.setResizeable(true);
		c.setDetail(true);
		c.setSummary(true);
		GridViewerColumn vc = createColumn(grp, c);
		vc.setLabelProvider(
				ExtendLabel.newInstance(d -> getCellText(d, col), d -> getColor(d.getString("index"), col)));
		vc.setEditingSupport(new EditingSupport(viewer) {

			@Override
			protected void setValue(Object element, Object value) {
				try {
					update((Document) element, col, value);
					viewer.refresh();
				} catch (Exception e) {
					Layer.message(e.getMessage(), Layer.ICON_CANCEL);
				}
			}

			@Override
			protected Object getValue(Object element) {
				return getCellText((Document) element, col);
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				return new TextCellEditor(viewer.getGrid());
			}

			@Override
			protected boolean canEdit(Object element) {
				return !isComputeCell(((Document) element).getString("index"), col);
			}
		});

	}

	private void update(Document row, String col, Object input) throws Exception {
		double v = getDoubleValue(input);
		double oldValue = Optional.ofNullable((Double) row.get(col)).orElse(0d);
		if (v == oldValue)
			return;
		row.put(col, v);
		BasicDBObject fu = new FilterAndUpdate().filter(new BasicDBObject("_id", row.get("_id")))
				.set(new BasicDBObject(col, v)).bson();
		Services.get(CommonService.class).updateStructuredData(fu);

	}

	private Document createRowData(String name, String index) {
		return new Document("name", name).append("index", index).append("_id", new ObjectId())
				.append("host_id", project.get_id()).append("type", type);
	}
	
	private double getDoubleValue(Object input) throws Exception {
		double inputAmount;
		try {
			if ("".equals(input)) {
				inputAmount = 0;
			} else {
				inputAmount = Double.parseDouble(input.toString());
			}
		} catch (Exception e) {
			throw new Exception("请输入数字");
		}
		return inputAmount;
	}

}
