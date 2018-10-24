package com.bizvisionsoft.pms.resource;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.bson.Document;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.nebula.jface.gridviewer.GridViewerColumn;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridColumnGroup;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

import com.bizivisionsoft.widgets.datetime.DateTimeSetting;
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
import com.bizvisionsoft.bruiengine.ui.DateTimeInputDialog;
import com.bizvisionsoft.bruiengine.util.Controls;
import com.bizvisionsoft.service.WorkService;
import com.bizvisionsoft.service.model.Period;
import com.bizvisionsoft.service.tools.Formatter;
import com.bizvisionsoft.serviceconsumer.Services;

public class ShowDeptResourceASM extends GridPart {

	@Inject
	private IBruiService brui;

	@Inject
	private BruiAssemblyContext context;

	private Calendar start;

	private Calendar end;

	private GridTreeViewer viewer;

	private Locale locale;

	private List<Document> resource;

	private Composite content;

	private ArrayList<GridColumn> footerTotalCols;

	private ArrayList<GridColumn> footerDateCols;

	private String userId;

	public ShowDeptResourceASM() {
	}

	public ShowDeptResourceASM(IBruiService brui, BruiAssemblyContext context, Composite parent) {
		this.brui = brui;
		this.context = context;
		this.content = parent;
	}

	@Override
	public GridTreeViewer getViewer() {
		return viewer;
	}

	@Init
	protected void init() {
		userId = brui.getCurrentUserId();
		start = Calendar.getInstance();
		end = Calendar.getInstance();
		start.set(Calendar.MONTH, 0);
		start.set(Calendar.DAY_OF_MONTH, 1);
		start.set(Calendar.HOUR_OF_DAY, 0);
		start.set(Calendar.MINUTE, 0);
		start.set(Calendar.SECOND, 0);
		start.set(Calendar.MILLISECOND, 0);

		end.set(Calendar.MONTH, 11);
		end.set(Calendar.DAY_OF_MONTH, 1);
		end.set(Calendar.HOUR_OF_DAY, 0);
		end.set(Calendar.MINUTE, 0);
		end.set(Calendar.SECOND, 0);
		end.set(Calendar.MILLISECOND, 0);
		end.add(Calendar.MONTH, 1);
		end.add(Calendar.MILLISECOND, -1);
	}

	@CreateUI
	public void createUI(Composite parent) {
		locale = RWT.getLocale();

		parent.setLayout(new FormLayout());

		List<Action> rightActions = new ArrayList<Action>();
		Action addAction = new Action();
		addAction.setName("period");
		addAction.setText("期间");
		addAction.setForceText(true);
		addAction.setStyle("info");
		rightActions.add(addAction);

		StickerTitlebar bar = null;
		bar = new StickerTitlebar(parent, null, rightActions).setActions(context.getAssembly().getActions());
		bar.addListener(SWT.Selection, e -> {
			Action action = ((Action) e.data);
			if ("period".equals(action.getName())) {
				setDate();
			}
		});
		FormData fd = new FormData();
		bar.setLayoutData(fd);
		fd.left = new FormAttachment(0);
		fd.top = new FormAttachment(0);
		fd.right = new FormAttachment(100);
		fd.height = 48;

		bar.setText("资源数据表");

		content = Controls.contentPanel(parent).mLoc().mTop(bar).layout(new FillLayout(SWT.VERTICAL)).get();

		viewer = new GridTreeViewer(content, SWT.H_SCROLL | SWT.V_SCROLL);
		Grid grid = viewer.getGrid();
		grid.setHeaderVisible(true);
		grid.setFooterVisible(true);
		grid.setLinesVisible(true);
		UserSession.bruiToolkit().enableMarkup(grid);

		footerTotalCols = new ArrayList<GridColumn>();
		footerDateCols = new ArrayList<GridColumn>();
		createViewer();

		doRefresh();

	}

	private void setDate() {
		DateTimeInputDialog dtid = new DateTimeInputDialog(brui.getCurrentShell(), "设置期间", "请设置查看期间",
				(a, b) -> (a == null || b == null) ? "必须选择时间" : null)
						.setDateSetting(DateTimeSetting.month().setRange(true));
		if (dtid.open() == DateTimeInputDialog.OK) {
			Date[] range = dtid.getValues();

			setResourceTransfer(range[0], range[1]);
		}
	}

