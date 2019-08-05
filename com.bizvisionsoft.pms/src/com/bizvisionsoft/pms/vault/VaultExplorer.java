package com.bizvisionsoft.pms.vault;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.bson.types.ObjectId;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.AssemblyContainer;
import com.bizvisionsoft.bruiengine.util.Controls;
import com.bizvisionsoft.service.model.IFolder;
import com.bizvisionsoft.service.model.VaultFolder;

public abstract class VaultExplorer {

	private static final Logger logger = LoggerFactory.getLogger(VaultExplorer.class);

	private IBruiService br;

	private BruiAssemblyContext context;

	protected GridPart folderPane;

	protected GridPart filePane;

	private IFolder[] currentPath;

	private AddressBar addressBar;

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
		ObjectId[] root = getRootFolder();
		context.setInput(root);
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
	public abstract ObjectId[] getRootFolder();

	/**
	 * 创建顶部工具栏
	 * 
	 * @param parent
	 * @return
	 */
	private Composite createAddressBar(Composite parent) {
		List<List<Action>> actions = createToolbarActions();
		addressBar = new AddressBar(parent, currentPath, actions, this::checkAuthority);
		addressBar.addListener(SWT.SetData, e -> {
			PathActionEvent ae = (PathActionEvent) e;
			String msg = Stream.of(ae.path).map(f -> f.getName() + "/").reduce((s1, s2) -> s1 + s2).orElse("");
			logger.debug("地址栏目录加载:" + msg);
		});

		addressBar.addListener(SWT.Modify, e -> {
			PathActionEvent ae = (PathActionEvent) e;
			String msg = Stream.of(ae.path).map(f -> f.getName() + "/").reduce((s1, s2) -> s1 + s2).orElse("");
			logger.debug("地址栏目录更改:" + msg);
			e.doit = true;// 必须设置为true,才能改变地址栏。默认为true, 当某些文件夹禁止访问时，可设置为false;
		});

		addressBar.addListener(SWT.Selection, e -> {
			PathActionEvent ae = (PathActionEvent) e;
			String msg = Stream.of(ae.path).map(f -> f.getName() + "/").reduce((s1, s2) -> s1 + s2).orElse("");
			logger.debug("地址栏工具栏事件: " + ae.action + ", 路径：" + msg);
		});
		return addressBar;
	}

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
		AssemblyContainer left = new AssemblyContainer(parent, context)//
				.setAssembly(br.getAssembly("vault/目录导航.gridassy"))//
				.setServices(br)//
				.setInput(getRootFolder())// 设置输入为当前的根目录，这个输入可以在DS中被调用
				.create();

		return left.getContainer();
	}

	/**
	 * // TODO 检查权限
	 * 
	 * @param a
	 * @return
	 */
	private boolean checkAuthority(Action a) {
		return true;
	}
}
