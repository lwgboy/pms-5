package com.bizvisionsoft.pms.vault;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruicommons.model.Assembly;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.BruiService;
import com.bizvisionsoft.bruiengine.service.IBruiContext;
import com.bizvisionsoft.bruiengine.service.UserSession;
import com.bizvisionsoft.bruiengine.ui.Part;
import com.bizvisionsoft.bruiengine.util.Controls;
import com.bizvisionsoft.service.DocumentService;
import com.bizvisionsoft.service.model.IFolder;
import com.bizvisionsoft.service.model.VaultFolder;
import com.bizvisionsoft.serviceconsumer.Services;

public class FolderDocSelector extends Part {

	public static void selectFolder(IBruiContext context, IFolder initialFolder, int selectionStyle) {
		Shell parentShell = Display.getCurrent().getActiveShell();
		FolderDocSelector selector = new FolderDocSelector(parentShell);
		selector.setParentContext(context).setTitle("选择目录").setSizeMode(false, true)
				.setExplorerStyle(VaultExplorer.ADDRESS_BAR | VaultExplorer.NAVIGATOR).setSelectionStyle(selectionStyle);
		selector.init(initialFolder);
		selector.open();
	}

	public static void selectFolder(IBruiContext context) {
		selectFolder(context, IFolder.Null, SWT.SINGLE);
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

	public FolderDocSelector(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.TITLE | SWT.RESIZE | SWT.ON_TOP | SWT.MAX | SWT.APPLICATION_MODAL);
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
					return result.toArray(new IFolder[0]);
				}
			}

			@Override
			protected Assembly getNavigatorAssembly() {
				Assembly assy = (Assembly) service.getAssembly("vault/目录导航.gridassy").clone();
				assy.getActions().clear();
				assy.getRowActions().clear();
				assy.setCheckOn(true);
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
				return new ArrayList<>();
			}
		};
		explorer.setBruiService(service).setContext(context);
		explorer.init();
	}

	@Override
	protected void createContents(Composite parent) {
		parent.setLayout(new FormLayout());
		Composite content = Controls.comp(parent).loc(SWT.TOP | SWT.LEFT | SWT.RIGHT).bottom(100, -36).get();
		// 创建内容区
		explorer.createUI(content);	
		// 创建按扭区
		Composite bar = Controls.comp(parent).loc(SWT.LEFT | SWT.RIGHT | SWT.BOTTOM).top(content).formLayout().get();
		createButtons(bar);
	}

	private void createButtons(Composite parent) {
		//TODO此处布局按
		Controls.button(parent).rwt(Controls.CSS_SERIOUS).setText("确定").loc(SWT.LEFT).listen(SWT.Selection, e -> close());

		//取消按钮
		if ((SWT.SINGLE & selectionStyle) != 0) {
			
		}

		//全选，取消全选
		if ((SWT.MULTI & selectionStyle) != 0) {
			
		}
	}

}