	public void setResourceTransfer(Date from, Date to) {
		start.setTime(from);
		start.set(Calendar.DAY_OF_MONTH, 1);
		start.set(Calendar.HOUR_OF_DAY, 0);
		start.set(Calendar.MINUTE, 0);
		start.set(Calendar.SECOND, 0);
		start.set(Calendar.MILLISECOND, 0);

		end.setTime(to);
		end.set(Calendar.DAY_OF_MONTH, 1);
		end.set(Calendar.HOUR_OF_DAY, 0);
		end.set(Calendar.MINUTE, 0);
		end.set(Calendar.SECOND, 0);
		end.set(Calendar.MILLISECOND, 0);
		end.add(Calendar.MONTH, 1);
		end.add(Calendar.MILLISECOND, -1);
		Grid grid = viewer.getGrid();
		GridColumnGroup[] columnGroups = grid.getColumnGroups();
		for (GridColumnGroup gridColumnGroup : columnGroups) {
			String name = (String) gridColumnGroup.getData("name");
			if (!"plan".equals(name) && !"actual".equals(name) && gridColumnGroup != null
					&& !gridColumnGroup.isDisposed()) {
				for (GridColumn gridColumn : gridColumnGroup.getColumns()) {
					footerDateCols.remove(gridColumn);
				}
				gridColumnGroup.dispose();
			}
		}
		GridColumn[] columns = grid.getColumns();
		for (GridColumn gridColumn : columns) {
			String name = (String) gridColumn.getData("name");
			if ("delete".equals(name) && gridColumn != null && !gridColumn.isDisposed()) {
				gridColumn.dispose();
			}
		}
		createDateColumn();

		doRefresh();

	}

	public void doRefresh() {
		if (userId != null) {
			resource = Services.get(WorkService.class)
					.getResourceOfChargedDept(new Period(start.getTime(), end.getTime()), userId);
		}

		if (resource != null) {
			viewer.setInput(resource);
			doRefreshFooterText();
		}
	}

	private void doRefreshFooterText() {
		footerTotalCols.forEach(col -> {
			col.setFooterText(getFooterText((String) col.getData("name")));
		});
		footerDateCols.forEach(col -> {
			col.setFooterText(getFooterDateText((String) col.getData("name"), (String) col.getData("id")));
		});
	}

	@Override
	public void setViewerInput() {
	}

	@Override
	public void setViewerInput(List<?> input) {
	}

	private void createViewer() {
		Grid grid = viewer.getGrid();
		grid.setData(RWT.FIXED_COLUMNS, 3);

		createColumn();

	}

