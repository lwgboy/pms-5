package com.bizvisionsoft.pms.vault;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.bizvisionsoft.bruicommons.factory.action.ActionFactory;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.AssemblyContainer;
import com.bizvisionsoft.bruiengine.util.Controls;

public abstract class VaultExplorer {

	private IBruiService br;

	private BruiAssemblyContext context;

	protected GridPart folderPane;

	protected GridPart filePane;

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
		List<Action> rightActions = getRightActions();
		AddressTitleBar bar = new AddressTitleBar(parent, rightActions, br, context);
		return bar;
	}

	private List<Action> getRightActions() {
		List<Action> actions = new ArrayList<Action>();

		// TODO 权限控制，基础控制
		// TODO 操作
		actions.add(new ActionFactory().text("新建文件夹").normalStyle().get());
		actions.add(new ActionFactory().text("新建文档").normalStyle().get());
		actions.add(new ActionFactory().text("查找文件夹").infoStyle().get());
		actions.add(new ActionFactory().text("查找文档").infoStyle().get());
		actions.add(new ActionFactory().text("全局查找").infoStyle().get());
		actions.add(new ActionFactory().text("属性查找").infoStyle().get());
		actions.add(new ActionFactory().img("/img/sort_w.svg").infoStyle().get());

		return actions;
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
