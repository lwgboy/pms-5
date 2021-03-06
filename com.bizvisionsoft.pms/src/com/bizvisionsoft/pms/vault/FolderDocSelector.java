package com.bizvisionsoft.pms.vault;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.bizivisionsoft.widgets.util.Layer;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruicommons.model.Assembly;
import com.bizvisionsoft.bruiengine.BruiAssemblyEngine;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.BruiService;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.UserSession;
import com.bizvisionsoft.bruiengine.ui.Part;
import com.bizvisionsoft.bruiengine.util.BruiColors.BruiColor;
import com.bizvisionsoft.bruiengine.util.Controls;
import com.bizvisionsoft.service.DocumentService;
import com.bizvisionsoft.service.model.IFolder;
import com.bizvisionsoft.service.model.VaultFolder;
import com.bizvisionsoft.serviceconsumer.Services;

public class FolderDocSelector extends Part {

	public static void selectFolder(IBruiContext context, Consumer<Object> doit) {
		selectFolder(context, IFolder.Null, SWT.SINGLE, doit);
	}

	public static void selectDocument(IBruiContext context, Consumer<Object> doit) {
		selectDocument(context, IFolder.Null, SWT.MULTI, doit);
	}

	public static void selectFolder(IBruiContext context, IFolder initialFolder, int selectionStyle, Consumer<Object> doit) {
		int explorerStyle = VaultExplorer.ADDRESS_BAR | VaultExplorer.NAVIGATOR;
		String title = "选择目录";
		boolean shortX = true;
		boolean shortY = false;
		select(context, initialFolder, selectionStyle, explorerStyle, title, shortX, shortY, doit);
	}

	public static void selectDocument(IBruiContext context, IFolder initialFolder, int selectionStyle, Consumer<Object> doit) {
		int explorerStyle = VaultExplorer.ADDRESS_BAR | VaultExplorer.NAVIGATOR | VaultExplorer.FILETABLE | VaultExplorer.SEARCH_FILE
				| VaultExplorer.SEARCH_FOLDER;
		String title = "选择文档";
		boolean shortX = false;
		boolean shortY = false;
		select(context, initialFolder, selectionStyle, explorerStyle, title, shortX, shortY, doit);
	}

	public static void select(IBruiContext context, IFolder initialFolder, int selectionStyle, int explorerStyle, String title,
			boolean shortX, boolean shortY, Consumer<Object> doit) {
		Shell parentShell = Display.getCurrent().getActiveShell();
		FolderDocSelector selector = new FolderDocSelector(parentShell);
		selector.setParentContext(context).setTitle(title).setSizeMode(shortY, shortX).setExplorerStyle(explorerStyle)
				.setSelectionStyle(selectionStyle);
		selector.init(initialFolder);
		selector.open(doit);
	}

	private boolean isTiny;
	private boolean isSmall;
	private String title;
	private VaultExplorer explorer;
	private BruiService service;
	private String domain;
	private int explorerStyle;
	private BruiAssemblyContext context;
	private int selectionStyle;

	private Object result;

