package com.bizvisionsoft.pms.formdef;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TextCellEditor;
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

import com.bizvisionsoft.bruicommons.ModelLoader;
import com.bizvisionsoft.bruicommons.model.Assembly;
import com.bizvisionsoft.bruicommons.model.FormField;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.service.UserSession;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.bruiengine.ui.Selector;
import com.bizvisionsoft.bruiengine.util.BruiColors;
import com.bizvisionsoft.bruiengine.util.BruiColors.BruiColor;
import com.bizvisionsoft.bruiengine.util.Controls;
import com.bizvisionsoft.service.model.ExportDocRule;
import com.bizvisionsoft.service.model.FormDef;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.service.tools.Formatter;

public class EditExportDocRuleDialog extends Dialog {

	public Logger logger = LoggerFactory.getLogger(getClass());

	public static int open(IBruiService br, BruiAssemblyContext context, ExportDocRule exportDocRule) {
		EditExportDocRuleDialog dialog = new EditExportDocRuleDialog(br, context, Display.getCurrent().getActiveShell());
		dialog.setExportDocRule(exportDocRule);
		return dialog.open();
	}

	private Map<String, String> formDefFieldList;
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

	public void setExportDocRule(ExportDocRule exportDocRule) {
		this.exportDocRule = exportDocRule;
		// 根据FormDef的编辑器类型ID获取最终版的编辑器Assembly
		FormDef formDef = exportDocRule.getFormDef();
		String editorTypeId = formDef.getEditorTypeId();
		String editorId = ModelLoader.getLatestVersionEditorIdOfType(editorTypeId);
		if (editorId != null) {
			Assembly formDefAssy = ModelLoader.getLibAssembly(editorId);
			// 获取FormDef对应编辑器的字段清单
			formDefFieldList = getFieldList(formDefAssy.getFields());
		}

	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.DETAILS_ID, "检查", false).setData(RWT.CUSTOM_VARIANT, Controls.CSS_INFO);

		createButton(parent, IDialogConstants.CANCEL_ID, "取消", false).setData(RWT.CUSTOM_VARIANT, Controls.CSS_WARNING);
		createButton(parent, IDialogConstants.OK_ID, "确定", true).setData(RWT.CUSTOM_VARIANT, Controls.CSS_NORMAL);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("文档导出规则");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite panel = (Composite) super.createDialogArea(parent);

		TabFolder folder = new TabFolder(panel, SWT.TOP | SWT.BORDER);

		Composite tabItem = createTabItem(folder, "基本信息");

		selectionField = createSelectionField(tabItem);
		postProc = createMultiTextField(tabItem);

		tabItem = createTabItem(folder, "导出文档字段");
		viewer = createTableField(tabItem);

		folder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		initData();

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
			Assembly selectedAssy = (Assembly) l.get(0);
			String id = selectedAssy.getId();
			selectionField.setText(id);

			// 获取选择编辑器的字段清单
			Map<String, String> selectedFieldList = getFieldList(selectedAssy.getFields());

			List<Document> fieldMap = getFieldMap(selectedFieldList, formDefFieldList);

