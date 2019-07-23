package com.bizvisionsoft.pms.formdef;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.nebula.jface.gridviewer.GridTableViewer;
import org.eclipse.nebula.jface.gridviewer.GridViewerColumn;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
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
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bizvisionsoft.bruicommons.model.Assembly;
import com.bizvisionsoft.bruicommons.model.FormField;
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
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.DETAILS_ID, "���", false).setData(RWT.CUSTOM_VARIANT, Controls.CSS_INFO);

		createButton(parent, IDialogConstants.CANCEL_ID, "ȡ��", false).setData(RWT.CUSTOM_VARIANT, Controls.CSS_WARNING);
		createButton(parent, IDialogConstants.OK_ID, "ȷ��", true).setData(RWT.CUSTOM_VARIANT, Controls.CSS_NORMAL);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("�ĵ���������");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite panel = (Composite) super.createDialogArea(parent);

		TabFolder folder = new TabFolder(panel, SWT.TOP | SWT.BORDER);

		Composite tabItem = createTabItem(folder, "������Ϣ");

		selectionField = createSelectionField(tabItem);
		postProc = createMultiTextField(tabItem);

		tabItem = createTabItem(folder, "�����ĵ��ֶ�");
		viewer = createTableField(tabItem);

		folder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		return panel;
	}

	private Composite createTabItem(TabFolder folder, String tabText) {
		TabItem item = new TabItem(folder, SWT.NONE);
		item.setText(tabText);
		ScrolledComposite sc = createPage(folder);
		item.setControl(sc);
		return (Composite) sc.getContent();
	}

	private ScrolledComposite createPage(Composite parent) {
		final ScrolledComposite sc = new ScrolledComposite(parent, SWT.V_SCROLL);
		Composite content = new Composite(sc, SWT.NONE);

		GridLayout layout = new GridLayout();
		layout.marginBottom = 4;
		layout.marginTop = 8;
		layout.marginLeft = 4;
		layout.marginRight = 4;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = 8;
		layout.horizontalSpacing = 0;
		content.setLayout(layout);

		sc.setExpandVertical(true);
		sc.setContent(content);

		sc.addListener(SWT.Resize, e -> {
			Point size = content.computeSize(sc.getBounds().width, SWT.DEFAULT);
			sc.getContent().setSize(size.x, size.y);
			sc.setMinHeight(size.y);
		});
		return sc;
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
	 * �����༭��ѡ���ֶ�
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

		// ����ѡ�����ı�
		Label titleLabel = new Label(container, SWT.CENTER);
		UserSession.bruiToolkit().enableMarkup(titleLabel);
		titleLabel.setText("�ĵ��༭��");
		titleLabel.setData(RWT.CUSTOM_VARIANT, "fieldrequired");

		GridData gd = new GridData(SWT.LEFT, SWT.FILL, false, false);
		gd.widthHint = 100;
		titleLabel.setLayoutData(gd);

		// ����ѡ�����
		Composite pane = Controls.comp(container, SWT.BORDER).formLayout().get();
		Text text = new Text(pane, SWT.NONE);
		text.setEditable(false);

		text.addListener(SWT.MouseDown, this::runShowSelector);
		text.setCursor(text.getDisplay().getSystemCursor(SWT.CURSOR_HAND));

		Controls.button(pane).rwt("inline").setText("ѡ��").loc(SWT.RIGHT | SWT.BOTTOM, 54, 36)//
				.select(this::runShowSelector)//
				.addLeft(() -> Controls.button(pane)).rwt("inline").setText("���").size(54, 36).bottom()//
				.select(e -> setSelection(new ArrayList<Document>()))//
				.addLeft(() -> Controls.handle(text)).loc(SWT.TOP | SWT.BOTTOM | SWT.LEFT);
		pane.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		return text;
	}

	protected void runShowSelector(Event e) {
		Selector.open("/���ѡ����.selectorassy", context, null, l -> {
			Assembly assembly = (Assembly) l.get(0);
			String id = assembly.getId();
			selectionField.setText(id);

			List<Document> fieldMap = new ArrayList<Document>();
			assembly.getFields().forEach(field -> setAssemblyField(fieldMap, field));
			viewer.setInput(fieldMap);
		});
	}

	/**
	 * ����Assembly��Field��fieldMap��
	 * 
	 * @param fieldMap
	 * @param formField
	 */
	private void setAssemblyField(List<Document> fieldMap, FormField formField) {
		String type = formField.getType();
		// TODO ȥ����page�������á�ȥ�������

		if (FormField.TYPE_PAGE.equals(type) || FormField.TYPE_INLINE.equals(type)) {
			formField.getFormFields().forEach(field -> setAssemblyField(fieldMap, field));
		} else if (!FormField.TYPE_BANNER.equals(type)) {
			Document doc = new Document();
			fieldMap.add(doc);
			doc.put("filed", formField.getName());
			String text = formField.getText();
			doc.put("filedName", (text != null ? text : "") + "[" + formField.getName() + "]");
			// TODO ���FormDef�ı༭�������ֶ�����Ĭ��ֵ
		}
	}

	public boolean setSelection(List<Document> data) {
		selectionField.setText("");
		exportDocRule.setEditorName(null);
		return true;
	}

	/**
	 * �����ֶ��嵥�ֶ�
	 * 
	 * @param parent
	 * @return
	 */
	private GridTableViewer createTableField(Composite parent) {
		Composite container = new Composite(parent, SWT.BORDER);
		container.setBackground(BruiColors.getColor(BruiColor.White));
		container.setLayout(new FormLayout());
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
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
				total += cols[i].getWidth();
			}
			for (int i = 0; i < cols.length; i++) {
				if (total != 0) {
					cols[i].setWidth(width * cols[i].getWidth() / total);
				}
			}
		});

		Composite toolbar = new Composite(container, SWT.NONE);
		toolbar.setLayout(new FormLayout());

		Controls.button(toolbar).rwt("inbox").setText("����").loc(SWT.LEFT, 64, 28).select(this::runCreateItem)//
				.addRight(() -> Controls.button(toolbar)).rwt("inbox").setText("ɾ��").size(64, 28)//
				.select(this::runRemoveSelected)//
				.addRight(() -> Controls.button(toolbar)).rwt("inbox").setText("���").size(64, 28)//
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
		// �ֶ���
		GridColumn col = new GridColumn(grid, SWT.NONE);
		col.setData("name", "filedName");
		col.setText("�ֶ�");
		col.setAlignment(SWT.LEFT);
		col.setWidth(120);
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

		// ����
		col = new GridColumn(grid, SWT.NONE);
		col.setData("name", "type");
		col.setText("����");
		col.setAlignment(SWT.CENTER);
		col.setWidth(80);
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

		// ֵ
		col = new GridColumn(grid, SWT.NONE);
		col.setData("name", "value");
		col.setText("ֵ");
		col.setAlignment(SWT.CENTER);
		col.setWidth(120);
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
	 * ��������JS�ֶ�
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
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// ����ѡ�����ı�
		Label titleLabel = new Label(container, SWT.CENTER);
		UserSession.bruiToolkit().enableMarkup(titleLabel);
		titleLabel.setText("����ű�(JavaScript)");
		titleLabel.setData(RWT.CUSTOM_VARIANT, "field_v");

		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd.horizontalSpan = 2;
		gd.heightHint = 28;
		titleLabel.setLayoutData(gd);

		// ����ѡ�����
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

		int width = point.x > 1200 ? point.x : 1200;// ��С���
		int height = point.y > 800 ? point.y : 800;// ��С�߶�

		// �����ȴ�����Ļ��ȣ���Ϊ��Ļ���
		width = width > disb.width ? disb.width : width;
		// ����߶ȴ�����Ļ�߶ȣ���Ϊ��Ļ�߶�
		height = height > disb.height ? disb.height : height;

		point.x = width;
		point.y = height;
		return point;
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.DETAILS_ID) {
			checkFormDef();
		}
		super.buttonPressed(buttonId);
	}

	private void checkFormDef() {
		// TODO Auto-generated method stub

	}

}
