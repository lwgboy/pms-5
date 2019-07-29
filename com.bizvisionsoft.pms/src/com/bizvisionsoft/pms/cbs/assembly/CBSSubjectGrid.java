package com.bizvisionsoft.pms.cbs.assembly;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.nebula.jface.gridviewer.GridViewerColumn;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.CreateUI;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruicommons.model.Assembly;
import com.bizvisionsoft.bruiengine.Brui;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.service.PermissionUtil;
import com.bizvisionsoft.service.CBSService;
import com.bizvisionsoft.service.CommonService;
import com.bizvisionsoft.service.model.AccountItem;
import com.bizvisionsoft.service.model.CBSItem;
import com.bizvisionsoft.service.model.CBSSubject;
import com.bizvisionsoft.service.model.ICBSScope;
import com.bizvisionsoft.service.tools.Formatter;
import com.bizvisionsoft.serviceconsumer.Services;

/**
 * 测试速度，项目周期10年，18年1月到28年12月的数据
 * getMonthlyAmount、getTotalAmount、getYearlyAmountSummary全部存在时，访问速度为：7.5秒
 * getTotalAmount、getYearlyAmountSummary全部存在时，访问速度为：5.5
 * getMonthlyAmount、getYearlyAmountSummary全部存在时，访问速度为：7
 * getMonthlyAmount、getTotalAmount、全部存在时，访问速度为： 7 getMonthlyAmount存在时，访问速度为：5
 * getTotalAmount存在时，访问速度为： 4.5 getYearlyAmountSummary存在时，访问速度为：5.5
 * getMonthlyAmount、getTotalAmount、getYearlyAmountSummary全部不存在时，访问速度为：3.5
 * 
 * 更新速度慢是因为viewer.refresh()的原因
 * 
 * @author gdiyang
 *
 */
public abstract class CBSSubjectGrid extends CBSGrid {

	protected CBSItem cbsItem;

	protected ICBSScope scope;

	protected List<Document> accoutItems;

	private Map<String, Double> cbsItemAmount;
	private Map<String, Map<String, Double>> accountItemAmount;

	public void init() {
		IBruiService br = getBruiService();
		scope = (ICBSScope) getContext().getRootInput();
		Object parentInput = getContext().getParentContext().getInput();
		if (parentInput instanceof CBSItem)
			cbsItem = (CBSItem) parentInput;

		if (cbsItem == null) {
			ObjectId cbs_id = scope.getCBS_id();
			if (cbs_id != null) {
				cbsItem = Services.get(CBSService.class).get(cbs_id, br.getDomain());
			}
		}
		if (cbsItem != null) {
			List<CBSSubject> cbsSubjects = Services.get(CBSService.class).getCBSSubject(cbsItem.get_id(), br.getDomain());
			accoutItems = Services.get(CommonService.class).getAllAccoutItemsHasParentIds(br.getDomain());

			// 按照科目、月份计算合计值
			cbsItemAmount = new HashMap<String, Double>();
			accountItemAmount = new HashMap<String, Map<String, Double>>();
			cbsSubjects.forEach(c -> {
				String period = c.getId();
				String subjectNumber = c.getSubjectNumber();
				Double amount = getAmount(c);
				changeAmountData(period, subjectNumber, amount);
			});
		}
		super.init();
	}

	/**
	 * 修改金额数据 <br/>
	 * 修改CBSItem和AccountItem中对应的期间、费用科目的金额
	 * 
	 * @param period
	 *            期间
	 * @param subjectNumber
	 *            费用科目
	 * @param amount
	 *            金额（差额，与当前金额数据的差值）
	 */
	@SuppressWarnings("unchecked")
	private void changeAmountData(String period, String subjectNumber, Double amount) {
		// 计算CBSItem的金额
		calculationAmount(cbsItemAmount, period, amount);

		// 计算各叶子节点费用科目的金额
		Map<String, Double> monthAmount = accountItemAmount.get(subjectNumber);
		if (monthAmount == null) {
			monthAmount = new HashMap<String, Double>();
			accountItemAmount.put(subjectNumber, monthAmount);
		}
		calculationAmount(monthAmount, period, amount);

		// 计算各摘要节点费用科目的金额
		accoutItems.stream().filter(d -> {
			return d.getString("id").equals(subjectNumber);
		}).forEach(d -> {
			List<String> parentIds = (List<String>) d.get("parentIds");
			parentIds.forEach(parentId -> {
				Map<String, Double> parentMonthAmount = accountItemAmount.get(parentId);
				if (parentMonthAmount == null) {
					parentMonthAmount = new HashMap<String, Double>();
					accountItemAmount.put(parentId, parentMonthAmount);
				}
				calculationAmount(parentMonthAmount, period, amount);
			});
		});
	}

