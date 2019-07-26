package com.bizvisionsoft.pms.formdef;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bson.Document;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.nebula.jface.gridviewer.GridTableViewer;
import org.eclipse.nebula.jface.gridviewer.GridViewerColumn;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.rap.json.JsonObject;
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
import org.eclipse.swt.widgets.Button;
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

import com.bizivisionsoft.widgets.swt.customized.SelectorCellEditor;
import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.annotations.AUtil;
import com.bizvisionsoft.bruicommons.ModelLoader;
import com.bizvisionsoft.bruicommons.model.Assembly;
import com.bizvisionsoft.bruicommons.model.FormField;
import com.bizvisionsoft.bruiengine.assembly.TextFilter;
import com.bizvisionsoft.bruiengine.assembly.exporter.ExportableFormBuilder;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.UserSession;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.bruiengine.ui.Selector;
import com.bizvisionsoft.bruiengine.util.BruiColors;
import com.bizvisionsoft.bruiengine.util.BruiColors.BruiColor;
import com.bizvisionsoft.bruiengine.util.Controls;
import com.bizvisionsoft.service.exporter.ExportableForm;
import com.bizvisionsoft.service.exporter.ExportableFormField;
import com.bizvisionsoft.service.model.ExportDocRule;
import com.bizvisionsoft.service.model.FormDef;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.service.tools.Formatter;

public class EditExportDocRuleDialog extends Dialog {

	public Logger logger = LoggerFactory.getLogger(getClass());

	public static int open(BruiAssemblyContext context, ExportDocRule exportDocRule) {
		EditExportDocRuleDialog dialog = new EditExportDocRuleDialog(context, Display.getCurrent().getActiveShell());
		dialog.setExportDocRule(exportDocRule);
		return dialog.open();
	}

	public static EditExportDocRuleDialog create(BruiAssemblyContext context, ExportDocRule exportDocRule) {
		EditExportDocRuleDialog dialog = new EditExportDocRuleDialog(context, Display.getCurrent().getActiveShell());
		dialog.setExportDocRule(exportDocRule);
		return dialog;
	}

	private Map<String, String> formDefFieldList;
	private ExportDocRule exportDocRule;

	private BruiAssemblyContext context;

	private Text selectionField;
	private GridTableViewer viewer;
	private Text postProc;

	protected EditExportDocRuleDialog(BruiAssemblyContext context, Shell parentShell) {
		super(parentShell);
		this.context = context;
	}

	public void setExportDocRule(ExportDocRule exportDocRule) {
		this.exportDocRule = AUtil.deepCopy(exportDocRule);
		// ����FormDef�ı༭������ID��ȡ���հ�ı༭��Assembly
		FormDef formDef = exportDocRule.getFormDef();
		String editorTypeId = formDef.getEditorTypeId();
		Assembly formDefAssy = ModelLoader.getLatestVersionEditorAssemblyOfType(editorTypeId);
		if (formDefAssy != null) {
			// ��ȡFormDef��Ӧ�༭�����ֶ��嵥
			formDefFieldList = ModelLoader.getEditorAssemblyFieldNameMap(formDefAssy);
		}

	}

	@Override
	protected Control createButtonBar(Composite parent) {
		return null;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
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

		Composite tabItem = createTabItem(folder, "�ֶ�����");

		selectionField = createSelectionField(tabItem);
		viewer = createTableField(tabItem);

		tabItem = createTabItem(folder, "����ű�(JavaScript)");
		postProc = createMultiTextField(tabItem);

		folder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		initData();

		Composite toolbar = new Composite(panel, SWT.NONE);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
		layoutData.heightHint = 32;
		toolbar.setLayoutData(layoutData);
		toolbar.setFont(parent.getFont());

		FormLayout layout = new FormLayout();
		layout.marginWidth = 4;
		toolbar.setLayout(layout);

		Button right = Controls.button(toolbar).rwt(Controls.CSS_NORMAL).setText(IDialogConstants.get().OK_LABEL)
				.loc(SWT.TOP | SWT.BOTTOM | SWT.RIGHT, 120).listen(SWT.Selection, e -> {
					buttonPressed(IDialogConstants.OK_ID);
				}).get();
		parent.getShell().setDefaultButton(right);
		right = Controls.button(toolbar).rwt(Controls.CSS_WARNING).setText(IDialogConstants.get().CANCEL_LABEL)
				.loc(SWT.TOP | SWT.BOTTOM, 120).right(right, -16).listen(SWT.Selection, e -> {
					buttonPressed(IDialogConstants.CANCEL_ID);
				}).get();

		// �������õĹ�����
		Controls.button(toolbar).rwt(Controls.CSS_INFO).setText("���").listen(SWT.Selection, e -> {
			buttonPressed(IDialogConstants.DETAILS_ID);
		}).loc(SWT.TOP | SWT.BOTTOM, 120).left();

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
		Selector.open("/���ѡ�������༭����.selectorassy", context, null, l -> {
			Assembly selectedAssy = (Assembly) l.get(0);
			String id = selectedAssy.getId();
			selectionField.setText(id);

			try {
				ExportableForm buildForm = ExportableFormBuilder.buildForm(selectedAssy);
				exportDocRule.setExportableForm(buildForm);
			} catch (IOException e1) {
				logger.error("�༭�������������ô���", e);
			}

			// ��ȡѡ��༭�����ֶ��嵥
			Map<String, String> selectedFieldList = ModelLoader.getEditorAssemblyFieldNameMap(selectedAssy);

			List<Document> fieldMap = getFieldMap(selectedFieldList, formDefFieldList);

			viewer.setInput(fieldMap);
		});
	}

