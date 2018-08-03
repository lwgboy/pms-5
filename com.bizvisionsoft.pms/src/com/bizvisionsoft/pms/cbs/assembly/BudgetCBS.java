package com.bizvisionsoft.pms.cbs.assembly;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.ui.common.CreateUI;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.util.BruiColors;
import com.bizvisionsoft.bruiengine.util.BruiColors.BruiColor;
import com.bizvisionsoft.bruiengine.util.Util;
import com.bizvisionsoft.service.CBSService;
import com.bizvisionsoft.service.model.CBSItem;
import com.bizvisionsoft.service.model.CBSPeriod;
import com.bizvisionsoft.service.model.ICBSScope;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.Function;

public class BudgetCBS extends BudgetGrid {

	static class ExtendLabel extends ColumnLabelProvider {

		private Function<CBSItem, String> func;
		private Function<CBSItem, Color> colorFunc;

		public static ColumnLabelProvider newInstance(Function<CBSItem, String> func) {
			ExtendLabel p = new ExtendLabel();
			p.func = func;
			return p;
		}

		public static ColumnLabelProvider newInstance(Function<CBSItem, String> func,
				Function<CBSItem, Color> colorFunc) {
			ExtendLabel p = new ExtendLabel();
			p.func = func;
			p.colorFunc = colorFunc;
			return p;
		}

		@Override
		public String getText(Object element) {
			return func.apply((CBSItem) element);
		}

		@Override
		public Color getBackground(Object element) {
			if (colorFunc != null) {
				return colorFunc.apply((CBSItem) element);
			}
			if (((CBSItem) element).countSubCBSItems() == 0) {
				return null;
			} else {
				return BruiColors.getColor(BruiColor.Grey_50);
			}
		}
	}

	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService bruiService;

	private ICBSScope scope;

	@Init
	public void init() {
		setContext(context);
		setConfig(context.getAssembly());
		setBruiService(bruiService);
		scope = (ICBSScope) context.getRootInput();
		super.init();
	}

	@CreateUI
	public void createUI(Composite parent) {
		super.createUI(parent);
	}

	public void addCBSItem(CBSItem parentCBSItem, CBSItem cbsItemData) {
		try {
			CBSItem child = Services.get(CBSService.class).insertCBSItem(cbsItemData);
			parentCBSItem.addChild(child);
			viewer.refresh(parentCBSItem, true);
		} catch (Exception e) {
			String message = e.getMessage();
			if (message.indexOf("index") >= 0) {
				Layer.message("请勿在同一范围内重复添加相同编号的成本项", Layer.ICON_CANCEL);
			}
		}
	}

	public void deleteCBSItem(CBSItem cbsItem) {
		CBSItem parentCBSItem = cbsItem.getParent();
		if (parentCBSItem == null) {
			throw new RuntimeException("不允许删除CBS根节点。");
		}
		Services.get(CBSService.class).delete(cbsItem.get_id());
		parentCBSItem.removeChild(cbsItem);
		viewer.refresh();
	}

	public void updateCBSPeriodBudget(CBSItem cbsItem, CBSPeriod periodData) {
		CBSItem parentCBSItem = cbsItem.getParent();
		if (parentCBSItem == null) {
			throw new RuntimeException("不允许更改CBS根节点预算。");
		}
		Services.get(CBSService.class).updateCBSPeriodBudget(periodData);
		CBSItem newCbsItem = Services.get(CBSService.class).get(((CBSItem) cbsItem).get_id());
		newCbsItem.setParent(cbsItem.getParent());
		replaceItem(cbsItem, newCbsItem);
		// viewer.update(cbsItem, null);
		viewer.refresh();
	}

	@Override
	protected Date[] getRange() {
		return scope.getCBSRange();
	}

	public ICBSScope getScope() {
		return scope;
	}

	@Override
	protected Color getNumberColor(Object item) {
		if (((CBSItem) item).countSubCBSItems() == 0) {
			return null;
		} else {
			return BruiColors.getColor(BruiColor.Grey_50);
		}
	}

	@Override
	protected String getBudgetTotalText(Object element) {
		return Util.getGenericMoneyFormatText(((CBSItem) element).getBudgetSummary());
	}

	@Override
	protected String getBudgetYearSummaryText(Object element, String year) {
		return Util.getGenericMoneyFormatText(((CBSItem) element).getBudgetYearSummary(year));
	}

