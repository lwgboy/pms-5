package com.bizvisionsoft.pms.vault;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
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

	public void createUI(Composite parent) {
		parent.setLayout(new FormLayout());
		// 创建顶部工具栏
		Composite bar = createBar(parent);

		FormData fd = new FormData();
		bar.setLayoutData(fd);
		fd.left = new FormAttachment(0);
		fd.top = new FormAttachment(0);
		fd.right = new FormAttachment(100);
		fd.height = 32;

		Composite content = Controls.contentPanel(parent).mLoc().mTop(bar).layout(new FormLayout()).get();

		// 创建左侧文件夹pane
		Composite left = createFolderPane(content);

		Label sep = new Label(content, SWT.SEPARATOR | SWT.VERTICAL);

		// 创建右侧文件pane
		Composite right = createFilePane(content);

		fd = new FormData();
		left.setLayoutData(fd);
		fd.left = new FormAttachment();
		fd.top = new FormAttachment();
		fd.right = new FormAttachment(20);
		fd.bottom = new FormAttachment(100);

		fd = new FormData();
		sep.setLayoutData(fd);
		fd.left = new FormAttachment(left);
		fd.top = new FormAttachment();
		fd.bottom = new FormAttachment(100);
		fd.width = 1;

		fd = new FormData();
		right.setLayoutData(fd);
		fd.left = new FormAttachment(sep);
		fd.top = new FormAttachment();
		fd.right = new FormAttachment(100);
		fd.bottom = new FormAttachment(100);
	}

	/**
	 * 创建顶部工具栏
	 * 
	 * @param parent
	 * @return
	 */
	private Composite createBar(Composite parent) {
		List<List<Action>> actions = createToolbarActions();
		currentPath = createDemoPath();
		Composite bar = new AddressBar(parent, currentPath, actions);
		bar.addListener(SWT.SetData, e -> {
			PathActionEvent ae = (PathActionEvent) e;
			String msg = Stream.of(ae.path).map(f -> f.getName() + "/").reduce((s1, s2) -> s1 + s2).orElse("");
			logger.debug("地址栏目录加载:" + msg);
		});

		bar.addListener(SWT.Modify, e -> {
			PathActionEvent ae = (PathActionEvent) e;
			String msg = Stream.of(ae.path).map(f -> f.getName() + "/").reduce((s1, s2) -> s1 + s2).orElse("");
			logger.debug("地址栏目录更改:" + msg);
			e.doit = true;// 必须设置为true,才能改变地址栏。默认为true, 当某些文件夹禁止访问时，可设置为false;
		});

		bar.addListener(SWT.Selection, e -> {
			PathActionEvent ae = (PathActionEvent) e;
			String msg = Stream.of(ae.path).map(f -> f.getName() + "/").reduce((s1, s2) -> s1 + s2).orElse("");
			logger.debug("地址栏工具栏事件: " + ae.action + ", 路径：" + msg);
		});
		return bar;
	}

	private IFolder[] createDemoPath() {
		ArrayList<IFolder> path = new ArrayList<IFolder>();
		VaultFolder folder = new VaultFolder();
		folder.setDesc("TX0000中文计算0001");
		path.add(folder);

		folder = new VaultFolder();
		folder.setDesc("TX0中文计算0000002");
		path.add(folder);

		folder = new VaultFolder();
		folder.setDesc("TX00中文计算000003");
		path.add(folder);

		folder = new VaultFolder();
		folder.setDesc("TX000中文计算00004");
		path.add(folder);

		folder = new VaultFolder();
		folder.setDesc("TX00中文计算000005");
		path.add(folder);

		folder = new VaultFolder();
		folder.setDesc("TX000中文计算00006");
		path.add(folder);

		folder = new VaultFolder();
		folder.setDesc("TX00中文计算000007");
		path.add(folder);

		folder = new VaultFolder();
		folder.setDesc("TX0000中文计算0008");
		path.add(folder);

		folder = new VaultFolder();
		folder.setDesc("TX000中文计算00009");
		path.add(folder);
		folder = new VaultFolder();
		folder.setDesc("TX00中文计算000010");
		path.add(folder);
		folder = new VaultFolder();
		folder.setDesc("TX00000011");
		path.add(folder);
		folder = new VaultFolder();
		folder.setDesc("TX00000012");
		path.add(folder);
		folder = new VaultFolder();
		folder.setDesc("TX00000013");
		path.add(folder);
		folder = new VaultFolder();
		folder.setDesc("TX0000中文计算0014");
		path.add(folder);
		folder = new VaultFolder();
		folder.setDesc("TX00000015");
		path.add(folder);
		folder = new VaultFolder();
		folder.setDesc("TX00000016");
		path.add(folder);
		folder = new VaultFolder();
		folder.setDesc("TX0000中文计算0017");
		path.add(folder);
		folder = new VaultFolder();
		folder.setDesc("TX00000018");
		path.add(folder);
		folder = new VaultFolder();
		folder.setDesc("TX000中文计算00019");
		path.add(folder);

		return path.toArray(new IFolder[0]);
	}

	private List<List<Action>> createToolbarActions() {

		List<List<Action>> result = new ArrayList<>();
		// TODO 权限控制，基础控制
		// TODO 操作

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
		// 全局查找

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
		AssemblyContainer right = new AssemblyContainer(parent, context).setAssembly(br.getAssembly("vault\\资料库文件列表.gridassy"))
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
	private Composite createFolderPane(Composite parent) {
		// 创建文件夹组件
		AssemblyContainer left = new AssemblyContainer(parent, context).setAssembly(br.getAssembly("vault\\资料库文件夹.gridassy"))
				.setServices(br).create();

		folderPane = (GridPart) left.getContext().getContent();
		// 添加文件夹选择的侦听，点击后open按钮，弹出文件夹操作。
		// folderPane.getViewer().getControl().addListener(SWT.Selection, e -> {
		// if ("open/".equals(e.text)) {
		// IFolder parentFolder = (IFolder) e.item.getData();
		// folderPane.getViewer().setSelection(new StructuredSelection(parentFolder));
		// showMenu(parentFolder);
		// }
		// });
		// 添加鼠标双击点击事件，双击后，打开该文件夹，显示其下的文件夹
		// folderPane.getViewer().getControl().addMouseListener(new MouseAdapter() {
		// @Override
		// public void mouseDoubleClick(MouseEvent e) {
		// IStructuredSelection s = (IStructuredSelection)
		// folderPane.getViewer().getSelection();
		// IFolder parentFolder = Optional.ofNullable((IFolder)
		// s.getFirstElement()).orElse(null);
		// selectFolderQueryFolder(parentFolder);
		// }
		// });

		// 查询文件夹，默认没有任何选中
		// selectFolderQueryFolder(null);
		return left.getContainer();
	}
}
