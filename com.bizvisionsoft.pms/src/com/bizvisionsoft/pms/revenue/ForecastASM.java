package com.bizvisionsoft.pms.revenue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.nebula.jface.gridviewer.GridViewerColumn;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.CreateUI;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruicommons.model.Assembly;
import com.bizvisionsoft.bruicommons.model.Column;
import com.bizvisionsoft.bruiengine.Brui;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.service.PermissionUtil;
import com.bizvisionsoft.bruiengine.service.UserSession;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.bruiengine.util.BruiColors;
import com.bizvisionsoft.bruiengine.util.BruiColors.BruiColor;
import com.bizvisionsoft.bruiengine.util.EngUtil;
import com.bizvisionsoft.service.RevenueService;
import com.bizvisionsoft.service.model.AccountIncome;
import com.bizvisionsoft.service.model.IRevenueScope;
import com.bizvisionsoft.service.model.RevenueForecastItem;
import com.bizvisionsoft.service.tools.Util;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

/**
 * 项目收益预测
 * 
 * @author hua
 *
 */
public class ForecastASM extends GridPart {

	private static final String DefaultType = "年";

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService br;

	private IRevenueScope scope;

	private String type;

	private int index;

	private RevenueService service;

	private Map<String, Map<Integer, Double>> data = new HashMap<>();

	private List<AccountIncome> calColumns;

	@Init
	public void init() {
		service = Services.get(RevenueService.class);
		setContext(context);
		setConfig(context.getAssembly());
		setBruiService(br);
		scope = context.getRootInput(IRevenueScope.class, false);
		// 记录计算列
		calColumns = AccountIncome.collect(scope.getRootAccountIncome(),
				item -> !Util.isEmptyOrNull(item.getFormula()));

		type = scope.getRevenueForecastType();
		if (type.isEmpty()) {// 第一次编辑收益预测
			if (br.confirm("收益预测方式", "收益的预测方式默认设定为按年预测，更改设定？")) {
				selectType();
			}
			if (type.isEmpty()) {
				type = DefaultType;
			}
		}
		loadData();
		super.init();
	}

	private void loadData() {
		service.listRevenueForecast(scope.getScope_id()).forEach(rfi -> {
			Map<Integer, Double> row = data.get(rfi.getSubject());
			if (row == null) {
				row = new HashMap<Integer, Double>();
				data.put(rfi.getSubject(), row);
			}
			row.put(rfi.getIndex(), rfi.getAmount());
			data.put(rfi.getSubject(), row);
		});

	}

	@CreateUI
	public void createUI(Composite parent) {
		super.createUI(parent);
		Layer.message("提示：点击期间列头可删除期间所有数据");
	}

	public void selectType() {
		Editor.open("选择收益预测方式", context, new BasicDBObject(), (r, d) -> {
			String type = r.getString("type");
			setType(type);
		});
	}

	private void setType(String type) {
		if (!this.type.isEmpty() && !this.type.equals(type) && br.confirm("收益预测方式", "设定新的收益预测方式，将清除目前的预测数据，请确认。")) {
			service.clearRevenueForecast(scope.getScope_id());
			this.data.clear();
			Layer.message("收益预测方式已设定为：" + type);
		}
		this.type = type;
	}

	public void reset() {
		// 删除不要的数据列
		GridColumn[] cols = viewer.getGrid().getColumns();
		for (int i = 0; i < index; i++) {
			cols[i + 3].dispose();
		}
		index = 0;
		appendAmountColumn();
		setViewerInput();
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
		// grid.setHideIndentionImage(true);
		UserSession.bruiToolkit().enableMarkup(grid);
		grid.setData(RWT.FIXED_COLUMNS, 3);
		grid.setBackground(BruiColors.getColor(BruiColor.Grey_50));

		return viewer;
	}

	@Override
	public void setViewerInput() {
		super.setViewerInput(Arrays.asList(scope));
	}

