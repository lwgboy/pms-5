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
		// ��������������
		Composite bar = createBar(parent);

		FormData fd = new FormData();
		bar.setLayoutData(fd);
		fd.left = new FormAttachment(0);
		fd.top = new FormAttachment(0);
		fd.right = new FormAttachment(100);
		fd.height = 32;

		Composite content = Controls.contentPanel(parent).mLoc().mTop(bar).layout(new FormLayout()).get();

		// ��������ļ���pane
		Composite left = createFolderPane(content);

		Label sep = new Label(content, SWT.SEPARATOR | SWT.VERTICAL);

		// �����Ҳ��ļ�pane
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
	 * ��������������
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

		// TODO Ȩ�޿��ƣ���������
		// TODO ����
		actions.add(new ActionFactory().text("�½��ļ���").normalStyle().get());
		actions.add(new ActionFactory().text("�½��ĵ�").normalStyle().get());
		actions.add(new ActionFactory().text("�����ļ���").infoStyle().get());
		actions.add(new ActionFactory().text("�����ĵ�").infoStyle().get());
		actions.add(new ActionFactory().text("ȫ�ֲ���").infoStyle().get());
		actions.add(new ActionFactory().text("���Բ���").infoStyle().get());
		actions.add(new ActionFactory().img("/img/sort_w.svg").infoStyle().get());

		return actions;
	}

	/**
	 * �����ļ�pane
	 * 
	 * @param parent
	 * @return
	 */
	private Composite createFilePane(Composite parent) {
		// �����ļ����
		AssemblyContainer right = new AssemblyContainer(parent, context).setAssembly(br.getAssembly("vault\\���Ͽ��ļ��б�.gridassy"))
				.setServices(br).create();

		filePane = (GridPart) right.getContext().getContent();
		// // �����ļ���ѡ��ʱ��������ѡ����޸��ļ������ѯ
		// folderPane.getViewer()
		// .addPostSelectionChangedListener(e -> selectFolderQueryFile((IFolder)
		// e.getStructuredSelection().getFirstElement()));
		// selectFolderQueryFile(null);
		return right.getContainer();
	}

	/**
	 * �����ļ���pane
	 * 
	 * @param parent
	 * @return
	 */
	private Composite createFolderPane(Composite parent) {
		// �����ļ������
		AssemblyContainer left = new AssemblyContainer(parent, context).setAssembly(br.getAssembly("vault\\���Ͽ��ļ���.gridassy"))
				.setServices(br).create();

		folderPane = (GridPart) left.getContext().getContent();
		// ����ļ���ѡ��������������open��ť�������ļ��в�����
		// folderPane.getViewer().getControl().addListener(SWT.Selection, e -> {
		// if ("open/".equals(e.text)) {
		// IFolder parentFolder = (IFolder) e.item.getData();
		// folderPane.getViewer().setSelection(new StructuredSelection(parentFolder));
		// showMenu(parentFolder);
		// }
		// });
		// ������˫������¼���˫���󣬴򿪸��ļ��У���ʾ���µ��ļ���
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

		// ��ѯ�ļ��У�Ĭ��û���κ�ѡ��
		// selectFolderQueryFolder(null);
		return left.getContainer();
	}
}
