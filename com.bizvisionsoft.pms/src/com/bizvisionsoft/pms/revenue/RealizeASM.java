package com.bizvisionsoft.pms.revenue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
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
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;

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
import com.bizvisionsoft.service.RevenueService;
import com.bizvisionsoft.service.model.AccountIncome;
import com.bizvisionsoft.service.model.IRevenueScope;
import com.bizvisionsoft.service.model.RevenueRealizeItem;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.service.tools.Formatter;
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

	private IRevenueScope scope;

	private RevenueService service;

	private List<Document> data;

	@Init
	public void init() {
		service = Services.get(RevenueService.class);
		setContext(context);
		setConfig(context.getAssembly());
		setBruiService(br);
		scope = context.getRootInput(IRevenueScope.class, false);
		data = service.groupRevenueRealizeAmountByPeriod(scope.getScope_id(), br.getDomain());
		super.init();
	}

	@CreateUI
	public void createUI(Composite parent) {
		super.createUI(parent);
		Layer.message("提示：点击期间列头可删除期间所有数据");
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
		c.setMarkupEnabled(true);
		c.setWidth(240);
		c.setAlignment(SWT.LEFT);
		c.setMoveable(false);
		c.setResizeable(true);
		createColumn(grid, c);

		c = new Column();
		c.setName("id");
		c.setText("编号");
		c.setMarkupEnabled(true);
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
				double value = getSummary(element);

				if (value == 0) {
					text = "";
				} else {
					text = Formatter.getMoneyFormatString(value);
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
		service.getRevenueRealizePeriod(scope.getScope_id(), br.getDomain()).forEach(id -> {
			Calendar cal = Calendar.getInstance();
			try {
				cal.setTime(new SimpleDateFormat("yyyyMM").parse(id));
				appendAmountColumn(cal);
			} catch (ParseException e) {
			}
		});
	}

	/**
	 * 追加一列
	 */
	public void appendAmountColumn(Calendar input) {
		Calendar cal = getStartOfMonth(input);
		String title = cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1);
		int columnPosition = getColumnPosition(cal);
		if (columnPosition == -1) {
			Layer.message("期间" + title + "已存在", Layer.ICON_ERROR);
			return;
		}
		Grid grid = viewer.getGrid();
		Column c = new Column();
		final String index = getIndex(cal);
		c.setName(index);
		c.setText("<div style='cursor:pointer;'>" + title + "</div>");
		c.setWidth(88);
		c.setMarkupEnabled(true);
		c.setAlignment(SWT.RIGHT);
		c.setMoveable(false);
		c.setResizeable(true);
		GridViewerColumn vcol = createColumn(grid, c, columnPosition);
		vcol.getColumn().setData("date", cal);
		vcol.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public void update(ViewerCell cell) {
				Object account = cell.getElement();

				String text = "";
				double value = getAmount(account, index);
				if (value != 0)
					text = Formatter.getMoneyFormatString(value);

				cell.setText(text);
				if (isAmountEditable(account))
					cell.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
			}

			@Override
			public boolean isLabelProperty(Object element, String property) {
				return ("" + index).equals(property);
			}

		});
		vcol.getColumn().addListener(SWT.Selection, e -> delete(e.widget));
		vcol.setEditingSupport(supportEdit(vcol));
	}

	private String getIndex(Calendar cal) {
		return cal.get(Calendar.YEAR) + String.format("%02d", cal.get(Calendar.MONTH) + 1);
	}

	private Calendar getStartOfMonth(Calendar cal) {
		Calendar result = Calendar.getInstance();
		result.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), 1, 0, 0, 0);
		result.set(Calendar.MILLISECOND, 0);
		return result;
	}

	/**
	 * 获得该日期在表格正确的列位置
	 * 
	 * @param cal
	 * @return
	 */
	private int getColumnPosition(Calendar cal) {
		GridColumn[] cols = viewer.getGrid().getColumns();
		int index = 3;
		while (index < cols.length) {
			Calendar cCal = (Calendar) cols[index].getData("date");
			if (cCal == null) {
				return index;
			}
			if (cCal.equals(cal)) {// 存在
				return -1;
			} else if (cCal.after(cal)) {
				break;
			}
			index++;
		}
		return index;
	}

	private boolean isAmountEditable(Object account) {
		return account instanceof AccountIncome && !((AccountIncome) account).hasChildren()
				&& Check.isNotAssigned(((AccountIncome) account).getFormula());
	}

	/**
	 * @param vcol
	 * @return
	 */
	protected EditingSupport supportEdit(GridViewerColumn vcol) {
		if (!hasPermission()) {
			return null;
		}

		Calendar cal = (Calendar) vcol.getColumn().getData("date");
		final String index = getIndex(cal);
		return new EditingSupport(viewer) {

			@Override
			protected void setValue(Object element, Object value) {
				try {
					update((AccountIncome) element, index, Formatter.getDouble((String) value));
				} catch (Exception e) {
					Layer.message(e.getMessage(), Layer.ICON_ERROR);
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

	private void delete(Widget col) {
		Calendar cal = (Calendar) col.getData("date");
		String index = getIndex(cal);
		if (br.confirm("删除", "请确认删除期间" + index)) {
			service.deleteRevenueRealize(scope.getScope_id(), index, br.getDomain());
			col.dispose();

			// 清除缓存
			data.stream().forEach(d -> ((Document) d.get("values")).remove(index));

			// 刷新表格
			ArrayList<Object> dirty = new ArrayList<>();
			viewer.getGrid().handleItems(itm -> dirty.add(itm.getData()));
			viewer.update(dirty.toArray(), new String[] { "total" });
			Layer.message("已删除期间" + index);
		}
	}

	public void update(AccountIncome account, String index, double amount) {
		// 更新数据库
		String subject = account.getId();

		RevenueRealizeItem item = new RevenueRealizeItem()//
				.setScope_id(scope.getScope_id())//
				.setId(index)//
				.setAmount(amount)//
				.setSubject(subject);
		service.updateRevenueRealizeItem(item, br.getDomain());

		// 更新缓存
		Document row = data.stream().filter(d -> d.get("_id").equals(subject)).findFirst().orElse(null);
		if (row == null) {
			row = new Document("_id", subject).append("values", new Document());
			data.add(row);
		}
		((Document) row.get("values")).put(index, amount);

		// 刷新表格
		Calendar cal = Calendar.getInstance();
		try {
			cal.setTime(new SimpleDateFormat("yyyyMM").parse(index));
		} catch (ParseException e) {
		}
		if (getColumnPosition(cal) != -1) {
			appendAmountColumn(cal);
		}

		ArrayList<Object> dirty = new ArrayList<>();
		dirty.add(account);
		GridItem treeItem = (GridItem) viewer.testFindItem(account);
		GridItem parentItem = treeItem.getParentItem();
		while (parentItem != null) {
			dirty.add(parentItem.getData());
			parentItem = parentItem.getParentItem();
		}
		viewer.update(dirty.toArray(), new String[] { "total", index });
	}

	private double readAmount(String subject, String index) {
		return service.getRevenueRealizeAmount(scope.getScope_id(), subject, index, br.getDomain());
	}

	private double getAmount(Object account, String index) {
		if (account instanceof IRevenueScope) {
			return getRowSummaryAccount(((IRevenueScope) account).getRootAccountIncome(), index);
		} else if (account instanceof AccountIncome) {
			AccountIncome ai = (AccountIncome) account;
			if (ai.hasChildren()) {
				return getRowSummaryAccount(ai.getSubAccountItems(), index);
			} else {
				return data.stream().filter(d -> ai.getId().equals(d.get("_id"))).findFirst()
						.map(d -> ((Document) d.get("values")).getDouble(index)).map(v -> v.doubleValue()).orElse(0d);
			}
		} else {
			return 0d;
		}
	}

	private double getSummary(Object account) {
		List<AccountIncome> children = null;
		if (account instanceof IRevenueScope) {
			children = ((IRevenueScope) account).getRootAccountIncome();
		} else if (account instanceof AccountIncome) {
			AccountIncome ai = (AccountIncome) account;
			Document doc = data.stream().filter(d -> ai.getId().equals(d.get("_id"))).findFirst().orElse(null);
			if (doc == null) {
				if (ai.hasChildren()) {
					children = ai.getSubAccountItems();
				}
			} else {
				Document values = (Document) doc.get("values");
				double v = 0d;
				Iterator<Object> iter = values.values().iterator();
				while (iter.hasNext()) {
					v += (Double) iter.next();
				}
				return v;
			}
		}
		if (children != null) {
			double v = 0d;
			for (int i = 0; i < children.size(); i++) {
				v += getSummary(children.get(i));
			}
			return v;
		}
		return 0;
	}

	private double getRowSummaryAccount(List<AccountIncome> children, String index) {
		double result = 0d;
		if (!Check.isNotAssigned(children)) {
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
