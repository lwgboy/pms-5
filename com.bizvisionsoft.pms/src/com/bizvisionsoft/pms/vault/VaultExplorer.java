package com.bizvisionsoft.pms.vault;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruicommons.model.Assembly;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.AssemblyContainer;
import com.bizvisionsoft.bruiengine.util.Controls;
import com.bizvisionsoft.service.model.IFolder;
import com.bizvisionsoft.service.tools.Check;

public abstract class VaultExplorer {

	private static final Logger logger = LoggerFactory.getLogger(VaultExplorer.class);

	private IBruiService br;

	private BruiAssemblyContext context;

	protected GridPart folderPane;

	protected GridPart filePane;

	private AddressBar addressBar;

	private BruiAssemblyContext contextNavi;

	public VaultExplorer() {
	}

	public VaultExplorer setContext(BruiAssemblyContext context) {
		this.context = context;
		return this;
	}

	public VaultExplorer setBruiService(IBruiService brui) {
		this.br = brui;
		return this;
	}

	protected void init() {
		context.setInput(getInitialFolder());
	}

	public void createUI(Composite parent) {
		parent.setLayout(new FormLayout());
		// 创建顶部工具栏
		Composite bar = Controls.handle(createAddressBar(parent)).loc(SWT.LEFT | SWT.TOP | SWT.RIGHT).height(32).get();

		Composite navigator = Controls.handle(createNaviPane(parent)).loc(SWT.LEFT | SWT.BOTTOM, .25f).top(bar).formLayout().get();

		Controls.handle(createFilePane(parent)).loc(SWT.RIGHT | SWT.BOTTOM).left(navigator, 1).top(bar).formLayout().get();
	}

	/**
	 * 子类实现本方法，返回根目录清单
	 * 
	 * @return
	 */
	public abstract IFolder getInitialFolder();

	/**
	 * 创建顶部工具栏
	 * 
	 * @param parent
	 * @return
	 */
	private Composite createAddressBar(Composite parent) {
		List<List<Action>> actions = createToolbarActions();

		IFolder[] path = getPath(context.getInput(IFolder.class, true));

		addressBar = new AddressBar(parent, path, actions, this::checkActionAuthorityOfCurrentFolder);
		addressBar.addListener(SWT.SetData, e -> {
			PathActionEvent ae = (PathActionEvent) e;
			String msg = Stream.of(ae.path).map(f -> f.getName() + "/").reduce((s1, s2) -> s1 + s2).orElse("");
			logger.debug("地址栏目录加载:" + msg);
		});

		addressBar.addListener(SWT.Modify, e -> {
			PathActionEvent ae = (PathActionEvent) e;
			e.doit = doPathModified(ae.path);// 必须设置为true,才能改变地址栏。默认为true, 当某些文件夹禁止访问时，可设置为false;
		});

		addressBar.addListener(SWT.Selection, e -> {
			PathActionEvent ae = (PathActionEvent) e;
			String msg = Stream.of(ae.path).map(f -> f.getName() + "/").reduce((s1, s2) -> s1 + s2).orElse("");
			logger.debug("地址栏工具栏事件: " + ae.action + ", 路径：" + msg);
		});
		return addressBar;
	}

	protected abstract IFolder[] getPath(IFolder folder);

	private List<List<Action>> createToolbarActions() {
		List<List<Action>> result = new ArrayList<>();

		List<Action> actions = new ArrayList<Action>();
		actions.add(VaultActions.create(VaultActions.createSubFolder, true, true));
		actions.add(VaultActions.create(VaultActions.createDocument, true, true));
		result.add(actions);

		actions = new ArrayList<Action>();
		actions.add(VaultActions.create(VaultActions.findSubFolder, true, true));
		actions.add(VaultActions.create(VaultActions.findDocuments, true, true));
		actions.add(VaultActions.create(VaultActions.search, true, false));
		result.add(actions);

		actions = new ArrayList<Action>();
		actions.add(VaultActions.create(VaultActions.sortDocuments, true, false));
		actions.add(VaultActions.create(VaultActions.addFavour, true, false));
		actions.add(VaultActions.create(VaultActions.setFolderProperties, true, false));
		result.add(actions);
		return result;
	}

