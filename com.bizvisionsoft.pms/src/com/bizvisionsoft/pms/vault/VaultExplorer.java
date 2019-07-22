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
		// ��������������
		StickerTitlebar bar = createBar(parent);

		FormData fd = new FormData();
		bar.setLayoutData(fd);
		fd.left = new FormAttachment(0);
		fd.top = new FormAttachment(0);
		fd.right = new FormAttachment(100);
		fd.height = 48;

		Composite content = Controls.contentPanel(parent).mLoc().mTop(bar).layout(new FormLayout()).get();

		// ��������ļ���pane
		Composite left = createFolderPane(content);

		Label sep = new Label(content, SWT.SEPARATOR | SWT.VERTICAL);

		// �����Ҳ��ļ�pane
		Composite right = createFilePane(content);

		// TODO �����ļ��е�ַ��button��ť

		// TODO �����ļ��в�ѯ��ť

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
	 * ��������������
	 * 
	 * @param parent
	 * @return
	 */
	private StickerTitlebar createBar(Composite parent) {
		StickerTitlebar bar = new StickerTitlebar(parent, null,
				Arrays.asList(new ActionFactory().name("��ѯ").image("/img/search_w.svg").tooltips("��ѯ��Ŀ�ĵ�").infoStyle().get()))
						.setActions(context.getAssembly().getActions()).setText(context.getAssembly().getTitle());
		bar.addListener(SWT.Selection, l -> {
			filePane.openQueryEditor();
		});
		return bar;
	}

	/**
	 * �����ļ�pane
	 * 
	 * @param parent
	 * @return
	 */
	private Composite createFilePane(Composite parent) {
		// �����ļ����
		AssemblyContainer right = new AssemblyContainer(parent, context).setAssembly(br.getAssembly(getFileGridassy())).setServices(br)
				.create();

		filePane = (GridPart) right.getContext().getContent();
		// �����ļ���ѡ��ʱ��������ѡ����޸��ļ������ѯ
		folderPane.getViewer()
				.addPostSelectionChangedListener(e -> selectFolderQueryFile((IFolder) e.getStructuredSelection().getFirstElement()));
		selectFolderQueryFile(null);
		return right.getContainer();
	}

	/**
	 * ��ѯ�ļ����µ��ļ�
	 * 
	 * @param folder
	 *            ��ѡ�ļ��У�����Ϊnull
	 */
	private void selectFolderQueryFile(IFolder folder) {
		BasicDBObject query = getFileQuery(folder);
		// TODO ���Ȩ�޿���

		filePane.doQuery(query);
	}

	/**
	 * �����ļ���pane
	 * 
	 * @param parent
	 * @return
	 */
	private Composite createFolderPane(Composite parent) {
		// �����ļ������
		AssemblyContainer left = new AssemblyContainer(parent, context).setAssembly(br.getAssembly(getFolderGridassy())).setServices(br)
				.create();

		folderPane = (GridPart) left.getContext().getContent();
		// ����ļ���ѡ��������������open��ť�������ļ��в�����
		folderPane.getViewer().getControl().addListener(SWT.Selection, e -> {
			if ("open/".equals(e.text)) {
				IFolder parentFolder = (IFolder) e.item.getData();
				folderPane.getViewer().setSelection(new StructuredSelection(parentFolder));
				showMenu(parentFolder);
			}
		});
		// ������˫������¼���˫���󣬴򿪸��ļ��У���ʾ���µ��ļ���
		folderPane.getViewer().getControl().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				IStructuredSelection s = (IStructuredSelection) folderPane.getViewer().getSelection();
				IFolder parentFolder = Optional.ofNullable((IFolder) s.getFirstElement()).orElse(null);
				selectFolderQueryFolder(parentFolder);
			}
		});

		// ��ѯ�ļ��У�Ĭ��û���κ�ѡ��
		selectFolderQueryFolder(null);
		return left.getContainer();
	}

	/**
	 * ��ѯ�ļ����µ��ļ���
	 * 
	 * @param folder
	 *            ��ѡ�ļ��У�����Ϊnull
	 */
	protected void selectFolderQueryFolder(IFolder folder) {
		// TODO ������Ŀ����֯����ʾ�ǲ�һ�µģ���Ҫ��������
		BasicDBObject query = getFolderQuery(folder);
		// TODO ���Ȩ�޿���

		folderPane.doQuery(query);
	}

	/**
	 * ��ʾ�ļ��в�����ť
	 * 
	 * @param folder
	 */
	private void showMenu(final IFolder folder) {
		// TODO Ȩ�޿���
		List<Action> folderMenu = new ArrayList<Action>();
		// ͨ���ĵ�
		folderMenu.add(new ActionFactory().text("����ͨ���ĵ�").name("createFile").img("/img/file_add_w.svg").normalStyle().exec((e, c) -> {
			Docu docu = br.newInstance(Docu.class).setFolder_id(folder.get_id()).setCreationInfo(br.operationInfo());
			Editor.open("ͨ���ĵ��༭��.editorassy", context, docu, true, (b, t) -> {
				filePane.insert(Services.get(DocumentService.class).createDocument(t, br.getDomain()));
			});
		}).get());
		// ���ݱ����崴���ĵ�
		// �ж������Ƿ���ڱ�����
		List<FormDef> containerFormDefs = folder.getContainerFormDefs();
		if (Check.isAssigned(containerFormDefs))
			folderMenu.add(new ActionFactory().text("������").name("createFile").img("/img/file_add_w.svg").normalStyle().exec((e, c) -> {
				// TODO ����ѡ����ѡ�б�����
				Selector.open("/vault/������ѡ����.selectorassy", context, folder.getContainer(), l->{
					// TODO ������ѡ�ı������ʼ��Document���ñ������ж���ı༭���򿪱༭
					// Docu docu =
					// br.newInstance(Docu.class).setFolder_id(folder.get_id()).setCreationInfo(br.operationInfo());
					// Editor.open("ͨ���ĵ��༭��.editorassy", context, docu, true, (b, t) -> {
					// filePane.insert(Services.get(DocumentService.class).createDocument(t,
					// br.getDomain()));
					// });
				});
				
			}).get());
		// �������ļ���
		folderMenu.add(new ActionFactory().text("�������ļ���").name("createFolder").img("/img/folder_add_w.svg").normalStyle().exec((e, c) -> {
			if (createFolder(folder)) {
				folderPane.refresh(folder);
				folderPane.getViewer().expandToLevel(folder, 1);
			}
		}).get());
		// �ļ��и���
		folderMenu.add(new ActionFactory().text("�ļ��и���").name("renameFolder").img("/img/folder_rename_w.svg").normalStyle().exec((e, c) -> {
			if (renameFolder(folder)) {
				folderPane.update(folder);
			}
		}).get());
		// ɾ���ļ���
		folderMenu.add(new ActionFactory().text("ɾ���ļ���").name("deleteFolder").img("/img/delete_w.svg").warningStyle().exec((e, c) -> {
			if (deleteFolder(folder)) {
				folderPane.remove(folder);
			}
		}).get());
		new ActionMenu(br).setActions(folderMenu).open();
	}

	/**
	 * �����ļ���
	 * 
	 * @param parentFolder
	 * @return
	 */
	private boolean createFolder(IFolder parentFolder) {
		InputDialog id = new InputDialog(br.getCurrentShell(), "�����ļ���", "�ļ�������", null, t -> {
			return t.trim().isEmpty() ? "����������" : null;
		});
		if (InputDialog.OK == id.open()) {
			return doCreateFolder(parentFolder, id.getValue());
		}
		return false;
	}

	/**
	 * ɾ���ļ���
	 * 
	 * @param folder
	 * @return
	 */
	private boolean deleteFolder(IFolder folder) {
		if (br.confirm("ɾ���ļ���", "��ȷ��ɾ���ļ���<span style='color:red;font-weight:bold;'>" + folder
				+ "</span>���ļ����Լ��¼��ĵ�������ɾ�����ò���<span style='color:red;font-weight:bold;'>���ɻָ�</span>��")) {
			return doDeleteFolder(folder);
		}
		return false;
	}

	/**
	 * �޸��ļ�������
	 * 
	 * @param folder
	 * @return
	 */
	private boolean renameFolder(IFolder folder) {
		InputDialog id = new InputDialog(br.getCurrentShell(), "�ļ��и���", "�µ�����", null, t -> {
			return t.trim().isEmpty() ? "����������" : null;
		});
		if (InputDialog.OK == id.open()) {
			return doRenameFolder(folder, id.getValue());
		}
		return false;
	}

	protected String getFolderGridassy() {
		return "vault\\���Ͽ��ļ���.gridassy";
	}

	protected String getFileGridassy() {
		return "vault\\���Ͽ��ļ��б�.gridassy";
	}

	protected abstract BasicDBObject getFileQuery(IFolder folder);

	protected abstract BasicDBObject getFolderQuery(IFolder folder);

	protected abstract boolean doCreateFolder(IFolder parentFolder, String value);

	protected abstract boolean doDeleteFolder(IFolder folder);

	protected abstract boolean doRenameFolder(IFolder folder, String value);

}
