package com.bizvisionsoft.pms.formdef;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.nebula.jface.gridviewer.GridTableViewer;
import org.eclipse.nebula.jface.gridviewer.GridViewerColumn;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bizvisionsoft.bruicommons.model.Assembly;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.service.UserSession;
import com.bizvisionsoft.bruiengine.ui.Selector;
import com.bizvisionsoft.bruiengine.util.BruiColors;
import com.bizvisionsoft.bruiengine.util.BruiColors.BruiColor;
import com.bizvisionsoft.bruiengine.util.Controls;
import com.bizvisionsoft.service.model.ExportDocRule;
import com.bizvisionsoft.service.tools.Check;

public class EditExportDocRuleDialog extends Dialog {

	public Logger logger = LoggerFactory.getLogger(getClass());

	public static int open(IBruiService br, BruiAssemblyContext context, ExportDocRule exportDocRule) {
		EditExportDocRuleDialog dialog = new EditExportDocRuleDialog(br, context, Display.getCurrent().getActiveShell());
		dialog.exportDocRule = exportDocRule;
		return dialog.open();
	}

	private ExportDocRule exportDocRule;
	private IBruiService br;
	private BruiAssemblyContext context;
	private Text selectionField;
	private GridTableViewer viewer;
	private Text postProc;

	protected EditExportDocRuleDialog(IBruiService br, BruiAssemblyContext context, Shell parentShell) {
		super(parentShell);
		this.br = br;
		this.context = context;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("文档导出规则");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite panel = (Composite) super.createDialogArea(parent);
		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 24;
		layout.marginHeight = 24;
		layout.horizontalSpacing = 24;
		layout.verticalSpacing = 12;
		panel.setLayout(layout);

		selectionField = createSelectionField(panel);
		viewer = createTableField(panel);
		postProc = createMultiTextField(panel);

		initData();

		return panel;
	}

	private void initData() {
		String editorId = exportDocRule.getEditorId();
		if (editorId != null)
			selectionField.setText(editorId);

		List<Document> fieldMap = exportDocRule.getFieldMap();
		if (Check.isAssigned(fieldMap))
			viewer.setInput(fieldMap);

		String postProc2 = exportDocRule.getPostProc();
		if (postProc2 != null)
			postProc.setText(postProc2);
	}

	/**
	 * 创建编辑器选择字段
	 * 
	 * @param parent
	 * @return
	 */
	private Text createSelectionField(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 2;
		container.setLayout(layout);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		// 创建选择器文本
		Label titleLabel = new Label(container, SWT.CENTER);
		UserSession.bruiToolkit().enableMarkup(titleLabel);
		titleLabel.setText("文档编辑器");
		titleLabel.setData(RWT.CUSTOM_VARIANT, "fieldrequired");

		GridData gd = new GridData(SWT.LEFT, SWT.FILL, false, false);
		gd.widthHint = 100;
		titleLabel.setLayoutData(gd);

		// 创建选择操作
		Composite pane = Controls.comp(container, SWT.BORDER).formLayout().get();
		Text text = new Text(pane, SWT.NONE);
		text.setEditable(false);

		text.addListener(SWT.MouseDown, this::runShowSelector);
		text.setCursor(text.getDisplay().getSystemCursor(SWT.CURSOR_HAND));

		Controls.button(pane).rwt("inline").setText("选择").loc(SWT.RIGHT | SWT.BOTTOM, 54, 36)//
				.select(this::runShowSelector)//
				.addLeft(() -> Controls.button(pane)).rwt("inline").setText("清空").size(54, 36).bottom()//
				.select(e -> setSelection(new ArrayList<Document>()))//
				.addLeft(() -> Controls.handle(text)).loc(SWT.TOP | SWT.BOTTOM | SWT.LEFT);
		pane.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		return text;
	}

	protected void runShowSelector(Event e) {
		Selector.open("/组件选择器.selectorassy", context, null, l -> {
			Assembly assembly = (Assembly) l.get(0);
			String id = assembly.getId();
			selectionField.setText(id);

			List<Document> fieldMap = new ArrayList<Document>();
			assembly.getFields().forEach(field -> {
				Document doc = new Document();
				fieldMap.add(doc);
				String type = field.getType();

				doc.put("filedName", field.getName());
			});
			viewer.setInput(fieldMap);
		});
	}

	public boolean setSelection(List<Document> data) {
		selectionField.setText("");
		exportDocRule.setEditorName(null);
		return true;
	}

