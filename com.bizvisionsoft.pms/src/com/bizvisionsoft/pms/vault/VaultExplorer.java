package com.bizvisionsoft.pms.vault;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bizvisionsoft.bruicommons.factory.action.ActionFactory;
import com.bizvisionsoft.bruicommons.model.Action;
import com.bizvisionsoft.bruiengine.assembly.GridPart;
import com.bizvisionsoft.bruiengine.service.BruiAssemblyContext;
import com.bizvisionsoft.bruiengine.service.IBruiService;
import com.bizvisionsoft.bruiengine.ui.AssemblyContainer;
import com.bizvisionsoft.bruiengine.util.Controls;
import com.bizvisionsoft.pms.vault.AddressBar.ActionEvent;
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
		List<List<Action>> actions = createToolbarActions();
		currentPath = createDemoPath();
		Composite bar = new AddressBar(parent, currentPath, actions);
		bar.addListener(SWT.SetData, e -> {
			ActionEvent ae = (ActionEvent) e;
			String msg = Stream.of(ae.path).map(f -> f.getName() + "/").reduce((s1, s2) -> s1 + s2).orElse("");
			logger.debug("��ַ��Ŀ¼����:" + msg);
		});

		bar.addListener(SWT.Modify, e -> {
			ActionEvent ae = (ActionEvent) e;
			String msg = Stream.of(ae.path).map(f -> f.getName() + "/").reduce((s1, s2) -> s1 + s2).orElse("");
			logger.debug("��ַ��Ŀ¼����:" + msg);
			e.doit = true;// ��������Ϊtrue,���ܸı��ַ����Ĭ��Ϊtrue, ��ĳЩ�ļ��н�ֹ����ʱ��������Ϊfalse;
		});

		bar.addListener(SWT.Selection, e -> {
			ActionEvent ae = (ActionEvent) e;
			String msg = Stream.of(ae.path).map(f -> f.getName() + "/").reduce((s1, s2) -> s1 + s2).orElse("");
			logger.debug("��ַ���������¼�: " + ae.action.getText() + ", path" + msg);
		});
		return bar;
	}

	private IFolder[] createDemoPath() {
		ArrayList<IFolder> path = new ArrayList<IFolder>();
		VaultFolder folder = new VaultFolder();
		folder.setDesc("TX00000001");
		path.add(folder);

		folder = new VaultFolder();
		folder.setDesc("TX00000002");
		path.add(folder);

		folder = new VaultFolder();
		folder.setDesc("TX00000003");
		path.add(folder);
		folder = new VaultFolder();
		folder.setDesc("TX00000004");
		path.add(folder);
		folder = new VaultFolder();
		folder.setDesc("TX00000005");
		path.add(folder);
		folder = new VaultFolder();
		folder.setDesc("TX00000006");
		path.add(folder);
		folder = new VaultFolder();
		folder.setDesc("TX00000007");
		path.add(folder);
		folder = new VaultFolder();
		folder.setDesc("TX00000008");
		path.add(folder);
		folder = new VaultFolder();
		folder.setDesc("TX00000009");
		path.add(folder);
		folder = new VaultFolder();
		folder.setDesc("TX00000010");
		path.add(folder);
		folder = new VaultFolder();
		folder.setDesc("TX00000011");
		path.add(folder);
		return path.toArray(new IFolder[0]);
	}

	private List<List<Action>> createToolbarActions() {

		List<List<Action>> result = new ArrayList<>();
		// TODO Ȩ�޿��ƣ���������
		// TODO ����

		List<Action> actions = new ArrayList<Action>();
		actions.add(new ActionFactory().img("/img/line_newFolder.svg").disImg("/img/line_newFolder_disable.svg").text("�½�Ŀ¼")
				.tooltips("�ڵ�ǰĿ¼�´�����Ŀ¼").get());
		actions.add(new ActionFactory().img("/img/line_newDoc.svg").text("�½��ĵ�").disImg("/img/line_newDoc_disable.svg")
				.tooltips("�ڵ�ǰĿ¼�´����ĵ�").get());
		result.add(actions);

		actions = new ArrayList<Action>();
		actions.add(new ActionFactory().img("/img/line_searchFolder.svg").text("����Ŀ¼").tooltips("�ڵ�ǰĿ¼�²�����Ŀ¼").get());
		actions.add(new ActionFactory().img("/img/line_searchDoc.svg").text("�����ĵ�").tooltips("�ڵ�ǰ�ļ����²����ĵ�").get());
		actions.add(new ActionFactory().img("/img/line_search.svg").text("����").tooltips("�������ļ����в����ĵ�").get());
		result.add(actions);

		actions = new ArrayList<Action>();
		actions.add(new ActionFactory().img("/img/line_sort.svg").text("����").tooltips("�Ե�ǰĿ¼�µ��ĵ�����").get());
		actions.add(new ActionFactory().img("/img/line_star.svg").text("�ղ�").tooltips("�ղص�ǰĿ¼").get());
		actions.add(new ActionFactory().img("/img/line_setting.svg").text("����").tooltips("���õ�ǰĿ¼������").get());
		result.add(actions);
		// ȫ�ֲ���

		return result;
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