	/**
	 * ��ȡ�����ĵ�������ֶ��嵥
	 * 
	 * @param selectedFieldList
	 * @param formDefFieldList
	 * @return
	 */
	private List<Document> getFieldMap(Map<String, String> selectedFieldList, Map<String, String> formDefFieldList) {
		List<Document> result = new ArrayList<Document>();
		if (Check.isAssigned(selectedFieldList))
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

	public boolean setSelection(List<Document> data) {
		selectionField.setText("");
		exportDocRule.setEditorId(null);
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

		// �����ı�
		Label titleLabel = new Label(container, SWT.CENTER);
		UserSession.bruiToolkit().enableMarkup(titleLabel);
		titleLabel.setText("�ֶ�����");
		titleLabel.setData(RWT.CUSTOM_VARIANT, "field");

		FormData fd = new FormData();
		fd.top = new FormAttachment();
		fd.left = new FormAttachment();
		fd.width = 100;
		titleLabel.setLayoutData(fd);

		// ����ѡ�����
		Composite pane = Controls.comp(container, SWT.BORDER).formLayout().get();
		Text text = new Text(pane, SWT.NONE);
		text.setMessage("�����ֶ�����");
		text.setCursor(text.getDisplay().getSystemCursor(SWT.CURSOR_HAND));

		Controls.button(pane).rwt("inline").setText("��ѯ").loc(SWT.RIGHT | SWT.BOTTOM, 54, 36)//
				.select(e -> {
					viewer.setFilters(new ViewerFilter[] { new TextFilter(viewer.getGrid(), text.getText()) });
				})//
				.addLeft(() -> Controls.button(pane)).rwt("inline").setText("���").size(54, 36).bottom()//
				.select(e -> {
					text.setText("");
					viewer.setFilters(new ViewerFilter[0]);
				})//
				.addLeft(() -> Controls.handle(text)).loc(SWT.TOP | SWT.BOTTOM | SWT.LEFT);

		fd = new FormData();
		fd.top = new FormAttachment();
		fd.right = new FormAttachment(100);
		fd.left = new FormAttachment(titleLabel);
		pane.setLayoutData(fd);

		GridTableViewer viewer = new GridTableViewer(container, SWT.CHECK | SWT.V_SCROLL | SWT.H_SCROLL);
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
			width -= 10;
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
		fd = new FormData();
		new Label(toolbar, SWT.HORIZONTAL | SWT.SEPARATOR).setLayoutData(fd);
		fd.top = new FormAttachment();
		fd.left = new FormAttachment();
		fd.right = new FormAttachment(100);

		Controls.button(toolbar).rwt("inbox").setText("����").loc(SWT.LEFT, 64, 28).left(0, 10).bottom(100, -10)//
				.select(this::runCreateItem)//
				.addRight(10, () -> Controls.button(toolbar)).rwt("inbox").setText("ɾ��").size(64, 28).bottom(100, -10)//
				.select(this::runRemoveSelected)//
				.addRight(10, () -> Controls.button(toolbar)).rwt("inbox").setText("���").size(64, 28).bottom(100, -10)//
				.select(this::runRemoveAllItems);

		fd = new FormData();
		grid.setLayoutData(fd);
		fd.top = new FormAttachment(titleLabel);
		fd.left = new FormAttachment(0);
		fd.right = new FormAttachment(100);
		fd.bottom = new FormAttachment(toolbar);

		fd = new FormData();
		toolbar.setLayoutData(fd);
		fd.height = 48;
		fd.left = new FormAttachment(0);
		fd.right = new FormAttachment(100);
		fd.bottom = new FormAttachment(100);
		return viewer;
	}

	@SuppressWarnings("unchecked")
	protected void runCreateItem(Event e) {
		Document item = new Document();
		((List<Document>) viewer.getInput()).add(item);
		viewer.add(item);
		viewer.editElement(item, 0);
	}

	@SuppressWarnings("unchecked")
	protected void runRemoveSelected(Event e) {
		if (viewer.getInput() == null)
			return;

		List<Document> elements = (List<Document>) getCheckedItems();
		((List<Document>) viewer.getInput()).removeAll(elements);
		viewer.remove(elements.toArray());
	}

	public List<?> getCheckedItems() {
		return Arrays.asList(viewer.getGrid().getItems()).stream().filter(i -> i.getChecked()).map(i -> i.getData())
				.collect(Collectors.toCollection(ArrayList::new));
	}

	protected void runRemoveAllItems(Event e) {
		if (MessageDialog.openConfirm(getShell(), "����ֶ�����", "��ȷ������ֶ����ã�")) {
			viewer.setInput(new ArrayList<Document>());
			Layer.message("�����");
		}
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
				viewer.refresh(element);
			}

		});

		// ����
		col = new GridColumn(grid, SWT.NONE);
		col.setData("name", "type");
		col.setText("����");
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
				int selectedIndex = (int) value;
				if (selectedIndex >= 0) {
					((Document) element).append("type", items.get(selectedIndex)).append("value", null).append("valueText", null);
					viewer.refresh(element);
				}
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

		// ֵ
		col = new GridColumn(grid, SWT.NONE);
		col.setData("name", "value");
		col.setText("ֵ");
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

				if (ExportDocRule.TYPE_FIELD_ARRAY.equals(type) || ExportDocRule.TYPE_FIELD_TABLE.equals(type)) {// ����Ϊ�������ʱ��ʹ��DialogCellEditor�����������ı�����б༭
					editor = new SelectorCellEditor(viewer.getGrid(), SWT.READ_ONLY) {
						@Override
						protected void openDialog() {
							String dialogTitle = "";
							String dialogMessage = "";
							if (ExportDocRule.TYPE_FIELD_ARRAY.equals(type)) {
								dialogTitle = "�༭���鳣��";
								dialogMessage = "����д�������͵ĳ�������ʽ���£�[\"Ԫ��1\",\"Ԫ��2\"]";
							} else if (ExportDocRule.TYPE_FIELD_TABLE.equals(type)) {
								dialogTitle = "�༭�����";
								dialogMessage = "����д������͵ĳ�������ʽ���£�[[\\\"Ԫ��1\\\",\\\"Ԫ��2\\\"]]";
							}

							InputDialog d = new InputDialog(getShell(), dialogTitle, dialogMessage, doc.getString("value"), txt -> {
								try {
									JsonObject.readFrom(txt);
									return null;
								} catch (Exception e) {
									return "��д�����ݸ�ʽ����Ҫ��";
								}
							}).setTextMultiline(true);
							if (d.open() == InputDialog.OK) {
								doc.put("value", d.getValue());
							}
						}
					};
				} else if (ExportDocRule.TYPE_FIELD_MAPPING.equals(type)) {// ����Ϊӳ��ʱ��ʹ��DialogCellEditor���������ֶ��嵥����ѡ��
					editor = new SelectorCellEditor(viewer.getGrid(), SWT.READ_ONLY) {
						@Override
						protected void openDialog() {
							Selector.create("/vault/�༭���ֶ�ѡ����.selectorassy", context, formDefFieldList).open(l -> {
								String name = ((Document) l.get(0)).getString("name");
								doc.put("value", name);
								doc.put("valueText",
										(formDefFieldList.get(name) != null ? formDefFieldList.get(name) : "") + "[$" + name + "]");
							});
						}
					};
				} else if (ExportDocRule.TYPE_FIELD_BOOLEAN.equals(type)) {// ����Ϊ����ʱ��ʹ��CheckboxCellEditor���б༭
					editor = new CheckboxCellEditor(viewer.getGrid());
				} else if (ExportDocRule.TYPE_FIELD_NUMBER.equals(type) || ExportDocRule.TYPE_FIELD_STRING.equals(type)) {// ����Ϊ�ı�����ֵʱ��ʹ��TextCellEditor���б༭
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
				else
					return Formatter.getString(doc.get("value"));
			}

			@Override
			protected void setValue(Object element, Object value) {
				Document doc = (Document) element;
				Object type = doc.get("type");
				if (ExportDocRule.TYPE_FIELD_BOOLEAN.equals(type))
					doc.put("value", value);
				else if (ExportDocRule.TYPE_FIELD_NUMBER.equals(type))
					try {
						doc.put("value", Double.parseDouble((String) value));
					} catch (Exception ex) {
					}
				else if (ExportDocRule.TYPE_FIELD_STRING.equals(type))
					doc.put("value", (String) value);
				viewer.refresh(element);
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

		// ����ѡ�����
		Text text = new Text(container, SWT.BORDER | SWT.MULTI | SWT.WRAP);
		text.setEditable(true);

		Controls.handle(text).loc(SWT.TOP | SWT.BOTTOM | SWT.LEFT);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
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

	@SuppressWarnings("unchecked")
	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {

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
			checkExportDocRule();
		}
		super.buttonPressed(buttonId);
	}

	@SuppressWarnings("unchecked")
	private void checkExportDocRule() {
		String editorTypeId = exportDocRule.getFormDef().getEditorTypeId();
		Assembly formDAssy = ModelLoader.getLatestVersionEditorAssemblyOfType(editorTypeId);
		if (formDAssy == null) {
			Layer.error("�޷���ȡ��������ѡ�༭��");
			return;
		}
		Map<String, String> formDFieldMap = ModelLoader.getEditorAssemblyFieldNameMap(formDAssy);

		ExportableForm exportableForm = exportDocRule.getExportableForm();
		List<Document> fieldLists = (List<Document>) viewer.getInput();

		List<String> errorField = new ArrayList<String>();
		List<String> errorExportableField = new ArrayList<String>();
		List<String> warningFieldField = new ArrayList<String>();

		// ��鵼���ĵ������ֶ��嵥�У�ӳ���ϵ���ֶ��Ƿ�ȫ�����ĵ�����ı༭���ֶ��б��У���������ʾ����
		Map<String, Document> fieldMaps = new HashMap<String, Document>();
		for (Document doc : fieldLists) {
			if (ExportDocRule.TYPE_FIELD_MAPPING.equals(doc.get("type"))) {
				String fieldName = doc.getString("value");
				if (!formDFieldMap.containsKey(fieldName)) {
					errorField.add(doc.getString("filedName"));
				}

			}
			fieldMaps.put(doc.getString("filed"), doc);
		}
		Set<String> formDFieldNames = formDFieldMap.keySet();
		formDFieldNames.removeAll(fieldMaps.keySet());
		for (String fieldName : formDFieldNames) {
			String text = formDFieldMap.get(fieldName);
			warningFieldField.add((text != null ? text : "") + "[" + fieldName + "]");
		}

		// ��鵼���ļ������е��ֶ��Ƿ����ĵ������ֶ��嵥��
		if (exportableForm != null)
			checkExportableFields(fieldMaps, exportableForm.fields, errorExportableField);

		StringBuffer sb = new StringBuffer();
		if (errorField.size() > 0) {
			sb.append("<span class='layui-badge'>����</span>: ");
			sb.append(Formatter.getString(errorField));
			sb.append("�����ֶ�δ�ڱ��ж��壬�޷������ĵ�.");
			sb.append("<br>");
		}

		if (errorExportableField.size() > 0) {
			sb.append("<span class='layui-badge'>����</span>: ");
			sb.append(Formatter.getString(errorExportableField));
			sb.append("�����ֶ��޷��������ļ�.");
			sb.append("<br>");
		}

		if (warningFieldField.size() > 0) {
			sb.append("<span class='layui-badge layui-bg-orange'>����</span>: ");
			sb.append(Formatter.getString(warningFieldField));
			sb.append("���ϱ��е��ֶ�δ�ҵ��ĵ�ӳ���ֶ�.");
			sb.append("<br>");
		}
		if (sb.length() > 0)
			Layer.alert("�����ĵ�������", sb.toString(), 600, 400, false);
		else
			Layer.message("�����ɡ�");
	}

	private void checkExportableFields(Map<String, Document> fieldMaps, List<ExportableFormField> exportableFields,
			List<String> errorField) {
		if (Check.isAssigned(exportableFields))
			for (ExportableFormField field : exportableFields) {
				String type = field.type;
				if (FormField.TYPE_PAGE.equals(type) || FormField.TYPE_INLINE.equals(type)) {
					checkExportableFields(fieldMaps, field.formFields, errorField);
				} else if (!FormField.TYPE_BANNER.equals(type)) {// ȥ��������ֶ�
					String name = field.name;
					if (!fieldMaps.containsKey(name)) {
						String text = field.text;
						errorField.add((text != null ? text : "") + "[" + name + "]");
					}

				}
			}
	}

	public ExportDocRule getExportDocRule() {
		return exportDocRule;
	}
}