	/**
	 * 创建文件pane
	 * 
	 * @param parent
	 * @return
	 */
	private Composite createFilePane(Composite parent) {
		// 创建文件组件
		AssemblyContainer right = new AssemblyContainer(parent, context).setAssembly(br.getAssembly("vault/资料库文件列表.gridassy"))
				.setServices(br).create();

		filePane = (GridPart) right.getContext().getContent();
		// // 增加文件夹选择时的侦听，选择后修改文件组件查询
		// folderPane.getViewer()
		// .addPostSelectionChangedListener(e -> selectFolderQueryFile((IFolder)
		// e.getStructuredSelection().getFirstElement()));
		// selectFolderQueryFile(null);
		return right.getContainer();
	}

	/**
	 * 创建文件夹pane
	 * 
	 * @param parent
	 * @return
	 */
	private Composite createNaviPane(Composite parent) {
		// 创建文件夹组件
		Assembly assy = getNavigatorAssembly();
		// 对组件样式修改
		assy.setGridPageControlStyle("SHORT");
		// 加载组件
		AssemblyContainer ac = new AssemblyContainer(parent, context)//
				.setAssembly(assy)//
				.setServices(br)//
				.setInput(context.getInput(IFolder.class, true))//
				.create();
		contextNavi = ac.getContext();
		return ac.getContainer();
	}

	protected abstract Assembly getNavigatorAssembly();

	/**
	 * 
	 * @param a
	 * @return
	 */
	private boolean checkActionAuthorityOfCurrentFolder(Action a) {
		return checkFolderAuthority(context.getInput(IFolder.class, false), a.getName());
	}

	private boolean checkFolderAuthority(IFolder input, String actionName) {
		// TODO 检查权限
		// TODO Auto-generated method stub
		return true;
	}

	private boolean doPathModified(IFolder[] path) {
		IFolder folder;
		if (Check.isNotAssigned(path)) {
			folder = getInitialFolder();
		} else {
			folder = path[path.length - 1];
		}
		context.setInput(folder);
		contextNavi.setInput(folder);
		GridPart navi = (GridPart) contextNavi.getContent();
		navi.doRefresh();

		// TODO 检查权限？是否可以浏览本目录
		return true;
	}

	private void doSetCurrentFolder(IFolder folder) {
		context.setInput(folder);
		contextNavi.setInput(folder);
		IFolder[] path = getPath(folder);
		addressBar.setPath(path);
		GridPart navi = (GridPart) contextNavi.getContent();
		navi.doRefresh();
		// TODO 其他上下文的input设置

	}

	private void doDeleteFolder(IFolder folder) {
		// TODO Auto-generated method stub
		logger.debug("doDeleteFolder:" + folder);

	}

	private void doMoveFolder(IFolder folder) {
		// TODO Auto-generated method stub
		logger.debug("doMoveFolder:" + folder);

	}

	private void doRenameFolder(IFolder folder) {
		logger.debug("doRenameFolder:" + folder);
	}

	public void handleAction(IFolder folder, Action action) {
		boolean authorized = checkFolderAuthority(folder, action.getName());
		if (!authorized)
			return;

		String name = action.getName();
		if (VaultActions.openFolder.name().equals(name)) {
			doSetCurrentFolder(folder);
		} else if (VaultActions.deleteFolder.name().equals(name)) {
			doDeleteFolder(folder);
		} else if (VaultActions.moveFolder.name().equals(name)) {
			doMoveFolder(folder);
		} else if (VaultActions.renameFolder.name().equals(name)) {
			doRenameFolder(folder);
		}

	}

	public boolean enableAction(IFolder folder, String actionName) {
		return checkFolderAuthority(folder, actionName);
	}
}