	@Override
	protected String getBudgetText(Object element, String name) {
		return Util.getGenericMoneyFormatText(((CBSItem) element).getBudget(name));
	}

//	@Override
//	protected EditingSupport supportMonthlyEdit(GridViewerColumn vcol) {
//		final String name = (String) vcol.getColumn().getData("name");
//		return new EditingSupport(viewer) {
//
//			@Override
//			protected void setValue(Object element, Object value) {
//				try {
//					updateCBSItemPeriodBudgetInput((CBSItem) element, name, value);
//				} catch (Exception e) {
//					Layer.message(e.getMessage(), Layer.ICON_CANCEL);
//				}
//			}
//
//			@Override
//			protected Object getValue(Object element) {
//				return Optional.ofNullable(((CBSItem) element).getBudget(name)).map(v -> {
//					if (v == 0)
//						return "";
//					return "" + v;
//				}).orElse("");
//			}
//
//			@Override
//			protected CellEditor getCellEditor(Object element) {
//				return new TextCellEditor(viewer.getGrid());
//			}
//
//			@Override
//			protected boolean canEdit(Object element) {
//				return ((CBSItem) element).countSubCBSItems() == 0;
//			}
//		};
//	}

	protected void updateCBSItemPeriodBudgetInput(CBSItem item, String name, Object input) throws Exception {
		double inputAmount = getDoubleValue(input);

		///////////////////////////////////////
		// 避免在没有修改的时候调用服务端程序
		double oldAmount = item.getBudget(name);
		if (inputAmount == oldAmount) {
			return;
		}
		///////////////////////////////////////

		CBSPeriod period = new CBSPeriod()//
				.setCBSItem_id(((CBSItem) item).get_id());
		Util.ifInstanceThen(context.getRootInput(), ICBSScope.class, r -> period.setRange(r.getCBSRange()));
		period.setBudget(inputAmount);
		period.setId(name);
		Date periodDate = new SimpleDateFormat("yyyyMM").parse(period.getId());
		period.checkRange(periodDate);
		updateCBSPeriodBudget(((CBSItem) item), period);
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

//	//////////////////////////////////////////////////////////////////////////////
//	// DEMO 奥飞
//	// 增加总体估算列
//	@Override
//	protected void createEstimationColumns(Grid grid) {
//		GridColumnGroup grp = new GridColumnGroup(grid, SWT.TOGGLE);
//		grp.setText("预算总盘");
//		grp.setExpanded(true);
//
//		// 负责公司／团队
//		Column c = new Column();
//		c.setText("负责");
//		c.setWidth(48);
//		c.setAlignment(SWT.CENTER);
//		c.setMoveable(false);
//		c.setResizeable(true);
//		c.setDetail(true);
//		c.setSummary(false);
//		GridViewerColumn vcol = createColumn(grp, c);
//		vcol.setLabelProvider(ExtendLabel.newInstance(itm -> {
//			if (itm.countSubCBSItems() > 0) {
//				return "";
//			}
//			if (Boolean.TRUE.equals(itm.getInternalPayment()))
//				return "内部";
//			return "外部";
//
//		}));
//		vcol.setEditingSupport(new EditingSupport(viewer) {
//
//			@Override
//			protected void setValue(Object element, Object value) {
//				updatePaymentMethod((CBSItem) element, Boolean.TRUE.equals(value));
//			}
//
//			@Override
//			protected Object getValue(Object element) {
//				return Boolean.TRUE.equals(((CBSItem) element).getInternalPayment());
//			}
//
//			@Override
//			protected CellEditor getCellEditor(Object element) {
//				return new CheckboxCellEditor(grid);
//			}
//
//			@Override
//			protected boolean canEdit(Object itm) {
//				return ((CBSItem) itm).countSubCBSItems() == 0;
//			}
//		});
//
//		// 总费用
//		c = new Column();
//		c.setText("总费用");
//		c.setWidth(80);
//		c.setAlignment(SWT.RIGHT);
//		c.setMoveable(false);
//		c.setResizeable(true);
//		c.setDetail(true);
//		c.setSummary(true);
//		vcol = createColumn(grp, c);
//		vcol.setLabelProvider(ExtendLabel.newInstance(t -> getNumberText(t.getTotalEstimation(), "0.00"),
//				t -> BruiColors.getColor(BruiColor.Grey_50)));
//
//		// 费用/集
//		c = new Column();
//		c.setText("费用/集");
//		c.setWidth(80);
//		c.setAlignment(SWT.RIGHT);
//		c.setMoveable(false);
//		c.setResizeable(true);
//		c.setDetail(true);
//		c.setSummary(false);
//		vcol = createColumn(grp, c);
//		vcol.setLabelProvider(ExtendLabel.newInstance(t -> getNumberText(getBudgetPerEpisode(t), "0.00"),
//				t -> BruiColors.getColor(BruiColor.Grey_50)));
//
//		// 费用/分钟
//		c = new Column();
//		c.setText("费用/分钟");
//		c.setWidth(80);
//		c.setAlignment(SWT.RIGHT);
//		c.setMoveable(false);
//		c.setResizeable(true);
//		c.setDetail(true);
//		c.setSummary(false);
//		vcol = createColumn(grp, c);
//		vcol.setLabelProvider(ExtendLabel.newInstance(t -> getNumberText(getBudgetPerMinite(t), "0.00"),
//				t -> BruiColors.getColor(BruiColor.Grey_50)));
//
//		// 总费用来源
//		c = new Column();
//		c.setText("来源");
//		c.setWidth(160);
//		c.setAlignment(SWT.CENTER);
//		c.setMoveable(false);
//		c.setResizeable(true);
//		c.setDetail(true);
//		c.setSummary(false);
//		vcol = createColumn(grp, c);
//		vcol.setLabelProvider(
//				ExtendLabel.newInstance(t -> getSourceText(t), t -> BruiColors.getColor(BruiColor.Grey_50)));
//
//		// 数量
//		c = new Column();
//		c.setText("数量");
//		c.setWidth(64);
//		c.setAlignment(SWT.RIGHT);
//		c.setMoveable(false);
//		c.setResizeable(true);
//		c.setDetail(true);
//		c.setSummary(true);
//		vcol = createColumn(grp, c);
//		vcol.setLabelProvider(ExtendLabel.newInstance(t -> getNumberText(t.getQty(), "0")));
//		vcol.setEditingSupport(new EditingSupport(viewer) {
//
//			@Override
//			protected void setValue(Object element, Object value) {
//				try {
//					updateQty((CBSItem) element, value);
//				} catch (Exception e) {
//					Layer.message(e.getMessage(), Layer.ICON_CANCEL);
//				}
//			}
//
//			@Override
//			protected Object getValue(Object element) {
//				return getNumberText(((CBSItem) element).getQty(), "0");
//			}
//
//			@Override
//			protected CellEditor getCellEditor(Object element) {
//				return new TextCellEditor(grid);
//			}
//
//			@Override
//			protected boolean canEdit(Object itm) {
//				return ((CBSItem) itm).countSubCBSItems() == 0;
//			}
//		});
//
//		// 单价
//		c = new Column();
//		c.setText("单价");
//		c.setWidth(64);
//		c.setAlignment(SWT.RIGHT);
//		c.setMoveable(true);
//		c.setResizeable(true);
//		c.setDetail(true);
//		c.setSummary(true);
//		vcol = createColumn(grp, c);
//		vcol.setLabelProvider(ExtendLabel.newInstance(t -> getNumberText(t.getPrice(), "0.00")));
//		vcol.setEditingSupport(new EditingSupport(viewer) {
//
//			@Override
//			protected void setValue(Object element, Object value) {
//				try {
//					updatePrice((CBSItem) element, value);
//				} catch (Exception e) {
//					Layer.message(e.getMessage(), Layer.ICON_CANCEL);
//				}
//			}
//
//			@Override
//			protected Object getValue(Object element) {
//				return getNumberText(((CBSItem) element).getPrice(), "0.00");
//			}
//
//			@Override
//			protected CellEditor getCellEditor(Object element) {
//				return new TextCellEditor(grid);
//			}
//
//			@Override
//			protected boolean canEdit(Object itm) {
//				return ((CBSItem) itm).countSubCBSItems() == 0;
//			}
//		});
//	}

//	private Double getBudgetPerMinite(CBSItem t) {
//		CBSEstimationSetting es = getRoot().getEstimationSetting();
//		if (es == null || es.episodeTime == null || es.episodeTime == 0) {
//			return null;
//		}
//		Double epi = getBudgetPerEpisode(t);
//		if (epi == null) {
//			return null;
//		}
//		return epi / es.episodeTime;
//	}
//
//	private Double getBudgetPerEpisode(CBSItem t) {
//		CBSEstimationSetting es = getRoot().getEstimationSetting();
//		if (es == null || es.episodeCount == null || es.episodeCount == 0) {
//			return null;
//		}
//		return t.getTotalEstimation() / es.episodeCount;
//	}
//
//	private String getNumberText(Number number, String format) {
//		if (number == null || number.doubleValue() == 0)
//			return "";
//		return Util.getFormatText(number, format, null);
//	}
//
//	private void updateQty(CBSItem item, Object input) throws Exception {
//		double v = getDoubleValue(input);
//		double oldValue = Optional.ofNullable(item.getQty()).orElse(0d);
//		if (v == oldValue)
//			return;
//		item.setQty(v);
//		BasicDBObject fu = new FilterAndUpdate().filter(new BasicDBObject("_id", item.get_id()))
//				.set(new BasicDBObject("qty", v)).bson();
//		Services.get(CBSService.class).update(fu);
//		viewer.refresh();
//	}
//
//	private void updatePrice(CBSItem item, Object input) throws Exception {
//		double v = getDoubleValue(input);
//		double oldValue = Optional.ofNullable(item.getPrice()).orElse(0d);
//		if (v == oldValue)
//			return;
//		item.setPrice(v);
//		BasicDBObject fu = new FilterAndUpdate().filter(new BasicDBObject("_id", item.get_id()))
//				.set(new BasicDBObject("price", v)).bson();
//		Services.get(CBSService.class).update(fu);
//		viewer.refresh();
//	}
//
//	private void updatePaymentMethod(CBSItem item, boolean payment) {
//		Boolean oldValue = item.getInternalPayment();
//		if (payment == Boolean.TRUE.equals(oldValue)) {
//			return;
//		}
//		item.setInternalPayment(payment);
//		BasicDBObject fu = new FilterAndUpdate().filter(new BasicDBObject("_id", item.get_id()))
//				.set(new BasicDBObject("internalPayment", payment)).bson();
//		Services.get(CBSService.class).update(fu);
//		viewer.update(item, null);
//	}
//
//	private String getSourceText(CBSItem itm) {
//		String id = itm.getId();
//		if ("1".equals(id))
//			return "";
//		if ("1.1".equals(id))
//			return "总数";
//		if ("1.2".equals(id))
//			return "总数";
//		if ("1.3".equals(id))
//			return "总数";
//		if ("1.4".equals(id))
//			return "总数";
//		if ("2".equals(id))
//			return "";
//		if ("2.1".equals(id))
//			return "集数*单价/集";
//		if ("2.2".equals(id))
//			return "集数*单价/集";
//		if ("2.3".equals(id))
//			return "集数*单价/集";
//		if ("2.4".equals(id))
//			return "产品数量*单价/产品";
//		if ("2.5".equals(id))
//			return "集数*单价/集";
//		if ("2.6".equals(id))
//			return "总数";
//		if ("3".equals(id))
//			return "";
//		if ("3.1".equals(id))
//			return "集数*单价/集";
//		if ("3.2".equals(id))
//			return "总数";
//		if ("3.3".equals(id))
//			return "产品数量*单价/产品";
//		if ("3.4".equals(id))
//			return "产品数量*单价/产品";
//		if ("4".equals(id))
//			return "";
//		if ("4.1".equals(id))
//			return "集数*费用/集";
//		if ("4.2".equals(id))
//			return "集数*费用/集";
//		if ("4.3".equals(id))
//			return "集数*费用/集";
//		if ("4.4".equals(id))
//			return "集数*费用/集";
//		if ("4.5".equals(id))
//			return "集数*费用/集";
//		if ("4.6".equals(id))
//			return "集数*费用/集";
//		if ("5".equals(id))
//			return "";
//		if ("5.1".equals(id))
//			return "总数";
//		if ("5.2".equals(id))
//			return "总数";
//		if ("5.3".equals(id))
//			return "插曲数量*费用/插曲";
//		if ("6".equals(id))
//			return "";
//		if ("6.1".equals(id))
//			return "月份*费用/月";
//		if ("6.2".equals(id))
//			return "月份*费用/月";
//		if ("6.3".equals(id))
//			return "集数*费用/集";
//		if ("6.4".equals(id))
//			return "月份*费用/月";
//		if ("6.5".equals(id))
//			return "总数";
//		if ("7".equals(id))
//			return "";
//		if ("7.1".equals(id))
//			return "总数";
//		return "";
//	}
//
//	public void setEstimation() {
//		Object input = context.getRootInput();
//		if (input instanceof Project) {
//			// final ObjectId cbsId = ((Project) input).getCBS_id();
//			final CBSItem cbsRoot = getRoot();
//			CBSEstimationSetting estimationSetting = cbsRoot.getEstimationSetting();
//			if (estimationSetting == null) {
//				estimationSetting = new CBSEstimationSetting();
//			}
//			Editor.open("预算总盘设置", context, estimationSetting, (r, t) -> {
//
//				BasicDBObject fu = new FilterAndUpdate().filter(new BasicDBObject("_id", cbsRoot.get_id()))
//						.set(new BasicDBObject("estimationSetting", r)).bson();
//				Services.get(CBSService.class).update(fu);
//				cbsRoot.setEstimationSetting(t);
//				viewer.refresh();
//			});
//
//		}
//	}
//
//	private CBSItem getRoot() {
//		return (CBSItem) ((List<?>) viewer.getInput()).get(0);
//	}

	///////////////////////////////////////////////////////////////////////////////

}
