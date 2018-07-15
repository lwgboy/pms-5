package com.bizvisionsoft.pms.product;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
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

	private static final String L = "项目进度";

	private static final String D = "项目比例";

	private static final String K = "SKU合计";

	private static final String B = "序号";

	private static final String A = "项目";

	private static final String C = "立项总体";

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

	private static final String[] option = new String[] { "3K", "5K", "10K", "20K" };

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService bruiService;

	private Project project;

	private List<Document> input;

	private List<String> pCol = new ArrayList<>();

	private Map<String, Map<String, Object>> matrix = new HashMap<>();

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
		input = Services.get(CommonService.class)
				.listStructuredData(new BasicDBObject("host_id", project.get_id()).append("type", type));
		if (input.isEmpty()) {
			input = initStructuredData();
		}
		setViewerInput(input);
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

		StickerTitlebar bar = new StickerTitlebar(parent, null, null)
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
		createColumn(grid, A, 240, SWT.LEFT);
		createColumn(grid, B, 60, SWT.CENTER);
		createColumn(grid, C, 80, SWT.RIGHT);
		createColumn(grid, D, 80, SWT.RIGHT);
		// 创建产品列
		createProductColumns(grid);

		createColumn(grid, K, 100, SWT.RIGHT);
		createColumn(grid, L, 100, SWT.RIGHT);

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
		Object value = getCellValue(d, col);
		if (value instanceof String) {
			return (String) value;
		}
		String format = getFormat(index, col);
		if (format == null) {
			return "";
		}
		return Util.getFormatText(value, format, null);
	}

	private String getFormat(String row, String col) {
		if (C.equals(col) || K.equals(col)) {
			if (Arrays.asList("01", "02", "03", "04", "05").contains(row)) {
				return null;
			} else if ("10".equals(row) || "17".equals(row)) {
				return "0.0%";
			} else {
				return "0";
			}
		}

		if (D.equals(col)) {
			if (Arrays.asList("01", "02", "03", "04", "05", "10", "17").contains(row)) {
				return null;
			} else {
				return "0.0%";
			}
		}

		if (isProductCol(col)) {
			if ("10".equals(row) || "17".equals(row)) {
				return "0.0%";
			} else {
				return "0";
			}
		}

		if (L.equals(col))
			return "0.0%";

		return null;
	}

	private Object getCellValue(Document d, String col) {
		String index = d.getString("index");
		try {
			return readMatrixValue(index, col);
		} catch (Exception e) {
		}
		Object value = readCellValue(d, col);
		writeMatrixValue(index, col, value);
		return value;
	}

	private Object readCellValue(Document d, String col) {
		String index = d.getString("index");

		if (A.equals(col))
			return d.getString("name");

		if (B.equals(col))
			return index;

		if (Arrays.asList("04", "05", "03", "11", "14", "15").contains(index) && isProductCol(col))
			return d.get(col);

		if ("01".equals(index) && !isComputeCell("01", col))
			return Boolean.TRUE.equals(d.get(col)) ? "是" : "否";

		if ("02".equals(index) && !isComputeCell("01", col))
			return d.getString(col);

		// 计算值
		if ("06".equals(index)) {
			Document row07 = getRow("07");
			if (C.equals(col) || K.equals(col) || isComputeCell(index, col))
				return getCellValue(row07, col);

			if (L.equals(col))
				return divideKC(d);
		}

		if ("07".equals(index)) {
			if (C.equals(col))
				return search("1.1", "scy1", "scy2", "scy3");

			if (D.equals(col))
				return divide(index, C, "06", C);

			if (isComputeCell(index, col))
				return getDoubleValue("03", col) * getDoubleValue("04", col) / (1.17 * 0.95);

			if (K.equals(col))
				return summaryProduct(d);

			if (L.equals(col))
				return divideKC(d);
		}

		if ("08".equals(index)) {
			if (C.equals(col))
				return getDoubleValue("09", C) + getDoubleValue("11", C);
			if (D.equals(col))
				return divide(index, C, "06", C);
			if (isComputeCell("08", col) || K.equals(col))
				return getDoubleValue("09", col) + getDoubleValue("11", col);
			if (L.equals(col))
				return divideKC(d);
		}

		if ("09".equals(index)) {
			if (C.equals(col))
				return search("2.1", "scy1", "scy2", "scy3");
			if (D.equals(col))
				return divide(index, C, "06", C);
			if (isComputeCell(index, col))
				return getDoubleValue("03", col) * getDoubleValue("05", col);
			if (K.equals(col))
				return summaryProduct(d);
			if (L.equals(col))
				return divideKC(d);
		}

		if ("10".equals(index)) {
			if (C.equals(col))
				return 1 - getDoubleValue("09", C) / getDoubleValue("07", C);
			if (isComputeCell("10", col) || K.equals(col))
				return divide("09", col, "07", col);
			if (L.equals(col))
				return divideKC(d);
		}

		if ("11".equals(index)) {
			if (C.equals(col))
				return search("2.4", "scy1", "scy2", "scy3");
			if (D.equals(col))
				return divide(index, C, "07", C);
			if (K.equals(col))
				return summaryProduct(d);
			if (L.equals(col))
				return divideKC(d);
		}

		if ("12".equals(index)) {
			if (C.equals(col))
				return (search("1.1", "scy1", "scy2", "scy3") + search("1.2", "scy1", "scy2", "scy3")
						+ search("1.3", "scy1", "scy2", "scy3") - search("2.1", "scy1", "scy2", "scy3")
						- search("2.2", "scy1", "scy2", "scy3") - search("2.3", "scy1", "scy2", "scy3")
						- search("2.4", "scy1", "scy2", "scy3")) * 0.17 * 0.12;

			if (D.equals(col))
				return divide(index, C, "06", C);
			if (isComputeCell("12", col))
				return (getDoubleValue("07", col) - getDoubleValue("09", col)) * 0.17 * 0.12;
			if (K.equals(col))
				return summaryProduct(d);
			if (L.equals(col))
				return divideKC(d);
		}

		if ("13".equals(index)) {
			if (C.equals(col) || K.equals(col) || isComputeCell(index, col))
				return getDoubleValue("14", C) + getDoubleValue("15", C);
			if (D.equals(col))
				return divide(index, C, "06", C);
			if (L.equals(col))
				return divideKC(d);
		}

		if ("14".equals(index)) {
			if (C.equals(col))
				return search("3.3", "scy1", "scy2", "scy3");
			if (D.equals(col))
				return divide(index, C, "06", C);
			if (K.equals(col))
				return summaryProduct(d);
			if (L.equals(col))
				return divideKC(d);
		}

		if ("15".equals(index)) {
			if (C.equals(col))
				return search("3.4", "scy1", "scy2", "scy3");
			if (D.equals(col))
				return divide(index, C, "06", C);
			if (K.equals(col))
				return summaryProduct(d);
			if (L.equals(col))
				return divideKC(d);
		}

		if ("16".equals(index)) {
			if (C.equals(col) || K.equals(col) || isComputeCell(index, col))
				return getDoubleValue("06", col) - getDoubleValue("08", col) - getDoubleValue("12", col)
						- getDoubleValue("13", col);

			if (D.equals(col))
				return divide(index, C, "06", C);
			if (L.equals(col))
				return divideKC(d);
		}

		if ("17".equals(index)) {
			if (C.equals(col) || K.equals(col) || isComputeCell(index, col))
				return divide("16", col, "07", col);

			if (L.equals(col))
				return divideKC(d);
		}

		return null;
	}

	private Object readMatrixValue(String index, String col) throws Exception {
		Map<String, Object> _r = matrix.get(index);
		if (_r == null)
			throw new Exception();
		if (!_r.containsKey(col))
			throw new Exception();
		return _r.get(col);
	}

	private void writeMatrixValue(String index, String col, Object value) {
		Map<String, Object> _r = matrix.get(index);
		if (_r == null) {
			_r = new HashMap<>();
			matrix.put(index, _r);
		}
		_r.put(col, value);
	}

	private double divideKC(Document d) {
		double j10 = getDoubleValue(d, K);
		double b10 = getDoubleValue(d, C);
		if (b10 != 0)
			return j10 / b10;
		return 0d;
	}

	private double divide(String row1, String col1, String row2, String col2) {
		double j10 = getDoubleValue(row1, col1);
		double b10 = getDoubleValue(row2, col2);
		if (b10 != 0)
			return j10 / b10;
		return 0d;
	}

	private Object summaryProduct(Document d) {
		double sum = 0;
		for (int i = 0; i < pCol.size(); i++) {
			sum += getDoubleValue(d, pCol.get(i));
		}
		return sum;
	}

	private double search(String index, String... cols) {
		List<Document> refRows = Services.get(CommonService.class).listStructuredData(
				new BasicDBObject("host_id", project.get_id()).append("type", "项目盈利预测分析").append("index", index));
		if (!refRows.isEmpty()) {
			Document ref = refRows.get(0);
			double sum = 0d;
			for (int i = 0; i < cols.length; i++) {
				sum += Optional.ofNullable(ref.getDouble(cols[i])).orElse(0d);
			}
			return sum;
		} else {
			return 0d;
		}
	}

	private Document getRow(String index) {
		return input.stream().filter(d -> d.getString("index").equals(index)).findFirst().orElse(null);
	}

	private Color getColor(String row, String col) {
		return isComputeCell(row, col) ? BruiColors.getColor(BruiColor.Grey_50) : null;
	}

	private boolean isComputeCell(String row, String col) {
		return Arrays.asList("06", "07", "08", "09", "10", "12", "13", "16", "17").contains(row) || !isProductCol(col);
	}

	private boolean isProductCol(String col) {
		return !Arrays.asList(A, B, C, D, K, L).contains(col);
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
		pCol.add(col);
		c.setName(col);
		c.setText(col);
		c.setWidth(100);
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
				Document row = (Document) element;
				String index = row.getString("index");
				try {
					if ("01".equals(index)) {
						updateBooleanValue(row, col, value);
					} else if ("02".equals(index)) {
						updateOptionValue(row, col, value);
					} else {
						updateNumberValue(row, col, value);
					}
					viewer.refresh();
				} catch (Exception e) {
					Layer.message(e.getMessage(), Layer.ICON_CANCEL);
				}
			}

			@Override
			protected Object getValue(Object element) {
				Document row = (Document) element;
				String index = row.getString("index");
				Object value = row.get(col);
				if ("01".equals(index)) {
					return Boolean.TRUE.equals(value);
				} else if ("02".equals(index)) {
					return Arrays.asList(option).indexOf(value);
				}

				return getCellText((Document) element, col);
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				Grid parent = viewer.getGrid();
				Document row = (Document) element;
				String index = row.getString("index");
				if ("01".equals(index)) {// 是否OEM
					return new CheckboxCellEditor(parent);
				} else if ("02".equals(index)) {
					return new ComboBoxCellEditor(parent, option, SWT.READ_ONLY);
				}
				return new TextCellEditor(parent);
			}

			@Override
			protected boolean canEdit(Object element) {
				return !isComputeCell(((Document) element).getString("index"), col);
			}
		});

	}

	private void updateNumberValue(Document row, String col, Object input) throws Exception {
		double v = str_double(input);
		double oldValue = Optional.ofNullable((Double) row.get(col)).orElse(0d);
		if (v == oldValue)
			return;
		update(row, col, v);
	}

	private void updateOptionValue(Document row, String col, Object input) throws Exception {
		String v;
		if (((Integer) input).intValue() == -1) {
			v = null;
		}else {
			v = option[(Integer) input];
		}
		String oldValue = row.getString(col);
		if(v ==null && oldValue==null) {
			return;
		}
		
		if(v!=null&& v.equals(oldValue)) {
			return;
		}
		
		update(row, col, v);
	}

	private void updateBooleanValue(Document row, String col, Object input) throws Exception {
		boolean v = Boolean.TRUE.equals(input);
		boolean oldValue = Boolean.TRUE.equals(row.get(col));
		if (v == oldValue)
			return;
		update(row, col, v);
	}

	private void update(Document row, String col, Object v) {
		row.put(col, v);
		matrix.clear();
		BasicDBObject fu = new FilterAndUpdate().filter(new BasicDBObject("_id", row.get("_id")))
				.set(new BasicDBObject(col, v)).bson();
		Services.get(CommonService.class).updateStructuredData(fu);
	}

	private Document createRowData(String name, String index) {
		return new Document("name", name).append("index", index).append("_id", new ObjectId())
				.append("host_id", project.get_id()).append("type", type);
	}

	private double str_double(Object input) throws Exception {
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

	private double getDoubleValue(Object value) {
		if (value instanceof Number) {
			return ((Number) value).doubleValue();
		} else {
			return 0d;
		}
	}

	private double getDoubleValue(String row, String col) {
		return getDoubleValue(getRow(row), col);
	}

	private double getDoubleValue(Document d, String col) {
		return getDoubleValue(getCellValue(d, col));
	}

}
