package com.bizvisionsoft.pms.vault;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.bizvisionsoft.bruicommons.factory.action.ActionFactory;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.assembly.StickerTitlebar;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.ActionMenu;
import com.bizvisionsoft.bruiengine.ui.AssemblyContainer;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.bruiengine.ui.Selector;
import com.bizvisionsoft.bruiengine.util.Controls;
import com.bizvisionsoft.service.DocumentService;
import com.bizvisionsoft.service.model.Docu;
import com.bizvisionsoft.service.model.FormDef;
import com.bizvisionsoft.service.model.IFolder;
import com.bizvisionsoft.service.tools.Check;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

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
		StickerTitlebar bar = createBar(parent);

		FormData fd = new FormData();
		bar.setLayoutData(fd);
		fd.left = new FormAttachment(0);
		fd.top = new FormAttachment(0);
		fd.right = new FormAttachment(100);
		fd.height = 48;

		Composite content = Controls.contentPanel(parent).mLoc().mTop(bar).layout(new FormLayout()).get();

		// 创建左侧文件夹pane
		Composite left = createFolderPane(content);

		Label sep = new Label(content, SWT.SEPARATOR | SWT.VERTICAL);

		// 创建右侧文件pane
		Composite right = createFilePane(content);

		// TODO 创建文件夹地址的button按钮

		// TODO 创建文件夹查询按钮

		fd = new FormData();
		left.setLayoutData(fd);
		fd.left = new FormAttachment();
		fd.top = new FormAttachment();
		fd.right = new FormAttachment(30);
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
	private StickerTitlebar createBar(Composite parent) {
		StickerTitlebar bar = new StickerTitlebar(parent, null,
				Arrays.asList(new ActionFactory().name("查询").image("/img/search_w.svg").tooltips("查询项目文档").infoStyle().get()))
						.setActions(context.getAssembly().getActions()).setText(context.getAssembly().getTitle());
		bar.addListener(SWT.Selection, l -> {
			filePane.openQueryEditor();
		});
		return bar;
	}

	/**
	 * 创建文件pane
	 * 
	 * @param parent
	 * @return
	 */
	private Composite createFilePane(Composite parent) {
		// 创建文件组件
		AssemblyContainer right = new AssemblyContainer(parent, context).setAssembly(br.getAssembly(getFileGridassy())).setServices(br)
				.create();

		filePane = (GridPart) right.getContext().getContent();
		// 增加文件夹选择时的侦听，选择后修改文件组件查询
		folderPane.getViewer()
				.addPostSelectionChangedListener(e -> selectFolderQueryFile((IFolder) e.getStructuredSelection().getFirstElement()));
		selectFolderQueryFile(null);
		return right.getContainer();
	}

	/**
	 * 查询文件夹下的文件
	 * 
	 * @param folder
	 *            所选文件夹，允许为null
	 */
	private void selectFolderQueryFile(IFolder folder) {
		BasicDBObject query = getFileQuery(folder);
		// TODO 添加权限控制

		filePane.doQuery(query);
	}

	/**
	 * 创建文件夹pane
	 * 
	 * @param parent
	 * @return
	 */
	private Composite createFolderPane(Composite parent) {
		// 创建文件夹组件
		AssemblyContainer left = new AssemblyContainer(parent, context).setAssembly(br.getAssembly(getFolderGridassy())).setServices(br)
				.create();

		folderPane = (GridPart) left.getContext().getContent();
		// 添加文件夹选择的侦听，点击后open按钮，弹出文件夹操作。
		folderPane.getViewer().getControl().addListener(SWT.Selection, e -> {
			if ("open/".equals(e.text)) {
				IFolder parentFolder = (IFolder) e.item.getData();
				folderPane.getViewer().setSelection(new StructuredSelection(parentFolder));
				showMenu(parentFolder);
			}
		});
		// 添加鼠标双击点击事件，双击后，打开该文件夹，显示其下的文件夹
		folderPane.getViewer().getControl().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				IStructuredSelection s = (IStructuredSelection) folderPane.getViewer().getSelection();
				IFolder parentFolder = Optional.ofNullable((IFolder) s.getFirstElement()).orElse(null);
				selectFolderQueryFolder(parentFolder);
			}
		});

		// 查询文件夹，默认没有任何选中
		selectFolderQueryFolder(null);
		return left.getContainer();
	}

	/**
	 * 查询文件夹下的文件夹
	 * 
	 * @param folder
	 *            所选文件夹，允许为null
	 */
	protected void selectFolderQueryFolder(IFolder folder) {
		// TODO 现在项目和组织的显示是不一致的，需要进行整合
		BasicDBObject query = getFolderQuery(folder);
		// TODO 添加权限控制

		folderPane.doQuery(query);
	}

	/**
	 * 显示文件夹操作按钮
	 * 
	 * @param folder
	 */
	private void showMenu(final IFolder folder) {
		// TODO 权限控制
		List<Action> folderMenu = new ArrayList<Action>();
		// 通用文档
		folderMenu.add(new ActionFactory().text("创建通用文档").name("createFile").img("/img/file_add_w.svg").normalStyle().exec((e, c) -> {
			Docu docu = br.newInstance(Docu.class).setFolder_id(folder.get_id()).setCreationInfo(br.operationInfo());
			Editor.open("通用文档编辑器.editorassy", context, docu, true, (b, t) -> {
				filePane.insert(Services.get(DocumentService.class).createDocument(t, br.getDomain()));
			});
		}).get());
		// 根据表单定义创建文档
		// 判断容器是否存在表单定义
		List<FormDef> containerFormDefs = folder.getContainerFormDefs();
		if (Check.isAssigned(containerFormDefs))
			folderMenu.add(new ActionFactory().text("创建表单").name("createFile").img("/img/file_add_w.svg").normalStyle().exec((e, c) -> {
				// TODO 弹出选择器选中表单定义
				Selector.open("/vault/表单定义选择器.selectorassy", context, folder.getContainer(), l->{
					// TODO 根据所选的表单定义初始化Document并用表单定义中定义的编辑器打开编辑
					// Docu docu =
					// br.newInstance(Docu.class).setFolder_id(folder.get_id()).setCreationInfo(br.operationInfo());
					// Editor.open("通用文档编辑器.editorassy", context, docu, true, (b, t) -> {
					// filePane.insert(Services.get(DocumentService.class).createDocument(t,
					// br.getDomain()));
					// });
				});
				
			}).get());
		// 创建子文件夹
		folderMenu.add(new ActionFactory().text("创建子文件夹").name("createFolder").img("/img/folder_add_w.svg").normalStyle().exec((e, c) -> {
			if (createFolder(folder)) {
				folderPane.refresh(folder);
				folderPane.getViewer().expandToLevel(folder, 1);
			}
		}).get());
		// 文件夹更名
		folderMenu.add(new ActionFactory().text("文件夹更名").name("renameFolder").img("/img/folder_rename_w.svg").normalStyle().exec((e, c) -> {
			if (renameFolder(folder)) {
				folderPane.update(folder);
			}
		}).get());
		// 删除文件夹
		folderMenu.add(new ActionFactory().text("删除文件夹").name("deleteFolder").img("/img/delete_w.svg").warningStyle().exec((e, c) -> {
			if (deleteFolder(folder)) {
				folderPane.remove(folder);
			}
		}).get());
		new ActionMenu(br).setActions(folderMenu).open();
	}

	/**
	 * 创建文件夹
	 * 
	 * @param parentFolder
	 * @return
	 */
	private boolean createFolder(IFolder parentFolder) {
		InputDialog id = new InputDialog(br.getCurrentShell(), "创建文件夹", "文件夹名称", null, t -> {
			return t.trim().isEmpty() ? "请输入名称" : null;
		});
		if (InputDialog.OK == id.open()) {
			return doCreateFolder(parentFolder, id.getValue());
		}
		return false;
	}

	/**
	 * 删除文件夹
	 * 
	 * @param folder
	 * @return
	 */
	private boolean deleteFolder(IFolder folder) {
		if (br.confirm("删除文件夹", "请确认删除文件夹<span style='color:red;font-weight:bold;'>" + folder
				+ "</span>，文件夹以及下级文档都将被删除。该操作<span style='color:red;font-weight:bold;'>不可恢复</span>。")) {
			return doDeleteFolder(folder);
		}
		return false;
	}

	/**
	 * 修改文件夹名称
	 * 
	 * @param folder
	 * @return
	 */
	private boolean renameFolder(IFolder folder) {
		InputDialog id = new InputDialog(br.getCurrentShell(), "文件夹更名", "新的名称", null, t -> {
			return t.trim().isEmpty() ? "请输入名称" : null;
		});
		if (InputDialog.OK == id.open()) {
			return doRenameFolder(folder, id.getValue());
		}
		return false;
	}

	protected String getFolderGridassy() {
		return "vault\\资料库文件夹.gridassy";
	}

	protected String getFileGridassy() {
		return "vault\\资料库文件列表.gridassy";
	}

	protected abstract BasicDBObject getFileQuery(IFolder folder);

	protected abstract BasicDBObject getFolderQuery(IFolder folder);

	protected abstract boolean doCreateFolder(IFolder parentFolder, String value);

	protected abstract boolean doDeleteFolder(IFolder folder);

	protected abstract boolean doRenameFolder(IFolder folder, String value);

}
