package com.bizvisionsoft.pms.filecabinet;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;

import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.assembly.StickerTitlebar;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.ActionMenu;
import com.bizvisionsoft.bruiengine.ui.AssemblyContainer;
import com.bizvisionsoft.bruiengine.ui.Editor;
import com.bizvisionsoft.bruiengine.util.Controls;
import com.bizvisionsoft.service.DocumentService;
import com.bizvisionsoft.service.model.Docu;
import com.bizvisionsoft.service.model.IFolder;
import com.bizvisionsoft.serviceconsumer.Services;
import com.mongodb.BasicDBObject;

public abstract class CabinetASM {

	private IBruiService br;

	private BruiAssemblyContext context;

	private GridPart folderPane;

	private GridPart filePane;

	public CabinetASM setContext(BruiAssemblyContext context) {
		this.context = context;
		return this;
	}

	public CabinetASM setBruiService(IBruiService brui) {
		this.br = brui;
		return this;
	}

	public void createUI(Composite parent) {
		parent.setLayout(new FormLayout());

		StickerTitlebar bar = createBar(parent);

		FormData fd = new FormData();
		bar.setLayoutData(fd);
		fd.left = new FormAttachment(0);
		fd.top = new FormAttachment(0);
		fd.right = new FormAttachment(100);
		fd.height = 48;

		Composite content = Controls.contentPanel(parent).mLoc().mTop(bar).layout(new FormLayout()).get();

		Composite left = createFolderPane(content);

		Label sep = new Label(content, SWT.SEPARATOR | SWT.VERTICAL);

		Composite right = createFilePane(content);

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

	private Composite createFilePane(Composite parent) {
		AssemblyContainer right = new AssemblyContainer(parent, context).setAssembly(br.getAssembly("项目档案库文件列表"))
				.setServices(br).create();

		filePane = (GridPart) right.getContext().getContent();
		select(null);
		folderPane.getViewer()
				.addPostSelectionChangedListener(e -> select((IFolder) e.getStructuredSelection().getFirstElement()));
		return right.getContainer();
	}

	private void select(IFolder folder) {
		if (folder != null) {
			filePane.doQuery(new BasicDBObject("folder_id", folder.get_id()));
		} else {
			filePane.doQuery(new BasicDBObject("folder_id", null));
		}
	}

	private Composite createFolderPane(Composite parent) {
		AssemblyContainer left;
		left = new AssemblyContainer(parent, context).setAssembly(br.getAssembly("项目档案库文件夹")).setServices(br)
				.create();

		folderPane = (GridPart) left.getContext().getContent();
		folderPane.getViewer().getControl().addListener(SWT.Selection, e -> {
			if ("open/".equals(e.text)) {
				IFolder parentFolder = (IFolder) e.item.getData();
				folderPane.getViewer().setSelection(new StructuredSelection(parentFolder));
				showMenu(parentFolder);
			}
		});

		folderPane.getViewer().getControl().addListener(SWT.Expand, e -> updateFolderIcon(e));
		folderPane.getViewer().getControl().addListener(SWT.Collapse, e -> updateFolderIcon(e));

		folderPane.setViewerInput(getInput());
		return left.getContainer();
	}

	private void updateFolderIcon(Event e) {
		IFolder folder = (IFolder) e.item.getData();
		GridItem item = (GridItem) e.item;
		folder.setOpened(item.isExpanded());
		folderPane.update(folder);
	}

	private void showMenu(final IFolder folder) {
		// 显示资源选择框
		Action a1 = new Action();
		a1.setName("createFile");
		a1.setText("创建文档");
		a1.setImage("/img/file_add_w.svg");
		a1.setStyle("normal");

		Action a2 = new Action();
		a2.setName("createFolder");
		a2.setText("创建子文件夹");
		a2.setImage("/img/folder_add_w.svg");
		a2.setStyle("normal");

		Action a3 = new Action();
		a3.setName("renameFolder");
		a3.setText("文件夹更名");
		a3.setImage("/img/folder_rename_w.svg");
		a3.setStyle("normal");

		Action a4 = new Action();
		a4.setName("deleteFolder");
		a4.setText("删除文件夹");
		a4.setImage("/img/delete_w.svg");
		a4.setStyle("warning");

		// 弹出menu
		new ActionMenu(br).setActions(Arrays.asList(a1, a2, a3, a4)).handleActionExecute("createFile", a -> {
			Docu docu = new Docu().setFolder_id(folder.get_id()).setCreationInfo(br.operationInfo());
			Editor.open("通用文档编辑器", context, docu, true, (b, t) -> {
				filePane.insert(Services.get(DocumentService.class).createDocument(t,br.getDomain()));
			});
			return false;
		}).handleActionExecute("renameFolder", a -> {
			if (renameFolder(folder)) {
				folderPane.update(folder);
			}
			return false;
		}).handleActionExecute("createFolder", a -> {
			if (createFolder(folder)) {
				folderPane.refresh(folder);
				folderPane.getViewer().expandToLevel(folder, 1);
			}
			return false;
		}).handleActionExecute("deleteFolder", a -> {
			if (deleteFolder(folder)) {
				folderPane.remove(folder);
			}
			return false;
		}).open();

	}

	protected abstract List<?> getInput();

	private StickerTitlebar createBar(Composite parent) {
		Action a = new Action();
		a.setName("创建项目根文件夹");
		a.setImage("/img/add_16_w.svg");
		a.setTooltips("创建项目根文件夹");
		a.setStyle("normal");

		Action b = new Action();
		b.setName("查询");
		b.setImage("/img/search_w.svg");
		b.setTooltips("查询项目文档");
		b.setStyle("info");

		StickerTitlebar bar = new StickerTitlebar(parent, null, Arrays.asList(a, b))
				.setActions(context.getAssembly().getActions()).setText(context.getAssembly().getTitle());
		bar.addListener(SWT.Selection, l -> {
			if ("创建项目根文件夹".equals(((Action) l.data).getName())) {
				if (createFolder(null)) {
					folderPane.setViewerInput(getInput());
				}
			} else if ("查询".equals(((Action) l.data).getName())) {
				filePane.openQueryEditor();
			}
		});
		return bar;
	}

	private boolean createFolder(IFolder parentFolder) {
		InputDialog id = new InputDialog(br.getCurrentShell(), "创建文件夹", "文件夹名称", null, t -> {
			return t.trim().isEmpty() ? "请输入名称" : null;
		});
		if (InputDialog.OK == id.open()) {
			return doCreateFolder(parentFolder, id.getValue());
		}
		return false;
	}

	private boolean deleteFolder(IFolder folder) {
		if (br.confirm("删除文件夹", "请确认删除文件夹<span style='color:red;font-weight:bold;'>" + folder
				+ "</span>，文件夹以及下级文档都将被删除。该操作<span style='color:red;font-weight:bold;'>不可恢复</span>。")) {
			return doDeleteFolder(folder);
		}
		return false;
	}

	private boolean renameFolder(IFolder folder) {
		InputDialog id = new InputDialog(br.getCurrentShell(), "文件夹更名", "新的名称", null, t -> {
			return t.trim().isEmpty() ? "请输入名称" : null;
		});
		if (InputDialog.OK == id.open()) {
			return doRenameFolder(folder, id.getValue());
		}
		return false;
	}

	protected abstract boolean doCreateFolder(IFolder parentFolder, String folderName);

	protected abstract boolean doDeleteFolder(IFolder folder);

	protected abstract boolean doRenameFolder(IFolder folder, String name);

}
