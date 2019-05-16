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
		AssemblyContainer right = new AssemblyContainer(parent, context).setAssembly(br.getAssembly("��Ŀ�������ļ��б�"))
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
		left = new AssemblyContainer(parent, context).setAssembly(br.getAssembly("��Ŀ�������ļ���")).setServices(br)
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
		// ��ʾ��Դѡ���
		Action a1 = new Action();
		a1.setName("createFile");
		a1.setText("�����ĵ�");
		a1.setImage("/img/file_add_w.svg");
		a1.setStyle("normal");

		Action a2 = new Action();
		a2.setName("createFolder");
		a2.setText("�������ļ���");
		a2.setImage("/img/folder_add_w.svg");
		a2.setStyle("normal");

		Action a3 = new Action();
		a3.setName("renameFolder");
		a3.setText("�ļ��и���");
		a3.setImage("/img/folder_rename_w.svg");
		a3.setStyle("normal");

		Action a4 = new Action();
		a4.setName("deleteFolder");
		a4.setText("ɾ���ļ���");
		a4.setImage("/img/delete_w.svg");
		a4.setStyle("warning");

		// ����menu
		new ActionMenu(br).setActions(Arrays.asList(a1, a2, a3, a4)).handleActionExecute("createFile", a -> {
			Docu docu = new Docu().setFolder_id(folder.get_id()).setCreationInfo(br.operationInfo());
			Editor.open("ͨ���ĵ��༭��", context, docu, true, (b, t) -> {
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
		a.setName("������Ŀ���ļ���");
		a.setImage("/img/add_16_w.svg");
		a.setTooltips("������Ŀ���ļ���");
		a.setStyle("normal");

		Action b = new Action();
		b.setName("��ѯ");
		b.setImage("/img/search_w.svg");
		b.setTooltips("��ѯ��Ŀ�ĵ�");
		b.setStyle("info");

		StickerTitlebar bar = new StickerTitlebar(parent, null, Arrays.asList(a, b))
				.setActions(context.getAssembly().getActions()).setText(context.getAssembly().getTitle());
		bar.addListener(SWT.Selection, l -> {
			if ("������Ŀ���ļ���".equals(((Action) l.data).getName())) {
				if (createFolder(null)) {
					folderPane.setViewerInput(getInput());
				}
			} else if ("��ѯ".equals(((Action) l.data).getName())) {
				filePane.openQueryEditor();
			}
		});
		return bar;
	}

	private boolean createFolder(IFolder parentFolder) {
		InputDialog id = new InputDialog(br.getCurrentShell(), "�����ļ���", "�ļ�������", null, t -> {
			return t.trim().isEmpty() ? "����������" : null;
		});
		if (InputDialog.OK == id.open()) {
			return doCreateFolder(parentFolder, id.getValue());
		}
		return false;
	}

	private boolean deleteFolder(IFolder folder) {
		if (br.confirm("ɾ���ļ���", "��ȷ��ɾ���ļ���<span style='color:red;font-weight:bold;'>" + folder
				+ "</span>���ļ����Լ��¼��ĵ�������ɾ�����ò���<span style='color:red;font-weight:bold;'>���ɻָ�</span>��")) {
			return doDeleteFolder(folder);
		}
		return false;
	}

	private boolean renameFolder(IFolder folder) {
		InputDialog id = new InputDialog(br.getCurrentShell(), "�ļ��и���", "�µ�����", null, t -> {
			return t.trim().isEmpty() ? "����������" : null;
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
