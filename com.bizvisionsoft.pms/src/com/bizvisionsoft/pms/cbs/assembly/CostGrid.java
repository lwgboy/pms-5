package com.bizvisionsoft.pms.cbs.assembly;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.nebula.jface.gridviewer.GridViewerColumn;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumnGroup;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;

import com.bizvisionsoft.bruicommons.model.Column;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.UserSession;
import com.bizvisionsoft.bruiengine.util.BruiColors;
import com.bizvisionsoft.bruiengine.util.BruiColors.BruiColor;

public abstract class CostGrid extends GridPart {

	@Override
	protected GridTreeViewer createGridViewer(Composite parent) {
		viewer = new GridTreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setAutoExpandLevel(GridTreeViewer.ALL_LEVELS);
		viewer.setUseHashlookup(false);

		Grid grid = viewer.getGrid();
		grid.setHeaderVisible(true);
		grid.setFooterVisible(false);
		grid.setLinesVisible(true);
		UserSession.bruiToolkit().enableMarkup(grid);
		grid.setData(RWT.FIXED_COLUMNS, 3);

		return viewer;
	}

	@Override
	protected void createColumns(Grid grid) {

		/////////////////////////////////////////////////////////////////////////////////////
		// 创建列
		Column c = new Column();
		c.setName("id");
		c.setText("编号");
		c.setWidth(120);
		c.setAlignment(SWT.LEFT);
		c.setMoveable(false);
		c.setResizeable(true);
		createColumn(grid, c);

		c = new Column();
		c.setName("name");
		c.setText("名称");
		c.setWidth(160);
		c.setAlignment(SWT.LEFT);
		c.setMoveable(false);
		c.setResizeable(true);
		createColumn(grid, c).getColumn();
		
		createEstimationColumns(grid);
		
		//////////////////////////////////////////////////////////////////////////////

		// Note: 这个范围意义不大
		// c = new Column();
		// c.setName("scopename");
		// c.setText("范围");
		// c.setWidth(120);
		// c.setAlignment(SWT.LEFT);
		// c.setMoveable(false);
		// c.setResizeable(true);
		// createColumn(grid, c);

		c = new Column();
		c.setName("budgetTotal");
		c.setText("各月<br/>合计");
		c.setWidth(88);
		c.setMarkupEnabled(true);
		c.setAlignment(SWT.RIGHT);
		c.setMoveable(false);
		c.setResizeable(true);
		GridViewerColumn vcol = createColumn(grid, c);
		vcol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return getBudgetTotalText(element);
			}

			@Override
			public Color getBackground(Object element) {
				return BruiColors.getColor(BruiColor.Grey_50);
			}

		});
		vcol.getColumn().setFooterText(getBudgetTotalFootText());

		Date[] range = getRange();
		Calendar start = Calendar.getInstance();
		start.setTime(range[0]);
		start.set(Calendar.DATE, 1);
		start.set(Calendar.HOUR_OF_DAY, 0);
		start.set(Calendar.MINUTE, 0);
		start.set(Calendar.SECOND, 0);

		Calendar end = Calendar.getInstance();
		end.setTime(range[1]);

		String year = null;
		GridColumnGroup grp = null;
		while (start.before(end)) {
			String nYear = "" + start.get(Calendar.YEAR);
			if (!nYear.equals(year)) {
				// 创建合计列
				if (grp != null) {
					createYearTotal("" + (start.get(Calendar.YEAR) - 1), grp);

				}

				// 创建gruop
				grp = new GridColumnGroup(grid, SWT.TOGGLE);
				grp.setText(nYear);
				grp.setExpanded(true);
				year = nYear;
			}
			int i = start.get(Calendar.MONTH) + 1;
			String month = String.format("%02d", i);
			final Column monthCol = new Column();
			monthCol.setName(year + month);
			monthCol.setText(i + "月");
			monthCol.setWidth(64);
			monthCol.setAlignment(SWT.RIGHT);
			monthCol.setMoveable(false);
			monthCol.setResizeable(true);
			monthCol.setDetail(true);
			monthCol.setSummary(false);
			vcol = createColumn(grp, monthCol);
			vcol.setLabelProvider(new ColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					return getBudgetText(element, monthCol.getName());
				}

				@Override
				public Color getBackground(Object element) {
					return getNumberColor(element);
				}

			});
			vcol.getColumn().setFooterText(getBudgetFootText(monthCol.getName()));
			EditingSupport editingSupport = supportMonthlyEdit(vcol);
			if (editingSupport != null)
				vcol.setEditingSupport(editingSupport);
			start.add(Calendar.MONTH, 1);
		}
		createYearTotal(year, grp);
	}

	/**
	 * 子类覆盖 增加列
	 * @param grid
	 */
	protected void createEstimationColumns(Grid grid) {
		
	}

	/**
	 * 子类覆盖 编辑各月预算
	 * @param vcol
	 * @return
	 */
	protected EditingSupport supportMonthlyEdit(GridViewerColumn vcol) {
		return null;
	}

	private void createYearTotal(String year, GridColumnGroup grp) {
		GridViewerColumn vcol;
		final Column ySumCol = new Column();
		ySumCol.setName(year);
		ySumCol.setText("合计");
		ySumCol.setWidth(88);
		ySumCol.setAlignment(SWT.RIGHT);
		ySumCol.setMoveable(false);
		ySumCol.setResizeable(true);
		ySumCol.setDetail(true);
		ySumCol.setSummary(true);
		vcol = createColumn(grp, ySumCol);
		vcol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return getBudgetYearSummaryText(element, ySumCol.getName());
			}

			@Override
			public Color getBackground(Object element) {
				return BruiColors.getColor(BruiColor.Grey_50);
			}
		});
		vcol.getColumn().setFooterText(getBudgetYearSummaryFootText(ySumCol.getName()));
	}

	protected String getBudgetFootText(String name) {
		return "";
	}

	protected String getBudgetYearSummaryFootText(String name) {
		return "";
	}

	protected String getBudgetTotalFootText() {
		return "";
	}

	public void expandToLevel(Object elementOrTreePath, int level) {
		viewer.expandToLevel(elementOrTreePath, level);
	}

	protected abstract String getBudgetText(Object element, String name);

	protected abstract String getBudgetYearSummaryText(Object element, String name);

	protected abstract String getBudgetTotalText(Object element);

	protected abstract Date[] getRange();

	protected abstract Color getNumberColor(Object item);

}
