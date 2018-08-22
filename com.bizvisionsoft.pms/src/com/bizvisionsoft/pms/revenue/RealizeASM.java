package com.bizvisionsoft.pms.revenue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.nebula.jface.gridviewer.GridViewerColumn;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

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
import com.bizvisionsoft.bruiengine.util.BruiColors;
import com.bizvisionsoft.bruiengine.util.BruiColors.BruiColor;
import com.bizvisionsoft.bruiengine.util.Util;
import com.bizvisionsoft.service.RevenueService;
import com.bizvisionsoft.service.model.AccountIncome;
import com.bizvisionsoft.service.model.IRevenueForecastScope;
import com.bizvisionsoft.service.model.RevenueRealizeItem;
import com.bizvisionsoft.serviceconsumer.Services;

/**
 * 项目收益预测
 * 
 * @author hua
 *
 */
public class RealizeASM extends GridPart {

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService br;

	private IRevenueForecastScope scope;

	private RevenueService service;

	private List<Document> data;

	private int colIndex;

	@Init
	public void init() {
		service = Services.get(RevenueService.class);
		setContext(context);
		setConfig(context.getAssembly());
		setBruiService(br);
		scope = context.getRootInput(IRevenueForecastScope.class, false);
		data = service.groupRevenueRealizeAmountByPeriod(scope.getScope_id());
		super.init();
	}

	@CreateUI
	public void createUI(Composite parent) {
		super.createUI(parent);
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
		grid.setHideIndentionImage(true);
		UserSession.bruiToolkit().enableMarkup(grid);
		grid.setData(RWT.FIXED_COLUMNS, 3);

		return viewer;
	}

	@Override
	public void setViewerInput() {
		super.setViewerInput(Arrays.asList(scope));
		updateBackground();
	}

	@Override
	protected GridViewerColumn createColumn(Object parent, Column c, int index) {
		GridViewerColumn col = super.createColumn(parent, c, index);
		colIndex++;
		return col;
	}

	@Override
	protected void createColumns(Grid grid) {

		/////////////////////////////////////////////////////////////////////////////////////
		// 创建列
		Column c = new Column();
		c.setName("name");
		c.setText("名称");
		c.setWidth(160);
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

				if (value == 0) {
					text = "";
				} else {
					text = Util.getGenericMoneyFormatText(value);
				}

				cell.setText(text);
				cell.setBackground(BruiColors.getColor(BruiColor.Grey_50));
			}