	/**
	 * 计算金额
	 * 
	 * @param amountMap
	 * @param period
	 * @param amount
	 */
	private void calculationAmount(Map<String, Double> amountMap, String period, Double amount) {
		// 月份合计
		Double d = amountMap.get(period);
		if (d == null && amount != null)
			d = amount;
		else if (d != null && amount != null)
			d += amount;
		amountMap.put(period, d);

		// 年合计
		String year = period.substring(0, 4);
		d = amountMap.get(year);
		if (d == null && amount != null)
			d = amount;
		else if (d != null && amount != null)
			d += amount;
		amountMap.put(year, d);

		// 总合计
		d = amountMap.get("total");
		if (d == null && amount != null)
			d = amount;
		else if (d != null && amount != null)
			d += amount;
		amountMap.put("total", d);
	}

	@CreateUI
	public void createUI(Composite parent) {
		super.createUI(parent);
	}

	@Override
	public void setViewerInput() {
		ArrayList<CBSItem> roots = new ArrayList<CBSItem>();
		roots.add(cbsItem);
		super.setViewerInput(roots);
	}

	@Override
	protected String getMonthlyAmountText(Object element, String period) {
		return Optional.ofNullable(getMonthlyAmount(element, period)).map(v -> Formatter.getMoneyFormatString(v))
				.orElse("");
	}

	@Override
	protected String getTotalAmountText(Object element) {
		return Optional.ofNullable(getTotalAmount(element)).map(v -> Formatter.getMoneyFormatString(v)).orElse("");
	}

	@Override
	protected String getYearlyAmountSummaryText(Object element, String year) {
		return Optional.ofNullable(getYearlyAmountSummary(element, year)).map(v -> Formatter.getMoneyFormatString(v))
				.orElse("");
	}

	private Double getMonthlyAmount(Object item, String period) {
		Double value = null;
		if (item instanceof CBSItem) {
			value = cbsItemAmount.get(period);
		} else if (item instanceof AccountItem) {
			value = Optional.ofNullable(accountItemAmount.get(((AccountItem) item).getId())).map(m -> m.get(period))
					.orElse(null);
		}
		return value;
	}

	protected abstract Double getAmount(CBSSubject u);

	private Double getYearlyAmountSummary(Object item, String year) {
		Double value = null;
		if (item instanceof CBSItem) {
			value = cbsItemAmount.get(year);
		} else if (item instanceof AccountItem) {
			value = Optional.ofNullable(accountItemAmount.get(((AccountItem) item).getId())).map(m -> m.get(year))
					.orElse(null);
		}
		return value;
	}

	private Double getTotalAmount(Object item) {
		Double value = null;
		if (item instanceof CBSItem) {
			value = cbsItemAmount.get("total");
		} else if (item instanceof AccountItem) {
			value = Optional.ofNullable(accountItemAmount.get(((AccountItem) item).getId())).map(m -> m.get("total"))
					.orElse(null);
		}
		return value;
	}

	@Override
	protected Date[] getRange() {
		return scope.getCBSRange();
	}

	@Override
	protected Color getNumberColor(Object item) {
		if (item instanceof AccountItem && ((AccountItem) item).countSubAccountItems() == 0) {
			return null;
		} else if (item instanceof CBSItem) {
			return color;
		}
		return color;
	}