	/**
	 * 创建字段清单字段
	 * 
	 * @param parent
	 * @return
	 */
	private GridTableViewer createTableField(Composite parent) {
		Composite container = new Composite(parent, SWT.BORDER);
		container.setBackground(BruiColors.getColor(BruiColor.White));
		container.setLayout(new FormLayout());
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.heightHint = 227;
		container.setLayoutData(gd);

		GridTableViewer viewer = new GridTableViewer(container, SWT.NONE | SWT.V_SCROLL | SWT.H_SCROLL);
		viewer.setUseHashlookup(false);
		viewer.setContentProvider(ArrayContentProvider.getInstance());

		Grid grid = viewer.getGrid();
		grid.setHeaderVisible(true);
		grid.setLinesVisible(true);

		createColumns(viewer, grid);

		grid.getParent().addListener(SWT.Resize, e -> {
			int width = grid.getParent().getBounds().width;
			if (width == 0) {
				return;
			}
			width -= 0;
			GridColumn[] cols = grid.getColumns();
			int total = 0;
			for (int i = 0; i < cols.length; i++) {
				if (!Boolean.TRUE.equals(cols[i].getData("fixedRight")))
					total += cols[i].getWidth();
			}
			for (int i = 0; i < cols.length; i++) {
				if (total != 0 && !Boolean.TRUE.equals(cols[i].getData("fixedRight"))) {
					cols[i].setWidth(width * cols[i].getWidth() / total);
				}
			}
		});

		Composite toolbar = new Composite(container, SWT.NONE);
		toolbar.setLayout(new FormLayout());

		Controls.button(toolbar).rwt("inbox").setText("创建").loc(SWT.LEFT, 64, 28).select(this::runCreateItem)//
				.addRight(() -> Controls.button(toolbar)).rwt("inbox").setText("删除").size(64, 28)//
				.select(this::runRemoveSelected)//
				.addRight(() -> Controls.button(toolbar)).rwt("inbox").setText("清空").size(64, 28)//
				.select(this::runRemoveAllItems);

		FormData fd = new FormData();
		grid.setLayoutData(fd);
		fd.top = new FormAttachment(0);
		fd.left = new FormAttachment(0);
		fd.right = new FormAttachment(100);
		fd.bottom = new FormAttachment(toolbar);

		fd = new FormData();
		toolbar.setLayoutData(fd);
		fd.height = 38;
		fd.left = new FormAttachment(0);
		fd.right = new FormAttachment(100);
		fd.bottom = new FormAttachment(100);
		return viewer;
	}

	protected void runCreateItem(Event e) {
		// TODO Auto-generated method stub

	}

	protected void runRemoveSelected(Event e) {
		// TODO Auto-generated method stub

	}

	protected void runRemoveAllItems(Event e) {
		// TODO Auto-generated method stub

	}

	private void createColumns(GridTableViewer viewer, Grid grid) {
		// 字段列
		GridColumn col = new GridColumn(grid, SWT.NONE);
		col.setData("name", "filedName");
		col.setText("字段");
		col.setAlignment(SWT.LEFT);
		col.setWidth(100);
		col.setMinimumWidth(100);
		col.setMoveable(false);
		col.setResizeable(true);
		col.setDetail(false);
		col.setSummary(false);

		GridViewerColumn vcol = new GridViewerColumn(viewer, col);
		vcol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((Document) element).getString("filedName");
			}
		});

		// 类型
		col = new GridColumn(grid, SWT.NONE);
		col.setData("name", "type");
		col.setText("类型");
		col.setAlignment(SWT.CENTER);
		col.setWidth(100);
		col.setMinimumWidth(100);
		col.setMoveable(false);
		col.setResizeable(true);
		col.setDetail(false);
		col.setSummary(false);

		vcol = new GridViewerColumn(viewer, col);
		vcol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((Document) element).getString("type");
			}
		});

		// 值
		col = new GridColumn(grid, SWT.NONE);
		col.setData("name", "value");
		col.setText("值");
		col.setAlignment(SWT.CENTER);
		col.setWidth(100);
		col.setMinimumWidth(100);
		col.setMoveable(false);
		col.setResizeable(true);
		col.setDetail(false);
		col.setSummary(false);

		vcol = new GridViewerColumn(viewer, col);
		vcol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((Document) element).getString("value");
			}
		});
	}

	/**
	 * 创建后处理JS字段
	 * 
	 * @param parent
	 * @return
	 */
	private Text createMultiTextField(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 2;
		container.setLayout(layout);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		// 创建选择器文本
		Label titleLabel = new Label(container, SWT.CENTER);
		UserSession.bruiToolkit().enableMarkup(titleLabel);
		titleLabel.setText("后处理脚本(JavaScript)");
		titleLabel.setData(RWT.CUSTOM_VARIANT, "field_v");

		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd.horizontalSpan = 2;
		gd.heightHint = 28;
		titleLabel.setLayoutData(gd);

		// 创建选择操作
		Text text = new Text(container, SWT.BORDER | SWT.MULTI | SWT.WRAP);
		text.setEditable(true);

		Controls.handle(text).loc(SWT.TOP | SWT.BOTTOM | SWT.LEFT);
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.horizontalSpan = 2;
		gd.heightHint = 96;
		text.setLayoutData(gd);

		return text;

	}

	@Override
	protected Point getInitialSize() {
		Rectangle disb = Display.getCurrent().getBounds();

		Point point = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT, true);

		int width = point.x > 820 ? point.x : 820;// 最小宽度
		int height = point.y > 480 ? point.y : 480;// 最小高度

		// 如果宽度大于屏幕宽度，设为屏幕宽度
		width = width > disb.width ? disb.width : width;
		// 如果高度大于屏幕高度，设为屏幕高度
		height = height > disb.height ? disb.height : height;

		point.x = width;
		point.y = height;
		return point;
	}
}