			viewer.setInput(fieldMap);
		});
	}

	/**
	 * 获取导出文档规则的字段清单
	 * 
	 * @param selectedFieldList
	 * @param formDefFieldList
	 * @return
	 */
	private List<Document> getFieldMap(Map<String, String> selectedFieldList, Map<String, String> formDefFieldList) {
		List<Document> result = new ArrayList<Document>();

		selectedFieldList.forEach((name, text) -> {
			Document doc = new Document();
			result.add(doc);
			doc.put("filed", name);
			doc.put("filedName", (text != null ? text : "") + "[" + name + "]");

			if (Check.isAssigned(formDefFieldList) && formDefFieldList.containsKey(name)) {
				doc.put("type", ExportDocRule.TYPE_FIELD_MAPPING);
				doc.put("value", name);
				doc.put("valueText", (formDefFieldList.get(name) != null ? formDefFieldList.get(name) : "") + "[$" + name + "]");
			}
		});

		return result;
	}

	/**
	 * 根据FormField获取字段清单
	 * 
	 * @param formFields
	 * @return
	 */
	private Map<String, String> getFieldList(List<FormField> formFields) {
		Map<String, String> result = new HashMap<String, String>();
		if (Check.isAssigned(formFields))
			formFields.forEach(formField -> {
				String type = formField.getType();
				// 去除掉page和行设置、
				if (FormField.TYPE_PAGE.equals(type) || FormField.TYPE_INLINE.equals(type)) {
					result.putAll(getFieldList(formField.getFormFields()));
				} else if (!FormField.TYPE_BANNER.equals(type)) {// 去除掉横幅字段
					result.put(formField.getName(), formField.getText());
				}
			});
		return result;
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
		Document item = new Document();
		setSelection(Arrays.asList(item));
		viewer.editElement(item, 0);
	}

	@SuppressWarnings("unchecked")
	protected void runRemoveSelected(Event e) {
		if (viewer.getInput() == null)
			return;
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		Document element = (Document) selection.getFirstElement();
		if (element != null) {
			((List<Document>) viewer.getInput()).remove(element);
			viewer.remove(element);
		}
	}

	protected void runRemoveAllItems(Event e) {
		// TODO Auto-generated method stub
		viewer.setInput(new ArrayList<Document>());
	}

	private void createColumns(GridTableViewer viewer, Grid grid) {
		// 字段列
		GridColumn col = new GridColumn(grid, SWT.NONE);
		col.setData("name", "filedName");
		col.setText("字段");
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
		vcol.setEditingSupport(new EditingSupport(viewer) {
			@Override
			protected CellEditor getCellEditor(Object element) {
				return new TextCellEditor(viewer.getGrid());
			}

			@Override
			protected boolean canEdit(Object element) {
				return true;
			}

			@Override
			protected Object getValue(Object element) {
				return Formatter.getString(((Document) element).get("filed"));
			}

			@Override
			protected void setValue(Object element, Object value) {
				Document doc = (Document) element;
				doc.append("filedName", (String) value);
				doc.append("filed", (String) value);
			}

		});

		// 类型
		col = new GridColumn(grid, SWT.NONE);
		col.setData("name", "type");
		col.setText("类型");
		col.setAlignment(SWT.CENTER);
		col.setWidth(80);
		col.setMinimumWidth(50);
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

		vcol.setEditingSupport(new EditingSupport(viewer) {

			List<String> items = Arrays.asList(ExportDocRule.TYPE_FIELD_ALL);

			@Override
			protected void setValue(Object element, Object value) {
				((Document) element).append("type", items.get((int) value)).append("value", null).append("valueText", null);
				viewer.refresh(element);
			}

			@Override
			protected Object getValue(Object element) {
				return items.indexOf(((Document) element).get("type"));
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				return new ComboBoxCellEditor(viewer.getGrid(), items.toArray(new String[0])) {
					@Override
					protected void doSetValue(Object value) {
						super.doSetValue(value);
					}
				};
			}

			@Override
			protected boolean canEdit(Object element) {
				return true;
			}
		});

		// 值
		col = new GridColumn(grid, SWT.NONE);
		col.setData("name", "value");
		col.setText("值");
		col.setAlignment(SWT.LEFT);
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
				Document doc = (Document) element;
				if (ExportDocRule.TYPE_FIELD_MAPPING.equals(doc.get("type")))
					return Formatter.getString(doc.get("valueText"));

				return Formatter.getString(doc.get("value"));
			}
		});
		vcol.setEditingSupport(new EditingSupport(viewer) {
			@Override
			protected CellEditor getCellEditor(Object element) {
				CellEditor editor = null;
				Document doc = (Document) element;
				Object type = doc.get("type");

				if (ExportDocRule.TYPE_FIELD_ARRAY.equals(type) || ExportDocRule.TYPE_FIELD_TABLE.equals(type)) {// 类型为数组或表格时，使用DialogCellEditor，弹出多行文本框进行编辑
					editor = new DialogCellEditor(viewer.getGrid()) {
						@Override
						protected Object openDialogBox(Control parent) {// Composite
							Editor.create("/vault/生成文档字段规则-多行文本.editorassy", context, doc, true).setTitle(doc.getString("type")).open();
							return null;
						}
					};
				} else if (ExportDocRule.TYPE_FIELD_MAPPING.equals(type)) {// 类型为映射时，使用DialogCellEditor，弹出表单字段清单进行选择
					editor = new DialogCellEditor(viewer.getGrid()) {
						@Override
						protected Object openDialogBox(Control parent) {// Composite
							Selector.create("/vault/编辑器字段选择器.selectorassy", context, formDefFieldList).open(l -> {
								String name = ((Document) l.get(0)).getString("name");
								doc.put("value", name);
								doc.put("valueText",
										(formDefFieldList.get(name) != null ? formDefFieldList.get(name) : "") + "[$" + name + "]");
							});
							return null;
						}
					};
				} else if (ExportDocRule.TYPE_FIELD_BOOLEAN.equals(type)) {// 类型为布尔时，使用CheckboxCellEditor进行编辑
					editor = new CheckboxCellEditor(viewer.getGrid());
				} else if (ExportDocRule.TYPE_FIELD_NUMBER.equals(type) || ExportDocRule.TYPE_FIELD_STRING.equals(type)) {// 类型为文本或数值时，使用TextCellEditor进行编辑
					editor = new TextCellEditor(viewer.getGrid());
				}
				return editor;
			}

			@Override
			protected boolean canEdit(Object element) {
				return true;
			}

			@Override
			protected Object getValue(Object element) {
				Document doc = (Document) element;
				Object type = doc.get("type");
				if (ExportDocRule.TYPE_FIELD_BOOLEAN.equals(type))
					return doc.getBoolean("value", false);
				else if (ExportDocRule.TYPE_FIELD_NUMBER.equals(type) || ExportDocRule.TYPE_FIELD_STRING.equals(type))
					return Formatter.getString(doc.get("value"));
				return null;
			}

			@Override
			protected void setValue(Object element, Object value) {
				Document doc = (Document) element;
				Object type = doc.get("type");
				if (ExportDocRule.TYPE_FIELD_BOOLEAN.equals(type))
					doc.put("value", value);
				else if (ExportDocRule.TYPE_FIELD_NUMBER.equals(type))
					doc.put("value", Double.parseDouble((String) value));
				else if (ExportDocRule.TYPE_FIELD_STRING.equals(type))
					doc.put("value", (String) value);
				viewer.refresh(element);
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
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

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

		int width = point.x > 1200 ? point.x : 1200;// 最小宽度
		int height = point.y > 800 ? point.y : 800;// 最小高度

		// 如果宽度大于屏幕宽度，设为屏幕宽度
		width = width > disb.width ? disb.width : width;
		// 如果高度大于屏幕高度，设为屏幕高度
		height = height > disb.height ? disb.height : height;

		point.x = width;
		point.y = height;
		return point;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId != IDialogConstants.CANCEL_ID) {

			String editorId = selectionField.getText();
			if (editorId != null)
				exportDocRule.setEditorId(editorId);

			List<Document> fieldMap = (List<Document>) viewer.getInput();
			if (Check.isAssigned(fieldMap))
				exportDocRule.setFieldMap(fieldMap);

			String postProc2 = postProc.getText();
			if (postProc2 != null)
				exportDocRule.setPostProc(postProc2);
		}
		if (buttonId == IDialogConstants.DETAILS_ID) {
			checkFormDef();
		}
		super.buttonPressed(buttonId);
	}

	private void checkFormDef() {
		// TODO Auto-generated method stub

	}

}