	public void updateCBSSubjectAmount(CBSSubject subject) {
		getUpsertedCBSSubject(subject);

		// TODO 测试, 造成修改记录后反应慢。refresh时调用默认调用所有的计算,预计要用7秒+。
		// long start = System.currentTimeMillis();
		// viewer.refresh();
		// long end = System.currentTimeMillis();
		// logger.debug("updateCBSSubjectAmount refresh:" + (end - start));

		// 获取修改的年份、月份和费用科目
		String period = subject.getId();
		String year = period.substring(0, 4);
		String subjectNumber = subject.getSubjectNumber();

		// 更新金额数据
		Double amount = getAmount(subject);
		// 获取当前金额数据中的值

		Double oldAmount = Optional.ofNullable(accountItemAmount.get(subjectNumber))
				.map(m -> Optional.ofNullable(m.get(period)).orElse(null)).orElse(null);
		if (oldAmount != null && amount != null) {
			amount = amount - oldAmount;
		}

		changeAmountData(period, subjectNumber, amount);

		// 获取修改列的index、年合计列的index和总合计的index
		// 修改列的index
		int periodIndex = -1;
		// 年合计的index
		int yearIndex = -1;
		// 总合计的index
		int totalIndex = -1;

		// 根据name获取年合计列和总合计列，并通过grid获取其index
		Grid grid = viewer.getGrid();
		for (GridColumn gridColumn : grid.getColumns()) {
			Object name = gridColumn.getData("name");
			if ("budgetTotal".equals(name)) {
				totalIndex = grid.indexOf(gridColumn);
			} else if (year.equals(name)) {
				yearIndex = grid.indexOf(gridColumn);
			} else if (period.equals(name)) {
				periodIndex = grid.indexOf(gridColumn);
			}
			if (periodIndex != -1 && yearIndex != -1 && totalIndex != -1) {
				break;
			}
		}

		// 刷新修改行所在的行及其上级行
		for (GridItem item : grid.getItems()) {
			Object data = item.getData();
			if (data instanceof AccountItem && subjectNumber.equals(((AccountItem) data).getId())) {
				refreshGridItemAndParent(item, period, periodIndex, year, yearIndex, totalIndex);
				break;
			}
		}
	}

	/**
	 * 刷新行及其上级行
	 * 
	 * @param item
	 *            待刷新的行
	 * @param period
	 *            月份
	 * @param periodIndex
	 *            修改列的index
	 * @param year
	 *            合计年份
	 * @param yearIndex
	 *            修改列年合计的index
	 * @param totalIndex
	 *            各月合计的index
	 */
	private void refreshGridItemAndParent(GridItem item, String period, int periodIndex, String year, int yearIndex,
			int totalIndex) {
		// 判断当前行是否存在上级行，存在时，刷新上级行
		GridItem parentItem = item.getParentItem();
		if (parentItem != null) {
			refreshGridItemAndParent(parentItem, period, periodIndex, year, yearIndex, totalIndex);
		}
		Object data = item.getData();
		// 计算并设置当前列
		if (periodIndex >= 0) {
			String periodText = getMonthlyAmountText(data, period);
			item.setText(periodIndex, periodText);
		}

		// 计算并设置年合计
		if (yearIndex >= 0) {
			String yearText = getYearlyAmountSummaryText(data, year);
			item.setText(yearIndex, yearText);
		}
		// 计算并设置各月合计
		if (totalIndex >= 0) {
			String totalText = getTotalAmountText(data);
			item.setText(totalIndex, totalText);
		}
	}

	protected abstract CBSSubject getUpsertedCBSSubject(CBSSubject subject);

	@Override
	protected EditingSupport supportMonthlyEdit(GridViewerColumn vcol) {
		if (!hasPermission()) {
			return null;
		}

		final String id = (String) vcol.getColumn().getData("name");
		return new EditingSupport(viewer) {

			@Override
			protected void setValue(Object element, Object value) {
				try {
					double d = Formatter.getDouble((String) value);
					CBSSubject subject = new CBSSubject().setCBSItem_id(cbsItem.get_id())
							.setSubjectNumber(((AccountItem) element).getId()).setId(id);
					setAmount(subject, d);
					updateCBSSubjectAmount(subject);
				} catch (Exception e) {
					Layer.message(e.getMessage(), Layer.ICON_ERROR);
				}

			}

			@Override
			protected Object getValue(Object element) {
				Double value = getMonthlyAmount((AccountItem) element, id);
				return value == null ? "" : value.toString();
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				return new TextCellEditor(viewer.getGrid());
			}

			@Override
			protected boolean canEdit(Object element) {
				if (element instanceof AccountItem)
					return ((AccountItem) element).behavior();
				return false;
			}
		};
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

	protected abstract void setAmount(CBSSubject subject, double amount);

	protected abstract String getEditbindingAction();

}