			@Override
			public boolean isLabelProperty(Object element, String property) {
				return "total".equals(property);
			}

		});

		try {
			createAmountColumns();
		} catch (ParseException e) {
			e.printStackTrace();
		}

	}

	private void createAmountColumns() throws ParseException {
		if (data.isEmpty()) {
			return;
		}
		List<String> period = service.getRevenueRealizePeriod(scope.getScope_id());
		if (period.isEmpty()) {
			return;
		}

		String _start = period.get(0);
		Calendar start = Calendar.getInstance();
		start.setTime(new SimpleDateFormat("yyyyMM").parse(_start));
		String _end = period.get(1);
		Calendar end = Calendar.getInstance();
		end.setTime(new SimpleDateFormat("yyyyMM").parse(_end));
		while (!start.after(end)) {
			appendAmountColumn(start.getTime());
			start.add(Calendar.MONTH, 1);
		}
	}

	/**
	 * 追加一列
	 */
	public void appendAmountColumn(Date date) {
		Grid grid = viewer.getGrid();
		Column c = new Column();
		final String index = new SimpleDateFormat("yyyyMM").format(date);
		c.setName(index);
		String title = new SimpleDateFormat("yyyy-MM").format(date);
		c.setText(title);
		c.setWidth(88);
		c.setMarkupEnabled(true);
		c.setAlignment(SWT.RIGHT);
		c.setMoveable(false);
		c.setResizeable(true);
		GridViewerColumn vcol = createColumn(grid, c, colIndex);
		vcol.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public void update(ViewerCell cell) {
				Object account = cell.getElement();

				String text = "";
				double value = getAmount(account, index);
				if (value != 0)
					text = Util.getGenericMoneyFormatText(value);

				cell.setText(text);
				if (!isAmountEditable(account))
					cell.setBackground(BruiColors.getColor(BruiColor.Grey_50));
			}

			@Override
			public boolean isLabelProperty(Object element, String property) {
				return ("" + index).equals(property);
			}

		});
		vcol.setEditingSupport(supportEdit(vcol));
	}

	private boolean isAmountEditable(Object account) {
		return account instanceof AccountIncome && ((AccountIncome) account).countSubAccountItems() == 0;
	}

	/**
	 * @param vcol
	 * @return
	 */
	protected EditingSupport supportEdit(GridViewerColumn vcol) {
		if (!hasPermission()) {
			return null;
		}

		final String index = (String) vcol.getColumn().getData("name");
		return new EditingSupport(viewer) {

			@Override
			protected void setValue(Object element, Object value) {
				try {
					update((AccountIncome) element, index, Util.getDoubleInput((String) value));
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

	protected void update(AccountIncome account, String index, double amount) {
		// 更新数据库
		RevenueRealizeItem item = new RevenueRealizeItem()//
				.setScope_id(scope.getScope_id())//
				.setId(index)//
				.setAmount(amount)//
				.setSubject(account.getId());
		service.updateRevenueRealizeItem(item);

		// // 更新缓存
		// Map<String, Double> row = data.get(account.getId());
		// if (row == null) {
		// row = new HashMap<String, Double>();
		// data.put(account.getId(), row);
		// }
		// row.put(index, amount);
		// // 刷新表格
		// while (this.index <= index) {
		// appendAmountColumn();
		// }
		//
		// ArrayList<Object> dirty = new ArrayList<>();
		// dirty.add(account);
		// GridItem treeItem = (GridItem) viewer.testFindItem(account);
		// GridItem parentItem = treeItem.getParentItem();
		// while (parentItem != null) {
		// dirty.add(parentItem.getData());
		// parentItem = parentItem.getParentItem();
		// }
		// List<String> properties = new ArrayList<>();
		// properties.add("total");
		// for (int i = 0; i < index; i++) {
		// properties.add("" + index);
		// }
		// viewer.update(dirty.toArray(), properties.toArray(new String[0]));
	}

	public void updateBackground() {
		GridItem[] items = viewer.getGrid().getItems();
		updateBackground(items);
	}

	private void updateBackground(GridItem[] items) {
		for (int i = 0; i < items.length; i++) {
			GridItem[] children = items[i].getItems();
			if (children.length > 0) {
				items[i].setBackground(BruiColors.getColor(BruiColor.Grey_50));
				updateBackground(children);
			}
		}
	}

	private double readAmount(String subject, String index) {
		return service.getRevenueRealizeAmount(scope.getScope_id(), subject, index);
	}

	private double getAmount(Object account, String index) {
		if (account instanceof IRevenueForecastScope) {
			return getRowSummaryAccount(((IRevenueForecastScope) account).getRootAccountIncome(), index);
		} else if (account instanceof AccountIncome) {
			AccountIncome ai = (AccountIncome) account;
			if (ai.countSubAccountItems() > 0) {
				return getRowSummaryAccount(ai.getSubAccountItems(), index);
			} else {
				return data.stream().filter(d -> ai.getId().equals(d.get("subject"))).findFirst()
						.map(d -> d.getDouble(index)).map(v -> v.doubleValue()).orElse(0d);
			}
		} else {
			return 0d;
		}
	}

	private double getRowSummaryAccount(List<AccountIncome> children, String index) {
		double result = 0d;
		if (!Util.isEmptyOrNull(children)) {
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
		return "编辑收益实现";
	}

}
