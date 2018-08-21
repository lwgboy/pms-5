package com.bizvisionsoft.pms.forecast;

import org.bson.types.ObjectId;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.nebula.jface.gridviewer.GridViewerColumn;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;

import com.bizvisionsoft.annotations.ui.common.CreateUI;
import com.bizvisionsoft.annotations.ui.common.Init;
import com.bizvisionsoft.annotations.ui.common.Inject;
import com.bizvisionsoft.bruicommons.model.Column;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.service.UserSession;
import com.bizvisionsoft.bruiengine.util.BruiColors;
import com.bizvisionsoft.bruiengine.util.BruiColors.BruiColor;
import com.bizvisionsoft.service.model.IRevenueForecastScope;

/**
 * 项目收益预测
 * 
 * @author hua
 *
 */
public class RevenueForecastASM extends GridPart {
	
	@Inject
	private BruiAssemblyContext context;

	@Inject
	private IBruiService br;

	private IRevenueForecastScope scope;
	
	@Init
	public void init() {
		setContext(context);
		setConfig(context.getAssembly());
		setBruiService(br);
		scope = context.getRootInput(IRevenueForecastScope.class,false);
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
			public String getText(Object element) {
				return getTotalAmountText(element);
			}

			@Override
			public Color getBackground(Object element) {
				return BruiColors.getColor(BruiColor.Grey_50);
			}

		});
		
		createExtendColumns(grid);

	}

	protected String getTotalAmountText(Object element) {
		return "";
	}

	protected void createExtendColumns(Grid grid) {
		
	}

	/**
	 * @param vcol
	 * @return
	 */
	protected EditingSupport supportEdit(GridViewerColumn vcol) {
		return null;
	}


	public void expandToLevel(Object elementOrTreePath, int level) {
		viewer.expandToLevel(elementOrTreePath, level);
	}


}