	@Override
	protected void createColumns(Grid grid) {

		/////////////////////////////////////////////////////////////////////////////////////
		// 创建列
		Column c = new Column();
		c.setName("name");
		c.setText("名称");
		c.setWidth(240);
		c.setAlignment(SWT.LEFT);
		c.setMoveable(false);
		c.setResizeable(true);
		createColumn(grid, c);

		c = new Column();
		c.setName("id");
		c.setText("编号");
		c.setWidth(64);
		c.setAlignment(SWT.CENTER);
		c.setMoveable(false);
		c.setResizeable(true);
		createColumn(grid, c);

		c = new Column();
		c.setName("total");
		c.setText("合计");
		c.setWidth(88);
		c.setMarkupEnabled(true);
		c.setAlignment(SWT.RIGHT);
		c.setMoveable(false);
		c.setResizeable(true);
		GridViewerColumn vcol = createColumn(grid, c);
		vcol.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public void update(ViewerCell cell) {
				Object element = cell.getElement();

				String text;
				double value = 0;
				for (int i = 0; i < index; i++) {
					value += getAmount(element, i);
				}
				if (value == 0) {
					text = "";
				} else {
					text = EngUtil.getGenericMoneyFormatText(value);
				}

				cell.setText(text);
				cell.setBackground(BruiColors.getColor(BruiColor.Grey_50));
			}

			@Override
			public boolean isLabelProperty(Object element, String property) {
				return "total".equals(property);
			}

		});

		createAmountColumns();

	}

	private void createAmountColumns() {
		int count = service.getForwardRevenueForecastIndex(scope.getScope_id()) + 1;
		while (index < count) {
			appendAmountColumn();
		}
	}

	/**
	 * 追加一列
	 */
	public void appendAmountColumn() {
		Grid grid = viewer.getGrid();
		Column c = new Column();
		c.setName("" + index);
		c.setText("第" + (index + 1) + type);
		c.setWidth(88);
		c.setMarkupEnabled(true);
		c.setAlignment(SWT.RIGHT);
		c.setMoveable(false);
		c.setResizeable(true);
		GridViewerColumn vcol = createColumn(grid, c, 2 + this.index + 1);
		final int idx = this.index;
		vcol.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public void update(ViewerCell cell) {
				Object account = cell.getElement();

				String text = "";
				double value = getAmount(account, idx);
				if (value != 0)
					text = EngUtil.getGenericMoneyFormatText(value);

				cell.setText(text);
				if (isAmountEditable(account))
					cell.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
			}

			@Override
			public boolean isLabelProperty(Object element, String property) {
				return ("" + idx).equals(property);
			}

		});
		vcol.setEditingSupport(supportEdit(vcol));
		vcol.getColumn().addListener(SWT.Selection, e -> delete((GridColumn) e.widget));

		index++;
	}

	private void delete(GridColumn column) {
		final int index = Integer.parseInt((String) column.getData("name"));
		String text = column.getText();
		if (br.confirm("清除", "请确认清除期间数据：" + text)) {
			service.deleteRevenueForecast(scope.getScope_id(), index);

			// 清除缓存
			Iterator<String> iter = data.keySet().iterator();
			while (iter.hasNext()) {
				String key = iter.next();
				Map<Integer, Double> map = data.get(key);
				map.remove(index);
			}
			// 刷新表格
			ArrayList<Object> dirty = new ArrayList<>();
			viewer.getGrid().handleItems(itm -> dirty.add(itm.getData()));
			viewer.update(dirty.toArray(), new String[] { "total", "" + index });
			Layer.message("已删除期间" + text);
		}

	}

	private boolean isAmountEditable(Object account) {
		return account instanceof AccountIncome && !((AccountIncome) account).hasChildren()
				&& Util.isEmptyOrNull(((AccountIncome) account).getFormula());
	}

	/**
	 * @param vcol
	 * @return
	 */
	protected EditingSupport supportEdit(GridViewerColumn vcol) {
		if (!hasPermission()) {
			return null;
		}

		final int index = Integer.parseInt((String) vcol.getColumn().getData("name"));
		return new EditingSupport(viewer) {

			@Override
			protected void setValue(Object element, Object value) {
				try {
					update((AccountIncome) element, index, EngUtil.getDoubleInput((String) value));
				} catch (Exception e) {
					Layer.message(e.getMessage(), Layer.ICON_CANCEL);
				}
			}

			@Override
			protected Object getValue(Object element) {
				double value = readAmount(((AccountIncome) element).getId(), index);
				return "" + value;
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				return new TextCellEditor(viewer.getGrid());
			}

			@Override
			protected boolean canEdit(Object element) {
				return isAmountEditable(element);
			}
		};
	}

	protected void update(AccountIncome account, int index, double amount) {
		// 更新数据库
		RevenueForecastItem item = new RevenueForecastItem()//
				.setScope_id(scope.getScope_id())//
				.setIndex(index)//
				.setAmount(amount)//
				.setType(type)//
				.setSubject(account.getId());
		service.updateRevenueForecastItem(item);

		// 更新缓存
		Map<Integer, Double> row = data.get(account.getId());
		if (row == null) {
			row = new HashMap<Integer, Double>();
			data.put(account.getId(), row);
		}
		row.put(index, amount);
		// 刷新表格
		while (this.index <= index) {
			appendAmountColumn();
		}

		ArrayList<Object> dirty = new ArrayList<>();
		dirty.addAll(calColumns);
		
		dirty.add(account);
		GridItem treeItem = (GridItem) viewer.testFindItem(account);
		GridItem parentItem = treeItem.getParentItem();
		while (parentItem != null) {
			dirty.add(parentItem.getData());
			parentItem = parentItem.getParentItem();
		}
		List<String> properties = new ArrayList<>();
		properties.add("total");
		for (int i = 0; i <= index; i++) {
			properties.add("" + i);
		}
		viewer.update(dirty.toArray(), properties.toArray(new String[0]));
	}

	private double readAmount(String subject, int index) {
		return service.getRevenueForecastAmount(scope.getScope_id(), subject, type, index);
	}

	private double getAmount(Object account, int index) {
		List<AccountIncome> children = null;
		if (account instanceof IRevenueScope) {
			children = ((IRevenueScope) account).getRootAccountIncome();
			return getRowSummaryAccount(children, index);
		} else if (account instanceof AccountIncome) {
			AccountIncome ai = (AccountIncome) account;
			if (ai.hasChildren()) {
				children = ai.getSubAccountItems();
				return getRowSummaryAccount(children, index);
			} else if (!Util.isEmptyOrNull(ai.getFormula())) {
				return calculate(ai, index);
			} else {
				return Optional.ofNullable(data.get(ai.getId())).map(row -> row.get(index)).map(d -> d.doubleValue())
						.orElse(0d);
			}
		}
		return 0d;
	}

	private double calculate(AccountIncome ai, int index) {
		return Util.calculate(ai.getFormula(), subject -> {
			AccountIncome account = AccountIncome.search(scope.getRootAccountIncome(), i -> i.getId().equals(subject));
			if (account != null) {
				return getAmount(account, index);
			}
			return null;
		});
	}

	private double getRowSummaryAccount(List<AccountIncome> children, int index) {
		double result = 0d;
		if (!EngUtil.isEmptyOrNull(children)) {
			for (int i = 0; i < children.size(); i++) {
				result += getAmount(children.get(i), index);
			}
		}
		return result;
	}

	private boolean hasPermission() {
		// 检查action的权限,映射到action进行检查
		IBruiContext context = getContext();
		Assembly assembly = context.getAssembly();
		List<Action> rowActions = assembly.getRowActions();
		if (rowActions == null || rowActions.isEmpty()) {
			return false;
		}
		return rowActions.stream().filter(a -> a.getName().equals(getEditbindingAction())).findFirst()
				.map(act -> PermissionUtil.checkAction(Brui.sessionManager.getUser(), act, context)).orElse(false);
	}

	protected String getEditbindingAction() {
		return "编辑收益预测";
	}

}