	public FolderDocSelector(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.TITLE | SWT.RESIZE | SWT.ON_TOP | SWT.MAX | SWT.APPLICATION_MODAL);
		setBlockOnOpen(true);
	}

	public FolderDocSelector setParentContext(IBruiContext parentContext) {
		context = UserSession.newAssemblyContext().setParent(parentContext);
		return this;
	}

	public FolderDocSelector setSizeMode(boolean isTiny, boolean isSmall) {
		this.isTiny = isTiny;
		this.isSmall = isSmall;
		return this;
	}

	public FolderDocSelector setTitle(String title) {
		this.title = title;
		return this;
	}

	public FolderDocSelector setExplorerStyle(int explorerStyle) {
		this.explorerStyle = explorerStyle;
		return this;
	}

	public FolderDocSelector setSelectionStyle(int selectionStyle) {
		this.selectionStyle = selectionStyle;
		return this;
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setText(title);
		super.configureShell(newShell);
	}

	@Override
	protected Point getInitialSize() {
		Rectangle disb = Display.getCurrent().getBounds();

		int width = isSmall ? 820 : 1200;// 最小宽度
		int height = isTiny ? 480 : 800;// 最小高度

		// 如果宽度大于屏幕宽度，设为屏幕宽度
		width = width > disb.width ? disb.width : width;
		// 如果高度大于屏幕高度，设为屏幕高度
		height = height > disb.height ? disb.height : height;

		return new Point(width, height);
	}

	private void init(IFolder initialFolder) {
		service = new BruiService(this);
		domain = service.getDomain();
		explorer = new VaultExplorer() {

			@Override
			protected IFolder[] getPath(IFolder folder) {
				if (IFolder.Null.equals(folder)) {
					return new IFolder[0];
				} else {
					List<VaultFolder> result = Services.get(DocumentService.class).getPath(folder.get_id(), domain);
					if (initialFolderPath != null && result.size() > 0) {
						List<VaultFolder> remove = new ArrayList<VaultFolder>();
						result.forEach(f -> {
							for (IFolder iFolder : initialFolderPath) {
								if (iFolder.get_id().equals(f.get_id()))
									remove.add(f);
							}
						});
						if (remove.size() > 0)
							result.removeAll(remove);
						result.removeAll(Arrays.asList(initialFolderPath));
					}
					return result.toArray(new IFolder[0]);
				}
			}

			@Override
			protected Assembly getFileTableAssembly() {
				Assembly assy = (Assembly) service.getAssembly("vault/文档列表.gridassy").clone();
				assy.getActions().clear();
				assy.getRowActions().clear();
				if ((VaultExplorer.FILETABLE & explorerStyle) != 0) {// 仅当选择文档的时候，在文档上才有勾选框
					assy.setCheckOn(true);
				}
				return assy;
			}

			@Override
			protected Assembly getNavigatorAssembly() {
				Assembly assy = (Assembly) service.getAssembly("vault/目录导航.gridassy").clone();
				assy.getActions().clear();
				assy.getRowActions().clear();
				if ((VaultExplorer.FILETABLE & explorerStyle) == 0) {// 仅当选择目录的时候，在目录上才有勾选框
					assy.setCheckOn(true);
				}
				return assy;
			}

			@Override
			protected Assembly getSearchFolderAssembly() {
				Assembly assy = (Assembly) service.getAssembly("vault/目录查询结果.gridassy").clone();
				assy.getActions().clear();
				if ((VaultExplorer.FILETABLE & explorerStyle) == 0) {// 仅当选择目录的时候，才有勾选框并且要清除选择按钮
					assy.getRowActions().clear();
					assy.setCheckOn(true);
				}
				return assy;
			}

			@Override
			protected Assembly getSearchFileAssembly() {
				Assembly assy = (Assembly) service.getAssembly("vault/文档查询结果.gridassy").clone();
				assy.getActions().clear();
				assy.getRowActions().clear();
				if ((VaultExplorer.FILETABLE & explorerStyle) != 0) {// 仅当选择文档的时候，才有勾选框
					assy.setCheckOn(true);
				}
				return assy;
			}

			@Override
			public IFolder getInitialFolder() {
				return initialFolder;
			}

			@Override
			protected int getStyle() {
				return explorerStyle;
			}

			@Override
			protected List<List<Action>> createToolbarActions() {
				ArrayList<List<Action>> result = new ArrayList<>();
				if ((VaultExplorer.SEARCH_FOLDER & explorerStyle) != 0) {
					List<Action> row = new ArrayList<>();
					result.add(row);
					row.add(VaultActions.create(VaultActions.searchFolder, true, false));
				}

				if ((VaultExplorer.SEARCH_FILE & explorerStyle) != 0) {
					List<Action> row;
					if (result.isEmpty()) {
						row = new ArrayList<>();
						result.add(row);
					} else {
						row = result.get(0);
					}
					row.add(VaultActions.create(VaultActions.findDocuments, true, false));
					row.add(VaultActions.create(VaultActions.search, true, false));
				}

				return result;
			}
		};
		explorer.setBruiService(service).setContext((BruiAssemblyContext) context.setEngine(new BruiAssemblyEngine(explorer)));
		explorer.init();
	}

	@Override
	protected void createContents(Composite parent) {
		parent.setLayout(new FormLayout());
		Composite content = Controls.comp(parent).loc(SWT.TOP | SWT.LEFT | SWT.RIGHT).bottom(100, -42).bg(BruiColor.Grey_200).get();
		// 创建内容区
		explorer.createUI(content);
		// 创建按扭区

		Controls.label(parent, SWT.SEPARATOR | SWT.HORIZONTAL).loc(SWT.LEFT | SWT.RIGHT).top(content);
		Composite bar = Controls.comp(parent).loc(SWT.LEFT | SWT.RIGHT | SWT.BOTTOM).top(content, 1).get();
		createButtons(bar);
	}

	private void createButtons(Composite parent) {
		FormLayout layout = new FormLayout();
		layout.marginHeight = 4;
		layout.marginWidth = 4;
		layout.spacing = 4;
		parent.setLayout(layout);
		// TODO此处布局按
		Controls<Button> btn = Controls.button(parent).rwt(Controls.CSS_NORMAL).setText("确定").loc(SWT.RIGHT | SWT.TOP | SWT.BOTTOM)
				.width(120).listen(SWT.Selection, e -> okPressed());

		// 取消按钮
		if (((SWT.SINGLE | SWT.MULTI) & selectionStyle) != 0) {
			Controls.button(parent).rwt(Controls.CSS_WARNING).setText("取消").loc(SWT.TOP | SWT.BOTTOM).right(btn.get()).width(120)
					.listen(SWT.Selection, e -> cancelPressed());
		}

		// 全选，取消全选
		if ((SWT.MULTI & selectionStyle) != 0) {
			btn = Controls.button(parent).rwt(Controls.CSS_INFO).setText("全部选择").loc(SWT.LEFT | SWT.TOP | SWT.BOTTOM).width(120)
					.listen(SWT.Selection, e -> selectAllPressed());
			Controls.button(parent).rwt(Controls.CSS_INFO).setText("取消选择").loc(SWT.TOP | SWT.BOTTOM).left(btn.get()).width(120)
					.listen(SWT.Selection, e -> unSelectAllPressed());
		}
	}

	private void unSelectAllPressed() {
		GridPart gridPart = explorer.getCurrentDisplayPart();
		gridPart.setCheckAll(false);
	}

	private void selectAllPressed() {
		GridPart gridPart = explorer.getCurrentDisplayPart();
		gridPart.setCheckAll(true);
	}

	private void cancelPressed() {
		setReturnCode(Window.CANCEL);
		close();
	}

	private void okPressed() {
		List<Object> checkedItems = explorer.getCurrentDisplayPart().getCheckedItems();
		if (checkedItems.size() == 0) {
			if ((VaultExplorer.FILETABLE & explorerStyle) == 0)
				Layer.error("请选择目录。");
			else
				Layer.error("请选择文档。");

			return;
		}

		if ((SWT.SINGLE & selectionStyle) != 0) {
			result = checkedItems.get(0);
		} else {
			result = checkedItems;
		}
		setReturnCode(Window.OK);
		close();
	}

	public Object getResult() {
		return result;
	}

	private FolderDocSelector open(Consumer<Object> doit) {
		if (Window.OK == open()) {
			doit.accept(getResult());
		}
		return this;
	}

}