	private void createColumn() {
		Column c;

		c = new Column();
		c.setName("resId");
		c.setText("资源编号");
		c.setWidth(120);
		c.setAlignment(SWT.LEFT);
		c.setMoveable(false);
		c.setResizeable(true);
		createTitleColumn(c);

		c = new Column();
		c.setName("type");
		c.setText("资源类型");
		c.setWidth(120);
		c.setAlignment(SWT.LEFT);
		c.setMoveable(false);
		c.setResizeable(true);
		createTitleColumn(c);

		c = new Column();
		c.setName("name");
		c.setText("名称");
		c.setWidth(120);
		c.setAlignment(SWT.LEFT);
		c.setMoveable(false);
		c.setResizeable(true);
		createTitleColumn(c);

		GridColumnGroup grp = new GridColumnGroup(viewer.getGrid(), SWT.CENTER);
		grp.setData("name", "plan");
		grp.setText("计划");
		grp.setExpanded(true);

		GridColumn col = new GridColumn(grp, SWT.CENTER);
		col.setText("标准");
		col.setData("name", "planBasicQty");
		col.setWidth(80);
		col.setMoveable(false);
		col.setResizeable(false);
		col.setAlignment(SWT.RIGHT);
		col.setResizeable(true);
		col.setDetail(true);
		col.setSummary(true);

		footerTotalCols.add(col);

		GridViewerColumn vcol = new GridViewerColumn(viewer, col);
		vcol.setLabelProvider(getTitleLabelProvider("planBasicQty"));

		col = new GridColumn(grp, SWT.CENTER);
		col.setData("name", "planOverTimeQty");
		col.setText("加班");
		col.setWidth(80);
		col.setMoveable(false);
		col.setResizeable(false);
		col.setAlignment(SWT.RIGHT);
		col.setResizeable(true);
		col.setDetail(true);
		col.setSummary(true);

		footerTotalCols.add(col);

		vcol = new GridViewerColumn(viewer, col);
		vcol.setLabelProvider(getTitleLabelProvider("planOverTimeQty"));

		col = new GridColumn(grp, SWT.CENTER);
		col.setData("name", "planAmount");
		col.setText("金额（万元）");
		col.setWidth(110);
		col.setMoveable(false);
		col.setResizeable(false);
		col.setAlignment(SWT.RIGHT);
		col.setResizeable(true);
		col.setDetail(true);
		col.setSummary(true);

		footerTotalCols.add(col);

		vcol = new GridViewerColumn(viewer, col);
		vcol.setLabelProvider(getTitleLabelProvider("planAmount"));

		grp = new GridColumnGroup(viewer.getGrid(), SWT.CENTER);
		grp.setData("name", "actual");
		grp.setText("实际");
		grp.setExpanded(true);

		col = new GridColumn(grp, SWT.CENTER);
		col.setText("标准");
		col.setData("name", "actualBasicQty");
		col.setWidth(80);
		col.setMoveable(false);
		col.setResizeable(false);
		col.setAlignment(SWT.RIGHT);
		col.setResizeable(true);
		col.setDetail(true);
		col.setSummary(true);

		footerTotalCols.add(col);

		vcol = new GridViewerColumn(viewer, col);
		vcol.setLabelProvider(getTitleLabelProvider("actualBasicQty"));

		col = new GridColumn(grp, SWT.CENTER);
		col.setData("name", "actualOverTimeQty");
		col.setText("加班");
		col.setWidth(80);
		col.setMoveable(false);
		col.setResizeable(false);
		col.setAlignment(SWT.RIGHT);
		col.setResizeable(true);
		col.setDetail(true);
		col.setSummary(true);

		footerTotalCols.add(col);

		vcol = new GridViewerColumn(viewer, col);
		vcol.setLabelProvider(getTitleLabelProvider("actualOverTimeQty"));

		col = new GridColumn(grp, SWT.CENTER);
		col.setData("name", "actualAmount");
		col.setText("金额（万元）");
		col.setWidth(110);
		col.setMoveable(false);
		col.setResizeable(false);
		col.setAlignment(SWT.RIGHT);
		col.setResizeable(true);
		col.setDetail(true);
		col.setSummary(true);

		footerTotalCols.add(col);

		vcol = new GridViewerColumn(viewer, col);
		vcol.setLabelProvider(getTitleLabelProvider("actualAmount"));

		createDateColumn();

		viewer.setContentProvider(new ITreeContentProvider() {

			@Override
			public void dispose() {
			}

			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}

			@Override
			public Object[] getElements(Object value) {
				if (value instanceof List) {
					return ((List<?>) value).toArray();
				} else if (value instanceof Object[]) {
					return (Object[]) value;
				}
				return new Object[0];
			}

			@Override
			public Object[] getChildren(Object parentElement) {
				return null;
			}

			@Override
			public Object getParent(Object element) {
				return null;
			}

			@Override
			public boolean hasChildren(Object element) {
				return false;
			}

		});
	}

	private String getFooterText(String key) {
		double value = 0d;
		for (Document doc : resource) {
			value += getDoubleValue(doc.get(key));
		}
		return Formatter.getString(value, "#,##0.0", locale);
	}

	@SuppressWarnings("unchecked")
	private String getFooterDateText(String name, String id) {
		double value = 0d;
		for (Document resourceDoc : resource) {
			Object obj = resourceDoc.get("resource");
			if (obj instanceof List) {
				List<Document> list = (List<Document>) obj;
				for (Document doc : list) {
					if (id.equals(doc.get("id"))) {
						Object docValue = doc.get(name);
						if (docValue instanceof Number)
							value += ((Number) docValue).doubleValue();
					}
				}

			}
		}
		String format = null;
		if ("planAmount".equals(name) || "actualAmount".equals(name))
			format = "#,##0.0";
		else
			format = "0.0";
		String text = Formatter.getString(value, format, locale);
		return "0.0".equals(text) ? "" : text;
	}

	private void createDateColumn() {
		Calendar now = Calendar.getInstance();
		now.setTime(start.getTime());
		while (now.before(end)) {
			createDateColumn(now.getTime());
			now.add(Calendar.MONTH, 1);
		}

		Column c = new Column();
		c.setName("delete");
		c.setText("");
		c.setWidth(0);
		c.setAlignment(SWT.CENTER);
		c.setMoveable(false);
		c.setResizeable(true);
		createTitleColumn(c);
	}

	private void createDateColumn(Date now) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM", locale);
		String name = sdf.format(now);
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMM");
		String id = sdf1.format(now);
		GridColumnGroup grp = new GridColumnGroup(viewer.getGrid(), SWT.CENTER);

		grp.setData("name", name);
		grp.setText(name);
		grp.setExpanded(true);

		GridColumn col = new GridColumn(grp, SWT.CENTER);
		col.setText("计划工时");
		col.setData("name", "planQty");
		col.setData("id", id);
		col.setWidth(80);
		col.setMoveable(false);
		col.setAlignment(SWT.RIGHT);
		col.setResizeable(true);
		col.setDetail(true);
		col.setSummary(true);

		footerDateCols.add(col);

		GridViewerColumn vcol = new GridViewerColumn(viewer, col);
		vcol.setLabelProvider(getColumnLabelProvider(id, "planQty"));

		col = new GridColumn(grp, SWT.CENTER);
		col.setData("name", "actualQty");
		col.setData("id", id);
		col.setText("实际工时");
		col.setWidth(80);
		col.setMoveable(false);
		col.setAlignment(SWT.RIGHT);
		col.setResizeable(true);
		col.setDetail(true);
		col.setSummary(true);

		footerDateCols.add(col);

		vcol = new GridViewerColumn(viewer, col);
		vcol.setLabelProvider(getColumnLabelProvider(id, "actualQty"));

		col = new GridColumn(grp, SWT.CENTER);
		col.setText("计划金额");
		col.setData("name", "planAmount");
		col.setData("id", id);
		col.setWidth(80);
		col.setMoveable(false);
		col.setAlignment(SWT.RIGHT);
		col.setResizeable(true);
		col.setDetail(true);
		col.setSummary(true);

		footerDateCols.add(col);

		vcol = new GridViewerColumn(viewer, col);
		vcol.setLabelProvider(getColumnLabelProvider(id, "planAmount"));

		col = new GridColumn(grp, SWT.CENTER);
		col.setData("name", "actualAmount");
		col.setData("id", id);
		col.setText("实际金额");
		col.setWidth(80);
		col.setMoveable(false);
		col.setAlignment(SWT.RIGHT);
		col.setResizeable(true);
		col.setDetail(true);
		col.setSummary(true);

		footerDateCols.add(col);

		vcol = new GridViewerColumn(viewer, col);
		vcol.setLabelProvider(getColumnLabelProvider(id, "actualAmount"));
	}

	private void createTitleColumn(Column c) {
		GridColumn col = new GridColumn(viewer.getGrid(), SWT.NONE);
		col.setText(c.getText());
		col.setWidth(c.getWidth());
		col.setMoveable(c.isMoveable());
		col.setResizeable(c.isResizeable());

		GridViewerColumn vcol = new GridViewerColumn(viewer, col);
		vcol.setLabelProvider(getTitleLabelProvider(c.getName()));

	}

	@SuppressWarnings("unchecked")
	private ColumnLabelProvider getColumnLabelProvider(String id, String key) {
		ColumnLabelProvider labelProvider = new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Object obj = ((Document) element).get("resource");
				if (obj instanceof List) {
					List<Document> list = (List<Document>) obj;
					for (Document doc : list) {
						if (id.equals(doc.get("id"))) {
							Object value = doc.get(key);
							String format = null;
							if ("planAmount".equals(key) || "actualAmount".equals(key))
								format = "#,##0.0";
							else
								format = "0.0";
							String text = Formatter.getString(value, format, locale);
							return "0.0".equals(text) ? "" : text;
						}
					}

				}
				return "";
			}

			@Override
			public Color getBackground(Object element) {
				return null;
			}
		};
		return labelProvider;
	}

	private ColumnLabelProvider getTitleLabelProvider(String name) {
		return new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				String format = null;
				Object value;
				if ("planAmount".equals(name)) {
					format = "#,##0.0";
					value = ((Document) element).get(name);
				} else if ("actualAmount".equals(name)) {
					format = "#,##0.0";
					value = ((Document) element).get(name);
				} else {
					value = ((Document) element).get(name);
					if (value instanceof Number)
						format = "0.0";
				}
				String text = Formatter.getString(value, format, locale);
				return "0.0".equals(text) ? "" : text;
			}
		};
	}

	private double getDoubleValue(Object object) {
		if (object instanceof Number)
			return ((Number) object).doubleValue();
		return 0;
	}
}
